package app.wetube.manage.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.annotation.StringDef
import app.wetube.item.ChannelDetail

class FavChaDB (context : Context, factory: SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context, DB_FILE, factory, DB_VER) {
    constructor(context: Context) : this(context, null)

    companion object {
        //Property
        const val DB_VER = 1
        const val DB_FILE = "channel.db"
        const val DB_TABLE = "Channel"

        //Column
        const val COL_ID = "_id"
        const val COL_NAME = "name"
        const val COL_CHAID = "chaid"
    }

    @StringDef(
        value = [
            COL_ID,
            COL_NAME,
            COL_CHAID
        ]
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Column

    //simple version
    val db get() = writableDatabase

    override fun onCreate(db: SQLiteDatabase?) {
        val table = "CREATE TABLE " + DB_TABLE +
                "( " + COL_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " + COL_CHAID + " TEXT NOT NULL " + ")"
        db!!.execSQL(table)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $DB_TABLE")
        onCreate(db)
        Log.w(this.javaClass.simpleName, "UPDATE\n\nFrom : $oldVersion \nTo : $newVersion")
    }

    fun open() {
        super.onOpen(db)
    }

    fun listAsList(): ArrayList<ChannelDetail> {
        val arrayList: ArrayList<ChannelDetail> = arrayListOf()
        open()
        val cursor = db.query(DB_TABLE, arrayOf(COL_ID, COL_NAME, COL_CHAID), null, null, null, null, null)
        if (cursor.moveToFirst()) {
            //get columns
            val titleColumn = cursor.getColumnIndex(COL_NAME);
            val chaidColumn = cursor.getColumnIndex(COL_CHAID);
            val idColumn = cursor.getColumnIndex(COL_ID);
            //add row to list
            do {
                val thisId = cursor.getInt(idColumn);
                val thisTitle = cursor.getString(titleColumn);
                val thisChaid = cursor.getString(chaidColumn);
                arrayList.add(ChannelDetail(thisTitle, thisChaid));
            } while (cursor.moveToNext())
            cursor.close()

        }
        close()

        return arrayList
    }

    fun insert(fc : ChannelDetail) {
        ContentValues().apply {
            put(COL_NAME, fc.title)
            put(COL_CHAID, fc.id)
            db.insert(DB_TABLE, null, this)
        }
    }


    fun list(): Cursor {
        open()
        val c = db.query(DB_TABLE, arrayOf(COL_ID, COL_NAME), null, null, null, null, null)
        close()
        return c
    }


    fun deleteChaByName(name: String): Boolean {
        var deleted = false
        val q = query(COL_NAME, name)
        val c = db.rawQuery(q, null)
        if (c.moveToFirst()) {
            val i = Integer.parseInt(c.getString(0))
            db.delete(DB_TABLE, "$COL_ID = ?", arrayOf(i.toString()))
            c.close()
            deleted = true
        }
        return deleted
    }

    //use for insert, update, get, delete
    fun doing(block: (FavChaDB) -> Unit) {
        open()
        block(this)
        close()
    }

    private fun query(@Column col: String, input: Any) =
        "SELECT * FROM $DB_TABLE WHERE $col = \"$input\""

    fun getById(videoId: Int): String {
        val q = query(COL_ID, videoId)
        val c = db.rawQuery(q, null)
        var s = ""
        if (c.moveToFirst()) {
            c.moveToFirst()
            s = c.getString(1)

            c.close()
        }
        return s
    }
}