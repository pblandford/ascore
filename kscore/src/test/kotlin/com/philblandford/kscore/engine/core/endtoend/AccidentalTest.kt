package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.eav
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.pitch.KeySignature
import com.philblandford.kscore.engine.types.ez
import core.representation.RepTest
import org.junit.Test

class AccidentalTest : RepTest() {

  @Test
  fun testAccidentalCreated() {
    SMV(73)
    RVA("Accidental", ea(1).copy(id = 1))
  }

  @Test
  fun testAccidentalLeftOfNote() {
    SMV(73)
    assert(isLeft("AccidentalArea", ea(1), "Tadpole", eav(1).copy(id = 1))!!)
  }

  @Test
  fun testAccidentalLeftOfNoteClusterUpstem() {
    SMV(60)
    SMV(63)
    assert(isLeft("AccidentalArea", ea(1), "Tadpole", eav(1).copy(id = 1))!!)
    assert(isLeft("AccidentalArea", ea(1), "Tadpole", eav(1).copy(id = 2))!!)
  }

  @Test
  fun testAccidentalLeftOfNoteClusterDownStem() {
    SMV(72)
    SMV(75)
    assert(isLeft("AccidentalArea", ea(1), "Tadpole", eav(1).copy(id = 1))!!)
    assert(isLeft("AccidentalArea", ea(1), "Tadpole", eav(1).copy(id = 2))!!)
  }

  @Test
  fun testAccidentalCluster() {
    SMV(72)
    SMV(73)
    assert(isLeft("Accidental", ea(1).copy(id = 2), "Accidental", ea(1).copy(id = 1))!!)
    RVA("Accidental", ea(1).copy(id = 1))
  }

  @Test
  fun testNoAccidentalCreatedNewKS() {
    SAE(KeySignature(3).toEvent(), ez(3))
    SMV(73, eventAddress = eav(3, dZero()))
    RVNA("Accidental", ea(3).copy(id = 1))
  }

  @Test
  fun testNoAccidentalCreatedNewKSAfterPreviousDifferent() {
    SMV(72, eventAddress = eav(2))
    SAE(KeySignature(3).toEvent(), ez(3))
    SMV(73, eventAddress = eav(3, dZero()))
    RVNA("Accidental", ea(3).copy(id = 1))
  }
}