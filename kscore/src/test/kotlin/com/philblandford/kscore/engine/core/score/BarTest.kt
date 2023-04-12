package core.score

import com.philblandford.kscore.engine.types.*
import assertEqual
import com.philblandford.kscore.msq
import com.philblandford.kscore.engine.core.score.Bar
import com.philblandford.kscore.engine.core.score.VoiceMap
import com.philblandford.kscore.engine.dsl.dslBar
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.duration
import com.philblandford.kscore.engine.duration.semibreve
import org.junit.Test

class BarTest {

    @Test
    fun testCreateBar() {
        val b = dslBar {
            voiceMaps = listOf(VoiceMap(), VoiceMap())
        }
        assertEqual(2, b.voiceMaps.count())
    }

    @Test
    fun testCreateBarOneEvent() {
        val b = dslBar {
            voiceMap {
                rest(semibreve())
            }
        }
        assertEqual(b.voiceMaps.first().getVoiceEvents().entries.first().value.duration(), semibreve())
    }


}