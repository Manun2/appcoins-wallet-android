package com.asfoundation.wallet.ui.iab

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.appcoins.wallet.commons.Logger
import com.asf.wallet.R
import com.asfoundation.wallet.C.Key.TRANSACTION
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.PaymentMethodsView.PaymentMethodId
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.Period
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxrelay2.PublishRelay
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.createorder.*
import com.paypal.checkout.error.OnError
import com.paypal.checkout.order.*
import com.paypal.checkout.paymentbutton.PaymentButtonContainer
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.test_payment_paypal_layout.*
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class PayPalTestFragment : BasePageViewFragment() {


  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "Payment Methods Fragment must be attached to IAB activity" }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)


  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

      payment_button_container.setup(
        createOrder =
        CreateOrder { createOrderActions ->
          val order =
            Order(
              intent = OrderIntent.CAPTURE,
              appContext = AppContext(
                userAction = UserAction.PAY_NOW,
                shippingPreference = ShippingPreference.NO_SHIPPING),
              purchaseUnitList =
              listOf(
                PurchaseUnit(
                  amount =
                  Amount(currencyCode = CurrencyCode.USD, value = "0.99")
                )
              )
            )
          createOrderActions.create(order)
        },
        onApprove =
        OnApprove { approval ->
          approval.orderActions.capture { captureOrderResult ->
            Log.i("CaptureOrder", "CaptureOrderResult: $captureOrderResult")
            text_paypal_info.text = "IAB completed successfully"
          }
        },
        onCancel =
        OnCancel {
          Log.d("OnCancel", "User canceled the PayPal flow.")
          text_paypal_info.text = "User canceled the PayPal flow."
        },
        onError =
        OnError { errorInfo ->
          Log.d("OnError", "Error: $errorInfo")
          text_paypal_info.text = "Error in PayPal flow: $errorInfo"
        }
      )

    }

  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.test_payment_paypal_layout, container, false)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
//    presenter.onSavedInstance(outState)
  }

  override fun onDestroyView() {
//    presenter.stop()
//    compositeDisposable.clear()
    super.onDestroyView()
  }


  override fun onResume() {

    super.onResume()
  }

//  override fun finish(bundle: Bundle) = iabView.finish(bundle)

}