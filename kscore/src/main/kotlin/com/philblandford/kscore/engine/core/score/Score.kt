package com.philblandford.kscore.engine.core.score

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.api.instrument
import com.philblandford.kscore.engine.beam.BeamDirectory
import com.philblandford.kscore.engine.beam.BeamMap
import com.philblandford.kscore.engine.beam.beam
import com.philblandford.kscore.engine.core.LayoutDescriptor
import com.philblandford.kscore.engine.core.representation.PAGE_RATIO
import com.philblandford.kscore.engine.core.representation.PAGE_WIDTH
import com.philblandford.kscore.engine.core.representation.pageWidths
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.eventadder.Right
import com.philblandford.kscore.engine.eventadder.ScoreResult
import com.philblandford.kscore.engine.eventadder.then
import com.philblandford.kscore.engine.map.*
import com.philblandford.kscore.engine.pitch.transposeKey
import com.philblandford.kscore.engine.tempo.Tempo
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.time.timeSignature
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.EventParam.*
import com.philblandford.kscore.engine.types.EventType.*
import com.philblandford.kscore.engine.util.add
import com.philblandford.kscore.engine.util.removeAt
import com.philblandford.kscore.option.getAllDefaults

/* The top of of the scorelevel hierarchy - implements the ScoreQuery interface */

object AllParts : EventGetterOption()

data class Score(
  val parts: List<Part> = listOf(Part()),
  override val eventMap: EventMap = initEvents(),
  val beamDirectory: BeamDirectory,
  val providedOLookup:OffsetLookup? = null
) : ScoreLevelImpl(), ScoreQuery {

  override val subLevels = parts

  override fun getTitle(): String? {
    return getParam(TITLE, TEXT)
  }

  override fun getSubtitle(): String? {
    return getParam(SUBTITLE, TEXT)
  }

  override fun getComposer(): String? {
    return getParam(COMPOSER, TEXT)
  }

  override fun getFilename(): String? {
    return getParam(FILENAME, TEXT)
  }

  override fun getMarker(): EventAddress? {
    return getParam(UISTATE, MARKER_POSITION)
  }

  override fun getBeamsForStave(start: Int, end: Int, staveId: StaveId): BeamMap {
    return beamDirectory.getBeamsForStave(staveId, start, end, this)
  }

  override fun getBeams(start: EventAddress?, endAddress: EventAddress?): BeamMap {
    return beamDirectory.getBeams(start, endAddress)
  }

  override fun getSubLevel(eventAddress: EventAddress): ScoreLevel? {
    return getPart(eventAddress.staveId.main)
  }

  override fun getAllSubLevels(): Iterable<ScoreLevel> {
    return parts
  }

  override fun subLevelIdx(eventAddress: EventAddress): Int {
    return eventAddress.staveId.main
  }

  override fun replaceSubLevel(scoreLevel: ScoreLevel, index: Int): Score {
    return Score(
      parts.removeAt(index - 1).add(index - 1, scoreLevel as Part),
      eventMap,
      beamDirectory
    )
  }

  private val acceptedTypes = setOf(
    KEY_SIGNATURE,
    TIME_SIGNATURE,
    HIDDEN_TIME_SIGNATURE,
    UISTATE,
    TITLE,
    SUBTITLE,
    COMPOSER,
    LYRICIST,
    TEMPO,
    TEMPO_TEXT,
    OPTION,
    LAYOUT,
    BARLINE,
    REPEAT_START,
    REPEAT_END,
    FERMATA,
    NAVIGATION,
    VOLTA,
    BREAK,
    STAVE_JOIN,
    REHEARSAL_MARK
  )

  override fun replaceSelf(eventMap: EventMap, newSubLevels: List<ScoreLevel>?): Score {
    return Score(newSubLevels?.map { it as Part }?.toList() ?: parts, eventMap,
      beamDirectory)
  }

  override fun getSpecialEvent(eventType: EventType, eventAddress: EventAddress): Event? {
    return when (eventType) {
      KEY_SIGNATURE -> eventMap.getEvent(eventType, eventAddress.staveless())?.let {
        processKeySignature(it, eventAddress, showConcert())
      }

      BREAK -> {
        if (selectedPart == 0) {
          eventMap.getEvent(BREAK, eventAddress)
        } else {
          null
        }
      }

      BARLINE -> getBarLine(eventAddress)
      else -> null
    }
  }

  override fun getSpecialEventAt(
    eventType: EventType,
    eventAddress: EventAddress
  ): Pair<EventMapKey, Event>? {
    return when (eventType) {
      KEY_SIGNATURE -> eventMap.getEventAt(eventType, eventAddress.staveless())?.let {
        it.first to processKeySignature(it.second, eventAddress, showConcert())
      }

      else -> null
    }
  }

  override fun getEmptyVoiceMaps(start: EventAddress?, end: EventAddress?): Iterable<EventAddress> {
    val startAddr = start ?: ea(1)
    val endAddr = end ?: eas(numBars, dZero(), getAllStaves(true).last())
    return (startAddr.barNum..endAddr.barNum).mapNotNull { barNum ->
      getBar(startAddr.copy(barNum))?.let {
        it.voiceMaps.getOrNull(0)
          ?.let { if (it.getVoiceEvents().isEmpty()) startAddr.copy(barNum, voice = 1) else null }
      }
    }.toSet()
  }

  override fun getRepeatBars(): Lookup<RepeatBarType> {
    return getEvents(REPEAT_BAR)?.flatMap { (k, v) ->
      val num = v.getInt(NUMBER)
      val thisOne = k.eventAddress to if (num == 1) RepeatBarType.ONE else RepeatBarType.TWO_START
      val nextOne =
        if (v.getInt(EventParam.NUMBER) == 2) listOf(k.eventAddress.inc() to RepeatBarType.TWO_END) else listOf()
      listOf(thisOne).plus(nextOne)
    }?.toMap() ?: mapOf()
  }

  override fun isRepeatBar(eventAddress: EventAddress): Boolean {
    return getEvent(REPEAT_BAR, eventAddress.startBar()) != null ||
        getParam<Int>(REPEAT_BAR, NUMBER, eventAddress.dec()) == 2
  }

  override fun isEmptyBar(eventAddress: EventAddress): Boolean {
    return getBar(eventAddress)?.voiceMaps?.any { it.getVoiceEvents().isNotEmpty() } == false
  }

  override fun prepareAddress(eventAddress: EventAddress, eventType: EventType): EventAddress {
    if (eventType == LAYOUT && selectedPart != 0) {
      return eventAddress.copy(staveId = StaveId(selectedPart, 0))
    }
    return when (eventType) {
      KEY_SIGNATURE, TIME_SIGNATURE, HIDDEN_TIME_SIGNATURE, TEMPO, TEMPO_TEXT -> eventAddress.startBar()
        .staveless()

      BREAK -> if (singlePartMode()) eventAddress.startBar().copy(
        staveId = StaveId(
          selectedPart,
          0
        )
      )
      else eventAddress.startBar().staveless()

      UISTATE, OPTION -> eZero()
      PART -> eventAddress.copy(staveId = eventAddress.staveId.copy(sub = 0))
      else -> super.prepareAddress(eventAddress, eventType)
    }
  }

  /* For transposing instruments, the key signature the user sees is not the same as
     the one stored internally
   */
  private fun processKeySignature(ks: Event, eventAddress: EventAddress, concert: Boolean): Event {

    /* If we are not displayed transposing instruments at concert pitch, or if the caller
     * has not specifically requested the underlying KS (staveId == sZero()), get the transposition
     * of the instrument for the specified part
     */
    return if (!concert && eventAddress.staveId != sZero()) {
      getPart(eventAddress.staveId.main)?.getParam<Int>(
        EventType.INSTRUMENT,
        TRANSPOSITION, ez(1)
      )?.let { transposition ->
        /* Replace the SHARPS parameter with the visible KS */
        ks.getParam<Int>(SHARPS)?.let { sharps ->
          ks.addParam(SHARPS, transposeKey(sharps, -transposition))
        }
      } ?: ks
    } else {
      ks
    }
  }

  private fun getBarLine(eventAddress: EventAddress): Event? {
    return eventMap.getEvent(BARLINE, eventAddress) ?: eventMap.getEvent(REPEAT_START, eventAddress)
      ?.let { Event(BARLINE, paramMapOf(EventParam.TYPE to BarLineType.START_REPEAT)) }
    ?: eventMap.getEvent(REPEAT_END, eventAddress)
      ?.let { Event(BARLINE, paramMapOf(EventParam.TYPE to BarLineType.END_REPEAT)) }
  }

  override fun getSpecialEvents(eventType: EventType): EventHash? {
    return when (eventType) {
      /* We don't store a PART event in our eventmap, so just generate one if requested */
      PART -> parts.withIndex().map {
        EventMapKey(
          PART,
          eZero().copy(staveId = StaveId(it.index, 0))
        ) to
            Event(PART, paramMapOf())
      }.toMap()

      BREAK -> {
        /* If not in single part mode, use the breaks stored at Score level */
        if (selectedPart == 0) {
          eventMap.getEvents(BREAK)
        } else {
          /* Otherwise the breaks stored in the selected part */
          parts.getOrNull(selectedPart - 1)?.getEvents(BREAK)?.map {
            it.key.copy(
              eventAddress = badgeEventAddress(
                it.key.eventAddress,
                selectedPart
              )
            ) to it.value
          }?.toMap()
        }
      }

      else -> null
    }
  }

  /* Compile a map of events of the requested types */
  override fun collateEvents(
    eventTypes: Iterable<EventType>,
    eventAddress: EventAddress?,
    endAddress: EventAddress?
  ): EventHash? {
    return if (singlePartMode() && eventAddress == null) {
      /* In single part mode, collect all the events for the selected part */
      var partEvents = parts.getOrNull(selectedPart - 1)?.collateEvents(
        eventTypes
      ) ?: eventHashOf()
      partEvents = partEvents.map { (k, v) ->
        k.copy(eventAddress = badgeEventAddress(k.eventAddress, selectedPart)) to v
      }.toMap()
      /* Add our own events */
      val thisEvents = eventTypes.toList().fold(eventHashOf()) { eh, et ->
        eh.plus(eventMap.getEvents(et, eventAddress, endAddress) ?: eventHashOf())
      }
      thisEvents.plus(partEvents)
    } else {
      super.collateEvents(eventTypes, eventAddress, endAddress)
    }
  }

  override val numParts: Int = allParts(false).toList().size
  override val numBars: Int = parts.firstOrNull()?.getNumBars() ?: 0
  override fun numStaves(part: Int): Int {
    return parts.getOrNull(part - 1)?.staves?.size ?: 0
  }

  override fun allParts(selected: Boolean): Iterable<Int> {
    val partRange = (1..parts.size)
    if (!selected) {
      return partRange
    } else {
      return if (selectedPart != 0) listOf(selectedPart) else (1..parts.size)
    }
  }

  override fun singlePartMode(): Boolean {
    return (getParam<Int>(UISTATE, SELECTED_PART, eZero()) ?: 0) != 0
  }

  override fun selectedPartName(): String? {
    return getPart(getParam(UISTATE, SELECTED_PART, eZero()) ?: 0)?.label
  }

  override fun selectedPart(): Int {
    return selectedPart
  }

  override val scoreLevelType = ScoreLevelType.SCORE
  override val subLevelType = ScoreLevelType.PART

  private val selectedPart = getParam<Int>(UISTATE, SELECTED_PART, eZero()) ?: 0

  val oLookup: OffsetLookup by lazy {
    providedOLookup ?: run {
      val timeSignatures = (1..(numBars.coerceAtLeast(1))).mapNotNull { bar ->
        getEventAt(
          TIME_SIGNATURE,
          ez(bar)
        )?.let { (_, ev) -> bar to TimeSignature.fromParams(ev.params) }
      }
      val hidden = (getEvents(HIDDEN_TIME_SIGNATURE)
        ?: eventHashOf()).map { it.key.eventAddress.barNum to TimeSignature.fromParams(it.value.params) }
      val map = (timeSignatures + hidden).toMap()

      offsetLookup(map, numBars)
    }
  }

  override val lastOffset: Duration = oLookup.lastOffset
  override val totalDuration: Duration = oLookup.totalDuration

  override fun getAllStaves(selected: Boolean): List<StaveId> {
    return (allParts(selected)).flatMap { main ->
      (1..numStaves(main)).map {
        StaveId(main, it)
      }
    }
  }

  override fun allBarAddresses(selected: Boolean): Iterable<EventAddress> {
    return (1..numBars).flatMap { bar ->
      getAllStaves(selected).map { stave ->
        ez(bar).copy(staveId = stave)
      }
    }
  }

  override fun numVoicesAt(eventAddress: EventAddress): Int {
    return getBar(eventAddress)?.voiceNumberMap?.voicesAt(eventAddress.offset) ?: 0
  }

  override fun stripAddress(eventAddress: EventAddress, eventType: EventType): EventAddress {
    return when {
      eventType == STAVE_JOIN -> eZero().copy(staveId = StaveId(eventAddress.staveId.main, 0))
      eventType == KEY_SIGNATURE -> eventAddress.voiceless().idless()
      eventType == FERMATA -> eventAddress.staveless()
      eventType == OPTION || eventType == UISTATE -> eZero()
      acceptedTypes.contains(eventType) -> ez(eventAddress.barNum)
      else -> eventAddress
    }
  }


  private data class GEKey(
    val eventType: EventType, val eventAddress: EventAddress?,
    val endAddress: EventAddress?
  )

  private val getEventsCache = mutableMapOf<GEKey, EventHash?>()
  override fun getEvents(
    eventType: EventType,
    eventAddress: EventAddress?,
    endAddress: EventAddress?,
    options: List<EventGetterOption>
  ): EventHash? {

    return if (eventAddress == null) {
      getAllEvents(eventType, options)
    } else {
      val key = GEKey(eventType, eventAddress, endAddress)
      getEventsCache[key] ?: run {

        val mutable = eventHashOfM()
        parcelRange(eventAddress, endAddress).forEach { (start, end) ->
          val evs = super.getEvents(eventType, start, end, options)?.map {
            it.key.copy(eventAddress = stripAddress(it.key.eventAddress, eventType)) to it.value
          }?.toMap()
          evs?.let { mutable.putAll(it) }
        }
        getEventsCache[key] = mutable
        mutable
      }
    }
  }

  private fun getAllEvents(eventType: EventType, options: List<EventGetterOption>): EventHash? {
    return if (!options.any { it is AllParts } && singlePartMode()) {

      return getSpecialEvents(eventType) ?: run {

        var partEvents = parts.getOrNull(selectedPart - 1)?.getEvents(eventType) ?: eventHashOf()
        partEvents = partEvents.map { (k, v) ->
          k.copy(eventAddress = badgeEventAddress(k.eventAddress, selectedPart)) to v
        }.toMap()
        val thisEvents = eventMap.getEvents(eventType) ?: eventHashOf()
        thisEvents.plus(partEvents)
      }
    } else {
      super.getEvents(eventType, null, null, listOf())
    }
  }

  override fun getEventsForPart(id: Int, level: Boolean): EventHash {
    val events = if (level) {
      getPart(id)?.getAllLevelEvents()
    } else {
      getPart(id)?.getAllEvents()
    }

    return events?.map {
      it.key.copy(
        eventAddress = it.key.eventAddress.copy(
          staveId = it.key.eventAddress.staveId.copy(
            main = id
          )
        )
      ) to it.value
    }?.toMap() ?: eventHashOf()
  }

  override fun getEventsForStave(
    staveId: StaveId,
    types: Iterable<EventType>,
    start: EventAddress?,
    end: EventAddress?
  ): EventHash {
    var events = getStave(staveId)?.collateEvents(types, start, end)

    start?.let {
      types.filter { it.isLine() }.forEach { type ->
        (0..1).forEach { id ->
          getStave(staveId)?.getEventAt(type, start.staveless().copy(id = id))?.let { unfinished ->
            addDuration(unfinished.first.eventAddress, unfinished.second.duration())?.let { end ->
              if (end >= start.staveless()) {
                events = events?.plus(unfinished)
              }
            }
          }
        }
      }
    }

    return events?.map {
      Pair(it.key.copy(eventAddress = it.key.eventAddress.copy(staveId = staveId)), it.value)
    }?.toMap() ?: eventHashOf()
  }

  override fun getEventsForBar(eventType: EventType, eventAddress: EventAddress): EventHash {
    return getBar(eventAddress)?.getEvents(eventType) ?: eventHashOf()
  }

  override fun getTimeSignature(eventAddress: EventAddress): TimeSignature? {
    return getEvent(HIDDEN_TIME_SIGNATURE, ez(eventAddress.barNum))?.let { timeSignature(it) }
      ?: run {
        getEventAt(TIME_SIGNATURE, ez(eventAddress.barNum))?.let { timeSignature(it.second) }
      }
  }

  override fun getKeySignature(eventAddress: EventAddress, concert: Boolean): Int? {
    val address = if (concert) eventAddress.copy(staveId = sZero()) else eventAddress
    return eventMap.getEventAt(KEY_SIGNATURE, address.staveless())?.let {
      it.first to processKeySignature(it.second, address, concert)
    }?.second?.getInt(SHARPS)
  }

  override fun getInstrument(eventAddress: EventAddress, adjustTranspose: Boolean): Instrument? {
    return getEventAt(EventType.INSTRUMENT, eventAddress)?.let {
      instrument(it.second)?.let { instr ->
        val transposition =
          if (adjustTranspose && getOption<Boolean>(OPTION_SHOW_TRANSPOSE_CONCERT) == true) 0 else instr.transposition
        instr.copy(transposition = transposition)
      }
    }
  }

  override fun getSystemEvents(): EventHash {
    return eventMap.getAllEvents()
  }

  override fun getPreviousStaveSegment(eventAddress: EventAddress): EventAddress? {
    return getStave(eventAddress.staveId)?.getPreviousStaveSegment(eventAddress)
      ?.copy(staveId = eventAddress.staveId)
  }

  override fun getFloorStaveSegment(eventAddress: EventAddress): EventAddress? {
    return getStave(eventAddress.staveId)?.segments?.floor(eventAddress.staveless())
      ?.copy(staveId = eventAddress.staveId)
  }

  override fun getNextStaveSegment(eventAddress: EventAddress): EventAddress? {
    return getStave(eventAddress.staveId)?.segments?.higher(eventAddress.staveless())
      ?.copy(staveId = eventAddress.staveId)
  }

  override fun haveStaveSegment(eventAddress: EventAddress): Boolean {
    return getStave(eventAddress.staveId)?.segments?.contains(eventAddress.staveless()) ?: false
  }

  override fun getNextVoiceSegment(eventAddress: EventAddress): EventAddress? {
    return getVoiceMap(eventAddress)?.let { vm ->
      val events =
        vm.getEvents(EventType.DURATION)?.toList()?.sortedBy { it.first.eventAddress.offset }
          ?: listOf()
      if (events.isEmpty()) {
        eventAddress.nextBarIfAny()
      } else {
        val compAddress =
          EventAddress(offset = eventAddress.offset, graceOffset = eventAddress.graceOffset)
        val next = events.takeLastWhile {
          it.first.eventAddress > compAddress
        }.firstOrNull()
        return next?.let {
          eventAddress.copy(
            offset = it.first.eventAddress.offset,
            graceOffset = it.first.eventAddress.graceOffset
          )
        }
          ?: eventAddress.nextBarIfAny()
      }
    } ?: eventAddress.nextBarIfAny()
  }

  override fun getPreviousVoiceSegment(eventAddress: EventAddress): EventAddress? {
    return getVoiceMap(eventAddress)?.let { vm ->
      val events = vm.getVoiceEvents().toList()
      if (events.isEmpty()) {
        lastVoiceSegmentOfPreviousBar(eventAddress)
      } else {
        val previous = events.takeWhile { it.first < eventAddress.offset }.lastOrNull()
        return previous?.let { eventAddress.copy(offset = it.first) }
          ?: lastVoiceSegmentOfPreviousBar(eventAddress)
      }
    } ?: eventAddress.nextBarIfAny()
  }

  private fun EventAddress.nextBarIfAny(): EventAddress? {
    return if (barNum < numBars) inc().startBar() else null
  }

  private fun lastVoiceSegmentOfPreviousBar(eventAddress: EventAddress): EventAddress? {
    return if (eventAddress.barNum < 2) {
      null
    } else {
      getVoiceMap(eventAddress.dec())?.let { vm ->
        val events = vm.getVoiceEvents().toList() ?: listOf()
        if (events.isEmpty()) {
          eventAddress.dec().startBar()
        } else {
          events.lastOrNull()?.let { eventAddress.dec().copy(offset = it.first) }
        }
      }
    }
  }

  override fun badgeEventAddress(eventAddress: EventAddress, levelIdx: Int): EventAddress {
    return badgeEventAddressC(eventAddress, levelIdx)
  }

  override fun getLastSegmentInDuration(address: EventAddress): EventAddress? {
    return getLastSegmentInDuration(address, getAllStaves(true))
  }

  private fun getLastSegmentInDuration(
    address: EventAddress,
    staves: Iterable<StaveId>
  ): EventAddress? {
    return getNextStaveSegment(address)?.let { end ->
      val endings = staves.mapNotNull {
        getPreviousStaveSegment(end.copy(staveId = it))
      }
      endings.maxOrNull()?.copy(staveId = address.staveId)
    }
  }

  private val parcelCache = mutableMapOf<Pair<EventAddress, EventAddress?>,
      Iterable<Pair<EventAddress, EventAddress?>>>()

  private fun parcelRange(
    start: EventAddress,
    end: EventAddress?
  ): Iterable<Pair<EventAddress, EventAddress?>> {

    val realEnd =
      end?.let {
        if (end.staveId != start.staveId) {
          getLastSegmentInDuration(end, getStaveRange(start.staveId, end.staveId)) ?: end
        } else end
      }

    return parcelCache[Pair(start, realEnd)] ?: run {
      val res = doParcelRange(start, realEnd)
      parcelCache.put(Pair(start, realEnd), res)
      res
    }
  }

  override fun getStaveRange(start: StaveId, end: StaveId): Iterable<StaveId> {
    val realStart = minOf(start, end)
    val realEnd = maxOf(start, end)
    return (realStart.main..realEnd.main).flatMap { partId ->
      getPart(partId)?.let { part ->
        val startSub = if (partId == start.main) start.sub else 1
        val endSub = if (partId == end.main) end.sub else part.staves.size
        (startSub..endSub).map { StaveId(partId, it) }
      } ?: listOf(sZero())
    }
  }

  override fun <T> getOption(option: EventParam): T? {
    return getParam(OPTION, option, eZero())
  }

  private fun doParcelRange(
    start: EventAddress,
    end: EventAddress?
  ): Iterable<Pair<EventAddress, EventAddress?>> {

    val voice = if (start.voice == 0) INT_WILD else start.voice

    return if (end == null) {
      return listOf(start to null)
    } else {
      (start.barNum..end.barNum).flatMap { bar ->
        getStaveRange(start.staveId, end.staveId).map { staveId ->
          val startBar = if (bar != start.barNum) {
            ez(bar).copy(staveId = staveId, voice = voice, graceOffset = dZero())
          } else {
            ez(bar, start.offset).copy(
              staveId = staveId,
              voice = voice,
              graceOffset = start.graceOffset
            )
          }
          val endBar = if (bar != end.barNum) {
            ez(bar, DURATION_WILD).copy(staveId = staveId, voice = voice)
          } else {
            ez(bar, end.offset).copy(
              staveId = staveId,
              voice = voice,
              graceOffset = end.graceOffset
            )
          }
          startBar to endBar
        }
      }
    }
  }

  fun getPart(id: Int): Part? {
    return parts.getOrNull(id - 1)
  }

  fun getStave(staveId: StaveId): Stave? {
    return getPart(staveId.main)?.getStave(staveId.sub)
  }

  fun getBar(eventAddress: EventAddress, eventType: EventType = EventType.NO_TYPE): Bar? {
    return getStave(eventAddress.staveId)?.getBar(eventAddress.barNum, eventType)
  }

  fun getVoiceMap(eventAddress: EventAddress): VoiceMap? {
    return getBar(eventAddress)?.getMap(eventAddress.voice)
  }

  fun getTuplet(eventAddress: EventAddress): Tuplet? {
    return getVoiceMap(eventAddress)?.getSubLevel(eventAddress) as Tuplet?
  }

  override fun addDuration(address: EventAddress, duration: Duration): EventAddress? {
    return oLookup.addDuration(address, duration)
  }

  override fun subtractDuration(address: EventAddress, duration: Duration): EventAddress? {
    return oLookup.subtractDuration(address, duration)
  }

  override fun addressToOffset(address: EventAddress): Duration? {
    return oLookup.addressToOffset(address)
  }

  override fun offsetToAddress(offset: Duration): EventAddress? {
    return oLookup.offsetToAddress(offset)
  }

  override fun getDuration(from: EventAddress, to: EventAddress): Duration? {
    return oLookup.getDuration(from, to)
  }

  override fun getNoteDuration(eventAddress: EventAddress): Duration? {

    return getEvent(NOTE, eventAddress)?.let {
      note(it)?.let { note ->
        if (note.isStartTie) {
          getTiedNoteDuration(note, eventAddress)
        } else {
          note.realDuration
        }
      }
    }
  }

  override fun getEventEnd(eventAddress: EventAddress): EventAddress? {
    return getParam<Duration>(EventType.DURATION, REAL_DURATION, eventAddress)?.let { duration ->
      addDuration(eventAddress, duration)
    } ?: eventAddress.copy(eventAddress.barNum + 1, offset = dZero())
  }

  override fun toString(): String {
    return "${getTitle()} ${getSubtitle()} ${getComposer()}"
  }

  override fun getOctaveShift(eventAddress: EventAddress): Int {
    return getEventAt(OCTAVE, eventAddress)?.let { (key, octave) ->
      addDuration(key.eventAddress, octave.duration())?.let { end ->
        if (end.voiceIdless() >= eventAddress.voiceIdless()) {
          octave.getInt(NUMBER)
        } else 0
      }
    } ?: 0
  }

  private fun getTiedNoteDuration(note: Note, eventAddress: EventAddress): Duration? {
    return oLookup.addDuration(eventAddress, note.realDuration)?.let { end ->
      getEvent(EventType.DURATION, eventAddress)?.let {
        chord(it)?.let { chord ->
          chord.notes.withIndex().find { it.value.pitch.midiVal == note.pitch.midiVal }
            ?.let { iv ->
              if (iv.value.isStartTie) {
                getNoteDuration(end.copy(id = iv.index + 1))?.addC(note.realDuration)
              } else {
                note.realDuration.add(iv.value.realDuration)
              }
            }
        }
      }
    }

  }

  private fun showConcert(): Boolean {
    return getParam<Boolean>(OPTION, OPTION_SHOW_TRANSPOSE_CONCERT) ?: false
  }

  fun refreshBeams(): ScoreResult {
    val beamDirectory = BeamDirectory.create(this)
    val newThis = beamDirectory.markBeamGroupMembers(this)
    return newThis.then { Right(it.copy(beamDirectory = beamDirectory)) }
  }

  companion object {
    fun create(
      instrumentGetter: InstrumentGetter,
      numBars: Int, ts: TimeSignature = TimeSignature(4, 4),
      ks: Int = 0, instruments: Iterable<String> = listOf("Violin"), upbeat: TimeSignature? = null,
      pageSize: PageSize = PageSize.A5
    ): Score {
      var events = initEvents()
      events = events.putEvent(ez(1), Event(KEY_SIGNATURE, paramMapOf(SHARPS to ks)))
      events = events.putEvent(ez(1), ts.toEvent())
      events =
        events.putEvent(ez(1), Tempo(crotchet(), 120).toEvent())
      upbeat?.let {
        events = events.putEvent(
          ez(1),
          TimeSignature(it.numerator, it.denominator, hidden = true).toEvent()
        )
        events = events.putEvent(ez(2), ts.toEvent())
      }
      val layoutDescriptor = LayoutDescriptor(pageWidths[pageSize] ?: PAGE_WIDTH)
      events = events.putEvent(eZero(), layoutDescriptor.toEvent())
      val parts = instruments.mapNotNull {
        instrumentGetter.getInstrument(it)?.let { part(it, numBars, ts) }
      }
      if (parts.isEmpty()) {
        throw Exception("Could not find any instruments")
      }
      return Score(parts, events, BeamDirectory(mapOf(), mapOf()))
    }
  }
}

internal fun initEvents(pageSize: PageSize? = null): EventMap {
  var events = emptyEventMap()
  events = events.putEvent(eZero(), Event(UISTATE, paramMapOf(MARKER_POSITION to ea(1))))
  events = events.putEvent(eZero(), Event(OPTION, getAllDefaults()))
  events = events.putEvent(eZero(), Event(LAYOUT, LayoutDescriptor().toEvent().params))
  pageSize?.let {
    events = events.setParam(eZero(), LAYOUT, LAYOUT_PAGE_WIDTH, pageWidths[pageSize])
    events = events.setParam(
      eZero(),
      LAYOUT,
      LAYOUT_PAGE_HEIGHT,
      (pageWidths[pageSize]!! * PAGE_RATIO).toInt()
    )
  }
  return events
}

private val badgeCache = mutableMapOf<Pair<EventAddress, Int>, EventAddress>()
private fun badgeEventAddressC(eventAddress: EventAddress, levelIdx: Int): EventAddress {
  return badgeCache[Pair(eventAddress, levelIdx)] ?: run {
    val res = eventAddress.copy(staveId = StaveId(levelIdx, eventAddress.staveId.sub))
    badgeCache.put(Pair(eventAddress, levelIdx), res)
    res
  }
}