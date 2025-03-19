package com.philblandford.kscore.engine.core.score

import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.representation.COMPOSER_TEXT_SIZE
import com.philblandford.kscore.engine.core.representation.SUBTITLE_TEXT_SIZE
import com.philblandford.kscore.engine.core.representation.TEXT_SIZE
import com.philblandford.kscore.engine.core.representation.TITLE_TEXT_SIZE
import com.philblandford.kscore.engine.types.*

fun String.value() = if (isEmpty()) null else this

data class MetaSection(
    val text: String = "", val size: Int = TEXT_SIZE,
    val font: String? = "", val offset: Coord? = Coord()
)

data class Meta(
    val sections: Map<MetaType, MetaSection> = mapOf(
        MetaType.TITLE to MetaSection(size = TITLE_TEXT_SIZE),
        MetaType.SUBTITLE to MetaSection(size = SUBTITLE_TEXT_SIZE),
        MetaType.COMPOSER to MetaSection(size = COMPOSER_TEXT_SIZE),
        MetaType.LYRICIST to MetaSection(size = COMPOSER_TEXT_SIZE)
    ),
    val fileName: String = ""
) {
    fun toEvent(): Event {
        return Event(
            EventType.META,
            paramMapOf(
                EventParam.SECTIONS to this
            )
        )
    }

    fun getSection(metaType: MetaType): MetaSection {
        return sections[metaType] ?: MetaSection()
    }

    fun setParam(metaType: MetaType, doIt: (MetaSection) -> MetaSection): Meta {
        val section = doIt(getSection(metaType))
        return copy(sections = sections.plus(metaType to section))
    }

    fun setText(metaType: MetaType, text: String): Meta {
        return setParam(metaType) { it.copy(text = text) }
    }

    fun setSize(metaType: MetaType, size: Int): Meta {
        return setParam(metaType) { it.copy(size = size) }
    }

    fun setOffset(metaType: MetaType, offset: Coord?): Meta {
        return setParam(metaType) { it.copy(offset = offset) }
    }

    fun setFont(metaType: MetaType, font: String): Meta {
        return setParam(metaType) { it.copy(font = font) }
    }

    fun deleteSection(metaType: MetaType): Meta {
        return copy(
            sections = sections.plus(
                metaType to MetaSection().copy(
                    size = getTextSize(
                        metaType
                    )
                )
            )
        )
    }

    private fun getTextSize(type: MetaType): Int {
        return when (type) {
            MetaType.TITLE -> TITLE_TEXT_SIZE
            MetaType.SUBTITLE -> SUBTITLE_TEXT_SIZE
            MetaType.COMPOSER -> COMPOSER_TEXT_SIZE
            MetaType.LYRICIST -> COMPOSER_TEXT_SIZE
            MetaType.FOOTER_LEFT -> COMPOSER_TEXT_SIZE
            MetaType.FOOTER_RIGHT -> COMPOSER_TEXT_SIZE
            MetaType.FOOTER_CENTER -> COMPOSER_TEXT_SIZE
        }
    }
}

fun meta(event: Event): Meta? {
    return event.getParam<Meta>(EventParam.SECTIONS)
}

fun ScoreQuery.meta(): Meta {

    val sections = MetaType.values().map { metaType ->
        val eventType = EventType.valueOf(metaType.toString())
        val section = getEvent(eventType)?.let { event ->
            MetaSection(
                event.getParam(EventParam.TEXT) ?: "",
                event.getParam(EventParam.TEXT_SIZE) ?: TEXT_SIZE,
                event.getParam(EventParam.FONT) ?: "default",
                event.getParam(EventParam.HARD_START) ?: Coord(),
            )
        } ?: MetaSection()
        metaType to section
    }.toMap()
    return Meta(sections)
}

