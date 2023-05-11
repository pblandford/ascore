package org.philblandford.ui.base

import ResourceManager
import SamplerManager
import TextFontManager
import org.philblandford.ascore2.android.export.AndroidExporter
import com.philblandford.ascore.external.export.Exporter
import com.philblandford.ascore.external.interfaces.ExporterIf
import com.philblandford.ascore.external.interfaces.ExternalSaver
import com.philblandford.ascore.external.interfaces.PdfCreator
import com.philblandford.ascore.external.interfaces.ScoreLoader
import com.philblandford.kscore.api.*
import com.philblandford.kscore.engine.types.ArticulationType
import com.philblandford.kscore.log.KSLogger
import com.philblandford.kscore.saveload.Loader
import com.philblandford.kscore.saveload.Saver
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
import org.philblandford.ascore2.android.billing.BillingManager
import org.philblandford.ascore2.android.export.AndroidExternalSaver
import org.philblandford.ascore2.android.export.AndroidImporter
import org.philblandford.ascore2.android.export.AndroidScoreLoader
import org.philblandford.ascore2.features.clipboard.usecases.*
import org.philblandford.ascore2.features.crosscutting.usecases.GetError
import org.philblandford.ascore2.features.crosscutting.usecases.GetErrorImpl
import org.philblandford.ascore2.features.crosscutting.usecases.SetError
import org.philblandford.ascore2.features.drawing.*
import org.philblandford.ascore2.features.edit.MoveSelectedArea
import org.philblandford.ascore2.features.edit.MoveSelectedAreaImpl
import org.philblandford.ascore2.features.edit.MoveSelectedNote
import org.philblandford.ascore2.features.edit.MoveSelectedNoteImpl
import org.philblandford.ascore2.features.edit.SetParamForSelected
import org.philblandford.ascore2.features.edit.SetParamForSelectedImpl
import org.philblandford.ascore2.features.edit.ToggleBooleanForNotes
import org.philblandford.ascore2.features.edit.ToggleBooleanForNotesImpl
import org.philblandford.ascore2.features.error.GetErrorFlow
import org.philblandford.ascore2.features.error.GetErrorFlowImpl
import org.philblandford.ascore2.features.export.ExportScore
import org.philblandford.ascore2.features.export.ExportScoreImpl
import org.philblandford.ascore2.features.export.GetExportBytes
import org.philblandford.ascore2.features.export.GetExportBytesImpl
import org.philblandford.ascore2.features.file.AutoSave
import org.philblandford.ascore2.features.gesture.*
import org.philblandford.ascore2.features.harmony.GetHarmoniesForKey
import org.philblandford.ascore2.features.harmony.GetHarmoniesForKeyImpl
import org.philblandford.ascore2.features.harmony.SetHarmonyInstrument
import org.philblandford.ascore2.features.harmony.SetHarmonyInstrumentImpl
import org.philblandford.ascore2.features.input.usecases.*
import org.philblandford.ascore2.features.insert.*
import org.philblandford.ascore2.features.instruments.*
import org.philblandford.ascore2.features.load.usecases.*
import org.philblandford.ascore2.features.page.*
import org.philblandford.ascore2.features.playback.usecases.*
import org.philblandford.ascore2.features.save.*
import org.philblandford.ascore2.features.score.*
import org.philblandford.ascore2.features.scorelayout.usecases.GetScoreLayout
import org.philblandford.ascore2.features.scorelayout.usecases.GetScoreLayoutImpl
import org.philblandford.ascore2.features.settings.SettingsDataSource
import org.philblandford.ascore2.features.settings.repository.SettingsRepository
import org.philblandford.ascore2.features.settings.usecases.*
import org.philblandford.ascore2.features.settings.usecases.GetAssignedFonts
import org.philblandford.ascore2.features.settings.usecases.GetAssignedFontsImpl
import org.philblandford.ascore2.features.sound.usecases.*
import org.philblandford.ascore2.features.startup.StartupManager
import org.philblandford.ascore2.features.ui.repository.UiStateRepository
import org.philblandford.ascore2.features.ui.usecases.*
import org.philblandford.ui.base.log.AndroidLogger
import org.philblandford.ui.clipboard.viewmodel.ClipboardViewModel
import org.philblandford.ui.crash.CrashHandler
import org.philblandford.ui.create.viewmodel.CreateViewModel
import org.philblandford.ui.createfromtemplate.viewmodel.CreateFromTemplateViewModel
import org.philblandford.ui.edit.items.harmony.viewmodel.HarmonyEditViewModel
import org.philblandford.ui.edit.items.instrumentedit.viewmodel.InstrumentEditViewModel
import org.philblandford.ui.edit.items.text.viewmodel.TextEditViewModel
import org.philblandford.ui.edit.viewmodel.EditViewModel
import org.philblandford.ui.export.viewmodel.ExportViewModel
import org.philblandford.ui.imports.viewmodel.ImportViewModel
import org.philblandford.ui.insert.choose.viewmodel.InsertChooseViewModel
import org.philblandford.ui.insert.common.viewmodel.DefaultInsertViewModel
import org.philblandford.ui.insert.items.barnumbering.viewmodel.BarNumberingViewModel
import org.philblandford.ui.insert.items.harmony.viewmodel.HarmonyInsertViewModel
import org.philblandford.ui.insert.items.instrument.viewmodel.InstrumentInsertViewModel
import org.philblandford.ui.insert.items.lyric.viewmodel.LyricInsertViewModel
import org.philblandford.ui.insert.items.meta.viewmodel.MetaInsertViewModel
import org.philblandford.ui.insert.items.ornament.viewmodel.OrnamentInsertViewModel
import org.philblandford.ui.insert.items.pagemargins.viewmodel.PageMarginsViewModel
import org.philblandford.ui.insert.items.pagesize.viewmodel.PageSizeViewModel
import org.philblandford.ui.insert.items.segmentwidth.viewmodel.SegmentWidthViewModel
import org.philblandford.ui.insert.items.transposeby.viewmodel.TransposeViewModel
import org.philblandford.ui.insert.items.tuplet.viewmodel.TupletInsertViewModel
import org.philblandford.ui.insert.row.viewmodel.RowInsertViewModel
import org.philblandford.ui.input.viewmodel.InputViewModel
import org.philblandford.ui.insert.items.text.viewmodel.TextInsertViewModel
import org.philblandford.ui.layout.viewmodel.LayoutOptionViewModel
import org.philblandford.ui.load.viewmodels.LoadViewModel
import org.philblandford.ui.main.inputpage.viewmodel.MainPageViewModel
import org.philblandford.ui.main.panel.viewmodels.PanelViewModel
import org.philblandford.ui.main.panel.viewmodels.TabsViewModel
import org.philblandford.ui.main.toprow.PlayViewModel
import org.philblandford.ui.main.utility.viewmodel.UtilityViewModel
import org.philblandford.ui.play.viewmodel.MixerViewModel
import org.philblandford.ui.print.AndroidPdfCreator
import org.philblandford.ui.print.AndroidPrinter
import org.philblandford.ui.quickscore.viewmodel.QuickScoreViewModel
import org.philblandford.ui.save.viewmodel.SaveViewModel
import org.philblandford.ui.screen.viewmodels.ScreenViewModel
import org.philblandford.ui.screen.viewmodels.ScreenZoomViewModel
import org.philblandford.ui.settings.viewmodel.InstrumentManageViewModel
import org.philblandford.ui.settings.viewmodel.SettingsViewModel
import org.philblandford.ui.theme.viewmodel.ThemeViewModel

object Dependencies {

  private val androidModules = module {
    single<KSLogger> { AndroidLogger() }
    single<ResourceManager> { AndroidResourceManager(androidContext(), get()) }
    single<SamplerManager> { FluidSamplerManager(get()) }
    single<InstrumentGetter> { DefaultInstrumentGetter(get(), get()).apply { refresh() } }
    single<SoundManager> { AndroidSoundManagerFluid(get(), get()) }
    single<TextFontManager> { AndroidTextFontManager(androidContext()) }
    single<DrawableGetter> { ComposeDrawableGetter(androidContext(), get()) }
    single<ScoreLoader> { AndroidScoreLoader(get(), get(), get()) }
    single { AndroidImporter(get()) }
    single { CrashHandler(get(), get(), get()) }
  }

  private val stubModules = module {

  }

  private val saveModule = module {
    single { Saver() }
    single<GetTitle> { GetTitleImpl(get()) }
    single<GetFileName> { GetFileNameImpl(get()) }
    single<SaveScore> { SaveScoreImpl(get(), get(), get()) }
    single { AutoSave(get(), get()) }
    viewModel { SaveViewModel(get(), get(), get(), get()) }
  }

  private val exportModule = module {
    single<ExternalSaver> { AndroidExternalSaver(get()) }
    single<PdfCreator> { AndroidPdfCreator(get(), get()) }
    single<ExporterIf> { AndroidExporter(get(), get()) }
    single { Exporter(get(), get(), get(), get()) }
    single<ExportScore> { ExportScoreImpl(get(), get()) }
    single<GetExportBytes> { GetExportBytesImpl(get(), get())}
    viewModel { ExportViewModel(get(), get(), get(), get()) }
  }

  private val loadModule = module {
    single { Loader() }
    single<LoadScore> { LoadScoreImpl(get(), get(), get()) }
    single<GetSavedScores> { GetSavedScoresImpl(get()) }
    single<DeleteScore> { DeleteScoreImpl(get()) }
    single<ImportScore> { ImportScoreImpl(get(), get(), get(), get()) }
    viewModel { LoadViewModel(get(), get(), get()) }
    viewModel { ImportViewModel(get(), get()) }
  }

  private val printModule = module {
    single { AndroidPdfCreator(get(), get()) }
    single { AndroidPrinter(get()) }
  }

  private val startupModule = module {
    single<InstallTemplates> { InstallTemplatesImpl(get(), get()) }
    single{ BillingManager(get()) }
    single { StartupManager(get(), get(), get(), get()) }
  }

  private val errorModule = module {
    single<GetErrorFlow> { GetErrorFlowImpl(get()) }
  }

  private val scoreModule = module {
    single<KScore> { KScoreImpl(get(), get(), get()) }
    single<ScoreUpdate> { ScoreUpdateImpl(get()) }
    single<ScoreLoadUpdate> { ScoreLoadUpdateImpl(get()) }
  }

  private val createModule = module {
    single<CreateScore> { CreateScoreImpl(get(), get()) }
    single<CreateDefaultScore> { CreateDefaultScoreImpl(get(), get()) }
    single<CreateScoreFromTemplate> { CreateScoreFromTemplateImpl(get(), get(), get())}
    viewModel { CreateViewModel(get(), get()) }
    viewModel { QuickScoreViewModel(get()) }
    viewModel { CreateFromTemplateViewModel(get(), get(), get()) }
  }


  private val utilityModule = module {
    single { UiStateRepository(get(), get(), get()) }
    single<Delete> { DeleteImpl(get(), get()) }
    single<CurrentVoice> { CurrentVoiceImpl(get()) }
    single<ToggleVoice> { ToggleVoiceImpl(get()) }
    single<Undo> { UndoImpl(get()) }
    single<Redo> { RedoImpl(get()) }
    single<ZoomIn> { ZoomInImpl(get()) }
    single<ZoomOut> { ZoomOutImpl(get()) }
    single<ClearSelection> { ClearSelectionImpl(get(), get()) }
    single<MoveSelection> { MoveSelectionImpl(get()) }
    single<HandleDeletePress> { HandleDeletePressImpl(get(), get(), get()) }
    single<HandleDeleteLongPress> { HandleDeleteLongPressImpl(get(), get()) }
    single<HandleLongPressRelease> { HandleLongPressReleaseImpl(get(), get()) }
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
        get(),
        get(),
        get()
      )
    }
  }

  private val panelModule = module {
    single<GetPanelLayout> { GetPanelLayoutImpl(get(), get()) }
    single<TogglePanelLayout> { TogglePanelLayoutImpl(get(), get()) }
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
    single<GetInstrumentAtMarker> { GetInstrumentAtMarkerImpl(get()) }
    single<GetKeySignatureAtMarker> { GetKeySignatureAtMarkerImpl(get()) }
    single { UpdateInputStateImpl() }.binds(arrayOf(UpdateInputState::class, NoteInputState::class))
    viewModel { InputViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
  }

  private val insertModule = module {
    single<GetMarker> { GetMarkerImpl(get()) }
    single<UpdateInsertParams> { UpdateInsertParamsImpl(get()) }
    single<UpdateInsertEvent> { UpdateInsertEventImpl(get()) }
    single<UpdateInsertItem> { UpdateInsertItemImpl(get()) }
    single<GetInsertItem> { GetInsertItemImpl(get()) }
    single<SelectInsertItem> { SelectInsertItemImpl(get()) }
    single<InsertItemMenu> { InsertItemMenuMenuImpl(get()) }
    single<InsertEventAtLocation> { InsertEventAtLocationImpl(get()) }
    single<InsertEventAtMarker> { InsertEventAtMarkerImpl(get()) }
    single<LocationIsInScore> { LocationIsInScoreImpl(get()) }
    single<SetMarker> { SetMarkerImpl(get()) }
    single<MoveMarker> { MoveMarkerImpl(get()) }
    single<InsertEvent> { InsertEventImpl(get()) }
    single<InsertMetaEvent> { InsertMetaEventImpl(get()) }
    single<GetMetaEvent> { GetMetaEventImpl(get()) }
    single<InsertLyricAtMarker> { InsertLyricAtMarkerImpl(get(), get()) }
    single<GetLyricAtMarker> { GetLyricAtMarkerImpl(get(), get()) }
    single<GetPageWidth> { GetPageWidthImpl(get()) }
    single<GetPageMinMax> { GetPageMinMaxImpl(get()) }
    single<SetPageWidth> { SetPageWidthImpl(get()) }
    single<SetPageWidthPreset> { SetPageWidthPresetImpl(get()) }
    single<GetSegmentMinMax> { GetSegmentMinMaxImpl(get()) }
    single<GetSegmentWidth> { GetSegmentWidthImpl(get()) }
    single<SetSegmentWidth> { SetSegmentWidthImpl(get()) }
    single<GetHarmoniesForKey> { GetHarmoniesForKeyImpl(get()) }
    single<SplitBar> { SplitBarImpl(get()) }
    single<RemoveBarSplit> { RemoveBarSplitImpl(get()) }
    single<GetAssignedFonts> { GetAssignedFontsImpl(get()) }
    single<GetDefaultTextSize> { GetDefaultTextSizeImpl(get()) }
    single<GetHelpKey> { GetHelpKeyImpl(get()) }
    single<SetHelpKey> { SetHelpKeyImpl(get()) }
    single<InsertTiesAtSelection> { InsertTiesAtSelectionImpl(get(), get()) }
    single<SetStemsAtSelection> { SetStemsAtSelectionImpl(get(), get()) }
    single<RemoveStemSettingsAtSelection> { RemoveStemSettingsAtSelectionImpl(get(), get()) }
    viewModel { InsertChooseViewModel(get()) }
    viewModel { DefaultInsertViewModel() }
    viewModel { BarNumberingViewModel(get(), get()) }
    viewModel { LyricInsertViewModel(get(), get(), get(), get()) }
    viewModel { HarmonyInsertViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { InstrumentInsertViewModel(get()) }
    viewModel { MetaInsertViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { OrnamentInsertViewModel() }
    viewModel { PageMarginsViewModel(get(), get()) }
    viewModel { TextInsertViewModel(get(), get()) }
    viewModel { parameters ->
      RowInsertViewModel<ArticulationType>(
        parameters.get(),
        parameters.get()
      )
    }
    viewModel { PageSizeViewModel(get(), get(), get(), get()) }
    viewModel { SegmentWidthViewModel(get(), get(), get()) }
    viewModel { TransposeViewModel(get()) }
    viewModel { TupletInsertViewModel() }
  }

  private val editModule = module {
    single<UpdateEventParam> { UpdateEventParamImpl(get()) }
    single<DeleteSelectedEvent> { DeleteSelectedEventImpl(get(), get()) }
    single<GetSelectedArea> { GetSelectedAreaImpl(get()) }
    single<MoveSelectedArea> { MoveSelectedAreaImpl(get()) }
    single<GetInstrumentAtSelection> { GetInstrumentAtSelectionImpl(get()) }
    single<SetInstrumentAtSelection> { SetInstrumentAtSelectionImpl(get()) }
    single<SetParamForSelected> { SetParamForSelectedImpl(get()) }
    single<ToggleBooleanForNotes> { ToggleBooleanForNotesImpl(get()) }
    single<MoveSelectedNote> { MoveSelectedNoteImpl(get()) }
    viewModel { EditViewModel(get(), get(), get(), get(), get()) }
    viewModel { HarmonyEditViewModel(get(), get(), get(), get(), get()) }
    viewModel { TextEditViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { InstrumentEditViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
  }

  private val screenModule = module {
    single { GetErrorImpl(get()) }.binds(arrayOf(SetError::class, GetError::class))
    single<ScoreChanged> { ScoreChangedImpl(get()) }
    single<GetUIState> { GetUIStateImpl(get()) }
    single { RedrawImpl() }.binds(arrayOf(Redraw::class, ListenForRedraw::class))
    single<DrawPage> { DrawPageImpl(get()) }
    single<GetScoreLayout> { GetScoreLayoutImpl(get()) }
    single<HandleTap> { HandleTapImpl(get(), get()) }
    single<HandleLongPress> { HandleLongPressImpl(get(), get()) }
    single<HandleDrag> { HandleDragImpl(get(), get()) }
    single<CheckForScore> { CheckForScoreImpl(get()) }
    single<GetLocation> { GetLocationImpl(get()) }
    viewModel {
      ScreenViewModel(
        get(),
        get(),
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
    viewModel { ScreenZoomViewModel(get(), get(), get(), get(), get()) }
    viewModel { MainPageViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ThemeViewModel(get()) }
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
    single<ToggleShuffle> { ToggleShuffleImpl(get()) }
    single<ToggleHarmonies> { ToggleHarmoniesImpl(get()) }
    single<ToggleLoop> { ToggleLoopImpl(get()) }
    single<GetPlaybackState> { GetPlaybackStateImpl(get()) }
    single<ToggleMute> { ToggleMuteImpl(get()) }
    single<ToggleSolo> { ToggleSoloImpl(get()) }
    single<SetHarmonyInstrument> { SetHarmonyInstrumentImpl(get()) }
    viewModel { PlayViewModel(get(), get(), get(), get()) }
    viewModel { MixerViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
  }

  private val clipboardModule = module {
    single<Copy> { CopyImpl(get(), get()) }
    single<Cut> { CutImpl(get(), get()) }
    single<Paste> { PasteImpl(get(), get()) }
    single<SetEndSelection> { SetEndSelectionImpl(get()) }
    single<GetSelection> { GetSelectionImpl(get()) }
    single<SelectionUpdate> { SelectionUpdateImpl(get()) }
    single<DeleteSelection> { DeleteSelectionImpl(get(), get()) }
    viewModel { ClipboardViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
  }

  private val settingsModule = module {
    single<GetColors> { GetColorsImpl(get()) }
    single<SetColors> { SetColorsImpl(get()) }
    single<GetAssignedFonts> { GetAssignedFontsImpl(get()) }
    single<SetFont> { SetFontImpl(get(), get()) }
    single<GetAvailableFonts> { GetAvailableFontsImpl(get()) }
    single { SettingsDataSource(get()) }
    single { SettingsRepository(get()) }
    single<GetOption> { GetOptionImpl(get()) }
    single<SetOption> { SetOptionImpl(get()) }
    single<AssignInstrument> { AssignInstrumentImpl(get()) }
    single<ClearInstrumentAssignments> { ClearInstrumentAssignmentsImpl(get())}
    single<UpdateFontOptions> { UpdateFontOptionsImpl(get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get()) }
    viewModel { LayoutOptionViewModel(get(), get()) }
    viewModel { InstrumentManageViewModel(get(), get(), get()) }
  }

  private val modules = listOf(
    scoreModule,
    errorModule,
    saveModule,
    exportModule,
    loadModule,
    printModule,
    startupModule,
    createModule,
    utilityModule,
    tabsModule,
    panelModule,
    insertModule,
    editModule,
    inputModule,
    screenModule,
    clipboardModule,
    soundModule,
    settingsModule
  )

  fun getModules(test: Boolean): List<Module> {
    return if (test) {
      stubModules + modules
    } else {
      androidModules + modules
    }
  }
}

