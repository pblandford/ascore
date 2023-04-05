package org.philblandford.ascore2.android.export

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File

class ExportContentProvider : ContentProvider() {
  override fun update(uri: Uri, values:ContentValues?, selection: String?, selectionArgs:Array<String>?): Int = 0

  override fun insert(uri: Uri, values: ContentValues?): Uri? = null

  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

  override fun onCreate(): Boolean = true

  override fun query(uri: Uri, projection: Array<String>?,
                     selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
    return null
  }

  override fun getType(uri: Uri): String = ""

  override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
    val fileName = uri.path?.split("/")?.last() ?: ""
    val cacheDir = context?.cacheDir
    val file = File(cacheDir, fileName)
    return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
  }
}