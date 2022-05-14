package org.philblandford.ui.util

import org.philblandford.ui.R
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.ArticulationType.*
import com.philblandford.kscore.engine.types.BarLineType.*
import com.philblandford.kscore.engine.types.BowingType.*
import com.philblandford.kscore.engine.types.ClefType.*
import com.philblandford.kscore.engine.types.DynamicType.*
import com.philblandford.kscore.engine.types.FermataType.SQUARE
import com.philblandford.kscore.engine.types.FermataType.TRIANGLE
import com.philblandford.kscore.engine.types.OrnamentType.*

internal val durationIds by lazy {
  listOf(
    R.drawable.hemidemisemiquaver_up to HEMIDEMISEMIQUAVER,
    R.drawable.demisemiquaver_up to DEMISEMIQUAVER,
    R.drawable.semiquaver_up to SEMIQUAVER,
    R.drawable.quaver_up to QUAVER,
    R.drawable.crotchet_up to CROTCHET,
    R.drawable.minim_up to MINIM,
    R.drawable.semibreve to SEMIBREVE,
    R.drawable.breve to BREVE,
    R.drawable.longa to LONGA
  )
}

val restIds by lazy {
  listOf(
    R.drawable.hemidemisemiquaver_rest to HEMIDEMISEMIQUAVER,
    R.drawable.demisemiquaver_rest to DEMISEMIQUAVER,
    R.drawable.semiquaver_rest to SEMIQUAVER,
    R.drawable.quaver_rest to QUAVER,
    R.drawable.crotchet_rest to CROTCHET,
    R.drawable.minim_rest_line to MINIM,
    R.drawable.semibreve_rest_line to SEMIBREVE,
    R.drawable.breve_rest_line to BREVE,
    R.drawable.longa_rest_line to LONGA
  )
}

private val restIdsReversed by lazy {
  restIds.associate { it.second to it.first }
}

fun Duration.toResource() = restIdsReversed[this]

val accidentalIds by lazy {
  listOf(
    R.drawable.sharp_underline to Accidental.FORCE_SHARP,
    R.drawable.flat_underline to Accidental.FORCE_FLAT,
    R.drawable.sharp to Accidental.SHARP,
    R.drawable.flat to Accidental.FLAT,
    R.drawable.natural to Accidental.NATURAL,
    R.drawable.double_sharp to Accidental.DOUBLE_SHARP,
    R.drawable.double_flat to Accidental.DOUBLE_FLAT
  )
}
val noteHeadIds by lazy {
  listOf(
    R.drawable.tadpole_head_empty to NoteHeadType.NORMAL,
    R.drawable.cross_minim to NoteHeadType.CROSS,
    R.drawable.diamond_minim to NoteHeadType.DIAMOND
  )
}

internal val barLineIds = listOf(
  R.drawable.bar_normal to BarLineType.NORMAL,
  R.drawable.bar_double to DOUBLE,
  R.drawable.bar_final to FINAL,
  R.drawable.bar_repeat_start to START_REPEAT,
  R.drawable.bar_repeat_end to END_REPEAT
)

internal val clefIds = listOf(
  R.drawable.treble_clef to TREBLE,
  R.drawable.bass_clef to BASS,
  R.drawable.alto_clef to ALTO,
  R.drawable.tenor_clef to TENOR,
  R.drawable.mezzo_clef to MEZZO,
  R.drawable.soprano_clef to SOPRANO,
  R.drawable.treble_octava_up to TREBLE_8VA,
  R.drawable.treble_octava_down to TREBLE_8VB,
  R.drawable.bass_clef_octava_up to BASS_8VA,
  R.drawable.bass_clef_octava_down to BASS_8VB
)

internal val dynamicIds = listOf(
  R.drawable.fortissimo_molto to MOLTO_FORTISSIMO,
  R.drawable.fortissimo to FORTISSIMO,
  R.drawable.forte to FORTE,
  R.drawable.mezzo_forte to MEZZO_FORTE,
  R.drawable.mezzo_piano to MEZZO_PIANO,
  R.drawable.piano to PIANO,
  R.drawable.pianissimo to PIANISSIMO,
  R.drawable.pianissimo_molto to MOLTO_PIANISSIMO,
  R.drawable.sforzandissimo to SFORZANDISSMO,
  R.drawable.sforzando to SFORZANDO,
  R.drawable.sforzando_piano to SFORZANDO_PIANO,
  R.drawable.fortepiano to FORTE_PIANO
)

internal val articulationIds = listOf(
  R.drawable.accent to ACCENT,
  R.drawable.staccato_icon to STACCATO,
  R.drawable.tenuto_icon to TENUTO,
  R.drawable.marcato_icon to MARCATO,
  R.drawable.spiccato_icon to STACCATISSIMO
)

internal val ornamentIds = listOf(
  R.drawable.trill to TRILL,
  R.drawable.turn to TURN,
  R.drawable.mordent to MORDENT,
  R.drawable.lower_mordent to LOWER_MORDENT
)

internal val bowingIds = listOf(
  R.drawable.upbow_down to UP_BOW,
  R.drawable.downbow_down to DOWN_BOW,
  R.drawable.lh_pizzicato to LH_PIZZICATO,
  R.drawable.snap_pizzicato to SNAP_PIZZICATO,
  R.drawable.harmonic to HARMONIC
)

internal val fingeringIds = listOf(
  R.drawable.zero to 0,
  R.drawable.one to 1,
  R.drawable.two to 2,
  R.drawable.three to 3,
  R.drawable.four to 4,
  R.drawable.five to 5,
)

internal val fermataIds = listOf(
  R.drawable.fermata to FermataType.NORMAL,
  R.drawable.fermata_square to SQUARE,
  R.drawable.fermata_triangle to TRIANGLE
)

internal val pauseIds = listOf(
  R.drawable.breath to PauseType.BREATH,
  R.drawable.caesura to PauseType.CAESURA
)

internal val numberIds = listOf(
  R.drawable.zero,
  R.drawable.one,
  R.drawable.two,
  R.drawable.three,
  R.drawable.four,
  R.drawable.five,
  R.drawable.six,
  R.drawable.seven,
  R.drawable.eight,
  R.drawable.nine
)

val keySignatureIds = listOf(
  R.drawable.key_c to 0,
  R.drawable.key_g to 1,
  R.drawable.key_d to 2,
  R.drawable.key_a to 3,
  R.drawable.key_e to 4,
  R.drawable.key_b to 5,
  R.drawable.key_fsharp to 6,
  R.drawable.key_csharp to 7,
  R.drawable.key_f to -1,
  R.drawable.key_bflat to -2,
  R.drawable.key_eflat to -3,
  R.drawable.key_aflat to -4,
  R.drawable.key_dflat to -5,
  R.drawable.key_gflat to -6,
  R.drawable.key_cflat to -7
)

val wedgeIds = listOf(
  R.drawable.hairpin_crescendo to WedgeType.CRESCENDO,
  R.drawable.hairpin_diminuendo to WedgeType.DIMINUENDO
)

val octaveIds = listOf(
  R.drawable.octave to 1,
  R.drawable.octave15 to 2,
  R.drawable.octavemb to -1,
  R.drawable.octave15mb to -2
)

val pedalIds = listOf(
  R.drawable.pedal_full to PedalType.LINE,
  R.drawable.pedal_end to PedalType.STAR
)

val staveJoinIds = listOf(
  R.drawable.bracket_part to StaveJoinType.BRACKET,
  R.drawable.grand_stave_part to StaveJoinType.GRAND,
)

val navigationIds = listOf(
  R.drawable.coda to NavigationType.CODA,
  R.drawable.da_capo to NavigationType.DA_CAPO,
  R.drawable.dal_segno to NavigationType.DAL_SEGNO,
  R.drawable.fine to NavigationType.FINE,
  R.drawable.segno to NavigationType.SEGNO
)

val repeatBarIds = listOf(
  R.drawable.one_bar_repeat to 1,
  R.drawable.two_bar_repeat to 2
)

val tremoloIds = listOf(
  R.drawable.blank to dZero(),
  R.drawable.tremolo_1 to QUAVER,
  R.drawable.tremolo_2 to SEMIQUAVER,
  R.drawable.tremolo_3 to DEMISEMIQUAVER,
  R.drawable.tremolo_4 to HEMIDEMISEMIQUAVER
)

val glissandoIds = listOf(
  R.drawable.gliss to GlissandoType.WAVY,
  R.drawable.gliss_straight to GlissandoType.LINE
)

val breakIds = listOf(
  R.drawable.system_break to BreakType.SYSTEM,
  R.drawable.page_break to BreakType.PAGE
)

val timeSignatureIds = listOf(
  R.drawable.common to TimeSignatureType.COMMON,
  R.drawable.cut_common to TimeSignatureType.CUT_COMMON
)

val metaIds = listOf(
  R.string.title to MetaType.TITLE,
  R.string.subtitle to MetaType.SUBTITLE,
  R.string.composer to MetaType.COMPOSER,
  R.string.lyricist to MetaType.LYRICIST,
)

fun MetaType.resourceId():Int {
  return metaIds.find { it.second == this }?.first ?: -1
}

