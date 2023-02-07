package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.bdsbilling.repository.entity.MethodSpecificPrice
import java.math.BigDecimal

open class PaymentMethod(
  open val id: String, open val label: String,
  open val iconUrl: String, val async: Boolean, val fee: PaymentMethodFee?,
  open val isEnabled: Boolean = true,
  val specificPrice: PaymentSpecificPrice? = null,
  open var disabledReason: Int? = null,
  val showTopup: Boolean = false
) {
  constructor() : this(
    "", "", "", false, null, false, null
  )

  companion object {
    @JvmField
    val APPC: PaymentMethod =
      PaymentMethod(
        "appcoins",
        "AppCoins (APPC)",
        "https://cdn6.aptoide.com/imgs/a/f/9/af95bd0d14875800231f05dbf1933143_logo.png",
        false,
        null,
        true,
        null
      )
  }
}

data class PaymentMethodFee(
  val isExact: Boolean,
  val amount: BigDecimal?,
  val currency: String?
) {
  fun isValidFee() = isExact && amount != null && currency != null
}

data class PaymentSpecificPrice(
  val amount: BigDecimal?,
  val currency: String?
) {
  constructor(price: MethodSpecificPrice) : this(price.value, price.currency)

  fun isValid() = amount != null && currency != null

}
