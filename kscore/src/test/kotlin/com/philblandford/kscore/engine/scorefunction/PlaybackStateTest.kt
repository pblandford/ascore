package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.types.*
import org.junit.Test

class PlaybackStateTest : ScoreTest() {

  @Test
  fun testSetVolume() {
    SSP(
      EventType.PLAYBACK_STATE, EventParam.VOLUME, 0.5f,
      eas(0, dZero(), StaveId(1, 0))
    )
    SVP(
      EventType.PLAYBACK_STATE, EventParam.VOLUME, 0.5f,
      eas(0, dZero(), StaveId(1, 0))
    )
  }

  @Test
  fun testSetVolumePartZero() {
    SSP(
      EventType.PLAYBACK_STATE, EventParam.VOLUME, 0.5f,
      eas(0, dZero(), sZero())
    )
    SVP(
      EventType.PLAYBACK_STATE, EventParam.VOLUME, 0.5f,
      eas(0, dZero(), sZero())
    )
  }

  @Test
  fun testSetVolumeSparesOthers() {
    SCD(instruments = listOf("Violin", "Viola"))
    SSP(
      EventType.PLAYBACK_STATE, EventParam.VOLUME, 100,
      eas(0, dZero(), StaveId(2, 0))
    )
    SSP(
      EventType.PLAYBACK_STATE, EventParam.VOLUME, 50,
      eas(0, dZero(), StaveId(1, 0))
    )
    SVP(
      EventType.PLAYBACK_STATE, EventParam.VOLUME, 50,
      eas(0, dZero(), StaveId(1, 0))
    )
    SVP(
      EventType.PLAYBACK_STATE, EventParam.VOLUME, 100,
      eas(0, dZero(), StaveId(2, 0))
    )
  }

  @Test
  fun testSetMute() {
    SSP(
      EventType.PLAYBACK_STATE, EventParam.MUTE, true,
      eas(0, dZero(), StaveId(1, 0))
    )
    SVP(
      EventType.PLAYBACK_STATE, EventParam.MUTE, true,
      eas(0, dZero(), StaveId(1, 0))
    )
  }

  @Test
  fun testSetMutePartZero() {
    SSP(
      EventType.PLAYBACK_STATE, EventParam.MUTE, true,
      eas(0, dZero(), sZero())
    )
    SVP(
      EventType.PLAYBACK_STATE, EventParam.MUTE, true,
      eas(0, dZero(), sZero())
    )
  }

  @Test
  fun testSetSolo() {
    SSP(
      EventType.PLAYBACK_STATE, EventParam.SOLO, true,
      eas(0, dZero(), StaveId(1, 0))
    )
    SVP(
      EventType.PLAYBACK_STATE, EventParam.SOLO, true,
      eas(0, dZero(), StaveId(1, 0))
    )
  }

  @Test
  fun testSetSoloPartZero() {
    SSP(
      EventType.PLAYBACK_STATE, EventParam.SOLO, true,
      eas(0, dZero(), sZero())
    )
    SVP(
      EventType.PLAYBACK_STATE, EventParam.SOLO, true,
      eas(0, dZero(), sZero())
    )
  }

  @Test
  fun testSetSoloMutesOthers() {
    SCD(instruments = listOf("Violin", "Viola"))
    SSP(
      EventType.PLAYBACK_STATE, EventParam.SOLO, true,
      eas(0, dZero(), StaveId(1, 0))
    )
    SVP(
      EventType.PLAYBACK_STATE, EventParam.MUTE, true,
      eas(0, dZero(), StaveId(2, 0))
    )
  }

  @Test
  fun testUnsetSoloUnmutesOthers() {
    SCD(instruments = listOf("Violin", "Viola"))
    SSP(
      EventType.PLAYBACK_STATE, EventParam.SOLO, true,
      eas(0, dZero(), StaveId(1, 0))
    )
    SSP(
      EventType.PLAYBACK_STATE, EventParam.SOLO, false,
      eas(0, dZero(), StaveId(1, 0))
    )
    SVP(
      EventType.PLAYBACK_STATE, EventParam.MUTE, false,
      eas(0, dZero(), StaveId(2, 0))
    )
  }

  @Test
  fun testUnsetSoloNotMutePart() {
    SCD(instruments = listOf("Violin", "Viola"))
    SSP(
      EventType.PLAYBACK_STATE, EventParam.SOLO, true,
      eas(0, dZero(), StaveId(1, 0))
    )
    SSP(
      EventType.PLAYBACK_STATE, EventParam.SOLO, false,
      eas(0, dZero(), StaveId(1, 0))
    )
    SVP(
      EventType.PLAYBACK_STATE, EventParam.MUTE, false,
      eas(0, dZero(), StaveId(1, 0))
    )
  }

  @Test
  fun testSetMuteUnsetsSolo() {
    SSP(
      EventType.PLAYBACK_STATE, EventParam.SOLO, true,
      eas(0, dZero(), StaveId(1, 0))
    )
    SSP(
      EventType.PLAYBACK_STATE, EventParam.MUTE, true,
      eas(0, dZero(), StaveId(1, 0))
    )
    SVP(
      EventType.PLAYBACK_STATE, EventParam.SOLO, false,
      eas(0, dZero(), StaveId(1, 0))
    )
  }

  @Test
  fun testSetMuteRetainsVolume() {
    SSP(
      EventType.PLAYBACK_STATE, EventParam.VOLUME, 50,
      eas(0, dZero(), StaveId(1, 0))
    )
    SSP(
      EventType.PLAYBACK_STATE, EventParam.MUTE, true,
      eas(0, dZero(), StaveId(1, 0))
    )
    SVP(
      EventType.PLAYBACK_STATE, EventParam.VOLUME, 50,
      eas(0, dZero(), StaveId(1, 0))
    )
  }

  @Test
  fun testSetMuteLeavesOthers() {
    SCD(instruments = listOf("Violin", "Viola"))
    SSP(
      EventType.PLAYBACK_STATE, EventParam.MUTE, true,
      eas(0, dZero(), StaveId(1, 0))
    )
    assert(
      EG().getParam<Boolean>(
        EventType.PLAYBACK_STATE,
        EventParam.MUTE,
        eas(0, dZero(), StaveId(2, 0))
      ) != true
    )
  }

}