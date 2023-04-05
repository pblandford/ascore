package com.philblandford.ascore.external.export.out

import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.types.ScoreQuery
import com.philblandford.kscore.sound.*
import com.philblandford.kscore.util.highestBit
import com.philblandford.kscore.util.numBytes


@ExperimentalUnsignedTypes
private typealias ByteList = MutableList<UByte>
@ExperimentalUnsignedTypes
private fun byteListOf(vararg bytes:Byte) = mutableListOf(*bytes.map { it.toUByte() }.toTypedArray())

private var ticks = 480

fun writeMidi(scoreQuery: ScoreQuery, instrumentGetter: InstrumentGetter):ByteArray {
  val map = midiPlayLookup(scoreQuery, instrumentGetter)
  return writeMidi(map)
}

fun writeMidi(midiPlayLookup: MidiPlayLookup):ByteArray {
  val midiFile = midiFile(midiPlayLookup)
  return midiFile.toByteArray()
}

fun MidiFile.toByteArray():ByteArray {
  val bytes = byteListOf()
  bytes.writeFile(this)
  return bytes.map { it.toByte() }.toByteArray()
}

@ExperimentalUnsignedTypes
private fun ByteList.writeFile(midiFile: MidiFile) {
  writeHeader(midiFile.tracks.count())
  midiFile.tracks.forEach { writeTrack(it) }
}

@ExperimentalUnsignedTypes
private fun ByteList.writeHeader(numTracks:Int) {
  write("MThd")
  write(6)
  write(1.toShort())
  write(numTracks.toShort())
  write(ticks.toShort())
}

@ExperimentalUnsignedTypes
private fun ByteList.writeTrack(midiTrack: MidiTrack) {
  write("MTrk")
  val startTrack = size
  // length will go here when we know it

  var lastOffset = dZero()
  var lastEvent:MidiEvent? = null
  midiTrack.events.forEach { (offset, event) ->
    writeTrackEvent(offset.subtract(lastOffset), event, lastEvent)
    lastOffset = offset
    lastEvent = event
  }
  writeEof()
  val length = size - startTrack
  val lengthBytes = byteListOf()
  lengthBytes.write(length)
  addAll(startTrack, lengthBytes)
}

@ExperimentalUnsignedTypes
private fun ByteList.writeEof() {
  addAll(byteListOf(0x1.toByte(), 0xff.toByte(), 0x2f.toByte(), 0.toByte()))
}

@ExperimentalUnsignedTypes
private fun ByteList.writeTrackEvent(delta: Duration, midiEvent: MidiEvent, lastEvent:MidiEvent?) {
  writeDelta(delta)
  when (midiEvent) {
    is NoteOnEvent -> writeNoteOn(midiEvent, lastEvent)
    is NoteOffEvent -> writeNoteOff(midiEvent, lastEvent)
    is ProgramChangeEvent -> writeProgramEvent(midiEvent)
    is TempoEvent -> writeTempoEvent(midiEvent)
    is TimeSignatureEvent -> writeTimeSignatureEvent(midiEvent)
    is KeySignatureEvent -> writeKeySignatureEvent(midiEvent)
    is PedalEvent -> writePedalEvent(midiEvent)
    else -> {}
  }
}

@ExperimentalUnsignedTypes
private fun ByteList.writeNoteOn(noteOnEvent: NoteOnEvent, lastEvent: MidiEvent?) {
  if (!(lastEvent is NoteOnEvent)) {
    write(0x90.toUByte() or noteOnEvent.channel.toUByte())
  }
  write(noteOnEvent.midiVal.toUByte())
  write(noteOnEvent.velocity.toUByte())
}

@ExperimentalUnsignedTypes
private fun ByteList.writeNoteOff(noteOffEvent: NoteOffEvent, lastEvent: MidiEvent?) {
  if (!(lastEvent is NoteOnEvent)) {
    write(0x90.toUByte() or noteOffEvent.channel.toUByte())
  }
  write(noteOffEvent.midiVal.toUByte())
  write(0.toUByte())
}

@ExperimentalUnsignedTypes
private fun ByteList.writeProgramEvent(programEvent:ProgramChangeEvent) {
  write(0xC0.toUByte() or programEvent.channel.toUByte())
  write((programEvent.program).toUByte())
}

@ExperimentalUnsignedTypes
private fun ByteList.writeTempoEvent(tempoEvent: TempoEvent) {
  val msPqn = 60000000/((tempoEvent.duration / crotchet()) * tempoEvent.bpm).toInt()
  write(0xff.toUByte())
  write(0x51.toUByte())
  writeSignificant(msPqn)
}


@ExperimentalUnsignedTypes
private fun ByteList.writeTimeSignatureEvent(timeSignatureEvent: TimeSignatureEvent) {
  write(0xff.toUByte())
  write(0x58.toUByte())
  write(0x04.toUByte())
  write(timeSignatureEvent.numerator.toUByte())
  write((timeSignatureEvent.denominator.highestBit()-1).toUByte())
  write(2.toUByte())
  write(8.toUByte())
}

@ExperimentalUnsignedTypes
private fun ByteList.writeKeySignatureEvent(keySignatureEvent: KeySignatureEvent) {
  write(0xff.toUByte())
  write(0x59.toUByte())
  write(0x02.toUByte())
  write(keySignatureEvent.sharps.toUByte())
  write(0.toUByte())
}

@ExperimentalUnsignedTypes
private fun ByteList.writePedalEvent(pedalEvent: PedalEvent) {
  write(0xb0.toUByte() or pedalEvent.channel.toUByte())
  write(0x40.toUByte())
  if (pedalEvent.on) {
    write(0x80.toUByte())
  } else {
    write(0x00.toUByte())
  }
}

@ExperimentalUnsignedTypes
fun ByteList.writeDelta(duration: Duration) {
  val ppqnDuration: Int = durationToPPQN(duration)
  val deltaBytes = createDelta(ppqnDuration)
  write(deltaBytes)
}

private fun durationToPPQN(duration: Duration): Int {
  if (duration == dZero()) return 0
  return Duration(ticks).multiply(duration.divide(crotchet())).toInt()
}

@ExperimentalUnsignedTypes
private fun createDelta(value: Int, soFar:ByteList = byteListOf()):Iterable<UByte> {

    val byteRemainderPair = getNext7Bits(value)
    val contBit = if (soFar.isEmpty()) 0.toUByte() else 0x80.toUByte()
    soFar.write(byteRemainderPair.first or contBit)
    return if (byteRemainderPair.second > 0) {
      createDelta(byteRemainderPair.second, soFar)
    } else {
      soFar.reversed()
  }
}

@ExperimentalUnsignedTypes
private fun getNext7Bits(value: Int): Pair<UByte, Int> {
  return Pair((value and 0x7f).toUByte(), value shr 7)
}

@ExperimentalUnsignedTypes
private fun ByteList.write(string: String) {
  string.forEach { write(it.toByte().toUByte()) }
}

@ExperimentalUnsignedTypes
private fun ByteList.write(bytes:Iterable<UByte>) {
  bytes.forEach { write(it) }
}

@ExperimentalUnsignedTypes
private fun ByteList.write(signed: Int) {
  val int = signed.toUInt()
  write(((int shr 24) and 0xff.toUInt()).toUByte())
  write(((int shr 16) and 0xff.toUInt()).toUByte())
  write(((int shr 8) and 0xff.toUInt()).toUByte())
  write((int and 0xff.toUInt()).toUByte())
}

@ExperimentalUnsignedTypes
private fun ByteList.writeSignificant(signed: Int) {
  val numBytes = signed.toLong().numBytes()
  write(numBytes.toUByte())

  val int = signed.toUInt()
  repeat(numBytes) { num ->
    write(((int shr ((numBytes-num-1)*8)) and 0xff.toUInt()).toUByte())
  }
}

@ExperimentalUnsignedTypes
private fun ByteList.write(signed: Short) {
  val short = signed.toUInt()
  write(((short.toUInt() shr 8) and 0xff.toUInt()).toUByte())
  write((short.toUInt() and 0xff.toUInt()).toUByte())
}

@ExperimentalUnsignedTypes
private fun ByteList.write(byte: UByte) {
  add(byte)
}
