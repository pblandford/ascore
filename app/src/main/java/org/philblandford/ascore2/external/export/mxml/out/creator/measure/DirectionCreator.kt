package com.philblandford.ascore.external.export.mxml.out.creator.measure

import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.numDots
import com.philblandford.kscore.engine.tempo.tempo
import com.philblandford.kscore.engine.types.*
import org.philblandford.ascore2.external.export.mxml.out.MxmlBeatUnit
import org.philblandford.ascore2.external.export.mxml.out.MxmlBeatUnitDot
import org.philblandford.ascore2.external.export.mxml.out.MxmlDashes
import org.philblandford.ascore2.external.export.mxml.out.MxmlDirection
import org.philblandford.ascore2.external.export.mxml.out.MxmlDirectionType
import org.philblandford.ascore2.external.export.mxml.out.MxmlDirectionTypeContent
import org.philblandford.ascore2.external.export.mxml.out.MxmlDynamics
import org.philblandford.ascore2.external.export.mxml.out.MxmlDynamicsComponent
import org.philblandford.ascore2.external.export.mxml.out.MxmlF
import org.philblandford.ascore2.external.export.mxml.out.MxmlFf
import org.philblandford.ascore2.external.export.mxml.out.MxmlFff
import org.philblandford.ascore2.external.export.mxml.out.MxmlFp
import org.philblandford.ascore2.external.export.mxml.out.MxmlMetronome
import org.philblandford.ascore2.external.export.mxml.out.MxmlMf
import org.philblandford.ascore2.external.export.mxml.out.MxmlMp
import org.philblandford.ascore2.external.export.mxml.out.MxmlOctaveShift
import org.philblandford.ascore2.external.export.mxml.out.MxmlP
import org.philblandford.ascore2.external.export.mxml.out.MxmlPedal
import org.philblandford.ascore2.external.export.mxml.out.MxmlPerMinute
import org.philblandford.ascore2.external.export.mxml.out.MxmlPp
import org.philblandford.ascore2.external.export.mxml.out.MxmlPpp
import org.philblandford.ascore2.external.export.mxml.out.MxmlRehearsal
import org.philblandford.ascore2.external.export.mxml.out.MxmlSffz
import org.philblandford.ascore2.external.export.mxml.out.MxmlSfp
import org.philblandford.ascore2.external.export.mxml.out.MxmlSfz
import org.philblandford.ascore2.external.export.mxml.out.MxmlStaff
import org.philblandford.ascore2.external.export.mxml.out.MxmlWedge
import org.philblandford.ascore2.external.export.mxml.out.MxmlWords
import org.philblandford.ascore2.external.export.mxml.out.durationToMxml
import kotlin.math.abs


internal fun createStartDirections(
  scoreQuery: ScoreQuery,
  eventAddress: EventAddress
): Iterable<MxmlDirection>? {


  val tempo = createTempo(scoreQuery, eventAddress)
  val rehearsal = createRehearsal(scoreQuery, eventAddress)
  val words = createWords(scoreQuery, eventAddress)
  val wedge = createWedge(scoreQuery, eventAddress, false)
  val expressionDash = createExpressionDash(scoreQuery, eventAddress, false)
  val dynamics = createDynamics(scoreQuery, eventAddress)
  val octave = createOctaveShift(scoreQuery, eventAddress, false)
  val pedal = createPedal(scoreQuery, eventAddress, false)

  return listOfNotNull(tempo, rehearsal, wedge, octave, pedal).plus(words).plus(expressionDash)
    .plus(dynamics)
}

internal fun createEndDirections(
  scoreQuery: ScoreQuery,
  eventAddress: EventAddress
): Iterable<MxmlDirection>? {


  val wedge = createWedge(scoreQuery, eventAddress, true)
  val expressionDash = createExpressionDash(scoreQuery, eventAddress, true)

  val octave = createOctaveShift(scoreQuery, eventAddress, true)
  val pedal = createPedal(scoreQuery, eventAddress, true)

  return listOfNotNull(wedge, octave, pedal).plus(expressionDash)
}


private fun createTempo(scoreQuery: ScoreQuery, eventAddress: EventAddress): MxmlDirection? {
  val content = if (eventAddress.offset == dZero() && eventAddress.staveId.sub == 1) {
    scoreQuery.getEvent(EventType.TEMPO, ez(eventAddress.barNum))?.let { tempoEvent ->
      tempo(tempoEvent)?.let { tempo ->
        val beatUnitStr = durationToMxml(tempo.duration) ?: "quarter"
        val dots = (0 until tempo.duration.numDots()).map { MxmlBeatUnitDot() }
        MxmlMetronome(MxmlBeatUnit(beatUnitStr), dots, MxmlPerMinute(tempo.bpm))
      }
    }
  } else null
  return content?.let { MxmlDirection("above", listOf(MxmlDirectionType(it))) }
}

private fun createRehearsal(scoreQuery: ScoreQuery, eventAddress: EventAddress): MxmlDirection? {
  val content = if (eventAddress.offset == dZero()) {
    scoreQuery.getParam<String>(EventType.REHEARSAL_MARK, EventParam.TEXT, ez(eventAddress.barNum))
      ?.let { mark ->
        MxmlRehearsal(mark)
      }
  } else null
  return content?.let { MxmlDirection(null, listOf(MxmlDirectionType(it))) }
}

private fun createWords(
  scoreQuery: ScoreQuery,
  eventAddress: EventAddress
): Iterable<MxmlDirection> {
  return if (eventAddress.voice == 1) {
    val tempo = if (eventAddress.staveId == StaveId(1,1)) {
      scoreQuery.getParam<String>(
        EventType.TEMPO_TEXT, EventParam.TEXT,
        eventAddress.voiceless()
      )?.let {
        MxmlDirection("above", listOf(MxmlDirectionType(MxmlWords(it))), MxmlStaff(1))
      }
    } else null

    val expression = (0..1).mapNotNull { id ->
      scoreQuery.getParam<String>(
        EventType.EXPRESSION_TEXT,
        EventParam.TEXT,
        eventAddress.copy(voice = 0, id = id)
      )
        ?.let { words ->
          val placement = if (id == 0) "above" else "below"
          MxmlDirection(
            placement,
            listOf(MxmlDirectionType(MxmlWords(words))),
            MxmlStaff(eventAddress.staveId.sub)
          )
        }
    }
    expression.plus(listOfNotNull(tempo))
  } else listOf()
}

private fun createExpressionDash(
  scoreQuery: ScoreQuery,
  eventAddress: EventAddress,
  end: Boolean
): Iterable<MxmlDirection> {

  return (0..1).mapNotNull { id ->
    createLine(
      scoreQuery, eventAddress.copy(id = id), EventType.EXPRESSION_DASH,
      { "start" },
      { _, type -> MxmlDashes(type) },
      end
    )?.let { direction ->
      if (!end) {
        scoreQuery.getParam<String>(
          EventType.EXPRESSION_DASH, EventParam.TEXT,
          eventAddress.copy(voice = 0, id = id)
        )?.let { text ->
          val wordsType = MxmlDirectionType(MxmlWords(text))
          direction.copy(directionType = direction.directionType.plus(wordsType))
        }
      } else {
        direction
      }
    }
  }
}

private fun createDynamics(
  scoreQuery: ScoreQuery,
  eventAddress: EventAddress
): Iterable<MxmlDirection> {
  return if (eventAddress.voice == 1) {
    (0..1).mapNotNull { id ->
      scoreQuery.getEvent(EventType.DYNAMIC, eventAddress.copy(voice = 0, id = id))
        ?.let { dynamic ->
          val type = dynamic.subType as DynamicType
          val up = dynamic.isTrue(EventParam.IS_UP)
          val placement = if (up) "above" else "below"
          val component = dynamicToMxl(type)
          MxmlDirection(
            placement,
            listOf(MxmlDirectionType(MxmlDynamics(component))),
            MxmlStaff(eventAddress.staveId.sub)
          )

        }
    }
  } else listOf()

}

private fun dynamicToMxl(dynamicType: DynamicType): MxmlDynamicsComponent {
  return when (dynamicType) {
    DynamicType.MOLTO_FORTISSIMO -> MxmlFff()
    DynamicType.FORTISSIMO -> MxmlFf()
    DynamicType.FORTE -> MxmlF()
    DynamicType.MEZZO_FORTE -> MxmlMf()
    DynamicType.MEZZO_PIANO -> MxmlMp()
    DynamicType.PIANO -> MxmlP()
    DynamicType.PIANISSIMO -> MxmlPp()
    DynamicType.MOLTO_PIANISSIMO -> MxmlPpp()
    DynamicType.SFORZANDISSMO -> MxmlSffz()
    DynamicType.SFORZANDO -> MxmlSfz()
    DynamicType.SFORZANDO_PIANO -> MxmlSfp()
    DynamicType.FORTE_PIANO -> MxmlFp()
  }
}

private fun createLine(
  scoreQuery: ScoreQuery,
  eventAddress: EventAddress,
  eventType: EventType,
  getType: (Event) -> String,
  getMxml: (Event, String) -> MxmlDirectionTypeContent,
  end: Boolean
): MxmlDirection? {
  return scoreQuery.getEvent(eventType, eventAddress.voiceless())?.let { event ->
    event.getParam<Duration>(EventParam.DURATION)?.let { duration ->
      val endVal = event.isTrue(EventParam.END)

      if (endVal == end || duration == dZero()) {
        val type = if (endVal && !(!end && duration == dZero())) "stop" else {
          getType(event)
        }
        val placement = if (event.isUp()) "above" else "below"
        val mxml = getMxml(event, type)
        MxmlDirection(
          placement,
          listOf(MxmlDirectionType(mxml)),
          MxmlStaff(eventAddress.staveId.sub)
        )
      } else {
        null
      }
    }
  }
}

private fun createWedge(
  scoreQuery: ScoreQuery,
  eventAddress: EventAddress,
  end: Boolean
): MxmlDirection? {

  return createLine(
    scoreQuery, eventAddress, EventType.WEDGE,
    {
      it.getParam<WedgeType>(EventParam.TYPE)?.let { wedgeType ->
        if (wedgeType == WedgeType.CRESCENDO) "crescendo" else "diminuendo"
      } ?: "crescendo"
    },
    { _, type -> MxmlWedge(type) },
    end
  )
}

private fun createOctaveShift(
  scoreQuery: ScoreQuery,
  eventAddress: EventAddress,
  end: Boolean
): MxmlDirection? {

  return createLine(
    scoreQuery, eventAddress, EventType.OCTAVE,
    {
      it.getParam<Int>(EventParam.NUMBER)?.let { num ->
        if (num < 0) "up" else "down"
      } ?: "up"
    },
    { event, type ->
      val num = event.getParam<Int>(EventParam.NUMBER)?.let { getOctaveSize(it) } ?: 8
      MxmlOctaveShift(type, num)
    },
    end
  )
}

private fun getOctaveSize(num: Int): Int {
  return abs(num) * 8 - (abs(num) - 1)
}


private fun createPedal(
  scoreQuery: ScoreQuery,
  eventAddress: EventAddress,
  end: Boolean
): MxmlDirection? {
  val address = eventAddress.copy(
    staveId = StaveId(
      eventAddress.staveId.main,
      scoreQuery.numStaves(eventAddress.staveId.main)
    )
  )
  return createLine(
    scoreQuery, address, EventType.PEDAL,
    { "start" },
    { event, type ->
      val line = if (event.subType == PedalType.STAR) "no" else "yes"
      MxmlPedal(type, line, null)
    },
    end
  )
}
