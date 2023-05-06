package org.philblandford.ascore2.android.billing


import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.philblandford.ascore2.util.failure
import org.philblandford.ascore2.util.ok
import timber.log.Timber

class BillingManager(context: Context) {

  private val productDetails = mutableMapOf<String, ProductDetails?>()
  private val coroutineScope = CoroutineScope(Dispatchers.IO)
  private var productIDs: List<String> = listOf("donate_small", "donate_medium", "donate_large")
  private var completionCallback:()->Unit = {}

  private val purchasesUpdatedListener =
    PurchasesUpdatedListener { billingResult, purchases ->
      Timber.e("Purchases update listener $purchases")
    }

  private val purchaseResponseListener: PurchasesResponseListener =
    PurchasesResponseListener { result, purchases ->
      result.toKotlinResult().onSuccess {

        Timber.e("PurchaseResponseListener $result $purchases")
        acknowledgePurchases(purchases)
      }.onFailure { Timber.e("PurchaseResponseListener $it") }
    }


  private fun acknowledgePurchases(purchases: List<Purchase>) {
    purchases.forEach { purchase ->
      if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
        if (!purchase.isAcknowledged) {
          val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
          coroutineScope.launch {
            withContext(Dispatchers.IO) {
              billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
              completionCallback()
            }
          }
        }
      }
    }
  }

  private var billingClient = BillingClient.newBuilder(context)
    .setListener(purchasesUpdatedListener).setListener { result, purchases ->

      result.toKotlinResult().onSuccess {
        if (purchases != null) {
          purchaseResponseListener.onQueryPurchasesResponse(result, purchases)
        } else {
          Timber.e("Purchases is null")
        }
      }
    }
    .enablePendingPurchases()
    .build()

  fun purchase(iap: String, activity: Activity, onSuccess:()->Unit): Result<Unit> {
    completionCallback = onSuccess
    return getProductDetails(iap)?.let { params ->
      val flowParams =
        BillingFlowParams.newBuilder().setProductDetailsParamsList(params)
          .build()
      billingClient.launchBillingFlow(activity, flowParams).toKotlinResult()
    } ?: failure("SKU details not set")
  }

  private fun getProductDetails(iap: String): List<ProductDetailsParams>? {
    return productDetails[iap]?.let { details ->
      listOf(
        ProductDetailsParams.newBuilder()
          .setProductDetails(details)
          .build()
      )
    }
  }

  fun getProductDetails() = productDetails

  fun start() {

    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingServiceDisconnected() {

      }

      override fun onBillingSetupFinished(billingResult: BillingResult) {
        Timber.e("Billing setup finished ${billingResult.debugMessage}")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
          coroutineScope.launch {
            queryProductDetails()
          }
        }
      }
    })
  }

  private fun getQueryParams(): QueryProductDetailsParams {
    return QueryProductDetailsParams.newBuilder()
      .setProductList(
        productIDs.map { productID ->
          QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productID)
            .setProductType(ProductType.INAPP)
            .build()
        }
      )
      .build()
  }

  private suspend fun queryProductDetails() {

    withContext(Dispatchers.IO) {
      val result = billingClient.queryProductDetails(getQueryParams())

      if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
        Timber.e("Billing query ok $result")
        result.productDetailsList?.forEach {
          productDetails[it.productId] = it
        }
      } else {
        Timber.e("Billing query failed $result")
      }
    }
  }

  private fun queryUserPurchases() {
    billingClient.queryPurchasesAsync(
      QueryPurchasesParams.newBuilder()
        .setProductType(ProductType.INAPP).build(), purchaseResponseListener
    )
  }

  private fun BillingResult.toKotlinResult(): Result<Unit> {
    return if (responseCode == BillingClient.BillingResponseCode.OK) {
      Unit.ok()
    } else {
      return failure(debugMessage)
    }
  }
}