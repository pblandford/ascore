package beam

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.beam.BeamMap
import com.philblandford.kscore.engine.beam.createBeams
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.time.TimeSignature
import getHashFromString
import org.junit.Test

class BeamCreatorTest {

  @Test
  fun testBeamTwoQuavers() {
    val map = getBeams("C8:C8", TimeSignature(2, 4))
    assertEqual(listOf("8:8"), map.getBeamStrings())
  }

  @Test
  fun testBeamFourQuavers() {
    val map = getBeams("C8:C8:C8:C8", TimeSignature(2, 4))
    assertEqual(listOf("8:8:8:8"), map.getBeamStrings())
  }

  @Test
  fun testBeamEightQuavers() {
    val map = getBeams(r("C8", 8), TimeSignature(4, 4))
    assertEqual(listOf("8:8:8:8", "8:8:8:8").toList(), map.getBeamStrings())
  }

  @Test
  fun testBeamTwoQuaversSeparate() {
    val map = getBeams("C8:R8:C8:R8", TimeSignature(2, 4))
    assertEqual(listOf<String>(), map.getBeamStrings())
  }

  @Test
  fun testBeamDottedQuaverSemiquaver() {
    val map = getBeams("C3/16:C16", TimeSignature(2, 4))
    assertEqual(listOf("3/16:16"), map.getBeamStrings())
  }

  @Test
  fun testBeamDottedQuaverTwoDemisemqiquavers() {
    val map = getBeams("C3/16:C32:C32", TimeSignature(2, 4))
    assertEqual(listOf("3/16:32:32"), map.getBeamStrings())
  }

  @Test
  fun testBeamDottedQuaverSemiquaverSemiquaver() {
    val map = getBeams("C3/16:C16:C16", TimeSignature(2, 4))
    assertEqual(listOf("3/16:16"), map.getBeamStrings())
  }

  @Test
  fun testBeamQuavers3_4() {
    val map = getBeams(r("C8", 6), TimeSignature(3, 4))
    assertEqual(listOf("8:8:8:8:8:8"), map.getBeamStrings())
  }

  @Test
  fun testBeamQuavers3_4OneCrotchet() {
    val map = getBeams("C4:" + r("C8", 4), TimeSignature(3, 4))
    assertEqual(listOf("8:8:8:8"), map.getBeamStrings())
  }

  @Test
  fun testBeamFourSemiquavers() {
    val map = getBeams("C16:C16:C16:C16", TimeSignature(2, 4))
    assertEqual(listOf("16:16:16:16"), map.getBeamStrings())
  }

  @Test
  fun testBeamQuavers3_8() {
    val map = getBeams(r("C8", 3), TimeSignature(3, 8))
    assertEqual(listOf("8:8:8"), map.getBeamStrings())
  }

  @Test
  fun testBeamSemiquavers3_8() {
    val map = getBeams(r("C16", 6), TimeSignature(3, 8))
    assertEqual(listOf("16:16:16:16:16:16"), map.getBeamStrings())
  }

  @Test
  fun testBeamQuavers5_8() {
    val map = getBeams(r("C8", 5), TimeSignature(5, 8))
    assertEqual(listOf("8:8:8", "8:8").toList(), map.getBeamStrings().toList())
  }

  @Test
  fun testBeamQuavers6_8() {
    val map = getBeams(r("C8", 6), TimeSignature(6, 8))
    verify(map, "8:8:8", "8:8:8")
  }

  @Test
  fun testBeamQuavers9_8() {
    val map = getBeams(r("C8", 9), TimeSignature(9, 8))
    verify(map, "8:8:8", "8:8:8", "8:8:8")
  }

  @Test
  fun testBeamSemiquavers6_8() {
    val map = getBeams(r("C16", 12), TimeSignature(6, 8))
    verify(map, "16:16:16:16:16:16", "16:16:16:16:16:16")
  }

  @Test
  fun testBeamSemiquavers12_8() {
    val map = getBeams(r("C16", 24), TimeSignature(12, 8))
    verify(map, "16:16:16:16:16:16", "16:16:16:16:16:16", "16:16:16:16:16:16", "16:16:16:16:16:16")
  }

  @Test
  fun testBeamDottedNotes6_8() {
    val map = getBeams("C3/16:C16:C8", TimeSignature(6, 8))
    assertEqual(listOf("3/16:16:8").toList(), map.getBeamStrings())
  }

  @Test
  fun testBeamQuaverTwoSemiquavers() {
    val map = getBeams("C8:C16:C16")
    verify(map, "8:16:16")
  }

  @Test
  fun testRestsNotBeamed() {
    val map = getBeams("R8:R8:R4:R2")
    verify(map)
  }

  @Test
  fun testDottedQuaverSemiquaverTwice() {
    val map = getBeams("C3/16:C16:C3/16:C16")
    verify(map, "3/16:16:3/16:16")
  }

  @Test
  fun testBeamSQS() {
    val map = getBeams("C16:C8:C16")
    verify(map, "16:8:16")
  }

  @Test
  fun testBeamGrace() {
    val map = getBeams("", graceNotes = mapOf(dZero() to "C16:C16"))
    verify(map, "16:16")
  }

  @Test
  fun testBeamGraceWithCrotchet() {
    val map = getBeams("", graceNotes = mapOf(dZero() to "C16:C16:C4"))
    verify(map, "16:16")
  }

  @Test
  fun testBeamGraceWithRest() {
    val map = getBeams("", graceNotes = mapOf(dZero() to "C16:R16"))
    verify(map)
  }

  @Test
  fun testBeamUserBeam() {
    val beams = getUserBeams(listOf(dZero() to Duration(5,8)))
    val map = getBeams(r("C8", 8), TimeSignature(4,4), beams)
    assertEqual(1, map.size)
    assertEqual(minim(1), map.toList().first().first.offset)
    assertEqual(2, map.toList().first().second.members.toList().size)
  }
}

private fun verify(map:BeamMap, vararg strings:String) {
  assertEqual(strings.toList(), map.getBeamStrings().toList())
}

private fun getBeams(string: String,
                     timeSignature: TimeSignature = TimeSignature(4,4),
                     userBeams: EventHash = eventHashOf(),
                     graceNotes:Map<Offset, String> = mapOf()
): BeamMap {
  var hash = getHashFromString(string)
  hash = graceNotes.toList().fold(hash) { h, (offset, string) ->
    hash.plus(getHashFromString(string, offset))
  }
  return createBeams(hash, userBeams, timeSignature)
}

private fun r(text: String, num: Int): String {
  val full = (1..num).fold("") { s, _ -> "$s$text:" }
  return full.dropLast(1)
}

private fun getUserBeams(beams:List<Pair<Duration, Duration>>): EventHash {
  return beams.map {
    EventMapKey(EventType.BEAM, ez(0, it.first)) to Event(EventType.BEAM,
      paramMapOf(EventParam.DURATION to it.second))
  }.toMap()
}
 fun BeamMap.getBeamStrings():Iterable<String> {
  return map { (_, beam) ->
    beam.members.map {
      if (it.duration.numerator == 1) {
        it.duration.denominator.toString() + ":"
      } else {
        "${it.duration.numerator}/${it.duration.denominator}" + ":"
      }
    }.reduce { acc, s -> acc + s }.dropLast(1)
  }.toList()
}