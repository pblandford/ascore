package org.philblandford.ascore2.features.settings.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventParam

class GetOptionImpl(private val kScore: KScore) : GetOption {
  override fun <T> invoke(option: EventParam): T? {
    return kScore.getOption(option)
  }
}