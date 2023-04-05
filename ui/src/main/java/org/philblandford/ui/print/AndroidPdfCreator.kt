package org.philblandford.ui.print

import ResourceManager
import android.content.Context
import android.graphics.pdf.PdfDocument
import com.philblandford.ascore.external.interfaces.PdfCreator
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.representation.scoreToRepresentation
import com.philblandford.kscore.engine.types.ScoreQuery
import com.philblandford.kscoreandroid.drawingandroid.AndroidDrawableGetter
import java.io.ByteArrayOutputStream

class AndroidPdfCreator(
  private val context: Context,
  private val resourceManager: ResourceManager
) : PdfCreator {

  private val PS_WIDTH_POINTS = 595
  private val PS_HEIGHT_POINTS = 842

  override fun createPdfBytes(scoreQuery: ScoreQuery): ByteArray? {

    return createPdfDocument(scoreQuery)?.let { doc ->
      val os = ByteArrayOutputStream()
      doc.writeTo(os)
      os.close()
      doc.close()
      os.toByteArray()
    }

  }

  override fun createPdfDocument(scoreQuery: ScoreQuery): PdfDocument? {
    val doc = PdfDocument()
    val dg = AndroidDrawableGetter(context, resourceManager)
    val df = DrawableFactory(dg)

    val res = scoreToRepresentation(
      scoreQuery, df
    )?.let { rep ->
      rep.pages.withIndex().forEach { iv ->
        val num = iv.index + 1
        val area = iv.value.base
        val info = PdfDocument.PageInfo.Builder(PS_WIDTH_POINTS, PS_HEIGHT_POINTS, num).create()
        val page = doc.startPage(info)
        val canvas = page.canvas
        canvas.scale(PS_WIDTH_POINTS.toFloat()/area.width, PS_HEIGHT_POINTS.toFloat()/area.height)

        dg.prepare(canvas)
        dg.drawTree(area, 0, 0, true)
        doc.finishPage(page)
      }
      doc
    }

    return res
  }

}