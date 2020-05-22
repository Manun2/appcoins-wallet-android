package com.asfoundation.wallet.di;

import com.asfoundation.wallet.backup.FileInteractor;
import com.asfoundation.wallet.interact.RestoreWalletInteractor;
import com.asfoundation.wallet.interact.SetDefaultWalletInteract;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.ui.balance.BalanceInteract;
import com.asfoundation.wallet.ui.balance.RestoreWalletPasswordInteractor;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(includes = { RepositoriesModule.class, AccountsManageModule.class }) class RestoreModule {
  @Singleton @Provides RestoreWalletInteractor provideRestoreWalletInteract(
      WalletRepositoryType walletRepository, PasswordStore passwordStore,
      PreferencesRepositoryType preferencesRepositoryType,
      SetDefaultWalletInteract setDefaultWalletInteract, FileInteractor fileInteractor) {
    return new RestoreWalletInteractor(walletRepository, setDefaultWalletInteract, passwordStore,
        preferencesRepositoryType, fileInteractor);
  }

  @Singleton @Provides RestoreWalletPasswordInteractor provideRestoreWalletInteractor(Gson gson,
      BalanceInteract balanceInteract, RestoreWalletInteractor restoreWalletInteractor) {
    return new RestoreWalletPasswordInteractor(gson, balanceInteract, restoreWalletInteractor);
  }
}
