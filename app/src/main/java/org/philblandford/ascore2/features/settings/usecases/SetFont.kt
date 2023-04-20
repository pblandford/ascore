package org.philblandford.ascore2.features.settings.usecases

import com.philblandford.kscore.engine.core.area.factory.TextType

interface SetFont {
    operator fun invoke(textType: TextType, font:String)
}