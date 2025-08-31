package app.wetube.manage.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.annotation.StringDef
import app.wetube.item.Video

class VidDB(context : Context, factory:SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context, DB_FILE, factory, DB_VER) {
    constructor(context : Context):this(context, null)
    companion object{
        //Property
        const val DB_VER = 1
        const val DB_FILE = "videos.db"
        const val DB_TABLE = "Video"

        //Column
        const val COL_ID = "_id"
        const val COL_NAME = "name"
        const val COL_VIDEOID = "videoid"
    }

    @StringDef(
        value = [
            COL_ID,
            COL_NAME,
            COL_VIDEOID
        ]
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Column

    //simple version
    val db get() = writableDatabase

    override fun onCreate(db: SQLiteDatabase?) {
        val table = "CREATE TABLE "+ DB_TABLE +
                "( " + COL_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                COL_NAME + " TEXT NOT NULL, " +
                COL_VIDEOID + " TEXT NOT NULL" + ")"
        db!!.execSQL(table)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $DB_TABLE")
        onCreate(db)
        Log.w(this.javaClass.simpleName, "UPDATE\n\nFrom : $oldVersion \nTo : $newVersion")
    }

    fun open(){
        super.onOpen(db)
    }

    fun listAsList() : ArrayList<Video> {
        val arrayList: ArrayList<Video> = arrayListOf()
        open()
        val cursor = db.query(DB_TABLE, arrayOf(COL_ID, COL_NAME, COL_VIDEOID), null, null, null, null, null)
        if (cursor.moveToFirst()) {
            //get columns
            val titleColumn = cursor.getColumnIndex(COL_NAME);
            val idColumn = cursor.getColumnIndex(COL_ID);
            val videoIDColumn = cursor.getColumnIndex(COL_VIDEOID)
            //add row to list
            do {
                val thisId = cursor.getInt(idColumn);
                val thisTitle = cursor.getString(titleColumn);
                val thisVid = cursor.getString(videoIDColumn);
                arrayList.add(Video(title = thisTitle, videoId = thisVid, id = thisId));
            } while (cursor.moveToNext())
            cursor.close()

        }
        close()

        return arrayList
    }

    fun insert(video: Video) = insert(video.title, video.videoId)
    fun insert(name:String, videoId : String){
        ContentValues().apply {
            put(COL_NAME, name)
            put(COL_VIDEOID, videoId)
            db.insert(DB_TABLE, null, this)
        }
    }

    fun getByVideoId( input : String): Video {
        val q = query(COL_VIDEOID, input)
        val c = db.rawQuery(q, null)
        val video = Video()
        if(c.moveToFirst()){
            c.moveToFirst()
            video.apply {
                id = c.getInt(0)
                title = c.getString(1)
                videoId = c.getString(2)
            }
            c.close()
        }
        return video
    }

    fun getByName( input : String): Video {
        val q = query(COL_NAME, input)
        val c = db.rawQuery(q, null)
        val video = Video()
        if(c.moveToFirst()){
            c.moveToFirst()
            video.apply {
                id = c.getInt(0)
                title = c.getString(1)
                videoId = c.getString(2)
            }
            c.close()
        }
        return video
    }

    fun list():Cursor {
        open()
        val c = db.query(DB_TABLE, arrayOf(COL_ID, COL_NAME, COL_VIDEOID), null, null, null, null, null)
        close()
        return c
    }

    fun updateVideo(video: Video): Boolean {
        var updated: Boolean
        ContentValues().apply {
            put(COL_NAME, video.title)
            put(COL_VIDEOID, video.videoId)
            updated = db.update(DB_TABLE, this, "$COL_ID = ${video.id}", null) > 0
        }

        return updated
    }

    fun deleteTripleByVideoId(videoIds: Array< out String>){
        val whereClause = "$COL_VIDEOID IN (" + videoIds.joinToString(",") + ")"
        db.delete(DB_TABLE, whereClause,null)
    }

    fun deleteVideoByVideoId(videoId: String):Boolean{
        var deleted = false
        val q = query(COL_VIDEOID,videoId)
        val c = db.rawQuery(q, null)
        if(c.moveToFirst()) {
            val i = Integer.parseInt(c.getString(0))
            db.delete(DB_TABLE, "$COL_ID = ?", arrayOf(i.toString()))
            c.close()
            deleted = true
        }
        return deleted
    }

    //use for insert, update, get, delete
    fun doing(block:(VidDB)->Unit){
        open()
        block(this)
        close()
    }

    private fun query(@Column col: String, input: Any) = "SELECT * FROM $DB_TABLE WHERE $col = \"$input\""
    fun getById(videoId: Int): Video {
        val q = query(COL_ID, videoId)
        val c = db.rawQuery(q, null)
        val video = Video()
        if(c.moveToFirst()){
            c.moveToFirst()
            video.apply {
                id = c.getInt(0)
                title = c.getString(1)
                this.videoId = c.getString(2)
            }
            c.close()
        }
        return video
    }


}