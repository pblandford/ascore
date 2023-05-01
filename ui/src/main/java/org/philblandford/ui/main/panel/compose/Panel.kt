package org.philblandford.ui.main.panel.compose

import TransposeBy
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.createfromtemplate.compose.CreateFromTemplate
import org.philblandford.ui.input.compose.percussion.PercussionInputPanel
import org.philblandford.ui.insert.choose.compose.InsertChoosePanel
import org.philblandford.ui.insert.items.articulation.compose.ArticulationInsert
import org.philblandford.ui.insert.items.barline.compose.BarLineInsert
import org.philblandford.ui.insert.items.barnumbering.compose.BarNumberingInsert
import org.philblandford.ui.insert.items.bars.compose.BarInsert
import org.philblandford.ui.insert.items.bowing.compose.BowingInsert
import org.philblandford.ui.insert.items.clef.compose.ClefInsert
import org.philblandford.ui.insert.items.dynamic.compose.DynamicInsert
import org.philblandford.ui.insert.items.fingering.compose.FingeringInsert
import org.philblandford.ui.insert.items.glissando.compose.GlissandoInsert
import org.philblandford.ui.insert.items.groupstaves.compose.GroupStavesInsert
import org.philblandford.ui.insert.items.harmony.compose.HarmonyInsert
import org.philblandford.ui.insert.items.instrument.compose.InstrumentInsert
import org.philblandford.ui.insert.items.keysignature.compose.KeySignatureInsert
import org.philblandford.ui.insert.items.lyric.compose.LyricInsert
import org.philblandford.ui.insert.items.meta.compose.MetaInsert
import org.philblandford.ui.insert.items.navigation.compose.NavigationInsert
import org.philblandford.ui.insert.items.octave.compose.OctaveInsert
import org.philblandford.ui.insert.items.octave.compose.WedgeInsert
import org.philblandford.ui.insert.items.ornament.compose.OrnamentInsert
import org.philblandford.ui.insert.items.pagemargins.compose.PageMargins
import org.philblandford.ui.insert.items.pagesize.compose.PageSize
import org.philblandford.ui.insert.items.pause.compose.PauseInsert
import org.philblandford.ui.insert.items.pedal.compose.PedalInsert
import org.philblandford.ui.insert.items.repeatbar.compose.RepeatBarInsert
import org.philblandford.ui.insert.items.scorebreak.compose.ScoreBreak
import org.philblandford.ui.insert.items.segmentwidth.compose.SegmentWidth
import org.philblandford.ui.insert.items.slur.compose.SlurInsert
import org.philblandford.ui.insert.items.tempo.compose.TempoInsert
import org.philblandford.ui.insert.items.text.compose.TextInsert
import org.philblandford.ui.insert.items.tie.compose.TieInsert
import org.philblandford.ui.insert.items.timesignature.compose.TimeSignatureInsert
import org.philblandford.ui.insert.items.transposeto.compose.TransposeTo
import org.philblandford.ui.insert.items.tremolo.compose.TremoloInsert
import org.philblandford.ui.insert.items.tuplet.compose.TupletInsert
import org.philblandford.ui.insert.items.volta.compose.VoltaInsert
import org.philblandford.ui.keyboard.compose.KeyboardPanel
import org.philblandford.ui.main.panel.viewmodels.PanelModel
import org.philblandford.ui.main.panel.viewmodels.PanelViewModel
import org.philblandford.ui.util.ThemeBox
import timber.log.Timber

@Composable
fun Panel() {
  VMView(PanelViewModel::class.java) { state, _, _ ->
    PanelInternal(state)
  }
}

@Composable
private fun PanelInternal(model: PanelModel) {
  var currentLayout by remember { mutableStateOf(model.layoutID) }

  currentLayout = model.layoutID

  Timber.e("Panel currentLayout $currentLayout")

  AnimatedContent(currentLayout,
  transitionSpec =
  {
    (slideInVertically { height -> height } + fadeIn() with
          slideOutHorizontally { height -> -height } + fadeOut()).using(
      SizeTransform(clip = false)
    )
  }
    ) { id ->

    ThemeBox {
      when (id) {
        LayoutID.ARTICULATION -> ArticulationInsert()
        LayoutID.BAR -> BarInsert()
        LayoutID.BARLINE -> BarLineInsert()
        LayoutID.BAR_NUMBERING -> BarNumberingInsert()
        LayoutID.BOWING -> BowingInsert()
        LayoutID.CLEF -> ClefInsert()
        LayoutID.DYNAMIC -> DynamicInsert()
        LayoutID.FINGERING -> FingeringInsert()
        LayoutID.GLISSANDO -> GlissandoInsert()
        LayoutID.GROUP_STAVES -> GroupStavesInsert()
        LayoutID.HARMONY -> HarmonyInsert()
        LayoutID.INSERT_CHOOSE -> InsertChoosePanel()
        LayoutID.INSTRUMENT -> InstrumentInsert()
        LayoutID.KEY -> KeySignatureInsert()
        LayoutID.KEYBOARD -> KeyboardPanel()
        LayoutID.LYRIC -> LyricInsert()
        LayoutID.MARGIN -> PageMargins()
        LayoutID.METADATA -> MetaInsert()
        LayoutID.NAVIGATION -> NavigationInsert()
        LayoutID.OCTAVE -> OctaveInsert()
        LayoutID.ORNAMENT -> OrnamentInsert()
        LayoutID.PAGE_SIZE -> PageSize()
        LayoutID.PAUSE -> PauseInsert()
        LayoutID.PEDAL -> PedalInsert()
        LayoutID.PERCUSSION -> PercussionInputPanel()
        LayoutID.REPEAT_BAR -> RepeatBarInsert()
        LayoutID.SCORE_BREAK -> ScoreBreak()
        LayoutID.SEGMENT_WIDTH -> SegmentWidth()
        LayoutID.SLUR -> SlurInsert()
        LayoutID.TEMPO -> TempoInsert()
        LayoutID.TEXT -> TextInsert()
        LayoutID.TIE -> TieInsert()
        LayoutID.TIME -> TimeSignatureInsert()
        LayoutID.TRANSPOSE_BY -> TransposeBy()
        LayoutID.TRANSPOSE_TO -> TransposeTo()
        LayoutID.TREMOLO -> TremoloInsert()
        LayoutID.TUPLET -> TupletInsert()
        LayoutID.VOLTA -> VoltaInsert()
        LayoutID.WEDGE -> WedgeInsert()
        else -> {}
      }
    }
  }
}