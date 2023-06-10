package com.philblandford.kscore.sound

import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.time.timeSignature
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.EventType.*
import com.philblandford.kscore.engine.types.NavigationType.*
import com.philblandford.kscore.log.ksLogv

typealias BarList = Iterable<Int>
typealias OTAMap = Map<Duration, EventAddress>

fun midiBarList(scoreQuery: ScoreQuery, start: Int? = null, end: Int? = null): Iterable<Int> {
  ksLogv("Creating barList")
  var events =
    scoreQuery.collateEvents(listOf(NAVIGATION, REPEAT_END, REPEAT_START, VOLTA))?.filterNot { it.key.eventAddress.barNum < 1 } ?: eventHashOf()
  events = events.filterNot { it.value.isTrue(EventParam.END) && it.value.getInt(EventParam.NUM_BARS, 1) > 1 }
  return midiBarList(events, scoreQuery.numBars, start, end)
}


fun midiBarList(
  events: EventHash, numBars: Int, start: Int? = null,
  end: Int? = null
): BarList {
  var list = doGetBarList(events, numBars)
  start?.let {
    list = list.dropWhile { it < start }
  }
  end?.let {
    list = list.takeWhile { it <= end }
  }
  return list
}


private fun doGetBarList(
  events: EventHash,
  numBars: Int,
  initRepeat: Int? = 1,
  initBlock: Int = 1
): BarList {
  val barList = mutableListOf<Int>()
  val voltaGroups = events.getVoltaGroups()

  var startRepeat: Int? = initRepeat
  var startBlock = initBlock

  val sorted = events.toList()
    .sortedWith(compareBy({ it.first.eventAddress.barNum }, { getPriority(it.second) }))
  sorted.forEach { (key, navigation) ->
    when (key.eventType) {
      REPEAT_START -> {
        startRepeat = key.eventAddress.barNum
      }
      REPEAT_END -> {
        startRepeat?.let { sr ->
          barList.addAll(startBlock until sr)
          voltaGroups.find { it.start <= key.eventAddress.barNum && it.end >= key.eventAddress.barNum}?.let { voltaGroup ->
            voltaGroup.voltas.find { it.start <= key.eventAddress.barNum && it.end >= key.eventAddress.barNum}?.let { volta ->
              barList.addAll(sr until voltaGroup.start)
              barList.addAll(volta.start..volta.end)
              if (volta == voltaGroup.voltas.dropLast(1).lastOrNull()) {
                barList.addAll(sr until voltaGroup.start)
                startRepeat = null
                startBlock = key.eventAddress.barNum+1
              }
            }
          } ?: run {
            barList.addAll(sr..key.eventAddress.barNum)
            barList.addAll(sr..key.eventAddress.barNum)
            startRepeat = null
            startBlock = key.eventAddress.barNum + 1
          }
        }
      }
      NAVIGATION -> {
        when (navigation.subType) {
          DA_CAPO -> {
            barList.addAll(startBlock..key.eventAddress.barNum)
            events.toList().find { it.second.subType == FINE }?.let {
              val repeated = getRepeatedSection(1, it.first.eventAddress.barNum, events)
              barList.addAll(repeated)
              startBlock = numBars + 1
              return@forEach
            }
            val codas = events.toList().filter { it.second.subType == CODA }
              .sortedBy { it.first.eventAddress.barNum }
            if (codas.size == 2) {
              val endBar = codas.first().first.eventAddress.barNum - 1
              val repeated = getRepeatedSection(1, endBar, events)
              barList.addAll(repeated)
              barList.addAll(codas.last().first.eventAddress.barNum..numBars)
              startBlock = numBars + 1
              return@forEach
            }
            barList.addAll(1..key.eventAddress.barNum)
            startBlock = key.eventAddress.barNum + 1
          }
          DAL_SEGNO -> {
            events.toList().find { it.second.subType == SEGNO }?.let { (segnoKey, _) ->
              barList.addAll(startBlock..key.eventAddress.barNum)
              barList.addAll(
                getRepeatedSection(
                  segnoKey.eventAddress.barNum,
                  key.eventAddress.barNum,
                  events.minus(key)
                )
              )
              startBlock = key.eventAddress.barNum + 1
            }
          }
        }
      }
      else -> {
      }
    }
  }
  barList.addAll(startBlock..numBars)

  return barList
}

private fun EventHash.getVoltaGroups():List<VoltaGroup> {

  val voltas = filter { it.key.eventType == VOLTA }.toList().sortedBy { it.first.eventAddress.barNum }
  val groups = mutableListOf<VoltaGroup>()
  var currentGroup: VoltaGroup? = null

  voltas.forEach { (key, volta) ->
    currentGroup?.let { cg ->
      if (cg.end == key.eventAddress.barNum - 1) {
        currentGroup = cg.copy(
          end = volta.endBar(key),
          voltas = cg.voltas + Volta(key.eventAddress.barNum, volta.endBar(key))
        )
      } else {
        groups.add(cg)
        currentGroup = VoltaGroup(
          key.eventAddress.barNum,
          key.eventAddress.barNum + volta.numBars() - 1,
          listOf(Volta(key.eventAddress.barNum, volta.endBar(key))))
      }
    } ?: run {
      currentGroup = VoltaGroup(
        key.eventAddress.barNum,
        key.eventAddress.barNum + volta.numBars() - 1,
        listOf(Volta(key.eventAddress.barNum, volta.endBar(key))))
    }
  }
  currentGroup?.let { groups.add(it) }
  return groups
}

private fun Event.numBars() = getInt(EventParam.NUM_BARS)
private fun Event.endBar(eventMapKey: EventMapKey) = eventMapKey.eventAddress.barNum + numBars() - 1

private data class Volta(val start: Int, val end: Int)

private data class VoltaGroup(val start: Int, val end: Int, val voltas: List<Volta>)

private fun getPriority(event: Event): Int {
  return priorities[Pair(event.eventType, event.subType)] ?: 0
}

private val priorities = mapOf(
  Pair(REPEAT_END, null) to 0,
  Pair(NAVIGATION, DA_CAPO) to 1
)

private fun getRepeatedSection(start: Int, end: Int, events: EventHash): BarList {
  return doGetBarList(events.toList().sortedBy { it.first.eventAddress.barNum }
    .dropWhile { it.first.eventAddress.barNum < start }.takeWhile { it.first.eventAddress.barNum <= end }.toMap(),
    end,
    start,
    start
  )
}

fun eventHashToOffsets(events: EventHash, barList: BarList): Pair<EventHash, OTAMap> {

  var currentTs = events[EMK(TIME_SIGNATURE, ez(1))] ?: TimeSignature(4, 4).toEvent()
  var barStartOffset = dZero()
  val map = mutableMapOf<EventMapKey, Event>()
  val otaMap = mutableMapOf<Duration, EventAddress>()
  val groupedEvents = events.toList().groupBy { it.first.eventAddress.barNum }

  barList.forEach { bar ->
    groupedEvents[bar]?.let { eventsForBar ->
      eventsForBar.forEach { (emk, ev) ->
        val total = barStartOffset.add(emk.eventAddress.offset)
        map[emk.copy(
          eventAddress = emk.eventAddress.copy(
            barNum = 0,
            offset = total,
            staveId = sZero(),
            voice = 0,
            id = 0
          )
        )] = ev
        otaMap.put(total, emk.eventAddress)
      }
      events[EMK(TIME_SIGNATURE, ez(bar))]?.let { currentTs = it }
    }
    barStartOffset = barStartOffset.add(timeSignature(currentTs)?.duration)

  }

  return Pair(map.toSortedMap(), otaMap.toSortedMap())
}
