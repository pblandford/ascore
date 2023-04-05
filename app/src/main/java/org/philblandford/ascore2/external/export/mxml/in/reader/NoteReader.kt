package com.philblandford.ascore.external.export.mxml.`in`.reader

import com.philblandford.ascore.external.export.mxml.out.*
import org.w3c.dom.Element

private var cache = mutableMapOf<Element, MxmlNote>()

internal fun Element.parseNote(): MxmlNote? {

  return cache[this] ?: run {

    val duration = getTextElem("duration")?.let { MxmlDuration(it.toInt()) }
    val type = getTextElem("type")?.let { MxmlType(it) }
    val dots = getChildren("dot").map { MxmlDot() }
    val voice = getChild("voice")?.let { MxmlVoice(it.textContent.toInt()) } ?: MxmlVoice(1)
    val timeModification = getChild("time-modification")?.parseTimeModification()
    val notehead = getChild("notehead")?.parseNotehead()
    val tie = getChild("tie")?.parseTie()
    val instrument = getChild("instrument")?.parseInstrument()
    val staff = getChild("staff")?.let { MxmlStaff(it.textContent.toInt()) }
    val notations = getChild("notations")?.parseNotations()
    val lyrics = getChildren("lyric").mapNotNull { it.parseLyric() }
    val grace = getChild("grace")?.let { MxmlGrace() }

    val note = getPitch()?.let { pitch ->
      val chord = getChild("chord")?.let { MxmlChord() }

      MxmlNote(
        grace,
        chord,
        pitch,
        duration,
        tie,
        instrument,
        voice,
        type,
        dots,
        timeModification,
        notehead,
        staff,
        listOf(),
        notations,
        lyrics
      )
    } ?: run {

      getChild("rest")?.let {
        MxmlNote(
          grace,
          null,
          MxmlRest(),
          duration,
          null,
          null,
          voice,
          type,
          dots,
          timeModification,
          null,
          staff,
          listOf(),
          notations
        )
      }
    }
    note?.let {
      cache.put(this, note)
      note
    }
  }

}


private fun Element.getPitch(): MxmlNoteDescriptor? {
  return getChild("pitch")?.parsePitch() ?: getChild("unpitched")?.parseUnpitched()
}

private fun Element.parsePitch(): MxmlPitch? {
  return getTextElem("step")?.let { step ->
    getTextElem("octave")?.let { octave ->
      val alter = getTextElem("alter")?.let { MxmlAlter(it.toInt()) }
      MxmlPitch(MxmlStep(step), alter, MxmlOctave(octave.toInt()))
    }
  }
}

private fun Element.parseUnpitched(): MxmlUnpitched? {
  return getChild("display-step")?.textContent?.let { displayStep ->
    getChild("display-octave")?.textContent?.let { displayOctave ->
      MxmlUnpitched(MxmlDisplayStep(displayStep), MxmlDisplayOctave(displayOctave.toInt()))
    }
  }
}

private fun Element.parseTie(): MxmlTie? {
  return getAttribute("type")?.let { type ->
    MxmlTie(type)
  }
}

private fun Element.parseInstrument(): MxmlInstrument? {
  return getAttribute("id")?.let { id ->
    MxmlInstrument(id)
  }
}

private fun Element.parseLyric(): MxmlLyric? {
  val syllabic = getChild("syllabic")?.let { MxmlSyllabic(it.textContent) }
  return getChild("text")?.let { text ->
    val number = getAttribute("number")?.toIntOrNull() ?: 1
    MxmlLyric(number, syllabic, MxmlText(text.textContent))
  }
}

private fun Element.parseTimeModification(): MxmlTimeModification? {
  return getChild("actual-notes")?.let { actual ->
    getChild("normal-notes")?.let { normal ->
      MxmlTimeModification(
        MxmlActualNotes(actual.textContent.toInt()),
        MxmlNormalNotes(normal.textContent.toInt())
      )
    }
  }
}

private fun Element.parseNotehead(): MxmlNotehead? {
  return MxmlNotehead(textContent)
}