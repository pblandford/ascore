package org.philblandford.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.philblandford.ui.theme.compose.AscoreTheme


@Composable
fun DialogTheme(dismiss:(()->Unit)? = null,
                content: @Composable BoxScope.(Modifier) -> Unit) {
  AscoreTheme {

    val interactionSource = remember { MutableInteractionSource() }

      Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
          content(
            Modifier
              .clip(RoundedCornerShape(10))
              .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10))
              .padding(10.dp)
              .align(Alignment.Center).clickable(interactionSource, indication = null) {  }
          )
        }
    }
  }
}

@Composable
fun DialogButton(text: String, modifier: Modifier = Modifier, action: () -> Unit) {
  Button(action, modifier, shape = CircleShape) {
    Text(text, style = MaterialTheme.typography.bodyLarge)
  }
}
