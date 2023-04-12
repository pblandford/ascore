package com.philblandford.kscore.engine.core.areadirectory.header

import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.area.factory.ImageDimension
import com.philblandford.kscore.engine.types.ClefType
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.INT_WILD

fun DrawableFactory.clefArea(clef:Event): Area? {
    val key = when (clef.subType) {
        ClefType.TREBLE -> "clef_treble"
        ClefType.BASS -> "clef_bass"
        ClefType.ALTO -> "clef_alto"
        ClefType.TENOR -> "clef_alto"
        ClefType.PERCUSSION -> "clef_percussion"
        ClefType.TREBLE_8VA -> "clef_treble_octava_up"
        ClefType.TREBLE_8VB -> "clef_treble_octava_down"
        ClefType.BASS_8VA -> "clef_bass_octava_up"
        ClefType.BASS_8VB -> "clef_bass_octava_down"
        ClefType.SOPRANO -> "clef_soprano"
        ClefType.MEZZO -> "clef_mezzo"
        else -> "clef_treble"
    }
    val dimens = getDimensions(clef)
    return getDrawableArea(ImageArgs(key, dimens.width, dimens.height))?.copy(tag = "Clef",
           requestedY = dimens.yMargin,  event = clef,
        addressRequirement = AddressRequirement.EVENT)
}

private fun getDimensions(clef:Event): ImageDimension {
    return when (clef.subType) {
        ClefType.TREBLE -> ImageDimension(INT_WILD, 13, 0, 2)
        ClefType.BASS -> ImageDimension(INT_WILD, 6, 0, 0)
        ClefType.ALTO -> ImageDimension(INT_WILD, 8, 0, 0)
        ClefType.TENOR -> ImageDimension(INT_WILD, 8, 0, 2)
        ClefType.PERCUSSION -> ImageDimension(INT_WILD, 6, 0, -1)
        ClefType.TREBLE_8VA -> ImageDimension(INT_WILD, 15, 0, 4)
        ClefType.TREBLE_8VB -> ImageDimension(INT_WILD, 15, 0, 2)
        ClefType.BASS_8VA -> ImageDimension(INT_WILD, 8, 0, 2)
        ClefType.BASS_8VB -> ImageDimension(INT_WILD, 8, 0, 0)
        ClefType.SOPRANO -> ImageDimension(INT_WILD, 8, 0, -4)
        ClefType.MEZZO -> ImageDimension(INT_WILD, 8, 0, -2)
        else -> ImageDimension(INT_WILD, 13, 0, 2)
    }.toPixels()
}