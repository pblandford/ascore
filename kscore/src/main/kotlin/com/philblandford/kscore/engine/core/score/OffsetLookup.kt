package com.philblandford.kscore.engine.core.score

import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.OffsetLookup
import com.philblandford.kscore.engine.types.ez
import java.util.*

private const val useCache = false

class OffsetLookupImpl(
  private val offsetMap: OffsetMap, private val addressMap: AddressMap,
  private val tsMap: Map<Int, TimeSignature>
) : OffsetLookup {

  private val lastBar = addressMap.lastEntry().key
  private val lastTs = tsMap.toList().maxByOrNull { it.first }!!.second

  private var addCache = mutableMapOf<Pair<EventAddress, Duration>, EventAddress>()
  private var subtractCache = mutableMapOf<Pair<EventAddress, Duration>, EventAddress>()

  private fun fromCache(address: EventAddress, duration: Duration): EventAddress? {
    return if (useCache) {
      addCache[Pair(address, duration)]
    } else {
      null
    }
  }

  override val lastOffset: Duration = offsetMap.lastKey()
  override val totalDuration: Duration = lastOffset + lastTs.duration

  override fun addDuration(address: EventAddress, duration: Duration): EventAddress? {
    return fromCache(address, duration) ?: run {
      addressMap[address.barNum]?.let {
        val total = it.addC(duration).addC(address.offset)
        offsetMap.floorEntry(total)?.let { floorEntry ->
          getBarOffset(floorEntry.value, floorEntry.key, total, address)
        }
      }?.let { res ->
        addCache[Pair(address, duration)] = res
        res
      }
    }
  }

  private fun getBarOffset(
    floorBarNum: Int, floorOffset: Offset,
    total: Offset, sourceAddress: EventAddress
  ): EventAddress {
    val offset = total.subtractC(floorOffset)
    return if (floorBarNum == lastBar && offset >= lastTs.duration) {
      val extraBars = ((offset - (lastTs.duration)) / lastTs.duration).toInt() + 1
      val remainder = offset - (lastTs.duration * extraBars)
      sourceAddress.copy(barNum = floorBarNum + extraBars, offset = remainder)
    } else {
      sourceAddress.copy(barNum = floorBarNum, offset = offset)
    }
  }


  override fun subtractDuration(address: EventAddress, duration: Duration): EventAddress? {
    return subtractCache[Pair(address, duration)] ?: run {
      addressMap[address.barNum]?.let {
        val total = it.subtractC(duration).addC(address.offset)
        offsetMap.floorEntry(total)?.let { floor ->
          val offset = total.subtractC(floor.key)
          address.copy(barNum = floor.value, offset = offset)
        } ?: address
      }?.let { res ->
        subtractCache[Pair(address, duration)] = res
        res
      }
    }
  }

  override fun addressToOffset(address: EventAddress): Duration? {
    return addressMap[address.barNum]?.add(address.offset) ?: run {
      tsMap.toList().maxByOrNull { it.first }?.let { lastTs ->
        val numBars = address.barNum - lastTs.first
        addressMap[lastTs.first]?.let { lastTsOffset ->
          val extra = lastTs.second.duration.multiply(numBars).add(address.offset)
          lastTsOffset.add(extra)
        }
      }
    }
  }

  override fun offsetToAddress(offset: Duration): EventAddress? {
    return offsetMap.floorEntry(offset)?.let { floor ->
      val remainder = offset.subtract(floor.key)
      ez(floor.value, remainder)
    }
  }

  override fun getDuration(from: EventAddress, to: EventAddress): Duration? {
    return addressToOffset(from)?.let { f ->
      addressToOffset(to)?.subtract(f)
    }
  }

  override val numBars = addressMap.lastKey() ?: 1
}

typealias OffsetMap = TreeMap<Duration, Int>
typealias AddressMap = TreeMap<Int, Duration>

fun offsetLookup(timeSignatures: Map<Int, TimeSignature>, numBars: Int): OffsetLookup {
  val offsetMap = createOffsetMap(timeSignatures, numBars)
  val addressMap = TreeMap(offsetMap.map { it.value to it.key }.toMap().toSortedMap())
  return OffsetLookupImpl(offsetMap, addressMap, timeSignatures)
}

fun offsetLookup(
  timeSignatures: Map<Int, TimeSignature>, totalDuration: Duration,
  discardLast: Boolean = false
): OffsetLookup {
  var offsetMap = createOffsetMapFromLastOffset(timeSignatures, totalDuration)
  if (discardLast) {
    offsetMap = TreeMap(offsetMap.toList().dropLast(1).toMap())
  }
  val addressMap = TreeMap(offsetMap.map { it.value to it.key }.toMap().toSortedMap())
  return OffsetLookupImpl(offsetMap, addressMap, timeSignatures)
}

private fun createOffsetMap(timeSignatures: Map<Int, TimeSignature>, numBars: Int): OffsetMap {
  var total = dZero()
  val map = mutableMapOf(dZero() to 1)
  var currentTs = timeSignatures.toList().minByOrNull { it.first }?.second
  (2..numBars).forEach { bar ->
    val thisTs = currentTs
    currentTs = timeSignatures[bar] ?: currentTs
    total = thisTs?.duration?.let { total.addC(it) } ?: total
    map.put(total, bar)
  }
  return TreeMap(map.toSortedMap())
}

private fun createOffsetMapFromLastOffset(
  timeSignatures: Map<Int, TimeSignature>,
  totalDuration: Duration
): OffsetMap {
  var total = dZero()
  var bar = 1
  val map = mutableMapOf<Offset, Int>()
  var currentTs = timeSignatures.toList().sortedBy { it.first }.first().second

  while (total < totalDuration) {
    map.put(total, bar)
    val thisTs = currentTs
    bar++
    currentTs = timeSignatures[bar] ?: currentTs
    total = total.addC(thisTs.duration)
  }
  return TreeMap(map.toSortedMap())
}