package com.philblandford.ascore.external.export.mxml.out

import com.philblandford.ascore.external.export.mxml.out.creator.createMxmlScore
import com.philblandford.ascore.external.export.xml.*
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.types.Accidental
import com.philblandford.kscore.engine.types.ClefType
import com.philblandford.kscore.engine.types.MetaType
import org.philblandford.ascore2.external.export.xml.write
import org.w3c.dom.Element

internal data class MxmlAttribute(val name: String, val value: String)

internal data class MxmlNode(
  val name: String, val text: String? = null, val attributes: List<MxmlAttribute> = listOf(),
  val children: List<MxmlNode> = listOf()
)

fun createMxml(score: Score): String? {
  val body = createMxmlScore(score)?.write(StringBuilder())
  return HEADER + body
}

private val HEADER = """<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 3.1 Partwise//EN" "http://www.musicxml.org/dtds/partwise.dtd">
"""
private val VERSION = "3.1"


internal abstract class MxmlBase : KxmlBase() {
  override fun getPrefix(): String {
    return "mxml-"
  }
}

internal data class MxmlScorePartwise(
  @Attribute val version: String = VERSION,
  @Child @Order(0)  val work:MxmlWork?,
  @Child @Order(1)  val identification: MxmlIdentification?,

  @Child @Order(2) val defaults: MxmlDefaults?,
  @Child @Order(3) val credits: List<MxmlCredit> = listOf(),
  @Child @Order(4) val partList: MxmlPartList,
  @Child @Order(5) val parts: List<MxmlPart>
) : MxmlBase()

internal data class MxmlDefaults(
  @Child @Order(0) val scaling: MxmlScaling? = null,
  @Child @Order(1) val pageLayout: MxmlPageLayout? = null,
  @Child @Order(2) val systemLayout: MxmlSystemLayout? = null,
  @Child @Order(3) val staffLayout: MxmlStaffLayout? = null
) : MxmlBase()

internal data class MxmlScaling(
  @Child @Order(0) val millimeters: MxmlMillimeters,
  @Child @Order(1) val tenths: MxmlTenths
) : MxmlBase()

internal data class MxmlMillimeters(@Text val num: Float) : MxmlBase()

internal data class MxmlTenths(@Text val num: Int) : MxmlBase()

internal data class MxmlPageLayout(
  @Child @Order(0) val pageHeight: MxmlPageHeight,
  @Child @Order(1) val pageWidth: MxmlPageWidth,
  @Child @Order(2) val margins: List<MxmlPageMargins> = listOf()
) : MxmlBase()

internal data class MxmlPageHeight(@Text val num: Float) : MxmlBase()

internal data class MxmlPageWidth(@Text val num: Float) : MxmlBase()

internal data class MxmlPageMargins(
  @Attribute val type: String?,
  @Child @Order(0) val leftMargin: MxmlLeftMargin,
  @Child @Order(1) val rightMargin: MxmlRightMargin,
  @Child @Order(2) val topMargin: MxmlTopMargin,
  @Child @Order(3) val bottomMargin: MxmlBottomMargin
) : MxmlBase()

internal data class MxmlLeftMargin(@Text val value: Float) : MxmlBase()
internal data class MxmlRightMargin(@Text val value: Float) : MxmlBase()
internal data class MxmlTopMargin(@Text val value: Float) : MxmlBase()
internal data class MxmlBottomMargin(@Text val value: Float) : MxmlBase()

internal data class MxmlWork(@Child val title: MxmlWorkTitle) : MxmlBase()
internal data class MxmlWorkTitle(@Text val text: String) : MxmlBase()

internal data class MxmlIdentification(@Child @Order(0) val creator:List<MxmlCreator>) : MxmlBase()
internal data class MxmlCreator(@Attribute val type:String, @Text val text:String) : MxmlBase()

internal data class MxmlCredit(@Child val words: MxmlCreditWords, @Attribute val page: String? = null) :
  MxmlBase()

internal data class MxmlCreditWords(
  @Attribute val justify: String? = null, @Attribute val valign: String? = null,
  @Text val text: String
) : MxmlBase()

internal data class MxmlPartList(@Child val scoreParts: List<MxmlScorePart>) : MxmlBase()

internal data class MxmlScorePart(
  @Attribute val id: String,
  @Child @Order(0) val partName: MxmlPartName,
  @Child @Order(1) val partAbbreviation: MxmlPartAbbreviation?,
  @Child @Order(2) val scoreInstrument: List<MxmlScoreInstrument>,
  @Child @Order(3) val midiInstrument: List<MxmlMidiInstrument>
) : MxmlBase() {
  val isPercussion = midiInstrument.any { it.midiUnpitched != null }
}

internal data class MxmlPartName(@Text val name: String) : MxmlBase()

internal data class MxmlPartAbbreviation(@Text val name: String) : MxmlBase()

internal data class MxmlScoreInstrument(@Attribute val id: String, @Child val instrumentName: MxmlInstrumentName) :
  MxmlBase()

internal data class MxmlMidiInstrument(
  @Attribute val id: String, @Child val midiChannel: MxmlMidiChannel,
  @Child val midiProgram: MxmlMidiProgram,
  @Child val midiUnpitched: MxmlMidiUnpitched? = null
) : MxmlBase()

internal data class MxmlMidiChannel(@Text val num: Int) : MxmlBase()

internal data class MxmlMidiProgram(@Text val num: Int) : MxmlBase()

internal data class MxmlMidiUnpitched(@Text val num: Int) : MxmlBase()

internal data class MxmlInstrumentName(@Text val name: String) : MxmlBase()

internal data class MxmlPart(@Attribute val id: String, @Child val measures: List<MxmlMeasure>) :
  MxmlBase()

internal data class MxmlMeasure(
  @Attribute val number: String,
  @Child val elements: List<MxmlMeasureElement> = listOf()
) : MxmlBase() {
  fun attributes(): MxmlAttributes? {
    return elements.find { it is MxmlAttributes } as MxmlAttributes?
  }
}

internal open class MxmlMeasureElement : MxmlBase()

internal data class MxmlPrint(
  @Attribute val newSystem: String?,
  @Child @Order(0) val systemLayout: MxmlSystemLayout?
) : MxmlMeasureElement()

internal data class MxmlSystemLayout(
  @Child @Order(0) val systemMargins: MxmlSystemMargins? = null,
  @Child @Order(1) val systemDistance: MxmlSystemDistance? = null,
  @Child @Order(1) val topSystemDistance: MxmlTopSystemDistance? = null
) : MxmlBase()

internal data class MxmlSystemMargins(
  @Child @Order(0) val leftMargin: MxmlLeftMargin,
  @Child @Order(1) val rightMargin: MxmlRightMargin
) : MxmlBase()

internal data class MxmlStaffLayout(@Child @Order(0) val staffDistance: MxmlStaffDistance?) : MxmlBase()

internal data class MxmlStaffDistance(@Text val num: Float) : MxmlBase()

internal data class MxmlSystemDistance(@Text val num: Float) : MxmlBase()

internal data class MxmlTopSystemDistance(@Text val num: Float) : MxmlBase()

internal data class MxmlAttributes(
  @Child @Order(0) val divisions: MxmlDivisions? = null,
  @Child @Order(1) val key: MxmlKey? = null,
  @Child @Order(2) val time: MxmlTime? = null,
  @Child @Order(3) val staves: MxmlStaves? = null,
  @Child @Order(4) val clef: List<MxmlClef> = listOf(),
  @Child @Order(5) val staffDetails: MxmlStaffDetails? = null,
  @Child @Order(6) val transpose: MxmlTranspose? = null,
  @Child @Order(7) val measureStyle: List<MxmlMeasureStyle> = listOf()
) : MxmlMeasureElement()

internal data class MxmlDivisions(@Text val num: Int) : MxmlBase()

internal data class MxmlKey(@Child val fifths: MxmlFifths) : MxmlBase()

internal data class MxmlFifths(@Text val num: Int) : MxmlBase()

internal data class MxmlStaves(@Text val num: Int) : MxmlBase()

internal data class MxmlTime(
  @Child @Order(0) val beat: MxmlBeats,
  @Child @Order(1) val beatType: MxmlBeatType
) : MxmlBase()

internal data class MxmlBeats(@Text val num: Int) : MxmlBase()

internal data class MxmlBeatType(@Text val num: Int) : MxmlBase()

internal data class MxmlClef(
  @Child @Order(0) val sign: MxmlSign,
  @Child @Order(1) val line: MxmlLine? = null,
  @Child @Order(2) val clefOctaveChange: MxmlClefOctaveChange? = null,
  @Attribute val number: Int? = null
) : MxmlBase()

internal data class MxmlSign(@Text val text: String) : MxmlBase()

internal data class MxmlLine(@Text val num: Int) : MxmlBase()

internal data class MxmlClefOctaveChange(@Text val num: Int) : MxmlBase()

internal data class MxmlStaffDetails(@Child @Order(0) val staffLines: MxmlStaffLines?) : MxmlBase()

internal data class MxmlStaffLines(@Text val num: Int) : MxmlBase()

internal data class MxmlTranspose(
  @Child @Order(0) val diatonic: MxmlDiatonic?,
  @Child @Order(1) val chromatic: MxmlChromatic
) : MxmlBase()

internal data class MxmlDiatonic(@Text val num: Int) : MxmlBase()

internal data class MxmlChromatic(@Text val num: Int) : MxmlBase()

internal data class MxmlMeasureStyle(@Attribute val staff:Int?,
                            @Child @Order(0) val measureRepeat: MxmlMeasureRepeat?) : MxmlBase()

internal data class MxmlMeasureRepeat(@Attribute val type: String, @Attribute val slashes: Int) : MxmlBase()

internal data class MxmlStaff(@Text val num: Int) : MxmlBase()

internal data class MxmlBeam(@Attribute val number: Int, @Text val text: String) : MxmlBase()

internal data class MxmlVoice(@Text val num: Int) : MxmlBase()

internal data class MxmlBackup(@Child val duration: MxmlDuration) : MxmlMeasureElement()

internal data class MxmlForward(@Child val duration: MxmlDuration) : MxmlMeasureElement()

internal data class MxmlBarline(
  @Attribute val location: String?,
  @Child @Order(0) val barStyle: MxmlBarStyle?,
  @Child @Order(1) val ending: MxmlEnding?,
  @Child @Order(2) val repeat: MxmlRepeat? = null
) : MxmlMeasureElement()

internal data class MxmlBarStyle(@Text val text: String) : MxmlBase()

internal data class MxmlRepeat(@Attribute val direction: String) : MxmlBase()

internal data class MxmlEnding(@Attribute val number: Int, @Attribute val type: String) : MxmlBase()


internal data class MxmlNote(
  @Child @Order(0) val grace: MxmlGrace?,
  @Child @Order(1) val chord: MxmlChord?,
  @Child @Order(2) val descriptor: MxmlNoteDescriptor,
  @Child @Order(3) val duration: MxmlDuration?,
  @Child @Order(4) val tie: MxmlTie?,
  @Child @Order(5) val instrument: MxmlInstrument?,
  @Child @Order(6) val voice: MxmlVoice,
  @Child @Order(7) val type: MxmlType?,
  @Child @Order(8) val dot: List<MxmlDot>,
  @Child @Order(9) val timeModification: MxmlTimeModification? = null,
  @Child @Order(10) val notehead: MxmlNotehead? = null,
  @Child @Order(11) val staff: MxmlStaff? = null,
  @Child @Order(12) val beam: List<MxmlBeam> = listOf(),
  @Child @Order(13) val notations: MxmlNotations? = null,
  @Child @Order(14) val lyric: List<MxmlLyric> = listOf()
) : MxmlMeasureElement()

internal open class MxmlNoteDescriptor : MxmlBase()

internal data class MxmlPitch(
  @Child @Order(0) val step: MxmlStep,
  @Child @Order(1) val alter: MxmlAlter?,
  @Child @Order(2) val octave: MxmlOctave
) : MxmlNoteDescriptor()

internal class MxmlGrace : MxmlBase()

internal class MxmlChord : MxmlBase()

internal class MxmlRest : MxmlNoteDescriptor()

internal data class MxmlTie(@Attribute val type: String) : MxmlBase()

internal data class MxmlInstrument(@Attribute val id: String) : MxmlBase()

internal data class MxmlUnpitched(
  @Child @Order(0) val mxmlDisplayStep: MxmlDisplayStep,
  @Child @Order(1) val mxmlDisplayOctave: MxmlDisplayOctave
) : MxmlNoteDescriptor()

internal data class MxmlDisplayStep(@Text val text: String) : MxmlBase()

internal data class MxmlDisplayOctave(@Text val num: Int) : MxmlBase()

internal data class MxmlStep(@Text val text: String) : MxmlBase()

internal data class MxmlAlter(@Text val num: Int) : MxmlBase()

internal data class MxmlOctave(@Text val num: Int) : MxmlBase()

internal data class MxmlNotehead(@Text val text: String) : MxmlBase()

internal data class MxmlDuration(@Text val num: Int) : MxmlBase()

internal data class MxmlTimeModification(
  @Child @Order(0) val actualNotes: MxmlActualNotes,
  @Child @Order(1) val normalNotes: MxmlNormalNotes
) : MxmlBase()

internal data class MxmlActualNotes(@Text val num: Int) : MxmlBase()

internal data class MxmlNormalNotes(@Text val num: Int) : MxmlBase()

internal data class MxmlNotations(@Child val elements: List<MxmlNotationsElement>) : MxmlBase()

internal sealed class MxmlNotationsElement() : MxmlBase()

internal data class MxmlTied(@Attribute val type: String) : MxmlNotationsElement()

internal data class MxmlSlur(
  @Attribute val type: String,
  @Attribute val number: String?,
  @Attribute val placement: String?
) : MxmlNotationsElement()

internal data class MxmlTuplet(@Attribute val type: String) : MxmlNotationsElement()

internal data class MxmlArticulations(@Child val articulations: List<MxmlArticulationsElement>) :
  MxmlNotationsElement()

internal sealed class MxmlArticulationsElement() : MxmlBase()

internal class MxmlAccent : MxmlArticulationsElement()
internal class MxmlTenuto : MxmlArticulationsElement()
internal class MxmlStaccatissimo : MxmlArticulationsElement()
internal class MxmlStaccato : MxmlArticulationsElement()
internal class MxmlStrongAccent : MxmlArticulationsElement()

internal data class MxmlOrnaments(
  @Child @Order(0) val element: List<MxmlOrnamentsElement>,
  @Child @Order(1) val accidentalMark: List<MxmlAccidentalMark> = listOf()
) :
  MxmlNotationsElement()

internal data class MxmlArpeggiate(@Attribute val direction: String? = null) : MxmlNotationsElement()

internal sealed class MxmlOrnamentsElement : MxmlBase()
internal class MxmlInvertedMordent : MxmlOrnamentsElement()
internal class MxmlMordent : MxmlOrnamentsElement()
internal class MxmlTrillMark : MxmlOrnamentsElement()
internal class MxmlTurn : MxmlOrnamentsElement()

internal data class MxmlAccidentalMark(
  @Attribute val placement: String,
  @Text val text: String
) : MxmlBase()

internal data class MxmlWavyLine(@Attribute val type: String, @Attribute val placement: String?) :
  MxmlOrnamentsElement()

internal data class MxmlTremolo(@Attribute val type: String?, @Text val beats: Int) : MxmlOrnamentsElement()

internal data class MxmlTechnical(@Child val elements: List<MxmlTechnicalElement>) :
  MxmlNotationsElement()

internal sealed class MxmlTechnicalElement() : MxmlBase()

internal data class MxmlFingering(@Text val text: String) : MxmlTechnicalElement()

internal data class MxmlFermata(@Text val shape: String?) : MxmlNotationsElement()

internal data class MxmlGlissando(
  @Attribute val type: String,
  @Attribute val lineType: String?
) : MxmlNotationsElement()

internal data class MxmlLyric(
  @Attribute val number: Int?,
  @Child @Order(0) val syllabic: MxmlSyllabic?,
  @Child @Order(1) val text: MxmlText
) : MxmlBase()

internal data class MxmlSyllabic(@Text val text: String) : MxmlBase()

internal data class MxmlExtend(@Attribute val type: String) : MxmlBase()

internal data class MxmlText(@Text val text: String) : MxmlBase()

internal data class MxmlType(@Text val text: String) : MxmlBase()

internal class MxmlDot : MxmlBase()

internal data class MxmlDirection(
  @Attribute val placement: String?,
  @Child @Order(0) val directionType: List<MxmlDirectionType>,
  @Child @Order(1) val staff: MxmlStaff? = null
) : MxmlMeasureElement()

internal data class MxmlDirectionType(@Child val content: MxmlDirectionTypeContent) : MxmlBase()

internal sealed class MxmlDirectionTypeContent() : MxmlBase()

internal data class MxmlMetronome(
  @Child @Order(0) val beatUnit: MxmlBeatUnit,
  @Child @Order(1) val beatUnitDot: List<MxmlBeatUnitDot>,
  @Child @Order(2) val perMinute: MxmlPerMinute
) : MxmlDirectionTypeContent()

internal data class MxmlRehearsal(@Text val text: String) : MxmlDirectionTypeContent()

internal data class MxmlWords(@Text val text: String) : MxmlDirectionTypeContent()

internal data class MxmlBeatUnit(@Text val text: String) : MxmlBase()

internal data class MxmlPerMinute(@Text val num: Int) : MxmlBase()

internal class MxmlBeatUnitDot() : MxmlBase()

internal data class MxmlDynamics(@Child val component: MxmlDynamicsComponent) : MxmlDirectionTypeContent()

internal sealed class MxmlDynamicsComponent() : MxmlBase()

internal class MxmlPppppp() : MxmlDynamicsComponent()
internal class MxmlPpppp() : MxmlDynamicsComponent()
internal class MxmlPppp() : MxmlDynamicsComponent()
internal class MxmlPpp() : MxmlDynamicsComponent()
internal class MxmlPp() : MxmlDynamicsComponent()
internal class MxmlP() : MxmlDynamicsComponent()
internal class MxmlFfffff() : MxmlDynamicsComponent()
internal class MxmlFffff() : MxmlDynamicsComponent()
internal class MxmlFfff() : MxmlDynamicsComponent()
internal class MxmlFff() : MxmlDynamicsComponent()
internal class MxmlFf() : MxmlDynamicsComponent()
internal class MxmlF() : MxmlDynamicsComponent()
internal class MxmlFp() : MxmlDynamicsComponent()
internal class MxmlFz() : MxmlDynamicsComponent()
internal class MxmlMf() : MxmlDynamicsComponent()
internal class MxmlMp() : MxmlDynamicsComponent()
internal class MxmlRf() : MxmlDynamicsComponent()
internal class MxmlRfz() : MxmlDynamicsComponent()
internal class MxmlSf() : MxmlDynamicsComponent()
internal class MxmlSfz() : MxmlDynamicsComponent()
internal class MxmlSffz() : MxmlDynamicsComponent()
internal class MxmlSfp() : MxmlDynamicsComponent()
internal class MxmlSfpp() : MxmlDynamicsComponent()

internal data class MxmlOctaveShift(
  @Attribute val type: String, @Attribute val size: Int?,
  @Attribute val number: Int? = null
) :
  MxmlDirectionTypeContent()

internal data class MxmlWedge(@Attribute val type: String) : MxmlDirectionTypeContent()

internal data class MxmlDashes(
  @Attribute val type: String,
  @Attribute val dashLength: Float = 7.5f
) : MxmlDirectionTypeContent()

internal data class MxmlPedal(
  @Attribute val type: String, @Attribute val line: String?,
  @Attribute val sign: String?
) : MxmlDirectionTypeContent()

internal data class MxmlHarmony(
  @Child @Order(0) val root: MxmlRoot,
  @Child @Order(1) val kind: MxmlKind,
  @Child @Order(2) val bass: MxmlBass? = null,
  @Child @Order(3) val offset: MxmlOffset? = null
) : MxmlMeasureElement()

internal data class MxmlRoot(
  @Child @Order(0) val rootStep: MxmlRootStep,
  @Child @Order(1) val rootAlter: MxmlRootAlter? = null
) : MxmlBase()

internal data class MxmlRootStep(@Text val text: String) : MxmlBase()

internal data class MxmlRootAlter(@Text val num: Int) : MxmlBase()

internal data class MxmlKind(@Text val text: String) : MxmlBase()

internal data class MxmlBass(
  @Child @Order(0) val bassStep: MxmlBassStep,
  @Child @Order(1) val bassAlter: MxmlBassAlter? = null
) : MxmlBase()

internal data class MxmlBassStep(@Text val text: String) : MxmlBase()

internal data class MxmlBassAlter(@Text val num: Int) : MxmlBase()

internal data class MxmlOffset(@Text val num: Int) : MxmlBase()

internal data class MxmlDegree(
  @Child @Order(0) val value: MxmlDegreeValue,
  @Child @Order(1) val alter: MxmlDegreeAlter,
  @Child @Order(2) val type: MxmlDegreeType
) : MxmlBase()

internal data class MxmlDegreeValue(@Text val text: String) : MxmlBase()
internal data class MxmlDegreeAlter(@Text val num: String) : MxmlBase()
internal data class MxmlDegreeType(@Text val text: String) : MxmlBase()

private val clefTypes = mapOf(
  ClefType.TREBLE to MxmlClef(MxmlSign("G"), MxmlLine(2)),
  ClefType.BASS to MxmlClef(MxmlSign("F"), MxmlLine(4)),
  ClefType.ALTO to MxmlClef(MxmlSign("C"), MxmlLine(3)),
  ClefType.TENOR to MxmlClef(MxmlSign("C"), MxmlLine(4)),
  ClefType.SOPRANO to MxmlClef(MxmlSign("C"), MxmlLine(1)),
  ClefType.MEZZO to MxmlClef(MxmlSign("C"), MxmlLine(2)),
  ClefType.TREBLE_8VA to MxmlClef(MxmlSign("G"), MxmlLine(2), MxmlClefOctaveChange(1)),
  ClefType.TREBLE_15VA to MxmlClef(MxmlSign("G"), MxmlLine(2), MxmlClefOctaveChange(2)),
  ClefType.TREBLE_8VB to MxmlClef(MxmlSign("G"), MxmlLine(2), MxmlClefOctaveChange(-1)),
  ClefType.BASS_8VA to MxmlClef(MxmlSign("F"), MxmlLine(4), MxmlClefOctaveChange(1)),
  ClefType.BASS_8VB to MxmlClef(MxmlSign("F"), MxmlLine(4), MxmlClefOctaveChange(-1)),
  ClefType.PERCUSSION to MxmlClef(MxmlSign("percussion"), MxmlLine(2))
)


private val clefTypesReversed = clefTypes.map { it.value to it.key }.toMap()

internal fun clefToMxml(clefType: ClefType): MxmlClef? {
  return clefTypes[clefType]
}

internal fun mxmlToClef(mxmlClef: MxmlClef): ClefType? {
  clefTypesReversed[mxmlClef.copy(number = null)]?.let { return it }
  throw MalformedXmlException("Unknown clef $mxmlClef")
}

private val noteTypes = mapOf(
  hemidemisemiquaver() to "64th",
  demisemiquaver() to "32nd",
  semiquaver() to "16th",
  quaver() to "eighth",
  crotchet() to "quarter",
  minim() to "half",
  semibreve() to "whole",
  breve() to "breve",
  longa() to "long"
)

private val noteTypesReversed = noteTypes.map { it.value to it.key }.toMap()

fun durationToMxml(duration: Duration): String? {
  return noteTypes[duration.undot()]
}

fun mxmlToDuration(mxml: String): Duration? {
  return noteTypesReversed[mxml]

}

fun Element.getAttributeOrNull(attr: String): String? {
  val value = getAttribute(attr)
  return if (value.isEmpty()) null else value
}

internal data class CreditAlignment(val justify: String, val valign: String)

private val alignments = mapOf(
  MetaType.TITLE to CreditAlignment("center", "top"),
  MetaType.SUBTITLE to CreditAlignment("center", "top"),
  MetaType.COMPOSER to CreditAlignment("right", "bottom"),
  MetaType.LYRICIST to CreditAlignment("left", "bottom")
)
private val alignmentsReversed = alignments.map { it.value to it.key }.toMap()

internal fun getAlignment(type: MetaType): CreditAlignment? {
  return alignments[type]
}

internal fun alignmentToType(creditAlignment: CreditAlignment, haveTitle: Boolean = false): MetaType? {

  return when (creditAlignment) {
    CreditAlignment("center", "top") ->
      if (haveTitle) MetaType.SUBTITLE else MetaType.TITLE
    else -> alignmentsReversed[creditAlignment]
  }
}

private val alters = mapOf(
  Accidental.DOUBLE_FLAT to -2,
  Accidental.FLAT to -1,
  Accidental.SHARP to 1,
  Accidental.DOUBLE_SHARP to 2
)
private val altersReversed = alters.map { it.value to it.key }.toMap()


fun accidentalToMxml(accidental: Accidental): Int? {
  return alters[accidental]
}

fun mxmlToAccidental(mxml: Int): Accidental? {
  return altersReversed[mxml]
}

internal class MalformedXmlException(msg: String) : Exception(msg)