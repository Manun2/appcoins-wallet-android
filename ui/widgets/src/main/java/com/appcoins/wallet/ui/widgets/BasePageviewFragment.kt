package com.asfoundation.wallet.viewmodel

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.analytics.analytics.PageViewAnalytics

abstract class BasePageViewFragment : Fragment() {

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