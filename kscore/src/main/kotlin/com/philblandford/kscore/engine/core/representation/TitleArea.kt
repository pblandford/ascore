package com.philblandford.kscore.engine.core.representation

import com.philblandford.kscore.engine.core.LayoutDescriptor
import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.TextArgs
import com.philblandford.kscore.engine.core.area.factory.TextType
import com.philblandford.kscore.engine.core.score.value
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.util.black

private fun yPosition(metaType: MetaType): Int = when (metaType) {
  MetaType.TITLE -> BLOCK_HEIGHT * 6
  MetaType.COMPOSER -> BLOCK_HEIGHT * 14
  MetaType.SUBTITLE -> BLOCK_HEIGHT * 16
  MetaType.LYRICIST -> BLOCK_HEIGHT * 20
  else -> 0
}

fun DrawableFactory.titleArea(
  scoreQuery: ScoreQuery, layoutDescriptor: LayoutDescriptor,
  partName: String?
): Area {
  var area = Area(
    layoutDescriptor.titleWidth, layoutDescriptor.titleHeight,
    tag = "TitleArea"
  )

  area = addTitle(scoreQuery.getEvent(EventType.TITLE), area, layoutDescriptor)
  area = addInstrument(partName, area)
  area = addSubTitle(scoreQuery.getEvent(EventType.SUBTITLE), area, layoutDescriptor)
  area = addComposer(scoreQuery.getEvent(EventType.COMPOSER), area, layoutDescriptor)
  area = addLyricist(scoreQuery.getEvent(EventType.LYRICIST), area, layoutDescriptor)
  return area
}

private fun DrawableFactory.addTitle(
  event: Event?,
  area: Area,
  layoutDescriptor: LayoutDescriptor
): Area {
  return getTextArea(event)?.let { textArea ->
    val xPos = layoutDescriptor.titleWidth / 2 - textArea.width / 2
    val yPos = yPosition(MetaType.TITLE)
    val coord = Coord(xPos, yPos).plus(event?.getParam<Coord>(EventParam.HARD_START) ?: Coord())
    area.addArea(
      textArea.copy(
        tag = "Title", event = event, extra = MetaType.TITLE,
        addressRequirement = AddressRequirement.EVENT
      ),
      coord
    )
  } ?: area
}

private fun DrawableFactory.addSubTitle(
  event: Event?,
  area: Area,
  layoutDescriptor: LayoutDescriptor
): Area {
  return getTextArea(event)?.let { textArea ->
    val xPos = layoutDescriptor.titleWidth / 2 - textArea.width / 2
    val yPos = yPosition(MetaType.SUBTITLE)
    val coord = Coord(xPos, yPos).plus(event?.getParam<Coord>(EventParam.HARD_START) ?: Coord())

    area.addArea(
      textArea.copy(
        tag = "Subtitle",
        event = event,
        extra = MetaType.SUBTITLE,
        addressRequirement = AddressRequirement.EVENT
      ),
      coord
    )
  } ?: area
}

private fun DrawableFactory.addComposer(
  event: Event?,
  area: Area,
  layoutDescriptor: LayoutDescriptor
): Area {
  return getTextArea(event)?.let { textArea ->
    val xPos = layoutDescriptor.titleWidth - textArea.width - BLOCK_HEIGHT * 2
    val yPos = yPosition(MetaType.COMPOSER)
    val coord = Coord(xPos, yPos).plus(event?.getParam<Coord>(EventParam.HARD_START) ?: Coord())

    area.addArea(
      textArea.copy(
        tag = "Composer",
        event = event,
        extra = MetaType.COMPOSER,
        addressRequirement = AddressRequirement.EVENT
      ),
      coord
    )
  } ?: area
}

private fun DrawableFactory.addLyricist(
  event: Event?,
  area: Area,
  layoutDescriptor: LayoutDescriptor
): Area {
  return getTextArea(event)?.let { textArea ->
    val xPos = layoutDescriptor.titleWidth - textArea.width - BLOCK_HEIGHT * 2
    val yPos = yPosition(MetaType.LYRICIST)
    val coord = Coord(xPos, yPos).plus(event?.getParam<Coord>(EventParam.HARD_START) ?: Coord())

    area.addArea(
      textArea.copy(
        tag = "Lyricist",
        event = event,
        extra = MetaType.LYRICIST,
        addressRequirement = AddressRequirement.EVENT
      ),
      coord
    )
  } ?: area
}

private fun DrawableFactory.addInstrument(
  partName: String?,
  area: Area
): Area {
  return partName?.let {
    getDrawableArea(TextArgs(partName))?.let { textArea ->
      val xPos = BLOCK_HEIGHT * 3
      val yPos = BLOCK_HEIGHT * 10

      area.addArea(
        textArea.copy(tag = "PartName", addressRequirement = AddressRequirement.EVENT),
        Coord(xPos, yPos)
      )
    }
  } ?: area
}

private fun Event.text() = params.g<String>(EventParam.TEXT) ?: ""
private fun Event.font() = params.g<String>(EventParam.FONT) ?: ""
private fun Event.size():Int {
  return params.g<Int>(EventParam.TEXT_SIZE) ?: MetaType.valueOf(eventType.toString()).textSize()
}
private fun DrawableFactory.getTextArea(event: Event?): Area? {
  return event?.let {
    getDrawableArea(
      TextArgs(
        event.text(), black(), event.size(), event.getTextType(),
        event.font().value()
      )
    )
  }
}

private fun Event.getTextType(): TextType {
  return when (eventType) {
    EventType.TITLE -> TextType.TITLE
    EventType.SUBTITLE -> TextType.SUBTITLE
    EventType.COMPOSER -> TextType.COMPOSER
    EventType.LYRICIST -> TextType.COMPOSER
    else -> TextType.TITLE
  }
}