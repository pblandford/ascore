package com.philblandford.kscore.engine.pitch

import com.philblandford.kscore.engine.types.*

data class Harmony(val tone: Pitch, val quality: String, val root: Pitch? = null) {

  fun toEvent(): Event {
    var params = paramMapOf(
      EventParam.TONE to tone,
      EventParam.QUALITY to quality
    )
    root?.let { params = params.plus(EventParam.ROOT to it) }
    return Event(EventType.HARMONY, params)
  }

  override fun toString(): String {
    return if (root != null) {
      "${tone.letterString()}$quality/${root.letterString()}"
    } else {
      "${tone.letterString()}$quality"
    }
  }

  fun pitches(octave: Int = tone.octave, startOffset:Int = 0): List<Pitch> {
    val start = tone.copy(octave = octave)
    val minor = quality.isMinor()
    val accidental = if (tone.accidental == Accidental.NATURAL) {
      if ((getSharps(start, minor) ?: 0) >= 0) Accidental.SHARP else Accidental.FLAT
    } else {
      if (tone.accidental == Accidental.FLAT) {
        Accidental.FORCE_FLAT
      } else Accidental.FORCE_SHARP
    }
    return namesToQualities[quality]?.let { steps ->
      val withStart = listOf(0) + steps
      val ordered = withStart.drop(startOffset) +
          withStart.take(startOffset).map { it + 12 }
      ordered.map {
        val pitch = start + it
        specialCase(tone, it, minor) ?: pitch.enharmonic(accidental)
      }
    } ?: listOf()
  }

  private fun specialCase(tone: Pitch, num:Int, minor:Boolean):Pitch? {
    if (tone.octaveless() == Pitch(NoteLetter.G, Accidental.FLAT).octaveless() && minor &&
        num == 3) {
      return Pitch(NoteLetter.B, Accidental.DOUBLE_FLAT).octaveless()
    }
    else return null
  }

}

private fun String.isMinor() = contains('m')

private val namesToQualities = mapOf(
  "" to listOf(4, 7),
  "m" to listOf(3, 7),
  "-" to listOf(3, 7),
  "7" to listOf(4, 7, 10),
  "6" to listOf(4, 7, 9),
  "m6" to listOf(3, 7, 9),
  "-6" to listOf(3, 7, 9),
  "2" to listOf(2, 7),
  "M7" to listOf(4, 7, 11),
  "maj7" to listOf(4, 7, 11),
  "\u0394" to listOf(4, 7, 11),
  "m7" to listOf(3, 7, 10),
  "-7" to listOf(3, 7, 10),
  "m6" to listOf(3, 7, 9),
  "-6" to listOf(3, 7, 9),
  "m\u03947" to listOf(3, 7, 11),
  "-\u03947" to listOf(3, 7, 11),
  "\u2300" to listOf(3, 7, 10),
  "\u00b0" to listOf(3, 7),
  "\u00b07" to listOf(3, 7, 10),
  "sus" to listOf(5, 7),
  "sus7" to listOf(5, 7, 10),
  "+" to listOf(4, 8),
  "+7" to listOf(4, 8, 10),
  "9" to listOf(4, 7, 11, 15),
  "\u266d9" to listOf(4, 7, 11, 13),
  "+11" to listOf(4, 7, 10, 18),
  "13" to listOf(4, 7, 10, 15, 14),
  "\u266d13" to listOf(4, 7, 10, 15, 21),
  "Alt" to listOf(3, 7, 10, 14, 21)
)

val qualityNames = namesToQualities.keys.toList()

fun harmony(description: String, octave: Int = 0): Harmony? {
  return description.toHarmony()?.let{ it.copy(tone = it.tone.copy(octave = octave)) }
}

fun harmony(event: Event): Harmony? {
  event.getParam<String>(EventParam.TEXT)?.let { text ->
    return text.toHarmony()
  }

  return event.getPitchParam(EventParam.TONE)?.let { tone ->
    val quality = event.getParam<String>(EventParam.QUALITY) ?: ""
    val root = event.getPitchParam(EventParam.ROOT)
    Harmony(tone, quality, root)
  }
}

private fun Event.getPitchParam(param:EventParam):Pitch? {
  return when (val value = params[param]) {
    is Pitch -> value
    is String -> value.toPitch()
    else -> null
  }
}

private fun String.toHarmony(): Harmony? {
  if (isEmpty()) {
    return null
  }
  val div = if (length > 1 && isAccidental(this[1])) 2 else 1
  val tone = substring(0, div).toPitch()
  val rem = substring(div)

  return if (rem.contains('/')) {
    val fields = rem.split('/')
    val quality = fields[0]
    val root = fields[1].toPitch()
    Harmony(tone, quality, root)
  } else {
    Harmony(tone, rem)
  }
}

private fun String.toPitch(): Pitch {
  val accidental = getOrNull(1)?.toAccidental() ?: Accidental.NATURAL
  return Pitch(NoteLetter.valueOf(this[0].toString()), accidental, 0)
}


fun Char.toAccidental(): Accidental? {
  return when (toString()) {
    "\u266d", "b" -> Accidental.FLAT
    "\u266e" -> Accidental.NATURAL
    "\u266f", "#" -> Accidental.SHARP
    "\ud834\udd2a", "x" -> Accidental.DOUBLE_SHARP
    else -> null
  }
}


private fun isAccidental(char: Char) = char.toAccidental() != null

private val qualitiesForScale = listOf("", "m7", "m7", "", "7", "m", "\u2300")

fun getCommonChords(key: Int): List<Harmony> {
  return (0..6).map {
    val pitch = getScaleDegree(key, it).copy(octave = 0)
    val quality = qualitiesForScale[it]
    Harmony(pitch, quality)
  }
}

