package org.philblandford.ascore2.features.clipboard.usecases

import com.philblandford.kscore.api.Location

interface SetEndSelection {
  operator fun invoke(location: Location):Result<Unit>
}