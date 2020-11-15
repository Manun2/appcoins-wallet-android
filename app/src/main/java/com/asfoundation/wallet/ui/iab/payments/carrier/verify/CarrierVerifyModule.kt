package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.StringProvider
import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoLoader
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

@Module
class CarrierVerifyModule {

  @Provides
  fun providesCarrierVerifyNavigator(fragment: CarrierVerifyFragment): CarrierVerifyNavigator {
    return CarrierVerifyNavigator(fragment.requireFragmentManager())
  }

  @Provides
  fun providesCarrierVerifyPhoneData(fragment: CarrierVerifyFragment): CarrierVerifyData {
    fragment.arguments!!.apply {
      return CarrierVerifyData(getString(CarrierVerifyFragment.DOMAIN_KEY)!!,
          getString(CarrierVerifyFragment.ORIGIN_KEY),
          getString(CarrierVerifyFragment.TRANSACTION_TYPE_KEY) ?: "",
          getString(CarrierVerifyFragment.TRANSACTION_DATA_KEY) ?: "",
          getString(CarrierVerifyFragment.CURRENCY_KEY)!!,
          getSerializable(CarrierVerifyFragment.FIAT_AMOUNT_KEY) as BigDecimal,
          getSerializable(CarrierVerifyFragment.APPC_AMOUNT_KEY) as BigDecimal,
          getSerializable(CarrierVerifyFragment.BONUS_AMOUNT_KEY) as BigDecimal,
          getString(CarrierVerifyFragment.SKU_DESCRIPTION)!!)
    }
  }

  @Provides
  fun providesCarrierVerifyPresenter(fragment: CarrierVerifyFragment,
                                     data: CarrierVerifyData,
                                     navigator: CarrierVerifyNavigator,
                                     interactor: CarrierInteractor,
                                     stringProvider: StringProvider,
                                     applicationInfoLoader: ApplicationInfoLoader): CarrierVerifyPresenter {
    return CarrierVerifyPresenter(CompositeDisposable(), fragment as CarrierVerifyView, data,
        navigator, interactor, applicationInfoLoader, stringProvider, CurrencyFormatUtils(),
        AndroidSchedulers.mainThread(), Schedulers.io())
  }
}