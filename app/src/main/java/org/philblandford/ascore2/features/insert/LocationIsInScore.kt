package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.Location

interface LocationIsInScore {
  operator fun invoke(location: Location):Boolean
}