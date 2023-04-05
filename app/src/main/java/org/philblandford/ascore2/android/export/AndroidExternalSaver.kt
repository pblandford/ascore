package org.philblandford.ascore2.android.export

import android.content.Context
import android.net.Uri
import com.philblandford.ascore.external.interfaces.ExternalSaver
import java.io.FileOutputStream

class AndroidExternalSaver(private val context: Context) : ExternalSaver {

  private var uri: Uri? = null

  override fun setUri(uri: Uri) {
    this.uri = uri
  }

  override fun save(bytes: ByteArray) {
    uri?.let {
      context.contentResolver.openFileDescriptor(it, "w")?.use {
        FileOutputStream(it.fileDescriptor).use { fos ->
          fos.write(bytes)
        }
      }
    }
  }
}