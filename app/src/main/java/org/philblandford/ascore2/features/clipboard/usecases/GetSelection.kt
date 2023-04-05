package org.philblandford.ascore2.features.clipboard.usecases

import kotlinx.coroutines.flow.StateFlow
import org.philblandford.ascore2.features.clipboard.entities.Selection

interface GetSelection {
  operator fun invoke(): StateFlow<Selection?>
}