package org.philblandford.ui.crash

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
import org.philblandford.ascore2.features.crosscutting.model.ErrorDescr
import org.philblandford.ascore2.features.crosscutting.usecases.GetError
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAccessor

class CrashHandler(private val getError: GetError, private val kScore: KScore){
  private val coroutineScope = CoroutineScope(Dispatchers.Default)
  private val storage = Firebase.storage

  fun init() {
    coroutineScope.launch {
      getError().collectLatest { error ->
        if (BuildConfig.DEBUG) {
          Timber.e("Crash handler got $error")
        } else {
          error.exception?.let {
            Firebase.crashlytics.recordException(Exception("Caught by crash handler", it))
            Firebase.crashlytics.setCustomKey("command", error.command?.toString() ?: "")
            Firebase.crashlytics.sendUnsentReports()
            Timber.e(it)
            try {
              uploadFile()
            } catch (e: Exception) {
              Timber.e("Could not upload", e)
            }
          }
        }
      }
    }
  }

  private fun uploadFile() {
    val timeStamp = LocalDateTime.now(ZoneOffset.UTC).toString()
    val storageRef = storage.reference
    val fileRef = storageRef.child("crashed_scores/$timeStamp")
    kScore.getScoreAsBytes()?.let { bytes ->
      coroutineScope.launch(Dispatchers.IO) {
        fileRef.putBytes(bytes).await()
      }
    }
  }
}