package org.philblandford.ascore2.features.clipboard.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.Location
import com.philblandford.kscore.engine.types.EventAddress

class GetLocationImpl(private val kScore: KScore) : GetLocation {

  override fun invoke(eventAddress: EventAddress): Location? {
    return kScore.getLocation(eventAddress)
  }
}