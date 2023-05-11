package org.philblandford.ascore2.features.crosscutting.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.saveload.Saver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.crosscutting.model.ErrorDescr
import timber.log.Timber

class GetErrorImpl(private val kScore: KScore) : GetError, SetError {
  private val errorFlow = MutableSharedFlow<ErrorDescr>()
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  init {
    coroutineScope.launch {
      kScore.getErrorFlow().collectLatest { error ->
        Timber.e("Received error from kScore $error")
        errorFlow.emit(
          ErrorDescr(
            error.exception.message ?: "Unknown",
            "Command: ${error.command}", error.exception,
            error.command, internal = error.internal
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