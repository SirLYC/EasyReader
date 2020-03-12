package com.lyc.easyreader.bookshelf.scan

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lyc.easyreader.base.arch.provideViewModel
import com.lyc.easyreader.base.ui.BaseActivity
import com.lyc.easyreader.base.ui.theme.color_light_blue
import com.lyc.easyreader.base.ui.theme.color_primary_text
import com.lyc.easyreader.base.ui.widget.BaseToolBar
import com.lyc.easyreader.base.utils.*
import com.lyc.easyreader.bookshelf.KEY_SELECT_SCAN_FILES

/**
 * Created by Liu Yuchuan on 2020/1/24.
 */
class BookScanActivity : BaseActivity(), View.OnClickListener,
    PositionSelectController.PositionSelectListener {
    private lateinit var viewModel: BookScanViewModel
    private lateinit var scanningBottomBar: View
    private lateinit var toolBottomBar: View
    private lateinit var scanningTv: TextView
    private lateinit var importButton: TextView
    private lateinit var selectAllButton: TextView
    private lateinit var selectController: PositionSelectController

    private var toolBarHeight: Int = 0

    companion object {
        fun start(activity: Activity, uri: Uri, requestCode: Int) {
            val intent = Intent(activity, BookScanActivity::class.java).apply {
                data = uri
            }
            activity.startActivityForResult(intent, requestCode)
        }

        const val SCANNING_TEXT_FORMAT = "扫描到%d个文件"

        const val IMPORT_BUTTON_TEXT_FORMAT = "导入%d项"

        private val VIEW_ID_STOP_SCAN = generateNewViewId()

        private val VIEW_ID_IMPORT = generateNewViewId()

        private val VIEW_ID_CHANGE_SELECT_ALL = generateNewViewId()

        private val VIEW_ID_RV = generateNewViewId()

        const val TAG = "BookScanActivity"
    }

    override fun beforeBaseOnCreate(savedInstanceState: Bundle?) {
        super.beforeBaseOnCreate(savedInstanceState)
        viewModel = provideViewModel()
        if (savedInstanceState == null) {
            viewModel.uri = intent?.data
        } else if (!isCreateFromConfigChange) {
            viewModel.restoreState(savedInstanceState)
        }
        viewModel.startScan()
        selectController = viewModel.selectController
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isChangingConfigurations) {
            viewModel.saveState(outState)
        }
    }

    override fun afterBaseOnCreate(savedInstanceState: Bundle?, rootView: FrameLayout) {
        super.afterBaseOnCreate(savedInstanceState, rootView)
        val toolBar = BaseToolBar(this)
        toolBar.setBarClickListener(this)
        toolBar.setTitle("扫描本地书籍")
        toolBarHeight = toolBar.getViewHeight()
        rootView.addView(toolBar, FrameLayout.LayoutParams(MATCH_PARENT, toolBarHeight))

        val recyclerView = RecyclerView(this)
        recyclerView.setBackgroundColor(Color.WHITE)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = BookScanAdapter(viewModel.list, selectController).also {
            it.observe(this)
        }
        recyclerView.id = VIEW_ID_RV

        val dp48 = dp2px(48)
        val refreshLayout = SwipeRefreshLayout(this)
        refreshLayout.addView(recyclerView)
        refreshLayout.isEnabled = false
        rootView.addView(refreshLayout, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            topMargin = toolBarHeight
            bottomMargin = dp48
        })

        val dp16 = dp2px(16)
        scanningBottomBar = FrameLayout(this).apply {
            elevation = dp2pxf(4f)
            setBackgroundColor(Color.WHITE)
            setPadding(dp16, 0, 0, 0)
            val textView = TextView(this@BookScanActivity)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(14f))
            textView.gravity = Gravity.CENTER or Gravity.LEFT
            textView.setTextColor(color_primary_text)
            addView(textView)
            scanningTv = textView

            val dp8 = dp2px(8)
            val stopButton = TextView(this@BookScanActivity)
            stopButton.setPadding(dp8)
            stopButton.id = VIEW_ID_STOP_SCAN
            stopButton.text = "停止扫描"
            stopButton.gravity = Gravity.CENTER
            stopButton.paint.isFakeBoldText = true
            stopButton.background = buildCommonButtonBg(Color.RED, true)
            stopButton.elevation = dp2pxf(8f)
            stopButton.setTextColor(buildCommonButtonTextColor(Color.RED))
            stopButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(14f))
            stopButton.setOnClickListener(this@BookScanActivity)
            addView(stopButton, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.RIGHT or Gravity.CENTER
                rightMargin = dp16
            })
        }
        rootView.addView(scanningBottomBar, FrameLayout.LayoutParams(MATCH_PARENT, dp48).apply {
            gravity = Gravity.BOTTOM
        })

        toolBottomBar = FrameLayout(this).apply {

            elevation = dp2pxf(4f)
            setBackgroundColor(Color.WHITE)

            val dp8 = dp2px(8)
            val importButton = TextView(this@BookScanActivity)
            importButton.setPadding(dp8)
            importButton.id = VIEW_ID_IMPORT
            importButton.text = IMPORT_BUTTON_TEXT_FORMAT
            importButton.gravity = Gravity.CENTER
            importButton.paint.isFakeBoldText = true
            importButton.background = buildCommonButtonBg(color_light_blue)
            importButton.elevation = dp2pxf(8f)
            importButton.setTextColor(buildCommonButtonTextColor(Color.WHITE))
            importButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(14f))
            importButton.setOnClickListener(this@BookScanActivity)
            addView(importButton, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.RIGHT or Gravity.CENTER
                rightMargin = dp16
            })
            this@BookScanActivity.importButton = importButton

            val selectAllButton = TextView(this@BookScanActivity)
            selectAllButton.setPadding(dp8)
            selectAllButton.id = VIEW_ID_CHANGE_SELECT_ALL
            selectAllButton.text = "全选"
            selectAllButton.gravity = Gravity.CENTER
            selectAllButton.paint.isFakeBoldText = true
            selectAllButton.background = buildCommonButtonBg(color_light_blue)
            selectAllButton.elevation = dp2pxf(8f)
            selectAllButton.setTextColor(buildCommonButtonTextColor(Color.WHITE))
            selectAllButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp2pxf(14f))
            selectAllButton.setOnClickListener(this@BookScanActivity)
            addView(selectAllButton, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.LEFT or Gravity.CENTER
                leftMargin = dp16
            })
            this@BookScanActivity.selectAllButton = selectAllButton
        }
        rootView.addView(toolBottomBar, FrameLayout.LayoutParams(MATCH_PARENT, dp48).apply {
            gravity = Gravity.BOTTOM
        })

        selectController.addListener(this)

        viewModel.list.sizeLiveData.observe(this, Observer { size ->
            scanningTv.text = SCANNING_TEXT_FORMAT.format(size)
        })

        viewModel.scanningLiveData.observe(this, Observer { scanning ->
            refreshLayout.isRefreshing = scanning
            scanningBottomBar.isVisible = scanning
            toolBottomBar.isVisible = !scanning
            if (!scanning) {
                applySelectChange()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.changeActivityVisibility(true)
        window.statusBarBlackText(true)
    }

    override fun onStop() {
        viewModel.changeActivityVisibility(false)
        super.onStop()
    }

    override fun onDestroy() {
        selectController.removeListener(this)
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            BaseToolBar.VIEW_ID_LEFT_BUTTON -> {
                finish()
            }

            VIEW_ID_STOP_SCAN -> {
                viewModel.stopScan()
            }

            VIEW_ID_CHANGE_SELECT_ALL -> {
                if (selectController.isSelectAll(viewModel.list.size)) {
                    selectController.unSelectAll()
                } else {
                    selectController.selectAll(viewModel.list.size)
                }
            }

            VIEW_ID_IMPORT -> {
                val uris = viewModel.list.map {
                    (it as BookScanItem).uri!!
                }.filterIndexed { index, _ -> selectController.selectContains(index) }
                    .toTypedArray()
                if (uris.isNotEmpty()) {
                    setResult(RESULT_OK, Intent().apply {
                        putExtra(KEY_SELECT_SCAN_FILES, uris)
                    })
                }
                finish()
            }
        }
    }


    private fun applySelectChange() {
        val cnt = selectController.selectCount()
        importButton.text = IMPORT_BUTTON_TEXT_FORMAT.format(cnt)
        importButton.isEnabled = cnt > 0
        selectAllButton.isVisible = viewModel.hasFile()
        selectAllButton.text =
            if (selectController.isSelectAll(viewModel.list.size)) "取消全选" else "全选"
    }


    override fun onPositionSelect(position: Int, select: Boolean) {
        applySelectChange()
    }

    override fun onSelectAllChange(select: Boolean) {
        applySelectChange()
    }
}
