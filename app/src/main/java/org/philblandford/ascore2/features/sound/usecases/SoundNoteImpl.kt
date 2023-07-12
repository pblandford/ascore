package org.philblandford.ascore2.features.sound.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.SoundManager
import com.philblandford.kscore.engine.types.EventParam

class SoundNoteImpl(
  private val soundManager: SoundManager,
  private val kScore: KScore
) : SoundNote {


  override operator fun invoke(midiVal: Int) {
    val instrument = kScore.getInstrumentAtMarker()
    instrument?.let {
      val transposition = if (kScore.getOption<Boolean>(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT) == true) 0 else instrument.transposition
      soundManager.soundSingleNote(midiVal + transposition, instrument.program, 100, 350,
      instrument.percussion, instrument.soundFont, instrument.bank)
    }
  }
}