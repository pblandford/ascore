import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.api.InstrumentGroup
import com.philblandford.kscore.api.defaultInstrument
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.dsl.empty
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.dsl.tupletMarker
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.map.eventMapOf
import com.philblandford.kscore.engine.scorefunction.ScoreTest
import com.philblandford.kscore.engine.time.TimeSignature

fun assertEqual(expected: Any?, candidate: Any?) {
  if (expected!!::class != candidate!!::class) {
    throw Exception("Expected ${expected::class} got ${candidate::class}")
  }
  if (expected != candidate) {
    throw Exception("Expected\n${expected} got \n${candidate}")

  }
}

fun assertNotEqual(expected: Any?, candidate: Any?) {

  if (expected == candidate) {
    throw Exception("Expected\n${expected} got \n${candidate}")

  }
}

fun <T> assertListEqual(expected: List<T>, candidate: List<T>) {
  if (expected.size != candidate.size) {
    throw Exception("expected size ${expected.size}, got size ${candidate.size}")
  }
  expected.zip(candidate).withIndex().forEach { iv ->
    try {
      assertEqual(iv.value.first, iv.value.second)
    } catch (e: Exception) {
      throw Exception("${e.message} at ${iv.index}")
    }
  }
}


private fun Iterable<Event>.compare(other: Iterable<Event>) {
  toList().zip(other) { one, two ->
    one.compare(two)
  }
}

private class CompareException(name: String?, msg: String) : java.lang.Exception("$name: $msg")

private val ignorableTypes = setOf(EventType.UISTATE)

private fun EventMap.compare(name: String?, other: EventMap) {
  if (getEventTypes().minus(ignorableTypes).sorted() != other.getEventTypes().minus(ignorableTypes)
      .sorted()
  ) {
    throw CompareException(name, "\nthis: ${getEventTypes()}\nthat: ${other.getEventTypes()}")
  }
  val thisEvents =
    getAllEvents().filterNot { ignorableTypes.contains(it.key.eventType) }.toList()
      .sortedBy { it.first }
  val thatEvents =
    other.getAllEvents().filterNot { ignorableTypes.contains(it.key.eventType) }.toList()
      .sortedBy { it.first }
  if (thisEvents.count() != thatEvents.count()) {
    throw CompareException(name, "\nthis ${thisEvents.count()}\n${thatEvents.count()}")
  }
  thisEvents.zip(thatEvents) { one, two ->
    one.second.compare(two.second)
  }
}

private fun Event.compare(other: Event) {
  if (eventType != other.eventType) {
    throw CompareException(eventType.toString(), "$eventType != ${other.eventType}")
  }
  val thisParamKeys = params.keys
  val thatParamKeys = other.params.keys
  if (thisParamKeys != thatParamKeys) {
    throw CompareException(eventType.toString(), "\n$thisParamKeys\n$thatParamKeys")
  }
  params.toList().sortedBy { it.first }
    .zip(other.params.toList().sortedBy { it.first }) { one, two ->
      if (one.first == EventParam.NOTES) {
        (one.second as Iterable<Event>).compare(two.second as Iterable<Event>)
      }
      if (one != two) {
        throw CompareException(eventType.toString(), "\n$one\n$two")
      }
    }
}

fun ScoreLevel.compare(other: ScoreLevel) {
  val name = this::class.simpleName

  eventMap.compare(name, other.eventMap)
  if (this is VoiceMap) {
    vmCompare(other as VoiceMap)
  }

  val children = getAllSubLevels()
  val otherChildren = other.getAllSubLevels()
  if (children.count() != otherChildren.count()) {
    throw CompareException(name, "${children.count()} != ${otherChildren.count()}")
  }
  children.zip(otherChildren).forEach { (one, two) ->
    one.compare(two)
  }
}

private fun VoiceMap.vmCompare(other: VoiceMap) {
  val thisBeams = beamMap.toList().sortedBy { it.first }
  val thatBeams = other.beamMap.toList().sortedBy { it.first }

  if (thisBeams.count() != thatBeams.count()) {
    throw CompareException("BeamMap", "${thisBeams.count()} != ${thatBeams.count()}")
  }

  thisBeams.zip(thatBeams).forEach { (one, two) ->
    if (one != two) {
      throw CompareException("BeamMap", "\n$one\n$two")
    }
  }
}

fun voiceMapFromString(
  string: String,
  timeSignature: TimeSignature = TimeSignature(4, 4)
): VoiceMap {
  val voiceEvents = getHashFromString(string)
  return voiceMap(timeSignature, eventMapOf(voiceEvents))
}

fun stringToVoiceEventMap(string: String): VoiceEventMap {
  val hash = getHashFromString(string)
  return toVoiceEvents(hash)
}

fun String.toHash(): EventHash {
  return getHashFromString(this)
}

fun getHashFromString(string: String, graceGroupOffset: Offset? = null): EventHash {
  if (string.isEmpty()) {
    return eventHashOf()
  }
  val fields = string.split(":")
  var currentOffset = dZero()
  val res = fields.map { field ->
    val dString = field.drop(1)
    val duration = getDuration(dString)
    val event: Event = when (field[0]) {
      'C' -> dslChord(duration)
      'E' -> empty(duration)
      'R' -> rest(duration)
      'T' -> tupletMarker(duration)
      else -> dslChord(duration)
    }
    val offset = currentOffset
    currentOffset = offset.add(duration)
    graceGroupOffset?.let { ggo ->
      EMK(EventType.DURATION, eag(1, ggo, offset)) to event

    } ?: run {
      EMK(EventType.DURATION, ea(1, offset)) to event
    }
  }
  return res.toMap()
}

fun getDuration(str: String): Duration {
  val fields = str.split("/")
  if (fields.size == 1) {
    return Duration(1, str.toInt())
  } else {
    return Duration(fields[0].toInt(), fields[1].toInt())
  }
}

fun ScoreTest.createCrotchets(numBars: Int, staveId: StaveId = StaveId(1, 1)) {
  repeat(numBars) { bar ->
    repeat(4) { offset ->
      SMV(eventAddress = easv(bar + 1, crotchet() * offset, staveId))
    }
  }
}

fun ScoreTest.grace(
  offset: Offset? = dZero(), bar: Int = 1, mode: GraceInputMode = GraceInputMode.SHIFT,
  type: GraceType = GraceType.APPOGGIATURA, midiVal: Int = 72,
  duration: Duration = semiquaver(),
  mainOffset: Offset = dZero(), voice: Int = 1, params: ParamMap = paramMapOf()
) {
  SMV(
    midiVal,
    duration = duration,
    eventAddress = eagv(bar, mainOffset, offset, voice),
    extraParams = params + paramMapOf(EventParam.GRACE_TYPE to type, EventParam.GRACE_MODE to mode)
  )
}

fun ScoreTest.graceRest(
  offset: Offset? = dZero(), bar: Int = 1, mode: GraceInputMode = GraceInputMode.SHIFT,
  type: GraceType = GraceType.APPOGGIATURA,
  duration: Duration = semiquaver(),
  mainOffset: Offset = dZero(), voice: Int = 1, params: ParamMap = paramMapOf()
) {
  SAE(
    rest(duration).addParams(
      params.plus(
        paramMapOf(
          EventParam.GRACE_TYPE to type,
          EventParam.GRACE_MODE to mode
        )
      )
    ),
    eagv(bar, mainOffset, offset, voice)
  )
}

const val InstrumentData = """
Piano:
Piano,0,1,TREBLE:BASS,Pno
Bright Acoustic Piano,0,2,TREBLE:BASS,Pno
Electric Grand Piano,0,3,TREBLE:BASS,El. Pno
Honky-tonk Piano,0,4,TREBLE:BASS,Pno
Electric Piano 1,0,5,TREBLE:BASS,El. Pno
Electric Piano 2,0,6,TREBLE:BASS,El. Pno
Harpsichord,0,7,TREBLE:BASS,Hps
Clavichord,0,8,TREBLE:BASS,Clv
Chromatic Percussion:
Celesta,12,9,TREBLE:BASS,Cst
Glockenspiel,24,10,TREBLE,Glock
Music Box,0,11,TREBLE
Vibraphone,0,12,TREBLE,Vibe
Marimba,0,13,TREBLE
Xylophone,12,14,TREBLE,Xyl
Tubular Bells,0,15,TREBLE,Tub. Bells
Dulcimer,0,16,TREBLE,Dulc.
Organ:
Drawbar Organ,0,17,TREBLE:BASS,Organ
Percussive Organ,0,18,TREBLE:BASS,Organ
Rock Organ,0,19,TREBLE:BASS,Organ
Church Organ,0,20,TREBLE:BASS,Organ
Reed Organ,0,21,TREBLE:BASS,Organ
Accordion,0,22,TREBLE,Acc
Harmonica,0,23,TREBLE,Harm
Tango Accordion,0,24,TREBLE,Acc
Guitar:
Acoustic Guitar (nylon),-12,25,TREBLE,Guitar
Acoustic Guitar (steel),-12,26,TREBLE,Guitar
Electric Guitar (jazz),-12,27,TREBLE,Guitar
Electric Guitar (clean),-12,28,TREBLE,Guitar
Electric Guitar (muted),-12,29,TREBLE,Guitar
Overdriven Guitar,0,30,TREBLE,Guitar
Distortion Guitar,0,31,TREBLE,Guitar
Guitar harmonics,0,32,TREBLE,Guitar
Bass:
Acoustic Bass,0,33,BASS,Ac. Bass
Electric Bass (finger),0,34,BASS,El. Bass
Electric Bass (pick),0,35,BASS,El. Bass
Fretless Bass,0,36,BASS,El. Bass
Slap Bass 1,0,37,BASS,Slap Bass
Slap Bass 2,0,38,BASS,Slap Bass
Synth Bass 1,0,39,BASS,Synth Bass
Synth Bass 2,0,40,BASS,Synth Bass
Strings:
Violin,0,41,TREBLE,Vln
Viola,0,42,ALTO,Vla
Cello,0,43,BASS,Vc
Contrabass,-12,44,BASS,Dbl Bass
Tremolo Strings,0,45,TREBLE,Trem Strings
Pizzicato Strings,0,46,TREBLE,Pizz Strings
Orchestral Harp,0,47,TREBLE:BASS,Harp
Timpani,0,48,BASS,Timp
Ensemble:
String Ensemble 1,0,49,TREBLE,Strings
String Ensemble 2,0,50,TREBLE,Strings
Synth Strings 1,0,51,TREBLE,Synth Strings
Synth Strings 2,0,52,TREBLE,Synth Strings
Choir Aahs,0,53,TREBLE,Choir
Voice Oohs,0,54,TREBLE,Choir
Synth Voice,0,55,TREBLE,Voice
Orchestra Hit,0,56,TREBLE,Orch.
Brass:
Trumpet,-2,57,TREBLE,Tpt
Trombone,0,58,BASS,Tbn
Tuba,0,59,BASS,Tba
Muted Trumpet,-2,60,TREBLE,Tpt
French Horn,-7,61,TREBLE,Hn
Brass Section,0,62,TREBLE,Brass
Synth Brass 1,0,63,TREBLE,Synth Brass
Synth Brass 2,0,64,TREBLE,Synth Brass
Reed:
Soprano Sax,-2,65,TREBLE,Sop. Sax
Alto Sax,-9,66,TREBLE,A. Sax
Tenor Sax,-14,67,TREBLE,Tn. Sax
Baritone Sax,-21,68,BASS,Bar. Sax
Oboe,0,69,TREBLE,Ob
English Horn,-7,70,TREBLE,Eng. Horn
Bassoon,0,71,BASS,Bsn
Clarinet,-2,72,TREBLE,Clar
Clarinet in A,-3,72,TREBLE,Clar
Pipe:
Piccolo,12,73,TREBLE,Picc
Flute,0,74,TREBLE,Fl
Recorder,0,75,TREBLE,Rec
Pan Flute,0,76,TREBLE
Blown Bottle,0,77,TREBLE
Shakuhachi,0,78,TREBLE
Whistle,0,79,TREBLE
Ocarina,0,80,TREBLE
Synth Lead:
Lead 1 (square),0,81,TREBLE
Lead 2 (sawtooth),0,82,TREBLE
Lead 3 (calliope),0,83,TREBLE
Lead 4 (chiff),0,84,TREBLE
Lead 5 (charang),0,85,TREBLE
Lead 6 (voice),0,86,TREBLE
Lead 7 (fifths),0,87,TREBLE
Lead 8 (bass + lead),0,88,TREBLE
Synth Pad:
Pad 1 (new age),0,89,TREBLE
Pad 2 (warm),0,90,TREBLE
Pad 3 (polysynth),0,91,TREBLE
Pad 4 (choir),0,92,TREBLE
Pad 5 (bowed),0,93,TREBLE
Pad 6 (metallic),0,94,TREBLE
Pad 7 (halo),0,95,TREBLE
Pad 8 (sweep),0,96,TREBLE
Percussion:
SET,Bongos,1,,2 2:60:High_Bongo:U:N 6:61:Low_Bongo:U:N
SET,Kit,1,,5 -2:49:Crash_Cymbal:U:C -1:46:Open_Hi-hat:U:C 0:51:Ride_Cymbal:U:C 1:48:High_Tom:U:N 2:43:Low_Tom:U:N 3:37:Rim_Shot:U:C 3:40:Snare_Drum:U:N 7:35:Bass_Drum_1:D:N 8:36:Bass_Drum_2:D:N 9:44:Pedal_Hi-Hat:D:C
Bass Drum 1,0,N,35,PERCUSSION
Bass Drum 2,0,N,36,PERCUSSION
Side Stick/Rimshot,0,N,37,PERCUSSION
Snare Drum 1,0,N,38,PERCUSSION
Hand Clap,0,N,39,PERCUSSION
Snare Drum 2,0,N,40,PERCUSSION
Low Tom 2,0,N,41,PERCUSSION
Closed Hi-hat,0,N,42,PERCUSSION
Low Tom 1,0,N,43,PERCUSSION
Pedal Hi-hat,0,N,44,PERCUSSION
Mid Tom 2,0,N,45,PERCUSSION
Open Hi-hat,0,C,46,PERCUSSION
Mid Tom 1,0,N,47,PERCUSSION
High Tom 2,0,N,48,PERCUSSION
Crash Cymbal 1,0,N,49,PERCUSSION
High Tom 1,0,N,50,PERCUSSION
Ride Cymbal 1,0,N,51,PERCUSSION
Chinese Cymbal,0,N,52,PERCUSSION
Ride Bell,0,N,53,PERCUSSION
Tambourine,0,N,54,PERCUSSION
Splash Cymbal,0,N,55,PERCUSSION
Cowbell,0,N,56,PERCUSSION
Crash Cymbal 2,0,N,57,PERCUSSION
Vibra Slap,0,N,58,PERCUSSION
Ride Cymbal 2,0,N,59,PERCUSSION
High Bongo,0,N,60,PERCUSSION
Low Bongo,0,N,61,PERCUSSION
Mute High Conga,0,N,62,PERCUSSION
Open High Conga,0,N,63,PERCUSSION
Low Conga,0,N,64,PERCUSSION
High Timbale,0,N,65,PERCUSSION
Low Timbale,0,N,66,PERCUSSION
High Agogô,0,N,67,PERCUSSION
Low Agogô,0,N,68,PERCUSSION
Cabasa,0,N,69,PERCUSSION
Maracas,0,N,70,PERCUSSION
Short Whistle,0,N,71,PERCUSSION
Long Whistle,0,N,72,PERCUSSION
Short Güiro,0,N,73,PERCUSSION
Claves,0,N,75,PERCUSSION
High Wood Block,0,N,76,PERCUSSION
Low Wood Block,0,N,77,PERCUSSION
Mute Cuíca,0,N,78,PERCUSSION
Open Cuíca,0,N,79,PERCUSSION
Mute Triangle,0,N,80,PERCUSSION
Open Triangle,0,N,81,PERCUSSION
"""

class TestInstrumentGetter : InstrumentGetter {
  private val groups = listOf(
    InstrumentGroup("Piano", listOf(i("Piano"), i("Harpsichord"))),
    InstrumentGroup("Strings", listOf(i("Violin"), i("Viola")))
  )


  override fun refresh() {
  }

  override fun getInstrumentGroups(): List<InstrumentGroup> {
    return groups
  }

  override fun getInstrument(name: String): Instrument? {
    return groups.flatMap { it.instruments }.find { it.name == name }
  }

  override fun getInstrumentGroup(name: String): InstrumentGroup? {
    return groups.find { it.name == name }
  }

  override fun assignInstrument(instrumentName: String, group: String) {

  }

  override fun createGroup(name: String, instruments: List<Instrument>) {

  }

  override fun clearUser() {

  }

  override fun getInstrument(programId: Int): Instrument? {
    return groups.flatMap { it.instruments }.find { it.program == programId }
  }

  private fun i(name: String): Instrument {
    return defaultInstrument().copy(name = name)
  }
}