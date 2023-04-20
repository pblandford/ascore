package core.areadirectory

import com.philblandford.kscore.engine.types.EventType.DURATION
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.eav
import com.philblandford.kscore.engine.core.areadirectory.segment.segmentArea
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class SegmentAreaTest : RepTest() {

  @Test
  fun testCreateSegmentArea() {
    val hash = mapOf(Pair(
      EventMapKey(
        DURATION,
        eav(1)
      ), dslChord(crotchet())))
    val sa = drawableFactory.segmentArea(hash, ea(1))
    assert(sa?.base?.width ?: 0 > 0)
  }


}