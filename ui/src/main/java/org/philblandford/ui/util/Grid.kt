import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.philblandford.ui.common.block
import org.philblandford.ui.util.ButtonState
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.ThemeBox

@Composable
fun GridSelection(
  images: List<Int>,
  rows: Int,
  columns: Int,
  modifier: Modifier = Modifier,
  size: Dp = block(),
  gap:Dp = 2.dp,
  border: Boolean = false,
  itemBorder: Boolean = false,
  tag: (Int) -> String = { "" },
  selected: () -> Int? = { null },
  onSelect: (Int) -> Unit
) {
  val sizeMod = modifier.size((size + gap) * columns, (size + gap) * rows)
  val borderMod = if (border) sizeMod.border(2.dp, MaterialTheme.colors.onSurface) else sizeMod
  ThemeBox(
    modifier = borderMod
  ) {
    Column {
      (0 until rows).forEach { row ->
        Row {
          (0 until columns).forEach { column ->
            val idx = (row * columns) + column
            Card(elevation = 2.dp) {
              SquareButton(resource = images[idx], size = size,
                tag = tag(idx),
                border = itemBorder,
                state = ButtonState.selected(selected() != null && selected() == idx),
                onClick = { onSelect(idx) })
            }
            Gap(gap)
          }
        }
        Gap(gap)
      }
    }
  }
}

