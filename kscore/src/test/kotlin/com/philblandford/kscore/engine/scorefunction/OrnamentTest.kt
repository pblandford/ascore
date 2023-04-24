package com.philblandford.kscore.engine.scorefunction


import assertEqual
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.duration.quaver
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import org.junit.Test

class OrnamentTest : ScoreTest() {

  @Test
  fun testAddOrnament() {
    SMV()
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    SVP(
      EventType.DURATION,
      EventParam.ORNAMENT,
      ChordDecoration(items = listOf(Ornament(OrnamentType.TRILL))),
      eav(1)
    )
  }

  @Test
  fun testAddOrnamentReplacesOld() {
    SMV()
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TURN))
    SVP(
      EventType.DURATION,
      EventParam.ORNAMENT,
      ChordDecoration(items = listOf(Ornament(OrnamentType.TURN))),
      eav(1)
    )
  }

  @Test
  fun testAddOrnamentAccidental() {
    SMV()
    SAE(
      EventType.ORNAMENT, eav(1), paramMapOf(
        EventParam.TYPE to OrnamentType.TRILL,
        EventParam.ACCIDENTAL_ABOVE to Accidental.FLAT
      )
    )
    SVP(
      EventType.DURATION,
      EventParam.ORNAMENT,
      ChordDecoration(items = listOf(Ornament(OrnamentType.TRILL, Accidental.FLAT))),
      eav(1)
    )
  }

  @Test
  fun testAddOrnamentNoChord() {
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    SVNP(EventType.DURATION, EventParam.ORNAMENT, eav(1))
  }

  @Test
  fun testDeleteOrnament() {
    SMV()
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    SDE(EventType.ORNAMENT, eav(1))
    SVNP(
      EventType.DURATION,
      EventParam.ORNAMENT,
      eav(1)
    )
  }

  @Test
  fun testDeleteOrnamentRange() {
    SMV()
    SMV(eventAddress = eav(2))
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    SAE(EventType.ORNAMENT, eav(2), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    SDE(EventType.ORNAMENT, eav(1), endAddress = eav(2))
    SVNP(EventType.DURATION, EventParam.ORNAMENT, eav(1))
    SVNP(EventType.DURATION, EventParam.ORNAMENT, eav(2))
  }

  @Test
  fun testGetOrnamentAsEvent() {
    SMV()
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    SVP(EventType.ORNAMENT, EventParam.TYPE, OrnamentType.TRILL, eav(1))
  }

  @Test
  fun testAddOrnamentAtTuplet() {
    SAE(tuplet(dZero(), 3, 8).toEvent(), eav(1))
    SMV(duration = quaver())
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    SVP(
      EventType.DURATION,
      EventParam.ORNAMENT,
      ChordDecoration(items = listOf(Ornament(OrnamentType.TRILL))),
      eav(1)
    )
  }

  @Test
  fun testAddOrnamentMarkerStays() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    SVP(EventType.UISTATE, EventParam.MARKER_POSITION, ea(1, minim()), ez(0))
  }

  @Test
  fun testSetType() {
    SMV()
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    SSP(EventType.ORNAMENT, EventParam.TYPE, OrnamentType.LOWER_MORDENT, eav(1))
    val ornament = EG().getParam<ChordDecoration<Ornament>>(EventType.DURATION, EventParam.ORNAMENT, eav(1))
    assertEqual(OrnamentType.LOWER_MORDENT, ornament?.items?.first()?.ornamentType)
  }
}