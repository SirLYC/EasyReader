package com.lyc.easyreader.bookshelf

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lyc.easyreader.api.main.AbstractMainTabFragment
import com.lyc.easyreader.api.settings.ISettingService
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.arch.provideViewModel
import com.lyc.easyreader.base.getSingleApi
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.ui.bottomsheet.LinearDialogBottomSheet
import com.lyc.easyreader.base.ui.getDrawableAttrRes
import com.lyc.easyreader.base.ui.getDrawableRes
import com.lyc.easyreader.base.ui.theme.color_light_blue
import com.lyc.easyreader.base.ui.theme.color_primary_text
import com.lyc.easyreader.base.ui.theme.color_secondary_text
import com.lyc.easyreader.base.ui.widget.BaseToolBar
import com.lyc.easyreader.base.ui.widget.ReaderPopupMenu
import com.lyc.easyreader.base.ui.widget.SimpleToolbar
import com.lyc.easyreader.base.utils.*
import com.lyc.easyreader.base.utils.rv.ReactiveAdapter
import com.lyc.easyreader.bookshelf.collect.CollectActivity
import com.lyc.easyreader.bookshelf.reader.ReaderActivity
import com.lyc.easyreader.bookshelf.scan.BookScanActivity
import com.lyc.easyreader.bookshelf.utils.detectCharset
import com.lyc.easyreader.bookshelf.utils.singleUriDocumentFile
import java.io.File
import java.io.FileInputStream

/**
 * Created by Liu Yuchuan on 2020/1/20.
 */
class BookShelfFragment : AbstractMainTabFragment(), View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener,
    ReactiveAdapter.ItemClickListener, ReactiveAdapter.ItemCheckListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var viewModel: BookShelfViewModel
    private var toolBar: View? = null
    private var editToolBar: SimpleToolbar? = null
    private var deleteButton: View? = null
    private var adapter: ReactiveAdapter? = null
    private val emptyViewList = hashSetOf<View>()
    private var openBookFile = false

    companion object {
        val REQUEST_CODE_FILE = generateNewRequestCode()
        val REQUEST_CODE_DIR = generateNewRequestCode()
        val REQUEST_CODE_SCAN = generateNewRequestCode()

        val VIEW_ID_EDIT_MORE = generateNewViewId()
        val VIEW_ID_DELETE = generateNewViewId()
        val VIEW_ID_ADD_FILE = generateNewViewId()
        val VIEW_ID_SCAN_FOLDER = generateNewViewId()

        val VIEW_ID_RV = generateNewViewId()

        const val TAG = "BookShelfFragment"
    }

    override fun newViewInstance(container: ViewGroup?): View? {
        val ctx = context!!
        val rootView = FrameLayout(ctx)
        val toolBar = SimpleToolbar(ctx, R.drawable.ic_more_horiz_24dp).also { this.toolBar = it }
        toolBar.setTitle("我的书架")
        toolBar.leftButton?.isVisible = false
        toolBar.setBarClickListener(this)
        rootView.addView(toolBar, FrameLayout.LayoutParams(MATCH_PARENT, toolBar.getViewHeight()))
        val editToolBar =
            SimpleToolbar(ctx, R.drawable.ic_more_horiz_24dp).also { this.editToolBar = it }
        editToolBar.titleTv.run {
            gravity = Gravity.LEFT or Gravity.CENTER
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = (layoutParams as FrameLayout.LayoutParams).apply {
                leftMargin = dp2px(56)
                rightMargin = dp2px(112)
            }
        }
        editToolBar.addView(ImageView(context).apply {
            deleteButton = this
            setPadding(dp2px(16), dp2px(12), dp2px(16), dp2px(12))
            scaleType = ImageView.ScaleType.CENTER
            id = VIEW_ID_DELETE
            getDrawableRes(R.drawable.ic_delete_24dp)?.let {
                it.changeToColor(color_primary_text)
                setImageDrawable(it)
            }
            getDrawableAttrRes(android.R.attr.selectableItemBackground)?.let {
                background = it
            }
            setOnClickListener(this@BookShelfFragment)
        }, FrameLayout.LayoutParams(BaseToolBar.BAR_HEIGHT, BaseToolBar.BAR_HEIGHT).apply {
            gravity = Gravity.RIGHT
            rightMargin = dp2px(56)
        })
        editToolBar.setBarClickListener(this)
        rootView.addView(
            editToolBar,
            FrameLayout.LayoutParams(MATCH_PARENT, editToolBar.getViewHeight())
        )
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
                getDrawableRes(com.lyc.easyreader.api.R.drawable.ic_add_24dp)?.apply {
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
                getDrawableRes(com.lyc.easyreader.api.R.drawable.ic_folder_open_24dp)?.apply {
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
        recyclerView.adapter = BookShelfListAdapter(viewModel.list).apply {
            itemCheckListener = this@BookShelfFragment
            itemClickListener = this@BookShelfFragment
            observe(this@BookShelfFragment)
            val selectAllDrawable = getDrawableRes(R.drawable.ic_select_all)?.apply {
                changeToColor(
                    color_primary_text
                )
            }
            val selectDrawable = getDrawableRes(R.drawable.ic_select)?.apply {
                changeToColor(
                    color_primary_text
                )
            }
            checkAllLiveData.observe(this@BookShelfFragment, Observer {
                editToolBar?.rightButton?.setImageDrawable(if (it) selectAllDrawable else selectDrawable)
            })
            adapter = this
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
        viewModel.editModeLiveData.observe(this, Observer {
            refreshLayout.isEnabled = !it
            toolBar?.isVisible = !it
            editToolBar?.isVisible = it
            applyCheckCountChange()
        })
        viewModel.afterDataUpdate = {
            if (adapter?.editMode == true && viewModel.list.isEmpty()) {
                adapter?.itemCheckListener = null
                adapter?.uncheckAll()
                viewModel.checkedIds.forEach { id ->
                    val list = viewModel.list.map { it.id }
                    adapter?.checkPosition(list.indexOf(id))
                }
                adapter?.itemCheckListener = this
                applyCheckCountChange()
            } else {
                setEditMode(enter = false, anim = false)
            }
        }
        if (savedInstanceState != null) {
            if (viewModel.editModeLiveData.value) {
                setEditMode(true, anim = false)
                viewModel.afterDataUpdate?.invoke()
            }
        }
    }

    private fun applyCheckCountChange() {
        val checkCount = adapter?.checkCount()
        editToolBar?.run {
            setTitle("已选择${checkCount ?: 0}项")
        }
        deleteButton?.isVisible = checkCount != 0
    }

    override fun onResume() {
        super.onResume()
        if (openBookFile) {
            recyclerView.scrollToPosition(0)
            openBookFile = false
        }
        activity?.window.statusBarBlackText(true)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            SimpleToolbar.VIEW_ID_RIGHT_BUTTON -> {
                if (adapter?.editMode == true) {
                    adapter?.toggleCheckAll()
                } else {
                    activity?.run {
                        val dialog = LinearDialogBottomSheet(this)
                        val collectId = dialog.addItem("收藏夹", R.drawable.ic_star_border_24dp)
                        val importFileId = dialog.addItem("导入本地书籍", R.drawable.ic_book_24dp)
                        val scanDirId = dialog.addItem("扫描书籍", R.drawable.ic_folder_open_24dp)
                        val batchId = if (viewModel.list.isNotEmpty()) {
                            dialog.addItem("批量管理", R.drawable.ic_format_list_bulleted_24dp)
                        } else {
                            -1
                        }
                        val settingId = dialog.addItem("设置", R.drawable.ic_settings_24dp)
                        dialog.show()
                        dialog.itemClickListener = { id, _ ->
                            when (id) {
                                collectId -> ReaderApplication.openActivity(CollectActivity::class)
                                importFileId -> performFileSearch()
                                scanDirId -> performDirSearch()
                                batchId -> {
                                    if (!viewModel.isLoadingLiveData.value) {
                                        setEditMode(true)
                                    }
                                }
                                settingId -> {
                                    getSingleApi<ISettingService>()?.openSettingActivity()
                                }
                            }
                        }
                    }

                }
            }
            BaseToolBar.VIEW_ID_LEFT_BUTTON -> {
                setEditMode(false)
            }
            VIEW_ID_ADD_FILE -> {
                performFileSearch()
            }
            VIEW_ID_SCAN_FOLDER -> {
                performDirSearch()
            }
            VIEW_ID_EDIT_MORE -> {

            }
            VIEW_ID_DELETE -> {
                val list = viewModel.getCheckItems()
                if (list.isNotEmpty()) {
                    activity?.run {
                        AlertDialog.Builder(this)
                            .setMessage("要删除这${list.size}项吗？")
                            .setPositiveButton("是") { _, _ ->
                                adapter?.uncheckAll()
                                BookManager.instance.deleteBooks(list.map { it.id }, true)
                                setEditMode(false)
                            }
                            .setNegativeButton("否", null)
                            .showWithNightMode()
                    }
                }
            }
        }
    }

    private fun setEditMode(enter: Boolean, anim: Boolean = true): Boolean {
        if (enter) {
            if (viewModel.list.isNotEmpty() && adapter?.enterEditMode(anim) == true) {
                viewModel.editModeLiveData.value = true
                return true
            }
        } else {
            if (adapter?.exitEditMode(anim) == true) {
                viewModel.editModeLiveData.value = false
                return true
            }
        }
        return false
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
        if (true) {
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

    override fun onItemClick(position: Int, view: View, editMode: Boolean) {
        if (position < 0 || position >= viewModel.list.size) {
            return
        }

        if (editMode) {
            adapter?.toggleCheck(position)
            return
        }

        val data = viewModel.list[position]
        val file = File(data.realPath)
        if (!file.exists()) {
            activity?.run {
                AlertDialog.Builder(this)
                    .setTitle("无法打开")
                    .setMessage("文件已被移动或删除，是否删除记录？")
                    .setPositiveButton("是") { _, _ ->
                        BookManager.instance.deleteBook(data.id)
                    }
                    .setNegativeButton("否", null)
                    .show()
            }
        } else {
            openBookFile = true
            ReaderActivity.openBookFile(data)
        }
    }

    override fun onItemLongClick(position: Int, view: View, editMode: Boolean): Boolean {
        if (editMode || position < 0 || position >= viewModel.list.size) {
            return false
        }
        val data = viewModel.list[position]
        activity?.apply {
            val menu = ReaderPopupMenu(this, view)

            val renameId = 1
            val deleteId = 5
            val collectId = 9
            val batchId = 15
            val shareId = 18

            menu.addItem(
                renameId,
                "重命名"
            )
            menu.addItem(
                deleteId,
                "删除"
            )
            if (data.collect) {
                menu.addItem(
                    collectId,
                    "取消收藏"
                )
            } else {
                menu.addItem(
                    collectId,
                    "收藏"
                )
            }
            menu.addItem(
                batchId,
                "批量管理"
            )
            menu.addItem(
                shareId,
                "分享"
            )
            menu.setIconEnable(true)
            menu.gravity = Gravity.RIGHT
            menu.show()

            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    collectId -> {
                        BookManager.instance.updateBookCollect(data.id, !data.collect)
                    }
                    deleteId -> {
                        AlertDialog.Builder(this)
                            .setMessage("要把“${data.filename}”从书架移除吗（不会删除导入原位置文件）？")
                            .setPositiveButton("是") { _, _ ->
                                BookManager.instance.deleteBook(data.id)
                            }
                            .setNegativeButton("否", null)
                            .show()
                    }
                    renameId -> {
                        activity?.run {
                            RenameDialog.show(supportFragmentManager, data)
                        }
                    }
                    batchId -> {
                        setEditMode(true)
                    }
                    shareId -> {
                        BookManager.instance.shareBookFile(data)
                    }
                }

                return@setOnMenuItemClickListener true
            }
        } ?: return false
        return true
    }

    override fun onItemCheckChange(position: Int, check: Boolean) {
        if (position < 0 || position >= viewModel.list.size) {
            return
        }
        val value = viewModel.list[position]
        viewModel.checkedIds.apply {
            if (check) {
                viewModel.checkedIds.add(value.id)
            } else {
                viewModel.checkedIds.remove(value.id)
            }
        }
        applyCheckCountChange()
    }

    override fun onItemCheckAllChange(checkAll: Boolean) {

    }

    override fun onInvisible() {
        super.onInvisible()
        setEditMode(enter = false, anim = false)
    }

    override fun onDestroy() {
        viewModel.afterDataUpdate = null
        super.onDestroy()
    }

    override fun onThisTabClick() {
        viewModel.refreshList()
    }

    override fun onBackPressed(): Boolean {
        if (setEditMode(false)) {
            return true
        }
        return false
    }
}
