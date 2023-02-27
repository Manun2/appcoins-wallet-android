package com.appcoins.wallet.billing.adyen.amazon

data class AmazonPayRedirectResponse(
  val checkoutSessionId: String,
  val amazonPayRedirectUrl: String
  )