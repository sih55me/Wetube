package app.wetube.core

import android.graphics.Bitmap
import android.os.AsyncTask
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ThumbDown(val onGetIt : (Bitmap) -> Unit,): AsyncTask<String, Bitmap, Int>() {
    private var mOnDone: ((Int) -> Unit)? = null
    override fun doInBackground(vararg params: String?): Int? {
        var count = 0
        for (i in 0..params.size-1) {
            val unduh = request(params[i]!!)
            if(unduh != null){
                count=+1
                try{
                    Thread.sleep(100)
                }catch (e: Exception){
                    e.printStackTrace()
                }
                publishProgress(unduh)
            }
        }
        return count
    }


    public fun onDone(listen : (Int) -> Unit): ThumbDown{
        mOnDone = listen
        return this
    }

    override fun onProgressUpdate(vararg values: Bitmap?) {
        onGetIt(
            requireNotNull(values.first())
        )

    }

    override fun onPostExecute(result: Int?) {
        mOnDone?.invoke(result ?: 0)
    }

    companion object{
        fun request(url: String): Bitmap?{
            var bitmap: Bitmap? = null
            var pust : InputStream? = null
            try {
                pust = letConnect(url)
                bitmap = android.graphics.BitmapFactory.decodeStream(pust)
                pust.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bitmap
        }

        @Throws(IOException::class, IllegalStateException::class)
        private fun letConnect(url: String): InputStream{

            val link = URL(url)
            val kon = link.openConnection()
            if(kon !is HttpURLConnection) throw IllegalStateException("URL is not Http")
            try{
                kon.apply {
                    allowUserInteraction = false
                    instanceFollowRedirects = true
                    requestMethod = "GET"
                    connect()
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        return kon.inputStream
                    }
                }

            }catch (e: Exception){
                throw IOException(e)
            }
            throw NullPointerException("nothin yet")
        }
    }



}