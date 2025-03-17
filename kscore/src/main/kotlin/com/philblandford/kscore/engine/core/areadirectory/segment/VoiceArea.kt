package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.VoiceGeography
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DotArgs
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.DOT_WIDTH
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.types.*

private data class CacheKey(val event: Event, val voice: Int, val numVoices: Int)

private val cacheId = "VOICE"


data class VoiceArea(val base: Area, val voiceGeography: VoiceGeography)

var numObj = 0

fun DrawableFactory.voiceArea(event: Event, voice: Int, numVoices: Int): VoiceArea? {

    numObj++
    val key = CacheKey(event, voice, numVoices)
    return getOrCreate(cacheId, key) {
        when (event.subType) {
            DurationType.CHORD -> chordArea(event, voice, numVoices)
            DurationType.REST -> restArea(event)
            DurationType.REPEAT_BEAT -> repeatBeatArea(event)
            else -> null
        }
    }
}


fun DrawableFactory.restArea(event: Event): VoiceArea? {
    val mult = if ((event.getParam<GraceType>(EventParam.GRACE_TYPE)
            ?: GraceType.NONE) != GraceType.NONE
    ) 0.7f else 1f

    return restKeys[event.duration().undot()]?.let { desc ->
        getDrawableArea(
            ImageArgs(
                desc.key,
                INT_WILD,
                (desc.height * BLOCK_HEIGHT * mult).toInt()
            )
        )?.let {

            var base = Area(tag = "Rest", event = event).addArea(
                it,
                Coord(0, (desc.offset * BLOCK_HEIGHT).toInt())
            )
            repeat(event.duration().numDots()) {
                getDrawableArea(DotArgs(DOT_WIDTH, DOT_WIDTH))?.let { dot ->
                    base = base.addRight(
                        dot.copy(tag = "Dot"),
                        DOT_WIDTH,
                        (BLOCK_HEIGHT * 2.3f).toInt()
                    )
                }
            }
            VoiceArea(
                base, VoiceGeography(
                    it.width, 0, event.duration(),
                    null, listOf(), desc.offset * BLOCK_HEIGHT, desc.height * BLOCK_HEIGHT
                )
            )
        }
    }
}


private data class RestDesc(val key: String, val height: Int, val offset: Int)

private val restKeys = mapOf(
    hemidemisemiquaver() to RestDesc("rest_semiquaver", 4, 2),
    demisemiquaver() to RestDesc("rest_demisemiquaver", 4, 2),
    semiquaver() to RestDesc("rest_semiquaver", 4, 2),
    quaver() to RestDesc("rest_quaver", 4, 2),
    crotchet() to RestDesc("rest_crotchet", 6, 1),
    minim() to RestDesc("rest_minim", 1, 3),
    semibreve() to RestDesc("rest_semibreve", 1, 2),
    breve() to RestDesc("rest_breve", 2, 2),
    longa() to RestDesc("rest_longa", 4, 2)
)
