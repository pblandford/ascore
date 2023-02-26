package org.philblandford.ascore2.features.harmony

import com.philblandford.kscore.engine.pitch.Harmony
import com.philblandford.kscore.engine.types.Pitch

interface GetHarmoniesForKey {
  operator fun invoke():List<Harmony>
}