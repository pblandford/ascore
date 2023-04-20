package core.areadirectory

import assertEqual
import com.philblandford.kscore.engine.core.StemGeography
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.ArticulationType.*
import com.philblandford.kscore.engine.types.OrnamentType.*
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.endEntry
import com.philblandford.kscore.engine.core.areadirectory.segment.addDecorations
import com.philblandford.kscore.engine.core.areadirectory.segment.chordArea
import com.philblandford.kscore.engine.core.areadirectory.segment.chordBaseArea
import com.philblandford.kscore.engine.core.areadirectory.segment.replaceStem
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.LEDGER_OFFSET
import com.philblandford.kscore.engine.core.representation.LINE_THICKNESS
import com.philblandford.kscore.engine.core.representation.TADPOLE_WIDTH
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.duration.Chord
import com.philblandford.kscore.engine.duration.Note
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.newadder.subadders.ChordDecoration
import org.junit.Test
import com.philblandford.kscore.engine.core.representation.RepTest

class ChordAreaTest : RepTest() {

  @Test
  fun testChordAreaCreated() {
    val event = dslChord(crotchet()) { pitch(NoteLetter.F) }
    val ca = drawableFactory.chordArea(event)
    assert(ca?.base?.childMap?.isNotEmpty() ?: false)
  }

  @Test
  fun testChordAreaCreatedNotePos() {
    val event = dslChord(crotchet()) { pitch(NoteLetter.F) }
    val ca = drawableFactory.chordArea(event)
    assertEqual(Coord(0, BLOCK_HEIGHT * 6), ca?.base?.childMap?.toList()?.first()?.first?.coord)
  }

  @Test
  fun testStemCreated() {
    val event = dslChord(crotchet()) { pitch(NoteLetter.F) }
    val ca = drawableFactory.chordArea(event)!!
    assert(ca.base.findByTag("Stem").isNotEmpty())
  }

  @Test
  fun testStemCreatedRightOfNote() {
    val event = dslChord(crotchet()) { pitch(NoteLetter.F) }
    val ca = drawableFactory.chordArea(event)!!
    val note = ca.base.findByTag("Tadpole").toList().first()
    val stem = ca.base.findByTag("Stem").toList().first()
    assertEqual(endEntry(note), endEntry(stem) - LINE_THICKNESS / 2)
  }

  @Test
  fun testStemCreatedDownStem() {
    val event = dslChord(crotchet()) { pitch(NoteLetter.F, octave = 5) }
    val ca = drawableFactory.chordArea(event)!!
    val stem = ca.base.findByTag("Stem").toList().first()
    assertEqual(Coord(LINE_THICKNESS / 2, 0), stem.first.coord)
  }

  @Test
  fun testLedgerCreated() {
    val event = dslChord { pitch(NoteLetter.C, octave = 4) }
    val ca = drawableFactory.chordArea(event)!!
    assertEqual(1, ca.base.findByTag("Ledger").toList().size)
  }

  @Test
  fun testLedgerCreatedWidth() {
    val event = dslChord { pitch(NoteLetter.C, octave = 4) }
    val ca = drawableFactory.chordArea(event)!!
    val note = ca.base.findByTagSingle("Tadpole")!!
    val ledger = ca.base.findByTagSingle("Ledger")!!
    assertEqual(note.width + LEDGER_OFFSET*2, ledger.width)
  }

  @Test
  fun testLedgerCreatedOffset() {
    val event = dslChord { pitch(NoteLetter.C, octave = 4) }
    val ca = drawableFactory.chordArea(event)!!
    val note = ca.base.findByTag("Tadpole").toList().first()
    val ledger = ca.base.findByTag("Ledger").toList().first()
    assertEqual(note.first.coord.x - LEDGER_OFFSET, ledger.first.coord.x)
  }

  @Test
  fun testLedgerCreatedWidthCluster() {
    val event = dslChord { pitch(NoteLetter.C, octave = 4); pitch(NoteLetter.B, octave = 3) }
    val ca = drawableFactory.chordArea(event)!!
    val notes = ca.base.findByTag("Tadpole")
    val width = notes.toList().sumBy { it.second.width }
    val ledger = ca.base.findByTagSingle("Ledger")!!
    assertEqual(width + LEDGER_OFFSET*2, ledger.width)
  }

  @Test
  fun testChordAreaCreatedSmall() {
    val normalEvent = dslChord { pitch(NoteLetter.C, octave = 4) }
    val normalTadpole = drawableFactory.chordArea(normalEvent)!!.base.findByTag("Tadpole").toList().first().second
    val smallEvent = Chord(crotchet(), listOf(Note(crotchet(), isSmall = true))).toEvent()
    val smallTadpole = drawableFactory.chordArea(smallEvent)!!.base.findByTag("Tadpole").toList().first().second
    assert(normalTadpole.width > smallTadpole.width)
  }

  @Test
  fun testChordAreaReplaceStem() {
    val event = dslChord(crotchet()) { pitch(NoteLetter.F) }
    val ca = drawableFactory.chordArea(event)
    val newCa = ca?.replaceStem(StemGeography(-20, BLOCK_HEIGHT * 7, 20, TADPOLE_WIDTH, true), drawableFactory = drawableFactory)
    assertEqual(-20, newCa?.base?.findPairByTagSingle("Stem")?.first?.y)
  }

  @Test
  fun testChordAreaReplaceStemArticulationsShift() {
    val event = dslChord(crotchet()) { pitch(NoteLetter.F) }.addParam(EventParam.ARTICULATION to
        ChordDecoration(true, listOf(ArticulationType.STACCATO)), EventParam.IS_UPSTEM to true)
    val ca = drawableFactory.chordArea(event)
    val newCa = ca?.replaceStem(
      StemGeography(-50, BLOCK_HEIGHT * 7,
      20, TADPOLE_WIDTH, true), 2, drawableFactory = drawableFactory)
    val artic = newCa?.base?.findPairByTagSingle("Articulation")?.first?.y
    assert(artic!! < -50)
  }

  @Test
  fun testCreateChordArea() {
    val chord = dslChord(crotchet())
    val area = drawableFactory.chordArea(chord)
    assert(area!!.base.width > 0)
  }

  @Test
  fun testDecorationAdder() {
    val chord = decChord(EventParam.ARTICULATION, STACCATO)
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second)!!.first
    assert(area.findByTag("Articulation").isNotEmpty())
  }

  @Test
  fun testArticulationAboveNote() {
    val chord = decChord(EventParam.ARTICULATION, STACCATO)
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second)!!.first
    assert(area.isAbove("Staccato", "Tadpole"))
  }

  @Test
  fun testArticulationMidwayX() {
    val chord = decChord(EventParam.ARTICULATION, STACCATO)
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second)!!.first
    assert(area.isInsideX("Staccato", "Tadpole"))
  }

  @Test
  fun testArticulationBelowNote() {
    val chord = decChord(EventParam.ARTICULATION, STACCATO, Pitch(NoteLetter.F, octave = 4))
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second)!!.first
    assert(area.isAbove("Tadpole", "Staccato"))
  }

  @Test
  fun testArticulationAboveStemTwoVoices() {
    val chord = decChord(EventParam.ARTICULATION, STACCATO).addParam(EventParam.IS_UPSTEM, true)
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second, 2)!!.first
    assert(area.isAbove("Staccato", "Stem"))
  }

  @Test
  fun testArticulationBelowStemTwoVoices() {
    val chord = decChord(EventParam.ARTICULATION, STACCATO).addParam(EventParam.IS_UPSTEM, false)
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second, 2)!!.first
    assert(area.isAbove("Stem", "Staccato"))
  }

  @Test
  fun testArticulationLeftOfStemTwoVoices() {
    val chord = decChord(EventParam.ARTICULATION, STACCATO).addParam(EventParam.IS_UPSTEM, false)
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second, 2)!!.first
    assert(area.isInsideX("Stem", "Staccato"))
  }

  @Test
  fun testOrnamentAboveNote() {
    val chord = decChord(EventParam.ORNAMENT, Ornament(TRILL))
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second)!!.first
    assert(area.isAbove("Ornament", "Tadpole"))
  }

  @Test
  fun testOrnamentAboveStem() {
    val chord = decChord(EventParam.ORNAMENT, Ornament(TRILL), Pitch(NoteLetter.F, octave = 4))
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second)!!.first
    assert(area.isAbove("Ornament", "Stem"))
  }

  @Test
  fun testOrnamentAboveStave() {
    val chord = decChord(EventParam.ORNAMENT, Ornament(TRILL), Pitch(NoteLetter.F, octave = 3))
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second)!!.first
    val ornament = area.findByTag("Ornament").toList().first()
    assert(ornament.first.coord.y + ornament.second.height < 0)
  }

  @Test
  fun testOrnamentAboveStemTwoVoices() {
    val chord = decChord(EventParam.ORNAMENT, Ornament(TRILL), Pitch(NoteLetter.F, octave = 4))
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second, 2)!!.first
    assert(area.isAbove("Ornament", "Stem"))
  }

  @Test
  fun testOrnamentBelowStemTwoVoices() {
    val chord = decChord(
      EventParam.ORNAMENT,
      Ornament(TRILL),
      Pitch(NoteLetter.F, octave = 4)
    ).addParam(EventParam.IS_UPSTEM, false)
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second, 2)!!.first
    assert(area.isAbove("Stem", "Ornament"))
  }

  @Test
  fun testFingeringAboveNote() {
    val chord = decChord(EventParam.FINGERING, 1)
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second)!!.first
    assert(area.isAbove("Fingering", "Tadpole"))
  }

  @Test
  fun testFingeringBelowNote() {
    val chord = decChord(EventParam.FINGERING, 1, Pitch(NoteLetter.F, octave = 4), false)
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second)!!.first
    assert(area.isAbove("Tadpole", "Fingering"))
  }

  @Test
  fun testFingeringBelowNoteSpecified() {
    val chord = decChord(EventParam.FINGERING, 1, up = false)
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second)!!.first
    assert(area.isAbove("Tadpole", "Fingering"))
  }

  @Test
  fun testBowingAboveNote() {
    val chord = decChord(EventParam.BOWING, BowingType.DOWN_BOW)
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second)!!.first
    assert(area.isAbove("Bowing", "Tadpole"))
  }

  @Test
  fun testBowingBelowNote() {
    val chord = decChord(EventParam.BOWING, BowingType.DOWN_BOW, Pitch(NoteLetter.F, octave = 4))
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second)!!.first
    assert(area.isAbove("Tadpole", "Bowing"))
  }

  @Test
  fun testBowingAboveArticulation() {
    val chord = decChord(EventParam.BOWING, BowingType.DOWN_BOW).addParam(EventParam.ARTICULATION,
      ChordDecoration(items = listOf(STACCATO)))
    val areaGeog = drawableFactory.chordBaseArea(chord)!!
    val area = drawableFactory.addDecorations(areaGeog.first, chord, areaGeog.second)!!.first
    assert(area.isAbove("Bowing", "Staccato"))
  }


  private fun <T> decChord(
    eventParam: EventParam,
    type: T,
    pitch: Pitch = Pitch(NoteLetter.F, octave = 5),
    up:Boolean = true
  ): Event {
    return dslChord(crotchet()) {
      pitch(
        pitch.noteLetter,
        pitch.accidental,
        pitch.octave
      )
    }.addParam(
      eventParam,
      ChordDecoration(up, listOf(type))
    )
  }


  private fun Area.isAbove(tag: String, tag2: String): Boolean {
    val one = findByTag(tag).toList().first()
    val two = findByTag(tag2).toList().first()
    return one.first.coord.y + one.second.height < two.first.coord.y
  }

  private fun Area.isInsideX(tag: String, tag2: String): Boolean {
    val one = findByTag(tag).toList().first()
    val two = findByTag(tag2).toList().first()
    return one.first.coord.x > two.first.coord.x &&
        one.first.coord.x + one.second.width < two.first.coord.x + two.second.width
  }
}