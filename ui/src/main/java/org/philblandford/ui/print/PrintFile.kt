package org.philblandford.ui.export

import androidx.compose.runtime.Composable
import com.philblandford.kscore.api.KScore
import org.koin.compose.koinInject
import org.philblandford.ui.LocalActivity
import org.philblandford.ui.print.AndroidPrinter

@Composable
fun PrintFile() {
  val printer: AndroidPrinter = koinInject()
  val kScore:KScore = koinInject()

  LocalActivity.current?.let { activity ->
    kScore.getScore()?.let { score ->
      printer.registerActivity(activity)
      printer.printScore(score)
    }
  }
}