package org.philblandford.ascore2.features.edit

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.Accidental

class MoveSelectedNoteImpl(private val kScore: KScore) : MoveSelectedNote {
  override fun invoke(amount: Int) {
    val accidental = when (amount) {
      1 -> Accidental.SHARP
      -1 -> Accidental.FLAT
      else -> null
    }

    kScore.shiftSelected(amount, accidental)
  }
}