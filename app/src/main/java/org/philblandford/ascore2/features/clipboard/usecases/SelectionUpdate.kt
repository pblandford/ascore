package org.philblandford.ascore2.features.clipboard.usecases

import kotlinx.coroutines.flow.Flow

interface SelectionUpdate {
  operator fun invoke(): Flow<Unit>
}