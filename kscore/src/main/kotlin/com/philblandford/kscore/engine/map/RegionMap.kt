package com.philblandford.kscore.engine.map

import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import java.util.*

typealias RegionMap = TreeMap<EventAddress, Event>

fun regionMap(
  eventHash: EventHash, eventType: EventType,
  addresses: Iterable<EventAddress>,
  amendEvent: (EventAddress, Event) -> Event = { _, e -> e }
): RegionMap {

  val map = TreeMap<EventAddress, Event>()

  var currentEvent: Pair<EventAddress,Event>? = null

  addresses.forEach { address ->
    repeat(2) { id ->
      val idAddress = if (id != address.id) address.copy(id = id) else address
      eventHash[EMK(eventType, idAddress)]?.let {
        currentEvent = idAddress to it
      }
      currentEvent?.let { (ea,e) ->
        map[address.idless()] = amendEvent(ea,e)
        if (e.isTrue(EventParam.END)) {
          currentEvent = null
        }
      }
    }
  }

  return map
}