package core.score

import com.philblandford.kscore.engine.types.*
import assertEqual
import com.philblandford.kscore.msq
import com.philblandford.kscore.engine.core.score.Part
import com.philblandford.kscore.engine.dsl.dslPart
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.semibreve
import org.junit.Test

class PartTest {
  @Test
  fun testCreatePart() {
    val p = dslPart {
      stave {
        bar {}
      }
    }
    assertEqual(1, p.staves.count())
  }


}