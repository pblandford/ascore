package org.philblandford.ui.create.compose

import androidx.compose.runtime.Composable
import org.philblandford.ui.create.compose.compact.CreateScoreCompact
import org.philblandford.ui.create.compose.expanded.CreateScoreExpanded
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.main.window.compact
import org.philblandford.ui.main.window.medium

@Composable
fun CreateScore(done:()->Unit) {
  if (LocalWindowSizeClass.current.medium()) {
    CreateScoreExpanded(done)
  } else {
    CreateScoreCompact(done)
  }
}