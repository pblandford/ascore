package com.philblandford.kscore.engine.util


fun black(alpha:Int = 0xff) = 0x000000 or (alpha shl 24)
fun white(alpha:Int = 0xff) = 0xffffff or (alpha shl 24)
fun blue(alpha: Int = 0xff) = 0x0000ff or (alpha shl 24)
fun lightGrey(alpha: Int = 0xff) = 0xc0c0c0 or (alpha shl 24)
fun selectionColor() = lightGrey(0x80)