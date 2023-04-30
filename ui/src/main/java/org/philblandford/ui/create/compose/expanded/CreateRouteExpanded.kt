package org.philblandford.ui.create.compose.expanded

import androidx.compose.runtime.Composable
import com.github.zsoltk.compose.router.Router
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.create.compose.compact.CreateInstruments
import org.philblandford.ui.create.compose.compact.CreateMetaData
import org.philblandford.ui.create.viewmodel.CreateViewModel

private sealed class CreateRoute {
  object MetaData : CreateRoute()
  object Page2 : CreateRoute()
  object Instruments : CreateRoute()
}

@Composable
fun CreateScoreExpanded(done: () -> Unit) {

  VMView(CreateViewModel::class.java) { state, iface, _ ->
    Router(CreateRoute.MetaData as CreateRoute) { backStack ->

      fun clear() {
        backStack.newRoot(CreateRoute.MetaData)
        done()
      }

      when (backStack.last()) {
        CreateRoute.MetaData -> {
          CreateMetaData(state, {
            backStack.push(CreateRoute.Page2)
          }, ::clear, iface)
        }
        CreateRoute.Page2 -> {
          CreatePage2Expanded(state, { backStack.push(CreateRoute.Instruments) }, ::clear, iface)
        }
        CreateRoute.Instruments -> {
          CreateInstruments(state.availableInstruments, state.newScoreDescriptor.instruments.toList(), {
            iface.create()
            clear()
          }, ::clear, iface)
        }
      }
    }
  }
}