package org.philblandford.ui.donate.compose

import androidx.annotation.RestrictTo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.inject
import org.philblandford.ascore2.android.billing.BillingManager
import org.philblandford.ui.LocalActivity
import org.philblandford.ui.R
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.LabelText

@Composable
fun Donate(dismiss: () -> Unit) {
  val billingManager: BillingManager = get()
  val activity = LocalActivity.current
  var sayThankYou by remember { mutableStateOf(false) }

  DialogTheme { modifier ->

    if (sayThankYou) {
      Column(modifier){
        Text(stringResource(R.string.thank_you), fontSize = 16.sp)
        Gap(0.5f)
        Button({ dismiss() }) {
          Text(stringResource(R.string.your_welcome))
        }
      }
    } else {
      Column(modifier.padding(10.dp)) {
        LabelText(stringResource(R.string.donate))
        Gap(0.5f)
        Text(stringResource(R.string.donate_text), fontSize = 16.sp)
        Gap(0.5f)
        billingManager.getProductDetails().forEach { (name, details) ->
          val (descr, price) = when (name) {
            "donate_small" -> stringResource(R.string.crisps) to (details?.oneTimePurchaseOfferDetails?.formattedPrice
              ?: "")

            "donate_medium" -> stringResource(R.string.beer) to (details?.oneTimePurchaseOfferDetails?.formattedPrice
              ?: "")

            "donate_large" -> stringResource(R.string.curry) to (details?.oneTimePurchaseOfferDetails?.formattedPrice
              ?: "")

            else -> "" to ""
          }
          activity?.let {
            if (price.isNotEmpty()) {
              Row(
                Modifier
                  .fillMaxWidth()
                  .clickable {
                    billingManager.purchase(name, activity) {
                      sayThankYou = true
                    }
                  },
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(descr, fontSize = 18.sp)
                Text(price, fontSize = 18.sp)
              }
              Gap(0.5f)
            }
          }
        }
      }
    }
  }
}