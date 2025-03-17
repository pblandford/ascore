package org.philblandford.ui.insert.choose.viewmodel

import com.philblandford.kscore.engine.duration.SEMIQUAVER
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.types.*
import org.philblandford.ascore2.features.ui.model.DeleteBehaviour
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ascore2.features.ui.model.TapInsertBehaviour
import org.philblandford.ui.R

internal val insertItems = listOf(
    InsertItem(
        R.drawable.tuplet_3, R.string.tuplet, "insert_tuplet", LayoutID.TUPLET,
        EventType.TUPLET,
        paramMapOf(
            EventParam.NUMERATOR to 3,
            EventParam.HIDDEN to false
        ),
        rangeCapable = true
    ),
    InsertItem(R.drawable.tie, R.string.tie, "insert_tie", LayoutID.TIE, EventType.TIE),
    InsertItem(
        R.drawable.accent_icon,
        R.string.articulation,
        "insert_articulation",
        LayoutID.ARTICULATION,
        EventType.ARTICULATION,
        paramMapOf(EventParam.TYPE to ArticulationType.ACCENT),
        rangeCapable = true
    ),
    InsertItem(
        R.drawable.trill,
        R.string.ornament,
        "insert_ornament",
        LayoutID.ORNAMENT,
        EventType.ORNAMENT,
        paramMapOf(
            EventParam.TYPE to OrnamentType.TRILL,
            EventParam.ACCIDENTAL_ABOVE to null,
            EventParam.ACCIDENTAL_BELOW to null
        ),
        rangeCapable = true,
        getEventType = {
            when (it) {
                is OrnamentType -> EventType.ORNAMENT
                ArpeggioType.NORMAL -> EventType.ARPEGGIO
                else -> EventType.LONG_TRILL
            }
        },
        isLine = {
            it == EventType.LONG_TRILL
        }
    ),
    InsertItem(R.drawable.treble_clef, R.string.clef, "insert_clef", LayoutID.CLEF, EventType.CLEF),
    InsertItem(
        R.drawable.key_d,
        R.string.key_signature,
        "insert_key",
        LayoutID.KEY,
        EventType.KEY_SIGNATURE,
        typeParam = EventParam.SHARPS
    ),
    InsertItem(
        R.drawable.tempo, R.string.tempo, "insert_tempo", LayoutID.TEMPO, EventType.TEMPO,
        paramMapOf(
            EventParam.BPM to 120,
            EventParam.DURATION to crotchet(),
            EventParam.HIDDEN to false
        )
    ),
    InsertItem(
        R.drawable.common,
        R.string.time_signature,
        "insert_time",
        LayoutID.TIME,
        EventType.TIME_SIGNATURE,
        paramMapOf(
            EventParam.TYPE to TimeSignatureType.CUSTOM,
            EventParam.NUMERATOR to 4,
            EventParam.DENOMINATOR to 4,
            EventParam.HIDDEN to false
        ),
    ),
    InsertItem(R.drawable.key_c, R.string.bars, "insert_bars", LayoutID.BAR, EventType.BAR),
    InsertItem(
        R.drawable.lyrics,
        R.string.lyrics,
        "insert_lyrics",
        LayoutID.LYRIC,
        EventType.LYRIC,
        paramMapOf(
            EventParam.NUMBER to 1,
            EventParam.AFTER to true
        ),
        tapInsertBehaviour = TapInsertBehaviour.SET_MARKER,
        deleteBehaviour = DeleteBehaviour.DELETE_AT_MARKER
    ),
    InsertItem(
        R.drawable.chord,
        R.string.harmony,
        "insert_harmonies",
        LayoutID.HARMONY,
        EventType.HARMONY,
        tapInsertBehaviour = TapInsertBehaviour.SET_MARKER,
        deleteBehaviour = DeleteBehaviour.DELETE_AT_MARKER
    ),
    InsertItem(
        R.drawable.text, R.string.text, "insert_text", LayoutID.TEXT, EventType.TEMPO_TEXT,
        paramMapOf(
            EventParam.TEXT to "",
            EventParam.IS_UP to true
        ),
        isRangeCapable = { it == EventType.EXPRESSION_TEXT || it == EventType.EXPRESSION_DASH }
    ),
    InsertItem(
        R.drawable.forte,
        R.string.dynamic,
        "insert_dynamic",
        LayoutID.DYNAMIC,
        EventType.DYNAMIC,
        paramMapOf(EventParam.IS_UP to false)
    ),
    InsertItem(
        R.drawable.bar_double,
        R.string.barline,
        "insert_barline",
        LayoutID.BARLINE,
        EventType.BARLINE
    ),

    InsertItem(
        R.drawable.trumpet, R.string.instrument, "insert_instrument", LayoutID.INSTRUMENT,
        EventType.PART
    ),
    InsertItem(
        R.drawable.title, R.string.meta_data, "insert_title", LayoutID.METADATA, EventType.TITLE,
        tapInsertBehaviour = TapInsertBehaviour.NONE
    ),
    InsertItem(
        R.drawable.footer, R.string.footer, "insert_footer", LayoutID.FOOTER, EventType.FOOTER_LEFT,
        tapInsertBehaviour = TapInsertBehaviour.NONE
    ),
    InsertItem(
        R.drawable.slur, R.string.slur, "insert_slur", LayoutID.SLUR, EventType.SLUR,
        params = paramMapOf(EventParam.IS_UP to true),
        line = true
    ),
    InsertItem(
        R.drawable.hairpin_crescendo,
        R.string.wedge,
        "insert_wedge",
        LayoutID.WEDGE,
        EventType.WEDGE,
        params = paramMapOf(EventParam.IS_UP to false, EventParam.TYPE to WedgeType.CRESCENDO),
        line = true
    ),
    InsertItem(
        R.drawable.octave,
        R.string.octave,
        "insert_octave",
        LayoutID.OCTAVE,
        EventType.OCTAVE,
        paramMapOf(EventParam.NUMBER to 1),
        line = true,
        typeParam = EventParam.NUMBER
    ),
    InsertItem(
        R.drawable.pedal, R.string.pedal, "insert_pedal", LayoutID.PEDAL,
        EventType.PEDAL,
        line = true
    ),
    InsertItem(
        R.drawable.downbow_down,
        R.string.bowing,
        "insert_bowing",
        LayoutID.BOWING,
        EventType.BOWING,
        paramMapOf(EventParam.TYPE to BowingType.UP_BOW),
        rangeCapable = true
    ),
    InsertItem(
        R.drawable.one,
        R.string.fingering,
        "insert_fingering",
        LayoutID.FINGERING,
        EventType.FINGERING,
        typeParam = EventParam.NUMBER,
        rangeCapable = true
    ),
    InsertItem(
        R.drawable.one,
        R.string.repeat_beat,
        "insert_repeat_beat",
        LayoutID.REPEAT_BEAT,
        EventType.DURATION,
        typeParam = EventParam.DURATION,
        params = paramMapOf(EventParam.TYPE to DurationType.REPEAT_BEAT),
        rangeCapable = true
    ),
    InsertItem(
        R.drawable.fermata, R.string.pause, "insert_pause", LayoutID.PAUSE,
        EventType.FERMATA, getEventType = {
            when (it) {
                is FermataType -> EventType.FERMATA
                else -> EventType.PAUSE
            }
        }),
    InsertItem(
        R.drawable.coda,
        R.string.navigation,
        "insert_navigation",
        LayoutID.NAVIGATION,
        EventType.NAVIGATION,
        paramMapOf(EventParam.START to true)
    ),
    InsertItem(
        R.drawable.first_time_bar,
        R.string.volta,
        "insert_volta",
        LayoutID.VOLTA,
        EventType.VOLTA,
        paramMapOf(EventParam.NUMBER to 1),
        line = true
    ),
    InsertItem(
        R.drawable.one_bar_repeat,
        R.string.repeat_bar,
        "insert_repeat_bar",
        LayoutID.REPEAT_BAR,
        EventType.REPEAT_BAR,
        paramMapOf(EventParam.NUMBER to 1),
        typeParam = EventParam.NUMBER,
        rangeCapable = true
    ),
    InsertItem(
        R.drawable.beamed_quavers,
        R.string.beams,
        "insert_beam_notes",
        LayoutID.BEAM,
        EventType.BEAM,
        paramMapOf(EventParam.TYPE to BeamType.JOIN),
        typeParam = EventParam.TYPE,
        rangeCapable = true,
        line = true
    ),
    InsertItem(
        R.drawable.tremolo,
        R.string.tremolo,
        "insert_tremolo",
        LayoutID.TREMOLO,
        EventType.TREMOLO,
        paramMapOf(EventParam.TREMOLO_BEATS to SEMIQUAVER),
        typeParam = EventParam.TREMOLO_BEATS,
        rangeCapable = true
    ),
    InsertItem(
        R.drawable.gliss, R.string.glissando, "insert_glissando", LayoutID.GLISSANDO,
        EventType.GLISSANDO
    ),

    InsertItem(
        R.drawable.transpose_by,
        R.string.transpose_by,
        "transpose_by",
        LayoutID.TRANSPOSE_BY,
        EventType.TRANSPOSE,
        paramMapOf(EventParam.ACCIDENTAL to Accidental.SHARP),
        rangeCapable = true
    ),
    InsertItem(
        R.drawable.transpose,
        R.string.transpose_to,
        "transpose_to",
        LayoutID.TRANSPOSE_TO,
        EventType.TRANSPOSE,
        rangeCapable = true
    ),
    InsertItem(
        R.drawable.page,
        R.string.page_size,
        "page_size",
        LayoutID.PAGE_SIZE,
        EventType.LAYOUT
    ),
    InsertItem(
        R.drawable.system_break,
        R.string.system_break,
        "system_break",
        LayoutID.SCORE_BREAK,
        EventType.BREAK
    ),
//  InsertItem(
//    R.drawable.segment,
//    R.string.segment_width,
//    "segment_width",
//    LayoutID.SEGMENT_WIDTH,
//    EventType.BREAK,
//    tapInsertBehaviour = TapInsertBehaviour.SET_MARKER
//  ),
    InsertItem(
        R.drawable.indent, R.string.page_margins, "page_margin", LayoutID.MARGIN,
        EventType.LAYOUT
    ),
    InsertItem(
        R.drawable.bracket_part,
        R.string.group_staves,
        "stave_join",
        LayoutID.GROUP_STAVES,
        EventType.STAVE_JOIN,
        paramMapOf(EventParam.TYPE to StaveJoinType.BRACKET),
        line = true
    ),
    InsertItem(
        R.drawable.four,
        R.string.bar_numbering,
        "bar_numbering",
        LayoutID.BAR_NUMBERING,
        EventType.LAYOUT,
        tapInsertBehaviour = TapInsertBehaviour.NONE
    )
)
