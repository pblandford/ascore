package org.philblandford.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup
import org.philblandford.ui.create.viewmodel.CreateInterface

@Composable
fun InstrumentList(
  modifier: Modifier, instrumentGroups: List<InstrumentGroup>,
  selected:Instrument? = null,
  select:(Instrument)->Unit
) {

  val expandedMap = remember { mutableStateOf((instrumentGroups.indices).associateWith { false }) }

  LazyColumn(
    modifier
      .padding(5.dp)
      .border(1.dp, MaterialTheme.colors.primary)
  ) {
    items(instrumentGroups.withIndex().toList()) { (idx, group) ->
      Column(Modifier.padding(horizontal = 5.dp)) {
        Row(Modifier.clickable {
          expandedMap.value = expandedMap.value + (idx to !(expandedMap.value[idx] ?: false))
        }, verticalAlignment = Alignment.CenterVertically) {
          Text("+", fontSize = 20.sp)
          Gap(5.dp)
          Text(group.name, fontSize = 17.sp)
        }
        if (expandedMap.value[idx] == true) {
          group.instruments.forEach {
            val background = if (selected == it) MaterialTheme.colors.secondary else Color.Transparent
            Text(
              it.name,
              Modifier
                .offset(20.dp)
                .padding(5.dp).background(background)
                .clickable {
                  select(it)
                }, fontSize = 16.sp
            )
          }
        }
      }
    }
  }
}
