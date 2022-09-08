package com.android.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"

        const val DOCID_ANDROID_DATA = "primary:Android/data"
        const val DOCID_ANDROID_OBB = "primary:Android/obb"

        const val REQ_SAF_R_DATA = 202030
        const val REQ_SAF_R_OBB  = 202036
    }

    var tv: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv = findViewById(R.id.tv)
        findViewById<View>(R.id.btnGo).setOnClickListener {
            var docId = DOCID_ANDROID_DATA
            if (DocumentVM.atLeastR()) {
                //docId += "/" + act.packageName
                val appSelectDialogFragment = AppSelectDialogFragment.newInstance(true)
                appSelectDialogFragment.onSelectedListener = object:AppSelectDialogFragment.OnSelectedListener{
                    override fun onSelected(appItem: AppSelectDialogFragment.AppItem?) {
                        appItem?.let {
                            docId += "/${it.pkg}"
                            if (it.hasPermission && it.uri != null) {
                                goSAF(it.uri!!)
                                //Log.e(TAG, "onSelected: $docId")
                            } else {
                                DocumentVM.requestFolderPermission(this@MainActivity, REQ_SAF_R_DATA, docId)
                            }
                        }
                        appSelectDialogFragment.dismiss()
                    }
                }
                appSelectDialogFragment.show(supportFragmentManager, "AppSelect4AndroidData")
            }
        }

        findViewById<View>(R.id.btnObb).setOnClickListener {
            var docId = DOCID_ANDROID_OBB
            if (DocumentVM.atLeastR()) {
                //docId += "/" + act.packageName
                val appSelectDialogFragment = AppSelectDialogFragment.newInstance(false)
                appSelectDialogFragment.onSelectedListener = object:AppSelectDialogFragment.OnSelectedListener{
                    override fun onSelected(appItem: AppSelectDialogFragment.AppItem?) {
                        appItem?.let {
                            docId += "/${it.pkg}"
                            if (it.hasPermission && it.uri != null) {
                                goSAF(it.uri!!)
                                //Log.e(TAG, "onSelected: $docId")
                            } else {
                                DocumentVM.requestFolderPermission(this@MainActivity, REQ_SAF_R_OBB, docId)
                            }
                        }
                        appSelectDialogFragment.dismiss()
                    }
                }
                appSelectDialogFragment.show(supportFragmentManager, "AppSelect4AndroidObb")
            }
        }

        findViewById<View>(R.id.myApp).setOnClickListener {
            val pkg = "com.folderv.file"
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
                startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=9196025730305614222"))
                )
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
            }
        }

        if(isZh(this)){
            findViewById<View>(R.id.tvCoolapk).visibility = View.VISIBLE
        }

    }

    @Synchronized
    private fun goSAF(uri: Uri, docId: String? = null, hide: Boolean? = false) {
        // read and write Storage Access Framework https://developer.android.com/guide/topics/providers/document-provider
        val root = DocumentFile.fromTreeUri(this, uri);
        // make a new dir
        //val dir = root?.createDirectory("test")

        //list children
        root?.listFiles()?.let {
            val sb = StringBuilder()
            for (documentFile in it) {
                if(documentFile.isDirectory){
                    sb.append("📁")
                }
                sb.append(documentFile.name).append('\n')
            }
            tv?.text = sb.toString()
        }
    }

    private fun goAndroidData(path: String?) {
        val uri = DocumentVM.getFolderUri(DOCID_ANDROID_DATA, true)
        goSAF(uri, path)
    }

    private fun goAndroidObb(path: String?) {
        val uri = DocumentVM.getFolderUri(DOCID_ANDROID_OBB, true)
        goSAF(uri, path)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        val act: Activity? = this
        val data = intent?.data
        if (requestCode == REQ_SAF_R_DATA) {
            if (act != null) {
                if (!DocumentVM.checkFolderPermission(act,DOCID_ANDROID_DATA)) {
                    if (resultCode == Activity.RESULT_OK) {
                        if (data != null) {
                            goSAF(data)
                        }
                    } else {
                        //showToast("canceled  " + resultCode) //TODO
                    }
                } else {
                    goAndroidData(null)
                }
            }
        }
        else if (requestCode == REQ_SAF_R_OBB) {
            if (act != null) {
                if (!DocumentVM.checkFolderPermission(act, DOCID_ANDROID_OBB)) {
                    if (resultCode == Activity.RESULT_OK) {
                        if (data != null) {
                            goSAF(data)
                        }
                    } else {
                        //showToast("canceled  " + resultCode) //TODO
                    }
                } else {
                    goAndroidObb(null)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, intent)
    }

    fun isZh(context: Context): Boolean {
        val locale: Locale = context.resources.configuration.locale
        val language = locale.language
        return language.endsWith("zh")
    }


}