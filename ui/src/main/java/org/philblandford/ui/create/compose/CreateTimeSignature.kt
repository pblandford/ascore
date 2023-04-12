package org.philblandford.ui.create.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.api.NewScoreDescriptor
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.TimeSignatureType
import org.philblandford.ui.R
import org.philblandford.ui.common.Gap
import org.philblandford.ui.create.viewmodel.CreateInterface
import org.philblandford.ui.create.viewmodel.CreateModel
import org.philblandford.ui.theme.PopupTheme
import org.philblandford.ui.util.ButtonState.Companion.selected
import org.philblandford.ui.util.CustomTimeSelector
import org.philblandford.ui.util.SquareButton

@Composable
internal fun CreateTimeSignature(
  model: CreateModel,
  next: () -> Unit,
  cancel: () -> Unit,
  iface: CreateInterface
) {

  WizardFrame(R.string.create_score_time_signature, next, cancel) {


    }
}
@Composable
fun TimeSelector(modifier:Modifier, timeSignature: TimeSignature, set:(TimeSignature)->Unit) {
  with(timeSignature) {

    Column(modifier) {

      Row(
        Modifier.align(Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
      ) {
        SquareButton(
          R.drawable.common, Modifier.padding(10.dp),
          selected(type == TimeSignatureType.COMMON),
          size = 55.dp
        ) {
          set (
            if (type == TimeSignatureType.COMMON) TimeSignature.custom(
              4,
              4
            ) else TimeSignature.common()
          )
        }
        Gap(1f)
        SquareButton(
          R.drawable.cut_common,
          state = selected(type == TimeSignatureType.CUT_COMMON),
          size = 65.dp
        ) {
          set (
            if (type == TimeSignatureType.CUT_COMMON) TimeSignature.custom(
              2,
              2
            ) else TimeSignature.cutCommon()
          )
        }
      }
      Gap(0.5)
      CustomTimeSelector(timeSignature, set)
      Gap(0.5)
    }
  }
}

@Composable
@Preview
private fun Preview() {
  PopupTheme {
    Box(Modifier.fillMaxSize()) {
      CreateTimeSignature(CreateModel(NewScoreDescriptor()), {}, {}, StubCreateInterface())
    }
  }
}


