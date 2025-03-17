package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.types.DurationType
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.eav
import com.philblandford.kscore.engine.types.paramMapOf
import org.junit.Test

class RepeatBeatTest : ScoreTest() {

    @Test
    fun testAddRepeatBeat() {
        SMV()
        SAE(EventType.DURATION, eav(1), params = paramMapOf(EventParam.DURATION to crotchet(), EventParam.TYPE to DurationType.REPEAT_BEAT))
        SVP(EventType.DURATION, EventParam.DURATION, crotchet(), eav(1))
        SVP(EventType.DURATION, EventParam.TYPE, DurationType.REPEAT_BEAT, eav(1))
        SVVM("B4:R4:R2", eav(1))
    }
}