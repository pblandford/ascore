package org.philblandford.ui.create.viewmodel

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup
import com.philblandford.kscore.api.NewScoreDescriptor
import com.philblandford.kscore.engine.tempo.Tempo
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.MetaType
import com.philblandford.kscore.engine.types.PageSize
import org.philblandford.ascore2.features.instruments.GetAvailableInstruments
import org.philblandford.ascore2.features.score.CreateScore
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.util.reorder

data class CreateModel(
  val newScoreDescriptor: NewScoreDescriptor,
  val availableInstruments:List<InstrumentGroup> = listOf()
) : VMModel()

interface CreateInterface : VMInterface {
  fun setTitle(title:String)
  fun setSubtitle(subtitle:String)
  fun setComposer(composer:String)
  fun setLyricist(lyricist:String)
  fun setKeySignature(key:Int)
  fun setTimeSignature(func:TimeSignature.()->TimeSignature)
  fun setUpbeatBar(func:TimeSignature.()->TimeSignature)
  fun setPageSize(pageSize: PageSize)
  fun setNumBars(bars:Int)
  fun setUpbeatEnabled(enabled:Boolean)
  fun setTempo(func: Tempo.()->Tempo)
  fun addInstrument(instrument: Instrument)
  fun removeInstrument(instrument: Instrument)
  fun reorderInstruments(oldIndex:Int, newIndex:Int)
  fun updateInstrument(idx:Int, instrument: Instrument)
  fun create()
}

class CreateViewModel(private val availableInstruments: GetAvailableInstruments,
private val createScore: CreateScore) :
  BaseViewModel<CreateModel, CreateInterface, VMSideEffect>(),
CreateInterface {
  override suspend fun initState(): Result<CreateModel> {
    return CreateModel(NewScoreDescriptor(), availableInstruments()).ok()
  }

  override fun getInterface() = this

  override fun setTitle(title: String) {
    updateScore { copy(meta = meta.setText(MetaType.TITLE, title)) }
  }

  override fun setSubtitle(subtitle: String) {
    updateScore { copy(meta = meta.setText(MetaType.SUBTITLE, subtitle)) }
  }

  override fun setComposer(composer: String) {
    updateScore { copy(meta = meta.setText(MetaType.COMPOSER, composer)) }
  }

  override fun setLyricist(lyricist: String) {
    updateScore { copy(meta = meta.setText(MetaType.LYRICIST, lyricist)) }
  }

  override fun setKeySignature(key: Int) {
    updateScore { copy(keySignature = key) }
  }

  override fun setTimeSignature(func: TimeSignature.() -> TimeSignature) {

    updateScore {
      copy(timeSignature = timeSignature.func()) }
  }

  override fun setUpbeatBar(func: TimeSignature.() -> TimeSignature) {

    updateScore {
      copy(upBeat = upBeat.func()) }
  }

  override fun setUpbeatEnabled(enabled: Boolean) {
    updateScore { copy(upbeatEnabled = enabled) }
  }

  override fun setTempo(func: Tempo.() -> Tempo) {
    updateScore {
      copy(tempo = tempo.func())
    }
  }

  override fun setPageSize(pageSize: PageSize) {
    updateScore {
      copy(pageSize = pageSize)
    }
  }

  override fun setNumBars(bars: Int) {
    updateScore {
      copy(numBars = bars)
    }
  }

  override fun addInstrument(instrument: Instrument) {
    updateScore { copy(instruments = instruments + instrument) }
  }

  override fun removeInstrument(instrument: Instrument) {
    updateScore { copy(instruments = instruments - instrument) }
  }

  override fun create() {
    receiveAction {
      createScore(it.newScoreDescriptor)
      it.ok()
    }
  }

  override fun reorderInstruments(oldIndex: Int, newIndex: Int) {

    updateScore { copy(instruments = instruments.toList().reorder(oldIndex, newIndex)) }
  }

  override fun updateInstrument(idx: Int, instrument: Instrument) {
    updateScore {
      val newInstruments = instruments.toMutableList().apply {
        removeAt(idx)
        add(idx, instrument)
      }
      copy(instruments = newInstruments)
    }
  }

  private fun updateScore(func:NewScoreDescriptor.()->NewScoreDescriptor) {
    update { copy(newScoreDescriptor = newScoreDescriptor.func()) }
  }
}