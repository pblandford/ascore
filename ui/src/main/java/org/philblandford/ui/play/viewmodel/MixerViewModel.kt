package org.philblandford.ui.play.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup
import com.philblandford.kscore.engine.util.replace
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.harmony.SetHarmonyInstrument
import org.philblandford.ascore2.features.instruments.GetAvailableInstruments
import org.philblandford.ascore2.features.instruments.GetInstruments
import org.philblandford.ascore2.features.instruments.GetVolume
import org.philblandford.ascore2.features.instruments.SetVolume
import org.philblandford.ascore2.features.playback.entities.MixerInstrument
import org.philblandford.ascore2.features.playback.entities.PlaybackState
import org.philblandford.ascore2.features.playback.usecases.GetPlaybackState
import org.philblandford.ascore2.features.playback.usecases.ToggleHarmonies
import org.philblandford.ascore2.features.playback.usecases.ToggleLoop
import org.philblandford.ascore2.features.playback.usecases.ToggleMute
import org.philblandford.ascore2.features.playback.usecases.ToggleShuffle
import org.philblandford.ascore2.features.playback.usecases.ToggleSolo
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect

data class MixerModel(
  val playbackState: PlaybackState,
  val instrumentGroups:List<InstrumentGroup>
) : VMModel()

interface MixerInterface : VMInterface {
  fun setVolume(idx: Int, volume: Int)
  fun toggleLoop()
  fun toggleShuffle()
  fun toggleHarmonies()
  fun toggleSolo(idx:Int)
  fun toggleMute(idx:Int)
  fun setHarmonyInstrument(instrument: Instrument)
}

class MixerViewModel(
  private val getInstruments: GetInstruments,
  val getVolume: GetVolume,
  private val setVolumeUC: SetVolume,
  private val toggleLoopUC: ToggleLoop,
  private val toggleShuffleUC: ToggleShuffle,
  private val toggleHarmoniesUC: ToggleHarmonies,
  private val getPlaybackState: GetPlaybackState,
  private val toggleSoloUC:ToggleSolo,
  private val toggleMuteUC: ToggleMute,
  private val setHarmonyInstrumentUC: SetHarmonyInstrument,
  private val getAvailableInstruments: GetAvailableInstruments
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
    return MixerModel(getPlaybackState().value, getAvailableInstruments()).ok()
  }

  override fun getInterface() = this

  override fun setVolume(idx: Int, volume: Int) {
    setVolumeUC(idx + 1, volume).ok()
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

  override fun toggleSolo(idx:Int) {
    toggleSoloUC(idx + 1)
  }

  override fun toggleMute(idx:Int) {
    toggleMuteUC(idx + 1)
  }

  override fun setHarmonyInstrument(instrument: Instrument) {
    setHarmonyInstrumentUC(instrument)
  }
}