package com.asfoundation.wallet.ui.iab

import android.net.Uri
import android.os.Bundle
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.*
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.billing.purchase.InAppDeepLinkRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class LocalPaymentInteractor(private val deepLinkRepository: InAppDeepLinkRepository,
                             private val walletService: WalletService,
                             private val partnerAddressService: AddressService,
                             private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                             private val billing: Billing,
                             private val billingMessagesMapper: BillingMessagesMapper
) {

  fun getPaymentLink(domain: String, skuId: String?, originalAmount: String?,
                     originalCurrency: String?, paymentMethod: String, developerAddress: String,
                     callbackUrl: String?, orderReference: String?,
                     payload: String?): Single<String> {

    return walletService.getAndSignCurrentWalletAddress()
        .flatMap { walletAddressModel ->
          Single.zip(
              partnerAddressService.getStoreAddressForPackage(domain),
              partnerAddressService.getOemAddressForPackage(domain),
              BiFunction { storeAddress: String, oemAddress: String ->
                DeepLinkInformation(storeAddress, oemAddress)
              })
              .flatMap {
                deepLinkRepository.getDeepLink(domain, skuId, walletAddressModel.address,
                    walletAddressModel.signedAddress, originalAmount, originalCurrency,
                    paymentMethod, developerAddress, it.storeAddress, it.oemAddress, callbackUrl,
                    orderReference, payload)
              }
        }
  }

  fun getTransaction(uri: Uri): Observable<Transaction> =
      inAppPurchaseInteractor.getTransaction(uri.lastPathSegment)
          .filter { isEndingState(it.status, it.type) }
          .distinctUntilChanged { transaction -> transaction.status }

  private fun isEndingState(status: Transaction.Status, type: String) =
      (status == PENDING_USER_PAYMENT && type == "TOPUP") ||
          status == COMPLETED ||
          status == FAILED ||
          status == CANCELED ||
          status == INVALID_TRANSACTION

  fun getCompletePurchaseBundle(type: String, merchantName: String, sku: String?,
                                orderReference: String?, hash: String?,
                                scheduler: Scheduler): Single<Bundle> {
    return if (isValidType(type) && sku != null) {
      val billingType = BillingSupportedType.valueOfInsensitive(type)
      billing.getSkuPurchase(merchantName, sku, scheduler, billingType)
          .map { billingMessagesMapper.mapPurchase(it, orderReference) }
    } else {
      Single.just(billingMessagesMapper.successBundle(hash))
    }
  }

  private fun isValidType(type: String): Boolean {
    return type.equals("INAPP", ignoreCase = true) || type.equals("SUBS", ignoreCase = true)
  }

  fun savePreSelectedPaymentMethod(paymentMethod: String) {
    inAppPurchaseInteractor.savePreSelectedPaymentMethod(paymentMethod)
  }

  fun saveAsyncLocalPayment(paymentMethod: String) {
    inAppPurchaseInteractor.saveAsyncLocalPayment(paymentMethod)
  }

  private data class DeepLinkInformation(val storeAddress: String, val oemAddress: String)

  fun isAsync(type: String) = type == "TOPUP"
}
