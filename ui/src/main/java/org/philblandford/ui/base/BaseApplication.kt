package org.philblandford.ui.base

import android.app.Application
import android.util.Log
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.philblandford.ascore2.features.file.AutoSave
import org.philblandford.ui.BuildConfig
import org.philblandford.ui.crash.CrashHandler
import timber.log.Timber


class BaseApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
          Log.println(priority, tag, message)
          t?.let {
            Log.e(tag, "", it)
          }
        }
      })
    }


    startKoin{
      modules(Dependencies.getModules(false)).androidContext(applicationContext)
    }
    startCrashHandler()
    startAutosave()
  }

  private fun startAutosave() {
    val autoSave:AutoSave by inject()
    autoSave.startAutoSave()
  }

  private fun startCrashHandler() {
    val crashHandler:CrashHandler by inject()
    crashHandler.init()
  }

}