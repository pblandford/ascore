package org.philblandford.ascore2.features.instruments

import kotlinx.coroutines.flow.StateFlow

interface GetSelectedPart {
  operator fun invoke():StateFlow<Int>
}