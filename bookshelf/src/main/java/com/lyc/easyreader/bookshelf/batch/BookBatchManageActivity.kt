package com.lyc.easyreader.bookshelf.batch

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.arch.provideViewModel
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.getDrawableRes
import com.lyc.easyreader.base.ui.theme.color_primary_text
import com.lyc.easyreader.base.ui.widget.BaseToolBar
import com.lyc.easyreader.base.ui.widget.SimpleToolbar
import com.lyc.easyreader.base.utils.*
import com.lyc.easyreader.base.utils.rv.ReactiveAdapter
import com.lyc.easyreader.bookshelf.R
import com.lyc.easyreader.bookshelf.db.BookShelfBook
import com.lyc.easyreader.bookshelf.secret.SecretManager

/**
 * Created by Liu Yuchuan on 2020/3/11.
 */
class BookBatchManageActivity : BaseActivity(), View.OnClickListener,
    ReactiveAdapter.ItemCheckListener, ReactiveAdapter.ItemClickListener {
    companion object {
        private const val TAG = "BookBatchActivity"

        private const val KEY_BOOK_FILES = "KEY_BOOK_FILES"
        private const val KEY_OPTIONS = "KEY_OPTIONS"

        private val VIEW_ID_BAR_DELETE = generateNewViewId()
        private val VIEW_ID_BAR_COLLECT = generateNewViewId()
        private val VIEW_ID_BAR_CANCEL_COLLECT = generateNewViewId()
        private val VIEW_ID_BAR_ADD_TO_SECRET = generateNewViewId()
        private val VIEW_ID_BAR_REMOVE_FROM_SECRET = generateNewViewId()

        fun batchManageBooks(
            books: List<BookShelfBook>,
            options: Array<BatchManageOption> = BatchManageOption.values()
        ) {
            if (books.isEmpty()) {
                return
            }

            val context = ReaderApplication.appContext()
            val intent = Intent(context, BookBatchManageActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(KEY_BOOK_FILES, ArrayList(books))
                putExtra(KEY_OPTIONS, options.map { it.name }.toTypedArray())
            }
            context.startActivity(intent)
        }
    }

    private var adapter: ReactiveAdapter? = null
    private var toolbar: SimpleToolbar? = null
    private var optionBarLine: OptionBarLine? = null
    private lateinit var viewModel: BookBatchManageViewModel

    private var optionDeleteIdx = -1
    private var optionCollectIdx = -1
    private var optionCancelCollectIdx = -1
    private var optionAddToSecretIdx = -1
    private var optionRemoveFromSecretIdx = -1

    private val optionButtonIdxs = hashSetOf<Int>()

    override fun beforeBaseOnCreate(savedInstanceState: Bundle?) {
        viewModel = provideViewModel()
        if (savedInstanceState != null && !isCreateFromConfigChange) {
            LogUtils.i(TAG, "Activity restore not from configure change, just finish it.")
            finish()
            return
        }

        intent?.getParcelableArrayListExtra<BookFile>(KEY_BOOK_FILES)?.let {
            viewModel.list.addAll(it)
        }

        if (viewModel.list.isEmpty()) {
            finish()
            return
        }
    }

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {
        if (viewModel.list.isEmpty()) {
            return
        }

        val toolbar = SimpleToolbar(this, R.drawable.ic_select)
        toolbar.setTitle("批量管理")
        toolbar.setBarClickListener(this)
        rootView.addView(
            toolbar,
            FrameLayout.LayoutParams(MATCH_PARENT, toolbar.getViewHeight())
        )
        this.toolbar = toolbar

        val options =
            intent?.getStringArrayExtra(KEY_OPTIONS)?.map { BatchManageOption.valueOf(it) }?.toSet()
                ?: BatchManageOption.values().toSet()

        val bottomBar = OptionBarLine(this).also { this.optionBarLine = it }
        rootView.addView(
            bottomBar,
            FrameLayout.LayoutParams(MATCH_PARENT, OptionBarLine.HEIGHT).apply {
                gravity = Gravity.BOTTOM
            }
        )

        var currentIdx = 0

        if (options.contains(BatchManageOption.DELETE)) {
            optionButtonIdxs.add(currentIdx)
            bottomBar.addButton(OptionBarButton(this, R.drawable.ic_delete_24dp, "删除").apply {
                id = VIEW_ID_BAR_DELETE
                setOnClickListener(this@BookBatchManageActivity)
            })
            optionDeleteIdx = currentIdx++
        }

        if (options.contains(BatchManageOption.COLLECT)) {
            optionButtonIdxs.add(currentIdx)
            bottomBar.addButton(OptionBarButton(this, R.drawable.ic_star_24dp, "收藏").apply {
                id = VIEW_ID_BAR_COLLECT
                setOnClickListener(this@BookBatchManageActivity)
            })
            optionCollectIdx = currentIdx++
        }

        if (options.contains(BatchManageOption.CANCEL_COLLECT)) {
            optionButtonIdxs.add(currentIdx)
            bottomBar.addButton(
                OptionBarButton(
                    this,
                    R.drawable.ic_star_border_24dp,
                    "移除收藏"
                ).apply {
                    id = VIEW_ID_BAR_CANCEL_COLLECT
                    setOnClickListener(this@BookBatchManageActivity)
                })
            optionCancelCollectIdx = currentIdx++
        }

        if (options.contains(BatchManageOption.ADD_TO_SECRET)) {
            optionButtonIdxs.add(currentIdx)
            bottomBar.addButton(OptionBarButton(this, R.drawable.ic_package, "加入私密").apply {
                id = VIEW_ID_BAR_ADD_TO_SECRET
                setOnClickListener(this@BookBatchManageActivity)
            })
            optionAddToSecretIdx = currentIdx++
        }

        if (options.contains(BatchManageOption.REMOVE_FROM_SECRET)) {
            optionButtonIdxs.add(currentIdx)
            bottomBar.addButton(OptionBarButton(this, R.drawable.ic_package, "移除私密").apply {
                id = VIEW_ID_BAR_REMOVE_FROM_SECRET
                setOnClickListener(this@BookBatchManageActivity)
            })
            optionRemoveFromSecretIdx = currentIdx
        }


        val rv = RecyclerView(this)
        rootView.addView(
            rv,
            FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                topMargin = toolbar.getViewHeight()
                bottomMargin = OptionBarLine.HEIGHT
            })
        rv.layoutManager = LinearLayoutManager(this)
        adapter = BatchBookListAdapter(viewModel.list).apply {
            enterEditMode(anim = false)
            rv.adapter = this
            observe(this@BookBatchManageActivity)
            itemCheckListener = this@BookBatchManageActivity
            itemClickListener = this@BookBatchManageActivity
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
            checkAllLiveData.observe(this@BookBatchManageActivity, Observer {
                toolbar.rightButton.setImageDrawable(if (it) selectAllDrawable else selectDrawable)
            })
            adapter = this
        }
        applyCheckCountChange()
    }

    override fun onResume() {
        window.statusBarBlackText(true)
        super.onResume()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            BaseToolBar.VIEW_ID_LEFT_BUTTON -> onBackPressed()
            SimpleToolbar.VIEW_ID_RIGHT_BUTTON -> adapter?.toggleCheckAll()
            VIEW_ID_BAR_DELETE -> handleDeleteClick()
            VIEW_ID_BAR_COLLECT -> handleCollectClick()
            VIEW_ID_BAR_CANCEL_COLLECT -> handleCancelCollectClick()
            VIEW_ID_BAR_ADD_TO_SECRET -> handleAddToSecretClick()
            VIEW_ID_BAR_REMOVE_FROM_SECRET -> handleRemoveFromSecretClick()
        }
    }

    private fun handleDeleteClick() {
        if (viewModel.checkedIds.isEmpty()) {
            return
        }
        AlertDialog.Builder(this)
            .setMessage("要删除这${viewModel.checkedIds.size}项吗？")
            .setPositiveButton("是") { _, _ ->
                if (viewModel.deleteCheckedIds()) {
                    finish()
                } else {
                    adapter?.uncheckAll()
                }
            }
            .setNegativeButton("否", null)
            .showWithNightMode()
    }

    private fun handleCollectClick() {
        if (viewModel.checkedIds.isEmpty()) {
            return
        }
        viewModel.changeCheckIdsCollect(true)
        adapter?.uncheckAll()
    }

    private fun handleCancelCollectClick() {
        if (viewModel.checkedIds.isEmpty()) {
            return
        }
        viewModel.changeCheckIdsCollect(false)
        adapter?.uncheckAll()
    }

    private fun handleAddToSecretClick() {
        if (viewModel.checkedIds.isEmpty()) {
            return
        }
        val set = viewModel.checkedIds.toSet()
        if (SecretManager.addBooksToSecret(viewModel.list.filter { set.contains(it.id) })) {
            adapter?.uncheckAll()
        }
    }

    private fun handleRemoveFromSecretClick() {
        if (viewModel.checkedIds.isEmpty()) {
            return
        }
        viewModel.removeCheckedIdsFromSecret()
        adapter?.uncheckAll()
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

    private fun applyCheckCountChange() {
        val checkCount = adapter?.checkCount() ?: 0
        toolbar?.run {
            if (checkCount > 0) {
                setTitle("已选择${checkCount}项")
            } else {
                setTitle("批量管理")
            }
        }
        optionBarLine?.run {
            optionButtonIdxs.forEach {
                getButtonAtIndex(it)?.isEnabled = checkCount != 0
            }
        }
    }

    override fun onItemClick(position: Int, view: View, editMode: Boolean) {
        if (position < 0 || position >= viewModel.list.size || !editMode) {
            return
        }
        adapter?.toggleCheck(position)
    }
}
