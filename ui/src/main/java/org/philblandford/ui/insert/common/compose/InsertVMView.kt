package org.philblandford.ui.insert.common.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.util.SquareButton

@Composable
inline fun <M : InsertModel, I : InsertInterface<M>,
        reified VM : InsertViewModel<M, I>> InsertVMView(
  model:M,
  content: @Composable (M, I) -> Unit
) {

    VMView<M, I, VMSideEffect, VM>() { state, iface, _ ->

      BackHandler {
        iface.back()
      }

      LaunchedEffect(model) {
        iface.initialise(model)
      }

      Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween) {
          if (state.title > 0) {
            Text(stringResource(state.title))
          }
          SquareButton(R.drawable.help)
        }
        content(state, iface)
      }
    }

}