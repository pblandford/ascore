package com.philblandford.kscore.resource

import java.io.InputStream

data class ImageDesc(val path: String, val key: String, val width: Int, val height: Int)

interface ResourceGetter {
  fun addTextFont(bytes: ByteArray, name: String)
  fun getFont(name: String): InputStream?
  fun getImage(key: String): ImageDesc?
  fun getAllImages(): Iterable<ImageDesc>
}

