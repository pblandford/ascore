package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.KScore

class UndoImpl(private val kScore: KScore) : Undo {
  override operator fun invoke() {
    kScore.undo()
  }
}