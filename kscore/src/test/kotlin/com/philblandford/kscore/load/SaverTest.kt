package com.philblandford.kscore.load

import TestInstrumentGetter
import assertEqual
import com.philblandford.kscore.api.PercussionDescr

import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.dsl.*
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.scorefunction.ScoreTest

import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.saveload.Loader
import com.philblandford.kscore.saveload.Saver
import compare
import compareLevel
import grace
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.*

class SaverTest : ScoreTest() {

  lateinit var saver: Saver
  lateinit var loader: Loader

  @Before
  override fun setup() {
    super.setup()
    saver = Saver()
    loader = Loader()
  }

  @Test
  fun testSaveEventHash() {
    val hash = eventHashOf(
      EventMapKey(EventType.CLEF, ea(1)) to Event(
        EventType.CLEF,
        paramMapOf(EventParam.TYPE to ClefType.TREBLE)
      )
    )
    val bytes = saver.saveEventHash(hash)
    val hash2 = loader.loadEventHash(LinkedList(bytes.toList()))
    assertEqual(hash.toMap(), hash2?.toMap())
  }

  @Test
  fun testSaveParamMap() {
    val map =
      paramMapOf(EventParam.TYPE to ClefType.TREBLE, EventParam.DURATION to DurationType.CHORD)
    val bytes = saver.saveParams(map)
    assertEqual(map.toMap(), loader.loadParams(LinkedList(bytes.toList()))?.toMap())
  }

  @Test
  fun testSaveParamMapWithNull() {
    val map =
      paramMapOf(EventParam.TYPE to null, EventParam.DURATION to DurationType.CHORD)
    val bytes = saver.saveParams(map)
    assertEqual(map.toMap(), loader.loadParams(LinkedList(bytes.toList()))?.toMap())
  }


  @Test
  fun testSaveEventAddress() {
    val bytes = saver.saveEventAddress(ea(1))
    assertEqual(ea(1), loader.loadEventAddress(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveEventAddressGrace() {
    val bytes = saver.saveEventAddress(eag(1))
    assertEqual(eag(1), loader.loadEventAddress(LinkedList(bytes.toList())))
  }


  @Test
  fun testSaveEventAddressGraceNotZero() {
    val bytes = saver.saveEventAddress(eag(1, dZero(), Offset(3, 16)))
    assertEqual(eag(1, dZero(), Offset(3, 16)), loader.loadEventAddress(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveDuration() {
    val bytes = saver.saveDuration(crotchet())
    assertEqual(crotchet(), loader.loadDuration(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveEventMapKey() {
    val emk = EventMapKey(EventType.CLEF, ea(1))
    val bytes = saver.saveEventMapKey(emk)
    assertEqual(emk, loader.loadEventMapKey(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveByte() {
    val bytes = saver.saveByte(78)
    assertEqual(78.toByte(), loader.loadByte(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveShort() {
    val bytes = saver.saveShort(1590)
    assertEqual(1590.toShort(), loader.loadShort(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveInt() {
    val bytes = saver.saveInt(34234)
    assertEqual(34234, loader.loadInt(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveString() {
    val bytes = saver.saveString("Hello")
    assertEqual("Hello", loader.loadString(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveClefType() {
    val bytes = saver.saveAny(ClefType.TREBLE)
    assertEqual(ClefType.TREBLE, loader.loadAny(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveChord() {
    val chord = dslChord(crotchet())
    val bytes = saver.saveParams(chord.params)
    assertEqual(chord.params.toList(), loader.loadParams(LinkedList(bytes.toList()))?.toList())
  }

  @Test
  fun testSavePitch() {
    val pitch = Pitch(NoteLetter.A, Accidental.NATURAL, 5, false)
    val bytes = saver.savePitch(pitch)
    assertEqual(pitch, loader.loadPitch(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveCoord() {
    val coord = Coord(23, 243)
    val bytes = saver.saveCoord(coord)
    assertEqual(coord, loader.loadCoord(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveIterable() {
    val list = listOf(1, 4, 54, 34234)
    val bytes = saver.saveIterable(list)
    assertEqual(list.toList(), loader.loadIterable<Int>(LinkedList(bytes.toList()))?.toList())
  }

  @Test
  fun testSaveMeta() {
    val meta = Meta()
    val bytes = saver.saveMeta(meta)
    assertEqual(meta, loader.loadMeta(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveMetaWithOffset() {
    val meta = Meta().setOffset(MetaType.TITLE, Coord(20, 20))
    val bytes = saver.saveMeta(meta)
    assertEqual(meta, loader.loadMeta(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveOrnamentClass() {
    val ornament = Ornament(OrnamentType.TRILL)
    val bytes = saver.saveOrnament(ornament)
    assertEqual(ornament, loader.loadOrnament(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveModifiable() {
    val modifiable = Modifiable(true, false)
    val bytes = saver.saveModifiable(modifiable)
    assertEqual(modifiable, loader.loadModifiable(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveChordDecoration() {
    val cd = ChordDecoration(true, listOf(ArticulationType.STACCATO))
    val bytes = saver.saveChordDecoration(cd)
    assertEqual(cd, loader.loadChordDecoration(LinkedList(bytes.toList())))
  }

  @Test
  fun testSaveVoiceMap() {
    val vm = dslVoiceMap(TimeSignature(4, 4), { ClefType.TREBLE }) {
      chord(crotchet())
      rest(minim())
    }
    val bytes = saver.saveVoiceMap(vm)
    val vm2 = loader.loadVoiceMap(LinkedList(bytes.toList()))!!
    assertEqual(vm.timeSignature, vm2.timeSignature)
    assertEqual(vm.getAllEvents(), vm2.getAllEvents())
  }

  @Test
  fun testSaveBar() {
    val bar = dslBar {
      voiceMap {
        chord(crotchet())
        rest(minim())
      }
    }
    val bytes = saver.saveBar(bar)
    assertEqual(bar.getAllEvents(), loader.loadBar(LinkedList(bytes.toList()))?.getAllEvents())
  }

  @Test
  fun testSaveStave() {
    val stave = dslStave({ TimeSignature(4, 4) }) {
      bar {
        voiceMap {
          chord(crotchet())
          rest(minim())
        }
      }
    }
    val bytes = saver.saveStave(stave)
    assertEqual(stave.getAllEvents(), loader.loadStave(LinkedList(bytes.toList()))?.getAllEvents())
  }

  @Test
  fun testSaveScore() {
    val score = blackSheep()
    val bytes = saver.createSaveScore(score)
    val newScore = loader.createScoreFromBytes(bytes)
    assertEqual(score.eventMap.getAllEvents(), newScore?.eventMap?.getAllEvents())
  }

  @Test
  fun testSaveQuickScore() {
    val score = Score.create(TestInstrumentGetter(), 32)
    val bytes = saver.createSaveScore(score)
    val newScore = loader.createScoreFromBytes(bytes)
    assertEqual(score.eventMap.getAllEvents(), newScore?.eventMap?.getAllEvents())
  }

  @Test
  fun testSaveQuickScoreAfterNoteAdd() {
    SCD()
    val bytes = saver.createSaveScore(SCORE())
    val newScore = loader.createScoreFromBytes(bytes)
    assertEqual(
      SCORE().eventMap.getAllEvents(),
      newScore?.eventMap?.getAllEvents()
    )
  }

  @Test
  fun testSavePercussionDescriptor() {
    val pd = PercussionDescr(4, 42, true, "Thing", NoteHeadType.NORMAL)
    val bytes = saver.savePercussionDesc(pd)
    val pd2 = loader.loadPercussionDesc(LinkedList(bytes.toList()))
    assertEqual(pd, pd2)
  }

  @Test
  fun testSavePercussionInstrument() {
    SCD(instruments = listOf("Bongos"))
    val bytes = saver.createSaveScore(SCORE())
    val newScore = loader.createScoreFromBytes(bytes)
    assertEqual(
      SCORE().eventMap.getAllEvents(),
      newScore?.eventMap?.getAllEvents()
    )
  }

  @Test
  fun testSaveBeams() {
    SCD()
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    doSave()
  }

  @Test
  fun testSaveBeamsDirectionsCorrect() {
    SCD()
    SMV(64, duration = quaver())
    SMV(72, duration = quaver(), eventAddress = eav(1, quaver()))
    doSave()
  }

  @Test
  fun testSaveBeamsDirectionsCorrectTransposeOption() {
    SCD(instruments = listOf("Trumpet"))
    listOf(71,72).withIndex().forEach { (idx, note) ->
      SMV(note, duration = quaver(), eventAddress = eav(1, quaver() * idx))
    }
    SSO(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT, true)
    doSave()
  }

  @Test
  fun testSaveOrnament() {
    SCD()
    SMV()
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    doSave()
  }

  @Test
  fun testSaveArticulation() {
    SCD()
    SMV()
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.STACCATO))
    doSave()
  }

  @Test
  fun testSaveBowing() {
    SCD()
    SMV()
    SAE(EventType.BOWING, eav(1), paramMapOf(EventParam.TYPE to BowingType.DOWN_BOW))
    doSave()
  }

  @Test
  fun testSaveArpeggio() {
    SCD()
    SMV()
    SAE(EventType.ARPEGGIO, eav(1))
    doSave()
  }

  @Test
  fun testSavePedal() {
    SCD()
    SMV()
    SAE(
      EventType.PEDAL,
      eav(1),
      paramMapOf(EventParam.TYPE to PedalType.LINE, EventParam.END to ea(2))
    )
    doSave()
  }

  @Test
  fun testSaveGlissando() {
    SCD()
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(
      EventType.GLISSANDO, ea(1), paramMapOf(
        EventParam.TYPE to GlissandoType.LINE,
        EventParam.END to ea(1, crotchet())
      )
    )
    doSave()
  }

  @Test
  fun testSaveWedge() {
    SCD()
    SMV()
    SAE(
      EventType.WEDGE,
      eav(1),
      paramMapOf(EventParam.TYPE to WedgeType.CRESCENDO, EventParam.END to ea(2))
    )
    doSave()
  }

  @Test
  fun testSaveBreak() {
    SCD()
    SMV()
    SAE(EventType.BREAK, eav(1), paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    doSave()
  }

  @Test
  fun testSaveTuplet() {
    SCD()
    SAE(tuplet(dZero(), 3, crotchet()).toEvent(), eav(1))
    doSave()
  }

  @Test
  fun testSaveTupletWithBeams() {
    SCD()
    SAE(tuplet(dZero(), 3, crotchet()).toEvent(), eav(1))
    repeat(3) {
      SMV(duration = quaver(), eventAddress = eav(1, Offset(1, 12).multiply(it)))
    }
    doSave()
  }

  @Test
  fun testSaveLyric() {
    SCD()
    SMV()
    SAE(
      EventType.LYRIC, eav(1), paramMapOf(
        EventParam.TEXT to "Thing",
        EventParam.TYPE to LyricType.START, EventParam.NUMBER to 1
      )
    )
    doSave()
  }

  @Test
  fun testSaveGraceNote() {
    SCD()
    grace()
    doSave()
  }

  @Test
  fun testSaveUserPosition() {
    SCD()
    SAE(
      EventType.WEDGE, ea(1), paramMapOf(
        EventParam.IS_UP to false,
        EventParam.DURATION to semibreve(),
        EventParam.TYPE to WedgeType.CRESCENDO,
        EventParam.HARD_START to Coord(10, 10)
      )
    )
    doSave()
  }

  @Test
  fun testSaveNoVersion() {
    SCD()
    val saver = Saver()
    val bytes = saver.createSaveScore(EG(), false)
    val newScore = loader.createScoreFromBytes(bytes)!!
    EG().compareLevel(newScore)
  }

  @Test
  fun testSaveLyricPosition() {
    SCD()
    SSO(EventParam.OPTION_LYRIC_POSITIONS, 1 to true)
    doSave()
  }

  @Test
  fun testSaveRepeatStart() {
    SCD()
    SAE(EventType.REPEAT_START, ea(2))
    doSave()
  }

  @Test
  fun testSaveRepeatEndStart() {
    SCD()
    SAE(EventType.REPEAT_END, ea(2))
    doSave()
  }

  @Test
  fun testSaveUserBeam() {
    SCD()
    repeat(4) {
      SMV(eventAddress = eav(1, quaver() * it))
    }
    SAE(
      EventType.BEAM, eav(1), paramMapOf(
        EventParam.TYPE to BeamType.JOIN, EventParam.END to eav(
          1,
          crotchet(1)
        )
      )
    )
    doSave()
  }

  @Test
  fun testConvertMetaToTitleEvent() {
    SCD()
    SAE(
      EventType.META, ez(0), paramMapOf(
        EventParam.SIMPLE to true,
        EventParam.SECTIONS to Meta().setText(MetaType.TITLE, "Title")
      )
    )
    val bytes = saver.createSaveScore(EG(), false)
    val newScore = loader.createScoreFromBytes(bytes)!!
    assertThat(newScore.getParam<String>(EventType.TITLE, EventParam.TEXT, eZero()), `is`("Title"))
  }

  @Test
  fun testConvertLyricOffsetToLyricOffsetByPosition() {
    SCD()
    SSO(EventParam.OPTION_LYRIC_OFFSET, Coord(0, 20))
    val bytes = saver.createSaveScore(EG(), false)
    val newScore = loader.createScoreFromBytes(bytes)!!
    assertThat(
      newScore.getOption(EventParam.OPTION_LYRIC_OFFSET_BY_POSITION), `is`(
        listOf(true to 0, false to 20)
      )
    )
  }

  private fun doSave() {
    val bytes = saver.createSaveScore(EG())
    val newScore = loader.createScoreFromBytes(bytes)!!
    EG().compare(newScore)
  }
}