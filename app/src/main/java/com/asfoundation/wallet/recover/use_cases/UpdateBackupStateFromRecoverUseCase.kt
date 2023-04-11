package com.asfoundation.wallet.recover.use_cases

import com.appcoins.wallet.legacy.domain.BackupSuccessLogUseCase
import com.appcoins.wallet.legacy.domain.GetWalletInfoUseCase
import io.reactivex.Completable
import javax.inject.Inject

class UpdateBackupStateFromRecoverUseCase @Inject constructor(
  private val getWalletInfoUseCase: com.appcoins.wallet.legacy.domain.GetWalletInfoUseCase,
  private val backupSuccessLogUseCase: com.appcoins.wallet.legacy.domain.BackupSuccessLogUseCase
) {

  operator fun invoke(): Completable {
    return getWalletInfoUseCase(null, cached = false, updateFiat = false)
      .flatMapCompletable {
        if (!it.hasBackup) {
          return@flatMapCompletable backupSuccessLogUseCase(it.wallet).andThen(Completable.complete())
        }
        Completable.complete()
      }
  }
}