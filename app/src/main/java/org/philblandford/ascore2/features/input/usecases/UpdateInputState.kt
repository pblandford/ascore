package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.NoteInputDescriptor

interface UpdateInputState {
  operator fun invoke(func:NoteInputDescriptor.()->NoteInputDescriptor)
}