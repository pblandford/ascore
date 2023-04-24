package com.philblandford.kscore.api

import com.philblandford.kscore.clipboard.Clipboard
import com.philblandford.kscore.engine.core.area.AddressRequirement
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.TextType
import com.philblandford.kscore.engine.core.representation.*
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.creation.ScoreCreator
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.eventadder.rightOrThrow
import com.philblandford.kscore.engine.pitch.Harmony
import com.philblandford.kscore.engine.pitch.getCommonChords
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.saveload.Loader
import com.philblandford.kscore.saveload.Saver
import com.philblandford.kscore.log.KSLogger
import com.philblandford.kscore.log.ksLoge
import com.philblandford.kscore.log.ksLogt
import com.philblandford.kscore.log.registerLogger
import com.philblandford.kscore.option.getOption
import com.philblandford.kscore.option.getOptionDefault
import com.philblandford.kscore.select.AreaToShow
import com.philblandford.kscore.select.SelectionManager
import com.philblandford.kscore.sound.MS
import com.philblandford.kscore.sound.MidiPlayer
import com.philblandford.kscore.sound.PlayState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly
import java.util.*


interface KScore {
  fun init()

  fun getPageWidth(page: Int = 1): Int
  fun getPageHeight(page: Int = 1): Int
  fun getMaxPageWidth(): Int
  fun getMinPageWidth(): Int
  fun setPageWidth(width: Int, maintainRatio: Boolean = true)
  fun setPageWidth(pageSize: PageSize)
  fun getPage(eventAddress: EventAddress): Int
  fun adjustPageWidth(amount: Int)
  fun prepareDraw(vararg args: Any)
  fun drawPage(page: Int, playbackMarker: EventAddress? = null)
  fun getNumPages(): Int
  fun getMaxSegmentWidth(): Int
  fun getMinSegmentWidth(): Int


  fun setMarker(location: Location)
  fun setMarker(eventAddress: EventAddress)
  fun getMarker(): EventAddress?
  fun moveMarker(left: Boolean = false)

  fun setStartSelectionOrEvent(location: Location)
  fun setStartSelection(location: Location)
  fun setStartSelection(eventAddress: EventAddress)
  fun setEndSelection(location: Location)
  fun setEndSelection(eventAddress: EventAddress)
  fun clearSelection()
  fun getStartSelect(): EventAddress?
  fun getEndSelect(): EventAddress?
  fun getEndSelectIncludeMultibar(): EventAddress?
  fun haveSelection(): Boolean
  fun getSelectedEvent(): Pair<Event, Any?>?
  fun getSelectedArea(): AreaToShow?
  fun moveSelection(left: Boolean)
  fun cycleArea()
  fun moveSelectedArea(x: Int, y: Int, eventParam: EventParam = EventParam.HARD_START)

  fun copy()
  fun cut()
  fun paste()

  fun undo()
  fun redo()

  fun getNumParts(): Int
  fun getNumBars(): Int

  fun getEventAddress(location: Location): EventAddress?
  fun getEvent(location: Location, fuzz: Int = 30): Pair<EventAddress, Event>?
  fun getEvent(eventType: EventType, eventAddress: EventAddress): Event?
  fun getEventAtMarker(eventType: EventType): Event?
  fun getMeta(metaType: EventType): String?
  fun isAboveStave(location: Location): Boolean

  fun getKeySignatureAtMarker(): Int
  fun getTranspositionAtMarker(): Int

  fun addEvent(
    eventType: EventType, eventAddress: EventAddress, params: ParamMap = paramMapOf(),
    endAddress: EventAddress? = null
  ): Boolean

  fun addEventAtMarker(eventType: EventType, params: ParamMap = paramMapOf())
  fun addNoteAtMarker(nsd: NoteInputDescriptor, voice: Int)
  fun addRestAtMarker(
    duration: Duration, voice: Int,
    graceInputMode: GraceInputMode = GraceInputMode.NONE
  )

  fun setParam(
    eventType: EventType,
    eventParam: EventParam,
    value: Any?,
    eventAddress: EventAddress
  )

  fun deleteRestAtMarker(voice: Int)
  fun deleteAtMarker(voice: Int)
  fun deleteEventAt(
    location: Location, eventType: EventType? = null,
    params: ParamMap = paramMapOf(), voice: Int = 1
  ): Boolean

  fun addEventAtSelection(eventType: EventType, params: ParamMap = paramMapOf())

  fun deleteEvent(
    eventType: EventType, eventAddress: EventAddress,
    endAddress: EventAddress? = null, params: ParamMap = paramMapOf()
  )

  fun deleteEventAtMarker(eventType: EventType, voice: Int = 0, id: Int = 0)
  fun deleteRange()
  fun deleteRange(eventType: EventType)

  fun setOption(option: EventParam, value: Any?)
  fun <T> getOption(option: EventParam): T?

  fun insertMeta(eventType: EventType, text: String)
  fun insertHarmonyAtMarker(harmony: Harmony)
  fun splitBar()
  fun removeBarSplit()

  fun <T> getParam(eventType: EventType, eventParam: EventParam, eventAddress: EventAddress): T?
  fun <T> getParamAtMarker(
    eventType: EventType, eventParam: EventParam, voice: Int = 0,
    id: Int = 0
  ): T?

  fun <T> setParamAtMarker(
    eventType: EventType, eventParam: EventParam,
    value: T, voice: Int = 0, id: Int = 0
  )

  fun <T> getParamAtSelection(
    eventType: EventType, eventParam: EventParam, voice: Int = 0,
    id: Int = 0
  ): T?

  @TestOnly
  fun setSelectedArea(areaToShow: AreaToShow)

  @TestOnly
  fun getRepresentation(): Representation?

  fun <T> getSelectedEventParam(eventParam: EventParam): T?

  fun <T> setSelectedEventParam(eventParam: EventParam, value: T)

  fun replaceSelectedEvent(params: ParamMap)
  fun replaceSelectedEventRetainParams(params: ParamMap)

  fun deleteSelectedEvent()

  fun <T> setParamAtSelection(
    eventType: EventType, eventParam: EventParam,
    value: T, voice: Int = 0, id: Int = 0
  )


  fun getHarmoniesForKey(): List<Harmony>

  fun shiftSelected(amount: Int, accidental: Accidental?)
  fun addTieAtSelected(voice: Int)
  fun setStemAtSelected(up: Boolean?, voice: Int)

  fun transposeTo(sharps: Int, up: Boolean)
  fun transposeBy(amount: Int)

  fun shiftSelectedArea(x: Int, y: Int, param: EventParam = EventParam.HARD_START)
  fun clearSelectedAreaShift(param: EventParam = EventParam.HARD_START)

  fun getBarArea(eventAddress: EventAddress): ScoreArea?
  fun getSegmentArea(eventAddress: EventAddress): ScoreArea?
  fun getEventArea(eventAddress: EventAddress, eventType: EventType, extra: Any? = null): ScoreArea?
  fun play(onStop: () -> Unit = {})

  fun pause()
  fun stop()
  fun isPlaying(): Boolean
  fun isPaused(): Boolean
  fun soundNote(midiVal: Int, velocity: Int = 100, length: MS = 250)

  fun getPlaybackMarker(): StateFlow<EventAddress?>

  fun getPlayState(): StateFlow<PlayState>

  fun getScoreAsB64(): String?
  fun getScoreAsBytes(): ByteArray?
  fun getScore(): Score?
  fun setScore(score: Score, progressFunc: ProgressFunc = noProgress)
  fun setScore(scoreBytes: ByteArray, progressFunc: ProgressFunc = noProgress)

  fun isNewScore(): Boolean

  fun getFontForType(type: TextType): String?
  fun getDefaultTextSize(eventType: EventType): Int
  fun getMinTextSize(): Int
  fun getMaxTextSize(): Int

  fun getCurrentFilename(): String?

  fun createDefaultScore(pageSize: PageSize = PageSize.A5)
  fun createScore(newScoreDescriptor: NewScoreDescriptor)

  fun scoreUpdate(): Flow<Unit>
  fun representationUpdate(): Flow<Unit>

  fun selectionUpdate(): Flow<Unit>

  fun printScore()

  fun getInstrument(name: String): Instrument?
  fun getInstrumentGroups(): List<String>
  fun getInstrumentsForGroup(group: String): List<Instrument>
  fun getInstrumentsInScore(): List<Instrument>
  fun getInstrumentAtMarker(): Instrument?
  fun getInstrumentAtSelection(): Instrument?
  fun getGroupForInstrument(name: String): String?
  fun setInstrumentAtSelection(instrument: Instrument)

  fun setSelectedPart(part: Int)
  fun getSelectedPart(): Int
  fun getPartInstruments(): List<Instrument>

  fun isMultibar(part: Int?): Boolean
  fun setMultibar(part: Int?, yes: Boolean)

  fun setVolume(part: Int, volume: Int)
  fun getVolume(part: Int): Int
  fun setMute(part: Int, yes: Boolean)
  fun isMute(part: Int): Boolean
  fun setSolo(part: Int, yes: Boolean)
  fun isSolo(part: Int): Boolean
  fun setShuffleRhythm(yes: Boolean)
  fun isShuffleRhythm(): Boolean
  fun setHarmonyPlaybackInstrument(name: String? = null)
  fun getHarmonyPlaybackInstrument(): String?
  fun setHarmonyPlayback(yes: Boolean)
  fun isHarmonyPlayback(): Boolean
  fun setLoop(yes: Boolean)
  fun isLoop(): Boolean

  fun batch(vararg cmd: () -> Unit)
  fun getCommandHistory(): List<Command>

  fun getErrorFlow(): Flow<ScoreError>

}

class KScoreImpl(
  logger: KSLogger, private val drawableGetter: DrawableGetter,
  private val soundManager: SoundManager
) : KScore {
  private val selectionManager = SelectionManager()
  private val clipboard = Clipboard()
  private val midiPlayer = MidiPlayer(soundManager)
  private val drawableFactory = DrawableFactory(drawableGetter)
  private val scoreContainer = ScoreContainer(drawableFactory)
  private val scoreCreator = ScoreCreator(soundManager)
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  init {
    registerLogger(logger)
    coroutineScope.launch {
      scoreContainer.currentScoreState.collectLatest { (score, rep) ->
        score?.let {
          rep?.let {
            selectionManager.updateFromScore(score, rep)
          }
        }
      }
    }
  }

  override fun init() {
  }

  override fun getPageWidth(page: Int): Int {
    return rep()?.pages?.toList()?.getOrNull(page - 1)?.base?.width
      ?: getPageWidth()
  }

  override fun getPageHeight(page: Int): Int {
    return rep()?.pages?.toList()?.getOrNull(page - 1)?.base?.height
      ?: getPageHeight()
  }

  override fun getMaxPageWidth(): Int {
    return PAGE_WIDTH_MAX
  }

  override fun getMinPageWidth(): Int {
    return PAGE_WIDTH_MIN
  }

  override fun setPageWidth(width: Int, maintainRatio: Boolean) {
    batch(
      {
        if (maintainRatio) {
          val height = (width * getPageRatio()).toInt()
          scoreContainer.setParam(
            EventType.LAYOUT, EventParam.LAYOUT_PAGE_HEIGHT, height, eZero(), repUpdate = false
          )
        }
      },
      {
        scoreContainer.setParam(
          EventType.LAYOUT, EventParam.LAYOUT_PAGE_WIDTH, width, eZero()
        )
      }
    )
  }

  override fun setPageWidth(pageSize: PageSize) {
    pageWidths[pageSize]?.let {
      setPageWidth(it, true)
    }
  }

  override fun getPage(eventAddress: EventAddress): Int {
    return rep()?.getPageNum(eventAddress) ?: 1
  }

  override fun adjustPageWidth(amount: Int) {
    val newWidth = getPageWidth() + amount
    setPageWidth(newWidth)
  }

  override fun getNumPages(): Int {
    return rep()?.pages?.count() ?: 0
  }

  override fun getMaxSegmentWidth(): Int {
    return SEGMENT_SPACE_MAX
  }

  override fun getMinSegmentWidth(): Int {
    return SEGMENT_SPACE_MIN
  }

  override fun prepareDraw(vararg args: Any) {
    drawableGetter.prepare(*args)
  }

  override fun drawPage(page: Int, playbackMarker: EventAddress?) {
    rep()?.getPageForDrawing(
      page, selectionManager.getSelectState().value,
      playbackMarker
    )?.let { p ->
      drawableGetter.drawTree(p)
    }
  }

  override fun getEventAddress(location: Location): EventAddress? {
    return rep()?.getEventAddress(
      location.page,
      location.x,
      location.y,
      AddressRequirement.SEGMENT,
      0
    )
  }

  override fun isAboveStave(location: Location): Boolean {
    return rep()?.isAboveStave(location.page, location.x, location.y) ?: false
  }

  override fun getEvent(location: Location, fuzz: Int): Pair<EventAddress, Event>? {
    return rep()?.getEvent(location.page, location.x, location.y, fuzz)
  }

  override fun getEvent(eventType: EventType, eventAddress: EventAddress): Event? {
    return score()?.getEvent(eventType, eventAddress)
  }

  override fun getEventAtMarker(eventType: EventType): Event? {
    return getMarker()?.let { marker ->
      getEvent(eventType, marker)
    }
  }

  override fun getMeta(metaType: EventType): String? {
    return score()?.getParam<String>(metaType, EventParam.TEXT, eZero())
  }

  override fun <T> getParam(
    eventType: EventType,
    eventParam: EventParam,
    eventAddress: EventAddress
  ): T? {
    return getEvent(eventType, eventAddress)?.getParam(eventParam)
  }

  override fun <T> getParamAtMarker(
    eventType: EventType, eventParam: EventParam,
    voice: Int, id: Int
  ): T? {
    return getMarker()?.let { marker ->
      score()?.getParam(eventType, eventParam, marker.copy(voice = voice, id = id))
    }
  }

  override fun <T> setParamAtMarker(
    eventType: EventType,
    eventParam: EventParam,
    value: T,
    voice: Int,
    id: Int
  ) {
    getMarker()?.let { marker ->
      scoreContainer.setParam(eventType, eventParam, value, marker.copy(voice = voice, id = id))
    }
  }

  override fun <T> getParamAtSelection(
    eventType: EventType, eventParam: EventParam,
    voice: Int, id: Int
  ): T? {
    return selectionManager.getStartSelection()?.let { eventAddress ->
      score()?.getParam(eventType, eventParam, eventAddress.copy(voice = voice, id = id))
    }
  }

  override fun <T> getSelectedEventParam(eventParam: EventParam): T? {
    return selectionManager.getSelectedArea()?.let { ats ->
      score()?.getParam(ats.event.eventType, eventParam, ats.eventAddress)
    }
  }


  override fun <T> setSelectedEventParam(eventParam: EventParam, value: T) {
    selectionManager.getSelectedArea()?.let { ats ->
      scoreContainer.setParam(ats.event.eventType, eventParam, value, ats.eventAddress)

      refreshAts()
    }
  }

  @TestOnly
  override fun setSelectedArea(areaToShow: AreaToShow) {
    selectionManager.setSelectedArea(
      areaToShow, listOf()
    )
  }

  @TestOnly
  override fun getRepresentation(): Representation? {
    return rep()
  }

  override fun replaceSelectedEvent(params: ParamMap) {
    selectionManager.getSelectedArea()?.let { ats ->
      scoreContainer.deleteEvent(ats.event.eventType, ats.eventAddress)
      scoreContainer.addEvent(Event(ats.event.eventType, params), ats.eventAddress)
      refreshAts()
    }
  }

  override fun replaceSelectedEventRetainParams(params: ParamMap) {
    selectionManager.getSelectedArea()?.let { ats ->
      scoreContainer.addEvent(
        Event(ats.event.eventType, ats.event.params.plus(params)),
        ats.eventAddress
      )
      refreshAts()
    }
  }

  override fun deleteSelectedEvent() {

    selectionManager.getSelectedArea()?.let { ats ->
      var params = paramMapOf(EventParam.HOLD to true)
      ats.extra?.let { params = params.plus(EventParam.TYPE to it) }
      scoreContainer.deleteEvent(
        ats.event.eventType,
        ats.eventAddress,
      )
      refreshAts()
    }
  }

  override fun <T> setParamAtSelection(
    eventType: EventType,
    eventParam: EventParam,
    value: T,
    voice: Int,
    id: Int
  ) {
    selectionManager.getSelectedArea()?.let { area ->
      scoreContainer.setParam(eventType, eventParam, value, area.eventAddress)
      refreshAts()
    } ?: run {
      selectionManager.getStartSelection()?.let { start ->
        scoreContainer.setParam(
          eventType, eventParam, value,
          start.copy(voice = voice, id = id), getEndSelect()
        )
      }
    }
  }

  override fun getHarmoniesForKey(): List<Harmony> {
    return getMarker()?.let { marker ->
      score()?.getKeySignature(
        marker,
        getOption<Boolean>(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT) ?: false
      )?.let { ks ->
        getCommonChords(ks)
      }
    } ?: listOf()
  }

  override fun getKeySignatureAtMarker(): Int {
    return getMarker()?.let {
      score()?.getKeySignature(
        it,
        getOption<Boolean>(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT) ?: false
      )
    } ?: 0
  }

  override fun getTranspositionAtMarker(): Int {
    return getMarker()?.let { score()?.getInstrument(it)?.transposition } ?: 0
  }

  override fun setMarker(location: Location) {
    getEventAddress(location)?.let { setMarker(it) }
  }

  override fun setMarker(eventAddress: EventAddress) {
    scoreContainer.pauseUndo(true)
    scoreContainer.setParam(
      EventType.UISTATE, EventParam.MARKER_POSITION, eventAddress, eZero()
    )
    scoreContainer.pauseUndo(false)
  }

  override fun getMarker(): EventAddress? {
    val ret =
      score()?.getParam<EventAddress>(EventType.UISTATE, EventParam.MARKER_POSITION, eZero())
    return ret
  }

  override fun moveMarker(left: Boolean) {
    getMarker()?.let { marker ->
      score()?.let {
        run {
          if (left) it.getPreviousStaveSegment(marker) else it.getNextStaveSegment(marker)
        }?.let {
          setMarker(it.voiceless().idless())
        }
      }
    }
  }

  override fun setStartSelectionOrEvent(location: Location) {
    val ats = rep()?.getAreaToShow(location.page, location.x, location.y, fuzz = 25) {
      it.event?.eventType != EventType.DURATION && it.event?.eventType != EventType.NOTE
    }
    if (ats != null) {
      ksLogt("COORD setStartSelectionOrEvent ${ats.scoreArea.rectangle}")
      setAreaToShow(ats)
    } else {
      getEventAddress(location)?.let { segment ->
        setStartSelection(segment)
      }
    }
  }

  override fun setStartSelection(location: Location) {
    getEventAddress(location)?.let { setStartSelection(it) }
  }

  override fun setStartSelection(eventAddress: EventAddress) {
    selectionManager.setStartSelect(eventAddress)
  }

  override fun setEndSelection(location: Location) {
    getEventAddress(location)?.let { setEndSelection(it) }
  }

  override fun setEndSelection(eventAddress: EventAddress) {
    selectionManager.setEndSelect(eventAddress)
  }

  override fun clearSelection() {
    selectionManager.clearSelection()
  }

  override fun getStartSelect(): EventAddress? {
    return selectionManager.getStartSelection()
  }

  override fun getEndSelect(): EventAddress? {
    return selectionManager.getEndSelection()
  }

  override fun getEndSelectIncludeMultibar(): EventAddress? {
    return getEndSelect()?.let { it.copy(barNum = rep()?.getEndBarForSegment(it) ?: it.barNum) }
  }

  override fun haveSelection(): Boolean {
    return selectionManager.getStartSelection() != null
  }

  override fun getSelectedEvent(): Pair<Event, Any?>? {
    return selectionManager.getSelectedArea()?.let { it.event to it.extra }
  }

  override fun getSelectedArea(): AreaToShow? {
    return selectionManager.getSelectedArea()
  }

  override fun moveSelection(left: Boolean) {
    selectionManager.moveSelection { ea ->
      if (left) {
        score()?.getPreviousStaveSegment(ea)
      } else {
        score()?.getNextStaveSegment(ea)
      }
    }
  }

  override fun cycleArea() {
    selectionManager.cycleArea { rep()?.getAreasAtAddress(it.voiceless())?.toList() ?: listOf() }
  }

  override fun moveSelectedArea(x: Int, y: Int, eventParam: EventParam) {
    selectionManager.getSelectedArea()?.let { area ->
      getEvent(area.event.eventType, area.eventAddress)?.let { scoreEvent ->
        setParam(area.event.eventType, eventParam, Coord(x, y), area.eventAddress)
      }
    }
  }

  override fun copy() {
    score()?.let { score ->
      selectionManager.getStartSelection()?.let { start ->
        selectionManager.getEndSelection()?.let { end ->
          clipboard.copy(start, end, score)
        }
      }
    }
  }

  override fun cut() {
    score()?.let { score ->
      selectionManager.getStartSelection()?.let { start ->
        selectionManager.getEndSelection()?.let { end ->
          clipboard.cut(start, end, score)
        }
      }
    }
  }

  override fun paste() {
    score()?.let { score ->
      selectionManager.getStartSelection()?.let { start ->
        scoreContainer.updateScore(clipboard.paste(start, score).rightOrThrow())
      }
    }
  }


  override fun addNoteAtMarker(nsd: NoteInputDescriptor, voice: Int) {
      scoreContainer.addEvent(nsd.toEvent(), eWild().copy(voice = voice))
  }

  override fun addRestAtMarker(duration: Duration, voice: Int, graceInputMode: GraceInputMode) {
    getMarker()?.let { marker ->
      scoreContainer.addEvent(
        rest(duration).addParam(EventParam.GRACE_MODE, graceInputMode).addParam(
          EventParam.GRACE_TYPE to
                  if (graceInputMode == GraceInputMode.NONE) GraceType.NONE else GraceType.APPOGGIATURA
        ),
        marker.copy(voice = voice)
      )
    }
  }

  override fun deleteRestAtMarker(voice: Int) {
    getMarker()?.let { marker ->
      scoreContainer.setParam(
        EventType.DURATION,
        EventParam.TYPE,
        DurationType.EMPTY,
        marker.copy(voice = voice)
      )
    }
  }

  override fun addEvent(
    eventType: EventType,
    eventAddress: EventAddress,
    params: ParamMap,
    endAddress: EventAddress?
  ): Boolean {
    scoreContainer.addEvent(Event(eventType, params), eventAddress, endAddress)
    return true
  }

  override fun setParam(
    eventType: EventType,
    eventParam: EventParam,
    value: Any?,
    eventAddress: EventAddress
  ) {
    scoreContainer.setParam(eventType, eventParam, value, eventAddress)
  }

  override fun addEventAtMarker(eventType: EventType, params: ParamMap) {
    getMarker()?.let { marker ->
      scoreContainer.addEvent(Event(eventType, params), marker)
    }
  }

  override fun addEventAtSelection(eventType: EventType, params: ParamMap) {
    selectionManager.getSelectedArea()?.let { ats ->
      scoreContainer.addEvent(Event(eventType, params), ats.eventAddress)
      refreshAts()
    } ?: run {
      selectionManager.getStartSelection()?.let { start ->
        selectionManager.getEndSelection()?.let { end ->
          scoreContainer.addEvent(Event(eventType, params), start, end)
        }
      }
    }
  }

  override fun deleteEventAt(
    location: Location, eventType: EventType?,
    params: ParamMap, voice: Voice
  ): Boolean {

    getEvent(location)?.let { ev ->
      if (ev.second.eventType == eventType || eventType == null) {
        scoreContainer.deleteEvent(ev.second.eventType, ev.first)
        return true
      }
    }

    eventType?.let { et ->
      getEventAddress(location)?.let { ea ->
        scoreContainer.deleteEvent(et, ea.copy(voice = voice))
      }
    }
    return true
  }

  override fun deleteEvent(
    eventType: EventType, eventAddress: EventAddress,
    endAddress: EventAddress?, params: ParamMap
  ) {
    scoreContainer.deleteEvent(eventType, eventAddress, endAddress)
  }

  override fun deleteEventAtMarker(eventType: EventType, voice: Int, id: Int) {
    getMarker()?.let { marker ->
      val address = marker.copy(voice = voice, id = id)
      deleteEvent(eventType, address)
    }
  }

  override fun deleteRange() {
    select { start, end ->
      scoreContainer.deleteRange(start, end)
    }
  }

  override fun deleteRange(eventType: EventType) {
    select { start, end ->
      scoreContainer.deleteEvent(eventType, start, end)
    }
  }

  override fun setOption(option: EventParam, value: Any?) {
    scoreContainer.setOption(option, value)
    refreshAts()
  }

  override fun <T> getOption(option: EventParam): T? {
    return scoreContainer.getOption(option, getOptionDefault(option))
  }

  override fun insertMeta(eventType: EventType, text: String) {
    scoreContainer.setParam(eventType, EventParam.TEXT, text, eZero())
  }

  override fun insertHarmonyAtMarker(harmony: Harmony) {
    getMarker()?.let { marker ->
      scoreContainer.addEvent(harmony.toEvent(), marker)
    }
  }

  override fun splitBar() {
    addEventAtMarker(EventType.BAR_BREAK)
  }

  override fun removeBarSplit() {
    getMarker()?.let { marker ->
      scoreContainer.deleteEvent(EventType.BAR_BREAK, marker.copy(offset = dZero()))
    }
  }

  override fun shiftSelected(amount: Int, accidental: Accidental?) {
    selectionManager.getSelectedArea()?.let {
      scoreContainer.addEvent(
        Event(
          EventType.NOTE_SHIFT,
          paramMapOf(EventParam.AMOUNT to amount, EventParam.ACCIDENTAL to accidental)
        ), it.eventAddress
      )
      refreshAts()
    } ?: run {
      select { start, end ->
        scoreContainer.addEvent(
          Event(
            EventType.NOTE_SHIFT,
            paramMapOf(EventParam.AMOUNT to amount, EventParam.ACCIDENTAL to accidental)
          ), start, end
        )
      }
    }
  }


  override fun shiftSelectedArea(x: Int, y: Int, param: EventParam) {
    selectionManager.getSelectedArea()?.let { ats ->
      val coord = ats.event.getParam<Coord>(param) ?: Coord()
      scoreContainer.setParam(ats.event.eventType, param, coord + Coord(x, y), ats.eventAddress)
      refreshAts()
    }
  }

  override fun clearSelectedAreaShift(param: EventParam) {
    selectionManager.getSelectedArea()?.let { ats ->
      scoreContainer.setParam(ats.event.eventType, param, null, ats.eventAddress)
      refreshAts()
    }
  }

  override fun transposeBy(amount: Int) {
    val start = getStartSelect() ?: eWild()
    scoreContainer.addEvent(
      Event(EventType.TRANSPOSE, paramMapOf(EventParam.AMOUNT to amount)), start,
      selectionManager.getEndSelection()
    )
  }

  override fun transposeTo(sharps: Int, up: Boolean) {
    val start = selectionManager.getStartSelection() ?: eWild()
    scoreContainer.addEvent(
      Event(
        EventType.TRANSPOSE, paramMapOf(
          EventParam.SHARPS to sharps,
          EventParam.IS_UP to up
        )
      ), start,
      selectionManager.getEndSelection()
    )
  }

  override fun addTieAtSelected(voice: Int) {
    select { start, end ->
      scoreContainer.addEvent(
        Event(EventType.TIE),
        start.copy(voice = voice),
        end.copy(voice = voice)
      )
    }
  }

  override fun setStemAtSelected(up: Boolean?, voice: Int) {
    select { start, end ->
      scoreContainer.setParam(
        EventType.DURATION,
        EventParam.IS_UPSTEM,
        up,
        start.copy(voice = voice),
        end.copy(voice = voice)
      )
    }
  }

  override fun undo() {
    scoreContainer.undo()
    refreshAts()
  }

  override fun redo() {
    scoreContainer.redo()
    refreshAts()
  }

  override fun getNumParts(): Int {
    return score()?.numParts ?: 0
  }

  override fun getNumBars(): Int {
    return score()?.numBars ?: 0
  }

  override fun getBarArea(eventAddress: EventAddress): ScoreArea? {
    return getScoreArea(AddressRequirement.BAR, eventAddress)
  }

  override fun getSegmentArea(eventAddress: EventAddress): ScoreArea? {
    return getScoreArea(AddressRequirement.SEGMENT, eventAddress)
  }

  private fun getScoreArea(
    addressRequirement: AddressRequirement,
    eventAddress: EventAddress
  ): ScoreArea? {
    ksLogt("get sa")
    return rep()?.getArea(addressRequirement, eventAddress)?.let { ret ->
      ksLogt("YES $ret")
      ScoreArea(
        ret.page,
        Rectangle(
          ret.areaMapKey.coord.x - ret.area.xMargin,
          ret.areaMapKey.coord.y - ret.area.yMargin, ret.area.width, ret.area.height
        )
      )
    }
  }

  override fun getEventArea(
    eventAddress: EventAddress, eventType: EventType,
    extra: Any?
  ): ScoreArea? {
    return rep()?.getArea(eventType, eventAddress, extra)?.let { ret ->
      ScoreArea(
        ret.page,
        Rectangle(ret.areaMapKey.coord.x, ret.areaMapKey.coord.y, ret.area.width, ret.area.height)
      )
    }
  }

  override fun deleteAtMarker(voice: Int) {
    getMarker()?.let { marker ->
      scoreContainer.deleteEvent(EventType.DURATION, marker.copy(voice = voice))
    }
  }

  override fun play(
    onStop: () -> Unit
  ) {

    score()?.let {
      val startPos = getStartSelect()
      val endPos = getEndSelect()
      val loop = { getOption<Boolean>(EventParam.OPTION_LOOP) ?: false }
      midiPlayer.play(
        it, loop,
        getLiveVelocityAdjust = { part ->
          if (isMute(part)) 0f else getVolume(part).toFloat() / 100
        }, start = startPos, end = endPos
      )
    }
  }

  override fun pause() {
    midiPlayer.pause()
  }

  override fun stop() {
    midiPlayer.stop()
  }

  override fun isPlaying(): Boolean {
    return midiPlayer.isPlaying()
  }

  override fun isPaused(): Boolean {
    return midiPlayer.isPaused()
  }


  override fun setScore(score: Score, progressFunc: ProgressFunc) {
    scoreContainer.setNewScore(score) { m: String, p: Float ->
      progressFunc("Creating Representation", m, p)
    }
  }

  override fun setScore(scoreBytes: ByteArray, progressFunc: ProgressFunc) {
    Loader().createScoreFromBytes(scoreBytes)?.let { score ->
      ksLogt("created score ${score.getTitle()}")
      setScore(score) { _, s2, pc ->
        progressFunc("Loading score ${score.getTitle().orEmpty()}", s2, pc)
      }
    }
  }

  override fun isNewScore(): Boolean {
    return scoreContainer.getCommandHistory().isEmpty()
  }

  override fun getFontForType(type: TextType): String? {
    return null//getDrawableGetter.getFont(type)
  }

  override fun getDefaultTextSize(eventType: EventType): Int {
    return when (eventType) {
      EventType.TITLE -> TITLE_TEXT_SIZE
      EventType.SUBTITLE -> SUBTITLE_TEXT_SIZE
      EventType.COMPOSER -> COMPOSER_TEXT_SIZE
      EventType.LYRICIST -> COMPOSER_TEXT_SIZE
      EventType.LYRIC -> LYRIC_SIZE
      EventType.HARMONY -> HARMONY_SIZE
      else -> TEXT_SIZE
    }
  }

  override fun getMinTextSize(): Int {
    return TEXT_MIN_SIZE
  }

  override fun getMaxTextSize(): Int {
    return TEXT_MAX_SIZE
  }

  override fun getScoreAsB64(): String? {
    return score()?.let { score ->
      val bytes = Saver().createSaveScore(score)
      return Base64.getEncoder().encodeToString(bytes)
    }
  }

  override fun getScoreAsBytes(): ByteArray? {
    return score()?.let { score ->
      Saver().createSaveScore(score)
    }
  }

  override fun getScore(): Score? {
    return score()
  }


  override fun getCurrentFilename(): String {
    return score()?.let { _ ->
      scoreContainer.getParam(EventType.FILENAME, EventParam.TEXT, eZero(), null as String?)
        ?: scoreContainer.getParam(EventType.TITLE, EventParam.TEXT, eZero(), null as String?)
    } ?: "untitled"
  }

  override fun soundNote(midiVal: Int, velocity: Int, length: MS) {
    getMarker()?.let { marker ->
      score()?.getEventAt(EventType.INSTRUMENT, marker)?.let { (_, event) ->
        val concert =
          score()?.getOption<Boolean>(EventParam.OPTION_SHOW_TRANSPOSE_CONCERT) ?: false
        instrument(event)?.let { instrument ->
          soundManager.soundSingleNote(
            if (concert) midiVal else midiVal + instrument.transposition,
            instrument.program,
            velocity,
            length,
            instrument.percussion,
            instrument.soundFont,
            instrument.bank
          )
        }
      }
    }
  }

  override fun getPlaybackMarker(): StateFlow<EventAddress?> {
    return midiPlayer.getPlaybackMarker()
  }

  override fun getPlayState(): StateFlow<PlayState> {
    return midiPlayer.getPlayState()
  }

  override fun createDefaultScore(pageSize: PageSize) {
    scoreContainer.setNewScore(scoreCreator.createDefault(pageSize = pageSize))
  }

  override fun createScore(newScoreDescriptor: NewScoreDescriptor) {
    val ns = scoreCreator.createScore(newScoreDescriptor)
    scoreContainer.setNewScore(ns)
  }

  override fun getInstrument(name: String): Instrument? {
    return soundManager.getInstrument(name)
  }

  override fun getInstrumentGroups(): List<String> {
    return soundManager.getInstrumentGroups().map { it.name }
  }

  override fun getInstrumentsForGroup(group: String): List<Instrument> {
    return soundManager.getInstrumentGroups().find { it.name == group }?.instruments ?: listOf()
  }

  override fun getInstrumentsInScore(): List<Instrument> {
    return score()?.getEvents(EventType.INSTRUMENT, options = listOf(AllParts))
      ?.filter { it.key.eventAddress.isStart() }
      ?.mapNotNull { (_, ev) ->
        instrument(ev)
      } ?: listOf()
  }

  override fun getInstrumentAtMarker(): Instrument? {
    return getMarker()?.let { marker ->
      ksLogt("marker $marker")
      score()?.getEventAt(EventType.INSTRUMENT, marker)?.let { (_, event) ->
        val instrument = instrument(event)
        instrument
      }
    }
  }

  override fun getInstrumentAtSelection(): Instrument? {
    return selectionManager.getStartSelection()?.let { marker ->
      score()?.getEventAt(EventType.INSTRUMENT, marker)?.let { (_, event) ->
        instrument(event)
      }
    }
  }

  override fun setInstrumentAtSelection(instrument: Instrument) {
    selectionManager.getStartSelection()?.let { address ->
      val addr = address.start().copy(staveId = address.staveId.copy(sub = 0))
      addEvent(
        EventType.INSTRUMENT, addr,
        instrument.toEvent().params
      )
      addEvent(EventType.STAVE, addr, paramMapOf(EventParam.CLEF to instrument.clefs))
      setParam(EventType.PART, EventParam.LABEL, instrument.label, addr)
      setParam(EventType.PART, EventParam.ABBREVIATION, instrument.abbreviation, addr)
    }
  }

  override fun getGroupForInstrument(name: String): String? {
    return soundManager.getInstrumentGroups()
      .find { it.instruments.find { it.name == name } != null }?.name
  }

  override fun setSelectedPart(part: Int) {
    batch (
      { scoreContainer.setSelectedPart(part) },
      {
        if (part != 0) {
          getMarker()?.let { marker ->
            setMarker(marker.copy(staveId = StaveId(part, 1)))
          }
        }
      },
      { scoreContainer.resetUndo()}
    )
  }

  override fun getSelectedPart(): Int {
    return score()?.selectedPart() ?: 0
  }

  override fun getPartInstruments(): List<Instrument> {
    return score()?.let { score ->
      score.allParts(false).map { score.getPart(it)?.mainInstrument ?: defaultInstrument() }
    } ?: listOf()
  }

  override fun isMultibar(part: Int?): Boolean {
    return score()?.getParam<Boolean>(EventType.OPTION, EventParam.OPTION_SHOW_MULTI_BARS)
      ?: false
  }

  override fun setMultibar(part: Int?, yes: Boolean) {
    scoreContainer.setParam(EventType.OPTION, EventParam.OPTION_SHOW_MULTI_BARS, yes, eZero())
  }

  override fun setVolume(part: Int, volume: Int) {
    val realPart = getPartForVol(part)
    scoreContainer.noUndo {
      setParam(
        EventType.PLAYBACK_STATE, EventParam.VOLUME, volume,
        partEA(realPart)
      )
    }
  }

  override fun getVolume(part: Int): Int {
    val realPart = getPartForVol(part)
    return scoreContainer.getParam(
      EventType.PLAYBACK_STATE, EventParam.VOLUME, partEA(realPart), 100
    ) ?: 100
  }

  override fun isMute(part: Int): Boolean {
    val realPart = getPartForVol(part)
    return scoreContainer.getParam(
      EventType.PLAYBACK_STATE, EventParam.MUTE,
      partEA(realPart), false
    ) ?: false
  }

  override fun setMute(part: Int, yes: Boolean) {
    val realPart = getPartForVol(part)
    scoreContainer.setParam(
      EventType.PLAYBACK_STATE, EventParam.MUTE, yes, partEA(realPart)
    )
  }

  override fun isSolo(part: Int): Boolean {
    val realPart = getPartForVol(part)
    return scoreContainer.getParam(
      EventType.PLAYBACK_STATE, EventParam.SOLO,
      partEA(realPart), false
    ) ?: false
  }

  override fun setSolo(part: Int, yes: Boolean) {
    val realPart = getPartForVol(part)
    scoreContainer.setParam(
      EventType.PLAYBACK_STATE, EventParam.SOLO, yes, partEA(realPart)
    )
  }

  private fun getPartForVol(part: Int): Int {
    return if (part > score()?.numParts ?: 0) 0 else part
  }

  override fun setShuffleRhythm(yes: Boolean) {
    scoreContainer.setParam(
      EventType.OPTION, EventParam.OPTION_SHUFFLE_RHYTHM, yes, eZero()
    )
    score()?.let { midiPlayer.refresh(it) }
  }

  override fun isShuffleRhythm(): Boolean {
    return score()?.let { getOption(EventParam.OPTION_SHUFFLE_RHYTHM, it) } ?: false
  }

  override fun setHarmonyPlaybackInstrument(name: String?) {
    scoreContainer.setOption(EventParam.OPTION_HARMONY_INSTRUMENT, name)
    score()?.let { midiPlayer.refresh(it) }
  }

  override fun getHarmonyPlaybackInstrument(): String? {
    return scoreContainer.getOption(EventParam.OPTION_HARMONY_INSTRUMENT)
  }

  override fun setHarmonyPlayback(yes: Boolean) {
    setOption(EventParam.OPTION_HARMONY, yes)
    score()?.let { midiPlayer.refresh(it) }
  }

  override fun isHarmonyPlayback(): Boolean {
    return getOption(EventParam.OPTION_HARMONY) ?: false
  }

  override fun setLoop(yes: Boolean) {
    setOption(EventParam.OPTION_LOOP, yes)
  }

  override fun isLoop(): Boolean {
    return getOption(EventParam.OPTION_LOOP) ?: false
  }

  override fun batch(vararg cmd: () -> Unit) {
    scoreContainer.batch(*cmd)
  }

  override fun getCommandHistory(): List<Command> {
    return scoreContainer.getCommandHistory()
  }

  override fun printScore() {
    // score()?.let { Exporter.printScore(it) }
  }

  override fun scoreUpdate(): Flow<Unit> {
    return scoreContainer.currentScore.map { score ->
      println("$score")
    }
  }

  override fun representationUpdate(): Flow<Unit> {
    return scoreContainer.currentRepresentation.map {}
  }

  override fun selectionUpdate(): Flow<Unit> {
    return selectionManager.getSelectState().map { }
  }

  override fun getErrorFlow(): Flow<ScoreError> {
    return scoreContainer.getErrorFlow()
  }

  private fun partEA(part: Int): EventAddress {
    return eas(0, dZero(), StaveId(part, 0))
  }

  private fun getPageWidth(): Int {
    return score()?.let { getOption(EventParam.LAYOUT_PAGE_WIDTH, it) } ?: PAGE_WIDTH
  }

  private fun getPageHeight(): Int {
    return score()?.let { getOption(EventParam.LAYOUT_PAGE_HEIGHT, it) } ?: PAGE_HEIGHT
  }

  private fun getPageRatio(): Float {
    return getPageHeight().toFloat() / getPageWidth()
  }

  private fun score(): Score? = scoreContainer.currentScore.value
  private fun rep(): Representation? = scoreContainer.currentRepresentation.value

  private fun select(cmd: (EventAddress, EventAddress) -> Unit) {
    selectionManager.getStartSelection()?.let { start ->
      selectionManager.getEndSelection()?.let { end ->
        cmd(start, end)
      }
    }
  }

  private fun setAreaToShow(areaToShow: AreaToShow) {
    selectionManager.setSelectedArea(
      areaToShow,
      rep()?.getAreasAtAddress(areaToShow.eventAddress.voiceIdless()) ?: listOf()
    )
  }

  private fun refreshAts() {
    selectionManager.refreshAreas { rep()?.getAreasAtAddress(it) ?: listOf() }
  }

}