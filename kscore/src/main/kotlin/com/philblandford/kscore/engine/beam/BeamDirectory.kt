package com.philblandford.kscore.engine.beam

import com.philblandford.kscore.engine.beam.BeamDirectory.Companion.overlaps
import com.philblandford.kscore.engine.core.representation.MAX_VOICE
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.Tuplet
import com.philblandford.kscore.engine.core.score.VoiceMap
import com.philblandford.kscore.engine.dsl.score
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.duration
import com.philblandford.kscore.engine.duration.minus
import com.philblandford.kscore.engine.duration.plus
import com.philblandford.kscore.engine.duration.realDuration
import com.philblandford.kscore.engine.eventadder.ScoreResult
import com.philblandford.kscore.engine.eventadder.ok
import com.philblandford.kscore.engine.eventadder.subadders.transformVoiceMaps
import com.philblandford.kscore.engine.eventadder.then
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventList
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.types.BarNum
import com.philblandford.kscore.engine.types.BeamType
import com.philblandford.kscore.engine.types.DurationType
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.OffsetLookup
import com.philblandford.kscore.engine.types.StaveId
import com.philblandford.kscore.engine.types.paramMapOf
import com.philblandford.kscore.log.ksLoge


typealias GroupedMap = Map<StaveId, Map<BarNum, List<Pair<EventAddress, Beam>>>>

class BeamDirectory(val allBeams: BeamMap, val user: BeamMap = mapOf()) {

  /* Group first by staveId, then by barNum */
  private val groupedMap = allBeams.toGroupedMap()

  fun getBeamsForStave(
    staveId: StaveId,
    startBar: Int,
    endBar: Int,
    offsetLookup: OffsetLookup
  ): BeamMap {
    val normal = groupedMap.getBeamsForStave(staveId, startBar, endBar)
    val userOverhang = user.filter {
      it.key.barNum < startBar &&
          (offsetLookup.addDuration(it.key, it.value.duration)?.barNum ?: 0) >= startBar
    }
    return normal + userOverhang
  }

  fun getBeams(start: EventAddress?, end: EventAddress?): BeamMap {
    return allBeams.filter {
      (start == null || it.key.horizontalGraceless >= start.horizontalGraceless)
          && (end == null || it.key.horizontalGraceless <= end.horizontalGraceless)
    }
  }

  fun removeBeamsForBar(eventAddress: EventAddress): BeamDirectory {
    return BeamDirectory(allBeams.removeBeamsForBar(eventAddress), user)
  }

  private fun BeamMap.removeBeamsForBar(eventAddress: EventAddress): BeamMap {
    return filterNot {
      it.key.barNum == eventAddress.barNum &&
          it.key.staveId == eventAddress.staveId
    }
  }

  fun markBeamGroupMembers(
    score: Score,
    startBar: Int = 1,
    endBar: Int = score.numBars,
    staveIds: List<StaveId> = score.getAllStaves(true)
  ): ScoreResult {

    val removed = score.transformVoiceMaps(startBar, endBar, staveIds) { vea ->
      val events = getEvents(EventType.DURATION) ?: eventHashOf()
      val newEvents =
        events.map {
          it.key to it.value.addParam(EventParam.IS_BEAMED, false)
            .removeParam(EventParam.IS_UPSTEM_BEAM)
        }.toMap()
      replaceVoiceEvents(newEvents).ok()
    }

    val beams =
      allBeams.filter { it.key.staveId in staveIds && it.key.barNum in (startBar..endBar) }

    return beams.toList().fold(removed) { s, (ea, beam) ->
      beam.members.fold(s) { s2, m ->
        s2.then { score ->
          val memberAddress =
            if (ea.isGrace) ea.copy(graceOffset = ea.graceOffset!! + m.offset)
            else score.addDuration(ea, m.offset) ?: ea
          score.transformVoiceMaps(
            memberAddress.barNum,
            memberAddress.barNum,
            listOf(memberAddress.staveId)
          ) { vea ->
            if (vea.vertical == ea.vertical) {
              val newHash = getEvents(EventType.DURATION)?.let { hash ->
                val key = EventMapKey(EventType.DURATION, memberAddress.barless())
                hash[key]?.let { event ->
                  hash + (key to event.addParams(
                    paramMapOf(
                      EventParam.IS_BEAMED to true,
                      EventParam.IS_UPSTEM_BEAM to beam.up
                    )
                  ))
                } ?: hash
              } ?: hashMapOf()
              replaceVoiceEvents(newHash).ok()
            } else this.ok()
          }
        }
      }
    }
  }

  fun update(score: Score, addresses: List<EventAddress>): BeamDirectory {
    val filteredMap =
      allBeams.filterNot { (ea, _) -> addresses.any { it.barNum == ea.barNum && it.staveId == ea.staveId } }
    val (all, user) = createFromVoiceMaps(score, addresses)
    return BeamDirectory(filteredMap + all, user)
  }

  companion object {
    fun create(score: Score, voiceMaps: List<EventAddress>? = null): BeamDirectory {
      val (all, user) = createFromVoiceMaps(score, voiceMaps)
      return BeamDirectory(all, user)
    }

    private fun createFromVoiceMaps(
      score: Score,
      voiceMaps: List<EventAddress>?
    ): Pair<BeamMap, BeamMap> {

      val addresses = voiceMaps ?: score.allBarAddresses(false)
        .flatMap { barAddr -> (1..MAX_VOICE).map { barAddr.copy(voice = it) } }
      val userBeams = mutableMapOf<EventAddress, Beam>()

      val maps = addresses.mapNotNull { eventAddress ->
        score.getVoiceMap(eventAddress)?.let { voiceMap ->
          val chords = voiceMap.eventMap.getEvents(EventType.DURATION) ?: eventHashOf()
          var normal = createBeams(chords, timeSignature = voiceMap.timeSignature).map {
            eventAddress.copy(offset = it.key.offset, graceOffset = it.key.graceOffset) to it.value
          }.toMap()
          val tuplet =
            createTupletBeams(voiceMap).map { eventAddress.copy(offset = it.key.offset) to it.value }
          val user = createUser(score, BeamType.JOIN)
          userBeams += user
          val userBreaks = createUser(score, BeamType.BREAK)
          normal = adjustNormalForUser(score, normal + tuplet, user + userBreaks)
          normal + user
        }
      }
      return maps.fold(beamMapOf()) { bigMap, map ->
        bigMap + map
      } to userBeams
    }

    private fun createUser(score: Score, type: BeamType): BeamMap {
      val beams = score.getEvents(EventType.BEAM)?.filter {
        !it.value.isTrue(EventParam.END) && (it.value.getParam<BeamType>(
          EventParam.TYPE
        ) ?: BeamType.JOIN) == type
      }
        ?.mapNotNull { (key, beamEvent) ->
          val start = key.eventAddress
          beamEvent.getParam<Duration>(EventParam.DURATION)?.let { duration ->
            score.addDuration(start, duration)?.let { end ->
              val events = score.getEvents(EventType.DURATION, start, end)
                ?.filterNot { it.key.eventAddress.isGrace } ?: eventHashOf()
              val leadingRestsRemoved =
                events.toList().sortedBy { it.first.eventAddress.horizontal }
                  .dropWhile { it.second.subType != DurationType.CHORD }

              leadingRestsRemoved.firstOrNull()?.first?.eventAddress?.let { actualStart ->
                val members = leadingRestsRemoved.mapNotNull { (dKey, dEvent) ->
                  score.getDuration(actualStart, dKey.eventAddress)?.let { offset ->
                    BeamMember(offset, dEvent.duration(), dEvent.realDuration())
                  }
                }
                val up = getUp(events)
                actualStart to Beam(members, up = up)
              }
            }
          }
        }
      return beams?.toMap() ?: mapOf()
    }

    private fun adjustNormalForUser(
      offsetLookup: OffsetLookup,
      normal: BeamMap,
      user: BeamMap
    ): BeamMap {
      val grouped = normal.toGroupedMap()
      val mutable = normal.toMutableMap()
      user.forEach { (userAddress, userBeam) ->
        offsetLookup.addDuration(userAddress, userBeam.duration)?.let { end ->
          val maybeOverlapping =
            grouped.getBeamsForStave(userAddress.staveId, userAddress.barNum, end.barNum)
              .filterNot { it.key.isGrace }.filter { beam ->
                beam.toPair().overlaps(Pair(userAddress, userBeam), offsetLookup) || beam.toPair()
                  .encloses(Pair(userAddress, userBeam), offsetLookup)
              }
          maybeOverlapping.forEach { (eventAddress, beam) ->
            mutable.remove(eventAddress)
            adjustBeam(offsetLookup, beam, eventAddress, userBeam, userAddress)?.let {
              mutable[it.first] = it.second
            }
          }
        }
      }
      return mutable
    }

    private fun Pair<EventAddress, Beam>.overlaps(
      other: Pair<EventAddress, Beam>,
      offsetLookup: OffsetLookup
    ): Boolean {
      val thisStart = offsetLookup.addressToOffset(first) ?: first.offset
      val thisEnd = thisStart + this.second.duration
      val otherStart = offsetLookup.addressToOffset(other.first) ?: other.first.offset
      val otherEnd = otherStart + other.second.duration

      return (thisStart >= otherStart && thisStart < thisEnd)
          || (thisEnd > otherStart && thisEnd < otherEnd)
    }

    private fun Pair<EventAddress, Beam>.encloses(
      other: Pair<EventAddress, Beam>,
      offsetLookup: OffsetLookup
    ): Boolean {
      val thisStart = offsetLookup.addressToOffset(first) ?: first.offset
      val thisEnd = thisStart + this.second.duration
      val otherStart = offsetLookup.addressToOffset(other.first) ?: other.first.offset
      val otherEnd = otherStart + other.second.duration

      return (thisStart <= otherStart && thisEnd >=
          otherEnd)
    }

    private fun adjustBeam(
      offsetLookup: OffsetLookup,
      beam: Beam, beamAddress: EventAddress, userBeam: Beam, userAddress: EventAddress
    ): Pair<EventAddress, Beam>? {

      return offsetLookup.addressToOffset(beamAddress)?.let { beamStart ->
        offsetLookup.addressToOffset(userAddress)?.let { userStart ->
          if (beamStart >= userStart && beamStart + beam.duration <= userStart + userBeam.duration) {
            null
          } else if (beamStart >= userStart + userBeam.duration) {
            beamAddress to beam
          } else if (beamStart + beam.duration <= userStart) {
            beamAddress to beam
          } else if (beamStart < userStart) {
            val members = beam.members.filter { beamStart + it.offset < userStart }
            if (members.size < 2) {
              null
            } else {
              beamAddress to Beam(members, up = beam.up)
            }
          } else if (beamStart >= userStart) {
            var members =
              beam.members.filter { beamStart + it.offset >= userStart + userBeam.duration }
            if (members.size < 2) {
              null
            } else {
              members.firstOrNull()?.offset?.let { firstMemberOffset ->
                members = members.map { it.copy(it.offset - members.first().offset) }
                val address =
                  offsetLookup.offsetToAddress(beamStart + firstMemberOffset) ?: beamAddress
                beamAddress.copy(
                  barNum = address.barNum,
                  offset = address.offset,
                  graceOffset = address.graceOffset
                ) to Beam(members, up = beam.up)
              }
            }
          } else {
            beamAddress to beam
          }
        }
      }
    }

    private fun createTupletBeams(voiceMap: VoiceMap): BeamMap {
      return voiceMap.tuplets.map { tuplet ->
        createTupletBeams(
          tuplet.getAllEvents().filter { it.key.eventType == EventType.DURATION }
            .map { (key, value) ->
              key.copy(
                eventAddress = key.eventAddress.copy(
                  offset = tuplet.writtenToReal(
                    key.eventAddress.offset
                  )
                )
              ) to value

            }.toMap(), tuplet
        )
      }.fold(beamMapOf()) { m, m1 -> m + m1 }
    }

    private fun createTupletBeams(durationEvents: EventHash, tuplet: Tuplet): BeamMap {
      if (durationEvents.count { it.value.subType == DurationType.CHORD } <= 1) {
        return mapOf()
      }

      val groups = getSubCrotchetGroups(durationEvents)
      return groups.map { events ->
        val firstMemberOffset =
          events.first { it.second.subType == DurationType.CHORD }.first.eventAddress.offset
        val members = events.map {
          BeamMember(
            it.first.eventAddress.offset - firstMemberOffset,
            it.second.duration(),
            it.second.realDuration()
          )
        }
        val up = getUp(durationEvents)

        events.first().first.eventAddress.copy(offset = tuplet.offset + firstMemberOffset) to Beam(
          members,
          tuplet.duration,
          up
        )
      }.toMap()
    }

    private fun getSubCrotchetGroups(durationEvents: EventHash): Iterable<EventList> {
      val groups = mutableListOf<EventList>()

      var list = durationEvents.toList()
      while (list.isNotEmpty()) {
        val group =
          list.takeWhile { it.second.duration() < crotchet() && it.second.subType == DurationType.CHORD }
        if (group.size > 1) {
          groups.add(group)
        }
        list = list.drop(group.size + 1)
      }
      return groups
    }

    private fun getUp(members: EventHash): Boolean {
      val noRest = members.filterNot { it.value.subType == DurationType.REST }.toList()
      val grouping = noRest.groupingBy { it.second.isTrue(EventParam.IS_UPSTEM) }.eachCount()
      return (grouping[true] ?: 0) >= (grouping[false] ?: 0)
    }

    private fun BeamMap.toGroupedMap() = toList().groupBy { it.first.staveId }.map {
      it.key to it.value.groupBy { it.first.barNum }
    }.toMap()

    private fun GroupedMap.getBeamsForStave(staveId: StaveId, startBar: Int, endBar: Int): BeamMap {
      return this[staveId]?.filter { it.key in (startBar..endBar) }?.values?.flatten()
        ?.toMap()
        ?: mapOf()
    }

  }
}