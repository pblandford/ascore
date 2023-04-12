package org.philblandford.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun block(num:Int):Dp = block(num.toDouble())

@Composable
fun block(num:Float):Dp = block(num.toDouble())

@Composable
fun block(num:Double = 1.0):Dp {
  val size = LocalConfiguration.current.screenWidthDp / 10
  return (num * size).dp
}

@Composable
fun Gap(blocks:Float) = Gap(blocks.toDouble())

@Composable
fun Gap(blocks:Double) {
  Box(Modifier.size(block(blocks)))
}
