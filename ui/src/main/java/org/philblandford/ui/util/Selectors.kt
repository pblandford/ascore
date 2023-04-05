package org.philblandford.ui.util

import GridSelection
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLogv
import org.philblandford.ui.common.block
import org.philblandford.ui.R


@Composable
fun DurationSelector(
  getDuration: () -> Duration, setDuration: (Duration) -> Unit,
  size: Dp = block(),
  tag: () -> String = { "" }
) {
  val resId = durationIds.find { it.second == getDuration() }?.first ?: R.drawable.crotchet_rest

  Box(Modifier.border(styledBorder())) {
    Row() {
      SquareButton(
        R.drawable.arrow_left, tag = "ButtonLeft",
        size = size
      ) {
        val selection = durationToSelection(getDuration())
        ksLogv("ButtonLeft $selection")

        if (selection > 0) {
          setDuration(selectionToDuration(selection - 1))
        }
      }
      SquareButton(resId, tag = "Display ${getDuration()}", size = size) {}
      SquareButton(R.drawable.arrow_right, tag = "ButtonRight", size = size) {
        val selection = durationToSelection(getDuration())
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
    dim = numDots > 0,
    tag = if (numDots < 2) "Onedot" else "Twodot",
    onLongPress = { setDots(2) }) {
    if (numDots > 0) setDots(0) else setDots(1)
  }
}


@Composable
fun ClefSpinner(
  selectedClef: () -> ClefType,
  border: Boolean = false,
  tag: String = "",
  setClef: (ClefType) -> Unit
) {
  val clefList = clefIds.toList()
  ImageGridDropdown(images = clefList.map { it.first },
    rows = 5,
    columns = 2,
    border = border,
    tag = { "$tag ${clefList[it].second}" },
    selected = { clefList.indexOfFirst { it.second == selectedClef() } },
    onSelect = {
      setClef(clefList[it].second)
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

