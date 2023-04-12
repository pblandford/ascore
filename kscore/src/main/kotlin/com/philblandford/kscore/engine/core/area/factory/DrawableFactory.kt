package com.philblandford.kscore.engine.core.area.factory

import com.philblandford.kscore.api.DrawableGetter
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.KDrawable
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.LINE_THICKNESS
import com.philblandford.kscore.engine.core.representation.TEXT_SIZE
import com.philblandford.kscore.engine.types.isWild
import com.philblandford.kscore.engine.util.black
import com.philblandford.kscore.log.ksLoge

enum class TextType {
  SYSTEM,
  EXPRESSION,
  TITLE,
  SUBTITLE,
  COMPOSER,
  LYRICIST,
  LYRIC,
  HARMONY,
  DEFAULT
}

sealed class DrawableArgs()
data class BeamArgs(val up: Boolean, val width: Int, val height: Int) : DrawableArgs()
data class DiagonalArgs(val width: Int, val height: Int, val thickness: Int, val up: Boolean) :
  DrawableArgs()

data class DotArgs(val width: Int, val height: Int) : DrawableArgs()
data class ImageArgs(
  val name: String,
  val width: Int,
  val height: Int,
  val rotation: Float? = null,
  val export: Boolean = true
) : DrawableArgs()

data class LineArgs(
  val length: Int, val horizontal: Boolean, val thickness: Int = LINE_THICKNESS,
  val color: Int = black(), val dashWidth: Int? = null, val dashGap: Int? = null,
  val export: Boolean = true
) : DrawableArgs()

data class RectArgs(
  val width: Int, val height: Int, val fill: Boolean, val color: Int = black(),
  val thickness: Int = LINE_THICKNESS
) : DrawableArgs()

data class SlurArgs(val start: Coord, val mid: Coord, val end: Coord, val up: Boolean) :
  DrawableArgs()

data class TextArgs(
  val text: String, val color: Int = black(),
  val size: Int = TEXT_SIZE, val type:TextType? = null, val font: String? = null
) : DrawableArgs()

data class ImageDimension(val width: Int, val height: Int, val xMargin: Int, val yMargin: Int) {
  private val widthToUse = if (width.isWild()) width else width * BLOCK_HEIGHT
  fun toPixels() = copy(
    width = widthToUse, height = height * BLOCK_HEIGHT, xMargin = xMargin * BLOCK_HEIGHT,
    yMargin = yMargin * BLOCK_HEIGHT
  )
}

private class CompositeCache<K, V> {
  private val cache = mutableMapOf<K, V?>()

  fun getOrCreate(key: K, create: () -> V): V {
    return cache[key] ?: run {
      val value = create()
      value?.let {
        cache += key to it
      }
      value
    }
  }
}

class DrawableFactory(private val drawableGetter: DrawableGetter) {

  private val compositeCaches = mutableMapOf<String, CompositeCache<*, *>>()
  private val cache = mutableMapOf<Pair<DrawableArgs, DrawableGetter?>, KDrawable>()
  private var useCache: Boolean = true

  fun getDrawable(drawableArgs: DrawableArgs): KDrawable? {
    if (useCache) {
      cache[drawableArgs to drawableGetter]?.let {
        return it
      }
    } else {
      ksLoge("Cache is disabled, this better be for debugging")
    }
    val drawable = drawableGetter.getDrawable(drawableArgs)
    drawable?.let {
      cache.put(drawableArgs to drawableGetter, it)
    }
    return drawable
  }

  fun getDrawableArea(drawableArgs: DrawableArgs): Area? {
    return getDrawable(drawableArgs)?.let {
      Area(
        width = it.width,
        height = it.height,
        drawable = it
      )
    }
  }

  fun TextType.getFont():String? {
    return when (this) {
      TextType.SYSTEM -> "tempo"
      TextType.EXPRESSION -> "expression"
      TextType.TITLE -> "default"
      TextType.SUBTITLE -> "default"
      TextType.COMPOSER -> "default"
      TextType.LYRICIST -> "default"
      TextType.LYRIC -> "default"
      TextType.HARMONY -> "default"
      TextType.DEFAULT -> "default"
    }
  }

  fun <K, V> getOrCreate(cacheId: String, cacheKey: K, create: () -> V): V {
    val cache = (compositeCaches[cacheId] as CompositeCache<K, V>?) ?: run {
      val c = CompositeCache<K, V>()
      compositeCaches[cacheId] = c
      c
    }
    return cache.getOrCreate(cacheKey, create)
  }

}