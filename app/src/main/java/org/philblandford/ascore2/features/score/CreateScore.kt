package org.philblandford.ascore2.features.score

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.NewScoreDescriptor

interface CreateScore {
  operator fun invoke(newScoreDescriptor: NewScoreDescriptor):Result<Unit>
}