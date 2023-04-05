package com.asfoundation.wallet.ui.wallets

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asf.wallet.databinding.WalletRoundedOutlinedCardBinding
import io.reactivex.subjects.PublishSubject

class WalletsViewHolder(
  itemView: View,
  private val uiEventListener: PublishSubject<String>,
  private val currencyFormatUtils: CurrencyFormatUtils
) : RecyclerView.ViewHolder(itemView) {

  private val binding by lazy { WalletRoundedOutlinedCardBinding.bind(itemView) }

  private val wallet_address get() = binding.walletAddress
  private val wallet_balance get() = binding.walletBalance
  @SuppressLint("SetTextI18n")
  fun bind(item: WalletBalance) {
    wallet_address.text = item.walletName
    wallet_balance.text =
      "${item.balance.symbol}${currencyFormatUtils.formatCurrency(item.balance.amount)}"
    itemView.setOnClickListener { uiEventListener.onNext(item.walletAddress) }
  }
}
