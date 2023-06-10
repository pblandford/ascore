package com.philblandford.mp3converter.engine.sample

import com.philblandford.mp3converter.engine.file.input.MidiEvent
import com.philblandford.mp3converter.ISampler
import com.philblandford.mp3converter.Ms
import com.philblandford.mp3converter.Preset
import kotlin.math.PI
import kotlin.math.sin

class SineSampler : ISampler {
  private val frequency = 440
  private val sampleRate = 44100
  private val step = (2 * PI) / (sampleRate / frequency)
  private val amplitude = Short.MAX_VALUE

  override fun open() {
    TODO("Not yet implemented")
  }

  override fun close() {
    TODO("Not yet implemented")
  }

  override fun passEvent(midiEvent: MidiEvent) {
  }

  override fun getSample( length: Ms): List<Short> {
    return (1..(length * frequency) / 1000).flatMap { step ->
      createSine()
    }
  }

  override fun getPresets(): List<Preset> {
    TODO("Not yet implemented")
  }

  override fun numChannels(): Int {
    TODO("Not yet implemented")
  }

  private fun createSine(): List<Short> {
    return inputs.map { input ->
      val short = (amplitude * sin(input)).toInt().toShort()
      println("$input ${sin(input)} ${amplitude * sin(input)} $short ")
      short
    }
  }

  private val inputs = generateSequence(0.0) { it + step }.takeWhile { it < 2 * PI }.toList()

}