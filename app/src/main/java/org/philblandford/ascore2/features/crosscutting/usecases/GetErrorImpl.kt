package org.philblandford.ascore2.features.crosscutting.usecases

import com.philblandford.kscore.api.KScore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.crosscutting.model.ErrorDescr

class GetErrorImpl(private val kScore: KScore) : GetError, SetError {
  private val errorFlow = MutableSharedFlow<ErrorDescr>()
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  init {
    coroutineScope.launch {
      kScore.getErrorFlow().collectLatest { error ->
        errorFlow.emit(
          ErrorDescr(
            error.exception.message ?: "Unknown",
            "Command: ${error.command}", error.exception
          )
        )
      }
    }
  }

  override operator fun invoke(): SharedFlow<ErrorDescr> {
    return errorFlow
  }

  override suspend operator fun invoke(errorDescr: ErrorDescr) {
    errorFlow.emit(errorDescr)
  }
}