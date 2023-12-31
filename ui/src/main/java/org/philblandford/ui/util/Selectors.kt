package org.philblandford.ui.util

import GridSelection
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.types.ClefType
import com.philblandford.kscore.log.ksLogv
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.util.ButtonState.Companion.selected


@Composable
fun DurationSelector(
  duration:Duration, setDuration: (Duration) -> Unit,
  size: Dp = block(),
) {
  val resId = durationIds.find { it.second == duration }?.first ?: R.drawable.crotchet_rest

  Box(Modifier.border(styledBorder())) {
    Row() {
      SquareButton(
        R.drawable.arrow_left, tag = "ButtonLeft",
        size = size
      ) {
        val selection = durationToSelection(duration)
        ksLogv("ButtonLeft $selection")

        if (selection > 0) {
          setDuration(selectionToDuration(selection - 1))
        }
      }
      SquareButton(resId, tag = "Display $duration", size = size * 0.9f) {}
      SquareButton(R.drawable.arrow_right, tag = "ButtonRight", size = size) {
        val selection = durationToSelection(duration)
        if (selection < durationIds.size - 1) {
          setDuration(selectionToDuration(selection + 1))
        }
      }
    }
  }
}

private fun selectionToDuration(selection: Int): Duration {
  return durationIds.get(selection).second
}

private fun durationToSelection(duration: Duration): Int {
  return durationIds.indexOfFirst { it.second == duration }
}

@Composable
fun DotToggle(
  numDots: Int, setDots: (Int) -> Unit
) {
  SquareButton(resource = if (numDots < 2) R.drawable.onedot else R.drawable.twodot,
    state = selected(numDots > 0),
    tag = if (numDots < 2) "Onedot" else "Twodot",
    onLongPress = { setDots(2) }) {
    if (numDots > 0) setDots(0) else setDots(1)
  }
}


@Composable
fun ClefSpinner(
  selectedClef: ClefType,
  border: Boolean = false,
  tag: String = "",
  setClef: (ClefType) -> Unit
) {
  val clefList = clefIds.toList()
  ImageGridDropdown(images = clefList.map { it.first },
    rows = 5,
    columns = 2,
    border = border,
    tag = { "$tag ${clefList.getOrNull(it)?.second}" },
    selected = clefList.indexOfFirst { it.second == selectedClef },
    onSelect = { idx ->
      clefList.getOrNull(idx)?.second?.let { clef ->
        setClef(clef)
      }
    })
}

@Composable
fun ClefSelector(selected: () -> ClefType?, onSelect: (ClefType) -> Unit) {
  IdSelector(ids = clefIds, rows = 2, selected = selected, onSelect = onSelect)
}

@Composable
fun <T> IdSelector(
  ids: List<Pair<Int, T>>,
  rows: Int = 1,
  selected: () -> T?,
  onSelect: (T) -> Unit
) {
  GridSelection(images = ids.map { it.first },
    rows = rows, columns = ids.size / rows,
    tag = {
      ids[it].second.toString()
    },
    itemBorder = true,
    selected = { ids.indexOfFirst { it.second == selected() } },
    onSelect = {
      onSelect(ids[it].second)
    })
}

