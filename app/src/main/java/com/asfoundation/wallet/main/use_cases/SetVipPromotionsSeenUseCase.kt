package com.asfoundation.wallet.main.use_cases

import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.legacy.domain.ObserveDefaultWalletUseCase
import io.reactivex.Completable
import io.reactivex.internal.operators.completable.CompletableFromAction
import javax.inject.Inject

class SetVipPromotionsSeenUseCase @Inject constructor(
  private val promotionsRepository: PromotionsRepository,
  private val observeDefaultWalletUseCase: com.appcoins.wallet.legacy.domain.ObserveDefaultWalletUseCase
) {

  operator fun invoke(isSeen: Boolean): Completable {
    return observeDefaultWalletUseCase()
      .flatMapCompletable {
        CompletableFromAction {
          promotionsRepository.setVipCalloutAlreadySeen(it.address, isSeen)
        }
      }
  }
}
