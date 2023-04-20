package org.philblandford.ascore2.features.ui.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface GetHelpKey {
    operator fun invoke(): StateFlow<String?>
}