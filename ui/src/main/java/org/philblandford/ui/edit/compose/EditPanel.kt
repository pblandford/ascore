package org.philblandford.ui.edit.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventType
import org.philblandford.ui.edit.items.clef.compose.ClefEdit
import org.philblandford.ui.edit.items.dynamics.compose.DynamicsEdit
import org.philblandford.ui.edit.items.tempo.compose.TempoEdit
import org.philblandford.ui.edit.items.text.compose.LyricEdit
import org.philblandford.ui.edit.items.text.compose.TextEdit
import org.philblandford.ui.edit.items.wedge.compose.WedgeEdit


@Composable
fun EditPanel(modifier: Modifier, type: EventType, scale: Float) {
    Box(modifier.background(MaterialTheme.colors.surface)) {
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.onSurface) {

      when (type) {
        EventType.CLEF -> ClefEdit()
        EventType.COMPOSER -> TextEdit(scale)
        EventType.DYNAMIC -> DynamicsEdit(scale)
        EventType.EXPRESSION_TEXT -> TextEdit(scale)
        EventType.LYRICIST -> TextEdit(scale)
        EventType.LYRIC -> LyricEdit(scale)
        EventType.REHEARSAL_MARK -> TextEdit(scale)
        EventType.SUBTITLE -> TextEdit(scale)
        EventType.TEMPO -> TempoEdit(scale)
        EventType.TEMPO_TEXT -> TextEdit(scale)
        EventType.TITLE -> TextEdit(scale)
        EventType.WEDGE -> WedgeEdit(scale)
        else -> {
          DefaultEdit(scale)
        }
      }
    }
  }
}