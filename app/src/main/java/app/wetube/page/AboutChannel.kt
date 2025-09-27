package app.wetube.page

import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Fragment
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.FileProvider
import android.support.v7.graphics.Palette
import android.text.SpannableStringBuilder
import android.util.Base64
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import app.wetube.R
import app.wetube.core.Search
import app.wetube.core.dime
import app.wetube.core.getBitmapFromView
import app.wetube.core.info
import app.wetube.core.tryOn
import app.wetube.databinding.InfoBinding
import app.wetube.page.dialog.PreviewImgPage
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.text.ifEmpty

class AboutChannel: Fragment() {
    val binding by lazy { InfoBinding.inflate(activity!!!!.layoutInflater) }

    private var isInit = false

    private var currentAM : ActionMode? = null


    private val dinfo = arrayListOf<String?>()


    private var thumb = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val ir = FrameLayout(inflater.context)
        ir.addView(binding.root)
        return ir

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.root.parent?.let {
            if(it is ViewGroup) {
                it.removeView(binding.root)
            }
        }
    }

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
                putString("info", binding.info.text.toString())
                putStringArrayList("dinfo", dinfo)
                putString("thumb", thumb)
            }
        }
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (binding.root.layoutParams as FrameLayout.LayoutParams).apply {
            leftMargin = dime(R.dimen.marginLayout).toInt()
            rightMargin = dime(R.dimen.marginLayout).toInt()
        }
        val idc = arguments?.getString("id") ?: return


        binding.icon.apply{
            setOnCreateContextMenuListener { m,_,_->
                val l = MenuItem.OnMenuItemClickListener {
                    when(it.title){
                        "More info" -> {
                            activity?.showDialog(316109, Bundle().apply {
                                putString("dinfo", dinfo.joinToString(separator = "\n"))
                            })
                        }
                        "Look" -> {
                            drawable?.let {
                                activity?.showDialog(
                                    PreviewImgPage.PREVIEW_IMAGE,
                                    Bundle().apply {
                                        putBinder(
                                            PreviewImgPage.PREVIEW_CODE,
                                            PreviewImgPage.Get(
                                                it,
                                                arguments.getString("name").toString()
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        "Share icon"->{
                            val intent = Intent(Intent.ACTION_SEND)
                            val imagefolder = File(activity!!.cacheDir, "images")
                            var uri: Uri? = null
                            val d = binding.icon.drawable
                            if (d == null) {
                                Toast.makeText(activity!!, "No image available", Toast.LENGTH_LONG).show()
                                false
                            }
                            try {
                                imagefolder.mkdirs()
                                val file = File(imagefolder, "shared_image.png")
                                val outputStream = FileOutputStream(file)

                                val bitmap = Bitmap.createBitmap(
                                    d.getIntrinsicWidth(),
                                    d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888
                                )
                                val canvas = Canvas(bitmap)
                                d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
                                d.draw(canvas)
                                bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                                outputStream.flush()
                                outputStream.close()
                                uri = FileProvider.getUriForFile(activity!!, "app.wetube.image-share", file)
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
                                .setTitle("Do you like ${binding.who.text}?")
                                .setPositiveButton("Yes", null)
                                .setNegativeButton("No", null)
                                .show().apply {
                                    Toast.makeText(activity, "Wait.......", Toast.LENGTH_LONG).show()
                                    Handler(activity.mainLooper).postDelayed({
                                        Toast.makeText(activity, "Get it!", Toast.LENGTH_LONG).show()
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
                        "Screenshot about"->{
                            val d = binding.root.getBitmapFromView()
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
                m?.apply {
                    m.setHeaderIcon(drawable)
                    m.setHeaderTitle(arguments.getString("name").toString())

                    add("More info").setOnMenuItemClickListener(l)
                    add("Look").setOnMenuItemClickListener(l)
                    add("Share icon").setOnMenuItemClickListener(l)
                    add("Like this channel?").setOnMenuItemClickListener(l)
                    add("Screenshot about").setOnMenuItemClickListener(l)
                }
            }
            setOnClickListener {
                it.showContextMenu()
            }
        }
        binding.who.text = SpannableStringBuilder(arguments.getString("name").toString())
        if(savedInstanceState == null){
            if(!isInit){
                Search(activity!!).findChannelById(idc, {
                    thumb = it.thumbnail
                    tryOn {
                        Picasso.get()
                            .load(thumb)
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.info)
                            .into(object  : Target {

                                override fun onBitmapLoaded(
                                    bitmap: Bitmap?,
                                    from: Picasso.LoadedFrom?
                                ) {
                                    binding.icon.setImageBitmap(bitmap)
                                    activity?.setTaskDescription(ActivityManager.TaskDescription(arguments.getString("name").toString(), bitmap))
                                    colorize(bitmap)
                                }




                                override fun onBitmapFailed(
                                    e: java.lang.Exception?,
                                    errorDrawable: Drawable?
                                ) {
                                    info("Cannot load the profile")
                                }

                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                    binding.icon.setImageToDefault()
                                }
                            })
                        val d = it.description.ifEmpty { "No information available" }
                        if(it.born.isNotEmpty()){
                            dinfo.add("Since : ${it.born}")
                        }
                        if(it.lived.isNotEmpty()){
                            dinfo.add("Live in : ${it.lived}")
                        }
                        if(it.genzid.isNotEmpty()) {
                            dinfo.add("Defined Id : ${it.genzid}")
                        }
                        binding.info.text = d
                        isInit = true
                    }
                })
            }
        }




        super.onViewCreated(view, savedInstanceState)
    }


    private fun colorize(resource: Bitmap?) {
        if(resource == null)return

        val b = Palette.from(resource).generate()
        b.mutedSwatch?.let {

            binding.root.setBackgroundColor(it.rgb)
            binding.info.setTextColor(it.bodyTextColor)
            binding.who.setTextColor(it.titleTextColor)
        }
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onViewStateRestored(savedInstanceState)
        if(savedInstanceState != null){
            binding.info.text = savedInstanceState.getString("info", "No information available")
            savedInstanceState.getStringArrayList("dinfo")?.let {
                dinfo.addAll(it)
            }
            thumb = savedInstanceState.getString("thumb")?: ""
            thumb.let {
                tryOn {
                    Picasso.get()
                        .load(it)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.info)
                        .into(object  : Target {

                            override fun onBitmapLoaded(
                                bitmap: Bitmap?,
                                from: Picasso.LoadedFrom?
                            ) {
                                binding.icon.setImageBitmap(bitmap)
                                activity?.setTaskDescription(ActivityManager.TaskDescription(arguments.getString("name").toString(), bitmap))
                                colorize(bitmap)
                            }




                            override fun onBitmapFailed(
                                e: java.lang.Exception?,
                                errorDrawable: Drawable?
                            ) {
                                info("Cannot load the profile")
                            }

                            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                binding.icon.setImageToDefault()
                            }
                        })
                    isInit = true
                }
            }

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





    override fun onConfigurationChanged(newConfig: Configuration) {
        (binding.con.layoutParams as FrameLayout.LayoutParams).apply {
            leftMargin = dime(R.dimen.marginLayout).toInt()
            rightMargin = dime(R.dimen.marginLayout).toInt()
        }
        super.onConfigurationChanged(newConfig)
    }

}