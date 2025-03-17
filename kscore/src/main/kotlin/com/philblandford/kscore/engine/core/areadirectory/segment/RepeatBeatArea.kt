package com.philblandford.kscore.engine.core.areadirectory.segment

import com.philblandford.kscore.engine.core.VoiceGeography
import com.philblandford.kscore.engine.core.area.factory.DiagonalArgs
import com.philblandford.kscore.engine.core.area.factory.DrawableArgs
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.duration.duration
import com.philblandford.kscore.engine.types.Event

fun DrawableFactory.repeatBeatArea(event: Event): VoiceArea? {
    return getDrawableArea(
        DiagonalArgs(
            BLOCK_HEIGHT*2,
            BLOCK_HEIGHT * 4,
            BLOCK_HEIGHT / 2,
            true
        )
    )?.let {
        VoiceArea(
            it.copy(tag = "RepeatBeat", requestedY = -BLOCK_HEIGHT*2),
            VoiceGeography(it.width, 0, event.duration(), null, listOf(), BLOCK_HEIGHT*2, BLOCK_HEIGHT * 4)
        )
    }
}
