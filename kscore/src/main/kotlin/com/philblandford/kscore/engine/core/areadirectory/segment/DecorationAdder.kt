package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.VoiceGeography
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.cZero
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.representation.ARTICULATION_OFFSET
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.STAVE_HEIGHT
import com.philblandford.kscore.engine.core.representation.TADPOLE_WIDTH
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.EventParam.*
import kotlin.math.max
import kotlin.math.min

private enum class ChordPart {
  STEM, NOTE
}

private val decorationTypes = listOf(ARTICULATION, BOWING, FINGERING, ORNAMENT)

private data class DecorationDesc(
  val chordPart: ChordPart, val above: Boolean,
  val clearStave: Boolean, val shift: Coord = cZero()
)

internal fun DrawableFactory.addDecorations(
  base: Area, chord: Event, geog: VoiceGeography,
  numVoices: Int = 1
): Pair<Area, VoiceGeography>? {
  var decorationArea =
    decorationTypes.fold(
      Pair(
        Area(tag = "Decoration"),
        geog.copy(articulationHeight = 0)
      )
    ) { (b, g), ep ->
      getArea(ep, chord, numVoices)?.let { ret ->
        addArea(b, g, ret.first, ret.second)
      } ?: Pair(b, g)
    }
  decorationArea =
    addTremoloArea(decorationArea.second, chord, decorationArea.first) to decorationArea.second
  return Pair(
    if (decorationArea.first.width > 0) base.addArea(decorationArea.first) else base,
    geog.copy(articulationHeight = decorationArea.second.articulationHeight)
  )
}

private fun addArea(
  base: Area, geog: VoiceGeography, area: Area,
  desc: DecorationDesc
): Pair<Area, VoiceGeography> {

  val x = getX(desc, geog, area)
  val y = getY(desc, geog, area)

  return base.addArea(
    area,
    Coord(x, y)
  ) to geog.copy(
    articulationHeight = geog.articulationHeight + area.height + ARTICULATION_OFFSET,
    articulationAbove = desc.above
  )
}

private fun getX(desc: DecorationDesc, geog: VoiceGeography, area: Area): Int {
  return if (desc.chordPart == ChordPart.NOTE) {
    val note = if (desc.above) {
      geog.notePositions.first()
    } else {
      geog.notePositions.last()
    }
    note.x + desc.shift.x
  } else {
    (geog.stemGeography?.xPos ?: 0) - area.width / 2 - 12 + desc.shift.x
  }
}


private fun getY(desc: DecorationDesc, geog: VoiceGeography, area: Area): Int {
  val y = if (desc.above) {
    if (desc.chordPart == ChordPart.NOTE) {
      val startPoint = if (geog.stemGeography?.up == true) {
        min(STAVE_HEIGHT, geog.topNote)
      } else {
        geog.topNote
      }
      startPoint - area.height - ARTICULATION_OFFSET - geog.articulationHeight
    } else {
      val top = if (desc.clearStave) min(0, geog.stemGeography?.yPos ?: 0) else
        geog.stemGeography?.yPos ?: 0
      val articulationOffset =
        if (geog.stemGeography?.up == true) 2 else ARTICULATION_OFFSET - geog.articulationHeight
      top - area.height - articulationOffset
    }
  } else {
    if (desc.chordPart == ChordPart.NOTE) {
      val startPoint = if (geog.stemGeography?.up == false) {
        max(0, geog.bottomNote + (BLOCK_HEIGHT * 1.5).toInt())
      } else {
        geog.bottom + BLOCK_HEIGHT
      }
      startPoint + ARTICULATION_OFFSET + geog.articulationHeight
    } else {
      val bottom = if (desc.clearStave) max(STAVE_HEIGHT, geog.stemGeography?.tip ?: 0) else
        geog.stemGeography?.tip ?: 0
      bottom + ARTICULATION_OFFSET + geog.articulationHeight
    }
  }
  return y + desc.shift.y
}

private fun DrawableFactory.getArea(
  eventParam: EventParam,
  chord: Event,
  numVoices: Int
): Pair<Area, DecorationDesc>? {
  return when (eventParam) {
    ARTICULATION -> {
      chord.getParam<ChordDecoration<ArticulationType>>(eventParam)?.let { dec ->
        getDesc(eventParam, chord, numVoices, dec)?.let { desc ->
          articulationArea(dec, chord, desc.above)?.let { it to desc }
        }
      }
    }

    ORNAMENT -> {
      chord.getParam<ChordDecoration<Ornament>>(eventParam)?.let { dec ->
        getDesc(eventParam, chord, numVoices, dec)?.let { desc ->
          ornamentArea(dec, chord)?.let { it to desc }
        }
      }
    }

    FINGERING -> {
      chord.getParam<ChordDecoration<Int>>(eventParam)?.let { dec ->
        getDesc(eventParam, chord, numVoices, dec)?.let { desc ->
          fingeringArea(dec)?.let { it to desc }
        }
      }
    }

    BOWING -> {
      chord.getParam<ChordDecoration<BowingType>>(eventParam)?.let { dec ->
        getDesc(eventParam, chord, numVoices, dec)?.let { desc ->
          bowingArea(dec, !desc.above)?.let { it to desc }
        }
      }
    }

    else -> null
  }
}


private fun <T> getDesc(
  eventParam: EventParam, chord: Event, numVoices: Int,
  chordDecoration: ChordDecoration<T>
): DecorationDesc? {
  return when (eventParam) {
    ARTICULATION -> articulationDesc(chord, numVoices)
    ORNAMENT -> ornamentDesc(chord, numVoices, chordDecoration)
    FINGERING -> fingeringDesc(chord, numVoices)
    BOWING -> articulationDesc(chord, numVoices)
    else -> null
  }
}

private fun articulationDesc(chord: Event, numVoices: Int): DecorationDesc {
  val above = if (numVoices > 1) {
    chord.isUpstem()
  } else {
    !chord.isUpstem()
  }
  val chordPart = if (numVoices > 1) ChordPart.STEM else ChordPart.NOTE
  return DecorationDesc(chordPart, above, false)
}

private fun <T> ornamentDesc(
  chord: Event,
  numVoices: Int,
  chordDecoration: ChordDecoration<T>
): DecorationDesc {
  val above = !(numVoices == 2 && !chord.isUpstem())
  val chordPart = if (chord.isUpstem()) ChordPart.STEM else {
    if (above) ChordPart.NOTE else ChordPart.STEM
  }
  val shift = if (chord.isUpstem()) Coord() else chordDecoration.shift
  return DecorationDesc(chordPart, above, true, shift = shift)
}

private fun fingeringDesc(chord: Event, numVoices: Int): DecorationDesc {
  val above = chord.getParam<ChordDecoration<Int>>(FINGERING)?.up
    ?: (numVoices == 2 && !chord.isUpstem())
  val chordPart = if (chord.isUpstem()) {
    if (above) ChordPart.STEM else ChordPart.NOTE
  } else {
    if (above) ChordPart.NOTE else ChordPart.STEM
  }
  return DecorationDesc(chordPart, above, true)
}

private fun DrawableFactory.addTremoloArea(
  voiceGeography: VoiceGeography,
  chord: Event,
  chordArea: Area
): Area {
  return tremoloArea(chord)?.let { tremoloArea ->
    val x = if (chord.duration() >= semibreve()) {
      0
    } else {
      (voiceGeography.stemGeography?.xPos ?: TADPOLE_WIDTH / 2) - tremoloArea.width / 2
    }
    val extraY = getExtraY(chord)
    val y = if (voiceGeography.stemGeography?.up == true) {
      (voiceGeography.stemGeography.tip) + voiceGeography.stemGeography.beamHeight + BLOCK_HEIGHT - extraY
    } else {
      (voiceGeography.stemGeography?.tip
        ?: STAVE_HEIGHT) - BLOCK_HEIGHT - (voiceGeography.stemGeography?.beamHeight
        ?: 0) - tremoloArea.height + extraY
    }
    chordArea.addArea(tremoloArea, Coord(x, y))
  } ?: chordArea
}

private fun getExtraY(event: Event): Int {
  return event.getParam<ChordDecoration<Duration>>(TREMOLO_BEATS)?.let { cd ->
    if (cd.items.first() <= demisemiquaver()) BLOCK_HEIGHT else 0
  } ?: 0
}
