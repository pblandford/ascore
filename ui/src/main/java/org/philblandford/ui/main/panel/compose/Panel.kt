package org.philblandford.ui.main.panel.compose

import androidx.compose.runtime.Composable
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ui.insert.items.articulation.compose.ArticulationInsert
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.main.panel.viewmodels.PanelModel
import org.philblandford.ui.main.panel.viewmodels.PanelViewModel
import org.philblandford.ui.insert.compose.InsertChoosePanel
import org.philblandford.ui.insert.items.bars.compose.BarInsert
import org.philblandford.ui.insert.items.clef.compose.ClefInsert
import org.philblandford.ui.insert.items.harmony.compose.HarmonyInsert
import org.philblandford.ui.insert.items.keysignature.compose.KeySignatureInsert
import org.philblandford.ui.insert.items.lyric.compose.LyricInsert
import org.philblandford.ui.insert.items.ornament.compose.OrnamentInsert
import org.philblandford.ui.insert.items.tempo.compose.TempoInsert
import org.philblandford.ui.insert.items.text.compose.TextInsert
import org.philblandford.ui.insert.items.tie.compose.TieInsert
import org.philblandford.ui.insert.items.timesignature.compose.TimeSignatureInsert
import org.philblandford.ui.insert.items.tuplet.compose.TupletInsert
import org.philblandford.ui.keyboard.compose.KeyboardPanel
import org.philblandford.ui.util.ThemeBox

@Composable
fun Panel() {
  VMView(PanelViewModel::class.java) { state, _, _ ->
    PanelInternal(state)
  }
}

@Composable
private fun PanelInternal(model:PanelModel) {
  ThemeBox {
    when (model.layoutID) {
      LayoutID.ARTICULATION -> ArticulationInsert()
      LayoutID.BAR -> BarInsert()
      LayoutID.CLEF -> ClefInsert()
      LayoutID.HARMONY -> HarmonyInsert()
      LayoutID.INSERT_CHOOSE -> InsertChoosePanel()
      LayoutID.KEY -> KeySignatureInsert()
      LayoutID.KEYBOARD -> KeyboardPanel()
      LayoutID.LYRIC -> LyricInsert()
      LayoutID.ORNAMENT -> OrnamentInsert()
      LayoutID.TEMPO -> TempoInsert()
      LayoutID.TEXT -> TextInsert()
      LayoutID.TIE -> TieInsert()
      LayoutID.TIME -> TimeSignatureInsert()
      LayoutID.TUPLET -> TupletInsert()
      else -> {}
    }
  }
}