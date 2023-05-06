package org.philblandford.ui.insert.items.text.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.engine.core.area.factory.TextType
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.clipboard.usecases.GetSelection
import org.philblandford.ascore2.features.settings.repository.SettingsRepository
import org.philblandford.ui.insert.common.viewmodel.DefaultInsertViewModel

class TextInsertViewModel(
  private val settingsRepository: SettingsRepository,
  private val getSelection: GetSelection
) : DefaultInsertViewModel() {

  init {
    viewModelScope.launch {
      getSelection().collectLatest { selection ->
        getInsertItem()?.let { item ->
          if (listOf(
              EventType.TEMPO_TEXT, EventType.EXPRESSION_TEXT, EventType.REHEARSAL_MARK,
              EventType.EXPRESSION_DASH
            ).contains(item.eventType)
          ) {
            if (selection != null) {
              setEventType(EventType.EXPRESSION_DASH)
            } else {
              setEventType(EventType.EXPRESSION_TEXT)
            }
          }
        }
      }
    }
  }

  override fun setEventType(eventType: EventType) {
    val textType = TextType.fromEventType(eventType)

    settingsRepository.getFonts()[textType]?.let { font ->
      setParam(EventParam.FONT, font)
    }
    super.setEventType(eventType)
  }
}