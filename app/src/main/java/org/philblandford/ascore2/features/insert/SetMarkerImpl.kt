package org.philblandford.ascore2.features.insert

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.Location
import org.philblandford.ascore2.util.ok
import org.philblandford.ascore2.util.tryResult

class SetMarkerImpl(private val kScore: KScore) : SetMarker {
  override operator fun invoke(location:Location):Result<Unit> {
    return tryResult {
      kScore.setMarker(location).ok()
    }
  }
}