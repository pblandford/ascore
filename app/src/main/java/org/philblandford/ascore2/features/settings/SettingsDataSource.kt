package org.philblandford.ascore2.features.settings

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson

class SettingsDataSource(private val context: Context) {

  private val prefName = "Settings"
  private val preferences = context.getSharedPreferences(prefName, MODE_PRIVATE)
  private val gson = Gson()

  fun <T> storeObject(key: String, obj: T) {
    val json = gson.toJson(obj)
    preferences.edit().putString(key, json).commit()
  }

  fun <T> getObject(key: String, clazz: Class<T>): T? {
    val json = preferences.getString(key, "")
    return if (!json.isNullOrEmpty()) {
      gson.fromJson<T>(json, clazz)
    } else null
  }
}