package org.philblandford.ui.play.viewmodel

import org.philblandford.ascore2.features.instruments.GetInstruments
import org.philblandford.ascore2.features.instruments.GetVolume
import org.philblandford.ascore2.features.instruments.SetVolume
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.play.compose.MixerInstrument

data class MixerModel(
  val instruments: List<MixerInstrument>
) : VMModel()

interface MixerInterface : VMInterface {
  fun setVolume(idx: Int, volume: Int)
}

class MixerViewModel(
  private val getInstruments: GetInstruments,
  val getVolume: GetVolume,
  private val setVolumeUC: SetVolume
) :
  BaseViewModel<MixerModel, MixerInterface, VMSideEffect>(), MixerInterface {

  override suspend fun initState(): Result<MixerModel> {
    val instruments = getInstruments().withIndex().map { (idx, instrument) ->
      MixerInstrument(
        instrument.name.split(" ").joinToString("") { it.first().uppercase() },
        instrument.name,
        getVolume(idx + 1)
      )
    }
    return MixerModel(instruments).ok()
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

}