package com.asfoundation.wallet.logging

import com.appcoins.wallet.commons.LogReceiver
import com.appcoins.wallet.commons.Logger
import javax.inject.Inject

class WalletLogger @Inject constructor(private var logReceivers: ArrayList<LogReceiver>): Logger {

  override fun log(tag: String?, message: String?, asError: Boolean) {
    logReceivers.forEach { receiver -> message?.let { message -> receiver.log(tag, message, asError) } }
  }

  override fun log(tag: String?, throwable: Throwable?) {
    logReceivers.forEach { it.log(tag, throwable) }
  }
  override fun log(tag: String?, message: String?, throwable: Throwable?) {
    logReceivers.forEach { it.log(tag, message, throwable) }
  }

  override fun addReceiver(receiver: LogReceiver) {
    logReceivers.add(receiver)
  }

  override fun removeReceiver(receiver: LogReceiver) {
    logReceivers.remove(receiver)
  }
}