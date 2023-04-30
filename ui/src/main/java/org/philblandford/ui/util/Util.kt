//@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package org.philblandford.ui.util

import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.log.ksLogv
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.util.ButtonState.Companion.selected

@Composable
fun styledBorder(): BorderStroke {
    return BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface)
}

@Composable
fun ToggleButton(
    resource: Int,
    selected: Boolean, toggle: () -> Unit,
    border: Boolean = false,
    tag: String = ""
) {
    SquareButton(
        resource = resource,
        border = border,
        state = selected(selected),
        tag = tag,
        onClick = toggle
    )
}

@Composable
fun ThemeBox(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    border: BorderStroke? = null,
    padding: Dp = border?.width ?: 0.dp,
    children: @Composable() () -> Unit = {}
) {
    var mod2 = modifier
        .background(backgroundColor, shape)
        .padding(padding)
    border?.let { mod2 = mod2.border(border) }
    Box(mod2) { children() }
}


@Composable
fun ToggleColumn(
    ids: List<Int>,
    modifier: Modifier = Modifier,
    spacing: Dp = 0.dp,
    border: Boolean = true,
    tag: (Int) -> String = { "" },
    size: @Composable() (Int) -> Dp = { block() },
    selected: () -> Int?,
    onSelect: (Int?) -> Unit
) {

    Box(modifier.border(if (border) 1.dp else 0.dp, Color.White)) {
        Column {
            ids.withIndex().map { iv ->
                SquareButton(
                    resource = iv.value,
                    size = size(iv.index),
                    state = selected(selected() == iv.index),
                    tag = "Button ${tag(iv.index)}",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        if (selected() == iv.index) {
                            onSelect(null)
                        } else {
                            onSelect(iv.index)
                        }
                    })
                Spacer(modifier = Modifier.width(spacing))
            }
        }
    }
}


@Composable
fun Stoppable(
    enable: Boolean, tag: String = "",
    disabledColor: Color = org.philblandford.ui.theme.compose.disabledColor,
    children: @Composable() () -> Unit
) {
    Box {
        Box(Modifier.testTag(tag)) {
            children()
        }
        if (!enable) {
            ksLogv("$tag is disabled")
            Box(
                Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {})
                    }
                    .testTag("$tag disabled")
                    .background(disabledColor)
            )
        }
    }
}


@Composable
fun UpDownDependent(modifier: Modifier, isUp: Boolean, set: (Boolean) -> Unit) {
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
        UpDownColumn(isUp, set, modifier)
    } else {
        UpDownRow(isUp, set, modifier)
    }
}

@Composable
fun UpDownColumn(isUp: Boolean, set: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        ToggleColumn(ids = listOf(R.drawable.up, R.drawable.down), selected = {
            if (isUp) 0 else 1
        }, onSelect = { set(it == 0) }, tag = {
            if (it == 0) "Up" else "Down"
        })
    }
}

@Composable
fun UpDownRow(isUp: Boolean, set: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        ToggleRow(ids = listOf(R.drawable.up, R.drawable.down), selected =
        if (isUp) 0 else 1, onSelect = { set(it == 0) }, tag = {
            if (it == 0) "Up" else "Down"
        })
    }
}

@Composable
fun Gap(size: Dp) = Spacer(Modifier.size(size))

@Composable
fun Gap(blocks: Float) = Gap(block(blocks))

@Composable
fun LeftRight(left: () -> Unit, right: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.border(styledBorder())) {
        SquareButton(
            resource = R.drawable.left_arrow,
            onClick = { left() }, tag = "LeftArrow"
        )
        SquareButton(
            resource = R.drawable.right_arrow,
            onClick = { right() }, tag = "RightArrow"
        )
    }
}

@Composable
fun ThemeButton(id: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier) {
        Text(stringResource(id = id), color = MaterialTheme.colorScheme.onPrimary)
    }
}


@Composable
fun StyledBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(7.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(2.dp)
    ) {
        Box(
            Modifier
                .padding(1.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(7.dp))
                .padding(5.dp),
        ) {
            content()
        }
    }
}


fun String.nullIfEmpty() = if (isEmpty()) null else this


val NoBorder = BorderStroke(0.dp, Color.Transparent)

@Composable
fun ScrollableColumn(
    modifier: Modifier,
    scrollState: ScrollState = rememberScrollState(),
    alignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit
) {
    Column(modifier.verticalScroll(scrollState), horizontalAlignment = alignment) {
        content()
    }
}
