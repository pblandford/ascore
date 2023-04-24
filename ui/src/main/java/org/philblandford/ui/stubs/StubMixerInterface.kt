package org.philblandford.ui.stubs

import kotlinx.coroutines.flow.Flow
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.play.viewmodel.MixerInterface

class StubMixerInterface : MixerInterface {
  override fun reset() {
    TODO("Not yet implemented")
  }

  override fun getSideEffects(): Flow<VMSideEffect> {
    TODO("Not yet implemented")
  }

  override fun setVolume(idx: Int, volume: Int) {
    TODO("Not yet implemented")
  }

  override fun toggleLoop() {
    TODO("Not yet implemented")
  }

  override fun toggleShuffle() {
    TODO("Not yet implemented")
  }

  override fun toggleHarmonies() {
    TODO("Not yet implemented")
  }

  override fun toggleSolo(idx:Int) {
    TODO("Not yet implemented")
  }

  override fun toggleMute(idx:Int) {
    TODO("Not yet implemented")
  }
}