package com.lyc.easyreader.bookshelf.reader.bookmark

import android.app.Dialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
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
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.arch.provideViewModel
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.bottomsheet.BaseBottomSheet
import com.lyc.easyreader.base.ui.bottomsheet.LinearDialogBottomSheet
import com.lyc.easyreader.base.ui.getDrawableRes
import com.lyc.easyreader.base.ui.widget.BaseToolBar
import com.lyc.easyreader.base.utils.*
import com.lyc.easyreader.base.utils.rv.ReactiveAdapter
import com.lyc.easyreader.bookshelf.R
import com.lyc.easyreader.bookshelf.db.BookShelfOpenHelper
import com.lyc.easyreader.bookshelf.reader.ReaderViewModel
import com.lyc.easyreader.bookshelf.reader.settings.ReaderSettings
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Created by Liu Yuchuan on 2020/2/9.
 */
class BookMarkDialog : BaseBottomSheet(), View.OnClickListener, ReactiveAdapter.ItemClickListener {
    companion object {
        const val TAG = "BookMarkDialog"

        private val VIEW_ID_CHAPTER_LIST = generateNewViewId()
    }

    private lateinit var readerViewModel: ReaderViewModel
    private lateinit var clearAllButton: TextView
    private lateinit var rv: RecyclerView
    private lateinit var emptyView: View

    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val ctx = ReaderApplication.appContext()
        val rootView = FrameLayout(ctx)
        rootView.elevation = dp2pxf(4f)
        rootView.background = GradientDrawable().apply {
            setColor(ReaderSettings.currentPageStyle.bgColor)
            val r = dp2pxf(16f)
            cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f)
        }

        val topBar = BookMarkTopBar(ctx)
        topBar.setBarClickListener(this)
        rootView.addView(topBar, FrameLayout.LayoutParams(MATCH_PARENT, topBar.getViewHeight()))
        clearAllButton = topBar.rightButton
        rv = RecyclerView(ctx)
        rv.id = VIEW_ID_CHAPTER_LIST
        rv.layoutManager = LinearLayoutManager(ctx)
        rootView.addView(rv, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            topMargin = topBar.getViewHeight()
        })

        val emptyView = LinearLayout(ctx)
        rootView.addView(emptyView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            topMargin = topBar.getViewHeight()
        })
        emptyView.gravity = Gravity.CENTER
        emptyView.setPadding(dp2px(32))
        emptyView.orientation = LinearLayout.VERTICAL
        val emptyContentColor =
            ReaderSettings.currentPageStyle.fontColor.addColorAlpha((0xFF * 0.8f).toInt())
        emptyView.addView(ImageView(ctx).apply {
            setImageDrawable(getDrawableRes(R.drawable.ic_sleeping_cat)?.apply {
                changeToColor(
                    emptyContentColor
                )
            })
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }, LinearLayout.LayoutParams(dp2px(200), dp2px(183)))
        emptyView.addView(TextView(ctx).apply {
            setTextColor(emptyContentColor)
            textSizeInDp = 16f
            gravity = Gravity.CENTER
            text = "没有书签~"
        }, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            topMargin = dp2px(32)
        })
        this.emptyView = emptyView

        return rootView
    }

    override fun changeWindowAndDialogAfterSetContent(dialog: Dialog, window: Window) {
        super.changeWindowAndDialogAfterSetContent(dialog, window)
        val lp = window.attributes
        if (getScreenOrientation() % 180 == 0) {
            lp.height = max((deviceHeight() * 0.75f).roundToInt(), dp2px(480))
        } else {
            lp.height = max((deviceHeight() * 0.75f).roundToInt(), dp2px(270))
        }
        window.attributes = lp
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(0.5f)
    }

    override fun onResume() {
        dialog?.window?.statusBarBlackText(ReaderSettings.currentPageStyle.statusBarBlack)
        super.onResume()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        readerViewModel = (activity as BaseActivity).provideViewModel()

        readerViewModel.loadBookMarksIfNeeded()

        readerViewModel.bookMarkList.sizeLiveData.observe(this, Observer {
            applyChapterLoadingStateChange(
                readerViewModel.loadingChapterListLiveData.value,
                it == 0
            )

            clearAllButton.isVisible = it != 0
        })

        readerViewModel.loadingChapterListLiveData.observe(this, Observer {
            applyChapterLoadingStateChange(
                it,
                readerViewModel.bookMarkList.sizeLiveData.value == 0
            )
        })

        rv.adapter = BookMarkAdapter(readerViewModel.bookMarkList).apply {
            observe(this@BookMarkDialog)
            itemClickListener = this@BookMarkDialog
        }
    }

    private fun applyChapterLoadingStateChange(isLoading: Boolean, isEmpty: Boolean) {
        rv.isVisible = !isLoading && !isEmpty
        emptyView.isVisible = !isLoading && isEmpty
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            BaseToolBar.VIEW_ID_LEFT_BUTTON -> {
                dismiss()
            }

            BookMarkTopBar.VIEW_ID_DELETE_ALL -> {
                readerViewModel.bookFileLiveData.value?.let {
                    activity?.run {
                        AlertDialog.Builder(this)
                            .setMessage("清空这本书的书签列表？")
                            .setPositiveButton("是") { _, _ ->
                                BookShelfOpenHelper.instance.deleteBookMarksFor(it.id)
                            }
                            .setNegativeButton("否", null)
                            .showWithNightMode()
                    }
                }
            }
        }
    }

    override fun onItemClick(position: Int, view: View, editMode: Boolean) {
        if (position < 0 || position >= readerViewModel.bookMarkList.size) {
            dismiss()
            return
        }
        val bookMark = readerViewModel.bookMarkList[position]
        if (bookMark.chapter < 0 || bookMark.offsetStart < 0 || bookMark.offsetEnd < 0 || bookMark.offsetStart > bookMark.offsetEnd) {
            dismiss()
            return
        }
        readerViewModel.skipBookMarkCall.value =
            Triple(bookMark.chapter, bookMark.offsetStart, bookMark.offsetEnd)
        dismiss()
    }

    override fun onItemLongClick(position: Int, view: View, editMode: Boolean): Boolean {
        if (position < 0 || position >= readerViewModel.bookMarkList.size) {
            dismiss()
            return false
        }
        val bookMark = readerViewModel.bookMarkList[position]
        val context = activity
        if (context == null) {
            dismiss()
            return false
        }
        val bottomSheet = LinearDialogBottomSheet(context)
        bottomSheet.bgColor = ReaderSettings.currentPageStyle.bgColor
        val deleteId = bottomSheet.addItem("删除", color = ReaderSettings.currentPageStyle.fontColor)
        val jumpId = bottomSheet.addItem("跳转至", color = ReaderSettings.currentPageStyle.fontColor)
        bottomSheet.itemClickListener = { id, _ ->
            when (id) {
                deleteId -> {
                    BookShelfOpenHelper.instance.deleteBookMark(bookMark)
                }
                jumpId -> {
                    onItemClick(position, view, editMode)
                }
            }
        }
        bottomSheet.show()
        return true
    }
}
