package org.philblandford.ui.input.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.api.NoteInputDescriptor
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.types.Accidental
import com.philblandford.kscore.engine.types.GraceInputMode
import com.philblandford.kscore.engine.types.GraceType
import com.philblandford.kscore.engine.types.NoteHeadType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.input.usecases.*
import org.philblandford.ascore2.features.insert.GetKeySignatureAtMarker
import org.philblandford.ascore2.features.score.ScoreUpdate
import org.philblandford.ascore2.features.sound.usecases.SoundNote
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.input.compose.keyboard.NoteInputButtonsInterface
import org.philblandford.ui.input.model.InputModel
import timber.log.Timber


interface InputInterface : VMInterface, NoteInputButtonsInterface {
  fun insertNote(midiVal: Int, hold: Boolean)
}

sealed class InputEffect : VMSideEffect() {
  object Redraw : InputEffect()
}

class InputViewModel(
  private val insertNoteUC: InsertNote,
  private val insertRestUC : InsertRest,
  private val noteInputState: NoteInputState,
  private val updateInputState: UpdateInputState,
  private val moveMarkerUC: MoveMarker,
  private val soundNote: SoundNote,
  private val getInstrumentAtMarker:GetInstrumentAtMarker,
  private val getKeySignature:GetKeySignatureAtMarker,
) :
  BaseViewModel<InputModel, InputInterface, InputEffect>(), InputInterface {

  init {
    viewModelScope.launch {
      noteInputState().collectLatest { nd ->
        update { copy(noteInputDescriptor = nd) }
      }
    }
    viewModelScope.launch {
      scoreUpdate().map { getKeySignature() }.distinctUntilChanged().collectLatest { key ->
        val accidental = if (key >= 0) Accidental.SHARP else Accidental.FLAT
        updateInputState{ copy(accidental = accidental)}
      }
    }
  }

  override suspend fun initState(): Result<InputModel> {
    val descrs = getInstrumentAtMarker()?.percussionDescrs ?: listOf()
    return InputModel(
      noteInputState().value,
      Accidental.values().toList() - Accidental.NATURAL,
      descrs,
    ).ok()
  }

  override fun getInterface() = this

  override fun insertNote(midiVal: Int, hold: Boolean) {
    receiveAction {
      insertNoteUC(midiVal, hold)
      soundNote(midiVal)
      if (it.noteInputDescriptor.isPlusOctave) {
        soundNote(midiVal - 12)
      }
      launchEffect(InputEffect.Redraw)
      it.ok()
    }
  }

  private fun updateState(func: NoteInputDescriptor.() -> NoteInputDescriptor) {
      updateInputState(func)
  }

  override fun toggleOctaveShift() {
    updateState { copy(isPlusOctave = !isPlusOctave) }
  }

  override fun toggleDotted() {
    updateState { copy(isDottedRhythm = !isDottedRhythm) }
  }

  override fun toggleTieToLast() {
    updateState { copy(isTie = !isTie) }
  }

  override fun setNoteHead(noteHeadType: NoteHeadType) {
    updateState { copy(noteHeadType = noteHeadType) }
  }

  override fun toggleSmall() {
    updateState { copy(isSmall = !isSmall) }
  }

  override fun toggleNoEdit() {
    updateState { copy(isNoEdit = !isNoEdit) }
  }

  override fun setDuration(duration: Duration) {
    updateState { copy(duration = duration, dots = 0) }
  }

  override fun setNumDots(dots: Int) {
    updateState { copy(dots = dots) }
  }

  override fun setAccidental(accidental: Accidental) {
    updateState { copy(accidental = accidental) }
  }

  override fun moveMarker(left: Boolean) {
    moveMarkerUC(left)
  }

  override fun insertRest() {
    insertRestUC()
  }

  override fun deleteRest() {

  }

  override fun setGrace(graceType: GraceType) {
    updateState { copy(graceType = graceType) }
  }

  override fun toggleGraceShift() {
    updateState {
      copy(
        graceInputMode = if (graceInputMode == GraceInputMode.ADD) GraceInputMode.SHIFT else
          GraceInputMode.ADD
      )
    }
  }
}