package org.philblandford.ui.imports.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.philblandford.ui.MainActivity
import org.philblandford.ui.imports.model.ImportModel
import org.philblandford.ui.imports.viewmodel.ImportViewModel
import org.philblandford.ui.theme.compose.AscoreTheme
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.block
import org.philblandford.ui.imports.viewmodel.ImportSideEffect
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.Gap
import timber.log.Timber


class IntentActivity : ComponentActivity(), KoinComponent {

  override fun onCreate(savedInstanceState: Bundle?) {

    Timber.e("onCreate $this")

    super.onCreate(savedInstanceState)

    setContent {

      AscoreTheme {
        val uiController = rememberSystemUiController()

        uiController.setStatusBarColor(MaterialTheme.colorScheme.surface)
        uiController.setNavigationBarColor(MaterialTheme.colorScheme.surface)

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

  DialogTheme { modifier ->

      Column {
        if (model.name.isNotEmpty()) {
          Label(stringResource(R.string.importing_file, model.name))
        }
        LinearProgressIndicator(model.progress / 100f, Modifier.fillMaxWidth())
      }
  }
}

@Composable
private fun Label(text: String, modifier: Modifier = Modifier) {
  Text(text, modifier, fontSize = 16.sp, maxLines = 1,color = MaterialTheme.colorScheme.onSurface)
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
