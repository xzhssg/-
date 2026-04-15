/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.ui;

import android.content.Context;
import android.graphics.Rect;
import android.text.Layout;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.widget.EditText;

import net.micode.notes.R;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义便签编辑框控件
 * 功能：处理清单模式的回车、删除、焦点、链接点击（电话/网址/邮箱）
 */
public class NoteEditText extends EditText {
    private static final String TAG = "NoteEditText";   // 日志标签
    private int mIndex;                                 // 当前编辑项在列表中的索引
    private int mSelectionStartBeforeDelete;            // 删除前光标位置

    // 链接协议类型
    private static final String SCHEME_TEL = "tel:";     // 电话
    private static final String SCHEME_HTTP = "http:";   // 网址
    private static final String SCHEME_EMAIL = "mailto:"; // 邮箱

    // 链接协议 → 菜单文字 映射
    private static final Map<String, Integer> sSchemaActionResMap = new HashMap<String, Integer>();
    static {
        sSchemaActionResMap.put(SCHEME_TEL, R.string.note_link_tel);
        sSchemaActionResMap.put(SCHEME_HTTP, R.string.note_link_web);
        sSchemaActionResMap.put(SCHEME_EMAIL, R.string.note_link_email);
    }

    /**
     * 文本变化回调接口
     * 由 NoteEditActivity 实现，处理删除、新增、显示隐藏复选框
     */
    public interface OnTextViewChangeListener {
        // 删除当前空文本时回调
        void onEditTextDelete(int index, String text);

        // 回车换行时回调
        void onEditTextEnter(int index, String text);

        // 文本变化（显示/隐藏选项）回调
        void onTextChange(int index, boolean hasText);
    }

    private OnTextViewChangeListener mOnTextViewChangeListener; // 回调监听器

    public NoteEditText(Context context) {
        super(context, null);
        mIndex = 0; // 初始化索引为0
    }

    /**
     * 设置当前项索引
     */
    public void setIndex(int index) {
        mIndex = index;
    }

    /**
     * 设置文本变化监听器
     */
    public void setOnTextViewChangeListener(OnTextViewChangeListener listener) {
        mOnTextViewChangeListener = listener;
    }

    public NoteEditText(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.editTextStyle);
    }

    public NoteEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 触摸事件：点击时精确设置光标位置
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 获取点击坐标并转换为文本偏移量
                int x = (int) event.getX();
                int y = (int) event.getY();
                x -= getTotalPaddingLeft();
                y -= getTotalPaddingTop();
                x += getScrollX();
                y += getScrollY();

                Layout layout = getLayout();
                int line = layout.getLineForVertical(y);    // 获取点击行
                int off = layout.getOffsetForHorizontal(line, x); // 获取点击偏移
                Selection.setSelection(getText(), off);      // 设置光标
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 按键按下：记录删除前光标位置
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                if (mOnTextViewChangeListener != null) {
                    return false; // 交给上层处理
                }
                break;
            case KeyEvent.KEYCODE_DEL:
                mSelectionStartBeforeDelete = getSelectionStart(); // 记录删除前光标
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 按键抬起：处理回车、删除逻辑
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_DEL:
                if (mOnTextViewChangeListener != null) {
                    // 光标在最前且不是第一项 → 删除该项
                    if (0 == mSelectionStartBeforeDelete && mIndex != 0) {
                        mOnTextViewChangeListener.onEditTextDelete(mIndex, getText().toString());
                        return true;
                    }
                } else {
                    Log.d(TAG, "OnTextViewChangeListener was not seted");
                }
                break;

            case KeyEvent.KEYCODE_ENTER:
                if (mOnTextViewChangeListener != null) {
                    // 回车 → 拆分文本，新建一项
                    int selectionStart = getSelectionStart();
                    String text = getText().subSequence(selectionStart, length()).toString();
                    setText(getText().subSequence(0, selectionStart));
                    mOnTextViewChangeListener.onEditTextEnter(mIndex + 1, text);
                } else {
                    Log.d(TAG, "OnTextViewChangeListener was not seted");
                }
                break;

            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 焦点变化：空内容时隐藏复选框
     */
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (mOnTextViewChangeListener != null) {
            if (!focused && TextUtils.isEmpty(getText())) {
                mOnTextViewChangeListener.onTextChange(mIndex, false);
            } else {
                mOnTextViewChangeListener.onTextChange(mIndex, true);
            }
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    /**
     * 创建长按菜单：识别链接（电话/网址/邮箱）并弹出操作菜单
     */
    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        if (getText() instanceof Spanned) {
            int selStart = getSelectionStart();
            int selEnd = getSelectionEnd();

            int min = Math.min(selStart, selEnd);
            int max = Math.max(selStart, selEnd);

            // 获取选中的链接
            final URLSpan[] urls = ((Spanned) getText()).getSpans(min, max, URLSpan.class);
            if (urls.length == 1) {
                int defaultResId = 0;
                // 匹配链接类型
                for(String schema: sSchemaActionResMap.keySet()) {
                    if(urls[0].getURL().indexOf(schema) >= 0) {
                        defaultResId = sSchemaActionResMap.get(schema);
                        break;
                    }
                }

                // 未匹配到则显示其他链接
                if (defaultResId == 0) {
                    defaultResId = R.string.note_link_other;
                }

                // 添加菜单并点击触发链接
                menu.add(0, 0, 0, defaultResId).setOnMenuItemClickListener(
                        new OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                urls[0].onClick(NoteEditText.this);
                                return true;
                            }
                        });
            }
        }
        super.onCreateContextMenu(menu);
    }
}
