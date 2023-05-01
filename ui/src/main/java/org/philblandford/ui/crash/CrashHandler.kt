package org.philblandford.ui.crash

import com.google.android.gms.common.util.CrashUtils
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.crosscutting.usecases.GetError
import timber.log.Timber

class CrashHandler(private val getError: GetError) {
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  fun init() {
    coroutineScope.launch {
      getError().collectLatest { error ->
        error.exception?.let {
          Firebase.crashlytics.recordException(Exception("Caught by crash handler", it))
          Firebase.crashlytics.sendUnsentReports()
          Timber.e(it)
        }
      }
    }
  }
}