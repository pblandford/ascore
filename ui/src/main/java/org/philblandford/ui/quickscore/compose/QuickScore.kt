package org.philblandford.ui.quickscore.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.quickscore.viewmodel.QuickScoreViewModel
import org.philblandford.ui.theme.DialogButton
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.Gap

@Composable
fun QuickScore(done:()->Unit) {
  VMView(QuickScoreViewModel::class.java) { _, iface, _ ->

    DialogTheme { modifier ->
      Column(modifier.fillMaxWidth()) {
        Text(stringResource(R.string.quick_score_text), style = MaterialTheme.typography.bodyLarge)
        Gap(1f)
        DialogButton(stringResource(R.string.ok), Modifier.align(Alignment.End)) {
          iface.create()
          done()
        }
      }
    }
  }
}