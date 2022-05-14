package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.NoteInputDescriptor

interface InsertNote {
  operator fun invoke(midiVal:Int, hold:Boolean)
}