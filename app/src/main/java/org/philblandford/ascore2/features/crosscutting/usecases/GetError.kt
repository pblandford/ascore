package org.philblandford.ascore2.features.crosscutting.usecases

import kotlinx.coroutines.flow.SharedFlow
import org.philblandford.ascore2.features.crosscutting.model.ErrorDescr

interface GetError {
  operator fun invoke():SharedFlow<ErrorDescr>
}