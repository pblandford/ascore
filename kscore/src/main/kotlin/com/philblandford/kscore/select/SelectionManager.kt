package com.philblandford.kscore.select

import com.philblandford.kscore.api.Rectangle
import com.philblandford.kscore.api.ScoreArea
import com.philblandford.kscore.engine.core.representation.Representation
import com.philblandford.kscore.engine.core.representation.getArea
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.log.ksLogd
import com.philblandford.kscore.log.ksLoge
import com.philblandford.kscore.log.ksLogt
import com.philblandford.kscore.log.ksLogv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


/**
 * Created by phil on 03/12/18.
 */

data class SelectState(
  val start: EventAddress? = null, val end: EventAddress? = null,
  val previousStart: EventAddress? = null,
  val previousEnd: EventAddress? = null,
  val area: AreaToShow? = null
)


data class AreaToShow(
  val scoreArea: ScoreArea,
  val eventAddress: EventAddress,
  val event: Event,
  val extra: Any? = null
) {
  val x = scoreArea.rectangle.x
  val y = scoreArea.rectangle.y
  val width = scoreArea.rectangle.width
  val height = scoreArea.rectangle.height
}

class SelectionManager {

  private var selectState = MutableStateFlow(SelectState())
  private var atsIdx = -1
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  fun setStartSelect(start: EventAddress) {
    ksLogd("set start selection at $start")
    setState(SelectState(start, start))
    atsIdx = -1
  }

  fun setEndSelect(end: EventAddress) {
    getStartSelection()?.let { start ->
      val both = listOf(start, end).sorted()
      updateState { copy(start = both.first(), end = both.last()) }
    }
  }

  fun moveSelection(getSegment: (EventAddress) -> EventAddress?) {
    getStartSelection()?.let { start ->
      val end = getEndSelection() ?: start
      if (end == start) {
        getSegment(start)?.let { previous ->
          setStartSelect(previous)
        }
      } else {
        getSegment(end)?.let { previous ->
          setEndSelect(previous)
        }
      }
    }
  }


  fun getStartSelection(): EventAddress? {
    val ats = getSelectedArea()
    return ats?.eventAddress?.voiceIdless() ?: selectState.value.start
  }

  fun getEndSelection(): EventAddress? {
    val ats = getSelectedArea()
    return ats?.eventAddress ?: selectState.value.end
  }

  fun getSelectState(): StateFlow<SelectState> {
    return selectState
  }


  fun clearSelection() {
    setState(SelectState(
      previousStart = selectState.value.start,
      previousEnd = selectState.value.end
    ))
    atsIdx = 0
  }


  fun cycleArea(getAreasAtAddress: (EventAddress) -> List<AreaToShow>) {
    ksLogv("CycleArea")

    getStartSelection()?.let { start ->
      val areas = getAreasAtAddress(start)

      val chords = areas.filter {
        it.event.eventType == EventType.DURATION || it.event.eventType == EventType.NOTE
      }.sortAreas()
      selectState.value.area?.let { ats ->
        var idx = chords.indexOf(ats) + 1
        if (idx >= chords.size) idx = 0
        setSelectedArea(chords[idx], chords)
        atsIdx = idx
      } ?: run {
        atsIdx = 0
        setSelectedArea(chords.first(), chords)
      }
    }
  }

  private fun List<AreaToShow>.sortAreas(): List<AreaToShow> {
    val byVoice = groupBy { it.eventAddress.voice }
    return byVoice.flatMap { group ->
      group.value.sortedWith { a, b ->
        if (a.event.eventType == EventType.DURATION) 1
        else if (b.event.eventType == EventType.DURATION) -1
        else a.eventAddress.id - b.eventAddress.id
      }
    }
  }

  fun refreshAreas(areasToShow: (EventAddress) -> List<AreaToShow>) {
    ksLogt("$atsIdx $areasToShow")
    getStartSelection()?.let { start ->
      with(selectState.value) {
        val newAreas =
          areasToShow(start).toList().filterAreas(area?.event?.eventType).sortAreas()
        area?.let {
          updateState {
            copy(area = newAreas.getOrNull(atsIdx) ?: newAreas.find { a ->
              area?.eventAddress?.let { it == a.eventAddress } ?: false
            })
          }
        }
        ksLogv("refreshAreas ${area}")
      }
    }
  }

  private fun EventType.chordPart() = this == EventType.DURATION || this == EventType.NOTE

  fun List<AreaToShow>.filterAreas(currentType: EventType?): List<AreaToShow> {
    return filter {
      if (currentType?.chordPart() == true) {
        it.event.eventType.chordPart()
      } else {
        currentType == null || it.event.eventType == currentType
      }
    }
  }

  fun getSelectedArea(): AreaToShow? {
    return selectState.value.area
  }

  fun setSelectedArea(ats: AreaToShow, areas: List<AreaToShow>) {
    setState(SelectState(area = ats))
    atsIdx = areas.indexOfFirst { it.event.eventType == ats.event.eventType }
    ksLogt("COORD setSelectedArea ${ats.scoreArea.rectangle}")
  }

  fun updateFromScore(score: Score, representation: Representation) {
    getSelectedArea()?.let { ats ->
      ksLoge("COORD kscore SM Update old ${ats.scoreArea.rectangle}")

      score.tryGetEvent(ats.event.eventType, ats.eventAddress)?.let { (latestEvent, latestAddress) ->
        representation.getArea(latestEvent.eventType, latestAddress, ats.extra)?.let { area ->
          ksLoge("COORD kscore update new ${area.x} ${area.y} ${latestEvent.params}")
          val newAts = ats.copy(event = latestEvent, eventAddress = latestAddress,
            scoreArea = ats.scoreArea.copy(rectangle = Rectangle(area.x, area.y, area.width, area.height)))
          setSelectedArea(newAts, listOf(newAts))
        }
      }
    }
  }

  private fun Score.tryGetEvent(eventType:EventType, eventAddress: EventAddress):Pair<Event, EventAddress>? {
    return getEvent(eventType, eventAddress)?.let { it to eventAddress } ?: run {
      val adjustedAddress = eventAddress.copy(id = (eventAddress.id + 1) % 2)
      getEvent(eventType, adjustedAddress)?.let { it to adjustedAddress }
    }
  }

  private fun setState(state: SelectState) {
    selectState.value = state
    coroutineScope.launch {
      selectState.emit(state)
    }
  }

  private fun updateState(func:SelectState.()->SelectState) {
    setState(selectState.value.func())
  }
}
