package com.philblandford.kscore.sound

import assertEqual
import com.philblandford.kscore.engine.types.ClefType
import com.philblandford.kscore.engine.types.NoteHeadType
import org.junit.Before
import org.junit.Test

class SoundManagerTest {
//
//  @Before
//  fun setup() {
//    Sound.registerSoundManager(TestSoundManager)
//  }
//
//  @Test
//  fun testLoadInstruments() {
//    val groups = Sound.getInstrumentGroupNames()
//    assertEqual(
//      listOf(
//        "Piano", "Chromatic Percussion", "Organ", "Guitar",
//        "Bass", "Strings", "Ensemble", "Brass", "Reed", "Pipe", "Synth Lead", "Synth Pad", "Percussion"
//      ).toList(), groups.toList()
//    )
//  }
//
//  @Test
//  fun testLoadInstrument() {
//    val violin = Sound.getInstrument("Violin")
//    assertEqual(41, violin?.program)
//    assertEqual(listOf(ClefType.TREBLE).toList(), violin?.clefs?.toList())
//  }
//
//  @Test
//  fun testLoadPercussionInstrument() {
//    val drum = Sound.getInstrument("Bass Drum 1")
//    assertEqual(listOf(ClefType.PERCUSSION).toList(), drum?.clefs?.toList())
//    assertEqual(
//      listOf(
//        PercussionDescr(4, 35, false, "Bass Drum 1", NoteHeadType.NORMAL)
//      ).toList(), drum?.percussionDescrs
//    )
//    assertEqual(1, drum?.staveLines)
//  }
//
//  @Test
//  fun testLoadPercussionKit() {
//    val drum = Sound.getInstrument("Kit")
//    assertEqual(listOf(ClefType.PERCUSSION).toList(), drum?.clefs?.toList())
//    assertEqual(
//      PercussionDescr(-2, 49, true, "Crash Cymbal", NoteHeadType.CROSS)
//      , drum?.percussionDescrs?.first()
//    )
//  }
//
//  @Test
//  fun testLoadPercussionBongos() {
//    val drum = Sound.getInstrument("Bongos")
//    assertEqual(listOf(ClefType.PERCUSSION).toList(), drum?.clefs?.toList())
//    assertEqual(2, drum?.percussionDescrs?.count())
//    assertEqual(2, drum?.staveLines)
//  }
}