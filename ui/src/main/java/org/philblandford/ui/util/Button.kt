package org.philblandford.ui.util

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.philblandford.ascore.android.ui.style.unselectedColor
import org.philblandford.ui.common.block


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SquareButton(
  resource: Int,
  modifier: Modifier = Modifier,
  dim: Boolean = false,
  onLongPress: () -> Unit = {},
  border: Boolean = false,
  size: Dp = block(),
  backgroundColor: Color? = null,
  foregroundColor: Color? = null,
  tag: String = "",
  onClick: () -> Unit = {}
) {
    SquareImage(
      resource,
      modifier.combinedClickable(onClick = onClick, onLongClick = onLongPress),
      dim,
      border,
      size,
      backgroundColor,
      foregroundColor,
      tag,
    )
}

@Composable
fun SquareImage(
  resource: Int,
  modifier: Modifier = Modifier,
  dim: Boolean = false,
  border: Boolean = false,
  size: Dp = block(),
  backgroundColor: Color? = null,
  foregroundColor: Color? = null,
  tag: String = ""
) {
  var sizeMod = modifier.size(size)
  if (border) sizeMod = sizeMod.border(1.dp, MaterialTheme.colors.onSurface)
  Box(
    sizeMod,
  ) {
    val realTag = if (dim) "$tag on" else tag
    val foreground = foregroundColor ?: MaterialTheme.colors.onSurface
    Image(
      painterResource(resource), "",
      Modifier
        .testTag(realTag)
        .size(size),
      colorFilter = ColorFilter.tint(if (dim) foreground.copy(alpha = 0.25f) else foreground)
    )

  }
}
