package org.philblandford.ascore2.features.ui.usecases

import android.text.Layout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.philblandford.ascore2.features.ui.model.LayoutID

interface GetPanelLayout {
  operator fun invoke(): StateFlow<LayoutID>
}