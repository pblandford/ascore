package org.philblandford.ui.edit.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventType
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.edit.items.articulation.compose.ArticulationEdit
import org.philblandford.ui.edit.items.bowing.compose.BowingEdit
import org.philblandford.ui.edit.items.clef.compose.ClefEdit
import org.philblandford.ui.edit.items.dynamics.compose.DynamicsEdit
import org.philblandford.ui.edit.items.fermata.compose.FermataEdit
import org.philblandford.ui.edit.items.fingering.compose.FingeringEdit
import org.philblandford.ui.edit.items.glissando.compose.GlissandoEdit
import org.philblandford.ui.edit.items.harmony.compose.HarmonyEdit
import org.philblandford.ui.edit.items.instrumentedit.compose.InstrumentEdit
import org.philblandford.ui.edit.items.keysignature.compose.KeySignatureEdit
import org.philblandford.ui.edit.items.navigation.compose.NavigationEdit
import org.philblandford.ui.edit.items.octave.compose.OctaveEdit
import org.philblandford.ui.edit.items.ornament.compose.OrnamentEdit
import org.philblandford.ui.edit.items.pause.compose.PauseEdit
import org.philblandford.ui.edit.items.pedal.compose.PedalEdit
import org.philblandford.ui.edit.items.slur.compose.SlurEdit
import org.philblandford.ui.edit.items.stavejoin.compose.StaveJoinEdit
import org.philblandford.ui.edit.items.tempo.compose.TempoEdit
import org.philblandford.ui.edit.items.text.compose.LyricEdit
import org.philblandford.ui.edit.items.text.compose.TextEdit
import org.philblandford.ui.edit.items.timesignature.compose.TimeSignatureEdit
import org.philblandford.ui.edit.items.wedge.compose.WedgeEdit


@Composable
fun EditPanel(modifier: Modifier, type: EventType, scale: Float) {
    Box(modifier.background(MaterialTheme.colorScheme.surface)) {
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {

      when (type) {
        EventType.ARTICULATION -> ArticulationEdit()
        EventType.BOWING -> BowingEdit()
        EventType.BREAK -> DeleteOnlyEdit()
        EventType.CLEF -> ClefEdit()
        EventType.COMPOSER -> TextEdit(scale)
        EventType.DURATION -> DeleteOnlyEdit()
        EventType.DYNAMIC -> DynamicsEdit(scale)
        EventType.EXPRESSION_DASH -> TextEdit(scale)
        EventType.EXPRESSION_TEXT -> TextEdit(scale)
        EventType.FERMATA -> FermataEdit(scale)
        EventType.FINGERING -> FingeringEdit(scale)
        EventType.FOOTER_LEFT -> TextEdit(scale)
        EventType.FOOTER_RIGHT -> TextEdit(scale)
        EventType.GLISSANDO -> GlissandoEdit(scale)
        EventType.HARMONY -> HarmonyEdit(scale)
        EventType.KEY_SIGNATURE -> KeySignatureEdit()
        EventType.LYRICIST -> TextEdit(scale)
        EventType.LYRIC -> LyricEdit(scale)
        EventType.NAVIGATION -> NavigationEdit(scale)
        EventType.OCTAVE -> OctaveEdit(scale)
        EventType.ORNAMENT -> OrnamentEdit()
        EventType.PART -> InstrumentEdit()
        EventType.PAUSE -> PauseEdit(scale)
        EventType.PEDAL -> PedalEdit(scale)
        EventType.REHEARSAL_MARK -> TextEdit(scale)
        EventType.SLUR -> SlurEdit(scale)
        EventType.STAVE_JOIN -> StaveJoinEdit()
        EventType.SUBTITLE -> TextEdit(scale)
        EventType.TEMPO -> TempoEdit(scale)
        EventType.TEMPO_TEXT -> TextEdit(scale)
        EventType.TIE -> DeleteOnlyEdit()
        EventType.TIME_SIGNATURE -> TimeSignatureEdit()
        EventType.TITLE -> TextEdit(scale)
        EventType.WEDGE -> WedgeEdit(scale)
        else -> {
          DefaultEdit(scale)
        }
      }
    }
  }
}