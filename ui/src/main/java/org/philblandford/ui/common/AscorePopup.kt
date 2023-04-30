package org.philblandford.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.philblandford.ui.theme.compose.AscoreTheme

@Composable
fun AscorePopup(modifier: Modifier = Modifier, content:@Composable ()->Unit) {
  Box(modifier.border(3.dp, MaterialTheme.colorScheme.background).wrapContentSize()) {
    Box(modifier.padding(3.dp).border(3.dp, MaterialTheme.colorScheme.primary).
    wrapContentSize()) {
        content()
    }
  }
}

@Preview
@Composable
private fun Preview() {
  AscoreTheme {
    AscorePopup() {
      Box(Modifier.size(200.dp)) {
        Text("Hello", Modifier.align(Alignment.Center))
      }
    }
  }
}