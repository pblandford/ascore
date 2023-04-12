package com.philblandford.kscore.engine.core.area

import com.philblandford.kscore.engine.core.representation.STAVE_HEIGHT
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.eZero
import com.philblandford.kscore.log.ksLogt
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private var plusCache = mutableMapOf<Pair<Coord, Coord>, Coord>()

data class Coord(val x: Int = 0, val y: Int = 0) {
  operator fun plus(coord: Coord): Coord {
    return plusCache[Pair(this, coord)] ?: run {
      val res = copy(x = x + coord.x, y = y + coord.y)
      plusCache.put(Pair(this, coord), res)
      res
    }
  }

  fun plusX(x: Int) = copy(x = this.x + x)
  fun plusY(y: Int) = copy(y = this.y + y)
  fun plus(x: Int, y: Int) = copy(x = this.x + x, y = this.y + y)

  operator fun minus(coord: Coord): Coord {
    return Coord(x - coord.x, y - coord.y)
  }
}

private val cz = Coord()
fun cZero() = cz

enum class AddressRequirement {
  SYSTEM, PART, STAVE, BAR, SEGMENT, EVENT, NONE
}

private var UID = 0

data class AreaMapKey(
  val coord: Coord = Coord(0, 0), val eventAddress: EventAddress = eZero(),
  val uid: Int = UID
) {
  init {
    UID++
  }
}
typealias AreaMap = Map<AreaMapKey, Area>

fun areaMapOf(vararg args: Pair<AreaMapKey, Area>) = mapOf(*args)
fun AreaMap.get(tag: String) = toList().find { it.second.tag == tag }

fun AreaMap.width(): Int {
  val left = minByOrNull { it.key.coord.x }?.key?.coord?.x ?: 0
  val right =
    maxByOrNull { it.key.coord.x + it.value.width }?.let { it.key.coord.x + it.value.width } ?: 0
  return right - left
}

fun AreaMap.height(): Int {
  val top = minByOrNull { it.key.coord.y }?.key?.coord?.y ?: 0
  val bottom =
    maxByOrNull { it.key.coord.y + it.value.height }?.let { it.key.coord.y + it.value.height } ?: 0
  return bottom - top
}

fun endEntry(pair: Pair<AreaMapKey, Area>): Int = pair.first.coord.x + pair.second.width
fun startEntry(pair: Pair<AreaMapKey, Area>): Int = pair.first.coord.x

data class Area(
  val width: Int = 0, val height: Int = 0, val xMargin: Int = 0, val yMargin: Int = 0,
  val ignoreX: Int = 0, val requestedY: Int = 0,
  val drawable: KDrawable? = null, val event: Event? = null, val tag: String = "",
  val addressRequirement: AddressRequirement = event?.let { AddressRequirement.EVENT }
    ?: AddressRequirement.NONE,
  val numBars: Int = 1,
  val extra: Any? = null,
  val childMap: AreaMap = areaMapOf(),
  val tagMap: Map<String, List<Pair<AreaMapKey, Area>>> = mapOf()
) {

  val bottom = height - yMargin
  val right = width - xMargin

  fun addArea(child: Area, coord: Coord = cZero(), eventAddress: EventAddress = eZero()): Area {

    val pos = coord.plusY(-child.requestedY)
    val newArea = copy(childMap = childMap.plus(AreaMapKey(pos, eventAddress) to child))
    val newXMargin = getNewMargin(child.xMargin, xMargin, pos.x)
    val newYMargin = getNewMargin(child.yMargin, yMargin, pos.y)
    val newWidth = calculateWidth(newArea.childMap, newXMargin)
    val newHeight = calculateHeight(newArea.childMap, newYMargin)
    return newArea.copy(
      width = newWidth,
      height = newHeight,
      xMargin = newXMargin,
      yMargin = newYMargin
    )
  }

  private fun calculateWidth(childMap: AreaMap, newXMargin: Int): Int {
    val childLeft = childMap.minByOrNull { it.key.coord.x }?.key?.coord?.x ?: 0
    val left = min(childLeft, -newXMargin)
    val childRight = childMap.maxByOrNull { it.key.coord.x + it.value.width - it.value.xMargin }?.let {
      it.key.coord.x + it.value.width - it.value.xMargin
    } ?: 0
    val right = max(childRight, right)
    return right - left
  }

  private fun calculateHeight(childMap: AreaMap, newYMargin: Int): Int {
    val childTop = childMap.minByOrNull { it.key.coord.y }?.key?.coord?.y ?: 0
    val top = min(childTop, -newYMargin)
    val childBottom = childMap.maxByOrNull { it.key.coord.y + it.value.bottom }?.let {
      it.key.coord.y + it.value.bottom
    } ?: 0
    val bottom = max(childBottom, bottom)
    return bottom - top
  }

  private fun getNewMargin(childMargin: Int, parentMargin: Int, childPos: Int): Int {
    if (childPos >= 0) {
      return if (childPos - childMargin < 0) {
        max(abs(childPos - childMargin), parentMargin)
      } else {
        parentMargin
      }
    } else {
      val cm = max(childMargin, -childPos)
      return max(cm, parentMargin)
    }
  }

  fun addAbove(
    child: Area, gap: Int = 0, x: Int = 0, eventAddress: EventAddress = eZero(),
    shiftDown: Boolean = false
  ): Area {
    return if (shiftDown) {
      val newMap =
        childMap.map { it.key.copy(coord = it.key.coord.plusY(child.height + gap)) to it.value }
          .toMap()
      val newArea = copy(childMap = newMap)
      newArea.addArea(child, Coord(x, 0), eventAddress = eventAddress)
    } else {
      addArea(child, Coord(x, -(gap + child.height) - yMargin), eventAddress = eventAddress)
    }
  }

  fun addBelow(
    child: Area, gap: Int = 0, x: Int = 0, ignoreMargin: Boolean = false,
    eventAddress: EventAddress = eZero()
  ): Area {
    if (height == 0 && !ignoreMargin) {
      return addArea(child, Coord(x, 0), eventAddress = eventAddress)
    } else {
      val extra = if (ignoreMargin) child.yMargin else 0
      return addArea(child, Coord(x, bottom + gap + extra), eventAddress = eventAddress)
    }
  }

  fun addLeft(
    child: Area, gap: Int = 0, y: Int = 0,
    eventAddress: EventAddress = eZero()
  ): Area {
    return addArea(child, Coord(-(gap + child.width) - xMargin, y), eventAddress = eventAddress)
  }

  fun addRight(
    child: Area, gap: Int = 0, y: Int = 0,
    ignoreMargin: Boolean = false,
    eventAddress: EventAddress = eZero()
  ): Area {
    val extra = if (ignoreMargin) child.xMargin else 0
    return addArea(child, Coord(right + gap + extra, y), eventAddress = eventAddress)
  }

  fun extendRight(amount: Int): Area {
    return copy(width = this.width + amount)
  }

  fun extendLeft(amount: Int): Area {
    val children = childMap.map {
      it.key.copy(coord = it.key.coord.plusX(amount)) to it.value
    }.toMap()
    return copy(width = this.width + amount, childMap = children)
  }

  fun removeYMargin(): Area {
    val children = childMap.map { (key, value) ->
      val newY = key.coord.y + yMargin
      key.copy(coord = Coord(key.coord.x, newY)) to value
    }.toMap()
    return copy(yMargin = 0, childMap = children)
  }

  fun findByTagSingle(tag: String): Area? {
    return findByTag(tag).toList().firstOrNull()?.second
  }

  fun findPairByTagSingle(tag: String): Pair<Coord, Area>? {
    return findByTag(tag).toList().firstOrNull()?.let { it.first.coord to it.second }
  }

  fun findByTag(
    tag: String,
    soFar: HashMap<AreaMapKey, Area> = hashMapOf(),
    offset: Coord = Coord()
  ): Map<AreaMapKey, Area> {
    soFar.putAll(childMap.filter { it.value.tag == tag }.map {
      it.key.copy(coord = it.key.coord + offset) to it.value
    })
    for (entry in childMap) {
      entry.value.findByTag(tag, soFar, offset + entry.key.coord)
    }
    return soFar
  }

  fun findFirst(tag: String): Pair<AreaMapKey, Area>? {
    return findByTag(tag).toList().firstOrNull()
  }

  fun findFromCoord(
    x: Int,
    y: Int,
    matchFunc: ((Area) -> Boolean),
    fuzz: Int = 0,
    coord: Coord = Coord()
  ): Pair<AreaMapKey, Area>? {
    ksLogt("${this.tag} $x $y")
    childMap.toList().find {
      matchFunc(it.second) && matches(it, x, y, fuzz, true)
    }?.let {
      return it.first.copy(coord = coord.plus(it.first.coord)) to it.second
    }
    childMap.forEach {
      if (matches(it.toPair(), x, y, fuzz, false)) {
        it.value.findFromCoord(
          x - it.key.coord.x, y - it.key.coord.y, matchFunc, fuzz,
          coord.plus(it.key.coord)
        )?.let {
          return it
        }
      }
    }
    return null
  }

  private fun matches(candidate: Pair<AreaMapKey, Area>, x: Int, y: Int, fuzz: Int,
  asFinalCandidate:Boolean): Boolean {
    val requestedX = if (asFinalCandidate) candidate.second.ignoreX else 0
    val requestedY = if (asFinalCandidate) candidate.second.requestedY else 0

    val coord = Coord(
      candidate.first.coord.x - candidate.second.xMargin + requestedX,
      candidate.first.coord.y - candidate.second.yMargin + requestedY
    )
    return coord.x < x + fuzz && coord.y < y + fuzz &&
        coord.x + candidate.second.width - requestedX > x - fuzz &&
        coord.y + candidate.second.height - requestedY > y - fuzz
  }

  fun getArea(
    eventAddress: EventAddress, x: Int = 0, y: Int = 0,
    match: (Area) -> Boolean
  ): Pair<AreaMapKey, Area>? {
    childMap.toList().find {
      it.first.eventAddress == eventAddress && match(it.second)
    }?.let {
      return it.first.copy(coord = Coord(it.first.coord.x + x, it.first.coord.y + y)) to it.second
    }
    childMap.forEach {
      it.value.getArea(eventAddress, x + it.key.coord.x, y + it.key.coord.y, match)
        ?.let { return it }
    }
    return null
  }

  fun getArea(eventType: EventType, eventAddress: EventAddress): Pair<AreaMapKey, Area>? {
    return getArea(eventAddress) { it.event?.eventType == eventType }
  }

  fun transformEventAddress(func: (EventAddress, Event?) -> EventAddress): Area {
    val newMap = childMap.map {
      it.key.copy(
        eventAddress = func(
          it.key.eventAddress,
          it.value.event
        )
      ) to it.value.transformEventAddress(func)
    }.toMap()
    return copy(childMap = newMap)
  }

  fun getTopForRange(start: Int, end: Int): Int {
    return getTopForRangeNullable(start, end) ?: 0
  }


  fun getTopForRangeNullable(start: Int, end: Int, filter: (Area) -> Boolean = { false }): Int? {
    val drawables = getDrawablesRange(start, end, filter = filter)
    return drawables.minByOrNull { it.key.coord.y }?.key?.coord?.y
  }


  fun getBottomForRange(start: Int, end: Int): Int {
    return getBottomForRangeNullable(start, end) ?: STAVE_HEIGHT
  }

  fun getBottomForRangeNullable(start: Int, end: Int): Int? {
    val drawables = getDrawablesRange(start, end)
    return drawables.maxByOrNull { it.key.coord.y + it.value.effectiveHeight }
      ?.let { it.key.coord.y + it.value.effectiveHeight }

  }

  private fun getDrawablesRange(
    start: Int, end: Int, offset: Coord = Coord(),
    filter: (Area) -> Boolean = { false },
    soFar: MutableMap<AreaMapKey, KDrawable> = mutableMapOf()
  ): Map<AreaMapKey, KDrawable> {
    val filtered =
      childMap.filterNot {
        filter(it.value) ||
            (it.key.coord.x - it.value.xMargin + offset.x + it.value.right < start ||
                it.key.coord.x - it.value.xMargin + offset.x > end)
      }
    val drawables = filtered.mapNotNull { (k, v) ->
      v.drawable?.let {
        k.copy(
          coord = Coord(
            k.coord.x - v.xMargin + offset.x,
            k.coord.y - v.yMargin + offset.y + it.trim
          )
        ) to it
      }
    }
    soFar.putAll(drawables)
    filtered.toList().forEach { (k, v) ->
      v.getDrawablesRange(
        start,
        end,
        Coord(offset.x + k.coord.x, offset.y + k.coord.y),
        filter,
        soFar
      )
    }
    return soFar
  }

  override fun toString(): String {
    return "$tag $width x $height"
  }

  fun createTagMap(): Area {
    return copy(tagMap = childMap.toList().groupBy { it.second.tag })
  }

  fun replaceArea(areaMapKey: AreaMapKey, new: Area): Area {
    return copy(childMap = childMap.plus(areaMapKey to new))
  }
}


interface KDrawable {
  val width: Int
  val height: Int
  val effectiveHeight: Int
  val trim: Int
  val export: Boolean
  fun tag() = ""
  fun draw(x: Int, y: Int, export: Boolean = false, vararg args: Any)
}