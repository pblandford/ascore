package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.eav


import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.core.representation.RepTest
import com.philblandford.kscore.engine.types.DurationType
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.paramMapOf
import org.junit.Test

class RepeatBeatTest : RepTest() {

    @Test
    fun testRepeatBeatCreated() {
        SAE(
            Event(
                EventType.DURATION,
                DurationType.REPEAT_BEAT,
                params = paramMapOf(EventParam.DURATION to crotchet())
            ), eav(1)
        )
        RVA("RepeatBeat", eav(1))
    }

    @Test
    fun testRestRemoved() {
        SAE(
            Event(
                EventType.DURATION,
                DurationType.REPEAT_BEAT,
                params = paramMapOf(EventParam.DURATION to crotchet())
            ), eav(1)
        )
        RVNA("Rest", eav(1))
    }

}