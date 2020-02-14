package com.lyc.easyreader.bookshelf.collect

import android.content.Intent
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
import com.lyc.easyreader.base.utils.changeToColor
import com.lyc.easyreader.base.utils.dp2px
import com.lyc.easyreader.base.utils.dp2pxf
import com.lyc.easyreader.base.utils.rv.ReactiveAdapter
import com.lyc.easyreader.base.utils.statusBarBlackText
import com.lyc.easyreader.bookshelf.BookManager
import com.lyc.easyreader.bookshelf.R
import com.lyc.easyreader.bookshelf.RenameDialog
import com.lyc.easyreader.bookshelf.reader.ReaderActivity

/**
 * Created by Liu Yuchuan on 2020/2/14.
 */
class CollectActivity : BaseActivity(), View.OnClickListener, ReactiveAdapter.ItemClickListener {
    private lateinit var collectViewModel: CollectViewModel

    companion object {
        fun start() {
            val context = ReaderApplication.appContext()
            context.startActivity(Intent(context, CollectActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    override fun beforeBaseOnCreate(savedInstanceState: Bundle?) {
        super.beforeBaseOnCreate(savedInstanceState)
        collectViewModel = provideViewModel()
        collectViewModel.refreshList()
    }

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {
        super.afterBaseOnCreate(savedInstanceState, rootView)
        val toolBar = BaseToolBar(this)
        toolBar.setTitle("收藏夹")
        toolBar.setBarClickListener(this)
        rootView.addView(toolBar, FrameLayout.LayoutParams(MATCH_PARENT, toolBar.getViewHeight()))
        val rv = RecyclerView(this)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = CollectItemAdapter(collectViewModel.collectBookList).apply {
            observe(this@CollectActivity)
            itemClickListener = this@CollectActivity
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
            text = "还没有任何收藏"
        }

        collectViewModel.isRefreshingLiveData.observe(this, Observer {
            emptyView.isVisible =
                collectViewModel.collectBookList.isEmpty() && collectViewModel.firstRefreshFinish
            rv.isVisible =
                collectViewModel.collectBookList.isNotEmpty() && collectViewModel.firstRefreshFinish
        })
    }

    override fun onResume() {
        window.statusBarBlackText(true)
        super.onResume()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            BaseToolBar.VIEW_ID_LEFT_BUTTON -> onBackPressed()
        }
    }

    override fun onItemClick(position: Int, view: View, editMode: Boolean) {
        if (position < 0 || position >= collectViewModel.collectBookList.size) {
            return
        }
        ReaderActivity.openBookFile(collectViewModel.collectBookList[position])
    }

    override fun onItemLongClick(position: Int, view: View, editMode: Boolean): Boolean {
        if (editMode || position < 0 || position >= collectViewModel.collectBookList.size) {
            return false
        }
        val data = collectViewModel.collectBookList[position]
        val menu = ReaderPopupMenu(this, view)

        val renameId = 1
        val collectId = 9
        val shareId = 18

        menu.addItem(
            renameId,
            "重命名"
        )
        menu.addItem(
            collectId,
            "取消收藏"
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
                    BookManager.instance.updateBookCollect(data.id, false)
                }
                renameId -> {
                    RenameDialog.show(supportFragmentManager, data)
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