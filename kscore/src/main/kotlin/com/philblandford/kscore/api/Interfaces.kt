package com.philblandford.kscore.api

import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.KDrawable
import com.philblandford.kscore.engine.core.area.factory.DrawableArgs

interface InstrumentGetter {
  fun refresh()
  fun getInstrumentGroups(): List<InstrumentGroup>
  fun getInstrument(name: String): Instrument?
  fun getInstrumentGroup(name: String): InstrumentGroup?
  fun assignInstrument(instrumentName: String, group: String)
  fun createGroup(name: String, instruments: List<Instrument> = listOf())
  fun clearUser()
}

interface SoundManager : InstrumentGetter {
  fun soundSingleNote(
    midiVal: Int, program: Int, velocity: Int, length: Int, percussion: Boolean,
    soundFont: String, bank: Int, channel: Int = 1
  )

  fun soundSingleNoteNoStop(
    midiVal: Int, program: Int, velocity: Int, percussion: Boolean,
    soundFont: String, bank: Int, channel: Int = 1
  )

  fun noteOn(midiVal: Int, velocity: Int, channel: Int)
  fun noteOff(midiVal: Int, channel: Int)
  fun programChange(program: Int, channel: Int, soundFont: String, bank: Int)
  fun pedalEvent(on: Boolean, channel: Int)
  fun stopAllNotes(soundFont:String? = null)

  fun reset()
  fun close()
}

interface DrawableGetter {
  fun getDrawable(drawableArgs: DrawableArgs): KDrawable?
  fun prepare(vararg args: Any)
  fun getDrawArgs(): Array<Any> = arrayOf()

  fun drawTree(area: Area, x:Int = 0, y:Int = 0,
               export: Boolean = false) {
    area.drawable?.draw(x, y, export, *getDrawArgs())
    area.childMap.forEach { drawTree(it.value, it.key.coord.x + x, it.key.coord.y + y, export) }
  }
}
