package com.lyc.easyreader.bookshelf.reader.page;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextPaint;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import com.lyc.common.thread.ExecutorFactory;
import com.lyc.easyreader.api.book.BookChapter;
import com.lyc.easyreader.api.book.BookFile;
import com.lyc.easyreader.base.ui.ReaderResourcesKt;
import com.lyc.easyreader.base.ui.theme.NightModeManager;
import com.lyc.easyreader.base.utils.DeviceUtilsKt;
import com.lyc.easyreader.base.utils.LogUtils;
import com.lyc.easyreader.base.utils.ViewUtilsKt;
import com.lyc.easyreader.bookshelf.reader.BookChapterCache;
import com.lyc.easyreader.bookshelf.reader.BookChapterWrapper;
import com.lyc.easyreader.bookshelf.reader.SelectTextTabDrawable;
import com.lyc.easyreader.bookshelf.reader.page.anim.PageAnimMode;
import com.lyc.easyreader.bookshelf.utils.StringUtilsKt;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Liu Yuchuan on 2020/2/2.
 */
public class PageLoader implements Handler.Callback {
    public static final int TEXT_SIZE_MIN_VALUE_DP = 12;
    public static final int TEXT_SIZE_MAX_VALUE_DP = 32;
    // 当前页面的状态
    public static final int STATUS_LOADING = 1;         // 正在加载
    public static final int STATUS_FINISH = 2;          // 加载完成
    public static final int STATUS_ERROR = 3;           // 加载错误 (一般是网络加载情况)
    public static final int STATUS_EMPTY = 4;           // 空数据
    public static final int STATUS_PARING = 5;          // 正在解析 (装载本地数据)
    public static final int STATUS_PARSE_ERROR = 6;     // 本地文件解析错误(暂未被使用)
    public static final int STATUS_CATEGORY_EMPTY = 7;  // 获取到的目录为空
    private static final int MSG_RELOAD_PAGES = 1;

    private static final String TAG = "PageLoader";

    // 缓存已经从文件系统读取的章节
    private BookChapterCache chapterCache = new BookChapterCache();

    private static final int TEXT_SIZE_MIN_VALUE = DeviceUtilsKt.dp2px(TEXT_SIZE_MIN_VALUE_DP);
    private static final int TEXT_SIZE_MAX_VALUE = DeviceUtilsKt.dp2px(TEXT_SIZE_MAX_VALUE_DP);

    private static final int TIP_ALPHA = (int) (0xff * 0.5f + 0.5f);

    // 右下角电池和时间显示间距
    private static final int MARGIN_CHARGE_TIME = DeviceUtilsKt.dp2px(8);

    private static final int CHARGE_WIDTH = 64;
    private static final int CHARGE_HEIGHT = 64;

    // 下方小部件
    private static final int WIDGET_MARGIN_BOTTOM = DeviceUtilsKt.dp2px(4);
    private static final int TINY_CHAPTER_MARGIN = DeviceUtilsKt.dp2px(4);
    // 下方小部件的高度
    private float widgetMaxHeight;

    // 当前章节列表
    private final List<BookChapter> chapterList;
    // 书本对象
    protected BookFile bookFile;
    // 监听器
    private OnPageChangeListener pageChangeListener;

    private float lastTimeTextX;
    // 绘制电池的画笔
    private final Paint batteryPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    // 判断章节列表是否加载完成
    private boolean isChapterListPrepare;
    // 当前章
    private int curChapterPos = 0;
    // 页面显示类
    private PageView pageView;
    // 当前显示的页
    private BookPage curPage;
    // 上一章的页面列表缓存
    private List<BookPage> prePageList;
    // 上一章的页面列表缓存实际位置
    private int prePageListPos;
    // 当前章节的页面列表
    private List<BookPage> curPageList;
    // 下一章的页面列表缓存
    private List<BookPage> nextPageList;
    // 下一章页面列表缓存实际位置
    private int nextPageListPos;
    // 期望在当前章节中的翻页
    private int pendingPosInChapter = -1;
    // 期望在当前章节中偏移量（字符数量）
    private int pendingOffsetStart = -1;
    private int pendingOffsetEnd = -1;
    private final RectF polarRect = new RectF();
    private final RectF batteryFrameRect = new RectF();
    private final RectF batteryCapRect = new RectF();
    // 绘制提示的画笔
    private final Paint tipPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    // 绘制标题的画笔
    private final Paint titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    // 绘制背景颜色的画笔(用来擦除需要重绘的部分)
    private final Paint bgPaint = new Paint();
    // 绘制小说内容的画笔
    private final TextPaint contentTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Rect chargeBounds = new Rect();
    // 阅读器的配置选项
    // 被遮盖的页，或者认为被取消显示的页
    private BookPage mCancelPage;
    // 是否打开过章节
    private boolean isChapterOpen;
    private boolean triedOpen = false;
    // 页面的翻页效果模式
    private PageAnimMode pageMode = PageAnimMode.SIMULATION;
    // 加载器的颜色主题
    private PageStyle pageStyle = PageStyle.BG_1;
    // 当前是否是夜间模式
    private boolean isNightMode;
    // 当前的状态
    private int status = STATUS_PARING;
    // 书籍绘制区域的宽高
    private int contentWidth;
    private int contentHeight;
    // 应用的宽高
    private int viewWidth;
    // 间距
    private int marginLeft = DeviceUtilsKt.dp2px(16);
    private int marginRight = DeviceUtilsKt.dp2px(16);
    private int marginTop = DeviceUtilsKt.dp2px(16);
    private int marginBottom = DeviceUtilsKt.dp2px(8);
    // 字体的颜色
    private int textColor;
    // 标题的大小
    private int titleTextSize;
    // 字体的大小
    private int contentTextSize;
    // 下方小字大小
    private int tipTextSize;
    // 行间距系数
    private float lineSpaceFactor;
    // 行间距
    private int textInterval;
    // 标题的行间距
    private int titleInterval;
    // 段间距系数
    private float paraSpaceFactor;
    // 段落距离(基于行间距的额外距离)
    private int textPara;
    private int titlePara;
    private int viewHeight;
    // 电池的百分比
    private boolean isCharging;
    // 当前页面的背景
    private int bgColor;
    // 上一章的记录
    private int lastChapterPos;
    // 缩进
    private int indentCount;
    private boolean indentFull;
    private String indentString;
    private boolean fontBold = false;
    private int batteryLevel;
    private Drawable chargeDrawable;

    private final Paint.FontMetrics finalFontMetrics = new Paint.FontMetrics();
    private PageCharPositions pageCharPositions;
    private final int hitTestSlop = DeviceUtilsKt.dp2px(8);
    private final float halfTabSize = DeviceUtilsKt.dp2pxf(12f);
    private int selectColor;
    private boolean isSelectMode;
    private int[] selectIndexes = new int[2];
    private SelectTextTabDrawable[] selectTextTabDrawables = {new SelectTextTabDrawable(), new SelectTextTabDrawable()};
    private int hitTestIndex = 0;

    private Handler handler = new Handler(this);

    private boolean drawTextBound;
    private boolean isDragTab = false;
    private boolean canOperateSelectText = false;

    PageLoader(PageView pageView, BookFile bookFile) {
        this.pageView = pageView;
        this.bookFile = bookFile;
        chapterList = new ArrayList<>();
        setIndent(2, true);
        lineSpaceFactor = 0.5f;
        paraSpaceFactor = 1.0f;
        // 初始化数据
        setUpTextParams(DeviceUtilsKt.dp2px(16));
        // 初始化画笔
        initPaint();
        lastChapterPos = curChapterPos;
    }

    void setCanOperateSelectTextAndNotify(boolean canOperateSelectText) {
        if (this.canOperateSelectText != canOperateSelectText) {
            this.canOperateSelectText = canOperateSelectText;
            if (pageChangeListener != null) {
                pageChangeListener.onCanOperateSelectTextChange(canOperateSelectText);
            }
        }
    }

    /**
     * 返回当前选择的文字起始和结束的边界
     *
     * @param pos left of first character, top, right, bottom; left of second character, top , right, bottom
     */
    public void getSelectPositions(@Size(8) float[] pos) {
        if (currentPageMeasured()) {
            int index0 = selectIndexes[0];
            if (index0 >= 0 && index0 < pageCharPositions.positions.size()) {
                CharPosition charPosition = pageCharPositions.positions.get(index0);
                pos[0] = charPosition.x;
                pos[1] = charPosition.y;
                pos[2] = charPosition.x + charPosition.width;
                pos[3] = charPosition.y + charPosition.height;
            }
            int index1 = selectIndexes[1];
            if (index1 >= 0 && index1 < pageCharPositions.positions.size()) {
                CharPosition charPosition = pageCharPositions.positions.get(index1);
                pos[4] = charPosition.x;
                pos[5] = charPosition.y;
                pos[6] = charPosition.x + charPosition.width;
                pos[7] = charPosition.y + charPosition.height;
            }
        }
    }

    /**
     * Get current select text and exit selectMode
     *
     * @return currentSelectText
     */
    public String getSelectedTextAndExitSelectMode() {
        if (!isSelectMode) {
            return null;
        }

        isSelectMode = false;
        isDragTab = false;
        pageView.drawCurPage(false);
        setCanOperateSelectTextAndNotify(false);

        int startIndex = Math.min(selectIndexes[0], selectIndexes[1]);
        int endIndex = Math.max(selectIndexes[0], selectIndexes[1]);
        if (currentPageMeasured()) {
            CharPosition startPosition = pageCharPositions.positions.get(startIndex);
            CharPosition endPosition = pageCharPositions.positions.get(endIndex);
            List<String> lines = curPage.lines;
            if (lines == null || lines.isEmpty()) {
                return null;
            }

            if (startPosition.line < 0 || endPosition.line < 0 || startPosition.line >= lines.size() || endPosition.line >= lines.size()) {
                return null;
            }

            if (startPosition.line == endPosition.line) {
                return lines.get(startPosition.line).substring(startPosition.charOffset, endPosition.charOffset + 1);
            }

            StringBuilder sb = new StringBuilder();
            for (int i = startPosition.line; i <= endPosition.line; i++) {
                if (i == startPosition.line) {
                    sb.append(lines.get(i).substring(startPosition.charOffset));
                } else if (i == endPosition.line) {
                    sb.append(lines.get(i).substring(0, endPosition.charOffset + 1));
                } else {
                    sb.append(lines.get(i));
                }
            }
            return sb.toString();
        }

        return null;
    }

    boolean hitTestSelectTab(int x, int y) {

        int hitTestCnt = 0;
        Rect rect = new Rect();
        for (int i = 0; i < selectTextTabDrawables.length; i++) {
            rect.set(selectTextTabDrawables[i].getBounds());
            rect.inset(-hitTestSlop, -hitTestSlop);
            if (rect.contains(x, y)) {
                hitTestIndex = i;
                hitTestCnt++;
            }
        }

        if (hitTestCnt == 2) {
            // If both tabs are hit, choose the nearest one.
            Rect rect0 = selectTextTabDrawables[0].getBounds();
            Rect rect1 = selectTextTabDrawables[1].getBounds();
            double s0 = Math.pow(x - rect0.centerX(), 2) + Math.pow(y - rect0.centerY(), 2);
            double s1 = Math.pow((x - rect1.centerX()), 2) + Math.pow(y - rect1.centerY(), 2);
            if (s0 > s1) {
                hitTestIndex = 1;
            } else {
                hitTestIndex = 0;
            }
        }

        isDragTab = hitTestCnt > 0;
        return isDragTab;
    }

    boolean isDragTab() {
        return isSelectMode && isDragTab;
    }

    boolean exitDrag() {
        if (isSelectMode) {
            if (isDragTab) {
                isDragTab = false;
                return true;
            }
        }

        isDragTab = false;
        return false;
    }

    void onDragTab(float x, float y) {
        if (isDragTab && isSelectMode) {
            // 给更多的控件让用户看到选择文字
            y -= halfTabSize * 2 + hitTestSlop;
            int[] indexes = new int[2];
            if (!binaryFindLineIndex(indexes, y)) {
                return;
            }

            int newPosition = binaryFindPositionInLine(indexes, x);

            int anotherIndex = hitTestIndex == 0 ? 1 : 0;

            if (selectIndexes[hitTestIndex] != newPosition) {
                boolean hitTestIndexBefore = hitTestIndex == 0;
                boolean newIndexBefore;
                if (newPosition < selectIndexes[anotherIndex]) {
                    newIndexBefore = true;
                } else if (newPosition == selectIndexes[anotherIndex]) {
                    newIndexBefore = hitTestIndex == 0;
                } else {
                    newIndexBefore = false;
                }

                if (newIndexBefore && !hitTestIndexBefore) {
                    // change dragging tab to start position
                    hitTestIndex = 0;
                    selectIndexes[1] = Math.max(selectIndexes[anotherIndex] - 1, 0);
                } else if (!newIndexBefore && hitTestIndexBefore) {
                    // change dragging tab to end position
                    hitTestIndex = 1;
                    selectIndexes[0] = Math.min(selectIndexes[anotherIndex] + 1, pageCharPositions.positions.size() - 1);
                }
                selectIndexes[hitTestIndex] = newPosition;
                pageView.drawCurPage(false);
            }
        }
    }

    private int binaryFindPositionInLine(@Size(2) int[] indexes, float x) {
        if (pageCharPositions == null || pageCharPositions.positions.isEmpty()) {
            return -1;
        }
        List<CharPosition> positions = pageCharPositions.positions;
        for (int index : indexes) {
            if (index < 0 || index >= positions.size()) {
                return -1;
            }
        }

        if (x <= positions.get(indexes[0]).x) {
            return indexes[0];
        }

        if (x >= positions.get(indexes[1]).x) {
            return indexes[1];
        }

        int low = indexes[0];
        int high = indexes[1];
        int mid;

        while (low <= high) {
            mid = (low + high) >> 1;
            CharPosition midVal = positions.get(mid);
            float midValLeft = midVal.x;
            float midValRight = midValLeft + midVal.width;
            if (x >= midValLeft && x < midValRight) {
                return mid;
            }

            if (x < midValLeft) {
                high = mid - 1;
                continue;
            }

            low = mid + 1;
        }

        return -1;
    }

    private boolean binaryFindLineIndex(@Size(2) int[] indexes, float y) {
        if (pageCharPositions == null || pageCharPositions.positions.isEmpty()) {
            return false;
        }

        List<CharPosition> positions = pageCharPositions.positions;

        if (y < positions.get(0).y) {
            int i = 0;
            indexes[0] = 0;
            while (i < positions.size() && positions.get(i).line == 0) {
                indexes[1] = i++;
            }
            return true;
        }

        CharPosition lastChar = positions.get(positions.size() - 1);
        if (y >= lastChar.y) {
            int i = positions.size() - 1;
            indexes[1] = i;
            while (i >= 0 && positions.get(i).line == lastChar.line) {
                indexes[0] = i--;
            }
            return true;
        }

        int low = 0;
        int high = positions.size() - 1;
        int mid;

        int index = -1;

        while (low <= high) {
            mid = (low + high) >> 1;
            CharPosition midVal = positions.get(mid);
            float midValTop = midVal.y;
            float midValBottom = midValTop + midVal.height;
            if (y >= midValTop && y < midValBottom) {
                index = mid;
                break;
            }

            if (y < midValTop) {
                high = mid - 1;
                continue;
            }
            low = mid + 1;
        }

        if (index == -1) {
            return false;
        }

        int i = index;
        int line = positions.get(index).line;
        while (i >= 0 && positions.get(i).line == line) {
            indexes[0] = i--;
        }
        i = index;
        while (i < positions.size() && positions.get(i).line == line) {
            indexes[1] = i++;
        }


        return true;
    }

    private int binaryFindCharacterAt(float x, float y) {
        if (pageCharPositions == null) {
            return -1;
        }

        List<CharPosition> positions = pageCharPositions.positions;

        int low = 0;
        int high = positions.size() - 1;
        int mid;

        while (low <= high) {
            mid = (low + high) >> 1;
            CharPosition midVal = positions.get(mid);
            float midValLeft = midVal.x;
            float midValTop = midVal.y;
            float midValRight = midValLeft + midVal.width;
            float midValBottom = midValTop + midVal.height;
            if (x >= midValLeft && y >= midValTop && x <= midValRight && y <= midValBottom) {
                return mid;
            }

            if (y < midValTop || (y <= midValBottom && x < midValLeft)) {
                high = mid - 1;
                continue;
            }

            low = mid + 1;
        }

        return -1;
    }

    boolean isSelectMode() {
        return isSelectMode;
    }

    boolean enterSelectMode(float x, float y) {
        if (isSelectMode) {
            return false;
        }

        if (!measureTextIfNeeded()) {
            return false;
        }

        int index = binaryFindCharacterAt(x, y);
        if (index == -1) {
            LogUtils.d(TAG, "没有找到在(" + x + ", " + y + ")的文字，跳过选择！");
            return false;
        }

        CharPosition charPosition = pageCharPositions.positions.get(index);
        LogUtils.d(TAG, "长按文字在第" + charPosition.line + "行第" + charPosition.charOffset + "列");
        selectIndexes[0] = selectIndexes[1] = index;

        DeviceUtilsKt.vibrate(50);
        isSelectMode = true;
        isDragTab = false;
        pageView.drawCurPage(false);
        LogUtils.d(TAG, "进入选择模式");
        return true;
    }

    boolean exitSelectMode() {
        if (!isSelectMode) {
            return false;
        }

        isSelectMode = false;
        pageView.drawCurPage(false);
        LogUtils.d(TAG, "退出选择模式");
        return true;
    }

    private boolean measureTextIfNeeded() {
        if (curPage == null || curPage.lines == null || curChapterPos < 0 || curChapterPos >= chapterList.size() || curPageList == null || curPage.position < 0 || curPage.position >= curPageList.size()) {
            return false;
        }

        if (currentPageMeasured()) return true;

        long start = SystemClock.elapsedRealtime();
        pageCharPositions = new PageCharPositions(curChapterPos, curPage.position, contentWidth, contentHeight, indentString, contentTextSize, lineSpaceFactor, paraSpaceFactor);
        int currentLine = 0;
        contentTextPaint.getFontMetrics(finalFontMetrics);
        float contentLineHeight = finalFontMetrics.bottom - finalFontMetrics.top;
        titlePaint.getFontMetrics(finalFontMetrics);
        float titleLineHeight = finalFontMetrics.bottom - finalFontMetrics.top;

        //设置总距离
        float interval = textInterval + contentLineHeight;
        float para = textPara + contentLineHeight;
        float titleInterval = this.titleInterval + titleLineHeight;
        float titlePara = this.titlePara + titleLineHeight;

        float currentY = marginTop;
        float currentX;
        String line;

        //对标题进行测量
        List<CharPosition> positions = pageCharPositions.positions;
        for (int i = 0; i < curPage.titleLines; ++i) {
            line = curPage.lines.get(i);

            //计算文字显示的起始点
            currentX = (viewWidth - titlePaint.measureText(line)) / 2;
            int len = line.length();
            for (int j = 0; j < len; j++) {
                char c = line.charAt(j);
                if (c == '\n') {
                    continue;
                }
                float textWidth = titlePaint.measureText(String.valueOf(c));
                CharPosition charPosition = new CharPosition(currentLine, j, currentX, currentY, textWidth, titleInterval, titleLineHeight);
                positions.add(charPosition);
                currentX += textWidth;
            }

            //设置尾部间距
            if (i == curPage.titleLines - 1) {
                currentY += titlePara;
            } else {
                //行间距
                currentY += titleInterval;
            }
            currentLine++;
        }

        //对内容进行测量
        for (int i = curPage.titleLines; i < curPage.lines.size(); ++i) {
            line = curPage.lines.get(i);
            currentX = marginLeft;
            int len = line.length();
            for (int j = 0; j < len; j++) {
                char c = line.charAt(j);
                if (c == '\n') {
                    continue;
                }
                float textWidth = contentTextPaint.measureText(String.valueOf(c));
                CharPosition charPosition = new CharPosition(currentLine, j, currentX, currentY, textWidth, interval, contentLineHeight);
                positions.add(charPosition);
                currentX += textWidth;
                // 这个是对中文引号的特殊处理
                if (j - 1 >= 0 && (c == '“' || c == '‘' || c == '”' || c == '’') && StringUtilsKt.isChinesePunctuation(line.charAt(j - 1))) {
                    float composedTextWidth = contentTextPaint.measureText(line.substring(j - 1, j + 1));
                    int size = positions.size();
                    if (size > 1) {
                        CharPosition lastPosition = positions.get(size - 2);
                        float lastCharWidth = composedTextWidth - textWidth;
                        float lastCharX = lastPosition.x;
                        lastPosition.width = lastCharWidth;
                        charPosition.x = lastCharX + lastCharWidth;
                        currentX = lastCharX + composedTextWidth;
                    }
                }
            }
            if (line.endsWith("\n")) {
                currentY += para;
            } else {
                currentY += interval;
            }
            currentLine++;
        }
        LogUtils.d(TAG, "Measure PageCharPositions" + " costs " + (SystemClock.elapsedRealtime() - start) + "ms.");
        return true;
    }

    /**
     * 判断当前已经测量的结果是否和现在的页面参数匹配
     */
    private boolean currentPageMeasured() {
        return curPage != null && pageCharPositions != null && pageCharPositions.isCurrentPagePosition(curChapterPos, curPage.position, contentWidth, contentHeight, indentString, contentTextSize, lineSpaceFactor, paraSpaceFactor);
    }

    private Drawable getChargeDrawable() {
        if (chargeDrawable == null) {
            chargeDrawable = ReaderResourcesKt.getDrawableRes(com.lyc.easyreader.api.R.drawable.ic_thunder);
            if (chargeDrawable != null) {
                ViewUtilsKt.changeToColor(chargeDrawable, contentTextPaint.getColor());
            }
        }
        return chargeDrawable;
    }

    private void setUpTextParams(int textSize) {
        // 文字大小
        this.contentTextSize = textSize;
        tipTextSize = Math.round(textSize * 0.75f);
        titleTextSize = Math.round(contentTextSize * 1.2f);
        // 行间距(大小为字体的一半)
        textInterval = Math.round(contentTextSize * lineSpaceFactor);
        titleInterval = Math.round(titleTextSize * lineSpaceFactor);
        // 段落间距(大小为字体的高度)
        textPara = Math.round(contentTextSize * paraSpaceFactor);
        titlePara = Math.round(titleTextSize * paraSpaceFactor);
    }

    private void initPaint() {
        // 绘制提示的画笔
        tipPaint.setColor(ViewUtilsKt.addColorAlpha(textColor, TIP_ALPHA));
        tipPaint.setTextAlign(Paint.Align.LEFT); // 绘制的起始点
        tipPaint.setTextSize(tipTextSize); // Tip默认的字体大小
        Paint.FontMetrics fontMetrics = finalFontMetrics;
        tipPaint.getFontMetrics(fontMetrics);
        widgetMaxHeight = fontMetrics.bottom - fontMetrics.top;

        // 绘制页面内容的画笔
        contentTextPaint.setColor(textColor);
        contentTextPaint.setTextSize(contentTextSize);

        // 绘制标题的画笔
        titlePaint.setColor(textColor);
        titlePaint.setTextSize(titleTextSize);
        titlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);

        // 绘制背景的画笔
        bgPaint.setColor(bgColor);

        // 初始化页面样式
        setNightMode(NightModeManager.INSTANCE.getNightModeEnable());
    }

    public void setChapterList(List<BookChapter> chapterList) {
        this.chapterList.clear();
        this.chapterList.addAll(chapterList);
        if (!this.chapterList.isEmpty()) {
            isChapterListPrepare = true;
            openChapter();
        } else {
            status = STATUS_EMPTY;
            pageView.drawCurPage(false);
        }
    }

    /**
     * 跳转到上一章
     */
    public boolean skipPreChapter() {
        if (!hasPrevChapter()) {
            return false;
        }

        // 载入上一章。
        if (parsePrevChapter()) {
            curPage = getCurPage(0);
            notifyCurPageChange();
        } else {
            curPage = new BookPage();
        }
        pageView.drawCurPage(false);
        return true;
    }

    /**
     * 跳转到下一章
     */
    public boolean skipNextChapter() {
        if (!hasNextChapter()) {
            return false;
        }

        //判断是否达到章节的终止点
        if (parseNextChapter()) {
            curPage = getCurPage(0);
            notifyCurPageChange();
        } else {
            curPage = new BookPage();
        }
        pageView.drawCurPage(false);
        return true;
    }

    /**
     * 跳转到指定章节
     * 如果目标位置就是当前章节，跳转到第一页
     */
    public void skipToChapter(int pos) {
        if (curChapterPos == pos && curPage != null && curPage.position == 0) {
            return;
        }
        // 设置参数
        curChapterPos = pos;

        // 将上一章的缓存设置为null
        prePageList = null;
        prePageListPos = -1;
        // 将下一章缓存设置为null
        nextPageList = null;
        nextPageListPos = -1;
        pendingPosInChapter = 0;

        // 打开指定章节
        openChapter();
    }

    public void skipToChapter(int pos, int offsetStart, int offsetEnd) {
        pendingOffsetStart = offsetStart;
        pendingOffsetEnd = offsetEnd;
        // 设置参数
        curChapterPos = pos;

        // 将上一章的缓存设置为null
        prePageList = null;
        prePageListPos = -1;
        // 将下一章缓存设置为null
        nextPageList = null;
        nextPageListPos = -1;
        pendingPosInChapter = 0;

        // 打开指定章节
        openChapter();
    }

    /**
     * 跳转到指定的页
     */
    public boolean skipToPage(int pos) {
        if (!isChapterListPrepare || !pageView.isPrepare()) {
            // 先记录下来，准备好了之后再打开
            pendingPosInChapter = pos;
            return false;
        }
        curPage = getCurPage(pos);
        if (curPage == null) {
            return false;
        }
        pageView.drawCurPage(false);
        return true;
    }

    /**
     * 根据字符串偏移得到当前最有可能的页号
     *
     * @param offsetStart 字符串偏移起始
     * @param offsetEnd   字符串偏移结束
     * @return 和起始结束重合最大的页号
     */
    private int findMostPossiblePageInOffset(int offsetStart, int offsetEnd) {
        if (curPageList == null) {
            return -1;
        }

        if (offsetStart > offsetEnd) {
            return -1;
        }

        BookPage first = curPageList.get(0);
        if (offsetEnd < first.getCharEnd() || offsetStart < first.getCharStart()) {
            return 0;
        }

        BookPage last = curPageList.get(curPageList.size() - 1);
        if (offsetStart >= last.getCharStart() || offsetEnd >= last.getCharEnd()) {
            return curPageList.size() - 1;
        }

        int startPage = findPagePosAtOffset(offsetStart);
        if (startPage == -1) {
            return -1;
        }
        int endPage = findPagePosAtOffset(offsetEnd);
        if (endPage == -1) {
            return -1;
        }

        if (startPage == endPage) {
            return startPage;
        }

        int page = -1;
        float percent = 0;
        // 找到可能的结果中区间重叠比例最高的
        for (int i = startPage; i <= endPage; i++) {
            BookPage bookPage = curPageList.get(i);
            int len = bookPage.getCharEnd() - bookPage.getCharStart();
            if (len <= 0) {
                continue;
            }
            int start = Math.max(bookPage.getCharStart(), offsetStart);
            int end = Math.min(bookPage.getCharEnd(), offsetEnd);
            float curPercent = (end - start) * 1f / (bookPage.getCharEnd() - bookPage.getCharStart());
            if (curPercent > percent) {
                percent = curPercent;
                page = i;
            }
        }

        return page;
    }

    /**
     * 二分查找字符串偏移落在的页
     *
     * @param offsetStart 需要查找的字符串
     * @return 页号如果找到，否则-1
     */
    private int findPagePosAtOffset(int offsetStart) {
        if (curPageList == null) {
            return -1;
        }

        int low = 0;
        int high = curPageList.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            BookPage midVal = curPageList.get(mid);
            if (offsetStart >= midVal.getCharEnd())
                low = mid + 1;
            else if (offsetStart < midVal.getCharStart())
                high = mid - 1;
            else
                return mid; // key found
        }
        return -1;  // key not found
    }

    /**
     * 翻到上一页
     */
    public boolean skipToPrePage() {
        return pageView.autoPrevPage();
    }

    /**
     * 翻到下一页
     */
    public boolean skipToNextPage() {
        return pageView.autoNextPage();
    }

    public int getBgColor() {
        return bgColor;
    }

    /**
     * 更新时间
     */
    public void updateTime() {
        if (!pageView.isRunning()) {
            pageView.drawCurPage(true);
        }
    }

    /**
     * 更新电池状态
     *
     * @param level      电量：0-100
     * @param isCharging 是否在充电
     */
    public void updateBattery(int level, boolean isCharging) {
        boolean needRedraw = false;
        if (batteryLevel != level) {
            batteryLevel = level;
            updateBatteryLocation();
            needRedraw = true;
        }

        if (this.isCharging != isCharging) {
            this.isCharging = isCharging;
            needRedraw = true;
        }

        if (needRedraw && !pageView.isRunning()) {
            pageView.drawCurPage(true);
        }
    }

    public boolean isFontBold() {
        return fontBold;
    }

    public void setFontBold(boolean fontBold) {
        if (this.fontBold != fontBold) {
            this.fontBold = fontBold;
            contentTextPaint.setFakeBoldText(fontBold);
            pageView.drawCurPage(false);
        }
    }

    public void setMarginLeft(int marginLeft) {
        setMargins(marginLeft, marginTop, marginRight, marginBottom);
    }

    public void setMarginRight(int marginRight) {
        setMargins(marginLeft, marginTop, marginRight, marginBottom);
    }

    public void setMarginTop(int marginTop) {
        setMargins(marginLeft, marginTop, marginRight, marginBottom);
    }

    public void setMarginBottom(int marginBottom) {
        setMargins(marginLeft, marginTop, marginRight, marginBottom);
    }

    public void setMargin(int margin) {
        setMargins(margin, margin, margin, margin);
    }

    public void setMargins(int left, int top, int right, int bottom) {
        boolean change = false;
        if (left != marginLeft) {
            marginLeft = left;
            change = true;
        }

        if (top != marginTop) {
            marginTop = top;
            change = true;
        }

        if (right != marginRight) {
            marginRight = right;
            change = true;
        }

        if (bottom != marginBottom) {
            marginBottom = bottom;
            change = true;
        }

        if (!change) {
            return;
        }
        applyViewSizeChange();
        postReloadPages();
    }

    public void setIndent(int count, boolean full) {
        if (count != indentCount || full != indentFull) {
            indentCount = count;
            indentFull = full;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                sb.append(' ');
            }
            String string = sb.toString();
            indentString = full ? StringUtilsKt.halfToFull(string) : string;
            postReloadPages();
        }
    }

    private void applyViewSizeChange() {
        if (viewWidth <= 0 || viewHeight <= 0) {
            return;
        }
        contentWidth = viewWidth - marginLeft - marginRight;
        contentHeight = Math.round(viewHeight - marginTop - marginBottom - WIDGET_MARGIN_BOTTOM - widgetMaxHeight);
        updateBatteryLocation();
    }

    /**
     * 设置文字相关参数
     */
    public void setContentTextSize(int textSize) {
        if (textSize < TEXT_SIZE_MIN_VALUE) {
            textSize = TEXT_SIZE_MIN_VALUE;
        } else if (textSize > TEXT_SIZE_MAX_VALUE) {
            textSize = TEXT_SIZE_MAX_VALUE;
        }
        if (textSize != contentTextSize) {
            // 设置文字相关参数
            setUpTextParams(textSize);
            updateBatteryLocation();
            contentTextPaint.setTextSize(contentTextSize);
            titlePaint.setTextSize(titleTextSize);
            postReloadPages();
        }
    }

    public float getLineSpaceFactor() {
        return lineSpaceFactor;
    }

    public void setLineSpaceFactor(float lineSpaceFactor) {
        if (lineSpaceFactor != this.lineSpaceFactor) {
            this.lineSpaceFactor = lineSpaceFactor;
            setUpTextParams(contentTextSize);
            postReloadPages();
        }
    }

    public float getParaSpaceFactor() {
        return paraSpaceFactor;
    }

    public void setParaSpaceFactor(float paraSpaceFactor) {
        if (paraSpaceFactor != this.paraSpaceFactor) {
            this.paraSpaceFactor = paraSpaceFactor;
            setUpTextParams(contentTextSize);
            postReloadPages();
        }
    }

    private void postReloadPages() {
        handler.removeMessages(MSG_RELOAD_PAGES);
        handler.sendEmptyMessage(MSG_RELOAD_PAGES);
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        if (msg.what == MSG_RELOAD_PAGES) {
            reloadPagesIfNeeded();
            return true;
        }

        return false;
    }

    private void reloadPagesIfNeeded() {
        if (viewWidth == 0 || viewHeight == 0) {
            return;
        }
        // 取消缓存
        prePageList = null;
        nextPageList = null;
        nextPageListPos = -1;

        // 如果当前已经显示数据
        if (isChapterListPrepare && status == STATUS_FINISH) {
            // 重新计算当前页面
            dealLoadPageList(curChapterPos);

            // 防止在最后一页，通过修改字体，以至于页面数减少导致崩溃的问题
            if (curPage.position >= curPageList.size()) {
                curPage.position = curPageList.size() - 1;
            }

            // 重新获取指定页面
            curPage = curPageList.get(curPage.position);
            pageView.drawCurPage(false);
        }
    }

    /**
     * 设置夜间模式
     */
    public void setNightMode(boolean nightMode) {
        isNightMode = nightMode;

        if (isNightMode) {
            setPageStyle(PageStyle.NIGHT);
        } else {
            setPageStyle(pageStyle);
        }
    }

    /**
     * 设置页面样式
     *
     * @param pageStyle:页面样式
     */
    public void setPageStyle(PageStyle pageStyle) {
        if (pageStyle != PageStyle.NIGHT) {
            this.pageStyle = pageStyle;
        }

        if (isNightMode && pageStyle != PageStyle.NIGHT) {
            return;
        }

        // 设置当前颜色样式
        textColor = pageStyle.getFontColor();
        bgColor = pageStyle.getBgColor();
        selectColor = ViewUtilsKt.getCenterColor(textColor, bgColor);
        for (SelectTextTabDrawable selectTextTabDrawable : selectTextTabDrawables) {
            selectTextTabDrawable.paint.setColor(selectColor);
        }

        tipPaint.setColor(ViewUtilsKt.addColorAlpha(textColor, TIP_ALPHA));
        titlePaint.setColor(textColor);
        contentTextPaint.setColor(textColor);

        bgPaint.setColor(bgColor);

        batteryPaint.setColor(tipPaint.getColor());
        if (isCharging) {
            ViewUtilsKt.changeToColor(getChargeDrawable(), bgColor);
        }

        pageView.drawCurPage(false);
    }

    /**
     * 翻页动画
     *
     * @param pageAnimMode:翻页模式
     * @see PageAnimMode
     */
    public void setPageAnimMode(PageAnimMode pageAnimMode) {
        pageMode = pageAnimMode;

        pageView.setPageMode(pageMode);

        // 重新绘制当前页
        pageView.drawCurPage(false);
    }

    public void setDrawTextBound(boolean drawTextBound) {
        if (this.drawTextBound != drawTextBound) {
            this.drawTextBound = drawTextBound;
            pageView.drawCurPage(true);
        }
    }

    /**
     * 设置页面切换监听
     */
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        pageChangeListener = listener;

        // 如果目录加载完之后才设置监听器，那么会默认回调
        if (isChapterListPrepare) {
            pageChangeListener.onCategoryFinish(chapterList);
        }
    }

    /**
     * 获取当前页的状态
     */
    public int getPageStatus() {
        return status;
    }

    public BookFile getBookFile() {
        return bookFile;
    }

    public List<BookChapter> getChapterCategory() {
        // 防止外部修改
        return new ArrayList<>(chapterList);
    }

    /**
     * 获取当前页的页码
     */
    public int getPagePos() {
        if (curPage == null) {
            return -1;
        }
        return curPage.position;
    }

    /**
     * 获取当前章节的章节位置
     */
    public int getChapterPos() {
        return curChapterPos;
    }

    /**
     * 打开指定章节
     */
    private void openChapter() {
        triedOpen = true;

        if (!pageView.isPrepare()) {
            return;
        }

        // 如果章节目录没有准备好
        if (!isChapterListPrepare) {
            status = STATUS_LOADING;
            pageView.drawCurPage(false);
            return;
        }

        // 如果获取到的章节目录为空
        if (chapterList.isEmpty()) {
            status = STATUS_CATEGORY_EMPTY;
            pageView.drawCurPage(false);
            return;
        }

        if (parseCurChapter()) {
            int position = -1;
            if (pendingOffsetStart != -1 && pendingOffsetEnd != -1 && pendingOffsetEnd >= pendingOffsetStart) {
                position = findMostPossiblePageInOffset(pendingOffsetStart, pendingOffsetEnd);
                if (position == -1) {
                    LogUtils.e(TAG, "Cannot find page at (" + pendingOffsetStart + ", " + pendingOffsetEnd + ")");
                } else {
                    LogUtils.e(TAG, "Page at (" + pendingOffsetStart + ", " + pendingOffsetEnd + ") is " + position);
                }
                pendingOffsetStart = -1;
                pendingOffsetEnd = -1;
            }
            if (position == -1 && pendingPosInChapter >= 0) {
                position = pendingPosInChapter;
                // 防止记录页的页号，大于当前最大页号
                if (position >= curPageList.size()) {
                    position = curPageList.size() - 1;
                }
            }

            if (position == -1) {
                position = 0;
            }

            pendingPosInChapter = -1;
            // 如果章节从未打开
            if (!isChapterOpen) {
                curPage = getCurPage(position);
                mCancelPage = curPage;
                // 切换状态
                isChapterOpen = true;
            } else {
                curPage = getCurPage(position);
            }

            if (pageChangeListener != null) {
                pageChangeListener.onChapterOpen(curChapterPos);
            }
        } else {
            curPage = new BookPage();
            status = STATUS_ERROR;
        }

        pageView.drawCurPage(false);
    }

    /**
     * 加载页面列表
     *
     * @param chapterPos:章节序号
     */
    private List<BookPage> loadPageList(int chapterPos) throws Exception {
        // 获取章节
        BookChapter chapter = chapterList.get(chapterPos);
        // 获取章节的文本流
        BufferedReader reader = getChapterReader(chapter);
        if (reader == null) {
            return null;
        }
        return loadPages(chapter, reader);
    }

    /**
     * 获取章节的文本流
     */
    private BufferedReader getChapterReader(BookChapter chapter) {
        BookChapterWrapper chapterWrapper = chapterCache.get(chapter);
        if (chapterWrapper != null) {
            BufferedReader bufferedReader = chapterWrapper.openBufferedReader();
            if (bufferedReader != null) {
                return bufferedReader;
            }
        }
        chapterCache.evict(chapter);
        BookChapterWrapper wrapper = new BookChapterWrapper(chapter, bookFile);
        BufferedReader bufferedReader = wrapper.openBufferedReader();
        if (bufferedReader != null) {
            chapterCache.put(chapter, wrapper);
        }
        return bufferedReader;
    }

    void drawPage(Bitmap bitmap, boolean isUpdate) {
        drawBackground(pageView.getBgBitmap(), isUpdate);
        if (!isUpdate) {
            drawContent(bitmap);
        }
        //更新绘制
        pageView.invalidate();
    }

    private void updateBatteryLocation() {
        if (viewWidth <= 0 || viewHeight <= 0) {
            return;
        }

        // 绘制电池
        int drawBottom = viewHeight - WIDGET_MARGIN_BOTTOM;

        float outFrameHeight = tipPaint.getTextSize() * 0.7f;
        float outFrameWidth = outFrameHeight * 2f;

        float polarRight = viewWidth - marginRight;
        Paint.FontMetrics fm = finalFontMetrics;
        tipPaint.getFontMetrics(fm);
        float textHeight = fm.descent - fm.ascent;
        float textCenter = drawBottom - textHeight * 0.5f;
        float outFrameBottom = textCenter + outFrameHeight * 0.5f;

        float polarHeight = outFrameHeight / 3f;
        float polarWidth = polarHeight * 0.5f;
        //电极的制作
        float polarLeft = polarRight - polarWidth;
        float polarTop = textCenter - polarHeight * 0.5f;
        polarRect.set(polarLeft, polarTop, polarRight, polarTop + polarHeight);

        //外框的制作
        float outFrameLeft = polarLeft - outFrameWidth;
        float outFrameTop = outFrameBottom - outFrameHeight;
        batteryFrameRect.set(outFrameLeft, outFrameTop, polarLeft, outFrameBottom);

        batteryCapRect.set(batteryFrameRect);
        batteryCapRect.inset(1f, 1f);

        batteryCapRect.right = batteryCapRect.left + (batteryCapRect.width() * batteryLevel / 100.0f);

        float chargeCenterX = batteryFrameRect.centerX();
        float chargeCenterY = batteryCapRect.centerY();
        float chargeHeightHalf = batteryCapRect.height() * 0.5f;
        float charWidthHalf = CHARGE_WIDTH * chargeHeightHalf / CHARGE_HEIGHT;
        chargeBounds.set(Math.round(chargeCenterX - charWidthHalf), Math.round(chargeCenterY - chargeHeightHalf), Math.round(chargeCenterX + charWidthHalf), Math.round(chargeCenterY + chargeHeightHalf));
    }

    private void drawBackground(Bitmap bitmap, boolean isUpdate) {
        Canvas canvas = new Canvas(bitmap);

        if (!isUpdate) {
            // 绘制背景
            canvas.drawColor(bgColor);
        } else if (lastTimeTextX > 0) {
            // 擦除右下角的区域
            // 准备重绘
            bgPaint.setColor(bgColor);
            canvas.drawRect(lastTimeTextX, viewHeight - WIDGET_MARGIN_BOTTOM - widgetMaxHeight, viewWidth, viewHeight - WIDGET_MARGIN_BOTTOM, bgPaint);
        }


        batteryPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(polarRect, batteryPaint);
        batteryPaint.setStyle(Paint.Style.STROKE);
        batteryPaint.setStrokeWidth(1f);
        canvas.drawRect(batteryFrameRect, batteryPaint);
        batteryPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(batteryCapRect, batteryPaint);
        if (isCharging) {
            Drawable chargeDrawable = getChargeDrawable();
            chargeDrawable.setBounds(chargeBounds);
            chargeDrawable.draw(canvas);
        }

        // 绘制当前时间
        //底部的字显示的位置Y
        tipPaint.getFontMetrics(finalFontMetrics);
        float y = viewHeight - finalFontMetrics.bottom - WIDGET_MARGIN_BOTTOM;
        String time = StringUtilsKt.formatReaderTime(System.currentTimeMillis());
        float x = batteryFrameRect.left - tipPaint.measureText(time) - MARGIN_CHARGE_TIME;
        lastTimeTextX = x;
        canvas.drawText(time, x, y, tipPaint);
        float rightDis = viewWidth - (x - TINY_CHAPTER_MARGIN);

        if (!isUpdate) {

            if (!chapterList.isEmpty()) {
                // 绘制页码
                // 底部的字显示的位置Y
                tipPaint.getFontMetrics(finalFontMetrics);
                y = viewHeight - finalFontMetrics.bottom - WIDGET_MARGIN_BOTTOM;
                float leftDis = 0f;
                // 只有finish的时候采用页码
                if (status == STATUS_FINISH) {
                    String percent = (curPage.position + 1) + "/" + curPageList.size();
                    canvas.drawText(percent, marginLeft, y, tipPaint);
                    leftDis = tipPaint.measureText(percent) + TINY_CHAPTER_MARGIN + marginLeft;
                }

                String pendingTitle = null;
                if (status != STATUS_FINISH) {
                    if (isChapterListPrepare) {
                        pendingTitle = chapterList.get(curChapterPos).getTitle();
                    }
                } else {
                    pendingTitle = curPage.title;
                }

                if (pendingTitle != null) {
                    float maxWidth = viewWidth - Math.max(leftDis, rightDis) * 2;
                    if (maxWidth > 0) {
                        int count = tipPaint.breakText(pendingTitle, true, maxWidth, null);
                        if (count > 0) {
                            Paint.Align orgAlign = tipPaint.getTextAlign();
                            tipPaint.setTextAlign(Paint.Align.CENTER);
                            if (count != pendingTitle.length()) {
                                canvas.drawText(pendingTitle.substring(0, count), viewWidth * 0.5f, y, tipPaint);
                            } else {
                                canvas.drawText(pendingTitle, viewWidth * 0.5f, y, tipPaint);
                            }
                            tipPaint.setTextAlign(orgAlign);
                        }
                    }
                }
            }
        }
    }

    private void drawContent(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);

        if (status != STATUS_FINISH) {
            //绘制字体
            String tip = "";
            switch (status) {
                case STATUS_LOADING:
                    tip = "正在拼命加载中...";
                    break;
                case STATUS_ERROR:
                    tip = "加载失败(点击边缘重试)";
                    break;
                case STATUS_EMPTY:
                    tip = "文章内容为空";
                    break;
                case STATUS_PARING:
                    tip = "正在排版请等待...";
                    break;
                case STATUS_PARSE_ERROR:
                    tip = "文件解析错误";
                    break;
                case STATUS_CATEGORY_EMPTY:
                    tip = "目录列表为空";
                    break;
            }

            //将提示语句放到正中间
            Paint.FontMetrics fontMetrics = finalFontMetrics;
            contentTextPaint.getFontMetrics(finalFontMetrics);
            float textHeight = fontMetrics.top - fontMetrics.bottom;
            float textWidth = contentTextPaint.measureText(tip);
            float pivotX = (viewWidth - textWidth) / 2;
            float pivotY = (viewHeight - textHeight) / 2;
            canvas.drawText(tip, pivotX, pivotY, contentTextPaint);
        } else {
            final BookPage curPage = this.curPage;
            if (curPage == null) {
                LogUtils.w(TAG, "When draw page, page==null");
                return;
            }
            final List<String> lines = curPage.lines;
            if (lines == null) {
                LogUtils.w(TAG, "When draw page, page.lines==null");
                return;
            }

            if (drawTextBound) {
                drawTextBound(canvas);
            }

            contentTextPaint.getFontMetrics(finalFontMetrics);
            float contentFirstBaseline = marginTop - finalFontMetrics.top;
            float contentLineHeight = finalFontMetrics.bottom - finalFontMetrics.top;
            titlePaint.getFontMetrics(finalFontMetrics);
            float titleLineHeight = finalFontMetrics.bottom - finalFontMetrics.top;
            float titleFirstBaseline = marginTop - finalFontMetrics.top;

            //设置总距离
            float interval = textInterval + contentLineHeight;
            float para = textPara + contentLineHeight;
            float titleInterval = this.titleInterval + titleLineHeight;
            // 这里计算加的是内容的行高，因为计算的是baseline，是根据下一行的高度决定的
            float titlePara = this.titlePara + contentLineHeight;
            String str;

            float baseline = contentFirstBaseline;
            //对标题进行绘制
            for (int i = 0; i < curPage.titleLines; ++i) {
                str = lines.get(i);

                //设置顶部间距
                if (i == 0) {
                    baseline = titleFirstBaseline;
                }

                //计算文字显示的起始点
                int start = (int) (viewWidth - titlePaint.measureText(str)) / 2;
                //进行绘制
                canvas.drawText(str, start, baseline, titlePaint);

                //设置尾部间距
                if (i == curPage.titleLines - 1) {
                    baseline += titlePara;
                } else {
                    //行间距
                    baseline += titleInterval;
                }
            }

            //对内容进行绘制
            for (int i = curPage.titleLines; i < lines.size(); ++i) {
                str = lines.get(i);

                canvas.drawText(str, marginLeft, baseline, contentTextPaint);
                contentTextPaint.getFontMetrics(finalFontMetrics);
                if (str.endsWith("\n")) {
                    baseline += para;
                } else {
                    baseline += interval;
                }
            }

            if (isSelectMode && currentPageMeasured()) {
                List<CharPosition> positions = pageCharPositions.positions;
                if (selectIndexes[0] >= 0 && selectIndexes[0] < positions.size() && selectIndexes[1] >= 0 && selectIndexes[1] < positions.size()) {
                    int startIndex = Math.min(selectIndexes[0], selectIndexes[1]);
                    int endIndex = Math.max(selectIndexes[0], selectIndexes[1]);
                    int color = contentTextPaint.getColor();
                    contentTextPaint.setColor(selectColor);
                    contentTextPaint.setAlpha((int) (0.3f * 0xff));

                    // 绘制选择的高亮方块
                    for (int i = startIndex; i <= endIndex; i++) {
                        CharPosition position = positions.get(i);
                        canvas.drawRect(position.x, position.y, position.x + position.width, position.y + position.drawHeight, contentTextPaint);
                    }
                    contentTextPaint.setAlpha(0xff);


                    // 绘制游标
                    CharPosition position0 = positions.get(selectIndexes[0]);
                    boolean isPosition0Start = selectIndexes[0] == startIndex;
                    float x0 = isPosition0Start ? position0.x : position0.x + position0.width;
                    // 先绘制边界线
                    canvas.drawLine(x0, position0.y, x0, position0.y + position0.drawHeight, contentTextPaint);
                    // 在绘制游标，游标在下面，是一段与等腰直角三角形两边相切的弧
                    selectTextTabDrawables[0].setBounds((int) (x0 - halfTabSize), (int) (position0.y + position0.drawHeight), (int) (x0 + halfTabSize), (int) (position0.y + position0.drawHeight + halfTabSize + halfTabSize));
                    selectTextTabDrawables[0].draw(canvas);
                    CharPosition position1 = positions.get(selectIndexes[1]);
                    float x1 = isPosition0Start ? position1.x + position1.width : position1.x;
                    canvas.drawLine(x1, position1.y, x1, position1.y + position1.drawHeight, contentTextPaint);
                    selectTextTabDrawables[1].setBounds((int) (x1 - halfTabSize), (int) (position1.y + position1.drawHeight), (int) (x1 + halfTabSize), (int) (position1.y + position1.drawHeight + halfTabSize + halfTabSize));
                    selectTextTabDrawables[1].draw(canvas);

                    contentTextPaint.setColor(color);
                }
            }
        }
    }

    // 绘制文字边界，调试选项
    private void drawTextBound(Canvas canvas) {
        measureTextIfNeeded();
        Paint.Style style = contentTextPaint.getStyle();
        contentTextPaint.setStyle(Paint.Style.STROKE);
        if (pageCharPositions != null) {
            for (CharPosition position : pageCharPositions.positions) {
                canvas.drawRect(position.x, position.y, position.x + position.width, position.y + position.drawHeight, contentTextPaint);
            }
        }
        contentTextPaint.setStyle(style);
    }

    /**
     * 在能够确定View宽高时调用
     * 如果需要打开章节也会打开章节（根据之前是否有调用过打开章节等方法）
     */
    void prepareDisplay(int w, int h) {
        // 获取PageView的宽高
        viewWidth = w;
        viewHeight = h;

        // 获取内容显示位置的大小
        applyViewSizeChange();

        // 重置 PageMode
        pageView.setPageMode(pageMode);

        if (!isChapterOpen) {
            // 展示加载界面
            pageView.drawCurPage(false);
            // 如果在 display 之前调用过 openChapter 肯定是无法打开的。
            // 所以需要通过 display 再重新调用一次。
            if (triedOpen) {
                // 打开书籍
                openChapter();
            }
            triedOpen = false;
        } else {
            // 如果章节已显示，那么就重新计算页面
            if (status == STATUS_FINISH) {
                dealLoadPageList(curChapterPos);
                // 重新设置文章指针的位置
                curPage = getCurPage(curPage.position);
            }
            pageView.drawCurPage(false);
        }
    }

    /**
     * 翻阅上一页
     */
    boolean prev() {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false;
        }

        if (status == STATUS_FINISH) {
            // 先查看是否存在上一页
            BookPage prevPage = getPrevPage();
            if (prevPage != null) {
                mCancelPage = curPage;
                curPage = prevPage;
                pageView.drawNextPage();
                return true;
            }
        }

        if (!hasPrevChapter()) {
            return false;
        }

        mCancelPage = curPage;
        if (parsePrevChapter()) {
            curPage = getPrevLastPage();
            notifyCurPageChange();
        } else {
            curPage = new BookPage();
        }
        pageView.drawNextPage();
        return true;
    }

    /**
     * 解析上一章数据
     *
     * @return 数据是否解析成功
     */
    private boolean parsePrevChapter() {
        // 加载上一章数据
        int prevChapter = curChapterPos - 1;

        lastChapterPos = curChapterPos;
        curChapterPos = prevChapter;

        // 当前章缓存为下一章
        nextPageList = curPageList;
        nextPageListPos = curChapterPos + 1;

        // 判断是否具有上一章缓存
        if (prePageList != null && prePageListPos == prevChapter) {
            curPageList = prePageList;
            prePageList = null;
            prePageListPos = -1;
            // 回调
            notifyChapterChange();
        } else {
            dealLoadPageList(prevChapter);
        }
        preLoadNextChapter();
        return curPageList != null;
    }

    private boolean hasPrevChapter() {
        //判断是否上一章节为空
        return curChapterPos - 1 >= 0;
    }

    boolean next() {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false;
        }

        if (status == STATUS_FINISH) {
            // 先查看是否存在下一页
            BookPage nextPage = getNextPage();
            if (nextPage != null) {
                mCancelPage = curPage;
                curPage = nextPage;
                pageView.drawNextPage();
                return true;
            }
        }

        if (!hasNextChapter()) {
            return false;
        }

        mCancelPage = curPage;
        // 解析下一章数据
        if (parseNextChapter()) {
            curPage = curPageList.get(0);
            notifyCurPageChange();
        } else {
            curPage = new BookPage();
        }
        pageView.drawNextPage();
        return true;
    }

    private boolean hasNextChapter() {
        // 判断是否到达目录最后一章
        return curChapterPos + 1 < chapterList.size();
    }

    private boolean parseCurChapter() {
        // 解析数据
        dealLoadPageList(curChapterPos);
        // 预加载下一页面
        preLoadNextChapter();
        // 预加载上一页面
        preLoadPreChapter();
        return curPageList != null;
    }

    /**
     * 解析下一章数据
     */
    private boolean parseNextChapter() {
        int nextChapter = curChapterPos + 1;

        lastChapterPos = curChapterPos;
        curChapterPos = nextChapter;

        // 将当前章的页面列表，作为上一章缓存
        prePageList = curPageList;
        prePageListPos = curChapterPos;

        // 是否下一章数据已经预加载了
        if (nextPageList != null && nextPageListPos == nextChapter) {
            curPageList = nextPageList;
            nextPageList = null;
            nextPageListPos = -1;
            // 回调
            notifyChapterChange();
        } else {
            // 处理页面解析
            dealLoadPageList(nextChapter);
        }
        // 预加载下一页面
        preLoadNextChapter();
        return curPageList != null;
    }

    private void dealLoadPageList(int chapterPos) {
        try {
            curPageList = loadPageList(chapterPos);
            if (curPageList != null) {
                if (curPageList.isEmpty()) {
                    status = STATUS_EMPTY;

                    // 添加一个空数据
                    BookPage page = new BookPage();
                    page.lines = new ArrayList<>(1);
                    curPageList.add(page);
                } else {
                    status = STATUS_FINISH;
                }
            } else {
                status = STATUS_LOADING;
            }
        } catch (Exception e) {
            e.printStackTrace();

            curPageList = null;
            status = STATUS_ERROR;
        }

        // 回调
        notifyChapterChange();
    }

    private void notifyChapterChange() {
        if (pageChangeListener != null) {
            pageChangeListener.onChapterChange(curChapterPos);
            pageChangeListener.onPageCountChange(curPageList != null ? curPageList.size() : 0);
        }
    }

    // 预加载下一章
    private void preLoadNextChapter() {
        final int nextChapter = curChapterPos + 1;

        // 如果不存在下一章，则不进行加载。
        if (!hasNextChapter()) {
            return;
        }

        if (nextPageList != null && nextPageListPos == nextChapter) {
            // 下一章已经加载好了，无需重复加载
            return;
        }

        ExecutorFactory.INSTANCE.getIO_EXECUTOR().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<BookPage> list = loadPageList(nextChapter);
                    ExecutorFactory.INSTANCE.getMAIN_EXECUTOR().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (curChapterPos + 1 == nextChapter && nextPageListPos != nextChapter) {
                                nextPageListPos = nextChapter;
                                nextPageList = list;
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 预加载上一章
    // 适用于翻前页
    private void preLoadPreChapter() {
        final int preChapter = curChapterPos - 1;

        // 如果不存在下一章，则不进行加载。
        if (!hasPrevChapter()) {
            return;
        }

        if (prePageList != null && prePageListPos == preChapter) {
            // 下一章已经加载好了，无需重复加载
            return;
        }

        ExecutorFactory.INSTANCE.getIO_EXECUTOR().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<BookPage> list = loadPageList(preChapter);
                    ExecutorFactory.INSTANCE.getMAIN_EXECUTOR().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (curChapterPos - 1 == preChapter && prePageListPos != preChapter) {
                                prePageListPos = preChapter;
                                prePageList = list;
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 取消翻页
    void pageCancel() {
        if (curPage.position == 0 && curChapterPos > lastChapterPos) { // 加载到下一章取消了
            if (prePageList != null) {
                cancelNextChapter();
            } else {
                if (parsePrevChapter()) {
                    curPage = getPrevLastPage();
                    notifyCurPageChange();
                } else {
                    curPage = new BookPage();
                }
            }
        } else if (curPageList == null
                || (curPage.position == curPageList.size() - 1
                && curChapterPos < lastChapterPos)) {  // 加载上一章取消了

            if (nextPageList != null) {
                cancelPreChapter();
            } else {
                if (parseNextChapter()) {
                    curPage = curPageList.get(0);
                    notifyCurPageChange();
                } else {
                    curPage = new BookPage();
                }
            }
        } else {
            // 假设加载到下一页，又取消了。那么需要重新装载。
            curPage = mCancelPage;
        }
    }

    private void cancelNextChapter() {
        int temp = lastChapterPos;
        lastChapterPos = curChapterPos;
        curChapterPos = temp;

        nextPageList = curPageList;
        nextPageListPos = curChapterPos + 1;
        curPageList = prePageList;
        prePageList = null;
        prePageListPos = -1;

        notifyChapterChange();

        curPage = getPrevLastPage();
        mCancelPage = null;
    }

    private void cancelPreChapter() {
        // 重置位置点
        int temp = lastChapterPos;
        lastChapterPos = curChapterPos;
        curChapterPos = temp;
        // 重置页面列表
        prePageList = curPageList;
        prePageListPos = curChapterPos - 1;
        curPageList = nextPageList;
        nextPageList = null;
        nextPageListPos = -1;

        notifyChapterChange();

        curPage = getCurPage(0);
        mCancelPage = null;
    }

    /**
     * 将章节数据，解析成页面列表
     *
     * @param chapter：章节信息
     * @param br：章节的文本流
     */
    private List<BookPage> loadPages(BookChapter chapter, BufferedReader br) {
        Paint.FontMetrics finalFontMetrics = Looper.getMainLooper() == Looper.myLooper() ? this.finalFontMetrics : new Paint.FontMetrics();
        tipPaint.getFontMetrics(finalFontMetrics);
        float titleLineTextHeight = finalFontMetrics.bottom - finalFontMetrics.top;
        contentTextPaint.getFontMetrics(finalFontMetrics);
        float contentLineTextHeight = finalFontMetrics.bottom - finalFontMetrics.top;
        //生成的页面
        List<BookPage> pages = new ArrayList<>();
        //使用流的方式加载
        List<String> lines = new ArrayList<>();
        int rHeight = contentHeight;
        int titleLinesCount = 0;
        boolean showTitle = true; // 是否展示标题
        String paragraph = chapter.getTitle();//默认展示标题
        try {
            while (showTitle || (paragraph = br.readLine()) != null) {
                // 重置段落
                if (!showTitle) {
                    paragraph = paragraph.replaceAll("\\s", "");
                    // 如果只有换行符，那么就不执行
                    if (TextUtils.isEmpty(paragraph)) continue;
                    paragraph = indentString == null ? "" : indentString + paragraph + "\n";
                } else {
                    //设置 title 的顶部间距
                    rHeight -= titlePara;
                }
                int wordCount;
                String subStr;
                while (paragraph.length() > 0) {
                    //当前空间，是否容得下一行文字
                    if (showTitle) {
                        rHeight -= titleLineTextHeight;
                    } else {
                        rHeight -= contentLineTextHeight;
                    }
                    // 一页已经填充满了，创建 TextPage
                    if (rHeight <= 0) {
                        // 创建Page
                        BookPage page = new BookPage();
                        page.position = pages.size();
                        page.title = chapter.getTitle();
                        page.lines = new ArrayList<>(lines);
                        page.titleLines = titleLinesCount;
                        pages.add(page);
                        // 重置Lines
                        lines.clear();
                        rHeight = contentHeight;
                        titleLinesCount = 0;

                        continue;
                    }

                    //测量一行占用的字节数
                    if (showTitle) {
                        wordCount = titlePaint.breakText(paragraph,
                                true, contentWidth, null);
                    } else {
                        wordCount = contentTextPaint.breakText(paragraph,
                                true, contentWidth, null);
                    }

                    subStr = paragraph.substring(0, wordCount);
                    if (!subStr.equals("\n")) {
                        //将一行字节，存储到lines中
                        lines.add(subStr);

                        //设置段落间距
                        if (showTitle) {
                            titleLinesCount += 1;
                            rHeight -= titleInterval;
                        } else {
                            rHeight -= textInterval;
                        }
                    }
                    //裁剪
                    paragraph = paragraph.substring(wordCount);
                }

                //增加段落的间距
                if (!showTitle && lines.size() != 0) {
                    rHeight = rHeight - textPara + textInterval;
                }

                if (showTitle) {
                    rHeight = rHeight - titlePara + titleInterval;
                    showTitle = false;
                }
            }

            if (lines.size() != 0) {
                //创建Page
                BookPage page = new BookPage();
                page.position = pages.size();
                page.title = chapter.getTitle();
                page.lines = new ArrayList<>(lines);
                page.titleLines = titleLinesCount;
                pages.add(page);
                //重置Lines
                lines.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        int currentOffSet = 0;
        for (int i = 0, s = pages.size(); i < s; i++) {
            BookPage firstPage = pages.get(i);
            firstPage.setCharStart(currentOffSet);
            currentOffSet += firstPage.calculateCharCnt();
            firstPage.setCharEnd(currentOffSet);
        }

        return pages;
    }

    public void getPageOffset(int pos, int[] offsets) {
        if (curPageList != null && pos >= 0 && pos < curPageList.size()) {
            BookPage bookPage = curPageList.get(pos);
            offsets[0] = bookPage.getCharStart();
            offsets[1] = bookPage.getCharEnd() - 1;
        }
    }

    private BookPage getCurPage(int pos) {
        notifyPageChange(pos);
        return curPageList.get(pos);
    }

    private void notifyCurPageChange() {
        if (pageChangeListener != null && curPage != null && curPage.position >= 0) {
            pageChangeListener.onPageChange(curPage.position);
        }
    }

    private void notifyPageChange(int pos) {
        if (pageChangeListener != null) {
            pageChangeListener.onPageChange(pos);
        }
    }

    private BookPage getPrevPage() {
        int pos = curPage.position - 1;
        if (pos < 0) {
            return null;
        }
        notifyPageChange(pos);
        return curPageList.get(pos);
    }

    private BookPage getNextPage() {
        int pos = curPage.position + 1;
        if (pos >= curPageList.size()) {
            return null;
        }
        notifyPageChange(pos);
        return curPageList.get(pos);
    }

    private BookPage getPrevLastPage() {
        int pos = curPageList.size() - 1;

        notifyPageChange(pos);

        return curPageList.get(pos);
    }

    private boolean canTurnPage() {

        if (!isChapterListPrepare) {
            return false;
        }

        if (status == STATUS_PARSE_ERROR
                || status == STATUS_PARING) {
            return false;
        } else if (status == STATUS_ERROR) {
            status = STATUS_LOADING;
        }
        return true;
    }

    public void destroy() {
        handler.removeCallbacksAndMessages(null);
    }

    public String getDescForCurrentPage() {
        if (curPage != null && curPage.lines != null) {
            for (String line : curPage.lines) {
                String trim = StringUtilsKt.fullToHalf(line).trim();
                if (!TextUtils.isEmpty(trim)) {
                    return trim;
                }
            }
        }

        return null;
    }

    public interface OnPageChangeListener {
        /**
         * 作用：章节切换的时候进行回调
         *
         * @param pos:切换章节的序号
         */
        void onChapterChange(int pos);

        void onChapterOpen(int pos);

        /**
         * 作用：章节目录加载完成时候回调
         *
         * @param chapters：返回章节目录
         */
        void onCategoryFinish(List<BookChapter> chapters);

        void onCanOperateSelectTextChange(boolean canOperateSelectText);

        /**
         * 作用：章节页码数量改变之后的回调。==> 字体大小的调整，或者是否关闭虚拟按钮功能都会改变页面的数量。
         *
         * @param count:页面的数量
         */
        void onPageCountChange(int count);

        /**
         * 作用：当页面改变的时候回调
         *
         * @param pos:当前的页面的序号
         */
        void onPageChange(int pos);
    }
}
