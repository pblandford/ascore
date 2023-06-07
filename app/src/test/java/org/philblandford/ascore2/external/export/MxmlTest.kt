package org.philblandford.ascore2.external.export


import com.philblandford.ascore.external.export.mxml.`in`.converter.getDenominator
import org.philblandford.ascore2.external.export.mxml.`in`.reader.scoreFromMxml
import com.philblandford.ascore.external.export.mxml.out.creator.RepeatBarDesc
import com.philblandford.ascore.external.export.mxml.out.creator.measure.createDivisions
import com.philblandford.ascore.external.export.mxml.out.creator.repeatBarQuery
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.clipboard.Clipboard
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.COMPOSER_TEXT_SIZE
import com.philblandford.kscore.engine.core.representation.SUBTITLE_TEXT_SIZE
import com.philblandford.kscore.engine.core.representation.TITLE_TEXT_SIZE
import com.philblandford.kscore.engine.core.representation.scoreToRepresentation
import com.philblandford.kscore.engine.core.score.Meta
import com.philblandford.kscore.engine.core.score.MetaSection
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.breve
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.duration.quaver
import com.philblandford.kscore.engine.duration.semibreve
import com.philblandford.kscore.engine.duration.semiquaver
import com.philblandford.kscore.engine.duration.times
import com.philblandford.kscore.engine.eventadder.rightOrThrow
import com.philblandford.kscore.engine.pitch.KeySignature
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.Accidental
import com.philblandford.kscore.engine.types.ArticulationType
import com.philblandford.kscore.engine.types.BarLineType
import com.philblandford.kscore.engine.types.BreakType
import com.philblandford.kscore.engine.types.ClefType
import com.philblandford.kscore.engine.types.DynamicType
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.FermataType
import com.philblandford.kscore.engine.types.LyricType
import com.philblandford.kscore.engine.types.MetaType
import com.philblandford.kscore.engine.types.NoteHeadType
import com.philblandford.kscore.engine.types.OrnamentType
import com.philblandford.kscore.engine.types.PedalType
import com.philblandford.kscore.engine.types.StaveId
import com.philblandford.kscore.engine.types.WedgeType
import com.philblandford.kscore.engine.types.ea
import com.philblandford.kscore.engine.types.eagv
import com.philblandford.kscore.engine.types.eas
import com.philblandford.kscore.engine.types.eav
import com.philblandford.kscore.engine.types.ez
import com.philblandford.kscore.engine.types.paramMapOf
import org.apache.commons.io.IOUtils
import org.junit.Test
import org.philblandford.ascore2.external.export.mxml.out.createMxml
import org.philblandford.ascore2.external.export.xml.Attribute
import org.philblandford.ascore2.external.export.xml.Child
import org.philblandford.ascore2.external.export.xml.KxmlBase
import org.philblandford.ascore2.external.export.xml.Text
import org.philblandford.ascore2.external.export.xml.write
import java.io.File
import java.nio.charset.Charset

class MxmlTest : ScoreTest() {

  internal data class MyClass(@Text val text: String) : KxmlBase()
  internal data class MyClassInt(@Text val text: Int) : KxmlBase()
  internal data class MyClassAttr(@Attribute val attr: String) : KxmlBase()
  internal data class MyClassAttrHyphen(@Attribute val attrHyphen: String) : KxmlBase()
  internal data class MyClassAttrOpt(@Attribute val attr: String? = null) : KxmlBase()
  internal data class MyClassChild(@Child val child: MyClass) : KxmlBase()
  internal data class MyClassChildOpt(@Child val child: MyClass? = null) : KxmlBase()
  internal data class MyClassChildren(@Child val children: Iterable<MyClass>) : KxmlBase()

  private var dtdDir: File? = null

  @Override
  override fun setup() {
    super.setup()
    val url = this.javaClass.getResource("musicxml-3.1")
    url?.let {
      dtdDir = File(it.toURI())
    }
  }

  @Test
  fun testWriteMxmlBase() {
    val textMx = MyClass("wibble")
    val xml = textMx.write(StringBuilder())
    assertEqual("<my-class>wibble</my-class>\n", xml)
  }

  @Test
  fun testWriteMxmlBaseTextInt() {
    val textMx = MyClassInt(3)
    val xml = textMx.write(StringBuilder())
    assertEqual("<my-class-int>3</my-class-int>\n", xml)
  }

  @Test
  fun testWriteMxmlBaseAttribute() {
    val textMx = MyClassAttr("wibble")
    val xml = textMx.write(StringBuilder())
    assertEqual("<my-class-attr attr=\"wibble\"/>\n", xml)
  }

  @Test
  fun testWriteMxmlBaseAttributeHyphen() {
    val textMx = MyClassAttrHyphen("wibble")
    val xml = textMx.write(StringBuilder())
    assertEqual("<my-class-attr-hyphen attr-hyphen=\"wibble\"/>\n", xml)
  }


  @Test
  fun testWriteMxmlBaseAttributeOpt() {
    val textMx = MyClassAttrOpt()
    val xml = textMx.write(StringBuilder())
    assertEqual("<my-class-attr-opt/>\n", xml)
  }

  @Test
  fun testWriteMxmlBaseChild() {
    val textMx = MyClassChild(MyClass("wibble"))
    val xml = textMx.write(StringBuilder())
    assertEqual("<my-class-child>\n\t<my-class>wibble</my-class>\n</my-class-child>\n", xml)
  }

  @Test
  fun testWriteMxmlBaseChildOpt() {
    val textMx = MyClassChildOpt()
    val xml = textMx.write(StringBuilder())
    assertEqual("<my-class-child-opt/>\n", xml)
  }

  @Test
  fun testWriteMxmlBaseChildren() {
    val textMx = MyClassChildren(listOf(MyClass("wibble"), MyClass("wobble")))
    val xml = textMx.write(StringBuilder())
    assertEqual(
      "<my-class-children>\n\t<my-class>wibble</my-class>\n\t<my-class>wobble</my-class>\n</my-class-children>\n",
      xml
    )
  }

  @Test
  fun testCreateDivisions() {
    assertEqual(1, createDivisions(listOf(crotchet())))
  }

  @Test
  fun testCreateDivisionsQuaver() {
    assertEqual(2, createDivisions(listOf(quaver())))
  }

  @Test
  fun testCreateDivisionsQuaverAndTuplet() {
    assertEqual(6, createDivisions(listOf(quaver(), Duration(1, 6))))
  }


  @Test
  fun testCreateDivisionsQuaverCrotchet() {
    assertEqual(2, createDivisions(listOf(quaver(), crotchet())))
  }

  @Test
  fun testCreateDivisionsTuplet() {
    assertEqual(3, createDivisions(listOf(crotchet(), Duration(1, 6))))
  }

  @Test
  fun testWrite() {
    doMxml(EG())
  }

  @Test
  fun testTitle() {
    SAE(
      Meta(
        mapOf(
          MetaType.TITLE to MetaSection("Title", TITLE_TEXT_SIZE),
          MetaType.SUBTITLE to MetaSection("Subtitle", SUBTITLE_TEXT_SIZE),
          MetaType.COMPOSER to MetaSection("Composer", COMPOSER_TEXT_SIZE),
          MetaType.LYRICIST to MetaSection("", COMPOSER_TEXT_SIZE)
        )
      ).toEvent(), ez(0)
    )
    doMxml(EG())
  }

  @Test
  fun testPartLabel() {
    SSP(EventType.PART, EventParam.LABEL, "Wibble", ea(1))
    doMxml(EG())
  }

  @Test
  fun testInstrumentNonDefaultName() {
    SAE(
      EventType.PART, ez(1).copy(staveId = StaveId(1, 0)),
      Instrument(
        "Violoncello", "Violoncello", "Strings",
        43, 0, listOf(ClefType.TREBLE), "default", 0
      ).toEvent().params
    )
    doMxml(EG())
  }

  @Test
  fun testWriteChord() {
    SMV()
    doMxml(EG())
  }

  @Test
  fun testWriteChordTwoNotes() {
    SMV()
    SMV(60)
    doMxml(EG())
  }

  @Test
  fun testWriteTwoChords() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    doMxml(EG())
  }

  @Test
  fun testWriteChordClustered() {
    SMV()
    SMV(70)
    doMxml(EG())
  }

  @Test
  fun testWriteChordClusteredBeamed() {
    SMV(duration = quaver())
    SMV(70, duration = quaver())
    SMV(70, duration = quaver(), eventAddress = eav(1, quaver()))
    doMxml(EG())
  }

  @Test
  fun testWriteChordClusteredBeamedUpstem() {
    SMV(62, duration = quaver())
    SMV(60, duration = quaver())
    SMV(62, duration = quaver(), eventAddress = eav(1, quaver()))
    doMxml(EG())
  }


  @Test
  fun testWriteChordClusteredBeamedMixedStem() {
    SMV(72, duration = quaver())
    SMV(70, duration = quaver())
    SMV(60, duration = quaver(), eventAddress = eav(1, quaver()))
    doMxml(EG())
  }


  @Test
  fun testWriteChordTwoStaves() {
    SCD(instruments = listOf("Piano"))
    doMxml(EG())
  }

  @Test
  fun testWriteChordTwoStavesEmptyBarInOne() {
    SCD(instruments = listOf("Piano"))
    repeat(4) { SDE(EventType.DURATION, eav(1, crotchet().multiply(it))) }
    doMxml(EG())
  }

  @Test
  fun testWriteChordDotted() {
    SMV(duration = crotchet(1))
    doMxml(EG())
  }

  @Test
  fun testWriteChordDoubleDotted() {
    SMV(duration = crotchet(2))
    doMxml(EG())
  }

  @Test
  fun testWriteRest() {
    SAE(rest())
    doMxml(EG())
  }

  @Test
  fun testWriteRestDotted() {
    SAE(rest(crotchet(1)))
    doMxml(EG())
  }

  @Test
  fun testWriteRestDoubleDotted() {
    SAE(rest(crotchet(2)))
    doMxml(EG())
  }

  @Test
  fun testWriteChordTwoVoice() {
    SMV()
    SMV(eventAddress = eav(1, dZero(), 2))
    doMxml(EG())
  }

  @Test
  fun testWriteChordCFlat() {
    SMV(71, accidental = Accidental.FORCE_FLAT)
    doMxml(EG())
  }

  @Test
  fun testWriteQuaver() {
    SMV(duration = quaver())
    doMxml(EG())
  }

  @Test
  fun testWriteTwoQuavers() {
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    doMxml(EG())
  }

  @Test
  fun testWriteEightQuavers() {
    repeat(8) {
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(it)))
    }
    doMxml(EG())
  }

  @Test
  fun testWriteEightQuaversTwoStaves() {
    SCDG()
    repeat(2) { staff ->
      repeat(8) {
        SMV(
          duration = quaver(),
          eventAddress = eav(1, quaver().multiply(it)).copy(staveId = StaveId(1, staff + 1))
        )
      }
    }
    doMxml(EG())
  }


  @Test
  fun testWriteQuaversUpstem() {
    repeat(8) { n ->
      SMV(midiVal = 60, duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    doMxml(EG())
  }


  @Test
  fun testWriteQuavers6_8() {
    SCD(TimeSignature(6, 8))
    repeat(6) { n ->
      SMV(duration = quaver(), eventAddress = eav(1, quaver().multiply(n)))
    }
    doMxml(EG())
  }

  @Test
  fun testWriteSemiQuavers6_8() {
    SCD(TimeSignature(6, 8))
    repeat(12) { n ->
      SMV(duration = semiquaver(), eventAddress = eav(1, semiquaver().multiply(n)))
    }
    doMxml(EG())
  }

  @Test
  fun testWriteSemiQuavers12_8() {
    SCD(TimeSignature(12, 8))
    repeat(24) { n ->
      SMV(duration = semiquaver(), eventAddress = eav(1, semiquaver().multiply(n)))
    }
    doMxml(EG())
  }


  @Test
  fun testWriteThreeQuaversAfterRest() {
    SAE(rest(quaver()), eav(1))
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    SMV(duration = quaver(), eventAddress = eav(1, crotchet()))
    SMV(duration = quaver(), eventAddress = eav(1, crotchet(1)))
    doMxml(EG())
  }

  @Test
  fun testWriteChordTwoVoiceIncompleteBackup() {
    SMV(duration = semibreve())
    SMV(eventAddress = eav(1, minim(), 2))
    SDE(EventType.DURATION, eav(1, dZero(), 2))
    doMxml(EG())
  }

  @Test
  fun testWriteChordForward() {
    SMV(eventAddress = eav(1, crotchet()))
    SDE(EventType.DURATION, eav(1))
    doMxml(EG())
  }

  @Test
  fun testWriteNotePercussion() {
    SCD(instruments = listOf("Bass Drum 1"))
    SMV(35)
    doMxml(EG())
  }

  @Test
  fun testWriteNotePercusNsionCrossHead() {
    SCD(instruments = listOf("Open Hi-hat"))
    SMV(46)
    doMxml(EG())
  }

  @Test
  fun testWriteChordPercussion() {
    SCD(instruments = listOf("Bongos"))
    SMV(60)
    SMV(61)
    doMxml(EG())
  }


  @Test
  fun testWriteTupletPercussion() {
    SCD(instruments = listOf("Bongos"))
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 8, EventParam.HIDDEN to false
      )
    )
    SMV(60, duration = quaver())
    SMV(61, eventAddress = eav(1, Offset(1, 12)))
    doMxml(EG())
  }

  @Test
  fun testWriteTupletCrotchetsPercussion() {
    SCD(instruments = listOf("Hand Clap"))
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 4, EventParam.HIDDEN to false
      )
    )
    SMV(39)
    SMV(39, eventAddress = eav(1, Offset(1, 6)))
    SMV(39, eventAddress = eav(1, Offset(1, 3)))
    doMxml(EG())
  }

  @Test
  fun testWriteChordNoteHead() {
    SMV(extraParams = paramMapOf(EventParam.NOTE_HEAD_TYPE to NoteHeadType.CROSS))
    doMxml(EG())
  }

  @Test
  fun testWriteTie() {
    SMV(duration = breve())
    doMxml(EG())
  }

  @Test
  fun testWriteBar2() {
    SMV(eventAddress = eav(2))
    doMxml(EG())
  }

  @Test
  fun testWriteKeySignature() {
    val score = Score.create(TestInstrumentGetter(),1, ks = 2)
    doMxml(score)
  }

  @Test
  fun testWriteKeySignatureDFlat() {
    val score = Score.create(TestInstrumentGetter(), 1, ks = -5)
    doMxml(score)
  }

  @Test
  fun testWriteKeySignatureWithNote() {
    SCD(ks = 2)
    SMV(74)
    doMxml(EG())
  }

  @Test
  fun testWriteKeySignatureAfterNote() {
    SCD(ks = 2)
    SMV(74)
    SAE(KeySignature(4).toEvent(), ez(3))
    SMV(74, eventAddress = eav(3))
    doMxml(EG())
  }

  @Test
  fun testWriteKeySignatureWithPercussion() {
    SCD(ks = 5, instruments = listOf("Violin", "Bass Drum 1"))
    SMV(35, eventAddress = eav(1).copy(staveId = StaveId(2, 1)))
    doMxml(EG())
  }

  @Test
  fun testWriteArticulation() {
    SMV()
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.STACCATO))
    doMxml(EG())
  }

  @Test
  fun testWriteTwoArticulations() {
    SMV()
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.STACCATO))
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.ACCENT))
    doMxml(EG())
  }

  @Test
  fun testWriteOrnament() {
    SMV()
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    doMxml(EG())
  }

  @Test
  fun testWriteOrnamentThirdBeat() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SMV(eventAddress = eav(1, minim()))
    SAE(EventType.ORNAMENT, eav(1, minim()), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    doMxml(EG())
  }

  @Test
  fun testWriteOrnamentThirdBeatFourthBeatPresent() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SMV(eventAddress = eav(1, minim()))
    SMV(eventAddress = eav(1, minim(1)))
    SAE(EventType.ORNAMENT, eav(1, minim()), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    doMxml(EG())
  }

  @Test
  fun testWriteOrnamentFourthBeat() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SMV(eventAddress = eav(1, minim()))
    SMV(eventAddress = eav(1, minim(1)))
    SAE(EventType.ORNAMENT, eav(1, minim(1)), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    doMxml(EG())
  }

  @Test
  fun testWriteOrnamentAccidentalAbove() {
    SMV()
    SAE(
      EventType.ORNAMENT, eav(1), paramMapOf(
        EventParam.TYPE to OrnamentType.TRILL,
        EventParam.ACCIDENTAL_ABOVE to Accidental.FLAT
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteOrnamentAccidentalBelow() {
    SMV()
    SAE(
      EventType.ORNAMENT, eav(1), paramMapOf(
        EventParam.TYPE to OrnamentType.TRILL,
        EventParam.ACCIDENTAL_BELOW to Accidental.FLAT
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteOrnamentAccidentalAboveBelow() {
    SMV()
    SAE(
      EventType.ORNAMENT, eav(1), paramMapOf(
        EventParam.TYPE to OrnamentType.TRILL,
        EventParam.ACCIDENTAL_ABOVE to Accidental.FLAT,
        EventParam.ACCIDENTAL_BELOW to Accidental.SHARP
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteArpeggio() {
    SMV()
    SAE(EventType.ARPEGGIO, eav(1))
    doMxml(EG())
  }

  @Test
  fun testWriteFingering() {
    SMV()
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.NUMBER to 1))
    doMxml(EG())
  }

  @Test
  fun testWriteTwoFingerings() {
    SMV()
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.NUMBER to 1))
    SAE(EventType.FINGERING, eav(1), paramMapOf(EventParam.NUMBER to 3))
    doMxml(EG())
  }

  @Test
  fun testWriteFermata() {
    SAE(EventType.FERMATA, ez(1), paramMapOf(EventParam.TYPE to FermataType.NORMAL))
    doMxml(EG())
  }

  @Test
  fun testWriteFermataSquare() {
    SAE(EventType.FERMATA, ez(1), paramMapOf(EventParam.TYPE to FermataType.SQUARE))
    doMxml(EG())
  }

  @Test
  fun testWriteFermataTriangle() {
    SAE(EventType.FERMATA, ez(1), paramMapOf(EventParam.TYPE to FermataType.TRIANGLE))
    doMxml(EG())
  }

  @Test
  fun testWriteFermataMultipleParts() {
    SCD(instruments = listOf("Violin", "Viola"))
    SAE(EventType.FERMATA, ez(1), paramMapOf(EventParam.TYPE to FermataType.NORMAL))
    doMxml(EG())
  }

  @Test
  fun testWriteAccidental() {
    SMV(73)
    doMxml(EG())
  }

  @Test
  fun testWriteChordAcrossOctave() {
    SMV(62)
    SMV(59)
    doMxml(EG())
  }

  @Test
  fun testWriteAccidentalChord() {
    SMV(64)
    SMV(61, accidental = Accidental.FLAT)
    SMV(58, accidental = Accidental.FLAT)
    SMV(55)
    doMxml(EG())
  }

  @Test
  fun testWriteAccidentalTwoParts() {
    SCD(instruments = listOf("Violin", "Violin"))
    SMV(73)
    SMV(eventAddress = eas(1, dZero(), StaveId(2, 1)))
    doMxml(EG())
  }

  @Test
  fun testWriteClef() {
    SMV(72)
    SAE(EventType.CLEF, ea(1), paramMapOf(EventParam.TYPE to ClefType.BASS))
    doMxml(EG())
  }

  @Test
  fun testWriteClefNotFirstBar() {
    SMV(72)
    SAE(EventType.CLEF, ea(2), paramMapOf(EventParam.TYPE to ClefType.BASS))
    doMxml(EG())
  }

  @Test
  fun testWriteClefMidBar() {
    SMV()
    SAE(EventType.CLEF, ea(1, minim()), paramMapOf(EventParam.TYPE to ClefType.BASS))
    SMV(eventAddress = eav(1, minim()))
    doMxml(EG())
  }

  @Test
  fun testWriteRehearsal() {
    SAE(EventType.REHEARSAL_MARK, ez(1), paramMapOf(EventParam.TEXT to "A"))
    doMxml(EG())
  }

  @Test
  fun testWriteExpressionText() {
    SAE(
      EventType.EXPRESSION_TEXT, ea(1), paramMapOf(
        EventParam.TEXT to "crescendo",
        EventParam.IS_UP to false
      )
    )
    doMxml(EG())
  }


  @Test
  fun testWriteExpressionTextPizz() {
    SAE(
      EventType.EXPRESSION_TEXT, ea(2), paramMapOf(
        EventParam.TEXT to "pizz",
        EventParam.IS_UP to false
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteExpressionDash() {
    SAE(
      EventType.EXPRESSION_DASH, ea(1),
      paramMapOf(EventParam.TEXT to "crescendo", EventParam.IS_UP to true, EventParam.END to ea(2))
    )
    doMxml(EG())
  }

  @Test
  fun testWriteTempoText() {
    SAE(EventType.TEMPO_TEXT, ea(1), paramMapOf(EventParam.TEXT to "Allegro"))
    doMxml(EG())
  }

  @Test
  fun testWriteBarLine() {
    SAE(EventType.BARLINE, ez(3), paramMapOf(EventParam.TYPE to BarLineType.DOUBLE))
    doMxml(EG())
  }

  @Test
  fun testWriteRepeatBarline() {
    SAE(EventType.REPEAT_START, ez(2))
    doMxml(EG())
  }

  @Test
  fun testWriteRepeatBarlineEnd() {
    SAE(EventType.REPEAT_END, ez(2))
    doMxml(EG())
  }

  @Test
  fun testWriteRepeatBar() {
    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    doMxml(EG())
  }

  @Test
  fun testWriteTwoRepeatBars() {
    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    SAE(EventType.REPEAT_BAR, ea(3), paramMapOf(EventParam.NUMBER to 1))
    doMxml(EG())
  }

  @Test
  fun testWrite10RepeatBars() {
    repeat(10) {
      SAE(EventType.REPEAT_BAR, ea(2 + it), paramMapOf(EventParam.NUMBER to 1))
    }
    doMxml(EG())
  }

  @Test
  fun testWriteRepeatBarTwoBars() {
    SAE(EventType.REPEAT_BAR, ea(3), paramMapOf(EventParam.NUMBER to 2))
    doMxml(EG())
  }


  @Test
  fun testWriteVolta() {
    SAE(EventType.VOLTA, ez(1), paramMapOf(EventParam.NUMBER to 1, EventParam.END to ez(1)))
    doMxml(EG())
  }

  @Test
  fun testWriteVoltaTwoBars() {
    SAE(EventType.VOLTA, ez(1), paramMapOf(EventParam.NUMBER to 1, EventParam.END to ez(2)))
    doMxml(EG())
  }

  @Test
  fun testWriteTwoVoltas() {
    SAE(EventType.VOLTA, ez(1), paramMapOf(EventParam.NUMBER to 1, EventParam.END to ez(1)))
    SAE(EventType.VOLTA, ez(2), paramMapOf(EventParam.NUMBER to 2, EventParam.END to ez(2)))
    doMxml(EG())
  }

  @Test
  fun testWriteTwoVoltasFirstIsTwoBars() {
    SAE(EventType.VOLTA, ez(1), paramMapOf(EventParam.NUMBER to 1, EventParam.END to ez(2)))
    SAE(EventType.VOLTA, ez(3), paramMapOf(EventParam.NUMBER to 2, EventParam.END to ez(3)))
    doMxml(EG())
  }

  @Test
  fun testWriteLyric() {
    SMV()
    SAE(
      EventType.LYRIC, eav(1), paramMapOf(
        EventParam.TEXT to "hello",
        EventParam.TYPE to LyricType.START,
        EventParam.NUMBER to 1
      )
    )
    doMxml(EG())
  }


  @Test
  fun testWrite2Lyrics() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(
      EventType.LYRIC, eav(1), paramMapOf(
        EventParam.TEXT to "wor",
        EventParam.TYPE to LyricType.START,
        EventParam.NUMBER to 1
      )
    )
    SAE(
      EventType.LYRIC, eav(1, crotchet()), paramMapOf(
        EventParam.TEXT to "ied",
        EventParam.TYPE to LyricType.END,
        EventParam.NUMBER to 1
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteLyricMid() {
    SMV()
    SAE(
      EventType.LYRIC, eav(1), paramMapOf(
        EventParam.TEXT to "hello",
        EventParam.TYPE to LyricType.MID,
        EventParam.NUMBER to 1
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteLyricTwoLines() {
    SMV()
    SAE(EventType.LYRIC, eav(1), paramMapOf(EventParam.TEXT to "hello", EventParam.NUMBER to 1))
    SAE(EventType.LYRIC, eav(1), paramMapOf(EventParam.TEXT to "wibble", EventParam.NUMBER to 2))
    doMxml(EG())
  }

  @Test
  fun testWriteTuplet() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 8, EventParam.HIDDEN to false
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteTupletNotes() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 8, EventParam.HIDDEN to false
      )
    )
    repeat(3) {
      SMV(duration = quaver(), eventAddress = eav(1, Duration(1, 12).multiply(it)))
    }
    doMxml(EG())
  }

  @Test
  fun testWriteTupletNotesUpstem() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 8, EventParam.HIDDEN to false
      )
    )
    repeat(3) {
      SMV(midiVal = 60, duration = quaver(), eventAddress = eav(1, Duration(1, 12).multiply(it)))
    }
    doMxml(EG())
  }

  @Test
  fun testWriteTupletNotes3_4() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 4, EventParam.HIDDEN to false
      )
    )
    repeat(3) {
      SMV(duration = crotchet(), eventAddress = eav(1, Duration(1, 6).multiply(it)))
    }
    doMxml(EG())
  }

  @Test
  fun testWriteTupletNotes3_4WithQuavers() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 4, EventParam.HIDDEN to false
      )
    )
    repeat(6) {
      SMV(duration = quaver(), eventAddress = eav(1, Duration(1, 12).multiply(it)))
    }
    doMxml(EG())
  }

  @Test
  fun testWriteTupletNotes3_4CrotchetsAndQuavers() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 4, EventParam.HIDDEN to false
      )
    )
    SMV(duration = crotchet(), eventAddress = eav(1))
    SMV(duration = crotchet(), eventAddress = eav(1, Duration(1, 6)))
    SMV(duration = quaver(), eventAddress = eav(1, Duration(1, 3)))
    SMV(duration = quaver(), eventAddress = eav(1, Duration(5, 12)))
    doMxml(EG())
  }

  @Test
  fun testWriteTupletNotesClefBetween() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 8, EventParam.HIDDEN to false
      )
    )
    SAE(EventType.CLEF, eav(1, Duration(1, 6)), paramMapOf(EventParam.TYPE to ClefType.BASS))
    repeat(3) {
      SMV(duration = quaver(), eventAddress = eav(1, Duration(1, 12).multiply(it)))
    }
    doMxml(EG())
  }

  @Test
  fun testWriteTupletSecondBeat() {
    SMV()
    SAE(
      EventType.TUPLET, eav(1, crotchet()), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 8, EventParam.HIDDEN to false
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteTupletLastBeat() {
    SMV()
    SMV(eventAddress = eav(1, minim()))
    SAE(
      EventType.TUPLET, eav(1, minim(1)), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 8, EventParam.HIDDEN to false
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteTupletWithCrotchet() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 8, EventParam.HIDDEN to false
      )
    )
    SMV(duration = crotchet())
    doMxml(EG())
  }


  @Test
  fun testWriteTupletQuaverCrotchet() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 8, EventParam.HIDDEN to false
      )
    )
    SMV(duration = quaver())
    SMV(duration = crotchet(), eventAddress = eav(1, Offset(1, 12)))
    doMxml(EG())
  }

  @Test
  fun testWriteTupletWithDottedValue() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 8, EventParam.HIDDEN to false
      )
    )
    SMV(duration = quaver(1))
    doMxml(EG())
  }

  @Test
  fun testWriteCrotchetTupletAfterQuavers() {
    repeat(4) {
      SMV(duration = quaver(), eventAddress = eav(1, quaver() * it))
    }
    SAE(
      EventType.TUPLET, eav(1, minim()), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 4, EventParam.HIDDEN to false
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteQuadruplet() {
    SCD(timeSignature = TimeSignature(6, 8))
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 4,
        EventParam.DENOMINATOR to 8
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteQuadrupletSemiquavers() {
    SCD(timeSignature = TimeSignature(6, 8))
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 4,
        EventParam.DENOMINATOR to 16
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteQuintuplet() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 5,
        EventParam.DENOMINATOR to 16, EventParam.HIDDEN to false
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteSextuplet() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 6,
        EventParam.DENOMINATOR to 8, EventParam.HIDDEN to false
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteSeptuplet() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 7,
        EventParam.DENOMINATOR to 8, EventParam.HIDDEN to false
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteSeptupletSemiquavers() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 7,
        EventParam.DENOMINATOR to 16, EventParam.HIDDEN to false
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteSeptupletSemiquaversLastBeat() {
    SMV()
    SMV(eventAddress = eav(1, minim()))
    SAE(
      EventType.TUPLET, eav(1, minim(1)), paramMapOf(
        EventParam.NUMERATOR to 7,
        EventParam.DENOMINATOR to 16, EventParam.HIDDEN to false
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteOctuplet() {
    SCD(TimeSignature(6, 8))
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 8,
        EventParam.DENOMINATOR to 16, EventParam.HIDDEN to false
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteDecuplet() {
    SCD(TimeSignature(6, 8))
    SAE(rest(crotchet(1)))
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 10, EventParam.HIDDEN to false
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteHarmony() {
    SAE(EventType.HARMONY, params = paramMapOf(EventParam.TEXT to "C"))
    doMxml(EG())
  }

  @Test
  fun testWriteHarmonyMidBar() {
    SMV()
    SAE(EventType.HARMONY, ea(1, minim()), params = paramMapOf(EventParam.TEXT to "C"))
    doMxml(EG())
  }

  @Test
  fun testWriteHarmonyMidQuavers() {
    repeat(8) {
      SMV(duration = quaver(), eventAddress = eav(1, quaver() * it))
    }
    SAE(EventType.HARMONY, ea(1, minim()), params = paramMapOf(EventParam.TEXT to "C"))
    doMxml(EG())
  }

  @Test
  fun testWriteHarmonyText() {
    SAE(EventType.HARMONY, params = paramMapOf(EventParam.TEXT to "Cm"))
    doMxml(EG())
  }

  @Test
  fun testWriteHarmonyTextRootNote() {
    SAE(EventType.HARMONY, params = paramMapOf(EventParam.TEXT to "C/G"))
    doMxml(EG())
  }

  @Test
  fun testWriteHarmonyTextRootNoteAccidental() {
    SAE(EventType.HARMONY, params = paramMapOf(EventParam.TEXT to "C/G#"))
    doMxml(EG())
  }

  @Test
  fun testWriteHarmonyAccidental() {
    SAE(EventType.HARMONY, params = paramMapOf(EventParam.TEXT to "C#"))
    doMxml(EG())
  }

  @Test
  fun testWriteHarmonyOffset() {
    SAE(EventType.PLACE_HOLDER, ea(1, minim()))
    SAE(EventType.HARMONY, params = paramMapOf(EventParam.TEXT to "C"))
    SAE(EventType.HARMONY, ea(1, minim()), params = paramMapOf(EventParam.TEXT to "G"))
    doMxml(EG())
  }

  @Test
  fun testWriteDynamic() {
    SAE(
      EventType.DYNAMIC, params = paramMapOf(
        EventParam.TYPE to DynamicType.PIANISSIMO,
        EventParam.IS_UP to true
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteDynamicMf() {
    SAE(
      EventType.DYNAMIC, params = paramMapOf(
        EventParam.TYPE to DynamicType.MEZZO_FORTE,
        EventParam.IS_UP to true
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteDynamicBelow() {
    SAE(
      EventType.DYNAMIC, params = paramMapOf(
        EventParam.TYPE to DynamicType.PIANISSIMO,
        EventParam.IS_UP to false
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteSlur() {
    SAE(EventType.SLUR, params = paramMapOf(EventParam.IS_UP to true, EventParam.END to ea(2)))
    doMxml(EG())
  }

  @Test
  fun testWriteSlurDown() {
    SAE(EventType.SLUR, params = paramMapOf(EventParam.IS_UP to false, EventParam.END to ea(2)))
    doMxml(EG())
  }

  @Test
  fun testWriteSlurOffset() {
    repeat(4) { SMV(eventAddress = eav(1, crotchet().multiply(it))) }
    SAE(
      EventType.SLUR, ea(1, minim()),
      paramMapOf(EventParam.IS_UP to true, EventParam.END to ea(1, minim(1)))
    )
    doMxml(EG())
  }

  @Test
  fun testWriteTWoSlurs() {
    repeat(4) { SMV(eventAddress = eav(1, crotchet().multiply(it))) }
    SAE(
      EventType.SLUR,
      eav(1),
      paramMapOf(EventParam.IS_UP to true, EventParam.END to ea(1, crotchet()))
    )
    SAE(
      EventType.SLUR, eav(1, minim()),
      paramMapOf(EventParam.IS_UP to true, EventParam.END to ea(1, minim(1)))
    )
    doMxml(EG())
  }

  @Test
  fun testWriteTWoSlursDown() {
    repeat(4) { SMV(eventAddress = eav(1, crotchet().multiply(it))) }
    SAE(
      EventType.SLUR,
      eav(1),
      paramMapOf(EventParam.IS_UP to false, EventParam.END to ea(1, crotchet()))
    )
    SAE(
      EventType.SLUR, eav(1, minim()),
      paramMapOf(EventParam.IS_UP to false, EventParam.END to ea(1, minim(1)))
    )
    doMxml(EG())
  }

  @Test
  fun testWriteSlurStaff2() {
    SCD(instruments = listOf("Piano"))
    repeat(4) { SMV(eventAddress = eav(1, crotchet().multiply(it))) }
    repeat(4) { SMV(eventAddress = eav(1, crotchet().multiply(it)).copy(staveId = StaveId(1, 2))) }
    SAE(
      EventType.SLUR,
      eav(1).copy(staveId = StaveId(1, 2)),
      paramMapOf(
        EventParam.IS_UP to false,
        EventParam.END to ea(1, crotchet()).copy(staveId = StaveId(1, 2))
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteSlurAcrossBar() {
    repeat(4) { SMV(eventAddress = eav(1, crotchet().multiply(it))) }
    repeat(4) { SMV(eventAddress = eav(2, crotchet().multiply(it))) }

    SAE(
      EventType.SLUR, params = paramMapOf(EventParam.IS_UP to true, EventParam.END to ea(2)),
      eventAddress = ea(1, minim(1))
    )
    doMxml(EG())
  }

  @Test
  fun testWriteSlurAcrossBarLastQuaverAfterRest() {
    SMV()
    SMV(eventAddress = eav(1, minim()))
    SAE(rest(quaver()), eav(1, minim(1)))
    SMV(duration = quaver(), eventAddress = eav(1, minim(2)))
    SMV(eventAddress = eav(2))

    SAE(
      EventType.SLUR, params = paramMapOf(EventParam.IS_UP to true, EventParam.END to ea(2)),
      eventAddress = ea(1, minim(1).add(quaver()))
    )
    doMxml(EG())
  }

  @Test
  fun testWriteTWoSlursAcrossBars() {
    repeat(3) { bar ->
      repeat(4) { SMV(eventAddress = eav(bar + 1, crotchet().multiply(it))) }
    }
    SAE(
      EventType.SLUR,
      ea(1),
      paramMapOf(EventParam.IS_UP to true, EventParam.END to ea(2, crotchet()))
    )
    SAE(
      EventType.SLUR, ea(2, minim()),
      paramMapOf(EventParam.IS_UP to true, EventParam.END to ea(3, minim(1)))
    )
    doMxml(EG())
  }

  @Test
  fun testWriteTWoSlursAcrossBarsUpDown() {
    repeat(3) { bar ->
      repeat(4) { SMV(eventAddress = eav(bar + 1, crotchet().multiply(it))) }
    }
    SAE(
      EventType.SLUR,
      ea(1),
      paramMapOf(EventParam.IS_UP to true, EventParam.END to ea(2, crotchet()))
    )
    SAE(
      EventType.SLUR, ea(2, minim()),
      paramMapOf(EventParam.IS_UP to false, EventParam.END to ea(3, minim(1)))
    )
    doMxml(EG())
  }

  @Test
  fun testWriteWedge() {
    SMV()
    SMV(eventAddress = eav(2))
    SAE(
      EventType.WEDGE, ea(1), paramMapOf(
        EventParam.TYPE to WedgeType.CRESCENDO,
        EventParam.IS_UP to true,
        EventParam.END to ea(2)
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteWedgeSecondNote() {
    repeat(4) {
      SMV(eventAddress = eav(1, crotchet().multiply(it)))
    }
    SAE(
      EventType.WEDGE, ea(1, crotchet()), paramMapOf(
        EventParam.TYPE to WedgeType.CRESCENDO,
        EventParam.IS_UP to true,
        EventParam.END to ea(1, minim(1))
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteWedgeSameStartEnd() {
    SMV(duration = semibreve())
    SAE(
      EventType.WEDGE, ea(1), paramMapOf(
        EventParam.TYPE to WedgeType.CRESCENDO,
        EventParam.IS_UP to true,
        EventParam.END to ea(1)
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWriteOctave() {
    SAE(EventType.OCTAVE, ea(1), paramMapOf(EventParam.NUMBER to 1, EventParam.END to ea(2)))
    doMxml(EG())
  }

  @Test
  fun testWriteOctaveOverNotes() {
    repeat(4) {
      SMV(eventAddress = ea(1, crotchet().multiply(it)))
    }
    SAE(EventType.OCTAVE, ea(1), paramMapOf(EventParam.NUMBER to 1, EventParam.END to ea(2)))
    doMxml(EG())
  }

  @Test
  fun testWriteOctaveBelowNotes() {
    repeat(4) {
      SMV(eventAddress = ea(1, crotchet().multiply(it)))
    }
    SAE(EventType.OCTAVE, ea(1), paramMapOf(EventParam.NUMBER to -1, EventParam.END to ea(2)))
    doMxml(EG())
  }

  @Test
  fun testWriteOctaveBelowNotesMidBar() {
    repeat(4) {
      SMV(eventAddress = ea(1, crotchet().multiply(it)))
    }
    SAE(
      EventType.OCTAVE,
      ea(1),
      paramMapOf(EventParam.NUMBER to -1, EventParam.END to ea(1, minim()))
    )
    doMxml(EG())
  }

  @Test
  fun testWriteOctaveSameNote() {
    SMV(duration = semibreve())
    SAE(EventType.OCTAVE, ea(1), paramMapOf(EventParam.NUMBER to 1, EventParam.END to ea(1)))
    doMxml(EG())
  }


  @Test
  fun testWriteOctaveOverNotesStaff2AlsoHasNotes() {
    SCD(instruments = listOf("Piano"))
    repeat(4) {
      SMV(eventAddress = ea(1, crotchet().multiply(it)))
      SMV(eventAddress = ea(1, crotchet().multiply(it)).copy(staveId = StaveId(1, 2)))
    }
    SAE(EventType.OCTAVE, ea(1), paramMapOf(EventParam.NUMBER to 1, EventParam.END to ea(2)))
    doMxml(EG())
  }

  @Test
  fun testWriteOctaveOverNotesStaff2() {
    SCD(instruments = listOf("Piano"))
    repeat(4) {
      SMV(eventAddress = ea(1, crotchet().multiply(it)))
      SMV(eventAddress = ea(1, crotchet().multiply(it)).copy(staveId = StaveId(1, 2)))
    }
    SAE(
      EventType.OCTAVE, ea(1).copy(staveId = StaveId(1, 2)),
      paramMapOf(EventParam.NUMBER to 1, EventParam.END to ea(2).copy(staveId = StaveId(1, 2)))
    )
    doMxml(EG())
  }

  @Test
  fun testWriteLongTrill() {
    SMV(duration = breve())
    SAE(
      EventType.LONG_TRILL, ea(1), paramMapOf(
        EventParam.END to ea(2),
        EventParam.IS_UP to true
      )
    )
    doMxml(EG())
  }

  @Test
  fun testWritePedal() {
    SAE(
      EventType.PEDAL,
      ea(1),
      paramMapOf(EventParam.END to ea(2), EventParam.TYPE to PedalType.LINE)
    )
    doMxml(EG())
  }

  @Test
  fun testWritePedalConsecutive() {
    SCDG()
    repeat(4) {
      SMV(eventAddress = eav(1, crotchet().multiply(it)))
    }
    SAE(
      EventType.PEDAL,
      ea(1),
      paramMapOf(EventParam.END to ea(1, crotchet()), EventParam.TYPE to PedalType.LINE)
    )
    SAE(
      EventType.PEDAL,
      ea(1, minim()),
      paramMapOf(EventParam.END to ea(1, minim(1)), EventParam.TYPE to PedalType.LINE)
    )
    doMxml(EG())
  }

  @Test
  fun testWritePedalConsecutiveBars() {
    SCDG()
    repeat(2) { bar ->
      repeat(4) {
        SMV(eventAddress = eav(bar + 1, crotchet().multiply(it)))
      }
    }
    SAE(
      EventType.PEDAL,
      ea(1),
      paramMapOf(EventParam.END to ea(1, minim(1)), EventParam.TYPE to PedalType.LINE)
    )
    SAE(
      EventType.PEDAL,
      ea(2),
      paramMapOf(EventParam.END to ea(2, minim(1)), EventParam.TYPE to PedalType.LINE)
    )
    doMxml(EG())
  }

  @Test
  fun testWriteUpbeatBar() {
    SAE(TimeSignature(1, 4, hidden = true).toHiddenEvent(), ez(1))
    doMxml(EG())
  }

  @Test
  fun testWriteUpbeatBarMultipleParts() {
    SCD(instruments = listOf("Violin", "Violin", "Viola", "Cello"))
    SAE(TimeSignature(1, 4, hidden = true).toHiddenEvent(), ez(1))
    doMxml(EG())
  }

  @Test
  fun testTransposingInstrument() {
    SCD(instruments = listOf("Trumpet"))
    doMxml(EG())
  }

  @Test
  fun testTransposingInstrumentWithNote() {
    SCD(instruments = listOf("Trumpet"))
    SMV(74)
    doMxml(EG())
  }

  @Test
  fun testTransposingInstrumentWithNoteNoAccidental() {
    SCD(instruments = listOf("Trumpet"))
    SMV(68)
    doMxml(EG())
  }

  @Test
  fun testTransposingInstrumentPlusConcertWithNoteNoAccidental() {
    SCD(instruments = listOf("Trumpet", "Trombone"))
    SMV(68)
    doMxml(EG())
  }

  @Test
  fun testTransposingInstrumentWithNoteAccidentalNotCancelled() {
    SCD(instruments = listOf("Trumpet"), ks = -4)
    SMV(74)
    doMxml(EG())
  }

  @Test
  fun testTransposingInstrumentAfterConcert() {
    SCD(instruments = listOf("Violin", "Trumpet"))
    doMxml(EG())
  }

  @Test
  fun testTransposingInstrumentBMajor() {
    SCD(instruments = listOf("Trumpet"), ks = 5)
    SMV(71)
    doMxml(EG())
  }

  @Test
  fun testInstrumentMidScore() {
    SMV()
    SAE(
      EventType.EXPRESSION_TEXT, ea(3), paramMapOf(
        EventParam.TEXT to "Viola",
        EventParam.IS_UP to false
      )
    )
    SMV(eventAddress = eav(3))
    doMxml(EG())
  }

  @Test
  fun testSystemBreak() {
    SAE(EventType.BREAK, ez(4), paramMapOf(EventParam.TYPE to BreakType.SYSTEM))
    doMxml(EG())
  }

  @Test
  fun testPageWidth() {
    SSO(EventParam.LAYOUT_PAGE_WIDTH, BLOCK_HEIGHT * 200)
    doMxml(EG())
  }

  @Test
  fun testPageHeight() {
    SSO(EventParam.LAYOUT_PAGE_HEIGHT, BLOCK_HEIGHT * 200)
    doMxml(EG())
  }

  @Test
  fun testPageMarginLeft() {
    SSO(EventParam.LAYOUT_LEFT_MARGIN, BLOCK_HEIGHT * 10)
    doMxml(EG())
  }

  @Test
  fun testPageMarginRight() {
    SSO(EventParam.LAYOUT_RIGHT_MARGIN, BLOCK_HEIGHT * 10)
    doMxml(EG())
  }

  @Test
  fun testPageMarginTop() {
    SSO(EventParam.LAYOUT_TOP_MARGIN, BLOCK_HEIGHT * 10)
    doMxml(EG())
  }

  @Test
  fun testPageMarginBottom() {
    SSO(EventParam.LAYOUT_BOTTOM_MARGIN, BLOCK_HEIGHT * 10)
    doMxml(EG())
  }

  @Test
  fun testPageSystemGap() {
    SSO(EventParam.LAYOUT_SYSTEM_GAP, BLOCK_HEIGHT * 10)
    doMxml(EG())
  }

//  @Test
//  fun testPageStaveGap() {
//    //    SSO(EventParam.LAYOUT_STAVE_GAP, BLOCK_HEIGHT * 10)
//    doMxml(EG())
//  }

  @Test
  fun testAppoggiatura() {
    SMV()
    SMV(eventAddress = eagv())
    doMxml(EG())
  }

  @Test
  fun testTwoAppoggiaturas() {
    SMV()
    SMV(eventAddress = eagv())
    SMV(eventAddress = eagv(graceOffset = crotchet()))
    doMxml(EG())
  }

  @Test
  fun testGlissando() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.GLISSANDO, ea(1), paramMapOf(EventParam.IS_STRAIGHT to false))
    doMxml(EG())
  }

  @Test
  fun testGlissandoStraight() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.GLISSANDO, ea(1), paramMapOf(EventParam.IS_STRAIGHT to true))
    doMxml(EG())
  }

  @Test
  fun testTremolo() {
    SMV()
    SAE(EventType.TREMOLO, eav(1), paramMapOf(EventParam.TREMOLO_BEATS to Duration(1, 16)))
    doMxml(EG())
  }

  @Test
  fun testPasteAfterImport() {
    SMV()
    val string = createMxml(EG())!!
    val newScore = scoreFromMxml(string, "", instrumentGetter)!!
    Clipboard.copy(ea(1), ea(2), newScore)
    val postPaste = Clipboard.paste(ea(1), newScore).rightOrThrow()
    newScore.compare(postPaste)
  }

  @Test
  fun testImportFile() {
    val url = this.javaClass.getResource("scores/score.xml")
    url?.let {
      val str = IOUtils.toString(it.toURI(), Charset.defaultCharset())
      val score = scoreFromMxml(str, "", instrumentGetter)
      val rep =
        scoreToRepresentation(
          score!!, drawableFactory
        )
    }
  }

  @Test
  fun testGetDenominator() {
    assertEqual(8, getDenominator(crotchet(), 2, 3))
  }

  @Test
  fun testGetDenominatorSextuplet() {
    assertEqual(16, getDenominator(crotchet(), 4, 6))
  }

  @Test
  fun testGetDenominatorDottedValue() {
    assertEqual(8, getDenominator(crotchet(1), 3, 4))
  }

  @Test
  fun testRepeatBarQuery() {
    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    val rbq = repeatBarQuery(EG())
    assertEqual(
      listOf(RepeatBarDesc(true, 1), RepeatBarDesc(false, 1)).toList(),
      rbq.getRepeatBars(ea(2)).toList()
    )
  }

  @Test
  fun testRepeatBarQueryTwoConsecutive() {
    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    SAE(EventType.REPEAT_BAR, ea(3), paramMapOf(EventParam.NUMBER to 1))
    val rbq = repeatBarQuery(EG())
    assertEqual(
      listOf(RepeatBarDesc(true, 1)).toList(),
      rbq.getRepeatBars(ea(2)).toList()
    )
    assertEqual(
      listOf(RepeatBarDesc(false, 1)).toList(),
      rbq.getRepeatBars(ea(3)).toList()
    )
  }


  @Test
  fun testRepeatBarQueryFourConsecutive() {
    repeat(4) {
      SAE(EventType.REPEAT_BAR, ea(2 + it), paramMapOf(EventParam.NUMBER to 1))
    }
    val rbq = repeatBarQuery(EG())
    assertEqual(
      listOf(RepeatBarDesc(true, 1)).toList(),
      rbq.getRepeatBars(ea(2)).toList()
    )
    assertEqual(listOf<RepeatBarDesc>(), rbq.getRepeatBars(ea(3)).toList())
    assertEqual(listOf<RepeatBarDesc>(), rbq.getRepeatBars(ea(4)).toList())
    assertEqual(
      listOf(RepeatBarDesc(false, 1)).toList(),
      rbq.getRepeatBars(ea(5)).toList()
    )
  }


  @Test
  fun testRepeatBarQueryTwoConsecutiveGroups() {
    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    SAE(EventType.REPEAT_BAR, ea(3), paramMapOf(EventParam.NUMBER to 1))
    SAE(EventType.REPEAT_BAR, ea(5), paramMapOf(EventParam.NUMBER to 1))
    SAE(EventType.REPEAT_BAR, ea(6), paramMapOf(EventParam.NUMBER to 1))
    val rbq = repeatBarQuery(EG())
    assertEqual(
      listOf(RepeatBarDesc(true, 1)).toList(),
      rbq.getRepeatBars(ea(2)).toList()
    )
    assertEqual(
      listOf(RepeatBarDesc(false, 1)).toList(),
      rbq.getRepeatBars(ea(3)).toList()
    )
    assertEqual(
      listOf(RepeatBarDesc(true, 1)).toList(),
      rbq.getRepeatBars(ea(5)).toList()
    )
    assertEqual(
      listOf(RepeatBarDesc(false, 1)).toList(),
      rbq.getRepeatBars(ea(6)).toList()
    )
  }

  @Test
  fun testRepeatBarQueryMultipleStaves() {
    SCDG()
    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    SAE(EventType.REPEAT_BAR, eas(3, dZero(), StaveId(1, 2)), paramMapOf(EventParam.NUMBER to 1))
    val rbq = repeatBarQuery(EG())
    assertEqual(
      listOf(RepeatBarDesc(true, 1), RepeatBarDesc(false, 1)).toList(),
      rbq.getRepeatBars(ea(2)).toList()
    )
    assertEqual(
      listOf(RepeatBarDesc(true, 1), RepeatBarDesc(false, 1)).toList(),
      rbq.getRepeatBars(eas(3, dZero(), StaveId(1, 2))).toList()
    )
  }

  @Test
  fun testRepeatBarQuery2Bars() {
    SAE(EventType.REPEAT_BAR, ea(3), paramMapOf(EventParam.NUMBER to 2))
    val rbq = repeatBarQuery(EG())
    assertEqual(
      listOf(RepeatBarDesc(true, 2)).toList(),
      rbq.getRepeatBars(ea(3)).toList()
    )
    assertEqual(
      listOf(RepeatBarDesc(false, 2)).toList(),
      rbq.getRepeatBars(ea(4)).toList()
    )
  }

  @Test
  fun testRepeatBarQuery2BarsConsecutive() {
    SAE(EventType.REPEAT_BAR, ea(3), paramMapOf(EventParam.NUMBER to 2))
    SAE(EventType.REPEAT_BAR, ea(5), paramMapOf(EventParam.NUMBER to 2))
    val rbq = repeatBarQuery(EG())
    assertEqual(
      listOf(RepeatBarDesc(true, 2)).toList(),
      rbq.getRepeatBars(ea(3)).toList()
    )
    assertEqual(
      listOf(RepeatBarDesc(false, 2)).toList(),
      rbq.getRepeatBars(ea(6)).toList()
    )
  }

  @Test
  fun testRepeatBarQuery2BarsNonConsecutive() {
    SAE(EventType.REPEAT_BAR, ea(3), paramMapOf(EventParam.NUMBER to 2))
    SAE(EventType.REPEAT_BAR, ea(6), paramMapOf(EventParam.NUMBER to 2))
    val rbq = repeatBarQuery(EG())
    assertEqual(
      listOf(RepeatBarDesc(true, 2)).toList(),
      rbq.getRepeatBars(ea(3)).toList()
    )
    assertEqual(
      listOf(RepeatBarDesc(false, 2)).toList(),
      rbq.getRepeatBars(ea(4)).toList()
    )
    assertEqual(
      listOf(RepeatBarDesc(true, 2)).toList(),
      rbq.getRepeatBars(ea(6)).toList()
    )
    assertEqual(
      listOf(RepeatBarDesc(false, 2)).toList(),
      rbq.getRepeatBars(ea(7)).toList()
    )
  }

  private fun doMxml(score: Score) {
    val string = createMxml(score)!!
    val newScore = scoreFromMxml(string, "", instrumentGetter)!!
    score.compare(newScore)
  }


}