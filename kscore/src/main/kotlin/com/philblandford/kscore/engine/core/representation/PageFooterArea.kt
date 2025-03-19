package com.philblandford.kscore.engine.core.representation

import com.philblandford.kscore.engine.core.LayoutDescriptor
import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.TextArgs
import com.philblandford.kscore.engine.core.area.factory.TextType
import com.philblandford.kscore.engine.core.areadirectory.header.numberArea
import com.philblandford.kscore.engine.core.score.value
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.MetaType
import com.philblandford.kscore.engine.types.PageSize
import com.philblandford.kscore.engine.types.ScoreQuery
import com.philblandford.kscore.engine.types.ez
import com.philblandford.kscore.engine.types.g
import com.philblandford.kscore.engine.util.black

internal fun DrawableFactory.pageFooterArea(
    num: Int,
    scoreQuery: ScoreQuery,
    layoutDescriptor: LayoutDescriptor
): Area {
    val (width, height) = layoutDescriptor.pageWidth to BLOCK_HEIGHT * 4
    var area = Area(width, height, tag = "PageFooter")


    area = addFooterText(scoreQuery.getEvent(EventType.FOOTER_LEFT), area, layoutDescriptor)
    area = addFooterText(scoreQuery.getEvent(EventType.FOOTER_CENTER), area, layoutDescriptor)
    area = addFooterText(scoreQuery.getEvent(EventType.FOOTER_RIGHT), area, layoutDescriptor)
    val numberHeight = (PAGE_NUMBER_HEIGHT * (layoutDescriptor.pageWidth.toFloat() / pageWidths[PageSize.A4]!!)).toInt()
    numberArea(num, numberHeight)?.let {
        area = area.addBelow(
            it.copy(tag = "PageNumber-$num"),
            x = width / 2 - it.width / 2
        )
    }
    return area
}

private fun DrawableFactory.addFooterText(
    event: Event?,
    area: Area,
    layoutDescriptor: LayoutDescriptor
): Area {
    return getTextArea(event)?.let { textArea ->

        val (xPos, label) = if (event?.eventType == EventType.FOOTER_LEFT) {
            BLOCK_HEIGHT * 6 to "FooterLeft"
        } else if (event?.eventType == EventType.FOOTER_CENTER) {
            layoutDescriptor.pageWidth / 2 - textArea.width / 2 to "FooterCenter"
        } else {
            layoutDescriptor.titleWidth - BLOCK_HEIGHT * 2 - textArea.width to "FooterRight"
        }
        val yPos = BLOCK_HEIGHT
        val coord = Coord(xPos, yPos).plus(event?.getParam<Coord>(EventParam.HARD_START) ?: Coord())
        area.addArea(
            textArea.copy(
                tag = label, event = event, extra = MetaType.TITLE,
                addressRequirement = AddressRequirement.EVENT
            ),
            coord
        )
    } ?: area
}


private fun Event.text() = params.g<String>(EventParam.TEXT) ?: ""
private fun Event.font() = params.g<String>(EventParam.FONT) ?: ""
private fun Event.size(): Int {
    return params.g<Int>(EventParam.TEXT_SIZE) ?: MetaType.valueOf(eventType.toString()).textSize()
}

private fun DrawableFactory.getTextArea(event: Event?): Area? {
    return event?.let {
        getDrawableArea(
            TextArgs(
                event.text(), black(), event.size(), TextType.COMPOSER,
                event.font().value()
            )
        )
    }
}
