package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.NoteInputDescriptor
import kotlinx.coroutines.flow.StateFlow

interface NoteInputState {
  operator fun invoke():StateFlow<NoteInputDescriptor>
}