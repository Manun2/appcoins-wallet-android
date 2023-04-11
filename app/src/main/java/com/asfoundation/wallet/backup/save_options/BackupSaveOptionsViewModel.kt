package com.asfoundation.wallet.backup.save_options

import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.legacy.domain.BackupSuccessLogUseCase
import com.appcoins.wallet.legacy.domain.SendBackupToEmailUseCase
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState

sealed class BackupSaveOptionsSideEffect : SideEffect {
  data class NavigateToSuccess(val walletAddress: String) : BackupSaveOptionsSideEffect()
  object ShowError : BackupSaveOptionsSideEffect()
}

object BackupSaveOptionsState : ViewState


class BackupSaveOptionsViewModel(
  private val data: BackupSaveOptionsData,
  private val sendBackupToEmailUseCase: com.appcoins.wallet.legacy.domain.SendBackupToEmailUseCase,
  private val backupSuccessLogUseCase: com.appcoins.wallet.legacy.domain.BackupSuccessLogUseCase,
  private val logger: Logger,
) : BaseViewModel<BackupSaveOptionsState, BackupSaveOptionsSideEffect>(
  initialState()
) {

  companion object {
    private val TAG = BackupSaveOptionsViewModel::class.java.name

    fun initialState(): BackupSaveOptionsState {
      return BackupSaveOptionsState
    }
  }

  fun sendBackupToEmail(text: String) {
    sendBackupToEmailUseCase(data.walletAddress, data.password, text)
      .andThen(backupSuccessLogUseCase(data.walletAddress))
      .doOnComplete { sendSideEffect { BackupSaveOptionsSideEffect.NavigateToSuccess(data.walletAddress) } }
      .scopedSubscribe { showError(it) }
  }

  private fun showError(throwable: Throwable) {
    logger.log(TAG, throwable)
    sendSideEffect { BackupSaveOptionsSideEffect.ShowError }
  }
}