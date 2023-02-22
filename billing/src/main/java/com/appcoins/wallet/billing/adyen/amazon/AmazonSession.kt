package com.appcoins.wallet.billing.adyen.amazon

import com.appcoins.wallet.billing.util.Error
import java.io.Serializable

data class AmazonSession(
  val checkoutSessionId: String?,
  val amazonPayRedirectUrl: String?,
  val errorMessage: String? = null,
  val errorCode: Int? = null,
  val error: Error = Error()
) : Serializable {

  constructor(error: Error) : this(
    "",
    null,
    null,
    null,
    error
  )

  constructor(response: CreateAmazonSessionResponse) : this(
    response.checkoutSessionId,
    response.amazonPayRedirectUrl,
    null,
    null,
    Error(
      hasError = false
    )
  )

}
