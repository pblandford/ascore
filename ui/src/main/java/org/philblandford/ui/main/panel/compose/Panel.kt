package org.philblandford.ui.main.panel.compose

import androidx.compose.runtime.Composable
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ui.insert.items.articulation.compose.ArticulationInsert
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.main.panel.viewmodels.PanelModel
import org.philblandford.ui.main.panel.viewmodels.PanelViewModel
import org.philblandford.ui.insert.compose.InsertChoosePanel
import org.philblandford.ui.insert.items.tie.compose.TieInsert
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
      LayoutID.KEYBOARD -> KeyboardPanel()
      LayoutID.INSERT_CHOOSE -> InsertChoosePanel()
      LayoutID.ARTICULATION -> ArticulationInsert()
      LayoutID.TUPLET -> TupletInsert()
      LayoutID.TIE -> TieInsert()
      else -> {}
    }
  }
}