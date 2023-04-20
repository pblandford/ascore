package core.geographyY

import assertEqual
import com.philblandford.kscore.engine.core.LayoutDescriptor
import com.philblandford.kscore.engine.core.areadirectory.areaDirectory
import com.philblandford.kscore.engine.core.geographyX.geographyXDirectory
import com.philblandford.kscore.engine.core.geographyY.geographyYDirectory
import com.philblandford.kscore.engine.core.representation.PAGE_WIDTH
import com.philblandford.kscore.engine.core.stave.partDirectory
import com.philblandford.kscore.engine.dsl.createScoreOneNote
import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class GeographyYDirectoryTest : RepTest() {

    @Test
    fun testCreateSystemGeographyDirectory() {
        val score = createScoreOneNote()
        val ad = drawableFactory.areaDirectory(score)!!
        val gx = geographyXDirectory(ad, score, PAGE_WIDTH)!!
        val staveDirectory = drawableFactory.partDirectory(score, ad, gx, LayoutDescriptor())!!
        val gy = geographyYDirectory(staveDirectory, gx, score)
        assertEqual(1, gy?.getSystemYGeographies()?.toList()?.size)
        assertEqual(1, gy?.getPageGeographies()?.toList()?.size)
    }
}