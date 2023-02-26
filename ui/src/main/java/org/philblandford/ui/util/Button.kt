package org.philblandford.ui.util

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import org.philblandford.ui.common.block


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SquareButton(
  resource: Int,
  modifier: Modifier = Modifier,
  selected: Boolean = false,
  onLongPress: () -> Unit = {},
  border: Boolean = false,
  size: Dp = block(),
  size2d: Pair<Dp, Dp>? = null,
  backgroundColor: Color? = null,
  foregroundColor: Color? = null,
  tag: String = "",
  onClick: () -> Unit = {}
) {
  SquareImage(
    resource,
    modifier.combinedClickable(onClick = onClick, onLongClick = onLongPress),
    selected,
    border,
    size,
    size2d,
    backgroundColor,
    foregroundColor,
    tag,
  )
}

@Composable
fun SquareImage(
  resource: Int,
  modifier: Modifier = Modifier,
  selected: Boolean = false,
  border: Boolean = false,
  size: Dp = block(),
  size2d: Pair<Dp, Dp>? = null,
  backgroundColor: Color? = null,
  foregroundColor: Color? = null,
  tag: String = ""
) {
  var sizeMod = size2d?.let { modifier.size(it.first, it.second) } ?: modifier.size(size)
  if (border) sizeMod = sizeMod.border(1.dp, Color.White)
  Box(
    sizeMod.background(
      backgroundColor
        ?: if (selected) MaterialTheme.colors.secondary else Color.Transparent,
      shape = RoundedCornerShape(5)
    ),
  ) {
    val realTag = if (selected) "$tag on" else tag
    Image(
      painterResource(resource), "",
      Modifier
        .testTag(realTag)
        .size(size)
        .padding(2.dp),
      colorFilter = ColorFilter.tint(foregroundColor ?: MaterialTheme.colors.onSurface)
    )
  }
}
