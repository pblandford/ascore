package org.philblandford.ascore2.features.ui.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class GetHelpKeyImpl(private val uiStateRepository: UiStateRepository) : GetHelpKey {
    override fun invoke(): StateFlow<String?> {
        return uiStateRepository.getHelpKey()
    }
}