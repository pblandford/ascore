import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.Accidental
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.items.transposeby.viewmodel.TransposeInterface
import org.philblandford.ui.insert.items.transposeby.viewmodel.TransposeViewModel
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.input.compose.selectors.AccidentalSpinner
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.NumberSelector
import org.philblandford.ui.util.SquareButton

@Composable
fun TransposeBy() {
  InsertVMView<InsertModel, TransposeInterface, TransposeViewModel>() { _, item, iface ->
    TransposeByInternal(item, iface)
  }
}

@Composable
private fun TransposeByInternal(insertItem: InsertItem, iface: TransposeInterface) {
  Box(
    Modifier
      .fillMaxWidth()
      .padding(10.dp)
  ) {
    Row(Modifier.align(Alignment.CenterStart)) {
      NumberSelector(min = -7, max = 7, num = insertItem.getParam<Int>(EventParam.AMOUNT) ?: 0,
        setNum = { iface.setParam(EventParam.AMOUNT, it) })
      Gap(block())
      AccidentalSpinner(
        accidentals = listOf(Accidental.SHARP, Accidental.FLAT),
        selectedAccidental = insertItem.getParam(EventParam.ACCIDENTAL) ?: Accidental.SHARP,
        setAccidental = { iface.setParam(EventParam.ACCIDENTAL, it) })
      Gap(0.5f)
      SquareButton(R.drawable.tick, tag = "GoButton") {
        iface.go()
      }
    }
  }
}
