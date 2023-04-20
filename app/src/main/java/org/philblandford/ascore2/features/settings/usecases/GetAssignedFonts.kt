package org.philblandford.ascore2.features.settings.usecases

import com.philblandford.kscore.engine.core.area.factory.TextType

interface GetAssignedFonts {
    operator fun invoke():Map<TextType, String>
}