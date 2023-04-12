package com.philblandford.kscore.engine.newadder.subadders

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.newadder.EventDestination
import com.philblandford.kscore.engine.newadder.Right
import com.philblandford.kscore.engine.newadder.ScoreResult
import com.philblandford.kscore.engine.newadder.duration.UpDownSubAdder
import com.philblandford.kscore.engine.newadder.then
import com.philblandford.kscore.engine.newadder.util.changeSubLevel
import com.philblandford.kscore.engine.types.*

internal object ExpressionTextSubAdder : UpDownSubAdder {

  override fun addEvent(
    score: Score,
    destination: EventDestination,
    eventType: EventType,
    params: ParamMap,
    eventAddress: EventAddress
  ): ScoreResult {

    return super.addEvent(
      score,
      destination,
      eventType,
      params,
      adjustAddress(eventAddress, params)
    )
      .then {
        it.setInstrument(eventAddress, params)
      }
  }

  private fun Score.setInstrument(
    eventAddress: EventAddress,
    params: ParamMap
  ): ScoreResult {
    return params.g<String>(EventParam.TEXT)?.let { text ->
      getStave(eventAddress.staveId)?.let { stave ->
        findInstrument(text, eventAddress)?.let { instrument ->
          val em = stave.eventMap.putEvent(eventAddress.staveless(), instrument.toEvent())
          changeSubLevel(stave.replaceSelf(em), eventAddress)
        }
      }
    } ?: Right(this)
  }

  private fun Score.findInstrument(
    text: String,
    eventAddress: EventAddress
  ): Instrument? {
    val startInstrument = getInstrument(eventAddress.start())
//    return when (text.toLowerCase(Locale.getDefault()).dropLastWhile { !it.isLetter() }) {
//      "pizz" -> Sound.getInstrument("Pizzicato Strings")
//      "arco" -> startInstrument
//      "mute" -> Sound.getInstrument("Muted Trumpet")
//      "muted" -> Sound.getInstrument("Muted Trumpet")
//      "open" -> startInstrument
//      else -> Sound.getInstrument(text)
//    }
    return null
  }
}