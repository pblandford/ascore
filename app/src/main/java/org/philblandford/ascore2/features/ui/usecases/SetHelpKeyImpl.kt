package org.philblandford.ascore2.features.ui.usecases

import org.philblandford.ascore2.features.ui.repository.UiStateRepository

class SetHelpKeyImpl(private val uiStateRepository: UiStateRepository) : SetHelpKey {

    override fun invoke(key: String?) {
        uiStateRepository.setHelpKey(key)
    }
}