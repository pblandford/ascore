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
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import com.github.zsoltk.compose.backpress.BackPressHandler
import com.github.zsoltk.compose.backpress.LocalBackPressHandler
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.imports.activity.IntentActivity
import org.philblandford.ui.main.outer.compose.OuterPage
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.theme.compose.AscoreTheme
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
              OuterPage()
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
