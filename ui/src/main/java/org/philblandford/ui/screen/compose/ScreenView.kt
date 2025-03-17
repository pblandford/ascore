package org.philblandford.ui.screen.compose

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import org.philblandford.ascore2.features.scorelayout.usecases.ScoreLayout
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.screen.viewmodels.ScreenEffect
import org.philblandford.ui.screen.viewmodels.ScreenInterface
import org.philblandford.ui.screen.viewmodels.ScreenModel
import org.philblandford.ui.screen.viewmodels.ScreenViewModel

@Composable
fun ScreenView(
  vertical: Boolean,
  center: Boolean,
  currentPage: MutableState<Int>,
  onScoreEmpty: () -> Unit,
  changeMethod: () -> Unit
) {
  VMView(ScreenViewModel::class.java) { model, iface, effects ->
    ScreenViewInternal(vertical, center, model, iface, effects, currentPage, onScoreEmpty, changeMethod)
  }
}

@Composable
private fun ScreenViewInternal(
  vertical: Boolean,
  center: Boolean,
  model: ScreenModel,
  iface: ScreenInterface,
  effects: Flow<ScreenEffect>,
  currentPage: MutableState<Int>,
  onScoreEmpty: () -> Unit,
  changeMethod: () -> Unit
) {


  BoxWithConstraints(Modifier.fillMaxSize()) {

    val defaultScale = calculateDefaultScale(model.scoreLayout)
    val scale = remember(defaultScale) { mutableStateOf(defaultScale) }
    if (vertical) {
      ScreenViewVertical(
        model,
        iface,
        effects,
        scale,
        defaultScale,
        maxWidth,
        maxHeight,
        currentPage,
        onScoreEmpty,
        changeMethod
      )
    } else {
      ScreenViewHorizontal(
        model,
        iface,
        Modifier.align(if (center) Alignment.Center else Alignment.TopCenter),
        effects,
        scale,
        defaultScale,
        maxWidth,
        maxHeight,
        currentPage,
        onScoreEmpty,
        changeMethod
      )
    }
  }
}

@Composable
private fun BoxWithConstraintsScope.calculateDefaultScale(scoreLayout: ScoreLayout): Float {
  val density = LocalDensity.current.density
  val margin = 5.dp
  val rawScreenWidth = (maxWidth - (margin * 2)) * density
  return (rawScreenWidth / scoreLayout.width).value
}
