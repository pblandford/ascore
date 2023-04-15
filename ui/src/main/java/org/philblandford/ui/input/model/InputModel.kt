package org.philblandford.ui.input.model

import com.philblandford.kscore.api.NoteInputDescriptor
import com.philblandford.kscore.api.PercussionDescr
import com.philblandford.kscore.engine.types.Accidental
import org.philblandford.ui.base.viewmodel.VMModel

data class InputModel(
  val noteInputDescriptor: NoteInputDescriptor,
  val accidentals: List<Accidental>,
  val percussionDescrs:List<PercussionDescr> = listOf()
) : VMModel()