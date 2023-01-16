package com.android.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.github.k1rakishou.fsaf.FileChooser
import com.github.k1rakishou.fsaf.FileManager
import com.github.k1rakishou.fsaf.callback.FSAFActivityCallbacks
import com.github.k1rakishou.fsaf_test_app.TestBaseDirectory
import com.github.k1rakishou.fsaf_test_app.tests.TestSuite
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), FSAFActivityCallbacks {

    private lateinit var testSuite: TestSuite

    private lateinit var fileManager: FileManager
    private lateinit var fileChooser: FileChooser

    private lateinit var sharedPreferences: SharedPreferences

    private val testBaseDirectory = TestBaseDirectory({
        getTreeUri()
    }, {
        null
    })

    companion object {
        private const val TAG = "MainActivity"

        const val DOCID_ANDROID_DATA = "primary:Android/data"
        const val DOCID_ANDROID_OBB = "primary:Android/obb"

        const val REQ_SAF_R_DATA = 202030
        const val REQ_SAF_R_OBB  = 202036

        const val TREE_URI = "tree_uri"
    }

    var tv: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("test", MODE_PRIVATE)

        fileManager = FileManager(applicationContext)
        fileChooser = FileChooser(applicationContext)
        testSuite = TestSuite(fileManager, this)
        //fileChooser.setCallbacks(this)

        if (getTreeUri() != null) {
            fileManager.registerBaseDir(TestBaseDirectory::class.java, testBaseDirectory)
        }

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

        findViewById<View>(R.id.btnTest).setOnClickListener {
            runTests()
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
                Log.d(TAG, "onActivityResult: $data")
                if (!DocumentVM.checkFolderPermission(act,DOCID_ANDROID_DATA)) {
                    if (resultCode == Activity.RESULT_OK) {
                        if (data != null) {
                            goSAF(data)

                            removeTreeUri()
                            storeTreeUri(data)

                            try {
                                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                contentResolver.takePersistableUriPermission(data, flags)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            //runTests()
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
                Log.d(TAG, "onActivityResult: $data")
                if (!DocumentVM.checkFolderPermission(act, DOCID_ANDROID_OBB)) {
                    if (resultCode == Activity.RESULT_OK) {
                        if (data != null) {
                            goSAF(data)

                            removeTreeUri()
                            storeTreeUri(data)

                            try {
                                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                contentResolver.takePersistableUriPermission(data, flags)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            //runTests()
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

    private fun runTests() {
        try {
            val baseSAFDir = fileManager.newBaseDirectoryFile<TestBaseDirectory>()
            if (baseSAFDir == null) {
                throw NullPointerException("baseSAFDir is null!")
            }

            val baseFileApiDir = fileManager.fromRawFile(
                File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "test")
            )

            fileManager.create(baseFileApiDir)

            testSuite.runTests(
                baseSAFDir,
                baseFileApiDir
            )

            val message = "=== ALL TESTS HAVE PASSED ==="
            println(message)
            showDialog(message)
        } catch (error: Throwable) {
            error.printStackTrace()
            showDialog(error.message ?: "Unknown error")
        }
    }

    private fun showDialog(message: String) {
        val dialog = AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()
    }

    private fun storeTreeUri(uri: Uri) {
        val dir = checkNotNull(fileManager.fromUri(uri)) { "fileManager.fromUri(${uri}) failure" }

        check(fileManager.exists(dir)) { "Does not exist" }
        check(fileManager.isDirectory(dir)) { "Not a dir" }

        fileManager.registerBaseDir<TestBaseDirectory>(testBaseDirectory)
        sharedPreferences.edit().putString(TREE_URI, uri.toString()).apply()
        Log.d(TAG, "storeTreeUri: $uri")
    }

    private fun removeTreeUri() {
        val treeUri = getTreeUri()
        if (treeUri == null) {
            println("Already removed")
            return
        }

        fileChooser.forgetSAFTree(treeUri)
        fileManager.unregisterBaseDir<TestBaseDirectory>()
        sharedPreferences.edit().remove(TREE_URI).apply()
    }

    private fun getTreeUri(): Uri? {
        return sharedPreferences.getString(TREE_URI, null)
            ?.let { str -> Uri.parse(str) }
    }

    override fun fsafStartActivityForResult(intent: Intent, requestCode: Int) {
        //
    }

}