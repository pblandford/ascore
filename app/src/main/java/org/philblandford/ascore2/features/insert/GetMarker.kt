package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.engine.types.EventAddress

interface GetMarker {
  operator fun invoke():EventAddress?
}