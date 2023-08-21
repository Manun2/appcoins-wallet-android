package com.appcoins.wallet.ui.widgets

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletColors

@Composable
fun RewardsActions(
  onClickEskills: () -> Unit,
  onClickPromoCode: () -> Unit,
  onClickGiftCard: () -> Unit,
  onClickOfferWall: () -> Unit,
) {
  val scrollState = rememberScrollState()
  Row(
    modifier = Modifier
      .horizontalScroll(scrollState)
  ) {
    Spacer(
      modifier = Modifier
        .width(8.dp)
    )
    ActionCard(
      image = R.drawable.ic_challenge_reward,
      title = R.string.challenge_reward_card_title,
      description = R.string.challenge_reward_card_body,
      titleStyle = MaterialTheme.typography.titleSmall,
      onClick = onClickOfferWall,
    )
    ActionCard(
      image = R.drawable.ic_eskills,
      title = R.string.rewards_eskills_card_title,
      description = R.string.rewards_eskills_card_body,
      onClick = onClickEskills,
    )
    ActionCard(
      image = R.drawable.ic_promocode,
      title = R.string.rewards_promo_code_card_title,
      description = R.string.rewards_promo_code_card_body,
      onClick = onClickPromoCode,
    )
    ActionCard(
      image = R.drawable.ic_giftcard,
      title = R.string.transaction_type_gift_card,
      description = R.string.gift_card_title,
      onClick = onClickGiftCard,
    )
    Spacer(
      modifier = Modifier
        .width(16.dp)
    )
  }
}

@Composable
fun ActionCard(
  @DrawableRes image: Int,
  @StringRes title: Int,
  @StringRes description: Int,
  titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier
      .padding(
        start = 8.dp,
        top = 16.dp
      )
      .width(166.dp)
      .height(200.dp)
      .clickable { onClick() },
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Image(
        painter = painterResource(image),
        "Earn Money",
        modifier = Modifier
          .height(54.dp)
          .width(54.dp)
      )
      Text(
        text = stringResource(id = title),
        style = titleStyle,
        color = WalletColors.styleguide_white
      )
      Text(
        text = stringResource(id = description),
        style = MaterialTheme.typography.bodyMedium,
        color = WalletColors.styleguide_dark_grey,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Preview
@Composable
private fun PreviewRewardsActions() {
  RewardsActions(
    { },
    { },
    { },
    { },
  )
}
