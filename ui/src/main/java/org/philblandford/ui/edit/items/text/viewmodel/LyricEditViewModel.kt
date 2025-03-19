package org.philblandford.ui.edit.items.text.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.option.getOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.edit.MoveSelectedArea
import org.philblandford.ascore2.features.input.usecases.DeleteSelectedEvent
import org.philblandford.ascore2.features.insert.GetDefaultTextSize
import org.philblandford.ascore2.features.insert.InsertEvent
import org.philblandford.ascore2.features.insert.UpdateEventParam
import org.philblandford.ascore2.features.settings.usecases.GetAvailableFonts
import org.philblandford.ascore2.features.settings.usecases.GetOption
import org.philblandford.ascore2.features.settings.usecases.SetOption
import org.philblandford.ascore2.features.ui.model.UIState
import org.philblandford.ascore2.features.ui.usecases.GetUIState

interface LyricEditInterface : TextEditInterface {
    val upDownFlow: MutableStateFlow<Boolean>
    fun toggleUpDown()
}

class LyricEditViewModel(
    private val getUIState: GetUIState,
    updateEvent: UpdateEventParam,
    insertEvent: InsertEvent,
    deleteSelectedEvent: DeleteSelectedEvent,
    moveSelectedArea: MoveSelectedArea,
    getFonts: GetAvailableFonts,
    getDefaultTextSize: GetDefaultTextSize,
    private val getOptionUC: GetOption,
    private val setOptionUC: SetOption
) : TextEditViewModel(
    getUIState,
    updateEvent,
    insertEvent,
    deleteSelectedEvent,
    moveSelectedArea,
    getFonts,
    getDefaultTextSize
), LyricEditInterface {
    private val _initialUp: Boolean
        get() {
            val num = (getUIState().value as? UIState.Edit)?.editItem?.event?.number(1) ?: 1
            return getOptionUC<List<Pair<Int, Boolean>>>(EventParam.OPTION_LYRIC_POSITIONS)?.toMap()
                ?.get(num) ?: false
        }
    override val upDownFlow = MutableStateFlow(_initialUp)

    override fun toggleUpDown() {
        getState().value?.editItem?.let { item ->
            val number = item.event.number(1)
            val up =
                getOptionUC<List<Pair<Int, Boolean>>>(EventParam.OPTION_LYRIC_POSITIONS)?.toMap()
                    ?.get(number) ?: false
            viewModelScope.launch {
                upDownFlow.emit(!up)
            }
            setOptionUC(EventParam.OPTION_LYRIC_POSITIONS, number to !up)
        }
    }
}