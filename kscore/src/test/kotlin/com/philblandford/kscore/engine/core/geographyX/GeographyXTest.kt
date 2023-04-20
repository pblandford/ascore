package core.geographyX

import assertEqual
import com.philblandford.kscore.engine.core.areadirectory.areaDirectory
import com.philblandford.kscore.engine.core.geographyX.geographyXDirectory
import com.philblandford.kscore.engine.core.representation.PAGE_WIDTH
import com.philblandford.kscore.engine.dsl.createScoreOneNote
import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class GeographyXTest : RepTest() {
    @Test
    fun testCreateGeographyXDirectory() {
        val score = createScoreOneNote()
        val areaDir = drawableFactory.areaDirectory(score)!!
        val geogXDir = geographyXDirectory(areaDir, score, PAGE_WIDTH)!!
        assertEqual(1, geogXDir.getSystemXGeographies().toList().size)
    }
}