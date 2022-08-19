package com.android.test

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class AppSelectDialogFragment : AppCompatDialogFragment() {

    var isAndroidData = true

    companion object {
        const val TAG = "AppSelectDialogFragment"

        const val ANDROID_DATA = "androidData"

        const val DOCID_ANDROID_DATA = "primary:Android/data"
        const val DOCID_ANDROID_OBB = "primary:Android/obb"

        fun newInstance(androidData: Boolean): AppSelectDialogFragment {
            val asd = AppSelectDialogFragment()
            val bundle = Bundle().apply {
                putBoolean(ANDROID_DATA, androidData)
            }
            asd.arguments = bundle
            return asd
        }
    }

    private var adapter: AppAdapter? = null
    var onSelectedListener: OnSelectedListener? = null

    interface OnSelectedListener {
        fun onSelected(appItem: AppItem?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val act = activity
        if (act != null) {
            adapter = AppAdapter(arrayListOf())
        }
        arguments?.let {
            isAndroidData = it.getBoolean(ANDROID_DATA)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val act = activity
        if (act != null) {
            val inflater = LayoutInflater.from(act)
            val layout = inflater.inflate(R.layout.dialog_fragment_app, null)
            val listAppItem = layout.findViewById<View>(R.id.listApp) as RecyclerView
            if (adapter == null) {
                adapter = AppAdapter(arrayListOf())
            }

            Thread {
                val activ = activity
                activ ?: return@Thread
                val dataPkg = hashMapOf<String, Uri>()
                val obbPkg = hashMapOf<String, Uri>()

                val pm = activ.packageManager ?: return@Thread
                val pkgList = arrayListOf<AppItem>()
                val pkgSet = HashSet<String>()

                val mainIntent = Intent(Intent.ACTION_MAIN)
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                val apps = pm.queryIntentActivities(mainIntent, 0) //PackageManager.MATCH_ALL
                for (app in apps) {
                    val appPkg: String = app.activityInfo.packageName
                    if (!pkgSet.contains(appPkg)) {
                        val item = AppItem()
                        item.pkg = appPkg
                        item.name = app.activityInfo.loadLabel(pm).toString()
                        item.desk = true
                        if (isAndroidData) {
                            val dir = File("/storage/emulated/0/Android/data", appPkg)
                            item.exits = dir!=null && dir.exists()
                            item.hasPermission = dataPkg.contains(appPkg)
                            if (item.hasPermission) {
                                item.uri = dataPkg[appPkg]
                            }
                        } else {
                            val dir = File("/storage/emulated/0/Android/obb", appPkg)
                            item.exits = dir!=null && dir.exists()
                            item.hasPermission = obbPkg.contains(appPkg)
                            if (item.hasPermission) {
                                item.uri = obbPkg[appPkg]
                            }
                        }
                        pkgList.add(item)
                        pkgSet.add(appPkg)
                    }
                }
                pkgList.sortBy { it.name }

                val applications = pm.getInstalledApplications(0)
                for (application in applications) {
                    val appPkg = application.packageName
                    if (!pkgSet.contains(appPkg)) {
                        val item = AppItem()
                        item.pkg = appPkg
                        item.name = application.loadLabel(pm).toString()
                        item.desk = false
                        if (isAndroidData) {
                            val dir = File("/storage/emulated/0/Android/data", appPkg)
                            item.exits = dir!=null && dir.exists()
                            item.hasPermission = dataPkg.contains(appPkg)
                            if (item.hasPermission) {
                                item.uri = dataPkg[appPkg]
                            }
                        } else {
                            val dir = File("/storage/emulated/0/Android/obb", appPkg)
                            item.exits = dir!=null && dir.exists()
                            item.hasPermission = obbPkg.contains(appPkg)
                            if (item.hasPermission) {
                                item.uri = obbPkg[appPkg]
                            }
                        }
                        pkgList.add(item)
                        pkgSet.add(appPkg)
                    }
                }
                pkgList.sortWith(compareBy({ !it.hasPermission }, { !it.desk }, { it.name }))

                activity?.runOnUiThread {
                    val appSelectRecyclerAdapter = AppSelectRecyclerAdapter(pkgList)
                    appSelectRecyclerAdapter.setOnAppClickListener { appItem ->
                        onSelectedListener?.onSelected(appItem)
                    }
                    listAppItem.adapter = appSelectRecyclerAdapter
                    //adapter?.setNewData(pkgList)
                }
            }.start()


            listAppItem.adapter = adapter
            adapter?.setOnItemClickListener { baseQuickAdapter, view, i ->
                if (adapter != null) {
                    val item = adapter!!.getItem(i)
                    onSelectedListener?.onSelected(item)
                }
            }
            return MaterialAlertDialogBuilder(act)
                //.setIcon(R.drawable.file_icon_apk)
                .setTitle(R.string.select) //.setMessage(property)
                .setView(layout)
                .setPositiveButton(R.string.cancel) { dialog, which -> dismiss() }
                .create()
        }
        return super.onCreateDialog(savedInstanceState)
    }

    inner class AppItem {
        var pkg: String? = null
        var name: String? = null
        var desk: Boolean = false//show in launcher
        var exits: Boolean = false
        var hasPermission: Boolean = false
        var uri: Uri? = null
    }

    inner class AppAdapter(data: MutableList<AppItem>) : BaseQuickAdapter<AppItem, BaseViewHolder>(R.layout.item_select_app, data) {

        override fun convert(helper: BaseViewHolder, appItem: AppItem) {
            helper.setText(R.id.tvName, appItem.name)
            helper.setText(R.id.tvPkg, appItem.pkg)
            val iv = helper.getView<ImageView>(R.id.ivIcon)
            try {
                appItem.pkg?.let {
                    val packageInfo = TestApp.instance.packageManager.getPackageInfo(it, 0)
                    if (packageInfo != null) {
                        Glide.with(iv)
                            .load(packageInfo)
                            .placeholder(R.drawable.file_icon_apk)
                            .into(iv)
                        //Log.w(TAG, "load PackageInfo: $it")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
