package org.philblandford.ascore2.features.crosscutting.usecases

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.philblandford.ascore2.features.crosscutting.model.ErrorDescr

class GetErrorImpl : GetError, SetError {
  private val errorFlow = MutableSharedFlow<ErrorDescr>()

  override operator fun invoke():SharedFlow<ErrorDescr> {
    return errorFlow
  }

  override suspend operator fun invoke(errorDescr: ErrorDescr) {
    errorFlow.emit(errorDescr)
  }
}