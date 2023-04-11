package com.asfoundation.wallet.wallet_blocked

import com.appcoins.wallet.legacy.domain.GetWalletInfoUseCase
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WalletBlockedInteract @Inject constructor(
    private val getWalletInfoUseCase: com.appcoins.wallet.legacy.domain.GetWalletInfoUseCase
) {

  fun isWalletBlocked(): Single<Boolean> {
    return getWalletInfoUseCase(null, cached = false, updateFiat = false)
        .map { walletInfo -> walletInfo.blocked }
        .onErrorReturn { false }
        .delay(1, TimeUnit.SECONDS)
  }
}