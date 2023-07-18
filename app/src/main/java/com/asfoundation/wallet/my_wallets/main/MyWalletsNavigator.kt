package com.asfoundation.wallet.my_wallets.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.appcoins.wallet.core.utils.jvm_common.C
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupWalletEntryFragment
import com.asfoundation.wallet.backup.BackupWalletEntryFragment.Companion.WALLET_NAME
import com.asfoundation.wallet.ui.MyAddressActivity
import com.asfoundation.wallet.ui.transact.TransferActivity
import javax.inject.Inject

class MyWalletsNavigator @Inject constructor(
  private val fragment: Fragment,
  private val navController: NavController
) : Navigator {

  fun navigateToMore(
    walletAddress: String,
    totalFiatBalance: String,
    appcoinsBalance: String,
    creditsBalance: String,
    ethereumBalance: String
  ) {
    navigate(
      navController,
      MyWalletsFragmentDirections.actionNavigateToMore(
        walletAddress,
        totalFiatBalance,
        appcoinsBalance,
        creditsBalance,
        ethereumBalance
      )
    )
  }

  fun navigateToName(
    walletAddress: String,
    walletName: String,
  ) {
    navigate(
      navController,
      MyWalletsFragmentDirections.actionNavigateToName(walletAddress, walletName)
    )
  }

  fun navigateToBalanceDetails(
    totalFiatBalance: String,
    appcoinsBalance: String,
    creditsBalance: String,
    ethereumBalance: String
  ) {
    navigate(
      navController,
      MyWalletsFragmentDirections.actionNavigateToBalanceDetails(
        totalFiatBalance,
        appcoinsBalance,
        creditsBalance,
        ethereumBalance
      )
    )
  }

  fun navigateToSend() {
    val intent = TransferActivity.newIntent(fragment.requireContext())
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    openIntent(intent)
  }

  fun navigateToReceive(wallet: Wallet) {
    val intent = Intent(fragment.requireContext(), MyAddressActivity::class.java)
    intent.putExtra(C.Key.WALLET, wallet)
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    openIntent(intent)
  }

  fun navigateToNfts() {
    navigate(navController, MyWalletsFragmentDirections.actionNavigateToNfts())
  }

  fun navigateToVerifyPicker() {
    navigate(navController, MyWalletsFragmentDirections.actionNavigateToVerifyPicker())
  }

  fun navigateToVerifyCreditCard() {
    navigate(navController, MyWalletsFragmentDirections.actionNavigateToVerifyCreditCard(false))
  }

  fun navigateToBackup(
    walletAddress: String,
    walletName: String
  ) {
    val bundle = Bundle()
    bundle.putString(BackupWalletEntryFragment.WALLET_ADDRESS_KEY, walletAddress)
    bundle.putString(WALLET_NAME, walletName)
    navController.navigate(R.id.action_navigate_to_backup_entry_wallet, args = bundle)
  }

  fun navigateToQrCode(qrCodeView: View) {
    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
      fragment.requireActivity(),
      Pair(qrCodeView, "qr_code_image")
    )
    val extras = ActivityNavigatorExtras(options)
    navController.navigate(R.id.action_navigate_to_qr_code, null, null, extras)
  }

  fun navigateToRemoveWallet(navController: NavController) {
    navController.navigate(R.id.action_navigate_to_remove_wallet)
  }

  private fun openIntent(intent: Intent) = fragment.requireContext()
    .startActivity(intent)
}