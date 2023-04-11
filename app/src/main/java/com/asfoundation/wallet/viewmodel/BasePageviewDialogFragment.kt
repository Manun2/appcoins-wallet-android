package com.asfoundation.wallet.viewmodel

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.asfoundation.wallet.App
import com.appcoins.wallet.core.analytics.analytics.PageViewAnalytics

abstract class BasePageViewDialogFragment : DialogFragment() {

  private lateinit var pageViewAnalytics: PageViewAnalytics

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    pageViewAnalytics = PageViewAnalytics()
  }

  override fun onResume() {
    super.onResume()
    pageViewAnalytics.sendPageViewEvent(javaClass.simpleName)
  }
}