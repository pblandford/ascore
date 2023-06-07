package org.philblandford.ui.crash

import android.content.Context
import android.net.Uri
import com.google.firebase.ktx.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.philblandford.kscore.api.KScore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.philblandford.ascore2.features.crosscutting.model.ErrorDescr
import org.philblandford.ascore2.features.crosscutting.usecases.GetError
import timber.log.Timber
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.ZoneOffset

class CrashHandler(
  private val getError: GetError, private val kScore: KScore,
  private val context: Context
) {
  private val coroutineScope = CoroutineScope(Dispatchers.Default)
  private val storage = Firebase.storage

  fun init() {
    coroutineScope.launch {
      getError().collectLatest { error ->
        Timber.e(error.exception)
        try {
          uploadFile(error)
        } catch (e: Exception) {
          Timber.e("Could not upload", e)
        }
      }
    }
  }

  private fun uploadFile(errorDescr: ErrorDescr) {

    val timeStamp = LocalDateTime.now(ZoneOffset.UTC).toString()
    val storageRef = storage.reference
    val dirRef = storageRef.child("crashed_scores/$timeStamp")

    coroutineScope.launch {
      try {
        withContext(Dispatchers.IO) {
          uploadCommand(errorDescr, dirRef)
          uploadException(errorDescr, dirRef)
          uploadScore(errorDescr, dirRef)
        }
      } catch (e:Exception) {
        Timber.e(e)
      }
    }
  }

  private suspend fun uploadCommand(errorDescr: ErrorDescr, dirRef: StorageReference) {
    errorDescr.command?.let { command ->
      val tmpFile = File(context.cacheDir, "cmd.txt")
      FileUtils.write(
        tmpFile,
        "${BuildConfig.VERSION_NAME}\n${errorDescr.command.toString()}",
        Charset.defaultCharset()
      )
      val fileRef = dirRef.child("cmd.txt")
      fileRef.putFile(Uri.fromFile(tmpFile)).await()
    }
  }

  private suspend fun uploadException(errorDescr: ErrorDescr, dirRef: StorageReference) {
    errorDescr.exception?.let { exception ->
      withContext(Dispatchers.IO) {
        val tmpFile = File(context.cacheDir, "exception.txt")
        FileUtils.write(tmpFile, exception.stackTraceToString(), Charset.defaultCharset())
        val fileRef = dirRef.child("exception.txt")
        fileRef.putFile(Uri.fromFile(tmpFile)).await()
      }
    }
  }

  private suspend fun uploadScore(errorDescr: ErrorDescr, dirRef: StorageReference) {
    kScore.getScoreAsBytes()?.let { bytes ->
      val fileRef = dirRef.child("score.asc")
      fileRef.putBytes(bytes).await()
    }
  }
}