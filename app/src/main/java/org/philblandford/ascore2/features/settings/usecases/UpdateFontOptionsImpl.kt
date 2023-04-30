package org.philblandford.ascore2.features.settings.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.core.area.factory.TextType
import com.philblandford.kscore.engine.types.EventParam

class UpdateFontOptionsImpl(private val kScore: KScore,
private val getAssignedFonts: GetAssignedFonts) : UpdateFontOptions {

  override fun invoke() {
    listOf(TextType.LYRIC, TextType.HARMONY).forEach { textType ->
      getOption(textType)?.let {
        getAssignedFonts()[textType]?.let { font ->
          kScore.setOption(it, font)
        }
      }
    }
  }

  private fun getOption(textType: TextType): EventParam? {
    return when (textType) {
      TextType.LYRIC -> EventParam.OPTION_LYRIC_FONT
      TextType.HARMONY -> EventParam.OPTION_HARMONY_FONT
      else -> null
    }
  }
}