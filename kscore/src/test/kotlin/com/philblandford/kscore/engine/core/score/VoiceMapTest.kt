package core.score

import com.philblandford.kscore.engine.types.*
import assertEqual
import com.philblandford.kscore.engine.core.score.ScoreLevel
import com.philblandford.kscore.engine.core.score.VoiceMap
import com.philblandford.kscore.engine.core.score.tuplet
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.dsl.dslVoiceMap
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.time.TimeSignature
import org.junit.Test
import voiceMapFromString

class VoiceMapTest {



  @Test
  fun testCreateEmptyMap() {
    val vm = VoiceMap()
    assertEqual(0, vm.getVoiceEvents().count())
  }


  @Test
  fun testGetAllEvents() {
    val vm = dslVoiceMap { chord(); rest(); rest(minim()) }
    val events = vm.getAllEvents()
    assertEqual(
      listOf(dZero(), crotchet(), minim()).toList(),
      events.keys.toList().map { it.eventAddress.offset })
    assertEqual(
      listOf(crotchet(), crotchet(), minim()).toList(),
      events.values.toList().map { it.duration() })
  }

  @Test
  fun testGetAllEventsIncludesTuplet() {
    val vm = dslVoiceMap {
      tuplet(3, 4,
        dslVoiceMap { rest(); rest(); rest() }); rest(minim())
    }
    val events = vm.getAllEvents()
    assert(events.any { it.key.eventType == EventType.TUPLET })
  }


  private fun ScoreLevel.eventString(): String {
    return (this as VoiceMap).eventString()
  }
}