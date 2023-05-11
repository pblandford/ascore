package com.philblandford.kscore.engine.core.endtoend


import assertEqual
import com.philblandford.kscore.api.defaultInstrument
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.eas

import com.philblandford.kscore.engine.core.representation.RepTest
import com.philblandford.kscore.engine.core.representation.getArea
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ez
import org.junit.Test

class InstrumentTest : RepTest() {

  @Test
  fun testAddInstrument() {
    val instrument = instrumentGetter.getInstrument("Viola")!!
    SAE(instrument.toEvent().copy(EventType.PART), ea(1))

    RVA("Part", eas(1, 1, 0))
    RVA("Part", eas(1, 2, 0))
  }

  @Test
  fun testSetTransposeKeySignatureChanges() {
    SMV(60)
    SAE(
      EventType.INSTRUMENT,
      ea(1),
      defaultInstrument().toEvent().addParam(EventParam.NAME, "Trumpet")
        .addParam(EventParam.TRANSPOSITION, -2).params
    )
    val sharps = REP().getArea("KeySignature", ea(1))?.second?.event?.getParam<Int>(EventParam.SHARPS)
    assertEqual(2,sharps)
  }
}