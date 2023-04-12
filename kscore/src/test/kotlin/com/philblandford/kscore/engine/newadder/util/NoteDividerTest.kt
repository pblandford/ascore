package com.philblandford.kscore.engine.newadder.util

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.score.Tuplet
import com.philblandford.kscore.engine.core.score.tuplet
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.time.TimeSignature
import org.junit.Test

class NoteDividerTest {

  @Test
  fun testDivideNoteSimple() {
    val map = divideNote(Note(crotchet()).toEvent(), eav(1), {null}) { ts(it) }
    assertEqual(
      "N" +
          "4", map.eventString()
    )
  }

  @Test
  fun testDivideNoteOverBar() {
    val map = divideNote(Note(breve()).toEvent(), eav(1), {null}) { ts(it) }.toList().sortedBy { it.first }
    assertEqual(eav(1), map.first().first)
    assertEqual(semibreve(), map.first().second.duration())
    assertEqual(eav(2), map.last().first)
    assertEqual(semibreve(), map.last().second.duration())
  }

  @Test
  fun testDivideNoteOverTuplet() {
    val tupletFunc = tupletFunc(listOf(T(3, crotchet(), dZero())))

    val map = divideNote(Note(crotchet()).toEvent(), eav(1, Duration(1,6)),
      tupletFunc) { ts(it) }.toList().sortedBy { it.first }
    assertEqual(eav(1, Duration(1,6)), map.first().first)
    assertEqual(quaver(), map.first().second.duration())
    assertEqual(Duration(1,12), map.first().second.realDuration())
    assertEqual(eav(1, crotchet()), map.last().first)
    assertEqual(quaver(), map.last().second.duration())
  }

  data class T(val numerator:Int, val duration: Duration, val offset: Offset, val barNum:Int = 1)

  private fun tupletFunc(descriptors:Iterable<T>):(EventAddress)->Tuplet? {

    val tuplets = descriptors.map {
      ez(it.barNum, it.offset) to  tuplet(it.offset, it.numerator, it.duration) }

    val func:(EventAddress) -> Tuplet? = { addr ->
      tuplets.find { (key, value) ->
        key.barNum == addr.barNum
          && key.offset <= addr.offset
            && key.offset.add(value.realDuration) > addr.offset}?.second
    }
    return func
  }

  private fun ts(eventAddress: EventAddress): TimeSignature {
    return TimeSignature(4, 4)
  }

  private fun Map<EventAddress, Event>.eventString(): String {
    return this.toList().joinToString(separator = "") { it.second.asString() + ":" }.dropLastWhile { it == ':' }
  }
}