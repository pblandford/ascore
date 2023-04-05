package com.philblandford.ascore.external.export.mxml.`in`.converter

import com.philblandford.ascore.external.export.mxml.out.MxmlBass
import com.philblandford.ascore.external.export.mxml.out.MxmlHarmony
import com.philblandford.ascore.external.export.mxml.out.MxmlRoot
import com.philblandford.kscore.engine.duration.plus
import com.philblandford.kscore.engine.pitch.Harmony
import com.philblandford.kscore.engine.types.Accidental
import com.philblandford.kscore.engine.types.NoteLetter
import com.philblandford.kscore.engine.types.Pitch

internal fun convertHarmony(mxmlHarmony: MxmlHarmony, measureState: MeasureState): MeasureState {
  return getName(mxmlHarmony.kind.text)?.let { name ->
    val event = Harmony(
      getTone(mxmlHarmony.root),
      name,
      mxmlHarmony.bass?.let { getBass(it) }
    ).toEvent()
    val address = mxmlHarmony.offset?.let { getDuration(it.num, measureState.attributes.divisions) }?.let { offset ->
      measureState.next.copy(offset = measureState.next.offset + offset, voice = 0)
    } ?: measureState.next.voiceless()

    val em = measureState.barEvents.putEvent(address, event)
    measureState.copy(barEvents = em)
  } ?: measureState
}

private fun getTone(mxmlRoot: MxmlRoot): Pitch {
  val letter = NoteLetter.valueOf(mxmlRoot.rootStep.text)
  val accidental = alterToAccidental(mxmlRoot.rootAlter?.num ?: 0)
  return Pitch(letter, accidental, 0)
}

private fun getBass(mxmlBass: MxmlBass): Pitch {
  val letter = NoteLetter.valueOf(mxmlBass.bassStep.text)
  val accidental = alterToAccidental(mxmlBass.bassAlter?.num ?: 0)
  return Pitch(letter, accidental, 0)
}

private fun alterToAccidental(alter: Int): Accidental {
  return when (alter) {
    1 -> Accidental.SHARP
    2 -> Accidental.DOUBLE_SHARP
    -1 -> Accidental.FLAT
    -2 -> Accidental.DOUBLE_FLAT
    else -> Accidental.NATURAL
  }
}

private fun getName(kind: String): String? {
  return when (kind) {
    "major" -> ""
    "minor" -> "m"
    "major-seventh" -> "M7"
    "minor-seventh" -> "m7"
    "dominant" -> "7"
    else -> ""
  }
}