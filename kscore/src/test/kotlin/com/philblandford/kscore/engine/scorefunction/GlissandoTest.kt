package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.times
import org.junit.Test

class GlissandoTest : ScoreTest() {

    @Test
    fun testAddGlissando() {
        SAE(EventType.GLISSANDO, ea(1), paramMapOf(EventParam.IS_STRAIGHT to false))
        SVP(EventType.GLISSANDO, EventParam.IS_STRAIGHT, false, ea(1))
    }

    @Test
    fun testAddGlissandoEndCreated() {
        SMV()
        SMV(eventAddress = eav(1, crotchet()))
        SAE(
            EventType.GLISSANDO, ea(1), paramMapOf(
                EventParam.IS_STRAIGHT to false,
                EventParam.IS_UP to false
            )
        )
        SVP(EventType.GLISSANDO, EventParam.END, true, ea(1, crotchet()))
    }

    @Test
    fun testAddGlissandoLastBeatOfScore() {
        SCD(bars = 1)
        repeat(4) {
            SMV(eventAddress = eav(1, crotchet() * it))
        }
        SAE(EventType.GLISSANDO, ea(1, crotchet() * 3), paramMapOf(EventParam.IS_STRAIGHT to false))
        SVP(EventType.GLISSANDO, EventParam.IS_STRAIGHT, false, ea(1, crotchet() * 3))
    }

    @Test
    fun testAddGlissandoLastBeatOfScoreSecondPart() {
        SCD(bars = 1, instruments = listOf("Violin", "Violin"))
        repeat(4) {
            SMV(eventAddress = easv(1, crotchet() * it, staveId = StaveId(2, 1)))
        }
        SAE(
            EventType.GLISSANDO,
            eas(1, crotchet() * 3, StaveId(2, 1)),
            paramMapOf(EventParam.IS_STRAIGHT to false)
        )
        SVP(
            EventType.GLISSANDO,
            EventParam.IS_STRAIGHT,
            false,
            eas(1, crotchet() * 3, StaveId(2, 1))
        )
    }

}