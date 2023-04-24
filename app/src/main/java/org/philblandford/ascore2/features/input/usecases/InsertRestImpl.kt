package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.duration.dot
import com.philblandford.kscore.engine.types.GraceInputMode
import com.philblandford.kscore.engine.types.GraceType

class InsertRestImpl(
  private val noteInputState: NoteInputState,
  private val currentVoice: CurrentVoice,
  private val kScore: KScore
) : InsertRest {
  override operator fun invoke() {
    with(noteInputState().value) {
      kScore.addRestAtMarker(duration.dot(dots), currentVoice().value, if (graceType != GraceType.NONE) graceInputMode else GraceInputMode.NONE)
    }
  }
}