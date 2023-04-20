package org.philblandford.ui.imports.activity

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.NonDisposableHandle.parent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.philblandford.ui.MainActivity
import org.philblandford.ui.imports.model.ImportModel
import org.philblandford.ui.imports.viewmodel.ImportViewModel
import org.philblandford.ui.theme.AscoreTheme
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.block
import org.philblandford.ui.imports.viewmodel.ImportSideEffect
import org.philblandford.ui.util.Gap
import timber.log.Timber


class IntentActivity : ComponentActivity(), KoinComponent {

  override fun onCreate(savedInstanceState: Bundle?) {

    Timber.e("onCreate $this")

    super.onCreate(savedInstanceState)

    setContent {

      AscoreTheme {
        val uiController = rememberSystemUiController()

        uiController.setStatusBarColor(MaterialTheme.colors.surface)
        uiController.setNavigationBarColor(MaterialTheme.colors.surface)

        VMView(ImportViewModel::class.java) { model, iface, effects ->

          val exception: MutableState<Exception?> = remember { mutableStateOf(null) }
          val coroutineScope = rememberCoroutineScope()
          val intentData by rememberSaveable { mutableStateOf(intent.data) }

          LaunchedEffect(Unit) {
            coroutineScope.launch {
              effects.collect {
                when (it) {
                  is ImportSideEffect.Error -> exception.value = it.exception
                  ImportSideEffect.Complete -> loadMain()
                }
              }
            }
          }

          exception.value?.let {
            // FirebaseCrashlytics.getInstance().recordException(it)
            Dialog({ exception.value = null; loadMain() }) {
              Text(it.message ?: "")
              exception.value = null
              loadMain()
            }
          }

          LaunchedEffect(intentData) {
            Timber.e("Launching $intentData")
            intentData?.let {
              iface.import(it)
            } ?: run {
              iface.start()
            }
          }
          IntentLayout(model)
        }
      }
    }
  }
}

private fun Activity.loadMain() {
  startActivity(
    Intent(
      this,
      MainActivity::class.java
    )//.apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
  )
  finish()
}

@Composable
private fun IntentLayout(model: ImportModel) {

  Box(
    Modifier
      .fillMaxSize()
      .background(MaterialTheme.colors.surface)) {
    Column(
      Modifier
        .fillMaxSize()
        .offset(y = 50.dp)
        .padding(horizontal = 30.dp, vertical = 50.dp), horizontalAlignment = Alignment.Start
    ) {
      Image(
        painterResource(id = R.drawable.logo), "",
        Modifier.width(block(3)), colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
      )
      Gap(1f)
      if (model.name.isNotEmpty()) {
        Label(stringResource(R.string.importing_file, model.name))
        Gap(0.5f)
      }
      Label(model.action)
      Gap(0.5f)
      Label(model.subAction)
      Gap(2f)
      LinearProgressIndicator(model.progress / 100f, Modifier.fillMaxWidth())
    }
  }
}

@Composable
private fun Label(text: String, modifier: Modifier = Modifier) {
  Text(text, modifier, fontSize = 16.sp, maxLines = 1,color = MaterialTheme.colors.onSurface)
}


@Composable
@Preview
private fun Preview() {
  AscoreTheme() {

    IntentLayout(
      ImportModel(
        "Fish",
        "Wibbling wobbles", "Arranging blobs", 0.5f
      )
    )
  }

}
