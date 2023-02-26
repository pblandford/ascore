package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.Location

interface SetMarker {
  operator fun invoke(location:Location):Result<Unit>
}