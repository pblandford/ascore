package org.philblandford.ascore2.features.settings.usecases

import com.philblandford.kscore.engine.types.EventParam

interface GetOption {
  operator fun<T> invoke(option:EventParam):T?
}