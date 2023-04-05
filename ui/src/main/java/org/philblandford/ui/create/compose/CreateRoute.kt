package org.philblandford.ui.create.compose

import androidx.compose.runtime.Composable
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.create.viewmodel.CreateViewModel
import com.github.zsoltk.compose.router.Router


sealed class CreateRoute {
  object MetaData : CreateRoute()
  object KeySignature : CreateRoute()
  object Instruments : CreateRoute()
}

@Composable
fun CreateScore(done: () -> Unit) {

  VMView(CreateViewModel::class.java) { state, iface, _ ->
    Router(CreateRoute.MetaData as CreateRoute) { backStack ->

      fun clear() {
        backStack.newRoot(CreateRoute.MetaData)
        done()
      }

      when (backStack.last()) {
        CreateRoute.MetaData -> {
          CreateMetaData(state, {
            backStack.push(CreateRoute.KeySignature)
          }, ::clear, iface)
        }
        CreateRoute.KeySignature -> {
          CreateKeyTimeSignature(state, { backStack.push(CreateRoute.Instruments) }, ::clear, iface)
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