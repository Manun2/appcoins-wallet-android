package com.appcoins.wallet.billing.adyen.amazon

import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.commons.Logger
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.*
import javax.inject.Inject

class AmazonPayRepository @Inject constructor(
  private val amazonPayApi: AmazonPayApi,
  private val amazonPayMapper: AmazonPayMapper,
  private val logger: Logger
  ) {

//  fun getAmazonPage(
//    walletAddress: String,
//    walletSignature: String,
//    value: String,
//    currency: String
//  ): Single<AmazonSession> {
//    return amazonPayApi.getAmazonPage(walletAddress, walletSignature, value, currency)
//      .map { amazonPayMapper.map(it) }
//      .onErrorReturn {
//        logger.log("AmazonPayRepository", it.message)
//        amazonPayMapper.mapPaymentModelError(it)
//      }
//  }

  fun concludeAmazonCheckout(
    sessionId: String,
    value: String, currency: String, reference: String?, walletAddress: String,
    origin: String?, packageName: String?, metadata: String?, sku: String?,
    callbackUrl: String?, transactionType: String, developerWallet: String?,
    entityOemId: String?, entityDomain: String?, entityPromoCode: String?,
    userWallet: String?,
    walletSignature: String,
    referrerUrl: String?
  ): Single<AmazonPayRedirectUrl> {
    return amazonPayApi.concludeAmazonCheckout(
      sessionId,
      AmazonPayment(
        callbackUrl = callbackUrl,
        domain = packageName,
        metadata = metadata,
        origin = origin,
        sku = sku,
        reference = reference,
        type = transactionType,
        currency = currency,
        value = value,
        developer = developerWallet,
        entityOemId = entityOemId,
        entityDomain = entityDomain,
        entityPromoCode = entityPromoCode,
        user = userWallet,
        referrerUrl = referrerUrl
      )
    )
      .map { amazonPayMapper.map(it) }
//      .onErrorReturn {     //TODO
//        logger.log("AmazonPayRepository", it.message)
//        amazonPayMapper.mapPaymentModelError(it)
//      }
  }

  fun makeAmazonPayment(
    sessionId: String,
    value: String, currency: String, reference: String?, walletAddress: String,
    origin: String?, packageName: String?, metadata: String?, sku: String?,
    callbackUrl: String?, transactionType: String, developerWallet: String?,
    entityOemId: String?, entityDomain: String?, entityPromoCode: String?,
    userWallet: String?,
    walletSignature: String,
    referrerUrl: String?
  ): Single<AmazonPayRedirectUrl> {
    return amazonPayApi.concludeAmazonCheckout(
      sessionId,
      AmazonPayment(
        callbackUrl = callbackUrl,
        domain = packageName,
        metadata = metadata,
        origin = origin,
        sku = sku,
        reference = reference,
        type = transactionType,
        currency = currency,
        value = value,
        developer = developerWallet,
        entityOemId = entityOemId,
        entityDomain = entityDomain,
        entityPromoCode = entityPromoCode,
        user = userWallet,
        referrerUrl = referrerUrl
      )
    )
      .map { amazonPayMapper.map(it) }
//      .onErrorReturn {    //TODO
//        logger.log("AmazonPayRepository", it.message)
//        amazonPayMapper.mapPaymentModelError(it)
//      }
  }

  interface AmazonPayApi {

    // will be called from webView directly
//    @GET("8.20230101/gateways/adyen_v2/amazonpay/login")  // TODO check integration
//    fun getAmazonPage(
//      @Query("value") value: String,
//      @Query("currency") currency: String,
//      @Query("callback") callback: String
//    ): Single<CreateAmazonSessionResponse>

    @POST("8.20230101/gateways/adyen_v2/amazonpay/xxxxxxx")  // TODO check integration, TODO change endpoint
    fun concludeAmazonCheckout(
//      @Query("wallet.address") walletAddress: String,
//      @Query("wallet.signature") walletSignature: String,
      @Body sessionId: String,
      @Body amazonPayment: AmazonPayment
    ): Single<AmazonPayRedirectResponse>

    @POST("8.20200815/gateways/adyen_v2/transactions")  // TODO check integration
    fun makeAmazonPayment(
      @Query("wallet.address") walletAddress: String,
      @Query("wallet.signature") walletSignature: String,
      @Body amazonToken: String,   //TODO check if it goes on the payment object
      @Body amazonPayment: AmazonPayment
    ): Single<Transaction>   //TODO result model

  }

  data class AmazonPayment(
    @SerializedName("callback_url") val callbackUrl: String?,
    @SerializedName("domain") val domain: String?,
    @SerializedName("metadata") val metadata: String?,
    @SerializedName("origin") val origin: String?,
    @SerializedName("product") val sku: String?,
    @SerializedName("reference") val reference: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("price.currency") val currency: String?,
    @SerializedName("price.value") val value: String?,
    @SerializedName("wallets.developer") val developer: String?,
    @SerializedName("entity.oemid") val entityOemId: String?,
    @SerializedName("entity.domain") val entityDomain: String?,
    @SerializedName("entity.promo_code") val entityPromoCode: String?,
    @SerializedName("wallets.user") val user: String?,
    @SerializedName("referrer_url") val referrerUrl: String?
  )

}