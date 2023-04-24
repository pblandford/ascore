package org.philblandford.ui.stubs

import com.philblandford.kscore.api.NoteInputDescriptor
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.types.Accidental
import com.philblandford.kscore.engine.types.GraceType
import com.philblandford.kscore.engine.types.NoteHeadType
import kotlinx.coroutines.flow.Flow
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.input.model.InputModel
import org.philblandford.ui.input.viewmodel.InputInterface

val stubInputModel = InputModel(NoteInputDescriptor(), accidentals = Accidental.values().toList())
class StubInputInterface : InputInterface {
  override fun reset() {
    TODO("Not yet implemented")
  }

  override fun getSideEffects(): Flow<VMSideEffect> {
    TODO("Not yet implemented")
  }

  override fun toggleOctaveShift() {
    TODO("Not yet implemented")
  }

  override fun toggleDotted() {
    TODO("Not yet implemented")
  }

  override fun toggleTieToLast() {
    TODO("Not yet implemented")
  }

  override fun setNoteHead(noteHeadType: NoteHeadType) {
    TODO("Not yet implemented")
  }

  override fun toggleSmall() {
    TODO("Not yet implemented")
  }

  override fun toggleNoEdit() {
    TODO("Not yet implemented")
  }

  override fun setDuration(duration: Duration) {
    TODO("Not yet implemented")
  }

  override fun setNumDots(dots: Int) {
    TODO("Not yet implemented")
  }

  override fun setAccidental(accidental: Accidental) {
    TODO("Not yet implemented")
  }

  override fun moveMarker(left: Boolean) {
    TODO("Not yet implemented")
  }

  override fun insertRest() {
    TODO("Not yet implemented")
  }

  override fun deleteRest() {
    TODO("Not yet implemented")
  }

  override fun setGrace(graceType: GraceType) {
    TODO("Not yet implemented")
  }

  override fun toggleGraceShift() {
    TODO("Not yet implemented")
  }

  override fun insertNote(midiVal: Int, hold: Boolean) {
    TODO("Not yet implemented")
  }
}