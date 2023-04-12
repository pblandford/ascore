package com.philblandford.kscore.engine.newadder.util

import com.philblandford.kscore.engine.accidental.mapBar
import com.philblandford.kscore.engine.core.score.Bar
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.duration.Note
import com.philblandford.kscore.engine.newadder.BarResult
import com.philblandford.kscore.engine.newadder.Right
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam

fun Bar.getNotes():Map<EventAddress, Note> {
  return voiceMaps.withIndex().flatMap { iv ->
    iv.value.getNotes().map { it.key.copy(voice = iv.index+1) to it.value }
  }.toMap()
}

fun Bar.setStems(): BarResult {
  val newVms =
    voiceMaps.withIndex().map { iv -> iv.value.setStems(voiceNumberMap, iv.index + 1) }
  return Right(replaceSelf(eventMap, newVms) as Bar)
}


fun Bar.setAccidentals(score: Score, eventAddress: EventAddress): BarResult {
  return if (score.getInstrument(eventAddress)?.percussion == false) {
    val concert = score.getOption<Boolean>(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT) ?: false
    val previousBar = if (eventAddress.barNum > 1) score.getBar(eventAddress.dec()) else null
    val ks = score.getKeySignature(eventAddress, concert) ?: 0
    val newKs = score.getKeySignature(eventAddress.dec(), concert) != ks
    Right(mapBar(previousBar, this, ks, newKs))
  } else {
    Right(this)
  }
}