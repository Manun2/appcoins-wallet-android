package com.asfoundation.wallet.backup.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.legacy.domain.GetWalletInfoUseCase

class BackupEntryViewModelFactory(
  private val data: BackupEntryData,
  private val getWalletInfoUseCase: com.appcoins.wallet.legacy.domain.GetWalletInfoUseCase,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val rxSchedulers: RxSchedulers,
) :
  ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return BackupEntryViewModel(data, getWalletInfoUseCase, currencyFormatUtils, rxSchedulers) as T
  }
}
