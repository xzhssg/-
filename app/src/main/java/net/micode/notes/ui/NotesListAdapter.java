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
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import net.micode.notes.data.Notes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 笔记列表适配器
 * 作用：将数据库中的笔记/文件夹数据绑定到 ListView 上
 * 支持：单选、多选、全选、获取选中项ID、桌面小部件关联信息等功能
 */
public class NotesListAdapter extends CursorAdapter {
    // 日志TAG
    private static final String TAG = "NotesListAdapter";
    // 上下文
    private Context mContext;
    // 存储列表项的选中状态：key=位置position，value=是否选中
    private HashMap<Integer, Boolean> mSelectedIndex;
    // 笔记总数量（不含文件夹）
    private int mNotesCount;
    // 是否开启多选模式
    private boolean mChoiceMode;

    /**
     * 桌面小部件属性类
     * 用于记录笔记绑定的小部件ID和类型
     */
    public static class AppWidgetAttribute {
        public int widgetId;      // 小部件ID
        public int widgetType;   // 小部件类型（2x/4x）
    };

    /**
     * 构造方法
     */
    public NotesListAdapter(Context context) {
        super(context, null);
        mSelectedIndex = new HashMap<Integer, Boolean>();
        mContext = context;
        mNotesCount = 0;
    }

    /**
     * 创建列表项视图（每个条目对应的View）
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new NotesListItem(context);
    }

    /**
     * 绑定数据到列表项
     * 将Cursor中的数据设置到ItemView上显示
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof NotesListItem) {
            // 将Cursor封装成NoteItemData数据对象
            NoteItemData itemData = new NoteItemData(context, cursor);
            // 绑定数据，并传入是否多选模式、是否选中状态
            ((NotesListItem) view).bind(context, itemData, mChoiceMode,
                    isSelectedItem(cursor.getPosition()));
        }
    }

    /**
     * 设置指定位置的选中状态
     */
    public void setCheckedItem(final int position, final boolean checked) {
        mSelectedIndex.put(position, checked);
        // 通知列表刷新
        notifyDataSetChanged();
    }

    /**
     * 是否处于多选模式
     */
    public boolean isInChoiceMode() {
        return mChoiceMode;
    }

    /**
     * 设置多选模式
     * 切换时清空选中状态
     */
    public void setChoiceMode(boolean mode) {
        mSelectedIndex.clear();
        mChoiceMode = mode;
    }

    /**
     * 全选或取消全选
     * 只对笔记项有效，文件夹不参与选中
     */
    public void selectAll(boolean checked) {
        Cursor cursor = getCursor();
        for (int i = 0; i < getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                if (NoteItemData.getNoteType(cursor) == Notes.TYPE_NOTE) {
                    setCheckedItem(i, checked);
                }
            }
        }
    }

    /**
     * 获取所有选中项的ID集合
     */
    public HashSet<Long> getSelectedItemIds() {
        HashSet<Long> itemSet = new HashSet<Long>();
        for (Integer position : mSelectedIndex.keySet()) {
            if (mSelectedIndex.get(position) == true) {
                Long id = getItemId(position);
                if (id == Notes.ID_ROOT_FOLDER) {
                    Log.d(TAG, "Wrong item id, should not happen");
                } else {
                    itemSet.add(id);
                }
            }
        }

        return itemSet;
    }

    /**
     * 获取选中笔记对应的桌面小部件信息
     * 用于删除/移动笔记时更新小部件
     */
    public HashSet<AppWidgetAttribute> getSelectedWidget() {
        HashSet<AppWidgetAttribute> itemSet = new HashSet<AppWidgetAttribute>();
        for (Integer position : mSelectedIndex.keySet()) {
            if (mSelectedIndex.get(position) == true) {
                Cursor c = (Cursor) getItem(position);
                if (c != null) {
                    AppWidgetAttribute widget = new AppWidgetAttribute();
                    NoteItemData item = new NoteItemData(mContext, c);
                    widget.widgetId = item.getWidgetId();
                    widget.widgetType = item.getWidgetType();
                    itemSet.add(widget);
                    /**
                     * 此处不要关闭cursor，由适配器统一管理
                     */
                } else {
                    Log.e(TAG, "Invalid cursor");
                    return null;
                }
            }
        }
        return itemSet;
    }

    /**
     * 获取已选中的笔记数量
     */
    public int getSelectedCount() {
        Collection<Boolean> values = mSelectedIndex.values();
        if (null == values) {
            return 0;
        }
        Iterator<Boolean> iter = values.iterator();
        int count = 0;
        while (iter.hasNext()) {
            if (true == iter.next()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 判断是否全部笔记都已选中
     */
    public boolean isAllSelected() {
        int checkedCount = getSelectedCount();
        return (checkedCount != 0 && checkedCount == mNotesCount);
    }

    /**
     * 判断指定位置是否被选中
     */
    public boolean isSelectedItem(final int position) {
        if (null == mSelectedIndex.get(position)) {
            return false;
        }
        return mSelectedIndex.get(position);
    }

    /**
     * 数据内容变化时重新统计笔记数量
     */
    @Override
    protected void onContentChanged() {
        super.onContentChanged();
        calcNotesCount();
    }

    /**
     * 切换Cursor时重新统计笔记数量
     */
    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        calcNotesCount();
    }

    /**
     * 计算列表中笔记的总数量（排除文件夹）
     * 用于判断是否全选
     */
    private void calcNotesCount() {
        mNotesCount = 0;
        for (int i = 0; i < getCount(); i++) {
            Cursor c = (Cursor) getItem(i);
            if (c != null) {
                if (NoteItemData.getNoteType(c) == Notes.TYPE_NOTE) {
                    mNotesCount++;
                }
            } else {
                Log.e(TAG, "Invalid cursor");
                return;
            }
        }
    }
}
