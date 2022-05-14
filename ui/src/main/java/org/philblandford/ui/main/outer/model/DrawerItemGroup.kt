package org.philblandford.ui.main.outer.model

import org.philblandford.ascore2.features.ui.model.LayoutID

data class DrawerItemGroup(val nameId: Int, val items: List<DrawerItem>)
data class DrawerItem(
  val nameId: Int,
  val layoutID: LayoutID = LayoutID.NEW_SCORE,
  val requiresPurchase: Boolean = false
)