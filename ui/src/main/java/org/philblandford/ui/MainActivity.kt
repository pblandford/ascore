package org.philblandford.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import com.github.zsoltk.compose.backpress.BackPressHandler
import com.github.zsoltk.compose.backpress.LocalBackPressHandler
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.inject
import org.philblandford.ascore2.features.settings.usecases.GetColors
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.imports.activity.IntentActivity
import org.philblandford.ui.main.outer.compose.OuterPage
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.theme.AscoreTheme
import org.philblandford.ui.theme.viewmodel.ThemeViewModel
import timber.log.Timber

val LocalActivity = compositionLocalOf<Activity?> { null }


class MainActivity : ComponentActivity() {
  private val backPressHandler = BackPressHandler()

  @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    Timber.e("onCreate $this")

    super.onCreate(savedInstanceState)
    setContent {
      val windowSizeClass = calculateWindowSizeClass(this)


      VMView(ThemeViewModel::class.java) { model, _, _ ->

        AscoreTheme(model.colorScheme) {


          val uiController = rememberSystemUiController()

          uiController.setStatusBarColor(MaterialTheme.colorScheme.surface)
          uiController.setNavigationBarColor(MaterialTheme.colorScheme.surface)

          Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            CompositionLocalProvider(
              LocalBackPressHandler provides backPressHandler,
              LocalActivity provides this,
              LocalContentColor provides MaterialTheme.colorScheme.onSurface,
              LocalWindowSizeClass provides windowSizeClass
            ) {
              OuterPage(::launchIntentActivity)
            }

          }
        }
      }
    }
  }

  private fun launchIntentActivity() {
    startActivity(Intent(this, IntentActivity::class.java))
  }


  override fun onBackPressed() {
    if (!backPressHandler.handle()) {
      super.onBackPressed()
    }
  }
}
