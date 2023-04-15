package org.philblandford.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun isCompact():Boolean {
  val config = LocalConfiguration.current
  return config.screenWidthDp > 840
}