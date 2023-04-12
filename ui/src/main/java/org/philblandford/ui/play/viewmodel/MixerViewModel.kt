package org.philblandford.ui.play.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.instruments.GetInstruments
import org.philblandford.ascore2.features.instruments.GetVolume
import org.philblandford.ascore2.features.instruments.SetVolume
import org.philblandford.ascore2.features.playback.entities.PlaybackState
import org.philblandford.ascore2.features.playback.usecases.GetPlaybackState
import org.philblandford.ascore2.features.playback.usecases.ToggleHarmonies
import org.philblandford.ascore2.features.playback.usecases.ToggleLoop
import org.philblandford.ascore2.features.playback.usecases.ToggleShuffle
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.play.compose.MixerInstrument

data class MixerModel(
  val instruments: List<MixerInstrument>,
  val playbackState: PlaybackState
) : VMModel()

interface MixerInterface : VMInterface {
  fun setVolume(idx: Int, volume: Int)
  fun toggleLoop()
  fun toggleShuffle()
  fun toggleHarmonies()
}

class MixerViewModel(
  private val getInstruments: GetInstruments,
  val getVolume: GetVolume,
  private val setVolumeUC: SetVolume,
  private val toggleLoopUC: ToggleLoop,
  private val toggleShuffleUC: ToggleShuffle,
  private val toggleHarmoniesUC: ToggleHarmonies,
  private val getPlaybackState: GetPlaybackState
) :
  BaseViewModel<MixerModel, MixerInterface, VMSideEffect>(), MixerInterface {

  init {
    viewModelScope.launch {
      getPlaybackState().collectLatest {
        update { copy(playbackState = it) }
      }
    }
  }

  override suspend fun initState(): Result<MixerModel> {
    val instruments = getInstruments().withIndex().map { (idx, instrument) ->
      MixerInstrument(
        instrument.name.split(" ").joinToString("") { it.first().uppercase() },
        instrument.name,
        getVolume(idx + 1)
      )
    }
    return MixerModel(instruments, getPlaybackState().value).ok()
  }

  override fun getInterface() = this

  override fun setVolume(idx: Int, volume: Int) {
    setVolumeUC(idx + 1, volume).ok()
    update {
      val newInstrument = instruments[idx].copy(level = volume)
      val newList = instruments.take(idx) + newInstrument + instruments.drop(idx + 1)
      copy(instruments = newList)
    }
  }

  override fun toggleLoop() {
    toggleLoopUC()
  }

  override fun toggleShuffle() {
    toggleShuffleUC()
  }

  override fun toggleHarmonies() {
    toggleHarmoniesUC()
  }
}