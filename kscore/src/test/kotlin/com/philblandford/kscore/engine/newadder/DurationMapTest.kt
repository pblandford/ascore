package com.philblandford.kscore.engine.newadder

import assertEqual
import com.philblandford.kscore.engine.beam.SimpleDuration
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.newadder.duration.*
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.DurationType
import org.junit.Test

class DurationMapTest {

  @Test
  fun testInsertSemibreve() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(semibreve()), dZero()).rightOrThrow()
    assertEqual("C1", map.eventString())
  }

  @Test
  fun testInsertMinim() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(minim()), dZero()).rightOrThrow()
    assertEqual("C2:R2", map.eventString())
  }

  @Test
  fun testInsertDottedMinimCrotchet() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(minim(1)), dZero()).rightOrThrow()
    map = map.add(DEvent(crotchet()), minim(1)).rightOrThrow()
    assertEqual("C3/4:C4", map.eventString())
  }

  @Test
  fun testInsertCrotchet() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet()), dZero()).rightOrThrow()
    assertEqual("C4:R4:R2", map.eventString())
  }


  @Test
  fun testInsertDottedCrotchetQuaver() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet(1)), dZero()).rightOrThrow()
    map = map.add(DEvent(quaver()), crotchet(1)).rightOrThrow()
    assertEqual("C3/8:C8:R2", map.eventString())
  }

  @Test
  fun testInsertDottedCrotchetSecondBeat() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet(1)), crotchet()).rightOrThrow()
    assertEqual("R4:C3/8:R8:R4", map.eventString())
  }

  @Test
  fun testInsertQuaver() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(quaver()), dZero()).rightOrThrow()
    assertEqual("C8:R8:R4:R2", map.eventString())
  }

  @Test
  fun testInsertTwoQuavers() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(quaver()), dZero()).rightOrThrow()
    map = map.add(DEvent(quaver()), quaver()).rightOrThrow()
    assertEqual("C8:C8:R4:R2", map.eventString())
  }

  @Test
  fun testInsertThreeQuavers() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(quaver()), dZero()).rightOrThrow()
    map = map.add(DEvent(quaver()), quaver()).rightOrThrow()
    map = map.add(DEvent(quaver()), crotchet()).rightOrThrow()
    assertEqual("C8:C8:C8:R8:R2", map.eventString())
  }

  @Test
  fun testInsertQuaverMidBar() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(quaver()), minim()).rightOrThrow()
    assertEqual("R2:C8:R8:R4", map.eventString())
  }

  @Test
  fun testInsertDottedCrotchet() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet(1)), dZero()).rightOrThrow()
    assertEqual("C3/8:R8:R2", map.eventString())
  }

  @Test
  fun testInsertDoubleDottedCrotchet() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet(2)), dZero()).rightOrThrow()
    assertEqual("C7/16:R16:R2", map.eventString())
  }

  @Test
  fun testInsertCrotchet3_4() {
    var map = DurationMap(TimeSignature(3, 4))
    map = map.add(DEvent(crotchet()), dZero()).rightOrThrow()
    assertEqual("C4:R4:R4", map.eventString())
  }

  @Test
  fun testInsertDottedCrotchetQuaver3_4() {
    var map = DurationMap(TimeSignature(3, 4))
    map = map.add(DEvent(crotchet(1)), dZero()).rightOrThrow()
    map = map.add(DEvent(quaver()), crotchet(1)).rightOrThrow()
    assertEqual("C3/8:C8:R4", map.eventString())
  }

  @Test
  fun testInsertDottedCrotchet6_8() {
    var map = DurationMap(TimeSignature(6, 8))
    map = map.add(DEvent(crotchet(1)), dZero()).rightOrThrow()
    assertEqual("C3/8:R3/8", map.eventString())
  }

  @Test
  fun testInsertDottedCrotchet9_8() {
    var map = DurationMap(TimeSignature(9, 8))
    map = map.add(DEvent(crotchet(1)), dZero()).rightOrThrow()
    assertEqual("C3/8:R3/8:R3/8", map.eventString())
  }

  @Test
  fun testInsertDottedQuaver6_8() {
    var map = DurationMap(TimeSignature(6, 8))
    map = map.add(DEvent(quaver(1)), dZero()).rightOrThrow()
    assertEqual("C3/16:R16:R8:R3/8", map.eventString())
  }

  @Test
  fun testInsertDottedQuaverSemiquaver6_8() {
    var map = DurationMap(TimeSignature(6, 8))
    map = map.add(DEvent(quaver(1)), dZero()).rightOrThrow()
    map = map.add(DEvent(semiquaver()), quaver(1)).rightOrThrow()
    assertEqual("C3/16:C16:R8:R3/8", map.eventString())
  }

  @Test
  fun testInsertDottedQuaver6_8SecondBeat() {
    var map = DurationMap(TimeSignature(6, 8))
    map = map.add(DEvent(quaver(1)), crotchet(1)).rightOrThrow()
    assertEqual("R3/8:C3/16:R16:R8", map.eventString())
  }

  @Test
  fun testAddCrotchetToExisting() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet()), dZero()).rightOrThrow()
    map = map.add(DEvent(crotchet()), crotchet()).rightOrThrow()
    assertEqual("C4:C4:R2", map.eventString())
  }

  @Test
  fun testAddCrotchetToExistingSecondMinim() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet()), dZero()).rightOrThrow()
    map = map.add(DEvent(crotchet()), minim()).rightOrThrow()
    assertEqual("C4:R4:C4:R4", map.eventString())
  }

  @Test
  fun testInsertMinimOverCrotchet() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet()), dZero()).rightOrThrow()
    map = map.add(DEvent(minim()), dZero()).rightOrThrow()
    assertEqual("C2:R2", map.eventString())
  }

  @Test
  fun testInsertCrotchetOverQuaver() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(quaver()), dZero()).rightOrThrow()
    map = map.add(DEvent(quaver()), quaver()).rightOrThrow()
    map = map.add(DEvent(crotchet()), dZero()).rightOrThrow()
    assertEqual("C4:R4:R2", map.eventString())
  }

  @Test
  fun testRemoveSemibreve() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(semibreve()), dZero()).rightOrThrow()
    map = map.delete(dZero()).rightOrThrow()
    assertEqual("", map.eventString())
  }

  @Test
  fun testRemoveMinim() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(minim()), dZero()).rightOrThrow()
    map = map.delete(dZero()).rightOrThrow()
    assertEqual("", map.eventString())
  }

  @Test
  fun testRemoveFirstCrotchet() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet()), dZero()).rightOrThrow()
    map = map.add(DEvent(crotchet()), crotchet()).rightOrThrow()
    map = map.delete(dZero()).rightOrThrow()
    assertEqual("R4:C4:R2", map.eventString())
  }

  @Test
  fun testInsertTupletMarker() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(minim(), DurationType.TUPLET_MARKER), dZero()).rightOrThrow()
    assertEqual("T2:R2", map.eventString())
  }

  @Test
  fun testAddMinimRest() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(minim(), DurationType.REST), dZero()).rightOrThrow()
    assertEqual("R2:R2", map.eventString())
  }

  @Test
  fun testAddCrotchetRest() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet(), DurationType.REST), dZero()).rightOrThrow()
    assertEqual("R4:R4:R2", map.eventString())
  }

  @Test
  fun testAddCrotchetRestHalfway() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet(), DurationType.REST), minim()).rightOrThrow()
    assertEqual("R2:R4:R4", map.eventString())
  }

  @Test
  fun testInsertMinimHalfway() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(minim()), minim()).rightOrThrow()
    assertEqual("R2:C2", map.eventString())
  }

  @Test
  fun testInsertCrotchetLastBeatHalfway() {
    var map = "C2:R2".toDMap(TimeSignature(4,4))
    map = map.add(DEvent(crotchet()), minim(1)).rightOrThrow()
    assertEqual("C2:R4:C4", map.eventString())
  }

  @Test
  fun testInsertQuaverLastBeatHalfway() {
    var map = "C2:R2".toDMap(TimeSignature(4,4))
    map = map.add(DEvent(quaver()), minim(1)).rightOrThrow()
    assertEqual("C2:R4:C8:R8", map.eventString())
  }

  @Test
  fun testRemoveMinimRest() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(minim(), DurationType.REST), dZero()).rightOrThrow()
    map = map.delete(dZero()).rightOrThrow()
    assertEqual("", map.eventString())
  }

  @Test
  fun testDeleteCrotchetRestHalfway() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet(), DurationType.REST), minim()).rightOrThrow()
    map = map.delete(minim()).rightOrThrow()
    assertEqual("R2:R2", map.eventString())
  }

  @Test
  fun testDeleteCrotchetRestFirstCrotchet() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet(), DurationType.REST), dZero()).rightOrThrow()
    map = map.delete(crotchet()).rightOrThrow()
    assertEqual("R2:R2", map.eventString())
  }

  @Test
  fun testDeleteCrotchetRestLastCrotchet() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet(), DurationType.REST), minim()).rightOrThrow()
    map = map.delete(minim(1)).rightOrThrow()
    assertEqual("R2:R2", map.eventString())
  }

  @Test
  fun testInsertCrotchet5_4() {
    var map = DurationMap(TimeSignature(5, 4))
    map = map.add(DEvent(crotchet()), dZero()).rightOrThrow()
    assertEqual("C4:R4:R4:R2", map.eventString())
  }

  @Test
  fun testInsertCrotchet7_4() {
    var map = DurationMap(TimeSignature(7, 4))
    map = map.add(DEvent(crotchet()), dZero()).rightOrThrow()
    assertEqual("C4:R4:R2:R3/4", map.eventString())
  }

  @Test
  fun testInsertCrotchet7_4AcrossBoundary() {
    var map = DurationMap(TimeSignature(7, 4))
    map = map.add(DEvent(crotchet()), Offset(7, 8)).rightOrThrow()
    assertEqual("R2:R4:R8:C4:R8:R4:R4", map.eventString())
  }

  @Test
  fun testInsertCrotchet7_4AcrossBoundaryQuaverBefore() {
    var map = "C4:C4:C4:R8:C8:R3/4".toDMap(TimeSignature(7, 4))
    map = map.add(DEvent(crotchet()), Offset(4, 4)).rightOrThrow()
    assertEqual("C4:C4:C4:R8:C8:C4:R4:R4", map.eventString())
  }

  @Test
  fun testInsertCrotchet7_4OnBoundary() {
    var map = DurationMap(TimeSignature(7, 4))
    map = map.add(DEvent(crotchet()), Offset(4, 4)).rightOrThrow()
    assertEqual("R1:C4:R4:R4", map.eventString())
  }

  @Test
  fun testAddRestRequestConsolidate() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet(), DurationType.REST), dZero(), true).rightOrThrow()
    assertEqual("", map.eventString())
  }

  @Test
  fun testRemoveEventRequestConsolidate() {
    var map = DurationMap(TimeSignature(4, 4))
    map = map.add(DEvent(crotchet(), DurationType.REST), dZero()).rightOrThrow()
    map = map.delete(dZero(), true).rightOrThrow()
    assertEqual("", map.eventString())
  }


  private fun String.toDMap(timeSignature: TimeSignature):DurationMap {
    val fields = split(":")
    var offset = dZero()
    val events = fields.map { f ->
      val type = if (f[0] == 'C') DurationType.CHORD else DurationType.REST
      val durationString = f.drop(1)
      val dFields = durationString.split("/")
      val duration = if (dFields.size == 1) {
        Duration(1, dFields[0].toInt())
      } else {
        Duration(dFields[0].toInt(), dFields[1].toInt())
      }
      val thisOffset = offset
      offset += duration
      thisOffset to DEvent(duration, type)
    }
    return DurationMap(timeSignature, events.toMap().toSortedMap())
  }
}