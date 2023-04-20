package org.philblandford.ascore2.features.edit

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.Accidental

class MoveSelectedNoteImpl(private val kScore: KScore) : MoveSelectedNote {
  override fun invoke(amount: Int) {
    kScore.shiftSelected(amount, null)
  }
}