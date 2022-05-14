package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.KScore

class InsertRestImpl(
  private val noteInputState: NoteInputState,
  private val currentVoice: CurrentVoice,
  private val kScore: KScore
) : InsertRest {
  override operator fun invoke() {
    kScore.addRestAtMarker(
      noteInputState().value.duration, currentVoice().value,
      noteInputState().value.graceInputMode
    )
  }
}