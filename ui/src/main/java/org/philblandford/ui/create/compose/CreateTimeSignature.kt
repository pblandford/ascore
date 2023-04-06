package org.philblandford.ui.create.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philblandford.kscore.api.NewScoreDescriptor
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.TimeSignatureType
import org.philblandford.ui.create.viewmodel.CreateInterface
import org.philblandford.ui.create.viewmodel.CreateModel
import org.philblandford.ui.R
import org.philblandford.ui.common.Gap
import org.philblandford.ui.theme.PopupTheme
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
fun TimeSelector(modifier:Modifier, newScoreDescriptor: NewScoreDescriptor, iface: CreateInterface) {
  with(newScoreDescriptor.timeSignature) {

    Column(modifier) {

      Row(
        Modifier.align(Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
      ) {
        SquareButton(
          R.drawable.common, Modifier.padding(10.dp),
          dim = newScoreDescriptor.timeSignature.type != TimeSignatureType.COMMON,
          size = 55.dp
        ) {
          iface.setTimeSignature {
            if (this.type == TimeSignatureType.COMMON) TimeSignature.custom(
              4,
              4
            ) else TimeSignature.common()
          }
        }
        Gap(1f)
        SquareButton(
          R.drawable.cut_common,
          dim = newScoreDescriptor.timeSignature.type != TimeSignatureType.CUT_COMMON,
          size = 65.dp
        ) {
          iface.setTimeSignature {
            if (this.type == TimeSignatureType.CUT_COMMON) TimeSignature.custom(
              2,
              2
            ) else TimeSignature.cutCommon()
          }
        }
      }
      Gap(0.5)
      CustomTimeSelector(numerator, denominator, {
        iface.setTimeSignature { TimeSignature.custom(it, denominator) }
      }, {
        iface.setTimeSignature { TimeSignature.custom(numerator, it) }
      })
      Gap(0.5)
    }
  }
}

@Composable
private fun SelectRow(textRes: Int, content: @Composable () -> Unit) {
  Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Text(
      stringResource(textRes), Modifier.fillMaxWidth(0.35f),
      fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End
    )
    Gap(1f)
    content()
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


