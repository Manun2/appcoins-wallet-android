package cm.aptoide.skills

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import cm.aptoide.skills.entity.UserData
import cm.aptoide.skills.interfaces.PaymentView
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.*
import cm.aptoide.skills.usecase.*
import cm.aptoide.skills.util.EskillsPaymentData
import cm.aptoide.skills.util.UriValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SkillsViewModel @Inject constructor(
  private val walletAddressObtainer: WalletAddressObtainer,
  private val joinQueueUseCase: JoinQueueUseCase,
  private val getTicketUseCase: GetTicketUseCase,
  private val loginUseCase: LoginUseCase,
  private val cancelTicketUseCase: CancelTicketUseCase,
  private val payTicketUseCase: PayTicketUseCase,
  private val saveQueueIdToClipboardUseCase: SaveQueueIdToClipboardUseCase,
  private val getApplicationInfoUseCase: GetApplicationInfoUseCase,
  private val getTicketPriceUseCase: GetTicketPriceUseCase,
  private val getUserBalanceUseCase: GetUserBalanceUseCase,
  private val sendUserToTopUpFlowUseCase: SendUserToTopUpFlowUseCase,
  private val hasAuthenticationPermissionUseCase: HasAuthenticationPermissionUseCase,
  private val getAuthenticationIntentUseCase: GetAuthenticationIntentUseCase,
  private val cachePaymentUseCase: CachePaymentUseCase,
  private val getCachedPaymentUseCase: GetCachedPaymentUseCase,
  private val sendUserVerificationFlowUseCase: SendUserVerificationFlowUseCase,
  private val isWalletVerifiedUseCase: IsWalletVerifiedUseCase,
  private val validateUrlUseCase: ValidateUrlUseCase,
  private val getTopUpListStatus: GetTopUpListUseCase,
  private val getVerificationUseCase: GetVerificationUseCase,
  private val buildUpdateIntentUseCase: BuildUpdateIntentUseCase,
  private val useReferralUseCase: UseReferralUseCase,
  private val userFirstTimeCheckUseCase: UserFirstTimeCheckUseCase,
  private val buildShareReferralIntentUseCase: BuildShareReferralIntentUseCase,
  private val getReferralUseCase: GetReferralUseCase
) : ViewModel() {
  lateinit var ticketId: String
  private val closeView: PublishSubject<Pair<Int, UserData>> = PublishSubject.create()

  companion object {
    const val RESULT_OK = 0
    const val RESULT_USER_CANCELED = 1
    const val RESULT_REGION_NOT_SUPPORTED = 2
    const val RESULT_SERVICE_UNAVAILABLE = 3
    const val RESULT_ERROR = 6
    const val RESULT_INVALID_URL = 7
    const val RESULT_INVALID_USERNAME = 8
    const val RESULT_ROOT_ERROR = 9
    const val RESULT_WALLET_VERSION_ERROR = 10
    const val GET_ROOM_RETRY_MILLIS = 3000L
    const val AUTHENTICATION_REQUEST_CODE = 33
  }

  fun handleWalletCreationIfNeeded(): Observable<String> {
    return walletAddressObtainer.getOrCreateWallet()
  }

  fun joinQueue(eskillsPaymentData: EskillsPaymentData): Observable<Ticket> {
    return joinQueueUseCase(eskillsPaymentData)
      .doOnSuccess { if (it is CreatedTicket) ticketId = it.ticketId }
      .toObservable()
  }

  fun getRoom(
    eskillsPaymentData: EskillsPaymentData, ticket: CreatedTicket, view: PaymentView
  ): Observable<UserData> {
    return Single.just(ticket)
      .flatMap {
        if (ticket.processingStatus == ProcessingStatus.IN_QUEUE) {
          Single.just(it)
        } else {
          if (hasAuthenticationPermissionUseCase()) {
            Single.fromCallable {
              cachePaymentUseCase(ticket, eskillsPaymentData)
              view.showFingerprintAuthentication()
            }
          } else {
            payTicketUseCase(ticket, eskillsPaymentData)
              .observeOn(AndroidSchedulers.mainThread())
              .doOnSubscribe { view.showLoading() }
              .flatMap { paymentResult ->
                handlePaymentResultStatus(view, paymentResult, ticket)
              }
          }
        }
      }
      .flatMapObservable {
        getTicketUpdates(ticket.ticketId, eskillsPaymentData.queueId)
          .flatMap {
            return@flatMap handlePurchasedTicketStatus(it)
          }
      }
  }

  private fun handlePaymentResultStatus(
    view: PaymentView,
    paymentResult: PaymentResult,
    ticket: CreatedTicket
  ): Single<Ticket> {
    return when (paymentResult) {
      is SuccessfulPayment -> Single.fromCallable { view.hideLoading() }
      is FailedPayment.GenericError -> Single.fromCallable { view.showError(RESULT_ERROR) }
      is FailedPayment.FraudError -> isWalletVerifiedUseCase().observeOn(
        AndroidSchedulers.mainThread()
      )
        .doOnSuccess { view.showFraudError(it) }
      is FailedPayment.NoNetworkError -> Single.fromCallable { view.showNoNetworkError() }
    }.map { ticket }
  }

  private fun handlePurchasedTicketStatus(ticket: Ticket): Observable<UserData> {
    return when (ticket) {
      is CreatedTicket -> {
        return when (ticket.processingStatus) {
          ProcessingStatus.PENDING_PAYMENT -> Observable.just(
            UserData.fromStatus(UserData.Status.PAYING)
          )
          ProcessingStatus.REFUNDED -> Observable.just(
            UserData.fromStatus(UserData.Status.REFUNDED)
          )
          ProcessingStatus.IN_QUEUE, ProcessingStatus.REFUNDING -> Observable.just(
            UserData.fromStatus(UserData.Status.IN_QUEUE, ticket.queueId)
          )
        }
      }
      is PurchasedTicket -> {
        loginUseCase(ticket.roomId, ticket.ticketId)
          .map { session ->
            return@map UserData(
              ticket.userId, ticket.roomId, ticket.walletAddress, session,
              UserData.Status.COMPLETED, ticket.queueId
            )
          }
          .toObservable()
      }
      is FailedTicket -> Observable.just(UserData.fromStatus(UserData.Status.FAILED))
    }
  }

  private fun getTicketUpdates(
    ticketId: String,
    queueIdentifier: QueueIdentifier?
  ): Observable<Ticket> {
    return Observable.interval(GET_ROOM_RETRY_MILLIS, TimeUnit.MILLISECONDS)
      .switchMapSingle { getTicketUseCase(ticketId, queueIdentifier) }
  }

  fun cancelPayment() {
    closeView.onNext(
      Pair(RESULT_USER_CANCELED, UserData.fromStatus(UserData.Status.REFUNDED))
    )
  }

  fun cancelTicket(): Single<TicketResponse> {
    // only paid tickets can be canceled/refunded on the backend side, meaning that if we
    // cancel before actually paying the backend will return a 409 HTTP. this way we allow
    // users to return to the game, without crashing, even if they weren't waiting in queue
    return cancelTicketUseCase(ticketId)
      .doOnSuccess {
        closeView.onNext(
          Pair(RESULT_USER_CANCELED, UserData.fromStatus(UserData.Status.REFUNDED))
        )
      }
      .doOnError {
        closeView.onNext(
          Pair(RESULT_ERROR, UserData.fromStatus(UserData.Status.REFUNDED))
        )
      }
  }

  fun validateUrl(uriString: String): UriValidationResult {
    return validateUrlUseCase(uriString)
  }

  fun closeView(): Observable<Pair<Int, UserData>> {
    return closeView
  }

  fun saveQueueIdToClipboard(queueId: String) {
    saveQueueIdToClipboardUseCase(queueId)
  }

  fun getApplicationInfo(packageName: String): ApplicationInfo {
    return getApplicationInfoUseCase(packageName)
  }

  fun getLocalFiatAmount(value: BigDecimal, currency: String): Single<Price> {
    return getTicketPriceUseCase.getLocalPrice(value, currency)
  }

  fun getFiatToAppcAmount(value: BigDecimal, currency: String): Single<Price> {
    return getTicketPriceUseCase.getAppcPrice(value, currency)
  }

  fun getFormattedAppcAmount(value: BigDecimal, currency: String): Single<String> {
    return getTicketPriceUseCase.getAppcFormatted(value, currency)
  }

  fun getCreditsBalance(): Single<BigDecimal> {
    return getUserBalanceUseCase()
  }

  fun sendUserToTopUpFlow(context: Context) {
    sendUserToTopUpFlowUseCase(context)
  }

  fun sendUserToVerificationFlow(context: Context) {
    sendUserVerificationFlowUseCase(context)
  }

  fun getAuthenticationIntent(context: Context): Intent {
    return getAuthenticationIntentUseCase(context)
  }

  fun getTopUpListStatus(): Status {
    return getTopUpListStatus(TransactionType.TOPUP, TopUpStatus.COMPLETED).blockingGet()
  }

  fun getVerification(): EskillsVerification{
    return getVerificationUseCase().blockingGet()
  }

  fun useReferralCode(referralCode: String): ReferralResult{
    return useReferralUseCase(referralCode).blockingGet()
  }

  fun userFirstTimeCheck(): Boolean{
    return userFirstTimeCheckUseCase().blockingGet()
  }

  fun buildUpdateIntent(): Intent {
    return buildUpdateIntentUseCase()
  }

  fun buildShareIntent(referralCode: String): Intent {
    return buildShareReferralIntentUseCase(referralCode)
  }

  fun restorePurchase(view: PaymentView): Single<Ticket> {
    return walletAddressObtainer.getWalletAddress()
      .flatMap { walletAddress ->
        getCachedPaymentUseCase(walletAddress).flatMap { cachedPayment ->
          payTicketUseCase(cachedPayment.ticket, cachedPayment.eskillsPaymentData)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { view.showLoading() }
            .flatMap { paymentResult ->
              handlePaymentResultStatus(view, paymentResult, cachedPayment.ticket)
            }
        }
      }
  }

  fun getReferral(): Single<ReferralResponse> {
    return getReferralUseCase()
  }
}
