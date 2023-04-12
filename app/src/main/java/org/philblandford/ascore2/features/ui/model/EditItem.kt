package org.philblandford.ascore2.features.ui.model

import com.philblandford.kscore.api.Location
import com.philblandford.kscore.api.Rectangle
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventAddress


data class EditItem(
  val event: Event,
  val address: EventAddress,
  val page:Int,
  val rectangle: Rectangle
)
