package com.philblandford.kscore.engine.scorefunction

class UserBeamTest : ScoreTest() {

//  @Test
//  fun testAddUserBeam() {
//    repeat(5) { n ->
//      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
//    }
//    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
//    SVP(EventType.BEAM, EventParam.DURATION, minim(), eav(1))
//  }
//
//  @Test
//  fun testAddUserBeamGetBeams() {
//    repeat(5) { n ->
//      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
//    }
//    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
//    val beams = EG().getBeams().toList().filterNot { it.second.endMarker }
//    assertEqual(1, beams.size)
//    val beam = beams.first().second
//    assertEqual(minim(), beam.duration)
//    assertEqual(5, beam.members.count())
//  }
//
//  @Test
//  fun testAddUserBeamChordsMarked() {
//    repeat(5) { n ->
//      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
//    }
//    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
//    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1))
//  }
//
//  @Test
//  fun testAddUserBeamLastChordMarked() {
//    repeat(5) { n ->
//      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
//    }
//    SAE(EventType.BEAM, eav(1), paramMapOf(EventParam.END to eav(1, minim())))
//    SVP(EventType.DURATION, EventParam.IS_BEAMED, true, eav(1, minim()))
//  }
}