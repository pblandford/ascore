package org.philblandford.ui.insert.common.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.getViewModel
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.insert.common.viewmodel.DefaultInsertViewModel
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.model.DefaultInsertInterface
import org.philblandford.ui.insert.model.InsertCombinedState
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.util.SquareButton


@Composable
inline fun <M : InsertModel, I : InsertInterface<M>,
        reified VM : InsertViewModel<M, out I>> InsertVMView(
  viewModelFactory: @Composable () -> VM = { getViewModel() },
  contents: @Composable (M, InsertItem, I) -> Unit
) {

  val viewModel: VM = viewModelFactory()

  if (viewModel.resetOnLoad) {
    LaunchedEffect(Unit) {
      viewModel.reset()
    }
  }


  viewModel.getInsertState()
    .collectAsState(InsertCombinedState(null, null)).value.let { (state, insertItem) ->

      BackHandler {
        viewModel.back()
      }

      Column(Modifier.fillMaxWidth()) {
        insertItem?.let { insertItem ->
          Row(
            Modifier
              .fillMaxWidth()
              .padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            if (insertItem.string > 0) {
              Text(stringResource(insertItem.string), color = MaterialTheme.colorScheme.onSurface)
            }
            SquareButton(R.drawable.help) { viewModel.toggleHelp() }
          }
          state?.let { state ->
            contents(state, insertItem, viewModel.getInterface())
          }
        }
      }

    }
}

@Composable
inline fun DefaultInsertVMView( contents: @Composable (InsertModel, InsertItem, DefaultInsertInterface) -> Unit
) {

  InsertVMView<InsertModel, DefaultInsertInterface, DefaultInsertViewModel> { model, item, iface ->
    contents(model, item, iface)
  }
}