package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.types.Event

object WedgeDecorator : LineDecorator {

  override fun DrawableFactory.getArea(event: Event, width: Int, lineDescriptor: LineDescriptor): Area? {
    return wedgeArea(event, width)
  }

  override fun DrawableFactory.getArea(event: Event): Area? {
    return null
  }
}