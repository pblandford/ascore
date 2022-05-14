package org.philblandford.ascore2.features.drawing

import kotlinx.coroutines.flow.SharedFlow

interface ListenForRedraw {
  operator fun invoke():SharedFlow<List<Int>>
}