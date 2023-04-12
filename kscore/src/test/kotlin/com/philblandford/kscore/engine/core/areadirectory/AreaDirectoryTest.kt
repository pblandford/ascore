package core.areadirectory

import assertEqual
import com.philblandford.kscore.engine.core.areadirectory.areaDirectory
import com.philblandford.kscore.engine.dsl.createScoreOneNote
import org.junit.Test
import core.representation.RepTest

class AreaDirectoryTest : RepTest() {

    @Test
    fun testCreateAreaDirectory() {
        val score = createScoreOneNote()
        val ad = drawableFactory.areaDirectory(score)!!
        assertEqual(1, ad.segmentStaveLookup.size)
      //  assert(ad.segmentBarLookup.toList().first().second.base.width > 0)
    }

    @Test
    fun testGetSegmentsForColumn() {
        val score = createScoreOneNote()
        val ad = drawableFactory.areaDirectory(score)!!
        val map = ad.getSegmentGeogsForColumn(1)!!
        assertEqual(1, map.size)
    }
}