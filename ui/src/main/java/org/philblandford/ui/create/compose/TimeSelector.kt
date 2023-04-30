package org.philblandford.ui.create.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.TimeSignatureType
import org.philblandford.ui.R
import org.philblandford.ui.util.ButtonState.Companion.selected
import org.philblandford.ui.util.CustomTimeSelector
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.SquareButton

@Composable
fun TimeSelector(modifier: Modifier, timeSignature: TimeSignature, column: Boolean, set:(TimeSignature)->Unit) {

  if (column) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
      TimeSelectorInner(timeSignature, set)
    }
  } else {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
      TimeSelectorInner(timeSignature, set)
    }
  }
}

@Composable
private fun TimeSelectorInner(timeSignature: TimeSignature ,set: (TimeSignature) -> Unit) {
  with(timeSignature) {

    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      SquareButton(
        R.drawable.common, Modifier.padding(10.dp),
        selected(type == TimeSignatureType.COMMON),
        size = 45.dp
      ) {
        set(
          if (type == TimeSignatureType.COMMON) TimeSignature.custom(
            4,
            4
          ) else TimeSignature.common()
        )
      }
      Gap(0.2f)
      SquareButton(
        R.drawable.cut_common,
        state = selected(type == TimeSignatureType.CUT_COMMON),
        size = 55.dp
      ) {
        set(
          if (type == TimeSignatureType.CUT_COMMON) TimeSignature.custom(
            2,
            2
          ) else TimeSignature.cutCommon()
        )
      }
    }
    Gap(0.5f)
    Box(
      Modifier
        .border(1.dp, MaterialTheme.colorScheme.onSurface)
        .padding(5.dp)) {
      CustomTimeSelector(timeSignature, set)
    }
    Gap(0.5f)
  }
}
