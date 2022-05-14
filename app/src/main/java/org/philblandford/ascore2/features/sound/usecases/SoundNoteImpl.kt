package org.philblandford.ascore2.features.sound.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.SoundManager

class SoundNoteImpl(
  private val soundManager: SoundManager,
  private val kScore: KScore
) : SoundNote {


  override operator fun invoke(midiVal: Int) {
    val instrument = kScore.getInstrumentAtMarker()
    instrument?.let {
      soundManager.soundSingleNote(midiVal, instrument.program, 100, 350,
      instrument.percussion, instrument.soundFont, instrument.bank)
    }
  }
}