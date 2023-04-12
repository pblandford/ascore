package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.types.*

import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.quaver
import com.philblandford.kscore.engine.duration.semiquaver

import core.representation.RepTest
import grace
import org.junit.Test

class GraceNoteTest : RepTest() {

  @Test
  fun testAddGraceNote() {
    SMV(
      extraParams = paramMapOf(
        EventParam.GRACE_MODE to GraceInputMode.ADD,
        EventParam.GRACE_TYPE to GraceType.APPOGGIATURA
      )
    )
    RVA("Tadpole", eagv(1, dZero(), dZero()).copy(id = 1))
  }

  @Test
  fun testAddGraceNoteWholeBarRemains() {
    SMV(
      extraParams = paramMapOf(
        EventParam.GRACE_MODE to GraceInputMode.ADD,
        EventParam.GRACE_TYPE to GraceType.APPOGGIATURA
      )
    )
    RVA("Rest", eav(1, dZero()))
  }

  @Test
  fun testAddGraceNoteAfter() {
    SMV(
      extraParams = paramMapOf(
        EventParam.GRACE_MODE to GraceInputMode.ADD,
        EventParam.GRACE_TYPE to GraceType.APPOGGIATURA
      )
    )
    SMV(
      eventAddress = eagv(1, dZero(), crotchet()), extraParams = paramMapOf(
        EventParam.GRACE_MODE to GraceInputMode.ADD,
        EventParam.GRACE_TYPE to GraceType.APPOGGIATURA
      )
    )
    RVA("Tadpole", eagv(1, dZero(), dZero()).copy(id = 1))
    RVA("Tadpole", eagv(1, dZero(), crotchet()).copy(id = 1))
  }

  @Test
  fun testAddGraceNoteSlashAdded() {
    SMV(
      extraParams = paramMapOf(
        EventParam.GRACE_MODE to GraceInputMode.ADD,
        EventParam.GRACE_TYPE to GraceType.ACCIACCATURA
      )
    )
    RVA("Slash", eagv(1, dZero(), dZero()))
  }

  @Test
  fun testAddGraceNotesBeamed() {
    grace()
    grace()
    RVA("Tadpole", eagv(1, dZero(), dZero()).copy(id = 1))
    RVA("Tadpole", eagv(1, dZero(), semiquaver()).copy(id = 1))
  }

  @Test
  fun testAddGraceNoteBeamedThenCrotchet() {
    grace()
    grace(semiquaver())
    grace(quaver(), duration = crotchet())
    RVA("Tadpole", eagv(1, dZero(), dZero()).copy(id = 1))
    RVA("Tadpole", eagv(1, dZero(), semiquaver()).copy(id = 1))
    RVA("Tadpole", eagv(1, dZero(), quaver()).copy(id = 1))
  }

  @Test
  fun testAddGraceNoteHasTail() {
    SMV(
      duration = semiquaver(),
      extraParams = paramMapOf(
        EventParam.GRACE_MODE to GraceInputMode.ADD,
        EventParam.GRACE_TYPE to GraceType.APPOGGIATURA
      )
    )
    RVA("Tail", eagv(1, dZero(), dZero()))
  }

  @Test
  fun testAddGraceNoteHasTailRestFollowing() {
    SMV(
      duration = semiquaver(),
      extraParams = paramMapOf(
        EventParam.GRACE_MODE to GraceInputMode.ADD,
        EventParam.GRACE_TYPE to GraceType.APPOGGIATURA
      )
    )
    SAE(
      rest(semiquaver()).addParams(
        paramMapOf(
          EventParam.GRACE_MODE to GraceInputMode.ADD,
          EventParam.GRACE_TYPE to GraceType.APPOGGIATURA
        )
      )
    )
    RVA("Tail", eagv(1, dZero(), dZero()))
  }
}