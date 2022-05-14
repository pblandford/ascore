package org.philblandford.ui.base

import android.app.Application
import android.util.Log
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.philblandford.ascore2.features.file.AutoSave
import org.philblandford.ascore2.features.startup.StartupManager
import org.philblandford.ui.BuildConfig
import timber.log.Timber

class BaseApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
          Log.e(tag, "AS2 $message", t)
        }
      })
    }

    startKoin{
      modules(Dependencies.getModules(false)).androidContext(applicationContext)
    }

    startAutosave()
  }

  private fun startAutosave() {
    val startupManager:StartupManager by inject()
    startupManager.start()
  }

}