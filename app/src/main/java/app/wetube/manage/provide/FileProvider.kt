package app.wetube.manage.provide

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import android.webkit.MimeTypeMap
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class FileProvider : ContentProvider() {
    private var mStrategy: PathStrategy? = null

    override fun onCreate(): Boolean {
        return true
    }

    override fun attachInfo(context: Context, info: ProviderInfo) {
        super.attachInfo(context, info)
        if (info.exported) {
            throw SecurityException("Provider must not be exported")
        } else if (!info.grantUriPermissions) {
            throw SecurityException("Provider must grant uri permissions")
        } else {
            this.mStrategy = getPathStrategy(context, info.authority)
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String?>?,
        sortOrder: String?
    ): Cursor {
        var projection = projection
        val file = this.mStrategy!!.getFileForUri(uri)
        if (projection == null) {
            projection = COLUMNS
        }

        var cols = arrayOfNulls<String>(projection.size)
        var values = arrayOfNulls<Any>(projection.size)
        var i = 0

        for (col in projection) {
            if ("_display_name" == col) {
                cols[i] = "_display_name"
                values[i++] = file.getName()
            } else if ("_size" == col) {
                cols[i] = "_size"
                values[i++] = file.length()
            }
        }

        cols = copyOf(cols, i)
        values = copyOf(values, i)
        val cursor = MatrixCursor(cols, 1)
        cursor.addRow(values)
        return cursor
    }

    override fun getType(uri: Uri): String? {
        val file = this.mStrategy!!.getFileForUri(uri)
        val lastDot = file.getName().lastIndexOf(46.toChar())
        if (lastDot >= 0) {
            val extension = file.getName().substring(lastDot + 1)
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (mime != null) {
                return mime
            }
        }

        return "application/octet-stream"
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("No external inserts")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String?>?
    ): Int {
        throw UnsupportedOperationException("No external updates")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String?>?): Int {
        val file = this.mStrategy!!.getFileForUri(uri)
        return if (file.delete()) 1 else 0
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val file = this.mStrategy!!.getFileForUri(uri)
        val fileMode = modeToMode(mode)
        return ParcelFileDescriptor.open(file, fileMode)
    }

    internal class SimplePathStrategy(private val mAuthority: String?) : PathStrategy {
        private val mRoots: HashMap<String?, File?> = HashMap<String?, File?>()

        fun addRoot(name: String?, root: File) {
            var root = root
            require(!TextUtils.isEmpty(name)) { "Name must not be empty" }
            try {
                root = root.getCanonicalFile()
            } catch (e: IOException) {
                throw IllegalArgumentException("Failed to resolve canonical path for " + root, e)
            }

            this.mRoots.put(name, root)
        }

        override fun getUriForFile(file: File): Uri? {
            var path: String?
            try {
                path = file.getCanonicalPath()
            } catch (var7: IOException) {
                throw IllegalArgumentException("Failed to resolve canonical path for " + file)
            }

            var mostSpecific: MutableMap.MutableEntry<String?, File?>? = null

            for (root in this.mRoots.entries) {
                val rootPath = (root.value as File).getPath()
                if (path.startsWith(rootPath) && (mostSpecific == null || rootPath.length > (mostSpecific.value as File).getPath().length)) {
                    mostSpecific = root
                }
            }

            requireNotNull(mostSpecific != null) { "Failed to find configured root that contains " + path }
            val rootPath = (mostSpecific!!.value as File).getPath()
            if (rootPath.endsWith("/")) {
                path = path.substring(rootPath.length)
            } else {
                path = path.substring(rootPath.length + 1)
            }

            path = Uri.encode(mostSpecific.key) + '/' + Uri.encode(path, "/")
            return (Uri.Builder()).scheme("content").authority(this.mAuthority).encodedPath(path)
                .build()
        }

        override fun getFileForUri(uri: Uri): File {
            var path = uri.getEncodedPath()
            val splitIndex = path!!.indexOf(47.toChar(), 1)
            val tag = Uri.decode(path.substring(1, splitIndex))
            path = Uri.decode(path.substring(splitIndex + 1))
            val root = this.mRoots.get(tag) as File
            requireNotNull(root != null) { "Unable to find configured root for " + uri }
            var file = File(root, path)

            try {
                file = file.getCanonicalFile()
            } catch (var8: IOException) {
                throw IllegalArgumentException("Failed to resolve canonical path for " + file)
            }

            if (!file.getPath().startsWith(root.getPath())) {
                throw SecurityException("Resolved path jumped beyond configured root")
            } else {
                return file
            }
        }
    }

    internal interface PathStrategy {
        fun getUriForFile(file: File): Uri?

        fun getFileForUri(uri: Uri): File
    }

    companion object {
        private val COLUMNS: Array<String?> = arrayOf<String?>("_display_name", "_size")
        private const val META_DATA_FILE_PROVIDER_PATHS = "com.wiwolf.LOVETOSHARE"
        private const val TAG_ROOT_PATH = "root-path"
        private const val TAG_FILES_PATH = "files-path"
        private const val TAG_CACHE_PATH = "cache-path"
        private const val TAG_EXTERNAL = "external-path"
        private const val TAG_EXTERNAL_FILES = "external-files-path"
        private const val TAG_EXTERNAL_CACHE = "external-cache-path"
        private const val TAG_EXTERNAL_MEDIA = "external-media-path"
        private const val ATTR_NAME = "name"
        private const val ATTR_PATH = "path"
        private val DEVICE_ROOT = File("/")
        private val sCache: HashMap<String?, PathStrategy?> = HashMap<String?, PathStrategy?>()
        fun getUriForFile(context: Context, authority: String, file: File): Uri? {
            val strategy = getPathStrategy(context, authority)
            return strategy.getUriForFile(file)
        }

        private fun getPathStrategy(context: Context, authority: String): PathStrategy {
            synchronized(sCache) {
                var strat = sCache.get(authority)
                if (strat == null) {
                    try {
                        strat = parsePathStrategy(context, authority)
                    } catch (e: IOException) {
                        throw IllegalArgumentException(
                            "Failed to parse android.support.FILE_PROVIDER_PATHS meta-data",
                            e
                        )
                    } catch (e: XmlPullParserException) {
                        throw IllegalArgumentException(
                            "Failed to parse android.support.FILE_PROVIDER_PATHS meta-data",
                            e
                        )
                    }

                    sCache.put(authority, strat)
                }
                return strat
            }
        }

        @Throws(IOException::class, XmlPullParserException::class)
        private fun parsePathStrategy(context: Context, authority: String): PathStrategy {
            val strat = SimplePathStrategy(authority)
            val info = context.getPackageManager().resolveContentProvider(authority, 128)
            val `in` = info!!.loadXmlMetaData(
                context.getPackageManager(),
                "com.wiwolf.LOVETOSHARE"
            )
            requireNotNull(`in` != null) { "Missing com.wiwolf.LOVETOSHARE meta-data" }
            var type: Int
            while ((`in`.next().also { type = it }) != 1) {
                if (type == 2) {
                    val tag = `in`.getName()
                    val name = `in`.getAttributeValue(null as String?, "name")
                    val path = `in`.getAttributeValue(null as String?, "path")
                    var target: File? = null
                    if ("root-path" == tag) {
                        target = DEVICE_ROOT
                    } else if ("files-path" == tag) {
                        target = context.getFilesDir()
                    } else if ("cache-path" == tag) {
                        target = context.getCacheDir()
                    } else if ("external-path" == tag) {
                        target = Environment.getExternalStorageDirectory()
                    } else if ("external-files-path" == tag) {
                        val externalFilesDirs =
                            context.getExternalFilesDirs( null as String?)
                        if (externalFilesDirs.size > 0) {
                            target = externalFilesDirs[0]
                        }
                    } else if ("external-cache-path" == tag) {
                        val externalCacheDirs = context.getExternalCacheDirs()
                        if (externalCacheDirs.size > 0) {
                            target = externalCacheDirs[0]
                        }
                    } else if ("external-media-path" == tag) {
                        val externalMediaDirs = context.getExternalMediaDirs()
                        if (externalMediaDirs.size > 0) {
                            target = externalMediaDirs[0]
                        }
                    }

                    if (target != null) {
                        strat.addRoot(name, buildPath(target, path)!!)
                    }
                }
            }

            return strat
        }

        private fun modeToMode(mode: String?): Int {
            val modeBits: Int
            if ("r" == mode) {
                modeBits = 268435456
            } else if ("w" != mode && "wt" != mode) {
                if ("wa" == mode) {
                    modeBits = 704643072
                } else if ("rw" == mode) {
                    modeBits = 939524096
                } else {
                    require("rwt" == mode) { "Invalid mode: " + mode }

                    modeBits = 1006632960
                }
            } else {
                modeBits = 738197504
            }

            return modeBits
        }

        private fun buildPath(base: File?, vararg segments: String?): File? {
            var cur = base

            for (segment in segments) {
                if (segment != null) {
                    cur = File(cur, segment)
                }
            }

            return cur
        }

        private fun copyOf(original: Array<String?>, newLength: Int): Array<String?> {
            val result = arrayOfNulls<String>(newLength)
            System.arraycopy(original, 0, result, 0, newLength)
            return result
        }

        private fun copyOf(original: Array<Any?>, newLength: Int): Array<Any?> {
            val result = arrayOfNulls<Any>(newLength)
            System.arraycopy(original, 0, result, 0, newLength)
            return result
        }
    }
}