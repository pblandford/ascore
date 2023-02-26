package org.philblandford.ui.create.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philblandford.kscore.api.NewScoreDescriptor
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.MetaType
import com.philblandford.kscore.engine.types.TimeSignatureType
import org.philblandford.ui.common.block
import org.philblandford.ui.create.viewmodel.CreateInterface
import org.philblandford.ui.create.viewmodel.CreateModel
import org.philblandford.ui.R
import org.philblandford.ui.common.Gap
import org.philblandford.ui.theme.PopupTheme
import org.philblandford.ui.util.CustomTimeSelector
import org.philblandford.ui.util.KeySelector
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.TimeSignatureSelector

@Composable
internal fun CreateTimeSignature(
  model: CreateModel,
  next: () -> Unit,
  cancel: () -> Unit,
  iface: CreateInterface
) {

  CreateFrame(R.string.create_score_time_signature, next, cancel) {
    SelectRow(R.string.create_score_common_time) {
      SquareButton(
        R.drawable.common,
        selected = model.newScoreDescriptor.timeSignature.type == TimeSignatureType.COMMON,
        size = 25.dp
      ) { iface.setTimeSignature { TimeSignature.common() } }
    }
    SelectRow(R.string.create_score_cut_common_time) {
      SquareButton(
        R.drawable.cut_common,
        selected = model.newScoreDescriptor.timeSignature.type == TimeSignatureType.CUT_COMMON,
        size = 25.dp
      ) { iface.setTimeSignature { TimeSignature.cutCommon() } }
    }
    SelectRow(R.string.create_score_custom_time) {
      with(model.newScoreDescriptor.timeSignature) {
        CustomTimeSelector(numerator, denominator, {
          iface.setTimeSignature { TimeSignature.custom(it, denominator) }
        }, {
          iface.setTimeSignature { TimeSignature.custom(numerator, it) }
        })
      }
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


