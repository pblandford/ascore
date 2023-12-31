package org.philblandford.ascore2.external.export.mxml.out.creator

import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.option.getOption
import org.philblandford.ascore2.external.export.mxml.out.MxmlBottomMargin
import org.philblandford.ascore2.external.export.mxml.out.MxmlDefaults
import org.philblandford.ascore2.external.export.mxml.out.MxmlLeftMargin
import org.philblandford.ascore2.external.export.mxml.out.MxmlMillimeters
import org.philblandford.ascore2.external.export.mxml.out.MxmlPageHeight
import org.philblandford.ascore2.external.export.mxml.out.MxmlPageLayout
import org.philblandford.ascore2.external.export.mxml.out.MxmlPageMargins
import org.philblandford.ascore2.external.export.mxml.out.MxmlPageWidth
import org.philblandford.ascore2.external.export.mxml.out.MxmlRightMargin
import org.philblandford.ascore2.external.export.mxml.out.MxmlScaling
import org.philblandford.ascore2.external.export.mxml.out.MxmlStaffDistance
import org.philblandford.ascore2.external.export.mxml.out.MxmlStaffLayout
import org.philblandford.ascore2.external.export.mxml.out.MxmlSystemDistance
import org.philblandford.ascore2.external.export.mxml.out.MxmlSystemLayout
import org.philblandford.ascore2.external.export.mxml.out.MxmlTenths
import org.philblandford.ascore2.external.export.mxml.out.MxmlTopMargin

internal fun Score.createDefaults(): MxmlDefaults {
  return MxmlDefaults(
    scaling = createScaling(),
    pageLayout = createPageLayout(),
    systemLayout = createSystemLayout(),
    staffLayout = null// createStaffLayout(score)
  )
}

private fun createScaling(): MxmlScaling {
  return MxmlScaling(MxmlMillimeters(7f), MxmlTenths(40))
}

private fun Score.createPageLayout(): MxmlPageLayout {
  val width = getOption<Int>(EventParam.LAYOUT_PAGE_WIDTH, this)
  val height = getOption<Int>(EventParam.LAYOUT_PAGE_HEIGHT, this)

  return MxmlPageLayout(
    MxmlPageHeight(height.toTenths()),
    MxmlPageWidth(width.toTenths()),
    listOf(createPageMargin())
  )
}

private fun Score.createPageMargin(): MxmlPageMargins {
  val left = getOption<Int>(EventParam.LAYOUT_LEFT_MARGIN, this)
  val right = getOption<Int>(EventParam.LAYOUT_RIGHT_MARGIN, this)
  val top = getOption<Int>(EventParam.LAYOUT_TOP_MARGIN, this)
  val bottom = getOption<Int>(EventParam.LAYOUT_BOTTOM_MARGIN, this)
  return MxmlPageMargins(
    "both",
    MxmlLeftMargin(left.toTenths()),
    MxmlRightMargin(right.toTenths()),
    MxmlTopMargin(top.toTenths()),
    MxmlBottomMargin(bottom.toTenths())
  )
}

private fun Score.createSystemLayout(): MxmlSystemLayout {
  val systemGap = getOption<Int>(EventParam.LAYOUT_SYSTEM_GAP, this)
  return MxmlSystemLayout(null, MxmlSystemDistance(systemGap.toTenths()), null)
}

private fun createStaffLayout(score: Score): MxmlStaffLayout {
  val staffGap = getOption<Int>(EventParam.LAYOUT_STAVE_GAP, score)
  return MxmlStaffLayout(MxmlStaffDistance(staffGap.toTenths()))
}

private val ratio = (BLOCK_HEIGHT * 2).toFloat() / 10
private fun Int.toTenths(): Float {
  return (toFloat() / ratio)
}