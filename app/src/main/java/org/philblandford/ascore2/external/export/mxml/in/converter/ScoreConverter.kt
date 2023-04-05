package com.philblandford.ascore.external.export.mxml.`in`.converter

import com.philblandford.ascore.external.export.mxml.out.MxmlPageMargins
import com.philblandford.ascore.external.export.mxml.out.MxmlScorePartwise
import com.philblandford.ascore.external.export.mxml.out.MxmlSystemMargins
import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.api.ProgressFunc2
import com.philblandford.kscore.api.noProgress2
import com.philblandford.kscore.engine.core.LayoutDescriptor
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.score.Part
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.Stave
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.map.emptyEventMap
import com.philblandford.kscore.engine.newadder.NewEventAdder
import com.philblandford.kscore.engine.newadder.Right
import com.philblandford.kscore.engine.newadder.rightOrThrow
import com.philblandford.kscore.engine.newadder.subadders.transformStaves
import com.philblandford.kscore.engine.newadder.util.setAccidentals
import com.philblandford.kscore.engine.newadder.util.setTies
import com.philblandford.kscore.engine.pitch.KeySignature
import com.philblandford.kscore.engine.tempo.Tempo
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.time.timeSignature
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.EventType.*
import com.philblandford.kscore.engine.util.pMap
import com.philblandford.kscore.log.ksLogv
import com.philblandford.kscore.option.getAllDefaults
import kotlinx.coroutines.runBlocking

internal fun mxmlScoreToScore(
  mxmlScore: MxmlScorePartwise,
  instrumentGetter: InstrumentGetter,
  progress: ProgressFunc2 = noProgress2
): Score? {
  var scoreEvents = getMeta(
    mxmlScore,
    emptyEventMap()
  )

  val partList = mxmlPartListToParts(mxmlScore.partList, instrumentGetter)

  var num = 0

  val parts = runBlocking {
    mxmlScore.parts.withIndex().pMap { mxmlPart ->
      ksLogv("Converting part ${mxmlPart.value.id}")
      mxmlScore.partList.scoreParts.find { it.id == mxmlPart.value.id }?.let { mxmlScorePart ->
        partList[mxmlPart.value.id]?.let { partDesc ->
          progress("Creating ${partDesc.name}", (num.toFloat() / partList.size) * 100)
          val partEvents =
            emptyEventMap().putEvent(ez(1), partDesc.instruments.toList().first().second.toEvent())
              .putEvent(
                eZero(), Event(
                  PART, paramMapOf(
                    EventParam.LABEL to partDesc.name,
                    EventParam.ABBREVIATION to partDesc.abbrevation
                  )
                )
              )

          mxmlPartToPart(
            mxmlPart.value,
            mxmlScorePart,
            partEvents,
            partDesc.instruments.map { it.key to it.value.toEvent() }.toMap(),
            scoreEvents
          )?.let { (part, thisScoreEvents) ->
            num++
            var newEm = part.eventMap
            newEm = addStaveJoins(part, newEm)

            thisScoreEvents.getAllEvents().forEach { (k, v) ->
              scoreEvents = scoreEvents.putEvent(k.eventAddress, v)
            }
            part.copy(eventMap = newEm)
          }
        }
      }
    }
  }.toList().filterNotNull()

  progress("Tidying", 80f)

  var score = Score(parts.toList(), scoreEvents)
  score = score.transformStaves { _, _, staveId ->
    setTies(score)
  }.rightOrThrow()
  score = (score.setAccidentals() as Right<Score>).r
  score = findHiddenTimeSignatures(score)
  score = markVoltas(score)
  score =
    setOf(SLUR, WEDGE, EXPRESSION_DASH, OCTAVE, GLISSANDO, LONG_TRILL, PEDAL).fold(score) { s, et ->
      markStaveLines(s, et)
    }
  score = sanitise(score)
  score = getLayout(score, mxmlScore)
  score = getSystemLayout(score, mxmlScore)
  score = getStaffLayout(score, mxmlScore)
  return score
}

private fun addStaveJoins(part: Part, eventMap: EventMap): EventMap {
  return if (part.staves.size > 1) {
    eventMap.putEvent(
      ez(0), Event(
        STAVE_JOIN,
        paramMapOf(
          EventParam.TYPE to StaveJoinType.GRAND,
          EventParam.NUMBER to part.staves.size - 1
        )
      )
    )
  } else {
    eventMap
  }
}

private fun sanitise(score: Score): Score {
  var copy = score.eventMap
  if (copy.getEvent(KEY_SIGNATURE, ez(1)) == null) {
    copy = copy.putEvent(ez(1), KeySignature(0).toEvent())
  }
  if (copy.getEvent(TIME_SIGNATURE, ez(1)) == null) {
    copy = copy.putEvent(ez(1), TimeSignature(4, 4).toEvent())
  }
  if (copy.getEvent(TEMPO, ez(1)) == null) {
    copy = copy.putEvent(ez(1), Tempo(crotchet(), 120).toEvent())
  }
  copy = copy.putEvent(eZero(), Event(OPTION, getAllDefaults()))
  copy = copy.putEvent(eZero(), LayoutDescriptor().toEvent())
  copy = copy.putEvent(
    eZero(),
    Event(UISTATE, paramMapOf(EventParam.MARKER_POSITION to ea(1)))
  )
  return score.copy(eventMap = copy)
}

private fun getMeta(mxmlScore: MxmlScorePartwise, eventMap: EventMap): EventMap {
  var mapCopy = eventMap
  var existing = mutableListOf<String>()

  mxmlScore.work?.title?.let { title ->
    mapCopy = mapCopy.setMeta(MetaType.TITLE, title.text)
    existing.add(title.text)
  }


  mxmlScore.identification?.let { id ->
    id.creator.forEach { creator ->
      val type = when (creator.type) {
        "composer" -> MetaType.COMPOSER
        "lyricist" -> MetaType.LYRICIST
        else -> null
      }
      type?.let {
        mapCopy = mapCopy.setMeta(it, creator.text)
        existing.add(creator.text)
      }
    }
  }

  /* Just assume any text not accounted for is the subtitle, about all we can do */
  mxmlScore.credits.forEach { credit ->
    if (!existing.contains(credit.words.text)) {
      mapCopy = mapCopy.setMeta(MetaType.SUBTITLE, credit.words.text)
    }
  }
  return mapCopy
}

private fun EventMap.setMeta(type: MetaType, text: String): EventMap {
  return setParam(eZero(), type.toEventType(), EventParam.TEXT, text).
  setParam(eZero(), type.toEventType(), EventParam.TEXT_SIZE, type.textSize()).
  setParam(eZero(), type.toEventType(), EventParam.FONT, "")
    .setParam(eZero(), type.toEventType(), EventParam.HARD_START, Coord())

}

private fun findHiddenTimeSignatures(score: Score): Score {

  var eventMap = score.eventMap

  score.parts.withIndex().forEach { partIv ->
    partIv.value.staves.withIndex().forEach { staveIv ->
      staveIv.value.bars.withIndex().forEach { barIv ->
        score.getEventAt(TIME_SIGNATURE, ez(barIv.index + 1))?.let { (_, event) ->
          timeSignature(event)?.let { ts ->
            barIv.value.voiceMaps.maxByOrNull { it.timeSignature.duration }
              ?.timeSignature?.let { actualTs ->
                if (ts.duration != actualTs.duration) {
                  eventMap = eventMap.putEvent(
                    ez(barIv.index + 1),
                    actualTs.copy(hidden = true).toEvent()
                  )
                  eventMap =
                    eventMap.putEvent(ez(barIv.index + 2), event.addParam(EventParam.HIDDEN, true))
                }
              }
          }
        }
      }
    }
  }
  return score.copy(eventMap = eventMap)
}


private fun markVoltas(score: Score): Score {
  val eventMap = markLineEvents(
    score.eventMap, VOLTA,
    { one, two -> one.getParam<Int>(EventParam.NUMBER) == two.getParam<Int>(EventParam.NUMBER) },
    { one, two -> EventParam.NUM_BARS to two.barNum - one.barNum + 1 },
    EventParam.NUM_BARS to 1
  )
  return score.copy(eventMap = eventMap)
}

private fun markStaveLines(score: Score, eventType: EventType): Score {
  val newParts = score.parts.map { part ->
    val newStaves = part.staves.map { stave ->
      val newMap = markLineEvents(
        stave.eventMap, eventType,
        { _, _ -> true },
        { one, two -> val d = score.getDuration(one, two) ?: dZero(); EventParam.DURATION to d },
        EventParam.DURATION to dZero()
      )
      Stave(stave.bars, newMap)
    }
    part.copy(newStaves.toList())
  }
  return score.copy(parts = newParts.toList())
}


private fun markPartLines(score: Score, eventType: EventType): Score {
  val newParts = score.parts.map { part ->
    val newMap = markLineEvents(
      part.eventMap, eventType,
      { _, _ -> true },
      { one, two -> val d = score.getDuration(one, two) ?: dZero(); EventParam.DURATION to d },
      EventParam.DURATION to dZero()
    )
    part.copy(eventMap = newMap)
  }
  return score.copy(parts = newParts.toList())
}

private fun markLineEvents(
  eventMap: EventMap, eventType: EventType,
  isSame: (Event, Event) -> Boolean,
  durationParam: (EventAddress, EventAddress) -> Pair<EventParam, Any>,
  defaultDuration: Pair<EventParam, Any>
): EventMap {
  var current: Pair<EventAddress, Event>? = null
  var newMap = eventMap
  val events = eventMap.getEvents(eventType)?.toList()?.sortedBy { it.first.eventAddress.barNum }

  events?.forEach { (key, event) ->
    if (event.isTrue(EventParam.END)) {
      current?.let { (cKey, cEvent) ->
        if (isSame(cEvent, event)) {
          val param = durationParam(cKey, key.eventAddress)
          newMap = newMap.putEvent(cKey, cEvent.addParam(param))
          newMap = newMap.putEvent(
            key.eventAddress,
            cEvent.addParam(param).addParam(EventParam.END, true)
          )
        }
      } ?: run {
        newMap = newMap.putEvent(key.eventAddress, event.addParam(defaultDuration))
      }
      current = null
    } else {
      current = key.eventAddress to event
    }
  }
  return newMap
}

private fun getLayout(score: Score, mxmlScore: MxmlScorePartwise): Score {
  return mxmlScore.defaults?.pageLayout?.let { pageLayout ->
    val width = pageLayout.pageWidth.num.toPixels()
    val height = pageLayout.pageHeight.num.toPixels()
    val newScore = pageLayout.margins.firstOrNull()?.let { margins ->
      getMargins(score, margins)
    } ?: score

    newScore.setLayoutOption(EventParam.LAYOUT_PAGE_WIDTH, width)
      .setLayoutOption(EventParam.LAYOUT_PAGE_HEIGHT, height)
  } ?: score
}

private fun getMargins(score: Score, mxmlPageMargins: MxmlPageMargins): Score {
  val left = mxmlPageMargins.leftMargin.value.toPixels()
  val right = mxmlPageMargins.rightMargin.value.toPixels()
  val top = mxmlPageMargins.topMargin.value.toPixels()
  val bottom = mxmlPageMargins.bottomMargin.value.toPixels()
  return listOf(
    left to EventParam.LAYOUT_LEFT_MARGIN,
    right to EventParam.LAYOUT_RIGHT_MARGIN,
    top to EventParam.LAYOUT_TOP_MARGIN,
    bottom to EventParam.LAYOUT_BOTTOM_MARGIN
  ).fold(score) { s, (value, param) ->
    s.setLayoutOption(param, value)
  }
}

private fun getSystemLayout(score: Score, mxmlScore: MxmlScorePartwise): Score {
  return mxmlScore.defaults?.systemLayout?.let { systemLayout ->
    systemLayout.systemDistance?.let { distance ->
      score.setLayoutOption(EventParam.LAYOUT_SYSTEM_GAP, distance.num.toPixels())
    }
  } ?: score
}

private fun getSystemMargins(score: Score, mxmlPageMargin: MxmlSystemMargins): Score {
  val left = mxmlPageMargin.leftMargin.value.toPixels()
  val right = mxmlPageMargin.rightMargin.value.toPixels()
  return listOf(
    left to EventParam.LAYOUT_LEFT_MARGIN,
    right to EventParam.LAYOUT_RIGHT_MARGIN
  ).fold(score) { s, (value, param) ->
    s.setLayoutOption(param, value)
  }
}

private fun getStaffLayout(score: Score, mxmlScore: MxmlScorePartwise): Score {
  return mxmlScore.defaults?.staffLayout?.let { systemLayout ->
    systemLayout.staffDistance?.let { distance ->
      score.setLayoutOption(EventParam.LAYOUT_STAVE_GAP, distance.num.toPixels())
    }
  } ?: score
}


private fun Score.setLayoutOption(option: EventParam, value: Int): Score {
  return NewEventAdder.setParam(this, LAYOUT, option, value, eZero()).rightOrThrow()
}

private val ratio = (BLOCK_HEIGHT * 2).toFloat() / 10
private fun Float.toPixels(): Int {
  return (this * ratio).toInt()
}
