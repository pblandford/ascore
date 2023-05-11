package org.philblandford.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import org.philblandford.ui.R

@Composable
fun StandardAlert(text:String, dismiss:()->Unit) {
  AlertDialog(onDismissRequest = { dismiss() },
    {
      Button({ dismiss() }) {
        Text(stringResource(R.string.ok))
      }
    },
    shape = RoundedCornerShape(10),
    modifier = Modifier.
    clip(RoundedCornerShape(10)).
    background(MaterialTheme.colorScheme.surface),
    title = {
      Text(
        text,
        style = MaterialTheme.typography.bodyMedium
      )
    })
}