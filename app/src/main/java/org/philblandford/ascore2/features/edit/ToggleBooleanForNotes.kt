package org.philblandford.ascore2.features.edit

import com.philblandford.kscore.engine.types.EventParam
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer

interface ToggleBooleanForNotes {
  operator fun invoke(param:EventParam, default:Boolean = false)
}