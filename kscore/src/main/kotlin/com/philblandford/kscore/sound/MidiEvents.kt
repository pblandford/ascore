package com.philblandford.kscore.sound

import com.philblandford.kscore.engine.duration.Duration


sealed class MidiEvent(
  open val channel: Int,
  open val priority: Int
)

data class TimeSignatureEvent(val numerator: Int, val denominator: Int) : MetaEvent( 4)
data class TempoEvent(val bpm:Int, val duration: Duration) : MetaEvent(4)
data class KeySignatureEvent(val sharps:Int) : MetaEvent(4)

sealed class MetaEvent(override val priority: Int) : MidiEvent(0, priority)

data class PedalEvent(override val channel: Int, val on: Boolean) : MidiEvent(channel, 3)
data class NoteOnEvent(val midiVal: Int, val velocity: Int, override val channel: Int) :
  MidiEvent(channel, 2)
data class NoteOffEvent(val midiVal: Int, override val channel: Int) : MidiEvent(channel, 1)
data class ProgramChangeEvent(val program: Int, override val channel: Int,
                              val soundFont: String, val bank:Int) : MidiEvent(channel, 0)

data class ControllerEvent(val controller: Int, val value: Int, override val channel: Int) :
  MidiEvent(channel, 0)

data class ChannelPressureEvent(val pressure: Int, override val channel: Int) : MidiEvent(channel, 0)
