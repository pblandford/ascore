package org.philblandford.ui.create.compose

import androidx.compose.runtime.Composable
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.create.viewmodel.CreateViewModel
import com.github.zsoltk.compose.router.Router


sealed class CreateRoute {
  object MetaData : CreateRoute()
  object KeySignature : CreateRoute()
}

@Composable
fun CreateScore() {

  VMView(CreateViewModel::class.java) { state, iface, _ ->
    Router(CreateRoute.MetaData as CreateRoute) { backStack ->
      when (backStack.last()) {
        CreateRoute.MetaData -> {
          CreateMetaData(state, {
            backStack.push(CreateRoute.KeySignature)
          }, {}, iface)
        }
        CreateRoute.KeySignature -> {
          CreateKeySignature(state, next = { }, cancel = { }, iface)
        }
      }
    }
  }
}