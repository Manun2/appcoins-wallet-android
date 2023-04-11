package com.asfoundation.wallet.ui

import com.asfoundation.wallet.entity.GasSettings
import com.asfoundation.wallet.entity.NetworkInfo
import com.appcoins.wallet.legacy.domain.FindNetworkInfoUseCase
import io.reactivex.Single
import com.appcoins.wallet.sharedpreferences.GasPreferencesDataSource
import java.math.BigDecimal
import javax.inject.Inject

class GasSettingsInteractor @Inject constructor(
  private val findNetworkInfoUseCase: com.appcoins.wallet.legacy.domain.FindNetworkInfoUseCase,
  private val gasPreferencesRepository: GasPreferencesDataSource
) {

  fun findDefaultNetwork(): Single<NetworkInfo> = findNetworkInfoUseCase()

  fun saveGasPreferences(price: BigDecimal, limit: BigDecimal) {
    val savedGasPrice = gasPreferencesRepository.getSavedGasPrice()
    val savedGasLimit = gasPreferencesRepository.getSavedGasLimit()
    if (savedGasPrice != price) {
      gasPreferencesRepository.saveGasPrice(price)
    }
    if (savedGasLimit != limit) {
      gasPreferencesRepository.saveGasLimit(limit)
    }
  }

  fun getSavedGasPreferences(): GasSettings {
    return GasSettings(gasPreferencesRepository.getSavedGasPrice(),
        gasPreferencesRepository.getSavedGasLimit())
  }
}
