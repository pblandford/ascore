package org.philblandford.ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScrollableRow(
  modifier: Modifier,
  scrollState: ScrollState = rememberScrollState(),
  content: @Composable () -> Unit
) {
  Row(modifier.horizontalScroll(scrollState)) {
    content()
  }
}