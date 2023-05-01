package com.philblandford.kscore.engine.core.score

import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.getLayoutDescriptor
import com.philblandford.kscore.engine.core.representation.*
import com.philblandford.kscore.engine.eventadder.*
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLoge
import com.philblandford.kscore.log.ksLogt
import com.philblandford.kscore.option.getOptionDefault
import com.philblandford.kscore.option.isLayoutOption
import com.philblandford.kscore.select.SelectState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CommandType {
  ADD, DELETE, SET
}

sealed class Command

data class AddCommand(
  val event: Event,
  val eventAddress: EventAddress,
  val endAddress: EventAddress?
) : Command()

data class DeleteCommand(
  val eventType: EventType, val eventAddress: EventAddress, val endAddress: EventAddress?,
  val params: ParamMap = paramMapOf()
) : Command()

data class DeleteRangeCommand(val eventAddress: EventAddress, val endAddress: EventAddress) :
  Command()

data class SetCommand<T>(
  val eventType: EventType,
  val eventParam: EventParam,
  val value: T?,
  val eventAddress: EventAddress,
  val endAddress: EventAddress? = null,
  val repUpdate: Boolean = true
) : Command()

data class UndoCommand(val undo: Boolean) : Command()

data class BatchCommand(val commands:List<Command>) : Command()

data class ScoreState(val score: Score?, val representation: Representation?)

data class ScoreError(val exception:Exception, val command: Command)

class ScoreContainer(private val drawableFactory: DrawableFactory) {

  private val coroutineScope = CoroutineScope(Dispatchers.Default)
  val currentScoreState = MutableStateFlow(ScoreState(null, null))
  var currentScore = currentScoreState.map { it.score }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
  var currentRepresentation = currentScoreState.map { it.representation }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
  private var exceptionHandler:(Exception)->Unit  = {}
  private val errorFlow = MutableSharedFlow<ScoreError>()
  private val undoStack = UndoStack()
  private val commandHistory = mutableListOf<Command>()
  private var batchStarted = false
  private var allowUndo = true
  private val commandQueue = MutableSharedFlow<Command>()
  private val batchQueue = mutableListOf<Command>()
  var synchronous = false

  init {
    listenForCommands()
  }

  fun setExceptionHandler(handler:(Exception)->Unit) {
    exceptionHandler = handler
  }

  fun setNewScore(score: Score, progress: (String, Float) -> Boolean = { _, _ -> false }) {
    val newRepresentation =
      scoreToRepresentation(
        score,
        drawableFactory,
        SelectState(),
        getLayoutDescriptor(score),
        progress = progress
      )
    setScoreState(score, newRepresentation)
    undoStack.clear()
    commandHistory.clear()
  }

  fun setNewScoreNoRepresentation(score: Score) {
    setScoreState(score, null)
    undoStack.clear()
    commandHistory.clear()
  }

  fun getErrorFlow():Flow<ScoreError> = errorFlow

  fun addEvent(
    event: Event,
    eventAddress: EventAddress = eZero(),
    endAddress: EventAddress? = null,
  ) {
    addCommand(AddCommand(event, eventAddress, endAddress))
  }

  fun deleteEvent(
    eventType: EventType, eventAddress: EventAddress = eZero(), endAddress: EventAddress? = null
  ) {
    addCommand(DeleteCommand(eventType, eventAddress, endAddress))
  }

  fun deleteRange(start: EventAddress, end: EventAddress) {
    addCommand(DeleteRangeCommand(start, end))
  }

  fun <T> setParam(
    eventType: EventType, eventParam: EventParam, value: T?,
    eventAddress: EventAddress, endAddress: EventAddress? = null, repUpdate: Boolean = true
  ) {
    addCommand(SetCommand(eventType, eventParam, value, eventAddress, endAddress, repUpdate))
  }

  fun undo() {
    addCommand(UndoCommand(true))
  }

  fun redo() {
    addCommand(UndoCommand(false))
  }

  fun resetUndo() {
    undoStack.clear()
  }

  private fun ScoreState.push() {
    if (!batchStarted) {
      score?.let { s ->
        representation?.let { r ->
          undoStack.push(s, r)
        }
      }
    }
  }

  fun batch(vararg cmds:()->Unit) {
    startBatch()
    cmds.forEach { it.invoke() }
    endBatch()
    addCommand(BatchCommand(batchQueue))
  }

  private fun ScoreState.runBatch(commands:List<Command>):ScoreState {
    return commands.toList().fold(this) { score, cmd ->
      score.applyCommand(cmd)
    }
  }

  private fun startBatch() {
    batchStarted = true
    batchQueue.clear()
  }

  fun pauseUndo(yes: Boolean) {
    allowUndo = !yes
  }

  private fun endBatch() {
    batchStarted = false
  }

  fun getCommandHistory(): List<Command> = commandHistory

  private fun listenForCommands() {
    coroutineScope.launch {
      commandQueue.collectLatest { command ->
        receiveCommand(command)
      }
    }
  }

  private fun addCommand(
    command: Command
  ) {
    if (synchronous) {
      val newScoreState = currentScoreState.value.applyCommand(command)
      currentScoreState.value = newScoreState
    } else {
      if (batchStarted) {
        batchQueue.add(command)
      } else {
        commandHistory.add(command)
        coroutineScope.launch {
          commandQueue.emit(command)
        }
      }
    }
  }

  private suspend fun receiveCommand(command: Command) {
    ksLoge("Double note SC dequeue $command")

    try {
        val newScoreState = currentScoreState.value.applyCommand(command)
        currentScoreState.emit(newScoreState)
    } catch (e:Exception) {
      ksLoge("SC caught error", e)
        errorFlow.emit(ScoreError(e, command))
    }
  }

  private fun ScoreState.applyCommand(command: Command):ScoreState {
    return try {
      when (command) {
        is AddCommand -> {
          push()
          addOrDelete(command.event, command.eventAddress, true, command.endAddress)
        }

        is BatchCommand -> {
          push()
          runBatch(command.commands)
        }

        is DeleteCommand -> {
          push()
          addOrDelete(Event(command.eventType), command.eventAddress, false, command.endAddress)
        }

        is DeleteRangeCommand -> {
          push()
          doDeleteRange(command.eventAddress, command.endAddress)
        }

        is SetCommand<*> -> {
          push()
          doSetParam(
            command.eventType,
            command.eventParam,
            command.value,
            command.eventAddress,
            command.endAddress,
            command.repUpdate
          )
        }

        is UndoCommand -> {
          if (command.undo) doUndo() else doRedo()
        }
      }
    } catch (e:Exception) {
      ksLoge("Failed applying $command", e)
      coroutineScope.launch {
        errorFlow.emit(ScoreError(e, command))
      }
      this
    }
  }


  private fun ScoreState.addOrDelete(
    event: Event, eventAddress: EventAddress = eZero(), add: Boolean = true,
    endAddress: EventAddress? = null,
  ): ScoreState {
    return score?.let { score ->
      val address = if (eventAddress.isWild()) score.getMarker()?.copy(voice = eventAddress.voice) else eventAddress
      address?.let {
        val sr = if (add) {
          score.addEvent(event, address, endAddress)
        } else {
          score.deleteEvent(event.eventType, event.params, address, endAddress)
        }?.let { ScoreReturn(it) }

        sr?.let {
          if (sr.scoreLevel != score) {
            updateScore(sr.scoreLevel as Score, sr.repUpdate)
          } else {
            this
          }
        }
      }
    } ?: this
  }

  private fun ScoreState.doDeleteRange(start: EventAddress, end: EventAddress):ScoreState {

    return score?.let { score ->
      val newScore = when (val res = NewEventAdder.deleteRange(score, start, end)) {
        is Right -> res.r
        is Warning -> res.r
        is AbortNoError -> null
        is Left -> throw Exception(res.l.message, res.l.exception)
      }
      newScore?.let { updateScore(it) }
    } ?: this
  }

  fun updateScore(score: Score) {
    coroutineScope.launch {
      val newScore = currentScoreState.value.updateScore(score)
      currentScoreState.emit(newScore)
    }
  }

  private fun ScoreState.updateScore(
    newScore:Score,
    repUpdate: RepUpdate = RepUpdateFull
  ):ScoreState {
    val newRep = score?.let { oldScore ->
        if (repUpdate is RepUpdateNone) representation else representation?.update(
          oldScore,
          newScore
        )
    }
    return ScoreState(newScore, newRep)
  }


  private fun <T> ScoreState.doSetParam(
    eventType: EventType, eventParam: EventParam, value: T,
    eventAddress: EventAddress, endAddress: EventAddress? = null, repUpdate: Boolean = true
  ):ScoreState {

    ksLogt("SetParam $eventType $eventParam $value $eventAddress $endAddress")
    return score?.let { score ->
      val newScore = score.setParam(eventType, eventParam, value, eventAddress, endAddress)

      newScore?.let {
        if (newScore != score) {
          updateScore(newScore, if (repUpdate) RepUpdateFull else RepUpdateNone)
        } else {
          this
        }
      }
    } ?: this
  }

  private fun ScoreState.doUndo():ScoreState {
    return yes { s, r ->
      undoStack.undo(s, r)?.let {
        ScoreState(it.score, it.representation)
      } ?: this
    }
  }

  private fun ScoreState.doRedo():ScoreState {
    return yes { s, r ->
      undoStack.redo(s, r)?.let {
        ScoreState(it.score, it.representation)
      } ?: this
    }

  }


  fun <T> getParam(
    eventType: EventType,
    eventParam: EventParam,
    eventAddress: EventAddress,
    default: T?
  ): T? {
    return currentScoreState.value.score?.getParam<T>(eventType, eventParam, eventAddress) ?: default
  }

  fun setOption(option: EventParam, value: Any?) {
    val eventType = if (isLayoutOption(option)) EventType.LAYOUT else EventType.OPTION
    setParam(eventType, option, value, eZero())
  }

  fun <T> getOption(option: EventParam, default: T? = null): T? {
    val eventType = if (isLayoutOption(option)) EventType.LAYOUT else EventType.OPTION
    return getParam<T>(eventType, option, eZero(), default) ?: getOptionDefault(option)
  }

  fun setSelectedPart(part: Int) {
    val new = currentScore.value?.selectedPart() != part
    setParam(EventType.UISTATE, EventParam.SELECTED_PART, part, eZero())
    if (new) {
      undoStack.clear()
    }
  }

  fun noUndo(cmd: ScoreContainer.() -> Unit) {
    pauseUndo(true)
    cmd()
    pauseUndo(false)
  }

  private fun ScoreState.yes(cmd: (Score, Representation) -> ScoreState):ScoreState {
    return score?.let { s ->
      representation?.let { r ->
        cmd(s, r)
      }
    } ?: this
  }

  private fun setScoreState(score: Score?, representation: Representation?) {
    if (synchronous) {
      currentScoreState.value = ScoreState(score, representation)
    } else {
      coroutineScope.launch {
        currentScoreState.emit(ScoreState(score, representation))
      }
    }
  }

}

