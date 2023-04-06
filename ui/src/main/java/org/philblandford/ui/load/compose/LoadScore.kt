package org.philblandford.ui.load.compose

import FileInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.godaddy.android.colorpicker.HsvColor
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.types.FileSource
import com.philblandford.kscore.engine.types.Horizontal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.load.viewmodels.LoadInterface
import org.philblandford.ui.load.viewmodels.LoadModel
import org.philblandford.ui.load.viewmodels.LoadViewModel
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.theme.PopupTheme
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.LabelText
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun LoadScore(dismiss: () -> Unit) {
  VMView(LoadViewModel::class.java) { state, iface, _ ->
    DialogTheme { modifier ->
      LoadScoreInternal(modifier, state, iface, dismiss)
    }
  }
}

@Composable
private fun LoadScoreInternal(
  modifier: Modifier,
  model: LoadModel, iface: LoadInterface, dismiss: () -> Unit
) {
  Column(
    modifier
      .fillMaxWidth().fillMaxHeight(0.7f), horizontalAlignment = Alignment.CenterHorizontally
  ) {
    LabelText(stringResource(R.string.load_score_title))
    Gap(0.5f)
    if (model.fileNames.isEmpty()) {
      Box(
        Modifier
          .fillMaxWidth()
          .height(100.dp)
      ) {
        Text(
          stringResource(R.string.load_score_no_files), Modifier.align(Alignment.Center),
          Color.LightGray
        )
      }
    } else {
      ScorePager(model, iface, dismiss)
    }
  }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ColumnScope.ScorePager(
  model: LoadModel, iface: LoadInterface,
  dismiss: () -> Unit
) {
  data class FileSourceType(val fileSource: FileSource, val nameRes: Int)

  val fileSourceTypes = listOf(
    FileSourceType(FileSource.SAVE, R.string.files_in_storage),
    FileSourceType(FileSource.AUTOSAVE, R.string.recent_files),
    FileSourceType(FileSource.EXTERNAL, R.string.files_in_external_storage)
  )
  val coroutineScope = rememberCoroutineScope()
  val pagerState = rememberPagerState()
  var deleteConfirm by remember { mutableStateOf<FileInfo?>(null) }

  TabRow(pagerState.currentPage) {
    fileSourceTypes.forEachIndexed { index, fileSourceType ->
      Tab(index == pagerState.currentPage,
        selectedContentColor = MaterialTheme.colors.onSurface,
        unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
        modifier = Modifier.background(MaterialTheme.colors.surface),
        onClick = {
          coroutineScope.launch {
            pagerState.animateScrollToPage(index)
          }
        }, text = {
          Text(stringResource(fileSourceType.nameRes))
        })
    }
  }

  Gap(10.dp)

  HorizontalPager(
    model.fileNames.size,
    Modifier
      .fillMaxWidth()
      .weight(1f),
    state = pagerState
  ) { idx ->
    LazyColumn(
      Modifier
        .fillMaxSize()
        .padding(5.dp)
    ) {
      items(model.fileNames[fileSourceTypes[idx].fileSource] ?: listOf()) { fileInfo ->
        ScoreDetailCard(fileInfo, {
          iface.load(fileInfo)
          dismiss()
        }) { deleteConfirm = it }
        Gap(10.dp)
      }
    }
  }

  deleteConfirm?.let { fileInfo ->
    ConfirmDialog(fileInfo, { iface.delete(fileInfo) }) {
      deleteConfirm = null
    }
  }

}

@Composable
private fun ConfirmDialog(fileInfo: FileInfo, action: () -> Unit, dismiss: () -> Unit) {
  Dialog(onDismissRequest = dismiss) {
    DialogTheme { modifier ->
      Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.file_delete_confirm, fileInfo.name))
        Gap(10.dp)
        Row {
          Button({ action(); dismiss() }) {
            Text(stringResource(R.string.ok))
          }
          Gap(1f)
          Button(dismiss) {
            Text(stringResource(R.string.cancel))
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ScoreDetailCard(
  fileInfo: FileInfo, select: (FileInfo) -> Unit,
  delete: (FileInfo) -> Unit
) {
  Card(
    { select(fileInfo) },
    Modifier.fillMaxWidth(),
    backgroundColor = MaterialTheme.colors.secondary,
    contentColor = MaterialTheme.colors.onSurface,
    shape = RoundedCornerShape(10)
  ) {
    Column(
      Modifier
        .fillMaxWidth()
        .padding(5.dp),
    ) {
      Row() {
        Text(fileInfo.name, Modifier.weight(1f))
        Icon(painterResource(R.drawable.delete), stringResource(R.string.delete),
          Modifier
            .size(30.dp)
            .padding(5.dp)
            .clickable { delete(fileInfo) }, tint = Color.Black
        )
      }
      Gap(0.5f)
      Text(
        SimpleDateFormat.getDateTimeInstance().format(Date(fileInfo.accessTime)),
        Modifier.align(Alignment.End), fontSize = 15.sp, fontWeight = FontWeight.Light
      )
    }
  }
}

@Composable
@Preview
private fun Preview() {
  val files =
    (1..5).map { FileInfo("File number $it", "", FileSource.SAVE,
      kotlin.random.Random.nextInt().toLong() * 1000) }
  val model = LoadModel(
    mapOf(FileSource.SAVE to files)
  )

  DialogTheme {

    LoadScoreInternal(it, model, object : LoadInterface {
      override fun reset() {
        TODO("Not yet implemented")
      }

      override fun load(fileInfo: FileInfo) {
        TODO("Not yet implemented")
      }

      override fun delete(fileInfo: FileInfo) {
        TODO("Not yet implemented")
      }

      override fun getSideEffects(): Flow<VMSideEffect> {
        TODO("Not yet implemented")
      }
    }) {

    }
  }

}