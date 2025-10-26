package app.wetube.page

import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Fragment
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.DialogPreference
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.PreferenceGroup
import android.support.v7.graphics.Palette
import android.text.SpannableStringBuilder
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import app.wetube.R
import app.wetube.core.Search
import app.wetube.core.dime
import app.wetube.core.getBitmapFromView
import app.wetube.core.info
import app.wetube.core.tryOn
import app.wetube.databinding.InfoBinding
import app.wetube.item.ChannelDetail
import app.wetube.manage.provide.FileProvider
import app.wetube.page.dialog.PreviewImgPage
import app.wetube.preference.InfoDialogPreference
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.File
import java.io.FileOutputStream
import kotlin.text.ifEmpty

class AboutChannel: PreferenceFragment() {

    private var isInit = false

    private var currentAM : ActionMode? = null


    private val dinfo = arrayListOf<String?>()

    lateinit var item : ChannelDetail


    private var thumb = ""

    private var ic: Drawable? = null




    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }



    companion object{
        fun newInstance(intent: Intent): AboutChannel {
            val args = Bundle().apply {
                intent.also{i->
                    putString("name", i.getStringExtra("name"))
                    putString("id", i.getStringExtra("id"))
                }
            }
            val fragment = AboutChannel()
            fragment.setHasOptionsMenu(true)
            fragment.arguments = args
            return fragment
        }
    }




    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        tryOn{
            outState?.apply {
                if(::item.isInitialized){
                    putParcelable("item", item)
                }
                putStringArrayList("dinfo", dinfo)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        val l = MenuItem.OnMenuItemClickListener {
            when (it.title) {
                "Load icon"->loadIcon()
                "Look" -> {
                    ic?.let {i->
                        activity?.showDialog(
                            PreviewImgPage.PREVIEW_IMAGE,
                            Bundle().apply {
                                putBinder(
                                    PreviewImgPage.PREVIEW_CODE,
                                    PreviewImgPage.Get(
                                        i,
                                        arguments.getString("name").toString()
                                    )
                                )
                            }
                        )
                    }
                }

                "Share icon" -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    val imagefolder = File(activity!!.cacheDir, "images")
                    var uri: Uri? = null
                    if (ic == null) {
                        Toast.makeText(activity!!, "No image available", Toast.LENGTH_LONG)
                            .show()
                        false
                    }
                    try {
                        imagefolder.mkdirs()
                        val file = File(imagefolder, "shared_image.png")
                        val outputStream = FileOutputStream(file)

                        val bitmap = Bitmap.createBitmap(
                            ic!!.getIntrinsicWidth(),
                            ic!!.getIntrinsicHeight(), Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(bitmap)
                        ic!!.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
                        ic!!.draw(canvas)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                        outputStream.flush()
                        outputStream.close()
                        uri = FileProvider.getUriForFile(
                            activity!!,
                            "app.wetube.image-share",
                            file
                        )
                    } catch (e: Exception) {
                        Toast.makeText(activity!!, e.message, Toast.LENGTH_LONG).show()
                    }

                    // putting uri of image to be shared
                    intent.apply {
                        putExtra(Intent.EXTRA_STREAM, uri)
                        // adding text to share

                        // Add subject Here
                        putExtra(Intent.EXTRA_SUBJECT, "Subject Here")

                        // setting type to image
                        setType("image/png")

                    }
                    startActivity(Intent.createChooser(intent, "Share Via"))
                }

                "Like this channel?" -> {
                    AlertDialog.Builder(activity)
                        .setTitle("Do you like ${item.title}?")
                        .setPositiveButton("Yes", null)
                        .setNegativeButton("No", null)
                        .create().apply {
                            window!!.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                            show()
                            Toast.makeText(activity, "Wait.......", Toast.LENGTH_LONG)
                                .show()
                            Handler(activity.mainLooper).postDelayed({
                                Toast.makeText(activity, "Get it!", Toast.LENGTH_LONG)
                                    .show()
                                val intent = Intent(Intent.ACTION_SEND)
                                val imagefolder = File(activity!!.cacheDir, "images")
                                val d = window?.decorView?.getBitmapFromView()
                                var uri: Uri? = null
                                if (d == null) {
                                    Toast.makeText(
                                        activity!!,
                                        "No image available",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                try {
                                    imagefolder.mkdirs()
                                    val file = File(imagefolder, "shared_image.png")
                                    val outputStream = FileOutputStream(file)
                                    d?.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                                    outputStream.flush()
                                    outputStream.close()
                                    uri = FileProvider.getUriForFile(
                                        activity!!,
                                        "app.wetube.image-share",
                                        file
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(activity!!, e.message, Toast.LENGTH_LONG)
                                        .show()
                                }

                                // putting uri of image to be shared
                                intent.apply {
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    // adding text to share

                                    // Add subject Here
                                    putExtra(Intent.EXTRA_SUBJECT, "Subject Here")

                                    // setting type to image
                                    setType("image/png")

                                }

                                // calling startactivity() to share
                                startActivity(Intent.createChooser(intent, "share"))
                                Handler(activity.mainLooper).postDelayed({
                                    this.dismiss()
                                }, 30L)

                            }, 250L)
                        }
                }

                "Screenshot about" -> {
                    val d = activity.window.decorView.getBitmapFromView()
                    val intent = Intent(Intent.ACTION_SEND)
                    val imagefolder = File(activity!!.cacheDir, "images")
                    var uri: Uri? = null
                    try {
                        imagefolder.mkdirs()
                        val file = File(imagefolder, "shared_image.png")
                        val outputStream = FileOutputStream(file)
                        d.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                        outputStream.flush()
                        outputStream.close()
                        uri = FileProvider.getUriForFile(
                            activity!!,
                            "app.wetube.image-share",
                            file
                        )
                    } catch (e: Exception) {
                        Toast.makeText(activity!!, e.message, Toast.LENGTH_LONG).show()
                    }

                    // putting uri of image to be shared
                    intent.apply {
                        putExtra(Intent.EXTRA_STREAM, uri)
                        // adding text to share

                        // Add subject Here
                        putExtra(Intent.EXTRA_SUBJECT, "Subject Here")

                        // setting type to image
                        setType("image/png")

                    }
                    startActivity(Intent.createChooser(intent, "Share Via"))
                }
            }
            true
        }
        menu?.addSubMenu("Tools")?.apply {
            add("Look").setOnMenuItemClickListener(l)
            add("Share icon").setOnMenuItemClickListener(l)
            add("Load icon").setOnMenuItemClickListener(l)
            add("Like this channel?").setOnMenuItemClickListener(l)
            add("Screenshot about").setOnMenuItemClickListener(l)
        }
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val idc = arguments?.getString("id") ?: return
        if(activity == null)return
        preferenceScreen = preferenceManager.createPreferenceScreen(activity)
        val profileNname = InfoDialogPreference(activity)
        profileNname.setTitle(arguments.getString("name").toString())
        profileNname.summary = "Tap to look the description"
        profileNname.setNegativeButtonText(R.string.close)
        profileNname.setPositiveButtonText(null)
        val bioCat = PreferenceCategory(activity)
        bioCat.setTitle("More info")
        preferenceScreen.addPreference(profileNname)
        preferenceScreen.addPreference(bioCat)
        if(savedInstanceState == null){
            if(!isInit){
                Search(activity!!).findChannelById(idc, {
                    item = it
                    thumb = it.thumbnail
                    profileNname.setDialogTitle(arguments.getString("name").toString())
                    profileNname.setDialogMessage(it.description.ifEmpty { "No information available" })
                    if(arguments.getString("name").toString() != it.title){
                        dinfo.add("Old Name : ${arguments.getString("name").toString()}\nNew name : ${it.title}")
                    }
                    if(it.born.isNotEmpty()){
                        dinfo.add("Since : ${it.born}")
                    }
                    if(it.lived.isNotEmpty()){
                        dinfo.add("Live in : ${it.lived}")
                    }
                    if(it.genzid.isNotEmpty()) {
                        dinfo.add("Defined Id : ${it.genzid}")
                    }
                    isInit = true
                }, onDone = {
                    loadIcon()
                    dinfo.forEach {
                        bioCat.addPreference(Preference(activity).apply {
                            setSummary(it)
                        })
                    }
                })
            }
        }




        super.onViewCreated(view, savedInstanceState)
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onViewStateRestored(savedInstanceState)
        if(savedInstanceState != null){
            val profileNname = preferenceScreen.getPreference(0)
            profileNname.summary = savedInstanceState.getString("info", "No information available")
            val par = savedInstanceState.getParcelable<ChannelDetail>("item")
            if(par == null)return
            item = par
            thumb = item.thumbnail
            loadIcon()
            profileNname.setTitle(item.title)
            profileNname.setSummary(item.description)
            if(item.born.isNotEmpty()){
                dinfo.add("Since : ${item.born}")
            }
            if(item.lived.isNotEmpty()){
                dinfo.add("Live in : ${item.lived}")
            }
            if(item.genzid.isNotEmpty()) {
                dinfo.add("Defined Id : ${item.genzid}")
            }
            loadIcon()
            val bioCat = preferenceScreen.getPreference(1)
            if(bioCat is PreferenceGroup){
                dinfo.forEach {
                    bioCat.addPreference(Preference(activity).apply {
                        setTitle(it)
                    })
                }
            }
        }
    }

    private fun loadIcon() {
        tryOn {
            Picasso.get()
                .load(thumb)
                .error(R.drawable.info)
                .into(object  : Target {

                    override fun onBitmapLoaded(
                        bitmap: Bitmap?,
                        from: Picasso.LoadedFrom?
                    ) {
                        ic = BitmapDrawable(bitmap)
                        try{
                            (preferenceScreen?.getPreference(0) as InfoDialogPreference).setDialogIcon(
                                ic
                            )
                        }catch (_: Exception){
                            preferenceScreen?.getPreference(0)?.setIcon(
                                ic
                            )
                        }
                        activity?.setTaskDescription(ActivityManager.TaskDescription(arguments.getString("name").toString(), bitmap))
                    }




                    override fun onBitmapFailed(
                        e: java.lang.Exception?,
                        errorDrawable: Drawable?
                    ) {
                        info("Cannot load the profile")
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    }
                })
            isInit = true
        }
    }


    override fun onStart() {
        super.onStart()
        setHasOptionsMenu(true)
    }

    override fun onDetach() {
        super.onDetach()
        tryOn {
            currentAM?.finish()
        }
        isInit = !isRemoving
    }


}