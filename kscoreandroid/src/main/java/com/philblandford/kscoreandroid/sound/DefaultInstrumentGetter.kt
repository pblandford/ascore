package com.philblandford.kscoreandroid.sound

import ResourceManager
import SamplerManager
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup
import com.philblandford.kscore.engine.util.replace
import com.philblandford.kscore.implementations.BaseInstrumentGetter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


class DefaultInstrumentGetter(
  private val resourceManager: ResourceManager,
  private val samplerManager: SamplerManager
) : BaseInstrumentGetter(
  resourceManager.getBaseInstrumentDescriptions(),
  resourceManager.getPreviousBaseInstrumentDescriptions()
) {

  private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

  private data class InstrumentJson(val groups: List<InstrumentGroup>)

  override fun getNonDefaultInstruments(): List<InstrumentGroup> {
    return samplerManager.getSoundfontInstruments()
      .map { InstrumentGroup(it.key, it.value.instruments) }
  }

  override fun createGroup(name: String, instruments: List<Instrument>) {
    groups = groups.plus(InstrumentGroup(name, instruments))
    commit()
  }

  override fun refresh() {
    super.refresh()
    getUserDescriptions()?.let { groups ->
      this.groups = groups
      this.groups = newInstruments.fold(this.groups) { g, i ->
        g.find { it.name == i.group }?.let { ig ->
          g.replace(ig, ig.copy(instruments = listOf(i) + ig.instruments))
        } ?: g + InstrumentGroup(i.group, listOf(i))
      }
      commit()
    }
    val old = samplerManager.getSoundfontInstruments()
    samplerManager.reloadSoundFonts()
    val new = samplerManager.getSoundfontInstruments()
    val diff = (new.toList() - old.toList())
    diff.forEach { (name, group) ->
      createGroup(name, group.instruments)
    }
  }

  override fun clearUser() {
    resourceManager.clearInstrumentDescriptions()
    refresh()
  }

  private fun getUserDescriptions(): List<InstrumentGroup>? {
    return resourceManager.getUserInstrumentDescriptions()?.let { json ->
      val adapter: JsonAdapter<InstrumentJson> = moshi.adapter(
        InstrumentJson::class.java
      )
      val instrumentJson = adapter.fromJson(json)
      instrumentJson?.groups
    }
  }

  override fun commit() {
    val adapter: JsonAdapter<InstrumentJson> = moshi.adapter(InstrumentJson::class.java)
    val json = adapter.toJson(InstrumentJson(groups))
    resourceManager.saveInstrumentDescriptions(json)
  }
}