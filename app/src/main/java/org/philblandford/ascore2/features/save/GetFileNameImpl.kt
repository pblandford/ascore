package org.philblandford.ascore2.features.save

import com.philblandford.kscore.api.KScore

class GetFileNameImpl(private val kScore: KScore) : GetFileName {
  override fun invoke(): String? {
    return kScore.getCurrentFilename()
  }
}