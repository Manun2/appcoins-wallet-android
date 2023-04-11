package com.appcoins.wallet.legacy.domain

import com.appcoins.wallet.intercom.SupportRepository
import io.intercom.android.sdk.Intercom
import javax.inject.Inject

class DisplayChatUseCase @Inject constructor(private val supportRepository: com.appcoins.wallet.intercom.SupportRepository) {

  operator fun invoke() {
    supportRepository.resetUnreadConversations()
    Intercom.client()
        .displayMessenger()
  }
}