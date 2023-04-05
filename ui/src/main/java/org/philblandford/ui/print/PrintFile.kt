package org.philblandford.ui.export

import androidx.compose.runtime.Composable
import com.philblandford.kscore.api.KScore
import org.koin.androidx.compose.inject
import org.philblandford.ui.LocalActivity
import org.philblandford.ui.print.AndroidPdfCreator
import org.philblandford.ui.print.AndroidPrinter

@Composable
fun PrintFile() {
  val printer: AndroidPrinter by inject()
  val kScore:KScore by inject()

  LocalActivity.current?.let { activity ->
    kScore.getScore()?.let { score ->
      printer.registerActivity(activity)
      printer.printScore(score)
    }
  }
}