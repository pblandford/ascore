package org.philblandford.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philblandford.ascore.android.ui.style.lightBlue
import com.philblandford.ascore.android.ui.style.lightBlue2
import com.philblandford.ascore.android.ui.style.powderBlue


@Composable
fun DialogTheme(content: @Composable BoxScope.(Modifier) -> Unit) {
  AscoreTheme {


    val typography = Typography(
      body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp
      ),
      body2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
      ),
      button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
      ),
      h1 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 21.sp
      ),
      h2 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 19.sp
      )
    )
    val colors = MaterialTheme.colors

    MaterialTheme(colors = colors, typography = typography) {
      Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.onSurface) {
          content(
            Modifier
              .clip(RoundedCornerShape(10))
              .background(MaterialTheme.colors.surface, RoundedCornerShape(10))
              .padding(10.dp)
              .align(Alignment.Center)
          )
        }
      }
    }
  }
}

@Composable
fun DialogButton(text: String, modifier: Modifier = Modifier, action: () -> Unit) {
  Button(action, modifier, shape = CircleShape) {
    Text(text, style = MaterialTheme.typography.h2)
  }
}
