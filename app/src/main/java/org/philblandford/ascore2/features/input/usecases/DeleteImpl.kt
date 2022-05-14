package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.features.clipboard.usecases.ClearSelection

class DeleteImpl(private val kScore: KScore, private val clearSelection: ClearSelection) : Delete {
  override operator fun invoke() {
    kScore.getStartSelect()?.let {
      kScore.deleteRange()
      clearSelection()
    } ?: run {
      kScore.deleteAtMarker(1)
    }
  }
}