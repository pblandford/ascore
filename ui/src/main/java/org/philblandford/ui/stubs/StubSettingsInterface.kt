package org.philblandford.ui.stubs

import com.godaddy.android.colorpicker.HsvColor
import com.philblandford.kscore.engine.core.area.factory.TextType
import kotlinx.coroutines.flow.Flow
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.settings.viewmodel.SettingsInterface

class StubSettingsInterface : SettingsInterface{
    override fun reset() {
        TODO("Not yet implemented")
    }

    override fun getSideEffects(): Flow<VMSideEffect> {
        TODO("Not yet implemented")
    }

    override fun setBackgroundColor(color: HsvColor) {
        TODO("Not yet implemented")
    }

    override fun setForegroundColor(color: HsvColor) {
        TODO("Not yet implemented")
    }

    override fun setFont(textType: TextType, font: String) {
        TODO("Not yet implemented")
    }
}