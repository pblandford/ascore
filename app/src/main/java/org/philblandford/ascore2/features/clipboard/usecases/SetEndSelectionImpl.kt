package org.philblandford.ascore2.features.clipboard.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.Location
import org.philblandford.ascore2.util.ok
import org.philblandford.ascore2.util.tryResult

class SetEndSelectionImpl(private val kScore: KScore) : SetEndSelection {
  override operator fun invoke(location: Location):Result<Unit> {
    return tryResult {
      kScore.setEndSelection(location).ok()
    }
  }
}