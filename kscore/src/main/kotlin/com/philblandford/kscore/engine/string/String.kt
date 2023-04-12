package com.philblandford.kscore.engine.string

fun lString(stringId: StringId):String {
    return strings[stringId] ?: ""
}

enum class StringId  {
    HELLO_WORLD
}

// TODO - this will have native implementations for each platform

val strings:HashMap<StringId, String> = hashMapOf(
        StringId.HELLO_WORLD to "Hello, World!"
)