package com.philblandford.ascore.external.export.mxml.out.creator.measure

import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.minus
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.pitch.harmony
import com.philblandford.kscore.engine.types.*
import org.philblandford.ascore2.external.export.mxml.out.MxmlBass
import org.philblandford.ascore2.external.export.mxml.out.MxmlBassAlter
import org.philblandford.ascore2.external.export.mxml.out.MxmlBassStep
import org.philblandford.ascore2.external.export.mxml.out.MxmlHarmony
import org.philblandford.ascore2.external.export.mxml.out.MxmlKind
import org.philblandford.ascore2.external.export.mxml.out.MxmlOffset
import org.philblandford.ascore2.external.export.mxml.out.MxmlRoot
import org.philblandford.ascore2.external.export.mxml.out.MxmlRootAlter
import org.philblandford.ascore2.external.export.mxml.out.MxmlRootStep


internal class HarmonyLookup(val harmonyEvents: EventHash, durationEvents: EventHash) {

  val durationLocations =
    durationEvents.map { it.key.eventAddress.voiceIdless() }.toSet()

  val homeless = mutableMapOf<EventAddress, Iterable<Pair<EventAddress, Event>>>()

  var lastLocation = dZero()
  var lastHomed = dZero()

  init {
    harmonyEvents.toList().forEach { (key, event) ->
      if (!durationLocations.contains(key.eventAddress)) {
        val newEvent = event.addParam(EventParam.DURATION to key.eventAddress.offset - lastLocation)
        val hKey = key.eventAddress.copy(offset = lastHomed)
        val list = homeless[hKey] ?: listOf()
        homeless[hKey] = list.plus(key.eventAddress to newEvent)
      } else {
        lastHomed = key.eventAddress.offset
      }
      lastLocation = key.eventAddress.offset
    }
  }

  fun getHarmonies(eventAddress: EventAddress): Iterable<Event> {
    val harmonies =
      harmonyEvents[EMK(EventType.HARMONY, eventAddress)]?.let { listOf(it) } ?: listOf()
    return homeless[eventAddress]?.let {
      harmonies.plus(it.filterNot { it.first == eventAddress }.map { it.second })
    } ?: harmonies
  }

}


internal fun createHarmony(
  harmonyLookup: HarmonyLookup,
  eventAddress: EventAddress,
  divisions: Int
): Iterable<MxmlHarmony> {
  return harmonyLookup.getHarmonies(eventAddress).mapNotNull { event ->
    harmony(event)?.let { harmony ->
      nameToDesc[harmony.quality]?.let { desc ->
        val bass = harmony.root?.createBass()
        val offset =
          event.getParam<Duration>(EventParam.DURATION)?.toMxml(divisions)?.let { offset ->
            MxmlOffset(offset)
          }
        MxmlHarmony(harmony.tone.createRoot(), MxmlKind(desc.name), bass, offset)
      }
    }
  }
}

private fun Pitch.createRoot(): MxmlRoot {
  return MxmlRoot(
    MxmlRootStep(noteLetter.toString()),
    MxmlRootAlter(accidental.toAlter())
  )
}


private fun Pitch.createBass(): MxmlBass {
  return MxmlBass(
    MxmlBassStep(noteLetter.toString()),
    MxmlBassAlter(accidental.toAlter())
  )
}

private fun Accidental.toAlter(): Int {
  return when (this) {
    Accidental.DOUBLE_FLAT -> -2
    Accidental.FLAT -> -1
    Accidental.NATURAL -> 0
    Accidental.SHARP -> 1
    Accidental.DOUBLE_SHARP -> 2
    else -> 0
  }
}

data class Degree(val value: String, val alter: Int, val add: Boolean = true)
data class HarmonyDesc(
  val name: String,
  val extension: String?,
  val useSymbol: Boolean,
  val degrees: Iterable<Degree>
)

val nameToDesc = mapOf(
  "" to HarmonyDesc("major", null, false, listOf()),
  "m" to HarmonyDesc("minor", "m", false, listOf()),
  "-" to HarmonyDesc("minor", null, true, listOf()),
  "6" to HarmonyDesc("major-sixth", "6", false, listOf()),
  "m6" to HarmonyDesc("minor-sixth", "6", false, listOf()),
  "-6" to HarmonyDesc("minor-sixth", null, true, listOf()),
  "2" to HarmonyDesc("major", "2", false, listOf(Degree("2", 0))),
  "7" to HarmonyDesc("dominant", "7", false, listOf()),
  "M7" to HarmonyDesc("major-seventh", "M7", false, listOf()),
  "\u0394" to HarmonyDesc("major", null, true, listOf()),
  "m7" to HarmonyDesc("minor-seventh", "m7", false, listOf()),
  "-7" to HarmonyDesc("minor-seventh", null, true, listOf()),
  "m\u03947" to HarmonyDesc("major-minor", "m^7", false, listOf()),
  "-\u03947" to HarmonyDesc("major-minor", null, true, listOf()),
  "\u2300" to HarmonyDesc("half-diminished", null, true, listOf()),
  "\u00b0" to HarmonyDesc("diminished", null, true, listOf()),
  "dim" to HarmonyDesc("diminished", null, false, listOf()),
  "\u00b07" to HarmonyDesc("diminished-seventh", "7", true, listOf()),
  "dim7" to HarmonyDesc("diminished-seventh", "7", true, listOf()),
  "+" to HarmonyDesc("augmented", null, true, listOf()),
  "+7" to HarmonyDesc("augmented-seventh", "7", true, listOf()),
  "9" to HarmonyDesc("dominant-ninth", "9", false, listOf()),
  "13" to HarmonyDesc("dominant-13th", "13", false, listOf()),
  "\u266d9" to HarmonyDesc("dominant", "7", false, listOf(Degree("9", -1))),
  "+11" to HarmonyDesc("dominant", "7", false, listOf(Degree("11", +1))),
  "\u266d13" to HarmonyDesc("dominant", "7", false, listOf(Degree("13", -1))),
  "Alt" to HarmonyDesc(
    "dominant", "7", false, listOf(
      Degree("5", -1),
      Degree("5", 1), Degree("9", -1), Degree("9", 1)
    )
  )
)
val descToName = nameToDesc.map { it.value to it.key }

//fun getName(kind: String, kindText: String?, useSymbols: Boolean, degrees: List<Degree>): String  {
//  (kind, kindText, useSymbols, degrees) match {
//    case("major", _, false, Vector(Degree("2", 0, _))) => "2"
//    case("major", _, false, _) => ""
//    case("major", _, true, _) => "\u0394"
//    case("minor", _, false, _) => "m"
//    case("minor", _, true, _) => "-"
//    case("major-sixth", _, _, _) => "6"
//    case("minor-sixth", _, false, _) => "m6"
//    case("minor-sixth", _, true, _) => "-6"
//    case("dominant", _, _, Vector()) => "7"
//    case("major-seventh", _, false, _) => "M7"
//    case("minor-seventh", _, false, _) => "m7"
//    case("minor-seventh", _, true, _) => "-7"
//    case("major-minor", _, false, _) => "m\u03947"
//    case("major-minor", _, true, _) => "-\u03947"
//    case("half-diminished", _, _, Vector()) => "\u2300"
//    case("diminished", _, true, _) => "\u00b0"
//    case("diminished", _, false, _) => "dim"
//    case("diminished-seventh", _, true, _) => "\u00b07"
//    case("diminished-seventh", _, false, _) => "dim7"
//    case("augmented", _, _, _) => "+"
//    case("augmented-seventh", _, _, _) => "+7"
//    case("dominant-ninth", _, _, _) => "9"
//    case("dominant-13th", _, _, _) => "13"
//    case("dominant", _, _, Vector(Degree("9", -1, _))) => "\u266d9"
//    case("dominant", _, _, Vector(Degree("11", 1, _))) => "+11"
//    case("dominant", _, _, Vector(Degree("13", -1, _))) => "\u266d13"
//    case("dominant", _, _, Vector(Degree("5", 1, _))) => "+7"
//    case(
//      "dominant", _, _, Vector(
//        Degree("5", -1, _),
//        Degree("5", 1, _), Degree("9", -1, _), Degree("9", 1, _)
//      )
//    ) => "Alt"
//    case _ => ""
//
//  }
//}
