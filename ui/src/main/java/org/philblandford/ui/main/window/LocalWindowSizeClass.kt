package org.philblandford.ui.main.window

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
val LocalWindowSizeClass = compositionLocalOf { WindowSizeClass.calculateFromSize(DpSize.Zero) }
fun WindowSizeClass.notCompact() = this.widthSizeClass != WindowWidthSizeClass.Compact
fun WindowSizeClass.expanded() = this.widthSizeClass == WindowWidthSizeClass.Expanded
fun WindowSizeClass.compact() = this.widthSizeClass == WindowWidthSizeClass.Compact
fun WindowSizeClass.medium() = this.widthSizeClass == WindowWidthSizeClass.Medium

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
val tabletWindowSize = WindowSizeClass.calculateFromSize(DpSize(700.dp, 1200.dp))

val LocalBlockSize = compositionLocalOf { 20.dp }

