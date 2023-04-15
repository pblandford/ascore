package org.philblandford.ui.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.TestOnly
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.philblandford.ascore2.features.crosscutting.model.ErrorDescr
import org.philblandford.ascore2.features.crosscutting.usecases.SetError
import org.philblandford.ascore2.features.crosscutting.usecases.SetProgress
import org.philblandford.ascore2.features.score.ScoreUpdate
import org.philblandford.ascore2.util.andThen
import org.philblandford.ascore2.util.ok
import timber.log.Timber


abstract class VMModel

interface VMInterface {
  fun reset()
  fun getSideEffects():Flow<VMSideEffect>
}

abstract class VMSideEffect


/* Test ONLY! Must be a better way to do this..
    Enables a special mode so that the VM behaves as synchronous input/output for ease of testing
 */
private var synchronous: Boolean = false

@TestOnly
fun setSynchronous(yes: Boolean) {
  synchronous = yes
}

private data class Action<M>(val id:Int, val func:suspend (M) -> Result<M>)

abstract class BaseViewModel<M : VMModel, I : VMInterface, S : VMSideEffect> : ViewModel(),
  KoinComponent {

  private val setErrorUC: SetError by inject()
  private val setProgressUC: SetProgress by inject()
  protected val scoreUpdate:ScoreUpdate by inject()
  private val _viewState = MutableStateFlow<M?>(null)
  private val _actions = Channel<Action<M>>(Channel.UNLIMITED)
  private val _sideEffects = Channel<S>()

  init {
    listen()
  }

  fun reset() {
    if (!resetOnLoad && _viewState.value != null) {
      return
    }
    launchInitState()
  }

  open val resetOnLoad = true

  val effectFlow = _sideEffects.receiveAsFlow()

  fun getSideEffects():Flow<VMSideEffect> = effectFlow

  protected fun launchEffect(effect: S) {
    viewModelScope.launch {
      _sideEffects.send(effect)
    }
  }

  private fun launchInitState() {
    if (synchronous) {
      runBlocking {
        initState().onSuccess {
          _viewState.value = it
        }.onFailure {
          setError(it)
        }
      }
    } else {
      viewModelScope.launch {
        emitOrFail {
          initState()
        }
      }
    }
  }

  protected fun <T> doAndRefresh(func: suspend () -> Result<T>) {
    receiveAction {
      func().andThen { initState() }
    }
  }

  protected suspend fun <T> Result<T>.getOrError(): T? {
    return if (isSuccess) {
      this.getOrNull()
    } else {
      setError(
        ErrorDescr(
          "Could not get value", this.exceptionOrNull()?.message ?: "",
          this.exceptionOrNull()
        )
      )
      null
    }
  }

  protected fun setErrorLaunch(errorDescr: ErrorDescr) {
    if (synchronous) {
      runBlocking {
        setError(errorDescr)
      }
    } else {
      viewModelScope.launch {
        setError(errorDescr)
      }
    }
  }

  protected suspend fun setError(string: String) {
    setError(Exception(string))
  }

  protected suspend fun setError(exception: Throwable?) {
    setError(
      ErrorDescr(
        "Operation failed",
        exception?.message ?: "", exception
      )
    )
  }

  private suspend fun setError(errorDescr: ErrorDescr) {
    Timber.e(errorDescr.message, errorDescr.exception)
    Timber.e(errorDescr.exception)
    setErrorUC(errorDescr)
  }

  fun getState() = _viewState.asStateFlow()

  private var actionId = 0
  fun receiveAction(action: suspend (M) -> Result<M>) {
    if (synchronous) {
      receiveSynchronous(action)
    } else {
      viewModelScope.launch {
        val actionClass = Action(actionId++, action)
        _actions.send(actionClass)
      }
    }
  }

  fun update(action: M.() -> M) {
    receiveAction {
      it.action().ok()
    }
  }

  fun updateSynchronous(action: M.()->M) {
    val newValue = _viewState.value?.action()
    viewModelScope.launch {
      _viewState.emit(newValue)
    }
    _viewState.value = newValue
  }

  protected suspend fun setProgress(yes:Boolean) {
    setProgressUC(yes)
  }

  private suspend fun emitState(state: M) {
    _viewState.emit(state)
  }

  protected fun setState(state:M) {
    _viewState.value = state
  }

  private fun listen() {
    viewModelScope.launch {
      _actions.consumeAsFlow().collect { action ->
        Timber.e("OI got action ${_viewState.value}")
        _viewState.value?.let { value ->
          emitOrFail {
            Timber.e("OI emitting ${action.func(value)}")
            action.func(value) }
        }
      }
    }
  }

  private suspend fun emitOrFail(action: suspend () -> Result<M>) {
    try {
      action().onSuccess { newState ->
        emitState(newState)
      }.onFailure {
        setError(ErrorDescr(it.message ?: "", "", it))
      }
    } catch (e: Exception) {
      val descr = ErrorDescr(
        "Error performing operation",
        e.message ?: "Unknown error", e
      )
      setError(descr)
    }
  }


  @Synchronized
  private fun updateStateSync(state: M) {
    _viewState.value = state
  }

  private fun receiveSynchronous(action: suspend (M) -> Result<M>) {
    runBlocking {
      _viewState.value?.let { old ->
        action(old).onSuccess { newState ->
          updateStateSync(newState)
        }.onFailure {
          setError(it)
        }
      }
    }
  }

  protected abstract suspend fun initState(): Result<M>
  abstract fun getInterface(): I

}
