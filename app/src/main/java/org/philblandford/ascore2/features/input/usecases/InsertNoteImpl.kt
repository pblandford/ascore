package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.NoteInputDescriptor
import org.philblandford.ascore2.features.drawing.Redraw
import timber.log.Timber

class InsertNoteImpl(
  private val kScore: KScore,
  private val inputState: NoteInputState,
  private val redraw: Redraw,
  private val currentVoice: CurrentVoice
) : InsertNote {
  override operator fun invoke(midiVal: Int, hold: Boolean) {
    Timber.e("UNDO UC $midiVal")

    kScore.addNoteAtMarker(
      inputState().value.copy(midiVal = midiVal, isHold = hold),
      currentVoice().value
    )
    redraw(listOf())
  }
}