package com.android.test

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"

        const val DOCID_ANDROID_DATA = "primary:Android/data"
        const val DOCID_ANDROID_OBB = "primary:Android/obb"

        const val REQ_SAF_R_DATA = 202030
        const val REQ_SAF_R_OBB  = 202036
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btnGo).setOnClickListener {

        var docId = DOCID_ANDROID_DATA
        if (Build.VERSION.SDK_INT >= 33) {
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
    }

    @Synchronized
    private fun goSAF(uri: Uri, docId: String? = null, hide: Boolean? = false) {

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


}