package org.philblandford.ui.create.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.philblandford.ui.R
import org.philblandford.ui.theme.DialogButton
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.Gap

@Composable
fun WizardFrame(
  titleRes: Int,
  next: () -> Unit,
  cancel: () -> Unit,
  content: @Composable BoxScope.() -> Unit
) {
  WizardFrame(stringResource(titleRes), next, cancel, content)
}

@Composable
fun WizardFrame(
  title: String,
  next: () -> Unit,
  cancel: () -> Unit,
  content: @Composable BoxScope.() -> Unit
) {
  DialogTheme {
    Box(Modifier.fillMaxSize()) {
      Surface(
        Modifier
          .fillMaxWidth()
          .fillMaxHeight(0.8f)
          .align(Alignment.Center),
        color = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(10)
      ) {
        Column(
          Modifier
            .fillMaxSize()
            .padding(20.dp)
        ) {
          Text(title, Modifier.fillMaxWidth(), style = MaterialTheme.typography.h1)
          Box(
            Modifier
              .fillMaxWidth()
              .weight(1f)
          ) {
            content()
          }
          Row(Modifier.align(Alignment.CenterHorizontally)) {
            DialogButton(stringResource(R.string.cancel), Modifier.width(100.dp)) {
              cancel()
            }
            Gap(1f)
            DialogButton(stringResource(R.string.next), Modifier.width(100.dp)) {
              next()
            }
          }
        }
      }
    }
  }
}

