package com.appcoins.wallet.billing.adyen.amazon

import com.appcoins.wallet.commons.Logger
import io.reactivex.Single
import retrofit2.http.*
import javax.inject.Inject

class AmazonPayRepository @Inject constructor(
  private val amazonPayApi: AmazonPayApi,
  private val amazonPayMapper: AmazonPayMapper,
  private val logger: Logger
  ) {

  fun createAmazonToken(
    walletAddress: String,
    walletSignature: String,
    value: String,
    currency: String
  ): Single<String> {
    return amazonPayApi.createAmazonToken(walletAddress, walletSignature, value, currency)
      .map { amazonPayMapper.map(it) }  // TODO
      .onErrorReturn {
        logger.log("AmazonPayRepository", it.message)
        amazonPayMapper.mapPaymentModelError(it)
      }
  }

  interface AmazonPayApi {

    @POST("8.20200815/gateways/adyen_v2/transactions")
    fun createAmazonToken(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body value: String,
      @Body currency: String
    ): Single<CreateAmazonSessionResponse>

  }

}