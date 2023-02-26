package org.philblandford.ui.base.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.getViewModel
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect

@Composable
inline fun <M : VMModel, I : VMInterface, S : VMSideEffect, reified VM : BaseViewModel<M, I, S>> VMView(
  viewModelClass: Class<out VM>,
  modifier: Modifier = Modifier,
  tag: String = "",
  viewModelFactory: @Composable ()->VM = { getViewModel() },
  contents: @Composable (M, I, Flow<S>) -> Unit
) {

  val viewModel: VM = viewModelFactory()

  if (viewModel.resetOnLoad) {
    LaunchedEffect(Unit) {
      viewModel.reset()
    }
  }
  viewModel.getState().collectAsState().value?.let { model ->
    Box(modifier.testTag(tag)) {
      contents(model, viewModel.getInterface(), viewModel.effectFlow)
    }
  }
}


@Composable
inline fun <M : VMModel, I : VMInterface, S : VMSideEffect, reified VM : BaseViewModel<M, out I, S>> VMView(
  modifier: Modifier = Modifier,
  tag: String = "",
  viewModelFactory: @Composable ()->VM = { getViewModel() },
  contents: @Composable (M, I, Flow<S>) -> Unit
) {

  val viewModel: VM = viewModelFactory()

  if (viewModel.resetOnLoad) {
    LaunchedEffect(Unit) {
      viewModel.reset()
    }
  }

  viewModel.getState().collectAsState().value?.let { model ->
    Box(modifier.testTag(tag)) {
      contents(model, viewModel.getInterface(), viewModel.effectFlow)
    }
  }
}

