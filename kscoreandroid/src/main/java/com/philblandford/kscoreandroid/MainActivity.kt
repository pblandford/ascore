package com.philblandford.kscoreandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.philblandford.kscore.engine.core.representation.StandaloneGenerator
import com.philblandford.kscore.engine.duration.Chord
import com.philblandford.kscore.engine.duration.Note
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.types.NoteLetter
import com.philblandford.kscore.engine.types.Pitch
import com.philblandford.kscoreandroid.drawingcompose.ComposeDrawableGetter
import com.philblandford.kscoreandroid.text.AndroidTextFontManager
import com.philblandford.kscoreandroid.ui.theme.KScoreAndroidTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      KScoreAndroidTheme {


        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {

        }
      }
    }
  }
}

@Composable
fun Greeting(name: String) {
  Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  KScoreAndroidTheme {
    Greeting("Android")
  }
}