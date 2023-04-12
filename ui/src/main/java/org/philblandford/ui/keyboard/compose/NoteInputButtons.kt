package org.philblandford.ui.keyboard.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.philblandford.kscore.api.NoteInputDescriptor
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.types.Accidental
import com.philblandford.kscore.engine.types.GraceInputMode
import com.philblandford.kscore.engine.types.NoteHeadType
import org.philblandford.ui.R
import org.philblandford.ui.common.Gap
import org.philblandford.ui.common.block
import org.philblandford.ui.keyboard.compose.selectors.*
import org.philblandford.ui.keyboard.viewmodel.InputModel
import org.philblandford.ui.util.*


interface NoteInputButtonsInterface : GraceSelectorInterface {
  fun toggleOctaveShift()
  fun toggleDotted()
  fun toggleTieToLast()
  fun setNoteHead(noteHeadType: NoteHeadType)
  fun toggleSmall()
  fun toggleNoEdit()
  fun setDuration(duration: Duration)
  fun setNumDots(dots: Int)
  fun setAccidental(accidental: Accidental)
  fun moveMarker(left: Boolean)
  fun insertRest()
  fun deleteRest()
}

@Composable
fun NoteInputButtonsColumn(
  model: InputModel,
  iface: NoteInputButtonsInterface
) {

  val showMore = remember { mutableStateOf(false) }

  Column(
    Modifier
      .fillMaxWidth()
      .testTag("NoteInputButtons")
  ) {
    if (showMore.value) {
      MoreRow(model.noteInputDescriptor, iface)
    }
    Gap(0.1)
    MainRow(
      true,
      showMore.value,
      { showMore.value = !showMore.value },
      Modifier.fillMaxWidth(), model.noteInputDescriptor,
      model.accidentals, iface
    )
    Gap(0.1f)
    Box(
      Modifier
        .height(block(0.1))
        .fillMaxWidth()
        .background(MaterialTheme.colors.onSurface)
    )
    Gap(0.1f)
  }
}

@Composable
fun NoteInputButtonsRow(
  model: InputModel,
  iface: NoteInputButtonsInterface
) {
  val block = block()
  Box(
    Modifier
      .fillMaxWidth()
      .height(block(1.2))
      .background(MaterialTheme.colors.surface)
  ) {
    ConstraintLayout(Modifier.fillMaxSize()) {
      val (left, right) = createRefs()
      MainRow(
        false, false, {},
        Modifier
          .width(block(10.5))
          .constrainAs(left) {
            start.linkTo(parent.start)
            centerVerticallyTo(parent)
          },
        model.noteInputDescriptor,
        model.accidentals,
        iface
      )
      MoreRow(model.noteInputDescriptor, iface,
        Modifier
          .width(block(10))
          .constrainAs(right) {
            end.linkTo(parent.end)
            centerVerticallyTo(parent)
          })
    }
  }
}

@Composable
private fun MoreRow(
  descriptor: NoteInputDescriptor,
  iface: NoteInputButtonsInterface,
  modifier: Modifier = Modifier
) {
  Box(
    modifier.testTag("MorePanel")
  ) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      GraceButtons(
        GraceSelectorModel(
          descriptor.graceType,
          descriptor.graceInputMode == GraceInputMode.ADD
        ),
        iface
      )

      ToggleButton(R.drawable.octave, descriptor.isPlusOctave, iface::toggleOctaveShift)
      ToggleButton(R.drawable.dotted, descriptor.isDottedRhythm, iface::toggleDotted)
      ToggleButton(R.drawable.tie, descriptor.isTie, iface::toggleTieToLast)
      NoteHeadSelector(descriptor.noteHeadType, iface::setNoteHead)
      ToggleButton(R.drawable.small, descriptor.isSmall, iface::toggleSmall)
      ToggleButton(R.drawable.no_edit, descriptor.isNoEdit, iface::toggleNoEdit)
    }
  }
}

@Composable
private fun MainRow(
  includeMore: Boolean,
  showMore: Boolean,
  toggleMore: () -> Unit,
  modifier: Modifier,
  descriptor: NoteInputDescriptor,
  accidentals: List<Accidental>,
  iface: NoteInputButtonsInterface,
) {
  Box(modifier.fillMaxWidth()) {
    Row(
      Modifier
        .height(block())
        .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
      DurationSelector(
        descriptor.duration,
        iface::setDuration
      )
      DotToggle(descriptor.dots, iface::setNumDots)
      AccidentalSpinner(
        descriptor.accidental,
        accidentals, iface::setAccidental
      )
      LeftRight({ iface.moveMarker(true) }, { iface.moveMarker(false) },
        Modifier.padding(1.dp)
      )
      RestButton(
        descriptor.duration, iface::insertRest, iface::deleteRest,
        Modifier.padding(1.dp)
      )
      if (includeMore) {
        MoreButton(showMore, toggleMore)
      }
    }
  }
}


@Composable
private fun RestButton(
  duration: Duration, insert: () -> Unit, delete: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier
      .width(block(2))
      .border(styledBorder())
  ) {
    SquareButton(resource = duration.toResource()!!,
      modifier = Modifier.align(Alignment.Center),
      onClick = { insert() },
      onLongPress = { delete() }
    )
  }
}

@Composable
private fun MoreButton(showMore: Boolean, toggle: () -> Unit) {
  SquareButton(
    resource = if (showMore) R.drawable.expand_more else R.drawable.expand_less,
    border = true,
    tag = "MoreButton",
    onClick = { toggle() }
  )
}

@Composable
@Preview
private fun Preview() {

}