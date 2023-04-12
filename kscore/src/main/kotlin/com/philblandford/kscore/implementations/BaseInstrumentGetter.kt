package com.philblandford.kscore.implementations

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.api.InstrumentGroup
import com.philblandford.kscore.api.PercussionDescr
import com.philblandford.kscore.engine.types.ClefType
import com.philblandford.kscore.engine.types.NoteHeadType
import com.philblandford.kscore.engine.util.replace


open class BaseInstrumentGetter(
  private val instrumentData: String?,
  private val previousInstrumentData:String?
) : InstrumentGetter {

  protected var groups: List<InstrumentGroup> = listOf()
  protected var newInstruments: List<Instrument> = listOf()

  override fun refresh() {
    groups = loadInstruments()
    newInstruments = getNewInstruments(groups)
  }

  override fun getInstrumentGroups(): List<InstrumentGroup> {
    return groups.filter { it.instruments.isNotEmpty() }
  }

  override fun createGroup(name: String, instruments: List<Instrument>) {

  }

  override fun clearUser() {

  }

  fun getInstrumentGroupNames(): List<String> = groups.map { it.name }

  fun getInstrumentsForGroup(group: String): List<Instrument> {
    return groups.find { it.name == group }?.instruments ?: listOf()
  }

  override fun getInstrument(name: String): Instrument? {
    return groups.flatMap { it.instruments }.find {
      it.name == name || it.abbreviation == name
    }
  }

  fun getGroupForInstrument(name: String): String? {
    return groups.find { it.instruments.any { it.name == name } }?.name
  }

  override fun getInstrumentGroup(name: String): InstrumentGroup? {
    return groups.find { it.name == name }
  }

  override fun assignInstrument(instrumentName: String, group: String) {
    getInstrument(instrumentName)?.let { instrument ->
      getGroupForInstrument(instrumentName)?.let { getInstrumentGroup(it)} ?.let {  existingGroup ->
        getInstrumentGroup(group)?.let { newGroup ->
          val newInstrument = instrument.copy(group = newGroup.name)
          val existingModified = existingGroup.copy(instruments = existingGroup.instruments.minus(instrument))
          val newGroupModified = newGroup.copy(instruments = newGroup.instruments.plus(newInstrument))
          var newGroups = groups
          newGroups = newGroups.replace(groups.indexOf(existingGroup), existingModified)
          newGroups = newGroups.replace(groups.indexOf(newGroup), newGroupModified)
          groups = newGroups
          commit()
        }
      }
    }
  }

  private fun loadInstruments():List<InstrumentGroup> {
    return instrumentData?.let {
       loadInstrumentInfo(it) + getNonDefaultInstruments()
    }  ?: listOf()
  }

  private fun getNewInstruments(current:List<InstrumentGroup>):List<Instrument> {
    return previousInstrumentData?.let {
      val previousGroups = loadInstrumentInfo(it)
      val currentFlattened = current.flatMap { it.instruments }.toSet()
      val previousFlattened = previousGroups.flatMap { it.instruments }.toSet()
      currentFlattened.filter {
        !previousFlattened.contains(it)
      }
    } ?: listOf()
  }

  protected open fun getNonDefaultInstruments():List<InstrumentGroup> = listOf()

  protected open fun commit() {}

  private fun loadInstrumentInfo(str: String): List<InstrumentGroup> {

    val lines = str.split("\n").map { it.trim() }.filterNot{ it.isEmpty()}
    val groups = mutableListOf<InstrumentGroup>()
    val instruments = mutableListOf<Instrument>()
    var currentGroup: InstrumentGroup? = null
    for (line in lines) {
      if (line.lastOrNull() == ':') {
        currentGroup?.let {
          groups.add(it.copy(instruments = instruments.toList()))
        }
        instruments.clear()
        currentGroup = readGroup(line)
      } else {
        readInstrument(line, currentGroup?.name ?: "")?.let { instruments.add(it) }
      }
    }
    currentGroup?.let {
      groups.add(it.copy(instruments = instruments.toList()))
    }
    return groups
  }

  private fun readGroup(line: String): InstrumentGroup? {
    return InstrumentGroup(line.dropLast(1), listOf())
  }

  private fun readInstrument(line: String, group: String): Instrument? {
    return if (group == "Percussion") {
      readInstrumentPercussion(line, group)
    } else {
      readInstrumentNormal(line, group)
    }
  }

  private fun readInstrumentNormal(line: String, group: String): Instrument? {
    val fields = line.split(",")
    if (fields[0] == "SET") return loadPercussionSet(line)
    val name = fields[0]
    val transposition = fields[1].toInt()
    val program = fields[2].toInt()
    val clefs = fields[3].split(":").map { ClefType.valueOf(it) }
    val abbreviation = fields.getOrNull(4) ?: name
    return Instrument(
      name, abbreviation,
      group, program, transposition, clefs, "default", 0
    )
  }

  private fun readInstrumentPercussion(line: String, group: String): Instrument? {
    val fields = line.split(",")
    if (fields[0] == "SET") return loadPercussionSet(line)
    val name = fields[0]
    val transposition = fields[1].toInt()
    val noteHead = when (fields[2]) {
      "C" -> NoteHeadType.CROSS
      else -> NoteHeadType.NORMAL
    }
    val program = fields[3].toInt()
    val clefs = fields[4].split(":").map { ClefType.valueOf(it) }
    val abbreviation = fields.getOrNull(5) ?: name
    val instrument = Instrument(
      name, abbreviation,
      group, program, transposition, clefs, "default", 0
    )
    return getPercussionDescrsSimple(instrument, noteHead)
  }


  private fun getPercussionDescrsSimple(
    instrument: Instrument,
    noteHead: NoteHeadType
  ): Instrument {
    val descrs = listOf(
      PercussionDescr(
        4, instrument.program, false, instrument.name,
        noteHead
      )
    )
    return instrument.copy(staveLines = 1, percussionDescrs = descrs, program = 1)
  }

  private fun loadPercussionSet(line: String): Instrument {
    val fields = line.split(" ")
    val intro = fields[0]
    val introFields = intro.split(",")
    val name = introFields[1].replace("_", " ")
    val program = introFields[2].toIntOrNull() ?: 1
    var abbr = introFields[3]
    if (abbr.isEmpty()) abbr = name
    val staveLines = introFields[4].toInt()

    val descrs = fields.drop(1).map {
      getPercussionDescr(it)
    }
    return Instrument(
      name, abbr, "Percussion", program, 0,
      listOf(ClefType.PERCUSSION), "default", 0, staveLines, descrs
    )
  }

  private fun getPercussionDescr(string: String): PercussionDescr {
    val fields = string.split(":")
    val position = fields[0].toInt()
    val program = fields[1].toInt()
    val name = fields[2].replace("_", " ")
    val up = fields[3] == "U"
    val noteHeadType = when (fields[4]) {
      "C" -> NoteHeadType.CROSS
      "D" -> NoteHeadType.DIAMOND
      else -> NoteHeadType.NORMAL
    }
    return PercussionDescr(position, program, up, name, noteHeadType)
  }
}