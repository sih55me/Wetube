package app.wetube.manage.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.annotation.StringDef

class HistoryDB (context : Context, factory: SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context, DB_FILE, factory, DB_VER) {
    constructor(context : Context):this(context, null)
    companion object{
        //Property
        const val DB_VER = 1
        const val DB_FILE = "history.db"
        const val DB_TABLE = "History"

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
                COL_NAME + " TEXT NOT NULL " + ")"
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

    fun listAsList() : ArrayList<String> {
        val arrayList: ArrayList<String> = arrayListOf()
        open()
        val cursor = db.query(DB_TABLE, arrayOf(COL_ID, COL_NAME), null, null, null, null, null)
        if (cursor.moveToFirst()) {
            //get columns
            val titleColumn = cursor.getColumnIndex(COL_NAME);
            val idColumn = cursor.getColumnIndex(COL_ID);
            //add row to list
            do {
                val thisId = cursor.getInt(idColumn);
                val thisTitle = cursor.getString(titleColumn);
                arrayList.add(thisTitle);
            } while (cursor.moveToNext())
            cursor.close()

        }
        close()

        return arrayList
    }

    fun insert(name: String){
        ContentValues().apply {
            put(COL_NAME, name)
            db.insert(DB_TABLE, null, this)
        }
    }


    fun list(): Cursor {
        open()
        val c = db.query(DB_TABLE, arrayOf(COL_ID, COL_NAME), null, null, null, null, null)
        close()
        return c
    }



    fun deleteByName(name: String):Boolean{
        var deleted = false
        val q = query(COL_NAME,name)
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
    fun doing(block:(HistoryDB)->Unit){
        open()
        block(this)
        close()
    }

    private fun query(@Column col: String, input: Any) = "SELECT * FROM $DB_TABLE WHERE $col = \"$input\""
    fun getById(videoId: Int): String {
        val q = query(COL_ID, videoId)
        val c = db.rawQuery(q, null)
        var s = ""
        if(c.moveToFirst()){
            c.moveToFirst()
            s = c.getString(1)

            c.close()
        }
        return s
    }


}