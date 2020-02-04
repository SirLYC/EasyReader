package com.lyc.easyreader.bookshelf.reader

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.arch.provideViewModel
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.bookshelf.reader.page.PageLoader
import com.lyc.easyreader.bookshelf.reader.page.PageView

/**
 * Created by Liu Yuchuan on 2020/1/30.
 */
class ReaderActivity : BaseActivity(), PageView.TouchListener {
    companion object {
        private const val KEY_BOOK_FILE = "KEY_BOOK_FILE"

        fun openBookFile(bookFile: BookFile) {
            if (bookFile.id == null) {
                ReaderToast.showToast("记录不存在")
                return
            }
            val context = ReaderApplication.appContext()
            val intent = Intent(context, ReaderActivity::class.java).apply {
                putExtra(KEY_BOOK_FILE, bookFile)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var viewModel: ReaderViewModel
    private var pageLoader: PageLoader? = null
    private var pageView: PageView? = null

    override fun beforeBaseOnCreate(savedInstanceState: Bundle?) {
        viewModel = provideViewModel()
        if (savedInstanceState == null) {
            intent?.getParcelableExtra<BookFile>(KEY_BOOK_FILE)?.run {
                viewModel.init(this)
            }
        } else if (!isCreateFromConfigChange) {
            viewModel.restoreState(savedInstanceState)
        }
        if (viewModel.bookFile == null) {
            ReaderToast.showToast("打开文件失败，请重试")
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.saveState(outState)
    }

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {
        if (viewModel.bookFile == null) {
            return
        }

        val page = PageView(this)
        pageView = page
        page.setTouchListener(this)
        rootView.addView(page)
        val loader = page.getPageLoader(viewModel.bookFile)
        viewModel.loadingChapterListLiveData.observe(this, Observer { loading ->
            if (!loading) {
                loader.setChapterListIfEmpty(viewModel.bookChapterList)
            }
        })
    }

    override fun prePage() {

    }

    override fun onTouch(): Boolean {
        return true
    }

    override fun center() {

    }

    override fun cancel() {
    }

    override fun nextPage() {
        pageLoader?.skipToNextPage()
    }
}
