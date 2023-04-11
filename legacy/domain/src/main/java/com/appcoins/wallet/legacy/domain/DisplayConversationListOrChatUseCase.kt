package com.appcoins.wallet.legacy.domain

import com.appcoins.wallet.intercom.SupportRepository
import io.intercom.android.sdk.Intercom
import javax.inject.Inject

class DisplayConversationListOrChatUseCase @Inject constructor(
    private val supportRepository: com.appcoins.wallet.intercom.SupportRepository
) {

  @Suppress("DEPRECATION")
  operator fun invoke() {
    //this method was introduced because if the app is closed intercom returns 0 unread conversations
    //even if there are more
    supportRepository.resetUnreadConversations()
    val handledByIntercom = getUnreadConversations() > 0
    if (handledByIntercom) {
      Intercom.client()
          .displayMessenger()
    } else {
      Intercom.client()
          .displayConversationsList()
    }
  }

  private fun getUnreadConversations() = Intercom.client().unreadConversationCount
}