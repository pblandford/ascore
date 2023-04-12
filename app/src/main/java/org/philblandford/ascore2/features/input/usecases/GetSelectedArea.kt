package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.select.AreaToShow

interface GetSelectedArea {
  operator fun invoke(): AreaToShow?
}