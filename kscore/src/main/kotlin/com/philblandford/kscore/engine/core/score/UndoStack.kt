package com.philblandford.kscore.engine.core.score

import com.philblandford.kscore.engine.core.representation.Representation
import com.philblandford.kscore.log.ksLoge

data class ScoreRep(val score:Score, val representation: Representation)

private const val MAX_STACK = 20

class UndoStack {

  private val undoStack = mutableListOf<ScoreRep>()
  private val redoStack = mutableListOf<ScoreRep>()

  private fun MutableList<ScoreRep>.push(scoreRep: ScoreRep) {
    add(scoreRep)
    if (size > MAX_STACK) {
      removeAt(0)
    }
  }

  fun clear() {
    undoStack.clear()
    redoStack.clear()
  }

  fun push(score: Score, representation: Representation) {
    undoStack.push(ScoreRep(score, representation))
    redoStack.clear()
  }

  fun undo(currentScore:Score, currentRep:Representation): ScoreRep? {
    return undoStack.lastOrNull()?.let {
      undoStack.remove(it)
      redoStack.push(ScoreRep(currentScore, currentRep))
      it
    }
  }

  fun redo(currentScore:Score, currentRep:Representation): ScoreRep? {
    return redoStack.lastOrNull()?.let {
      redoStack.remove(it)
      undoStack.push(ScoreRep(currentScore, currentRep))
      it
    }
  }

}