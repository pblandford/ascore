package org.philblandford.ascore2.features.harmony

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.pitch.Harmony

class GetHarmoniesForKeyImpl(private val kScore: KScore) : GetHarmoniesForKey {
  override fun invoke(): List<Harmony> {
    return kScore.getHarmoniesForKey()
  }
}