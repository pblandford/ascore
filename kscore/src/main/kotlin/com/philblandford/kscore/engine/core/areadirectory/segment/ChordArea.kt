package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.StemGeography
import com.philblandford.kscore.engine.core.VoiceGeography
import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.*
import com.philblandford.kscore.engine.core.representation.*
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.util.highestBit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


fun DrawableFactory.chordArea(event: Event, voice: Int = 1, numVoices: Int = 1): VoiceArea? {
  return chordBaseArea(event, voice)?.let { (area, geog) ->
    addDecorations(area, event, geog, numVoices)?.let { (area, geog) ->
      VoiceArea(area, geog)
    }
  }
}

internal fun DrawableFactory.chordBaseArea(event: Event, voice:Int = 1): Pair<Area, VoiceGeography>? {
  val baseArea =
    Area(event = event, tag = "Chord").copy(addressRequirement = AddressRequirement.EVENT)
  val notePositions = getNotePositions(event)
  val notes = event.params.g<Iterable<Event>>(EventParam.NOTES) ?: listOf()
  val small =
    event.getParam<Iterable<Event>>(EventParam.NOTES)
      ?.any { !it.isTrue(EventParam.IS_SMALL) } == false
  return addNotes(baseArea, notes)?.let {
    val stemGeography = createStemGeography(it, event, small, voice, notePositions)
    addDots(it, event, notePositions)?.let {
      addTail(it, event, small, stemGeography)?.let { (newArea, newStem) ->
        addStem(newArea, event, newStem)?.let {
          addLedgers(it, notePositions, small)?.let {
            addSlash(event, newStem, it)?.let { area ->
              extractGeographyChord(area, newStem, event)?.let { geog ->
                Pair(area, geog)
              }
            }
          }
        }
      }
    }
  }
}

private fun getNotePositions(event: Event): Iterable<Coord> {
  val notes = event.params.g<Iterable<Event>>(EventParam.NOTES)
  return (notes?.mapNotNull { it.params.g<Coord>(EventParam.POSITION) }
    ?: listOf()).sortedBy { it.y }
}

private fun DrawableFactory.addNotes(baseArea: Area, notes: Iterable<Event>): Area? {
  var areaCopy = baseArea
  notes.withIndex().forEach {
    getHead(it.value)?.let { (tArea, offset) ->
      val pos = it.value.getParam<Coord>(EventParam.POSITION) ?: Coord(0, 0)
      val noteArea = tArea.copy(event = it.value, addressRequirement = AddressRequirement.NONE)
      areaCopy = areaCopy.addArea(
        noteArea, Coord(pos.x * tArea.width, pos.y * BLOCK_HEIGHT - BLOCK_HEIGHT + offset),
        eventAddress = eZero().copy(id = it.index + 1)
      )
    }
  }
  return areaCopy
}

private fun DrawableFactory.getHead(event: Event): Pair<Area, Int>? {
  val type = event.getParam<NoteHeadType>(EventParam.NOTE_HEAD_TYPE) ?: NoteHeadType.NORMAL
  val key = when (type) {
    NoteHeadType.NORMAL -> {
      when (event.duration().undot()) {
        longa() -> "tadpole_head_longa"
        breve() -> "tadpole_head_breve"
        semibreve() -> "tadpole_head_semibreve"
        minim() -> "tadpole_head_empty"
        else -> "tadpole_head"
      }
    }
    NoteHeadType.CROSS -> {
      when {
        event.duration() >= minim() -> "tadpole_head_cross_empty"
        else -> "tadpole_head_cross"
      }
    }
    NoteHeadType.DIAMOND -> {
      when {
        event.duration() >= minim() -> "tadpole_head_diamond_empty"
        else -> "tadpole_head_diamond"
      }
    }
  }
  val height = getHeight(event.duration())
  val smallMult = if (event.isTrue(EventParam.IS_SMALL)) 0.75f else 1f
  val offset = getYOffset(event.duration(), event.isTrue(EventParam.IS_SMALL))
  return getDrawableArea(
    ImageArgs(
      key,
      INT_WILD,
      (height * smallMult).toInt()
    )
  )?.copy(tag = "Tadpole")
    ?.let { it to offset }
}

private fun getHeight(duration: Duration): Int {
  return when (duration.undot()) {
    longa() -> BLOCK_HEIGHT * 6
    breve() -> BLOCK_HEIGHT * 3
    else -> BLOCK_HEIGHT * 2
  }
}

private fun getYOffset(duration: Duration, small: Boolean): Int {
  val offset = when (duration.undot()) {
    longa() -> -(BLOCK_HEIGHT * 0.5).toInt()
    breve() -> -(BLOCK_HEIGHT * 0.5).toInt()
    else -> 0
  }
  return if (small) BLOCK_HEIGHT / 4 else offset
}

private fun createStemGeography(
  baseArea: Area, chord: Event, small: Boolean,
  voice:Int,
  notePositions: Iterable<Coord>
): StemGeography {
  val startNotes = notePositions.first().y * BLOCK_HEIGHT
  val endNotes = notePositions.last().y * BLOCK_HEIGHT
  val tAreaWidth = baseArea.findByTag("Tadpole").toList().firstOrNull()?.second?.width ?: 0

  val height = if (small) STEM_HEIGHT_SMALL else STEM_HEIGHT

  val up = chord.isUpstem()
  val x = if (up) tAreaWidth - baseArea.xMargin - LINE_THICKNESS / 2 else LINE_THICKNESS / 2
  val noteSpan = abs(endNotes - startNotes)
  val tip = if (up) {
    min(startNotes - height, BLOCK_HEIGHT * 4)
  } else {
    max(endNotes + height, BLOCK_HEIGHT * 4)
  }
  val base = if (up) endNotes else startNotes
  return StemGeography(tip, base, noteSpan, x, up)
}

private fun DrawableFactory.addStem(
  baseArea: Area,
  chord: Event,
  stemGeography: StemGeography
): Area? {
  if (chord.duration() < semibreve()) {
    val line = getDrawableArea(LineArgs(stemGeography.height, false))?.copy(tag = "Stem")
    val yPos = if (stemGeography.up) stemGeography.tip else stemGeography.tip - stemGeography.height
    val coord = Coord(stemGeography.xPos, yPos)
    return line?.let { baseArea.addArea(it, coord) }
  } else {
    return baseArea
  }
}

private fun DrawableFactory.addDots(
  baseArea: Area,
  chord: Event,
  notePositions: Iterable<Coord>
): Area? {
  val numDots = chord.duration().numDots()

  var area = baseArea

  (1..numDots).forEach { dot ->
    notePositions.forEach { np ->
      val offset = if (np.y % 2 == 0) DOT_WIDTH else DOT_WIDTH / 2
      val pos =
        Coord(
          baseArea.width - baseArea.xMargin + DOT_WIDTH + (dot - 1) * DOT_WIDTH * 2,
          np.y * BLOCK_HEIGHT - offset
        )
      getDrawableArea(DotArgs(DOT_WIDTH, DOT_WIDTH))?.let {
        area = area.addArea(it.copy(tag = "Dot"), pos)
      }
    }
  }

  return area
}

private fun DrawableFactory.addTail(
  baseArea: Area, chord: Event, small: Boolean,
  stemGeography: StemGeography
): Pair<Area, StemGeography>? {
  return if (chord.duration() >= crotchet() || chord.isTrue(EventParam.IS_BEAMED)) {
    baseArea to stemGeography
  } else {
    val numTails = chord.duration().undot().denominator.highestBit() - 3
    val extraHeight = if (numTails > 2) (numTails - 2) * TAIL_GAP else 0
    val up = chord.isUpstem()
    val newStemGeog = if (up) {
      stemGeography.copy(tip = stemGeography.tip - extraHeight)
    } else {
      stemGeography.copy(tip = stemGeography.tip + extraHeight)
    }

    getTailArea(up, small, numTails)?.let {
      val yPos = if (up) newStemGeog.tip else newStemGeog.tip - it.height
      baseArea.addArea(it, Coord(newStemGeog.xPos, yPos))
    }?.let { it to newStemGeog }
  }
}

private fun DrawableFactory.getTailArea(up: Boolean, small: Boolean, num: Int): Area? {
  val height = if (small) TAIL_HEIGHT_SMALL else TAIL_HEIGHT
  val singleTail = if (up) {
    getDrawableArea(ImageArgs("tail_up_stem", INT_WILD, height))
  } else {
    getDrawableArea(ImageArgs("tail_down_stem", INT_WILD, height))
  }
  var offset = 0
  return singleTail?.let { single ->
    (1..num).fold(Area(tag = "Tail")) { main, _ ->
      val a = main.addArea(single, Coord(0, offset))
      offset += TAIL_GAP
      a
    }
  }
}

private fun DrawableFactory.addLedgers(
  baseArea: Area,
  notePositions: Iterable<Coord>,
  small: Boolean
): Area? {

  val tadpoleWidth = baseArea.childMap.filter { it.second.event?.eventType == EventType.NOTE }
    .maxByOrNull { it.second.width }?.second?.width ?: TADPOLE_WIDTH

  val ledgerPositions = getLedgerPositions(notePositions.toList(), tadpoleWidth)

  return ledgerPositions.fold(baseArea) { a, descr ->
    getDrawableArea(LineArgs(descr.width, true))?.let { area ->
      val la = area.copy(tag = "Ledger")
      a.addArea(la, Coord(descr.position.x, descr.position.y))
    } ?: a
  }
}

private fun DrawableFactory.addSlash(
  chord: Event,
  stemGeography: StemGeography,
  area: Area
): Area {
  return if (chord.isTrue(EventParam.IS_SLASH)) {
    getDrawableArea(DiagonalArgs(SLASH_WIDTH, SLASH_HEIGHT, LINE_THICKNESS, true))?.let { slash ->
      val x = stemGeography.xPos - slash.width / 2
      val y =
        if (stemGeography.up) stemGeography.yPos + BLOCK_HEIGHT
        else stemGeography.yPos + stemGeography.height - (BLOCK_HEIGHT * 3).toInt()
      area.addArea(slash.copy(tag = "Slash"), Coord(x, y))
    } ?: area
  } else area
}

private fun extractGeographyChord(
  area: Area,
  stemGeography: StemGeography,
  event: Event
): VoiceGeography? {
  val width = area.width
  val xMargin = area.xMargin
  val duration = area.event?.duration() ?: crotchet()
  val notePositions = area.findByTag("Tadpole").map { it.key.coord }.sortedBy { it.y }
  return VoiceGeography(
    width, xMargin, duration, stemGeography, notePositions,
    beamed = event.isTrue(EventParam.IS_BEAMED)
  )
}

internal fun VoiceArea.replaceStem(
  stemGeography: StemGeography,
  numVoice: Int = 1,
  drawableFactory: DrawableFactory
): VoiceArea {
  if (this.voiceGeography.stemGeography == stemGeography) {
    return this
  }

  var newArea = this.base
  return newArea.event?.let { event ->
    newArea = newArea.copy(childMap = newArea.childMap.filterNot {
      it.second.tag == "Stem" || it.second.tag == "Decoration"
    })
    newArea = drawableFactory.addStem(newArea, event, stemGeography) ?: newArea
    val newGeog = voiceGeography.copy(stemGeography = stemGeography)

    drawableFactory.addDecorations(newArea, event, newGeog, numVoice)?.let { (na, ng) ->
      VoiceArea(na, ng)
    }
  } ?: this

}