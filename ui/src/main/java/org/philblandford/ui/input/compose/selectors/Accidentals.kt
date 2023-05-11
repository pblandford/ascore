package org.philblandford.ui.input.compose.selectors

import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.types.Accidental
import org.philblandford.ui.util.ImageGridDropdown
import org.philblandford.ui.util.accidentalIds

@Composable
fun AccidentalSpinner(
  selectedAccidental: Accidental,
  accidentals: List<Accidental> = Accidental.values().toList(),
  setAccidental: (Accidental) -> Unit
) {
  val ids = accidentalIds.filter { accidentals.contains(it.second) }
  ImageGridDropdown(
    rows = if (accidentals.size % 2 == 0) accidentals.size / 2 else accidentals.size,
    columns = if (accidentals.size % 2 == 0) 2 else 1,
    images = ids.map { it.first },
    selected =  ids.indexOfFirst { it.second == selectedAccidental },
    onSelect = { setAccidental(ids[it].second) }
  )
}