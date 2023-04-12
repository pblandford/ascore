package core.score

import assertEqual
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.emptyEventMap
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*
import getHashFromString
import org.junit.Test

class VoiceNumberMapTest {
  @Test
  fun testOneNote() {
    val vms = getVoiceMaps("C4")
    val vnm = voiceNumberMap(vms)
    assertEqual(1, vnm.voicesAt(dZero()))
  }

  @Test
  fun testTwoVoices() {
    val vms = getVoiceMaps("C4", "C4")
    val vnm = voiceNumberMap(vms)
    assertEqual(2, vnm.voicesAt(dZero()))
  }

  @Test
  fun testLongNoteAgainstShort() {
    val vms = getVoiceMaps("C2", "C4:C4")
    val vnm = voiceNumberMap(vms)
    assertEqual(2, vnm.voicesAt(dZero()))
    assertEqual(2, vnm.voicesAt(crotchet()))
  }

  @Test
  fun testLongNoteAgainstShortSecondHalfEmpty() {
    val vms = getVoiceMaps("C2:C2", "C4:C4")
    val vnm = voiceNumberMap(vms)
    assertEqual(2, vnm.voicesAt(dZero()))
    assertEqual(2, vnm.voicesAt(crotchet()))
    assertEqual(1, vnm.voicesAt(minim()))
  }

  @Test
  fun testDottedNoteAgainstShorter() {
    val vms = getVoiceMaps("C3/8:C8", "C4:C8:C8")
    val vnm = voiceNumberMap(vms)
    assertEqual(2, vnm.voicesAt(dZero()))
    assertEqual(2, vnm.voicesAt(crotchet()))
    assertEqual(2, vnm.voicesAt(crotchet(1)))
  }

  @Test
  fun testRealWrittenDurationsDiffer() {
    val vms = getVoiceMaps("C1/12:C1/12:C1/12").map {
      val new = it.eventMap.getEvents(EventType.DURATION)!!.map {
        it.key to it.value.addParam(EventParam.DURATION, quaver())
      }.toMap()
      VoiceMap(it.timeSignature, it.eventMap.replaceEvents(EventType.DURATION, new))
    }
    val vnm = voiceNumberMap(vms)
    assertEqual(1, vnm.voicesAt(dZero()))
    assertEqual(1, vnm.voicesAt(Duration(1, 12)))
    assertEqual(1, vnm.voicesAt(Duration(1, 6)))
  }

  @Test
  fun testTwoVoicesFirstIsEmpty() {
    val vms = getVoiceMaps("", "C4")
    val vnm = voiceNumberMap(vms)
    assertEqual(2, vnm.voicesAt(dZero()))
  }

  @Test
  fun testTwoVoicesSecondIsEmpty() {
    val vms = getVoiceMaps("C4", "")
    val vnm = voiceNumberMap(vms)
    assertEqual(1, vnm.voicesAt(dZero()))
  }

  @Test
  fun testTwoVoicesSecondMarkedWithEmpty() {
    val vms = getVoiceMaps("C4", "E4")
    val vnm = voiceNumberMap(vms)
    assertEqual(1, vnm.voicesAt(dZero()))
  }

  @Test
  fun testTwoVoicesSecondMarkedWithEmptyFirstNote() {
    val vms = getVoiceMaps("C4:C4", "E4:C4")
    val vnm = voiceNumberMap(vms)
    assertEqual(1, vnm.voicesAt(dZero()))
    assertEqual(2, vnm.voicesAt(crotchet()))
  }



  private fun getVoiceMaps(vararg strings: String): Iterable<VoiceMap> {
    return strings.map {
      val hash = getHashFromString(it)
      voiceMap(
        TimeSignature(4, 4),
        emptyEventMap().replaceEvents(EventType.DURATION, hash)
      )
    }
  }



}
