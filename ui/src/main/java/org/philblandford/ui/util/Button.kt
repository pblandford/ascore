package org.philblandford.ui.util

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.philblandford.ui.common.block

enum class ButtonState {
  NONE,
  SELECTED,
  DIMMED;

  companion object {
    fun dimmed(yes: Boolean): ButtonState = if (yes) DIMMED else NONE
    fun selected(yes: Boolean): ButtonState = if (yes) SELECTED else NONE
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SquareButton(
  resource: Int,
  modifier: Modifier = Modifier,
  state: ButtonState = ButtonState.NONE,
  onLongPress: () -> Unit = {},
  border: Boolean = false,
  size: Dp = block(),
  foregroundColor: Color? = null,
  backgroundColor: Color? = null,
  tag: String = "",
  onClick: () -> Unit = {}
) {
  SquareImage(
    resource,
    modifier.combinedClickable(onClick = onClick, onLongClick = onLongPress),
    state,
    border,
    size,
    foregroundColor,
    backgroundColor
  )
}

@Composable
fun SquareImage(
  resource: Int,
  modifier: Modifier = Modifier,
  state: ButtonState = ButtonState.NONE,
  border: Boolean = false,
  size: Dp = block(),
  foregroundColor: Color? = null,
  backgroundColor: Color? = null,
) {

  val foreground = if (state == ButtonState.SELECTED) {
    backgroundColor ?: MaterialTheme.colorScheme.surface
  } else {
    foregroundColor ?: MaterialTheme.colorScheme.onSurface
  }
  val background = if (state == ButtonState.SELECTED) {
    foregroundColor ?: MaterialTheme.colorScheme.onSurface
  } else {
    backgroundColor ?: MaterialTheme.colorScheme.surface
  }

  var sizeMod = modifier.background(background).size(size)
  if (border) sizeMod = sizeMod.border(1.dp, MaterialTheme.colorScheme.onSurface)

  val colorFilter = when (state) {
    ButtonState.DIMMED -> ColorFilter.tint(foreground.copy(alpha = 0.25f))
    else -> ColorFilter.tint(foreground)
  }

  Box(
    sizeMod,
  ) {
    Image(
      painterResource(resource), "",
      Modifier
        .size(size),
      colorFilter = colorFilter
    )

  }
}
