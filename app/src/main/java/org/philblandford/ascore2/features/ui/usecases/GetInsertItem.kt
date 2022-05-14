package org.philblandford.ascore2.features.ui.usecases

import com.philblandford.kscore.engine.types.EventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.philblandford.ascore2.features.ui.model.InsertItem

interface GetInsertItem {
  operator fun invoke(): Flow<InsertItem?>
}