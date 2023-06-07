package org.philblandford.ascore2.external.export.xml

import kotlin.reflect.KCallable
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation


internal fun KxmlBase.name():String? {
  return this::class.simpleName?.convertName()
}

internal fun KxmlBase.attributes():List<KCallable<*>> {
  return this::class.members .filter { it.annotations.any { annotation -> annotation is Attribute } }
}

internal fun KxmlBase.children():List<KxmlBase> {
  val childProps = this::class.members.filter { it.annotations.any { annotation -> annotation is Child } }.sortedBy {
    it.annotations.find{ ann -> ann is Order }?.let { (it as Order).n } ?: 0
  }
  return childProps.flatMap {
    val value = (it as KProperty1<KxmlBase, *>).get(this)
    if (value is Iterable<*>) {
      value.map { it as KxmlBase? }
    } else {
      listOf(value as KxmlBase?)
    }
  }.filterNotNull()
}

class KxmlDifferenceException(name:String?, msg:String) : Exception("$name: $msg") {}

internal fun KxmlBase.compare(other: KxmlBase) {

  if (name() != other.name()) {
    throw KxmlDifferenceException(
      name(),
      "${name()} != ${other.name()}"
    )
  }
  if (text() != other.text()) {
    throw KxmlDifferenceException(
      name(),
      "${text()} != ${other.text()}"
    )
  }
  if (attributes() != other.attributes()) {
    throw KxmlDifferenceException(
      name(),
      "${attributes()} != ${other.attributes()}"
    )
  }
  if (children().size != other.children().size) {
    throw KxmlDifferenceException(
      name(),
      "${children()} != ${other.children()}"
    )
  }
  children().zip(other.children()) { a, b ->
    a.compare(b)
  }

}

internal fun KxmlBase.text():Any? {
  val prop = this::class.members.find {  it.findAnnotation<Text>() != null } as KProperty1<KxmlBase, Any>?
  return prop?.get(this)
}

internal fun KxmlBase.write(stringBuilder: StringBuilder, numTabs: Int = 0): String {

  val name = name()
  val text = text()
  val children = children()

  repeat(numTabs) { stringBuilder.append("\t") }
  stringBuilder.append("<$name")
  attributes().forEach {
    val cast = it as KProperty1<KxmlBase, Any>
    val value = cast.get(this)
    if (value != null) {
      stringBuilder.append(" ${it.name.convertName()}=\"$value\"")
    }
  }
  if (children.count() == 0 && text == null) {
    stringBuilder.append("/>\n")
  } else {
    if (text != null) {
      stringBuilder.append(">$text</$name>\n")
    } else {
      stringBuilder.append(">\n")
      children.forEach { child ->
        child.write(stringBuilder, numTabs + 1)
      }
      repeat(numTabs) { stringBuilder.append("\t") }
      stringBuilder.append("</$name>\n")
    }
  }
  return stringBuilder.toString()

}