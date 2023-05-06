package org.philblandford.ui.main.outer.model

import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ui.R


val drawerItems = listOf(
  DrawerItemGroup(
    R.string.new_score, listOf(
      DrawerItem(R.string.full_score, LayoutID.NEW_SCORE),
      DrawerItem(R.string.quick_score, LayoutID.QUICK_SCORE),
      DrawerItem(R.string.new_score_template_short, LayoutID.NEW_SCORE_TEMPLATE),
    )
  ),
  DrawerItemGroup(
    R.string.file, listOf(
      DrawerItem(R.string.load, LayoutID.LOAD_SCORE, true),
      DrawerItem(R.string.save, LayoutID.SAVE_SCORE, true),
      DrawerItem(R.string.print, LayoutID.PRINT_SCORE, true)
    )
  ),
  DrawerItemGroup(
    R.string.export_import, listOf(
      //  DrawerItem(R.string.importFile, LayoutID.IMPORT, false),
      DrawerItem(R.string.pdf, LayoutID.EXPORT_PDF, true),
      DrawerItem(R.string.mxml, LayoutID.EXPORT_MXML, true),
      DrawerItem(R.string.midi, LayoutID.EXPORT_MIDI, true),
      DrawerItem(R.string.mp3, LayoutID.EXPORT_MP3, true),
      DrawerItem(R.string.wav, LayoutID.EXPORT_WAV, true),
      DrawerItem(R.string.save, LayoutID.EXPORT_SAVE, true)
    )
  ),
  DrawerItemGroup(
    R.string.settings, listOf(
      DrawerItem(R.string.instruments, LayoutID.MANAGE_SOUNDFONT),
      DrawerItem(R.string.settings, LayoutID.SETTINGS_LAYOUT),
      DrawerItem(R.string.about, LayoutID.ABOUT),
      DrawerItem(R.string.manual, LayoutID.MANUAL),
      DrawerItem(R.string.donate, LayoutID.DONATE)
    )
  )
)