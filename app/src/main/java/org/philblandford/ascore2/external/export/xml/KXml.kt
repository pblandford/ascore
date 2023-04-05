package com.philblandford.ascore.external.export.xml

import com.philblandford.ascore.external.export.mxml.setDtd
import com.philblandford.kscore.log.ksLogt
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

@Target(AnnotationTarget.PROPERTY)
internal annotation class Attribute

@Target(AnnotationTarget.PROPERTY)
internal annotation class Child

@Target(AnnotationTarget.PROPERTY)
internal annotation class Text

@Target(AnnotationTarget.PROPERTY)
internal annotation class Order(val n: Int)


@Target(AnnotationTarget.PROPERTY)
internal annotation class Y()

internal abstract class KxmlBase {

  protected open fun getPrefix(): String = ""

  open fun String.convertName(): String {
    return if (length > 0) {
      var copy = this[0].toLowerCase().toString().plus(drop(1))
      copy = copy.replace(Regex("[A-Z]")) { mr -> "-" + mr.value.toLowerCase(Locale.getDefault()) }
      copy = copy.removePrefix(getPrefix())
      copy
    } else {
      this
    }
  }
}

internal fun readXml(xml: String, dtdPath:String): Document? {
  ksLogt("readXml $dtdPath")
  val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
  documentBuilder.setDtd(dtdPath)
  val document = documentBuilder.parse(InputSource(StringReader(xml)))
  ksLogt("readXml done $document")
  return document
}