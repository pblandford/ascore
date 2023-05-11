package org.philblandford.ui.imports.compose

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import org.philblandford.ui.MainActivity


@Composable
fun ImportView() {
  val context = LocalContext.current

  val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument() ) { res ->
    context.startActivity(Intent(context, MainActivity::class.java).apply {
      data = res
    })
  }

  LaunchedEffect(Unit) {
    launcher.launch(arrayOf("*/*"))
  }
}