package org.philblandford.ascore2.features.settings.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventParam

class SetOptionImpl(private val kScore: KScore) : SetOption {

  override fun <T> invoke(eventParam: EventParam, option: T?) {
    kScore.setOption(eventParam, option)
  }
}