package org.philblandford.ascore2.features.clipboard.usecases

import com.philblandford.kscore.api.KScore
import kotlinx.coroutines.flow.Flow

class SelectionUpdateImpl(private val kScore: KScore) : SelectionUpdate {
  override fun invoke(): Flow<Unit> {
    return kScore.selectionUpdate()
  }
}