package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.base.model.payments.response.RedirectAction
import com.adyen.checkout.base.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.base.model.payments.response.Threeds2FingerprintAction
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.billing.ErrorInfo
import com.appcoins.wallet.billing.ErrorInfo.ErrorType
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.*
import com.appcoins.wallet.billing.common.response.TransactionResponse
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.appcoins.wallet.billing.repository.ResponseErrorBaseBody
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.billing.util.getErrorCodeAndMessage
import com.appcoins.wallet.billing.util.getMessage
import com.appcoins.wallet.billing.util.isNoNetworkException
import com.google.gson.Gson
import org.json.JSONObject
import retrofit2.HttpException

class AdyenResponseMapper(private val gson: Gson) {

  fun map(response: PaymentMethodsResponse,
          method: AdyenPaymentRepository.Methods): PaymentInfoModel {
    //This was done due to the fact that using the PaymentMethodsApiResponse to map the response
    // directly with retrofit was breaking when the response came with a configuration object
    // since the Adyen lib considers configuration a string.
    val adyenResponse: PaymentMethodsApiResponse =
        PaymentMethodsApiResponse.SERIALIZER.deserialize(JSONObject(response.payment.toString()))
    val storedPaymentModel =
        findPaymentMethod(adyenResponse.storedPaymentMethods, method, true, response.price)
    return if (storedPaymentModel.error.hasError) {
      findPaymentMethod(adyenResponse.paymentMethods, method, false, response.price)
    } else {
      storedPaymentModel
    }
  }

  fun map(response: AdyenTransactionResponse): PaymentModel {
    val adyenResponse = response.payment
    var actionType: String? = null
    var jsonAction: JSONObject? = null
    var redirectUrl: String? = null
    var action: Action? = null
    var fraudResultsId: List<Int> = emptyList()

    if (adyenResponse != null) {
      if (adyenResponse.fraudResult != null) {
        fraudResultsId = adyenResponse.fraudResult.results.map { it.fraudCheckResult.checkId }
      }
      if (adyenResponse.action != null) {
        actionType = adyenResponse.action.get("type")?.asString
        jsonAction = JSONObject(adyenResponse.action.toString())
      }
    }

    if (actionType != null && jsonAction != null) {
      when (actionType) {
        REDIRECT -> {
          action = RedirectAction.SERIALIZER.deserialize(jsonAction)
          redirectUrl = action.url
        }
        THREEDS2FINGERPRINT -> action = Threeds2FingerprintAction.SERIALIZER.deserialize(jsonAction)
        THREEDS2CHALLENGE -> action = Threeds2ChallengeAction.SERIALIZER.deserialize(jsonAction)
      }
    }
    return PaymentModel(adyenResponse?.resultCode, adyenResponse?.refusalReason,
        adyenResponse?.refusalReasonCode?.toInt(), action, redirectUrl, action?.paymentData,
        response.uid, null, response.hash, response.orderReference, fraudResultsId,
        map(response.status), response.metadata?.errorMessage, response.metadata?.errorCode)
  }

  fun map(response: TransactionResponse): PaymentModel {
    return PaymentModel(response, map(response.status))
  }

  private fun map(status: TransactionStatus): PaymentModel.Status {
    return when (status) {
      TransactionStatus.PENDING -> PENDING
      TransactionStatus.PENDING_SERVICE_AUTHORIZATION -> PENDING_SERVICE_AUTHORIZATION
      TransactionStatus.SETTLED -> SETTLED
      TransactionStatus.PROCESSING -> PROCESSING
      TransactionStatus.COMPLETED -> COMPLETED
      TransactionStatus.PENDING_USER_PAYMENT -> PENDING_USER_PAYMENT
      TransactionStatus.INVALID_TRANSACTION -> INVALID_TRANSACTION
      TransactionStatus.FAILED -> FAILED
      TransactionStatus.CANCELED -> CANCELED
      TransactionStatus.FRAUD -> FRAUD
    }
  }

  fun map(response: Transaction): PaymentModel {
    return PaymentModel("", null, null, null, "", "", response.uid, response.metadata?.purchaseUid,
        response.hash, response.orderReference, emptyList(), map(response.status))
  }

  private fun map(status: Transaction.Status): PaymentModel.Status {
    return when (status) {
      Transaction.Status.PENDING -> PENDING
      Transaction.Status.PENDING_SERVICE_AUTHORIZATION -> PENDING_SERVICE_AUTHORIZATION
      Transaction.Status.SETTLED -> SETTLED
      Transaction.Status.PROCESSING -> PROCESSING
      Transaction.Status.COMPLETED -> COMPLETED
      Transaction.Status.PENDING_USER_PAYMENT -> PENDING_USER_PAYMENT
      Transaction.Status.INVALID_TRANSACTION -> INVALID_TRANSACTION
      Transaction.Status.FAILED -> FAILED
      Transaction.Status.CANCELED -> CANCELED
      Transaction.Status.FRAUD -> FRAUD
    }
  }

  fun mapInfoModelError(throwable: Throwable): PaymentInfoModel {
    throwable.printStackTrace()
    val codeAndMessage = throwable.getErrorCodeAndMessage()
    val errorInfo = mapErrorInfo(codeAndMessage.first, codeAndMessage.second)
    return PaymentInfoModel(
        Error(true, throwable.isNoNetworkException(), errorInfo))
  }

  fun mapPaymentModelError(throwable: Throwable): PaymentModel {
    throwable.printStackTrace()
    val codeAndMessage = throwable.getErrorCodeAndMessage()
    val errorInfo = mapErrorInfo(codeAndMessage.first, codeAndMessage.second)
    return PaymentModel(Error(true, throwable.isNoNetworkException(), errorInfo))
  }

  fun mapVerificationPaymentModeSuccess(): VerificationPaymentModel {
    return VerificationPaymentModel(true, null, null, null)
  }

  fun mapVerificationPaymentModelError(throwable: Throwable): VerificationPaymentModel {
    throwable.printStackTrace()
    return if (throwable is HttpException) {
      val body = throwable.getMessage()
      val verificationTransactionResponse =
          gson.fromJson(body, VerificationTransactionResponse::class.java)
      var errorType = VerificationPaymentModel.ErrorType.OTHER
      when (verificationTransactionResponse.code) {
        "Request.Invalid" -> errorType = VerificationPaymentModel.ErrorType.INVALID_REQUEST
        "Request.TooMany" -> errorType = VerificationPaymentModel.ErrorType.TOO_MANY_ATTEMPTS
      }
      VerificationPaymentModel(false, errorType,
          verificationTransactionResponse.data?.refusalReason,
          verificationTransactionResponse.data?.refusalReasonCode?.toInt(), Error(hasError = true,
          isNetworkError = false))
    } else {
      val codeAndMessage = throwable.getErrorCodeAndMessage()
      val errorInfo = mapErrorInfo(codeAndMessage.first, codeAndMessage.second)
      VerificationPaymentModel(false, VerificationPaymentModel.ErrorType.OTHER, null, null,
          Error(true, throwable.isNoNetworkException(), errorInfo))
    }
  }

  fun mapVerificationCodeError(throwable: Throwable): VerificationCodeResult {
    throwable.printStackTrace()
    if (throwable is HttpException) {
      val body = throwable.getMessage()
      val verificationTransactionResponse =
          gson.fromJson(body, VerificationErrorResponse::class.java)
      var errorType = VerificationCodeResult.ErrorType.OTHER
      when (verificationTransactionResponse.code) {
        "Body.Invalid" -> errorType = VerificationCodeResult.ErrorType.WRONG_CODE
        "Request.TooMany" -> errorType = VerificationCodeResult.ErrorType.TOO_MANY_ATTEMPTS
      }
      val errorInfo = mapErrorInfo(throwable.code(), body)
      return VerificationCodeResult(false, errorType, Error(hasError = true,
          isNetworkError = true, info = errorInfo))
    }
    return VerificationCodeResult(success = false,
        errorType = VerificationCodeResult.ErrorType.OTHER,
        error = Error(hasError = true, isNetworkError = false,
            info = ErrorInfo(text = throwable.message)))
  }

  private fun findPaymentMethod(paymentMethods: List<PaymentMethod>?,
                                method: AdyenPaymentRepository.Methods,
                                isStored: Boolean, price: Price): PaymentInfoModel {
    paymentMethods?.let {
      for (paymentMethod in it) {
        if (paymentMethod.type == method.adyenType) return PaymentInfoModel(paymentMethod, isStored,
            price.value, price.currency)
      }
    }
    return PaymentInfoModel(Error(true))
  }

  private fun mapErrorInfo(httpCode: Int?, message: String?): ErrorInfo {
    val messageGson = gson.fromJson(message, ResponseErrorBaseBody::class.java)
    val errorType = getErrorType(httpCode, messageGson.code, messageGson.text)
    return ErrorInfo(httpCode, messageGson.code, messageGson.text, errorType)
  }

  private fun getErrorType(httpCode: Int?, messageCode: String?, text: String?): ErrorType {
    return when {
      httpCode != null && httpCode == 400 && messageCode == "Body.Fields.Missing"
          && text?.contains("payment.billing") == true -> ErrorType.BILLING_ADDRESS
      messageCode == "NotAllowed" -> ErrorType.SUB_ALREADY_OWNED
      messageCode == "Authorization.Forbidden" -> ErrorType.BLOCKED
      httpCode == 409 -> ErrorType.CONFLICT
      else -> ErrorType.UNKNOWN
    }
  }

  companion object {
    const val REDIRECT = "redirect"
    const val THREEDS2FINGERPRINT = "threeDS2Fingerprint"
    const val THREEDS2CHALLENGE = "threeDS2Challenge"
  }
}
