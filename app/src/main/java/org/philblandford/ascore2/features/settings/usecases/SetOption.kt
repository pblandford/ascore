package org.philblandford.ascore2.features.settings.usecases

import com.philblandford.kscore.engine.types.EventParam

interface SetOption {
  operator fun <T>invoke(eventParam: EventParam, option:T?)
}