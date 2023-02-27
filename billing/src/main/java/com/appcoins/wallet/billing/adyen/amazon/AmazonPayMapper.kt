package com.appcoins.wallet.billing.adyen.amazon

import com.appcoins.wallet.billing.common.BillingErrorMapper
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.billing.util.getErrorCodeAndMessage
import com.appcoins.wallet.billing.util.isNoNetworkException
import javax.inject.Inject

open class AmazonPayMapper @Inject constructor(
  private val billingErrorMapper: BillingErrorMapper,
) {

  open fun map(response: CreateAmazonSessionResponse): AmazonSession {
    return AmazonSession(response.checkoutSessionId, response.amazonPayRedirectUrl)
  }

  open fun map(response: AmazonPayRedirectResponse): AmazonPayRedirectUrl {
    return AmazonPayRedirectUrl(response.checkoutSessionId, response.amazonPayRedirectUrl)
  }

  open fun mapPaymentModelError(throwable: Throwable): AmazonSession {
    throwable.printStackTrace()
    val codeAndMessage = throwable.getErrorCodeAndMessage()
    val errorInfo = billingErrorMapper.mapErrorInfo(codeAndMessage.first, codeAndMessage.second)  // TODO remove if not needed
    val error = Error(true, throwable.isNoNetworkException(), errorInfo)
    return AmazonSession(error)
  }

}
