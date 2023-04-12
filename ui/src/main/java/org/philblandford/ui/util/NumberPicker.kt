package org.philblandford.ui.util

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun NumberPicker(
  value:Int,
  modifier: Modifier = Modifier,
  range: List<Int> = (0..10).toList(),
  image: @Composable BoxScope.(Int, Modifier) -> Unit,
  onStateChanged: (Int) -> Unit = {},
) {
  val coroutineScope = rememberCoroutineScope()
  val numbersColumnHeight = 36.dp
  val halvedNumbersColumnHeight = numbersColumnHeight / 2
  val halvedNumbersColumnHeightPx = with(LocalDensity.current) { halvedNumbersColumnHeight.toPx() }

  fun animatedStateValue(offset: Float): Int = value - (offset / halvedNumbersColumnHeightPx).toInt()

  val animatedOffset = remember { Animatable(0f) }.apply {
    val idx = range.indexOf(value)
      val offsetRange = remember(idx, range) {
        val first = -(range.last() - idx) * halvedNumbersColumnHeightPx
        val last = -(range.first() - idx) * halvedNumbersColumnHeightPx
        first..last
      }
      updateBounds(offsetRange.start, offsetRange.endInclusive)
    }
  val coercedAnimatedOffset = animatedOffset.value % halvedNumbersColumnHeightPx
  val animatedStateValue = animatedStateValue(animatedOffset.value)

  Column(
    modifier = modifier
      .draggable(
        orientation = Orientation.Vertical,
        state = rememberDraggableState { deltaY ->
          coroutineScope.launch {
            animatedOffset.snapTo(animatedOffset.value + deltaY)
          }
        },
        onDragStopped = { velocity ->
          coroutineScope.launch {
            val endValue = animatedOffset.fling(
              initialVelocity = velocity,
              animationSpec = exponentialDecay<Float>(20f),
              adjustTarget = { target ->
                val coercedTarget = target % halvedNumbersColumnHeightPx
                val coercedAnchors =
                  listOf(-halvedNumbersColumnHeightPx, 0f, halvedNumbersColumnHeightPx)
                val coercedPoint = coercedAnchors.minByOrNull { abs(it - coercedTarget) }!!
                val base =
                  halvedNumbersColumnHeightPx * (target / halvedNumbersColumnHeightPx).toInt()
                coercedPoint + base
              }
            ).endState.value

            val newValue = animatedStateValue(endValue)
            onStateChanged(newValue)
            animatedOffset.snapTo(0f)
          }
        }
      )
  ) {
    val spacing = 4.dp

    val arrowColor = MaterialTheme.colors.onSecondary.copy(alpha = ContentAlpha.disabled)

   // Arrow(direction = ArrowDirection.UP, tint = arrowColor)

    Spacer(modifier = Modifier.height(spacing))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .offset { IntOffset(x = 0, y = coercedAnimatedOffset.roundToInt()) }
    ) {
      val baseLabelModifier = Modifier.align(Alignment.CenterEnd)
      range.getOrNull(animatedStateValue - 1)?.let {
        image(
          it, baseLabelModifier
            .offset(y = -halvedNumbersColumnHeight)
            .alpha(coercedAnimatedOffset / halvedNumbersColumnHeightPx)
        )
      }
      range.getOrNull(animatedStateValue)?.let {
        image(
          it, baseLabelModifier
            .alpha(1 - abs(coercedAnimatedOffset) / halvedNumbersColumnHeightPx)
        )
      }
      range.getOrNull(animatedStateValue + 1)?.let {
        image(
          it, baseLabelModifier
            .offset(y = halvedNumbersColumnHeight)
            .alpha(-coercedAnimatedOffset / halvedNumbersColumnHeightPx)
        )
      }
    }

    Spacer(modifier = Modifier.height(spacing))

  //  Arrow(direction = ArrowDirection.DOWN, tint = arrowColor)
  }
}

@Composable
private fun Label(text: String, modifier: Modifier) {
  Text(
    text = text,
    modifier = modifier.pointerInput(Unit) {
      detectTapGestures(onLongPress = {
        // FIXME: Empty to disable text selection
      })
    }
  )
}

private suspend fun Animatable<Float, AnimationVector1D>.fling(
  initialVelocity: Float,
  animationSpec: DecayAnimationSpec<Float>,
  adjustTarget: ((Float) -> Float)?,
  block: (Animatable<Float, AnimationVector1D>.() -> Unit)? = null,
): AnimationResult<Float, AnimationVector1D> {
  val targetValue = animationSpec.calculateTargetValue(value, initialVelocity)
  val adjustedTarget = adjustTarget?.invoke(targetValue)

  return if (adjustedTarget != null) {
    animateTo(
      targetValue = adjustedTarget,
      initialVelocity = initialVelocity,
      block = block
    )
  } else {
    animateDecay(
      initialVelocity = initialVelocity,
      animationSpec = animationSpec,
      block = block,
    )
  }
}

@Composable
@Preview
private fun Preview() {
  var state by remember{ mutableStateOf(0) }
  NumberPicker(state, Modifier.size(50.dp), range = (0..9).toList(), image = { num, modifier ->
    numberIds.getOrNull(num)?.let { id ->
      Image(painterResource(id), "", modifier.fillMaxSize())
    }
  }) {
    state = it
  }
}