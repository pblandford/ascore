package org.philblandford.ui.base

import ResourceManager
import SamplerManager
import TextFontManager
import com.philblandford.kscore.api.*
import com.philblandford.kscore.engine.types.ArticulationType
import com.philblandford.kscore.log.KSLogger
import com.philblandford.kscoreandroid.drawingcompose.ComposeDrawableGetter
import com.philblandford.kscoreandroid.resource.AndroidResourceManager
import com.philblandford.kscoreandroid.sound.AndroidSoundManagerFluid
import com.philblandford.kscoreandroid.sound.DefaultInstrumentGetter
import com.philblandford.kscoreandroid.sound.FluidSamplerManager
import com.philblandford.kscoreandroid.text.AndroidTextFontManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module
import org.philblandford.ascore2.features.clipboard.usecases.*
import org.philblandford.ascore2.features.drawing.*
import org.philblandford.ascore2.features.file.AutoSave
import org.philblandford.ascore2.features.gesture.HandleLongPress
import org.philblandford.ascore2.features.gesture.HandleLongPressImpl
import org.philblandford.ascore2.features.gesture.HandleTap
import org.philblandford.ascore2.features.gesture.HandleTapImpl
import org.philblandford.ascore2.features.ui.repository.UiStateRepository
import org.philblandford.ascore2.features.input.usecases.*
import org.philblandford.ascore2.features.instruments.*
import org.philblandford.ui.base.log.AndroidLogger
import org.philblandford.ascore2.features.score.CreateDefaultScore
import org.philblandford.ascore2.features.score.CreateDefaultScoreImpl
import org.philblandford.ascore2.features.score.CreateScore
import org.philblandford.ascore2.features.score.CreateScoreImpl
import org.philblandford.ascore2.features.scorelayout.usecases.GetScoreLayout
import org.philblandford.ascore2.features.scorelayout.usecases.GetScoreLayoutImpl
import org.philblandford.ascore2.features.sound.usecases.*
import org.philblandford.ascore2.features.startup.StartupManager
import org.philblandford.ascore2.features.ui.usecases.*
import org.philblandford.ui.clipboard.viewmodel.ClipboardViewModel
import org.philblandford.ui.create.viewmodel.CreateViewModel
import org.philblandford.ui.main.panel.viewmodels.PanelViewModel
import org.philblandford.ui.keyboard.viewmodel.InputViewModel
import org.philblandford.ui.main.toprow.PlayViewModel
import org.philblandford.ui.play.viewmodel.MixerViewModel
import org.philblandford.ui.screen.viewmodels.ScreenViewModel
import org.philblandford.ui.main.utility.viewmodel.UtilityViewModel
import org.philblandford.ui.insert.row.viewmodel.RowInsertViewModel
import org.philblandford.ui.insert.items.tuplet.viewmodel.TupletInsertViewModel
import org.philblandford.ui.insert.choose.viewmodel.InsertChooseViewModel
import org.philblandford.ui.insert.common.viewmodel.DefaultInsertViewModel
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.main.inputpage.viewmodel.MainPageViewModel
import org.philblandford.ui.main.panel.viewmodels.TabsViewModel

object Dependencies {

  private val androidModules = module {
    single<KSLogger> { AndroidLogger() }
    single<ResourceManager> { AndroidResourceManager(androidContext(), get()) }
    single<SamplerManager> { FluidSamplerManager(get()) }
    single<InstrumentGetter> { DefaultInstrumentGetter(get(), get()).apply { refresh() } }
    single<SoundManager> { AndroidSoundManagerFluid(get(), get()) }
    single<TextFontManager> { AndroidTextFontManager(androidContext()) }
    single<DrawableGetter> { ComposeDrawableGetter(androidContext(), get()) }
  }

  private val stubModules = module {

  }

  private val saveModule = module {
    single { AutoSave(get(), get()) }
  }

  private val startupModule = module {
    single { StartupManager(get(), get()) }
  }

  private val scoreModule = module {
    single<KScore> { KScoreImpl(get(), get(), get()) }
  }

  private val createModule = module {
    single<CreateScore> { CreateScoreImpl(get()) }
    single<CreateDefaultScore> { CreateDefaultScoreImpl(get()) }
    viewModel { CreateViewModel() }
  }

  private val utilityModule = module {
    single { UiStateRepository() }
    single<Delete> { DeleteImpl(get(), get()) }
    single<CurrentVoice> { CurrentVoiceImpl(get()) }
    single<ToggleVoice> { ToggleVoiceImpl(get()) }
    single<Undo> { UndoImpl(get()) }
    single<Redo> { RedoImpl(get()) }
    single<ZoomIn> { ZoomInImpl(get()) }
    single<ZoomOut> { ZoomOutImpl(get()) }
    single<ClearSelection> { ClearSelectionImpl(get(), get()) }
    viewModel {
      UtilityViewModel(
        get(),
        get(),
        get(),
        get(),
        get(),
        get(),
        get(),
        get(),
        get(),
        get()
      )
    }
  }

  private val panelModule = module {
    single<GetPanelLayout> { GetPanelLayoutImpl(get(), get()) }
    single<TogglePanelLayout> { TogglePanelLayoutImpl(get()) }
    viewModel { PanelViewModel(get()) }
  }

  private val tabsModule = module {
    single<GetSelectedPart> { GetSelectedPartImpl(get()) }
    single<SelectPart> { SelectPartImpl(get()) }
    viewModel { TabsViewModel(get(), get(), get()) }
  }

  private val inputModule = module {
    single<MoveMarker> { MoveMarkerImpl(get()) }
    single<InsertNote> { InsertNoteImpl(get(), get(), get(), get()) }
    single<InsertRest> { InsertRestImpl(get(), get(), get()) }
    single { UpdateInputStateImpl() }.binds(arrayOf(UpdateInputState::class, NoteInputState::class))
    viewModel { InputViewModel(get(), get(), get(), get(), get(), get()) }
  }

  private val insertModule = module {
    single<UpdateInsertItem> { UpdateInsertItemImpl(get()) }
    single<GetInsertItem> { GetInsertItemImpl(get()) }
    single<SelectInsertItem> { SelectInsertItemImpl(get()) }
    single<InsertItemMenu> { InsertItemMenuMenuImpl(get()) }
    viewModel { InsertChooseViewModel(get()) }
    viewModel { DefaultInsertViewModel(get(), get(), get()) }
    viewModel { RowInsertViewModel<ArticulationType>(get(), get(), get()) }
    viewModel { TupletInsertViewModel(get(), get(), get()) }

  }

  private val screenModule = module {
    single<ScoreChanged> { ScoreChangedImpl(get()) }
    single<GetUIState> { GetUIStateImpl(get()) }
    single { RedrawImpl() }.binds(arrayOf(Redraw::class, ListenForRedraw::class))
    single<DrawPage> { DrawPageImpl(get()) }
    single<GetScoreLayout> { GetScoreLayoutImpl(get()) }
    single<HandleTap> { HandleTapImpl(get(), get()) }
    single<HandleLongPress> { HandleLongPressImpl(get(), get()) }
    viewModel { ScreenViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { MainPageViewModel(get()) }
  }

  private val soundModule = module {
    single<SoundNote> { SoundNoteImpl(get(), get()) }
    single<Play> { PlayImpl(get()) }
    single<Stop> { StopImpl(get()) }
    single<Pause> { PauseImpl(get()) }
    single<GetPlaybackMarker> { GetPlaybackMarkerImpl(get()) }
    single<GetPlayState> { GetPlayStateImpl(get()) }
    single<GetInstruments> { GetInstrumentsImpl(get()) }
    single<GetAvailableInstruments> { GetAvailableInstrumentsImpl(get()) }
    single<GetVolume> { GetVolumeImpl(get()) }
    single<SetVolume> { SetVolumeImpl(get()) }
    viewModel { PlayViewModel(get(), get(), get(), get()) }
    viewModel { MixerViewModel(get(), get(), get()) }
  }

  private val clipboardModule = module {
    single<Copy> { CopyImpl(get(), get()) }
    single<Cut> { CutImpl(get(), get()) }
    single<Paste> { PasteImpl(get(), get()) }
    viewModel { ClipboardViewModel(get(), get(), get()) }
  }

  private val modules = listOf(
    scoreModule,
    saveModule,
    startupModule,
    createModule,
    utilityModule,
    tabsModule,
    panelModule,
    insertModule,
    inputModule,
    screenModule,
    clipboardModule,
    soundModule
  )

  fun getModules(test: Boolean): List<Module> {
    return if (test) {
      stubModules + modules
    } else {
      androidModules + modules
    }
  }
}

