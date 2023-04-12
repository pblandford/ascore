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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.philblandford.ui.MainActivity
import org.philblandford.ui.imports.model.ImportModel
import org.philblandford.ui.imports.viewmodel.ImportViewModel
import timber.log.Timber


class IntentActivity : ComponentActivity(), KoinComponent {

  override fun onCreate(savedInstanceState: Bundle?) {

    super.onCreate(savedInstanceState)

    val viewModel:ImportViewModel by viewModel()

    setContent {
      val exception: MutableState<Exception?> = remember { mutableStateOf(null) }
      val coroutineScope = rememberCoroutineScope()

      exception.value?.let {
       // FirebaseCrashlytics.getInstance().recordException(it)
        Dialog({ exception.value = null}) {
          Text(it.message ?: "")
//          exception.value = null
//          loadMain()
        }
      }

      Timber.e("INTENT ${intent}")

      LaunchedEffect(intent.data) {
        coroutineScope.launch {
          intent.data?.let {
            viewModel.import(it)
            loadMain()
          }
        }
      }

      val model = viewModel.getState().collectAsState()
      Timber.e("OI!! $model")


      Dialog({  }) {
        model.value?.let {
          Box(
            Modifier
              .fillMaxWidth()
              .height(200.dp)) {
            LinearProgressIndicator(it.progress, Modifier.align(Alignment.Center))
          }
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
    )
  )
}

@Composable
private fun IntentLayout(importModel: ImportModel) {

  Dialog({  }) {
      Box(
        Modifier
          .fillMaxWidth()
          .height(200.dp)) {
        LinearProgressIndicator(importModel.progress, Modifier.align(Alignment.Center))
      }
  }
}

@Composable
@Preview
private fun Preview() {
  IntentLayout(ImportModel("Fish", 0.5f))

}
