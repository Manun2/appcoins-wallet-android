package com.asfoundation.wallet.ui.iab

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.appcoins.wallet.commons.Logger
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.billing.paypal.PaypalReturnSchemas
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.webview_fragment.*
import kotlinx.android.synthetic.main.webview_fragment.view.*
import java.net.URISyntaxException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

@AndroidEntryPoint
class BillingWebViewFragment : BasePageViewFragment() {
  private val timeoutReference: AtomicReference<ScheduledFuture<*>?> = AtomicReference()

  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor
  @Inject
  lateinit var analytics: BillingAnalytics
  @Inject
  lateinit var logger: Logger

  lateinit var currentUrl: String
  private var executorService: ScheduledExecutorService? = null
  private var webViewActivity: WebViewActivity? = null
  private var asyncDetailsShown = false
  private val TAG = BillingWebViewFragment::class.java.name

  companion object {
    private const val CARRIER_BILLING_RETURN_SCHEMA = "https://%s/return/carrier_billing"
    const val CARRIER_BILLING_ONE_BIP_SCHEMA = "https://pay.onebip.com/"
    private const val ADYEN_PAYMENT_SCHEMA = "adyencheckout://"
    private const val LOCAL_PAYMENTS_SCHEMA = "myappcoins.com/t/"
    private const val LOCAL_PAYMENTS_URL = "https://myappcoins.com/t/"
    private var PAYPAL_SUCCESS_SCHEMA = PaypalReturnSchemas.RETURN.schema
    private var PAYPAL_CANCEL_SCHEMA = PaypalReturnSchemas.CANCEL.schema
    private val EXTERNAL_INTENT_SCHEMA_LIST = listOf(
      "picpay://",
      "shopeeid://",
      "grab://",
      "intent://",
      "open.dolfinwallet://",
      "momo://",
      "tez://",
      "upi://",
      ExternalAppEnum.GOJEK.uriScheme,
      ExternalAppEnum.PHONEPE.uriScheme,
      ExternalAppEnum.PAYTM.uriScheme,
      ExternalAppEnum.BHIM.uriScheme,
    )
    private const val ASYNC_PAYMENT_FORM_SHOWN_SCHEMA = "https://pm.dlocal.com//v1/gateway/show?"
    private const val CODAPAY_FINAL_REDIRECT_SCHEMA = "https://airtime.codapayments.com/epcgw/dlocal/"
    private const val CODAPAY_BACK_URL = "https://pay.dlocal.com/payment_method_connectors/global_pm//back"
    private const val CODAPAY_CANCEL_URL = "codapayments.com/airtime/cancelConfirm"
    private const val URL = "url"
    private const val CURRENT_URL = "currentUrl"
    private const val ORDER_ID_PARAMETER = "OrderId"
    const val OPEN_SUPPORT = BuildConfig.MY_APPCOINS_BASE_HOST + "open-support"

    private var currentExtAppSelected: ExternalAppEnum? = null

    fun newInstance(url: String?): BillingWebViewFragment {
      return BillingWebViewFragment().apply {
        arguments = Bundle().apply {
          putString(URL, url)
        }
        retainInstance = true
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(context is WebViewActivity) { "WebView fragment must be attached to WebView Activity" }
    webViewActivity = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    executorService = Executors.newScheduledThreadPool(0)
    require(arguments != null && requireArguments().containsKey(URL)) { "Provided url is null!" }
    currentUrl = if (savedInstanceState == null) {
      requireArguments().getString(URL)!!
    } else {
      savedInstanceState.getString(CURRENT_URL)!!
    }
    CookieManager.getInstance()
      .setAcceptCookie(true)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.webview_fragment, container, false)

    view.webview.webViewClient = object : WebViewClient() {
      override fun shouldOverrideUrlLoading(view: WebView, clickUrl: String): Boolean {
        when {
          clickUrl.contains(LOCAL_PAYMENTS_SCHEMA) ||
              clickUrl.contains(ADYEN_PAYMENT_SCHEMA) ||
              clickUrl.contains(PAYPAL_SUCCESS_SCHEMA) -> {
            currentUrl = clickUrl
            finishWithValidations(clickUrl)
          }
          isExternalIntentSchema(clickUrl) -> {
            launchActivityForSchema(view, clickUrl)
          }
          clickUrl.contains(CODAPAY_FINAL_REDIRECT_SCHEMA) && clickUrl.contains(
            ORDER_ID_PARAMETER
          ) -> {
            val orderId = Uri.parse(clickUrl)
              .getQueryParameter(ORDER_ID_PARAMETER)
            finishWithValidations(LOCAL_PAYMENTS_URL + orderId)
          }
          clickUrl.contains(CARRIER_BILLING_RETURN_SCHEMA.format(BuildConfig.APPLICATION_ID)) -> {
            currentUrl = clickUrl
            finishWithValidations(clickUrl)
          }
          clickUrl.contains(CODAPAY_CANCEL_URL) -> finishWithFail(clickUrl)
          clickUrl.contains(OPEN_SUPPORT) -> finishWithFail(clickUrl)
          clickUrl.contains(PAYPAL_CANCEL_SCHEMA) -> finishWithFail(clickUrl)
          else -> {
            currentUrl = clickUrl
            return false
          }
        }
        return true
      }

      override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        if (!url.contains("/redirect")) {
          val timeout = timeoutReference.getAndSet(null)
          timeout?.cancel(false)
          webview_progress_bar?.visibility = View.GONE
        }
        if (url.contains(ASYNC_PAYMENT_FORM_SHOWN_SCHEMA)) {
          asyncDetailsShown = true
        }
      }
    }
    view.webview.settings.javaScriptEnabled = true
    view.webview.settings.domStorageEnabled = true
    view.webview.settings.useWideViewPort = true
    view.webview.loadUrl(currentUrl)

    view?.warning_get_bt?.setOnClickListener {
      dismissGetAppWarning()
      currentExtAppSelected?.let {
        openDownloadExternalApp(it)
      }
    }
    view?.warning_cancel_bt?.setOnClickListener {
      dismissGetAppWarning()
    }

    return view
  }

  private fun openDownloadExternalApp(appInfo: ExternalAppEnum) {
    try {
      // try to open in an app store for download:
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(appInfo.marketUri)))
    } catch (e: ActivityNotFoundException) {
      try {
        // try to open google play web page:
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(appInfo.googlePlayUrl)))
      } catch (e: ActivityNotFoundException) {
        logger.log(TAG, "Unable to open app store or GP page to download: ${appInfo.appName}")
      }
    }
  }

  fun isExternalIntentSchema(clickUrl: String): Boolean {
    for (schema in EXTERNAL_INTENT_SCHEMA_LIST) {
      if (clickUrl.contains(schema)) {
        return true
      }
    }
    return false
  }

  fun handleBackPressed(): Boolean {
    return if (asyncDetailsShown) {
      webview.loadUrl(CODAPAY_BACK_URL)
      asyncDetailsShown = false
      true
    } else {
      val intent = Intent()
      intent.data = Uri.parse(currentUrl)
      webViewActivity?.setResult(WebViewActivity.FAIL, intent)
      false
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(CURRENT_URL, currentUrl)
  }

  override fun onDestroy() {
    executorService!!.shutdown()
    super.onDestroy()
  }

  override fun onDetach() {
    webViewActivity = null
    super.onDetach()
  }

  private fun launchActivityForSchema(webView: WebView, url: String) {
    try {
      val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
      if (intent != null) {
        val packageManager = requireContext().packageManager
        val info =
          packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (info != null) {
          requireContext().startActivity(intent)
        } else {
          val fallbackUrl = intent.getStringExtra("browser_fallback_url")
          if(fallbackUrl != null) {
            webView.loadUrl(fallbackUrl)
          } else {
            val appInfo = getAppInfo(url)
            if (appInfo != null)
              showGetAppWarning(appInfo)
            else
              logger.log(TAG, "Unable to open external app from the webview: $url")
          }
        }
      }
    } catch (e: URISyntaxException) {
      e.printStackTrace()
      if (view != null) {
        Snackbar.make(
          requireView(), R.string.unknown_error,
          Snackbar.LENGTH_SHORT
        )
          .show()
      }
    }
  }

  private fun prepareIntentToFinishURL(url: String) : Intent {
    val intent = Intent()
    intent.data = Uri.parse(url)
    return intent
  }

  private fun finishWithValidations(url: String) {
    val intent = prepareIntentToFinishURL(url)
    val resultCode = Uri.parse(url).getQueryParameter("resultCode")
    if (resultCode.equals("cancelled", true)) {
      webViewActivity?.setResult(WebViewActivity.USER_CANCEL, intent)
    } else {
      webViewActivity?.setResult(WebViewActivity.SUCCESS, intent)
    }

    webViewActivity?.finish()
  }

  private fun finishWithFail(url: String) {
    webViewActivity?.setResult(WebViewActivity.FAIL, prepareIntentToFinishURL(url))
    webViewActivity?.finish()
  }

  private fun getAppInfo(url: String): ExternalAppEnum? {
    return ExternalAppEnum.values().find { url.contains(it.uriScheme) }
  }

  private fun showGetAppWarning(appInfo: ExternalAppEnum) {
    currentExtAppSelected = appInfo
    view?.warning_group?.startAnimation(AnimationUtils.loadAnimation(context,R.anim.pop_in_animation))
    view?.warning_group?.visibility = View.VISIBLE
    view?.warning_name_tv?.text = appInfo.appName
    view?.warning_app_iv?.setImageResource(appInfo.appIcon)
  }

  private fun dismissGetAppWarning() {
    view?.warning_group?.startAnimation(AnimationUtils.loadAnimation(context,R.anim.pop_out_animation))
    view?.warning_group?.visibility = View.GONE
  }
}
