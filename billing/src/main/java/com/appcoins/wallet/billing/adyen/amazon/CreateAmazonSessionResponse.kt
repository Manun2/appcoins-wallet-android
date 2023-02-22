package com.appcoins.wallet.billing.adyen.amazon

data class CreateAmazonSessionResponse(
  val checkoutSessionId: String,
  val amazonPayRedirectUrl: String
  )