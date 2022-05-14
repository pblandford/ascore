package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.KScore

class RedoImpl(private val kScore: KScore) : Redo {
  override operator fun invoke() {
    kScore.redo()
  }
}