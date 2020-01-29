package com.lyc.bookshelf

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lyc.api.book.BookFile
import com.lyc.api.main.AbstractMainTabFragment
import com.lyc.base.ReaderApplication
import com.lyc.base.arch.provideViewModel
import com.lyc.base.ui.BaseActivity
import com.lyc.base.ui.ReaderToast
import com.lyc.base.ui.getDrawableAttrRes
import com.lyc.base.ui.getDrawableRes
import com.lyc.base.ui.theme.color_light_blue
import com.lyc.base.ui.theme.color_secondary_text
import com.lyc.base.ui.widget.ReaderPopupMenu
import com.lyc.base.ui.widget.SimpleToolbar
import com.lyc.base.utils.*
import com.lyc.bookshelf.scan.BookScanActivity
import com.lyc.bookshelf.utils.detectCharset
import com.lyc.bookshelf.utils.singleUriDocumentFile
import java.io.FileInputStream

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
class BookShelfFragment : AbstractMainTabFragment(), View.OnClickListener,
    PopupMenu.OnMenuItemClickListener, SwipeRefreshLayout.OnRefreshListener,
    BookShelfListAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var viewModel: BookShelfViewModel
    private val emptyViewList = hashSetOf<View>()
    private var menu: PopupMenu? = null

    companion object {
        const val MENU_ID_ADD_FROM_LOCAL = 1
        const val MENU_ID_SCAN_LOCAL = 4

        val REQUEST_CODE_FILE = generateNewRequestCode()
        val REQUEST_CODE_DIR = generateNewRequestCode()
        val REQUEST_CODE_SCAN = generateNewRequestCode()

        val VIEW_ID_ADD_FILE = generateNewViewId()
        val VIEW_ID_SCAN_FOLDER = generateNewViewId()

        val VIEW_ID_RV = generateNewViewId()

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
        val contentLayout = LinearLayout(ctx)
        contentLayout.gravity = Gravity.CENTER
        contentLayout.orientation = LinearLayout.VERTICAL
        recyclerView = RecyclerView(ctx)
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.isVisible = false
        recyclerView.id = VIEW_ID_RV
        contentLayout.addView(recyclerView, LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))

        ImageView(ctx).run {
            contentLayout.addView(this, LinearLayout.LayoutParams(dp2px(100), dp2px(100)))
            emptyViewList.add(this)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageDrawable(getDrawableRes(R.drawable.ic_empty_box)?.apply {
                changeToColor(color_secondary_text)
            })
        }
        TextView(ctx).run {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(16f))
            setTextColor(color_secondary_text)
            gravity = Gravity.CENTER
            setPadding(dp2px(24))
            contentLayout.addView(this, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            emptyViewList.add(this)
            text = "还没有添加任何书籍"
        }

        TextView(ctx).apply {
            emptyViewList.add(this)
            setCompoundDrawablesWithIntrinsicBounds(
                getDrawableRes(com.lyc.api.R.drawable.ic_add_24dp)?.apply {
                    changeToColor(color_light_blue)
                },
                null,
                null,
                null
            )
            compoundDrawablePadding = dp2px(8)
            id = VIEW_ID_ADD_FILE
            setOnClickListener(this@BookShelfFragment)
            setPadding(dp2px(16))
            paint.isFakeBoldText = true
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(14f))
            setTextColor(buildCommonButtonTextColor(color_light_blue))
            text = "添加文件"
            background = getDrawableAttrRes(android.R.attr.selectableItemBackground)
            contentLayout.addView(this, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        }


        TextView(ctx).apply {
            emptyViewList.add(this)
            setCompoundDrawablesWithIntrinsicBounds(
                getDrawableRes(com.lyc.api.R.drawable.ic_folder_open_24dp)?.apply {
                    changeToColor(color_light_blue)
                },
                null,
                null,
                null
            )
            compoundDrawablePadding = dp2px(8)
            id = VIEW_ID_SCAN_FOLDER
            setOnClickListener(this@BookShelfFragment)
            setPadding(dp2px(16))
            paint.isFakeBoldText = true
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(14f))
            setTextColor(color_light_blue)
            text = "扫描目录"
            background = getDrawableAttrRes(android.R.attr.selectableItemBackground)
            contentLayout.addView(this, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        }


        for (view in emptyViewList) {
            view.isVisible = false
        }

        refreshLayout.addView(contentLayout)
        refreshLayout.setOnRefreshListener(this)
        rootView.addView(refreshLayout, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            topMargin = toolBar.getViewHeight()
        })
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = provideViewModel()
        recyclerView.adapter = BookShelfListAdapter(viewModel.list, this).apply {
            observe(this@BookShelfFragment)
        }
        viewModel.isLoadingLiveData.observe(this, Observer { isLoading ->
            refreshLayout.isRefreshing = isLoading
        })
        viewModel.hasDataLiveData.observe(this, Observer { hasData: Boolean? ->
            val firstLoadFinish = viewModel.firstLoadFinish
            recyclerView.isVisible = firstLoadFinish && hasData == true
            val emptyViewVisible = firstLoadFinish && hasData == false
            emptyViewList.forEach {
                it.isVisible = emptyViewVisible
            }
        })
        viewModel.firstLoadIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        activity?.window.statusBarBlackText(true)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            SimpleToolbar.VIEW_ID_RIGHT_BUTTON -> {
                menu?.dismiss()

                menu = ReaderPopupMenu(v.context, v, Gravity.LEFT or Gravity.BOTTOM).also {
                    it.setOnMenuItemClickListener(this)
                    it.menu.run {
                        add(0, MENU_ID_ADD_FROM_LOCAL, MENU_ID_ADD_FROM_LOCAL, "导入本地书籍")
                        add(0, MENU_ID_SCAN_LOCAL, MENU_ID_SCAN_LOCAL, "扫描书籍")
                    }
                    it.show()
                }
            }
            VIEW_ID_ADD_FILE -> {
                performFileSearch()
            }
            VIEW_ID_SCAN_FOLDER -> {
                performDirSearch()
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
                val len = singleUriDocumentFile(singleUri).length()
                if (len <= 0) {
                    ReaderToast.showToast("文件过小")
                    return true
                }
            }

            val uriList =
                if (singleUri != null) {
                    testLogUri(singleUri)
                    listOf(singleUri)
                } else {
                    multiUri?.forEach { testLogUri(it) }
                    multiUri
                }
            (activity as? BaseActivity)?.let {
                if (uriList != null) {
                    BookManager.instance.importBooks(uriList)
                }
            }

            return true
        } else if (requestCode == REQUEST_CODE_DIR) {
            val dirUri = data?.data
            if (dirUri != null) {
                activity?.let {
                    BookScanActivity.start(it, dirUri, REQUEST_CODE_SCAN)
                }
            }
        } else if (requestCode == REQUEST_CODE_SCAN) {
            val uris =
                data?.getParcelableArrayExtra(KEY_SELECT_SCAN_FILES)?.mapNotNull {
                    it as? Uri
                }
            if (uris != null && uris.isNotEmpty()) {
                uris.forEach { testLogUri(it) }
                BookManager.instance.importBooks(uris)
            }
        }

        return false
    }

    private fun testLogUri(uri: Uri) {
        if ("release" == BuildConfig.BUILD_TYPE) {
            return
        }
        val documentFile = singleUriDocumentFile(uri)
        val charset = uri.detectCharset()
        LogUtils.d(
            TAG,
            "Uri=${Uri.decode(uri.toString())}; File exists: ${documentFile.exists()}; Charset=${charset}"
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

    override fun onRefresh() {
        viewModel.refreshList()
    }

    override fun onBookShelfItemClick(pos: Int, data: BookFile, view: BookShelfItemView) {
        LogUtils.d(TAG, "Click book item [position=${pos}] [data=${data}].")
    }
}
