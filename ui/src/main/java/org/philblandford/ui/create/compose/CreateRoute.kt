package org.philblandford.ui.create.compose

import androidx.compose.runtime.Composable
import com.github.zsoltk.compose.router.Router
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.create.viewmodel.CreateViewModel


sealed class CreateRoute {
  object MetaData : CreateRoute()
  object KeySignature : CreateRoute()
  object Tempo : CreateRoute()
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
          CreateKeyTimeSignature(state, { backStack.push(CreateRoute.Tempo) }, ::clear, iface)
        }
        CreateRoute.Tempo -> {
          CreateTempo(state, { backStack.push(CreateRoute.Instruments) }, ::clear, iface)
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