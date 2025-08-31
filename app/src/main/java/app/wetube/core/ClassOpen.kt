package app.wetube.core

open class ClassOpen(cname: String, vararg parametClass: Class<*>){
    open val cz = Class.forName(cname)

    open val result :Any?= null

}