package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.NoteInputDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UpdateInputStateImpl : UpdateInputState, NoteInputState {
  private val descriptor = MutableStateFlow(NoteInputDescriptor())
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  override operator fun invoke(func:NoteInputDescriptor.()->NoteInputDescriptor) {
    coroutineScope.launch {
      descriptor.emit(descriptor.value.func())
    }
  }

  override fun invoke(): StateFlow<NoteInputDescriptor> {
    return descriptor
  }
}