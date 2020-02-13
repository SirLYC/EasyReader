package com.lyc.easyreader.bookshelf.reader

import android.app.Dialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.arch.provideViewModel
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.bottomsheet.BaseBottomSheet
import com.lyc.easyreader.base.ui.widget.BaseToolBar
import com.lyc.easyreader.base.utils.*
import com.lyc.easyreader.base.utils.rv.ReactiveAdapter
import com.lyc.easyreader.bookshelf.reader.settings.ReaderSettings
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Created by Liu Yuchuan on 2020/2/9.
 */
class ChapterDialog : BaseBottomSheet(), View.OnClickListener, ReactiveAdapter.ItemClickListener {
    companion object {
        const val TAG = "ChapterDialog"

        private val VIEW_ID_CHAPTER_LIST = generateNewViewId()
    }

    private lateinit var readerViewModel: ReaderViewModel
    private lateinit var reverseButton: TextView
    private lateinit var rv: RecyclerView

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

        val topBar = ChapterDialogTopBar(ctx)
        topBar.setBarClickListener(this)
        topBar.setTitle("目录")
        rootView.addView(topBar, FrameLayout.LayoutParams(MATCH_PARENT, topBar.getViewHeight()))
        reverseButton = topBar.rightButton

        rv = RecyclerView(ctx)
        rv.id = VIEW_ID_CHAPTER_LIST
        rv.layoutManager = LinearLayoutManager(ctx)
        rootView.addView(rv, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            topMargin = topBar.getViewHeight()
        })

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

        readerViewModel.chapterReverse.observe(this, Observer {
            reverseButton.text = if (it) "正序" else "逆序"
        })

        readerViewModel.bookChapterList.sizeLiveData.observe(this, Observer {
            applyChapterLoadingStateChange(
                readerViewModel.loadingChapterListLiveData.value,
                it == 0
            )
        })

        readerViewModel.loadingChapterListLiveData.observe(this, Observer {
            applyChapterLoadingStateChange(
                it,
                readerViewModel.bookChapterList.sizeLiveData.value == 0
            )
        })

        rv.adapter = BookChapterListAdapter(
            readerViewModel
        ).apply {
            itemClickListener = this@ChapterDialog
            readerViewModel.chapterReverse.observe(this@ChapterDialog, Observer {
                reverse = it
            })
            observe(this@ChapterDialog)
        }

        val currentChapter = readerViewModel.currentChapter.value
        if (currentChapter >= 0 && currentChapter < readerViewModel.bookChapterList.size) {
            rv.scrollToPosition(currentChapter)
        }
    }


    private fun applyChapterLoadingStateChange(isLoading: Boolean, isEmpty: Boolean) {
        rv.isVisible = !isLoading && !isEmpty
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            BaseToolBar.VIEW_ID_LEFT_BUTTON -> {
                dismiss()
            }

            ChapterDialogTopBar.VIEW_ID_CHAPTER_REVERSE -> {
                readerViewModel.chapterReverse.value = !readerViewModel.chapterReverse.value
            }
        }
    }

    override fun onItemClick(position: Int, view: View, editMode: Boolean) {
        LogUtils.d(TAG, "On chapter item click! Chapter pos=$position")
        dismiss()
        readerViewModel.changeChapterCall.value = position
    }

    override fun onItemLongClick(position: Int, view: View, editMode: Boolean): Boolean {
        return false
    }
}
