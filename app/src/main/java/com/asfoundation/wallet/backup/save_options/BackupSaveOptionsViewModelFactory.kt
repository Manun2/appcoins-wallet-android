package com.asfoundation.wallet.backup.save_options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.legacy.domain.BackupSuccessLogUseCase
import com.appcoins.wallet.legacy.domain.SendBackupToEmailUseCase

class BackupSaveOptionsViewModelFactory(
  private val data: BackupSaveOptionsData,
  private val sendBackupToEmailUseCase: com.appcoins.wallet.legacy.domain.SendBackupToEmailUseCase,
  private val backupSuccessLogUseCase: com.appcoins.wallet.legacy.domain.BackupSuccessLogUseCase,
  private val logger: Logger
) :
  ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return BackupSaveOptionsViewModel(
      data, sendBackupToEmailUseCase, backupSuccessLogUseCase,
      logger
    ) as T
  }
}