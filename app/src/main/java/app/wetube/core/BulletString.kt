package app.wetube.core

object BulletString {
    fun bullet(strl: List<String>):ArrayList<String>  {
        val list = arrayListOf<String>()
        var order = 0
        for (i in strl) {
            order += 1
            list.add("$order. $i")
        }
        return list
    }

    fun bulletString(strl : List<String>):String = bullet(strl).joinToString(separator = "\n")

}