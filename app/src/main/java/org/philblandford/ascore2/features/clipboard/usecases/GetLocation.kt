package org.philblandford.ascore2.features.clipboard.usecases

import com.philblandford.kscore.api.Location
import com.philblandford.kscore.engine.types.EventAddress

interface GetLocation {
  operator fun invoke(eventAddress: EventAddress):Location?
}