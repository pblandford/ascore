package org.philblandford.ascore2.android.export

import android.app.Activity
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.*
import com.philblandford.ascore.external.interfaces.PdfCreator
import com.philblandford.ascore.external.interfaces.PrinterIf
import com.philblandford.kscore.engine.types.ScoreQuery
import org.philblandford.ascore2.R
import java.io.FileOutputStream
import java.io.IOException

class AndroidPrinter(private val pdfCreator: PdfCreator) : PrinterIf  {

  private lateinit var activity:Activity

  fun registerActivity(activity: Activity) {
    this.activity = activity
  }

  override fun printScore(scoreQuery: ScoreQuery) {
    val printManager = activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val jobName = "${activity.getString(R.string.app_name)} Document"
    printManager.print(jobName, AndroidPrintAdapter(scoreQuery), null)
  }

  private inner class AndroidPrintAdapter(val scoreQuery: ScoreQuery) : PrintDocumentAdapter() {

    lateinit var pdfDocument: PdfDocument

    override fun onLayout(
      oldAttributes: PrintAttributes?,
      newAttributes: PrintAttributes?,
      cancellationSignal: CancellationSignal?,
      callback: LayoutResultCallback?,
      extras: Bundle?
    ) {
      if (cancellationSignal?.isCanceled == true) {
        callback?.onLayoutCancelled()
      } else {
        pdfCreator.createPdfDocument(scoreQuery)?.let {
          pdfDocument = it
          PrintDocumentInfo.Builder("print_output.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(pdfDocument.pages.size)
            .build()
            .also { info ->
              callback?.onLayoutFinished(info, true)
            }
        }
      }
    }

    override fun onWrite(
      pages: Array<out PageRange>?,
      destination: ParcelFileDescriptor?,
      cancellationSignal: CancellationSignal?,
      callback: WriteResultCallback?
    ) {
      if (cancellationSignal?.isCanceled == true) {
        callback?.onWriteCancelled()
      } else {
        try {
          destination?.fileDescriptor?.also { fd ->
            pdfDocument.writeTo(FileOutputStream(fd))
          }
          val pageRanges = arrayOf(PageRange.ALL_PAGES)
          callback?.onWriteFinished(pageRanges)
        } catch (e: IOException) {
          callback?.onWriteFailed(e.toString())
          return
        } finally {
          pdfDocument.close()
        }
      }
    }
  }
}