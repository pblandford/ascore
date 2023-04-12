package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.select.AreaToShow

class GetSelectedAreaImpl(private val kScore: KScore) : GetSelectedArea {

  override fun invoke(): AreaToShow? {
    return kScore.getSelectedArea()
    }

}