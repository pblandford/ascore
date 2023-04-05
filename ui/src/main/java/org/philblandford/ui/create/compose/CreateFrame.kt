package org.philblandford.ui.create.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.philblandford.ui.R
import org.philblandford.ui.common.Gap

@Composable
fun CreateFrame(titleRes:Int, next:()->Unit, cancel:()->Unit, content: @Composable ColumnScope.() -> Unit) {
  CreateFrame(stringResource(titleRes), next, cancel, content)
}

@Composable
fun CreateFrame(title:String, next:()->Unit, cancel:()->Unit, content: @Composable ColumnScope.()->Unit) {
  Column(
    Modifier
      .fillMaxWidth(0.9f).padding(5.dp)
      .wrapContentHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Gap(0.2f)
    content()
    ButtonRow(next, cancel)
  }

}

@Composable
private fun ButtonRow(next: () -> Unit, cancel: () -> Unit) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
    Button(next) {
      Text(stringResource(R.string.next))
    }
    Button(cancel) {
      Text(stringResource(R.string.cancel))
    }
  }
}