package app.wetube.core

fun String.changeEnHt():String{
    return replace("&amp;", "&").replace("&#39;", "'").replace("&apos;", "'").replace("&quot;", "\"")
}