package com.philblandford.kscore.engine.eventadder

import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.eventadder.subadders.*
import com.philblandford.kscore.engine.types.EventType

internal val destinations = mapOf(
  EventType.ARPEGGIO to EventDestination(listOf(ScoreLevelType.VOICEMAP), ArpeggioSubAdder),
  EventType.ARTICULATION to EventDestination(listOf(ScoreLevelType.VOICEMAP), ArticulationSubAdder),
  EventType.BAR to EventDestination(listOf(ScoreLevelType.BAR), BarSubAdder),
  EventType.BAR_BREAK to EventDestination(listOf(ScoreLevelType.BAR), BarBreakSubAdder),
  EventType.BARLINE to EventDestination(listOf(ScoreLevelType.SCORE), BarLineSubAdder),
  EventType.BEAM to EventDestination(listOf(ScoreLevelType.PART), UserBeamSubAdder),
  EventType.BOWING to EventDestination(listOf(ScoreLevelType.VOICEMAP), BowingSubAdder),
  EventType.BREAK to EventDestination(
    listOf(ScoreLevelType.SCORE, ScoreLevelType.PART),
    BreakSubAdder
  ),
  EventType.CLEF to EventDestination(listOf(ScoreLevelType.STAVE), ClefSubAdder),
  EventType.COMPOSER to EventDestination(listOf(ScoreLevelType.SCORE), MoveableSubAdder),
  EventType.DURATION to EventDestination(listOf(ScoreLevelType.VOICEMAP), DurationSubAdder),
  EventType.DYNAMIC to EventDestination(listOf(ScoreLevelType.STAVE), DynamicSubAdder),
  EventType.EXPRESSION_DASH to EventDestination(listOf(ScoreLevelType.STAVE), LineSubAdder),
  EventType.EXPRESSION_TEXT to EventDestination(
    listOf(ScoreLevelType.STAVE),
    ExpressionTextSubAdder
  ),
  EventType.FERMATA to EventDestination(listOf(ScoreLevelType.SCORE), MoveableSubAdder),
  EventType.FILENAME to EventDestination(listOf(ScoreLevelType.SCORE), GenericSubAdder),
  EventType.FINGERING to EventDestination(listOf(ScoreLevelType.VOICEMAP), FingeringSubAdder),
  EventType.FOOTER_LEFT to EventDestination(listOf(ScoreLevelType.SCORE), MoveableSubAdder),
  EventType.FOOTER_RIGHT to EventDestination(listOf(ScoreLevelType.SCORE), MoveableSubAdder),
  EventType.FOOTER_CENTER to EventDestination(listOf(ScoreLevelType.SCORE), MoveableSubAdder),
  EventType.FREE_TEXT to EventDestination(listOf(ScoreLevelType.SCORE), GenericSubAdder),
  EventType.GLISSANDO to EventDestination(listOf(ScoreLevelType.STAVE), GlissandoSubAdder),
  EventType.HIDDEN_TIME_SIGNATURE to EventDestination(
    listOf(ScoreLevelType.SCORE),
    HiddenTimeSignatureSubAdder
  ),
  EventType.INSTRUMENT to EventDestination(listOf(ScoreLevelType.PART), InstrumentSubAdder),
  EventType.KEY_SIGNATURE to EventDestination(listOf(ScoreLevelType.SCORE), KeySignatureSubAdder),
  EventType.HARMONY to EventDestination(listOf(ScoreLevelType.BAR), HarmonySubAdder),
  EventType.LAYOUT to EventDestination(listOf(ScoreLevelType.SCORE), LayoutSubAdder),
  EventType.LONG_TRILL to EventDestination(listOf(ScoreLevelType.STAVE), LineSubAdder),
  EventType.LYRIC to EventDestination(listOf(ScoreLevelType.VOICEMAP), LyricSubAdder),
  EventType.LYRICIST to EventDestination(listOf(ScoreLevelType.SCORE), MoveableSubAdder),
  EventType.META to EventDestination(listOf(ScoreLevelType.SCORE), MetaSubAdder),
  EventType.NAVIGATION to EventDestination(listOf(ScoreLevelType.SCORE), MoveableSubAdder),
  EventType.NO_TYPE to EventDestination(listOf(ScoreLevelType.PART), BeamSubAdder),
  EventType.NOTE to EventDestination(listOf(ScoreLevelType.VOICEMAP), NoteSubAdder),
  EventType.NOTE_SHIFT to EventDestination(listOf(ScoreLevelType.VOICEMAP), NoteShiftSubAdder),
  EventType.OCTAVE to EventDestination(listOf(ScoreLevelType.STAVE), OctaveSubAdder),
  EventType.OPTION to EventDestination(listOf(ScoreLevelType.SCORE), OptionSubAdder),
  EventType.ORNAMENT to EventDestination(listOf(ScoreLevelType.VOICEMAP), OrnamentSubAdder),
  EventType.PART to EventDestination(listOf(ScoreLevelType.SCORE), PartSubAdder),
  EventType.PAUSE to EventDestination(listOf(ScoreLevelType.BAR), MoveableSubAdder),
  EventType.PEDAL to EventDestination(listOf(ScoreLevelType.STAVE), PedalSubAdder),
  EventType.PLACE_HOLDER to EventDestination(listOf(ScoreLevelType.BAR), PlaceHolderSubAdder),
  EventType.PLAYBACK_STATE to EventDestination(
    listOf(ScoreLevelType.PART, ScoreLevelType.SCORE),
    PlaybackStateSubAdder
  ),
  EventType.REHEARSAL_MARK to EventDestination(listOf(ScoreLevelType.SCORE), MoveableSubAdder),
  EventType.REPEAT_BAR to EventDestination(listOf(ScoreLevelType.STAVE), RepeatBarSubAdder),
  EventType.REPEAT_END to EventDestination(listOf(ScoreLevelType.SCORE), GenericSubAdder),
  EventType.REPEAT_START to EventDestination(listOf(ScoreLevelType.SCORE), GenericSubAdder),
  EventType.SLUR to EventDestination(listOf(ScoreLevelType.STAVE), LineSubAdder),
  EventType.SPACE to EventDestination(listOf(ScoreLevelType.BAR)),
  EventType.STAVE to EventDestination(listOf(ScoreLevelType.PART), StaveSubAdder),
  EventType.STAVE_JOIN to EventDestination(listOf(ScoreLevelType.SCORE), StaveJoinSubAdder),
  EventType.SUBTITLE to EventDestination(listOf(ScoreLevelType.SCORE), MoveableSubAdder),
  EventType.TEMPO to EventDestination(listOf(ScoreLevelType.SCORE), TempoSubAdder),
  EventType.TEMPO_TEXT to EventDestination(listOf(ScoreLevelType.SCORE), MoveableSubAdder),
  EventType.TIE to EventDestination(listOf(ScoreLevelType.VOICEMAP), TieSubAdder),
  EventType.TIME_SIGNATURE to EventDestination(listOf(ScoreLevelType.SCORE), TimeSignatureSubAdder),
  EventType.TITLE to EventDestination(listOf(ScoreLevelType.SCORE), MoveableSubAdder),
  EventType.TRANSPOSE to EventDestination(listOf(ScoreLevelType.SCORE), TransposeSubAdder),
  EventType.TREMOLO to EventDestination(listOf(ScoreLevelType.VOICEMAP), TremoloSubAdder),
  EventType.TUPLET to EventDestination(listOf(ScoreLevelType.VOICEMAP), TupletSubAdder),
  EventType.UISTATE to EventDestination(listOf(ScoreLevelType.SCORE)),
  EventType.VOLTA to EventDestination(listOf(ScoreLevelType.SCORE), VoltaSubAdder),
  EventType.WEDGE to EventDestination(listOf(ScoreLevelType.STAVE), LineSubAdder)
)

val allDestinations =
  destinations.map { it.value } + EventDestination(listOf(ScoreLevelType.PART), BeamSubAdder) +
      EventDestination(listOf(ScoreLevelType.SCORE), MarkerSubAdder)