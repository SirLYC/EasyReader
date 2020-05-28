package com.lyc.readertestapp.testfilescan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.bookshelf.utils.forEach
import com.lyc.easyreader.bookshelf.utils.treeDocumentFile
import com.lyc.readertestapp.R
import kotlinx.android.synthetic.main.activity_test_file_scan.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.system.measureTimeMillis

/**
 * Created by Liu Yuchuan on 2020/5/27.
 */
class TestFileScanActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE = 6
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_file_scan)

        bt_begin.setOnClickListener {
            performDirSearch()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let {
                et_depth.text?.toString()?.toIntOrNull()?.let { depth ->
                    if (depth >= 2) {
                        ReaderToast.showToast("开始，深度=$depth")
                        thread {
                            runFileScan(it, depth)
                        }
                    }
                }
            }
        }
    }

    private fun runFileScan(uri: Uri, maxDepth: Int = 2) {
        val dir = treeDocumentFile(uri)

        val fileCount = AtomicInteger(0)

        val lock = ReentrantLock()
        runOnUiThread {
            tv_top.text = ("Scanning...")
        }
        val timeTop = measureTimeMillis {
            dir.forEach({ file ->
                fileCount.incrementAndGet()
                file.name
                lock.withLock { }
            }, maxDepth = maxDepth, fastMode = false)
        }

        val finalFileCount = fileCount.get()
        runOnUiThread {
            tv_top.text = ("Finish. FileCount=$finalFileCount, time=${timeTop}ms")
        }

        fileCount.set(0)

        runOnUiThread {
            tv_bottom.text = ("Scanning...")
        }
        val timeBottom = measureTimeMillis {
            dir.forEach({ file ->
                fileCount.incrementAndGet()
                file.name
                lock.withLock { }
            }, maxDepth = maxDepth)
        }

        runOnUiThread {
            tv_bottom.text = ("Finish. FileCount=${fileCount.get()}, time=${timeBottom}ms")
        }
    }

    private fun performDirSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, REQUEST_CODE)
    }
}
