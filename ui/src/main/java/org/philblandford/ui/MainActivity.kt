package org.philblandford.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.github.zsoltk.compose.backpress.BackPressHandler
import com.github.zsoltk.compose.backpress.LocalBackPressHandler
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.philblandford.ui.main.inputpage.compose.MainPageView
import org.philblandford.ui.main.outer.compose.OuterPage
import org.philblandford.ui.theme.AscoreTheme

val LocalActivity = compositionLocalOf<Activity?> { null }

class MainActivity : ComponentActivity() {
  private val backPressHandler = BackPressHandler()


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {

      AscoreTheme {
        val uiController = rememberSystemUiController()

        uiController.setStatusBarColor(MaterialTheme.colors.surface)
        uiController.setNavigationBarColor(MaterialTheme.colors.surface)
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
          CompositionLocalProvider(
            LocalBackPressHandler provides backPressHandler,
            LocalActivity provides this
          ) {
            OuterPage()
          }
        }
      }
    }
  }


  override fun onBackPressed() {
    if (!backPressHandler.handle()) {
      super.onBackPressed()
    }
  }
}
