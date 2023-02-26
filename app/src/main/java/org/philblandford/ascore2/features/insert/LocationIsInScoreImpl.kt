package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.Location

class LocationIsInScoreImpl(private val kScore: KScore) : LocationIsInScore {
  override operator fun invoke(location: Location):Boolean {
    return kScore.getEventAddress(location) != null
  }
}