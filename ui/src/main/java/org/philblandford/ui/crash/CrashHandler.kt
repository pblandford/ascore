package org.philblandford.ui.crash

import android.content.Context
import android.net.Uri
import com.google.android.gms.common.util.CrashUtils
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.saveload.Saver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apache.commons.io.FileUtils
import org.philblandford.ascore2.features.crosscutting.model.ErrorDescr
import org.philblandford.ascore2.features.crosscutting.usecases.GetError
import timber.log.Timber
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAccessor

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

    errorDescr.command?.let {
      val tmpFile = File(context.cacheDir, "cmd.txt")
      FileUtils.write(tmpFile, "${BuildConfig.VERSION_NAME}\n${errorDescr.command.toString()}", Charset.defaultCharset())
      val fileRef = dirRef.child("cmd.txt")
      coroutineScope.launch(Dispatchers.IO) {
        fileRef.putFile(Uri.fromFile(tmpFile)).await()
      }
    }
    errorDescr.exception?.let { exception ->
      val tmpFile = File(context.cacheDir, "exception.txt")
      FileUtils.write(tmpFile, exception.stackTraceToString(), Charset.defaultCharset())
      val fileRef = dirRef.child("exception.txt")
      coroutineScope.launch(Dispatchers.IO) {
        fileRef.putFile(Uri.fromFile(tmpFile)).await()
      }
    }

    kScore.getScoreAsBytes()?.let { bytes ->
      coroutineScope.launch(Dispatchers.IO) {
        val fileRef = dirRef.child("score.asc")
        fileRef.putBytes(bytes).await()
      }
    }
  }
}