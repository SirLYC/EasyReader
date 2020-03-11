package com.lyc.easyreader.bookshelf.secret

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lyc.easyreader.base.ReaderApplication
import com.lyc.easyreader.base.arch.provideViewModel
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.getDrawableRes
import com.lyc.easyreader.base.ui.theme.color_secondary_text
import com.lyc.easyreader.base.ui.widget.BaseToolBar
import com.lyc.easyreader.base.ui.widget.ReaderPopupMenu
import com.lyc.easyreader.base.ui.widget.SimpleToolbar
import com.lyc.easyreader.base.utils.changeToColor
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.dp2pxf
import com.lyc.easyreader.base.utils.rv.ReactiveAdapter
import com.lyc.easyreader.base.utils.statusBarBlackText
import com.lyc.easyreader.bookshelf.BookManager
import com.lyc.easyreader.bookshelf.R
import com.lyc.easyreader.bookshelf.RenameDialog
import com.lyc.easyreader.bookshelf.batch.BatchManageOption
import com.lyc.easyreader.bookshelf.batch.BookBatchManageActivity
import com.lyc.easyreader.bookshelf.reader.ReaderActivity
import com.lyc.easyreader.bookshelf.secret.settings.SecretSettingsActivity

/**
 * Created by Liu Yuchuan on 2020/2/14.
 */
class SecretActivity : BaseActivity(), View.OnClickListener, ReactiveAdapter.ItemClickListener {
    private lateinit var secretViewModel: SecretViewModel

    override fun beforeBaseOnCreate(savedInstanceState: Bundle?) {
        super.beforeBaseOnCreate(savedInstanceState)
        secretViewModel = provideViewModel()
        secretViewModel.refreshList()
    }

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {
        super.afterBaseOnCreate(savedInstanceState, rootView)
        val toolBar = SimpleToolbar(this, R.drawable.ic_settings_24dp)
        toolBar.setTitle("私密空间")
        toolBar.setBarClickListener(this)
        rootView.addView(toolBar, FrameLayout.LayoutParams(MATCH_PARENT, toolBar.getViewHeight()))
        val rv = RecyclerView(this)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = SecretListAdapter(secretViewModel.secretBookList).apply {
            observe(this@SecretActivity)
            itemClickListener = this@SecretActivity
        }
        rootView.addView(rv, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            topMargin = toolBar.getViewHeight()
        })

        val emptyView = LinearLayout(this)
        rootView.addView(emptyView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            topMargin = toolBar.getViewHeight()
        })
        emptyView.orientation = LinearLayout.VERTICAL
        emptyView.gravity = Gravity.CENTER
        val image = ImageView(this)
        image.scaleType = ImageView.ScaleType.CENTER_INSIDE
        image.setImageDrawable(getDrawableRes(R.drawable.ic_empty_box)?.apply {
            changeToColor(color_secondary_text)
        })
        emptyView.addView(image, LinearLayout.LayoutParams(dp2px(100), dp2px(100)))
        TextView(this).run {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(16f))
            setTextColor(color_secondary_text)
            gravity = Gravity.CENTER
            setPadding(dp2px(24))
            emptyView.addView(
                this, LinearLayout.LayoutParams(
                    MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            text = "还没有任何私密书籍"
        }

        secretViewModel.isRefreshingLiveData.observe(this, Observer {
            emptyView.isVisible =
                secretViewModel.secretBookList.isEmpty() && secretViewModel.firstRefreshFinish
            rv.isVisible =
                secretViewModel.secretBookList.isNotEmpty() && secretViewModel.firstRefreshFinish
        })
    }

    override fun onResume() {
        window.statusBarBlackText(true)
        super.onResume()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            BaseToolBar.VIEW_ID_LEFT_BUTTON -> onBackPressed()
            SimpleToolbar.VIEW_ID_RIGHT_BUTTON -> ReaderApplication.openActivity(
                SecretSettingsActivity::class
            )
        }
    }

    override fun onItemClick(position: Int, view: View, editMode: Boolean) {
        if (position < 0 || position >= secretViewModel.secretBookList.size) {
            return
        }
        ReaderActivity.openBookFile(secretViewModel.secretBookList[position])
    }

    override fun onItemLongClick(position: Int, view: View, editMode: Boolean): Boolean {
        if (editMode || position < 0 || position >= secretViewModel.secretBookList.size) {
            return false
        }
        val data = secretViewModel.secretBookList[position]
        val menu = ReaderPopupMenu(this, view)

        val secreteId = 1
        val renameId = 5
        val secretId = 11
        val batchId = 15
        val shareId = 18

        menu.addItem(
            secreteId,
            "无痕阅读"
        )
        menu.addItem(
            renameId,
            "重命名"
        )
        menu.addItem(secretId, "移除私密空间")
        menu.addItem(batchId, "批量管理")
        menu.addItem(
            shareId,
            "分享"
        )
        menu.setIconEnable(true)
        menu.gravity = Gravity.RIGHT
        menu.show()

        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                secreteId -> {
                    ReaderActivity.openBookFile(data, true)
                }
                renameId -> {
                    RenameDialog.show(supportFragmentManager, data)
                }
                secretId -> {
                    BookManager.instance.removeBooksFromSecret(listOf(data))
                }
                batchId -> {
                    BookBatchManageActivity.batchManageBooks(
                        secretViewModel.secretBookList,
                        arrayOf(
                            BatchManageOption.DELETE,
                            BatchManageOption.COLLECT,
                            BatchManageOption.CANCEL_COLLECT,
                            BatchManageOption.ADD_TO_SECRET
                        )
                    )
                }
                shareId -> {
                    BookManager.instance.shareBookFile(data)
                }
            }

            return@setOnMenuItemClickListener true
        }

        return true
    }
}
