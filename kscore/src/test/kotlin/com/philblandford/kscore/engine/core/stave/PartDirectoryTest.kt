package core.stave

import assertEqual
import com.philblandford.kscore.engine.core.LayoutDescriptor
import com.philblandford.kscore.engine.core.areadirectory.areaDirectory
import com.philblandford.kscore.engine.core.geographyX.geographyXDirectory
import com.philblandford.kscore.engine.core.representation.PAGE_WIDTH
import com.philblandford.kscore.engine.core.stave.partDirectory
import com.philblandford.kscore.engine.dsl.createScoreOneNote
import core.representation.RepTest
import org.junit.Test

class PartDirectoryTest : RepTest() {

    @Test
    fun testCreateStaveDirectory() {
        val score = createScoreOneNote()
        val ad = drawableFactory.areaDirectory(score)!!
        val gx = geographyXDirectory(ad, score, PAGE_WIDTH)!!
        val staveDirectory = drawableFactory.partDirectory(score, ad, gx, LayoutDescriptor())
        assertEqual(1, staveDirectory?.getParts()?.size)
    }
}