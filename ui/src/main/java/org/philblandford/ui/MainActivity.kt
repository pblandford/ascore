package org.philblandford.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.github.zsoltk.compose.backpress.BackPressHandler
import com.github.zsoltk.compose.backpress.LocalBackPressHandler
import com.philblandford.kscore.log.ksLoge
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.main.outer.compose.OuterPage
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.theme.compose.AscoreTheme
import org.philblandford.ui.theme.viewmodel.ThemeViewModel


val LocalActivity = compositionLocalOf<Activity?> { null }


class MainActivity : ComponentActivity() {
    private val backPressHandler = BackPressHandler()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        ksLoge("MainActivity.onCreate $savedInstanceState")

        super.onCreate(savedInstanceState)
        setContent {

            val windowSizeClass = calculateWindowSizeClass(this)


            VMView(ThemeViewModel::class.java) { model, _, _ ->
                val colorScheme = model.colorScheme

                LaunchedEffect(model.colorScheme) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.light(
                            colorScheme.surface.toArgb(),
                            colorScheme.surface.toArgb()
                        ),
                        navigationBarStyle = SystemBarStyle.light(
                            colorScheme.surface.toArgb(),
                            colorScheme.surface.toArgb()
                        )
                    )
                    val light = colorScheme.surface.luminance() > 0.5
                    WindowInsetsControllerCompat(
                        window,
                        window.decorView
                    ).isAppearanceLightStatusBars = light
                    WindowInsetsControllerCompat(
                        window,
                        window.decorView
                    ).isAppearanceLightNavigationBars = light

                }
                AscoreTheme(model.colorScheme) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets.safeContent,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) { padding ->
                        CompositionLocalProvider(
                            LocalBackPressHandler provides backPressHandler,
                            LocalActivity provides this,
                            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                            LocalWindowSizeClass provides windowSizeClass
                        ) {
                            OuterPage(Modifier.padding(padding))
                        }
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
