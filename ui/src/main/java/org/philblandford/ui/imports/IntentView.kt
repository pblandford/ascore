package org.philblandford.ui.imports

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.block
import org.philblandford.ui.imports.model.ImportModel
import org.philblandford.ui.imports.viewmodel.ImportSideEffect
import org.philblandford.ui.imports.viewmodel.ImportViewModel
import org.philblandford.ui.main.toprow.PlayViewModel
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.StandardAlert
import timber.log.Timber


@Composable
fun IntentView(intent: Intent, done: () -> Unit) {

  Box(
    Modifier
      .fillMaxWidth()
      .fillMaxHeight(0.5f)
  ) {

    VMView(ImportViewModel::class.java) { model, iface, effects ->

      val exception: MutableState<Exception?> = remember { mutableStateOf(null) }
      val coroutineScope = rememberCoroutineScope()
      val intentData by rememberSaveable { mutableStateOf(intent.data) }
      var completedFileName by remember { mutableStateOf<String?>(null) }

      LaunchedEffect(Unit) {
        coroutineScope.launch {
          effects.collect {
            when (it) {
              is ImportSideEffect.Error -> exception.value = it.exception
              is ImportSideEffect.Complete -> {
                it.fileName?.let { completedFileName = it } ?: run { done() }
              }
            }
          }
        }
      }

      completedFileName?.let { name ->
        StandardAlert(stringResource(R.string.file_import_confirm, name)) {
          completedFileName = null; done()
        }
      } ?: run {
        IntentLayout(model)
      }

      exception.value?.let {
        Dialog({ exception.value = null; done() }) {
          Text(it.message ?: "")
          exception.value = null
          done()
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

    }
  }
}


@Composable
private fun IntentLayout(model: ImportModel) {

  DialogTheme { modifier ->
    Box(
      modifier
        .fillMaxWidth()
        .padding(10.dp)
        .background(MaterialTheme.colorScheme.surface)
    ) {
      Column(
        Modifier
          .fillMaxSize(),
        horizontalAlignment = Alignment.Start
      ) {
        Image(
          painterResource(id = R.drawable.logo),
          "",
          Modifier.width(block(3)),
          colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        )
        Gap(0.5f)
        if (model.name.isNotEmpty()) {
          Label(stringResource(R.string.importing_file, model.name))
          Gap(0.5f)
        }
        Label(model.action)
        Gap(0.5f)
        LinearProgressIndicator(model.progress / 100f, Modifier.fillMaxWidth())
      }
    }
  }
}

@Composable
private fun Label(text: String, modifier: Modifier = Modifier) {
  Text(text, modifier, fontSize = 16.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
}