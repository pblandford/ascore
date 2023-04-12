package core.stave

import assertEqual
import com.philblandford.kscore.msq
import com.philblandford.kscore.engine.core.areadirectory.segment.SegmentArea
import com.philblandford.kscore.engine.core.areadirectory.segment.segmentArea
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.map.EventMapKey
import org.junit.Test
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.*
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarEndAreaPair
import com.philblandford.kscore.engine.core.areadirectory.barstartend.BarStartAreaPair
import com.philblandford.kscore.engine.core.stave.StaveArea
import com.philblandford.kscore.engine.core.stave.createStave
import com.philblandford.kscore.engine.dsl.dslChord
import core.representation.RepTest

class StaveAreaTest : RepTest() {

    @Test
    fun testStaveLinesCreated() {
        val stave = doCreateStave()
        assertEqual(5, stave.base.findByTag("StaveLine").size)
        val lines = stave.base.findByTag("StaveLines").toList().first().second
        lines.childMap.toList().withIndex().forEach {
            assertEqual(it.index * BLOCK_HEIGHT * 2, it.value.first.coord.y)
        }
    }

    private fun doCreateStave(): StaveArea {
        val sl = mapOf(ea(1) to crotchetSegment())
        val sg = SystemXGeography(1, 1, 0, 0,
                sortedMapOf(1 to BarPosition(0, ResolvedBarGeography())), 1.0f)
        return drawableFactory.createStave(sl, mapOf(), bsl(), bel(), sg, msq(), ea(1), true)!!
    }

    private fun crotchetSegment(): SegmentArea {
        return drawableFactory.segmentArea(mapOf(
          EventMapKey(
            EventType.DURATION,
            eav(1)
          ) to dslChord(crotchet())),
                ea(1))!!
    }

    private fun barGeography(): BarGeography {
        return BarGeography(BarStartGeographyPair(), BarEndGeographyPair(),
                mapOf(hZero() to SlicePosition(0, 0, 100)))
    }


}

private fun bsl() = mapOf<EventAddress, BarStartAreaPair>()
private fun bel() = mapOf<EventAddress, BarEndAreaPair>()