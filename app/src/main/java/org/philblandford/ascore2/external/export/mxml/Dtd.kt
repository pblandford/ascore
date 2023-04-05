package com.philblandford.ascore.external.export.mxml

import com.philblandford.kscore.log.ksLoge
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import java.io.File
import java.io.FileInputStream
import javax.xml.parsers.DocumentBuilder

fun DocumentBuilder.setDtd(dtdPath: String) {
  val resolver = EntityResolver { publicId, systemId ->
    val name = systemId.split("/").last()

    val path = "${dtdPath}/$name"
    val file = File(path)
    if (file.exists()) {
      val inputStream = FileInputStream(File(path))
      InputSource(inputStream)
    } else {
      ksLoge("DTD $dtdPath not found")
      null
    }
  }
  setEntityResolver(resolver)
}