package org.philblandford.ui.main.inputpage.compose

import Help
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.window.Dialog
import com.philblandford.kscore.log.ksLoge
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.crosscutting.model.ErrorDescr
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ui.LocalActivity
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.clipboard.compose.ClipboardExtraView
import org.philblandford.ui.clipboard.compose.ClipboardView
import org.philblandford.ui.common.block
import org.philblandford.ui.imports.IntentView
import org.philblandford.ui.main.inputpage.viewmodel.MainPageModel
import org.philblandford.ui.main.inputpage.viewmodel.MainPageSideEffect
import org.philblandford.ui.main.inputpage.viewmodel.MainPageViewModel
import org.philblandford.ui.main.panel.compose.Panel
import org.philblandford.ui.main.toprow.TopRow
import org.philblandford.ui.main.utility.compose.UtilityRow
import org.philblandford.ui.screen.compose.ScreenView
import org.philblandford.ui.screen.compose.ScreenZoom
import org.philblandford.ui.util.DraggableItem
import timber.log.Timber


@Composable
fun MainPageView(
    openDrawer: () -> Unit, setPopupLayout: (LayoutID) -> Unit, toggleMixer: () -> Unit,
    onScoreEmpty: () -> Unit,
) {
    val activity = LocalActivity.current

    var intent by rememberSaveable { mutableStateOf(activity?.intent) }

    LaunchedEffect(activity?.intent?.data) {
        intent = activity?.intent
    }

    VMView(MainPageViewModel::class.java) { state, iface, effect ->

        val coroutineScope = rememberCoroutineScope()
        val alertText = remember { mutableStateOf<ErrorDescr?>(null) }

        val currentPage = remember { mutableIntStateOf(1) }

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                effect.collectLatest { effect ->
                    when (effect) {
                        is MainPageSideEffect.Error -> alertText.value = effect.errorDescr
                    }
                }
            }
        }

        alertText.value?.let { errorDescr ->
            AlertDialog(
                onDismissRequest = { alertText.value = null },
                confirmButton = { Button({ alertText.value = null }) { Text("OK") } },
                containerColor = MaterialTheme.colorScheme.surface,
                textContentColor = MaterialTheme.colorScheme.onSurface,
                text = {
                    Text(errorDescr.message, color = MaterialTheme.colorScheme.onSurface)
                },
                title = {
                    Text(
                        errorDescr.headline, style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                })
        }

        state.helpKey?.let {
            Dialog({ iface.dismissHelp() }) {
                Help(it) { iface.dismissHelp() }
            }
        }

        ksLoge("Have Score ${iface.haveScore()}")
        var showIntentView by remember { mutableStateOf(!iface.haveScore()
                || intent?.action == "android.intent.action.VIEW") }
        if (showIntentView) {
            Dialog({}) {
                IntentView(intent) { showIntentView = false }
            }
        }

        Box(Modifier.fillMaxSize()) {
            val showPanel = rememberSaveable { mutableStateOf(true) }
            val fullScreen = rememberSaveable { mutableStateOf(false) }

            BackHandler(fullScreen.value) {
                fullScreen.value = false
            }

            if (fullScreen.value) {
                ScreenBox(
                    Modifier.fillMaxSize(),
                    true,
                    state,
                    currentPage,
                    onScoreEmpty,
                    iface::toggleVertical
                )
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                ) {
                    TopRow(
                        Modifier.height(block()),
                        state.canShowTabs,
                        state.vertical,
                        openDrawer,
                        { setPopupLayout(LayoutID.LAYOUT_OPTIONS) },
                        { fullScreen.value = true },
                        { toggleMixer() },
                        { iface.toggleVertical() })

                    Box(Modifier.fillMaxWidth()) {

                        ScreenBox(
                            Modifier.fillMaxSize(),
                            false,
                            state,
                            currentPage,
                            onScoreEmpty,
                            iface::toggleVertical
                        )
                        Timber.e("Panel show ${showPanel.value}")

                        Column(Modifier.align(Alignment.BottomCenter)) {
                            AnimatedVisibility(
                                showPanel.value, enter = slideInVertically { it },
                                exit = slideOutVertically { it }
                            ) {
                                Panel()
                            }
                            UtilityRow(showPanel.value) { showPanel.value = !showPanel.value }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenBox(
    modifier: Modifier, center: Boolean, state: MainPageModel,
    currentPage: MutableState<Int>,
    onScoreEmpty: () -> Unit,
    changeMethod: () -> Unit
) {

    val clipboardOffset = remember { mutableStateOf(Offset(0f, 10f)) }
    val clipboardExtraOffset = remember { mutableStateOf(Offset(0f, -50f)) }
    val zoomOffset = remember { mutableStateOf(Offset(0f, 0f)) }

    Box(modifier) {
        ScreenView(state.vertical, center, currentPage, onScoreEmpty, changeMethod)

        if (state.showClipboard) {
            DraggableItem(Modifier.align(Alignment.TopCenter), clipboardOffset) {
                ClipboardView(Modifier)
            }
            DraggableItem(Modifier.align(Alignment.BottomCenter), clipboardExtraOffset) {
                ClipboardExtraView()
            }
        }

        if (state.showNoteZoom) {
            state.selectedArea?.let { address ->
                DraggableItem(Modifier.align(Alignment.TopEnd), zoomOffset) {
                    ScreenZoom(
                        Modifier
                            .align(Alignment.TopEnd), address
                    )
                }
            }
        }
    }
}