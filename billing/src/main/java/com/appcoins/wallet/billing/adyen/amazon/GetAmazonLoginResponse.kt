package com.appcoins.wallet.billing.adyen.amazon

data class GetAmazonLoginResponse(
  val checkoutSessionId: String,
  val amazonPayRedirectUrl: String
  )