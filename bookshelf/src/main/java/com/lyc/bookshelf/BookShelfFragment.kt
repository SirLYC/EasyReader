package com.lyc.bookshelf

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lyc.api.main.AbstractMainTabFragment
import com.lyc.base.ReaderApplication
import com.lyc.base.ui.widget.SimpleToolbar
import com.lyc.base.utils.LogUtils
import com.lyc.base.utils.generateNewRequestCode
import com.lyc.base.utils.statusBarBlackText
import com.lyc.bookshelf.scan.BookScanActivity
import com.lyc.bookshelf.utils.detectCharset
import com.lyc.bookshelf.utils.singleUriDocumentFile
import java.io.FileInputStream

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
class BookShelfFragment : AbstractMainTabFragment(), View.OnClickListener,
    PopupMenu.OnMenuItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private var menu: PopupMenu? = null

    companion object {
        const val MENU_ID_ADD_FROM_LOCAL = 1
        const val MENU_ID_SCAN_LOCAL = 4

        val REQUEST_CODE_FILE = generateNewRequestCode()
        val REQUEST_CODE_DIR = generateNewRequestCode()
        val REQUEST_CODE_SCAN = generateNewRequestCode()

        const val TAG = "BookShelfFragment"
    }

    override fun newViewInstance(container: ViewGroup?): View? {
        val ctx = context!!
        val rootView = FrameLayout(ctx)
        val toolBar = SimpleToolbar(ctx, R.drawable.ic_more_horiz_24dp)
        toolBar.setTitle("我的书架")
        toolBar.leftButton?.isVisible = false
        toolBar.setBarClickListener(this)
        rootView.addView(toolBar, FrameLayout.LayoutParams(MATCH_PARENT, toolBar.getViewHeight()))
        refreshLayout = SwipeRefreshLayout(ctx)
        recyclerView = RecyclerView(ctx)
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        refreshLayout.addView(recyclerView)
        rootView.addView(refreshLayout, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            topMargin = toolBar.getViewHeight()
        })
        return rootView
    }

    override fun onResume() {
        super.onResume()
        activity?.window.statusBarBlackText(true)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            SimpleToolbar.VIEW_ID_RIGHT_BUTTON -> {
                menu?.dismiss()

                menu = PopupMenu(v.context, v, Gravity.LEFT or Gravity.BOTTOM).also {
                    it.setOnMenuItemClickListener(this)
                    it.menu.run {
                        add(0, MENU_ID_ADD_FROM_LOCAL, MENU_ID_ADD_FROM_LOCAL, "导入本地书籍")
                        add(0, MENU_ID_SCAN_LOCAL, MENU_ID_SCAN_LOCAL, "扫描书籍")
                    }
                    it.show()
                }
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            MENU_ID_ADD_FROM_LOCAL -> {
                performFileSearch()
            }

            MENU_ID_SCAN_LOCAL -> {
                performDirSearch()
            }
        }
        return true
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (resultCode != RESULT_OK) {
            return false
        }
        if (requestCode == REQUEST_CODE_FILE) {
            val singleUri = data?.data
            val multiUri = data?.clipData?.run {
                List(itemCount) { index -> getItemAt(index).uri }.filterNotNull()
            }

            if (singleUri != null) {
                testLogUri(singleUri)
            } else {
                multiUri?.forEach { testLogUri(it) }
            }

            return true
        } else if (requestCode == REQUEST_CODE_DIR) {
            val dirUri = data?.data
            if (dirUri != null) {
                activity?.let {
                    BookScanActivity.start(it, dirUri, REQUEST_CODE_SCAN)
                }
            }
        }

        return false
    }

    private fun testLogUri(uri: Uri) {
        val documentFile = singleUriDocumentFile(uri)
        val charset = uri.detectCharset()
        LogUtils.d(
            TAG,
            "Uri=${uri.path}; File exists: ${documentFile.exists()}; Charset=${charset}"
        )
        ReaderApplication.appContext().contentResolver.openFileDescriptor(uri, "r")
            ?.use { fd ->
                FileInputStream(fd.fileDescriptor).bufferedReader(charset)
                    .use { reader ->
                        repeat(5) {
                            LogUtils.d(TAG, reader.readLine())
                        }
                    }
            }
    }


    private fun performFileSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, REQUEST_CODE_FILE)
    }

    private fun performDirSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, REQUEST_CODE_DIR)
    }
}
