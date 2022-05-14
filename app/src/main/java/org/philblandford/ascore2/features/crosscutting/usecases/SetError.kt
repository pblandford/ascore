package org.philblandford.ascore2.features.crosscutting.usecases

import org.philblandford.ascore2.features.crosscutting.model.ErrorDescr


interface SetError {
  suspend operator fun invoke(errorDescr: ErrorDescr)
}