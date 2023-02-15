package com.appcoins.wallet.billing.adyen.amazon

import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.common.BillingErrorMapper
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.billing.util.getErrorCodeAndMessage
import com.appcoins.wallet.billing.util.isNoNetworkException
import javax.inject.Inject

open class AmazonPayMapper @Inject constructor(
  private val billingErrorMapper: BillingErrorMapper,
) {

  open fun map(response: CreateAmazonSessionResponse): String {
    return
  }

  open fun mapPaymentModelError(throwable: Throwable): String {
    throwable.printStackTrace()
    val codeAndMessage = throwable.getErrorCodeAndMessage()
    val errorInfo = billingErrorMapper.mapErrorInfo(codeAndMessage.first, codeAndMessage.second)
    val error = Error(true, throwable.isNoNetworkException(), errorInfo)
    return PaymentModel(error)
  }

}
