package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.types.*

import org.junit.Test

class LyricTest : ScoreTest() {

  @Test
  fun testAddLyric() {
    SMV()
    SAE(
      EventType.LYRIC,
      eav(1),
      params = paramMapOf(EventParam.TEXT to "Flob", EventParam.NUMBER to 1)
    )
    SVP(EventType.LYRIC, EventParam.TEXT, "Flob", eav(1).copy(id = 1))
  }

  @Test
  fun testAddNoteLyricRemains() {
    SMV()
    SAE(
      EventType.LYRIC,
      eav(1),
      params = paramMapOf(EventParam.TEXT to "Flob", EventParam.NUMBER to 1)
    )
    SMV()
    SVP(EventType.LYRIC, EventParam.TEXT, "Flob", eav(1).copy(id = 1))
  }

  @Test
  fun testDeleteLyric() {
    SMV()
    SAE(
      EventType.LYRIC,
      eav(1),
      params = paramMapOf(EventParam.TEXT to "Flob", EventParam.NUMBER to 1)
    )
    SDE(EventType.LYRIC, eav(1).copy(id = 1))
    SVNE(EventType.LYRIC, eav(1).copy(id = 1))
  }

  @Test
  fun testDeleteLyricSecondLine() {
    SMV()
    SAE(
      EventType.LYRIC,
      eav(1),
      params = paramMapOf(EventParam.TEXT to "Flob", EventParam.NUMBER to 2)
    )
    SDE(EventType.LYRIC, eav(1).copy(id = 2))
    SVNE(EventType.LYRIC, eav(1).copy(id = 2))
  }

  @Test
  fun testDeleteLyricSecondLineFirstLineUntouched() {
    SMV()
    SAE(
      EventType.LYRIC,
      eav(1),
      params = paramMapOf(EventParam.TEXT to "One", EventParam.NUMBER to 1)
    )
    SAE(
      EventType.LYRIC,
      eav(1),
      params = paramMapOf(EventParam.TEXT to "Two", EventParam.NUMBER to 2)
    )
    SDE(EventType.LYRIC, ea(1).copy(id = 2))
    SVP(EventType.LYRIC, EventParam.TEXT, "One", eav(1).copy(id = 1))
    SVNE(EventType.LYRIC, ea(1).copy(id = 2))
  }

  @Test
  fun testDeleteLyricVoiceIgnored() {
    SMV()
    SAE(
      EventType.LYRIC,
      eav(1),
      params = paramMapOf(EventParam.TEXT to "Flob", EventParam.NUMBER to 1)
    )
    SDE(EventType.LYRIC, eav(1, voice = 2).copy(id = 1))
    SVNE(EventType.LYRIC, eav(1).copy(id = 1))
  }

  @Test
  fun testDeleteLyricRange() {
    SMV()
    SAE(EventType.LYRIC, eav(1), params = paramMapOf(EventParam.TEXT to "Flob", EventParam.NUMBER to 1))
    SDE(EventType.LYRIC, ea(1), endAddress = ea(2))
    SVNE(EventType.LYRIC, eav(1).copy(id = 1))
  }

  @Test
  fun testAddLyricPosition() {
    SAE(EventType.LYRIC, eav(1), params = paramMapOf(EventParam.TEXT to "Flob", EventParam.NUMBER to 1))
    SSO(EventParam.OPTION_LYRIC_POSITIONS, 1 to true)
    val map = EG().getOption<List<Pair<Int, Boolean>>>(EventParam.OPTION_LYRIC_POSITIONS)
    assertEqual(mapOf(1 to true).toList(), map)
  }

  @Test
  fun testToggleLyricPosition() {
    SAE(EventType.LYRIC, eav(1), params = paramMapOf(EventParam.TEXT to "Flob", EventParam.NUMBER to 1))
    SSO(EventParam.OPTION_LYRIC_POSITIONS, 1 to true)
    SSO(EventParam.OPTION_LYRIC_POSITIONS, 1 to null)
    val map = EG().getOption<List<Pair<Int, Boolean>>>(EventParam.OPTION_LYRIC_POSITIONS)
    assertEqual(mapOf(1 to false).toList(), map?.toList())
  }

  @Test
  fun testSetLyricOffsetGlobal() {
    SAE(EventType.LYRIC, eav(1), params = paramMapOf(EventParam.TEXT to "Flob", EventParam.NUMBER to 1))
    SSO(EventParam.OPTION_LYRIC_OFFSET, Coord(0, 20))
    SVO(EventParam.OPTION_LYRIC_OFFSET, Coord(0,20))
  }

  @Test
  fun testSetLyricOffsetAbove() {
    SAE(EventType.LYRIC, eav(1), params = paramMapOf(EventParam.TEXT to "Flob", EventParam.NUMBER to 1))
    SSO(EventParam.OPTION_LYRIC_OFFSET_BY_POSITION, true to 20)
    SVO(EventParam.OPTION_LYRIC_OFFSET_BY_POSITION, listOf(true to 20))
  }

  @Test
  fun testSetLyricOffsetAboveExistingMap() {
    SAE(EventType.LYRIC, eav(1), params = paramMapOf(EventParam.TEXT to "Flob", EventParam.NUMBER to 1))
    SSO(EventParam.OPTION_LYRIC_OFFSET_BY_POSITION, true to 20)
    SSO(EventParam.OPTION_LYRIC_OFFSET_BY_POSITION, false to 30)
    SVO(EventParam.OPTION_LYRIC_OFFSET_BY_POSITION, listOf(true to 20, false to 30)) { toList() }
  }

}