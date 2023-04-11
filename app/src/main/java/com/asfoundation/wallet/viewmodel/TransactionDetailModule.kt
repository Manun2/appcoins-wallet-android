package com.asfoundation.wallet.viewmodel

import androidx.annotation.NonNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appcoins.wallet.legacy.domain.DisplayChatUseCase
import com.appcoins.wallet.legacy.domain.FindDefaultWalletUseCase
import com.appcoins.wallet.legacy.domain.FindNetworkInfoUseCase
import com.asfoundation.wallet.router.ExternalBrowserRouter
import com.asfoundation.wallet.router.TransactionDetailRouter
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import io.reactivex.disposables.CompositeDisposable

@InstallIn(ActivityComponent::class)
@Module
class TransactionDetailModule {

  @Provides
  fun provideTransactionDetailViewModelFactory(
    findDefaultWalletUseCase: com.appcoins.wallet.legacy.domain.FindDefaultWalletUseCase,
    findNetworkInfoUseCase: com.appcoins.wallet.legacy.domain.FindNetworkInfoUseCase,
    externalBrowserRouter: ExternalBrowserRouter,
    displayChatUseCase: com.appcoins.wallet.legacy.domain.DisplayChatUseCase,
    transactionDetailRouter: TransactionDetailRouter,
    localCurrencyConversionService: LocalCurrencyConversionService
  ): TransactionDetailViewModelFactory {
    return TransactionDetailViewModelFactory(
      findDefaultWalletUseCase, findNetworkInfoUseCase,
      externalBrowserRouter, CompositeDisposable(), displayChatUseCase, transactionDetailRouter,
      localCurrencyConversionService
    )
  }
}

class TransactionDetailViewModelFactory(
  private val findDefaultWalletUseCase: com.appcoins.wallet.legacy.domain.FindDefaultWalletUseCase,
  private val findNetworkInfoUseCase: com.appcoins.wallet.legacy.domain.FindNetworkInfoUseCase,
  private val externalBrowserRouter: ExternalBrowserRouter,
  private val compositeDisposable: CompositeDisposable,
  private val displayChatUseCase: com.appcoins.wallet.legacy.domain.DisplayChatUseCase,
  private val transactionDetailRouter: TransactionDetailRouter,
  private val localCurrencyConversionService: LocalCurrencyConversionService
) : ViewModelProvider.Factory {

  @NonNull
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return TransactionDetailViewModel(
      findDefaultWalletUseCase, findNetworkInfoUseCase,
      externalBrowserRouter, compositeDisposable, displayChatUseCase, transactionDetailRouter,
      localCurrencyConversionService
    ) as T
  }
}