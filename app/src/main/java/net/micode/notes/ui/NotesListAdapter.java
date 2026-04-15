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
 * 作用：将数据库中的笔记/文件夹数据，绑定到 ListView 列表项上
 * 功能：支持列表多选、全选、获取选中项ID、关联桌面小部件等
 */
public class NotesListAdapter extends CursorAdapter {
    // 日志标签
    private static final String TAG = "NotesListAdapter";
    // 上下文对象
    private Context mContext;
    // 存储列表项选中状态：key=位置position，value=是否选中
    private HashMap<Integer, Boolean> mSelectedIndex;
    // 当前列表中【普通笔记】的总数量（排除文件夹）
    private int mNotesCount;
    // 是否开启【多选模式】
    private boolean mChoiceMode;

    /**
     * 桌面小部件属性静态内部类
     * 用于记录笔记绑定的小部件ID和类型，删除/修改时同步更新小部件
     */
    public static class AppWidgetAttribute {
        public int widgetId;      // 小部件唯一ID
        public int widgetType;    // 小部件类型（2x/4x）
    };

    /**
     * 构造方法
     * 初始化选中状态集合、上下文、笔记数量
     */
    public NotesListAdapter(Context context) {
        super(context, null);
        mSelectedIndex = new HashMap<Integer, Boolean>();
        mContext = context;
        mNotesCount = 0;
    }

    /**
     * 创建新的列表项视图
     * 每个列表项都是一个 NotesListItem 自定义控件
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new NotesListItem(context);
    }

    /**
     * 绑定数据到视图
     * 将游标中的数据封装成 NoteItemData，设置给列表项
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof NotesListItem) {
            // 将游标数据封装为笔记数据对象
            NoteItemData itemData = new NoteItemData(context, cursor);
            // 绑定数据、多选模式、选中状态
            ((NotesListItem) view).bind(context, itemData, mChoiceMode,
                    isSelectedItem(cursor.getPosition()));
        }
    }

    /**
     * 设置指定位置的列表项为选中/未选中状态
     * @param position 列表项位置
     * @param checked  是否选中
     */
    public void setCheckedItem(final int position, final boolean checked) {
        mSelectedIndex.put(position, checked);
        // 通知列表刷新UI
        notifyDataSetChanged();
    }

    /**
     * 判断当前是否处于多选模式
     */
    public boolean isInChoiceMode() {
        return mChoiceMode;
    }

    /**
     * 设置是否开启多选模式
     * 开启时清空之前的选中记录
     */
    public void setChoiceMode(boolean mode) {
        mSelectedIndex.clear();
        mChoiceMode = mode;
    }

    /**
     * 全选/取消全选 所有【普通笔记】（跳过文件夹）
     * @param checked true=全选 false=取消全选
     */
    public void selectAll(boolean checked) {
        Cursor cursor = getCursor();
        for (int i = 0; i < getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                // 只选中普通笔记，不选中文件夹
                if (NoteItemData.getNoteType(cursor) == Notes.TYPE_NOTE) {
                    setCheckedItem(i, checked);
                }
            }
        }
    }

    /**
     * 获取所有选中项的ID集合
     * 用于批量删除、移动操作
     */
    public HashSet<Long> getSelectedItemIds() {
        HashSet<Long> itemSet = new HashSet<Long>();
        for (Integer position : mSelectedIndex.keySet()) {
            // 只处理选中状态的项
            if (mSelectedIndex.get(position) == true) {
                Long id = getItemId(position);
                // 排除根文件夹ID（不允许操作根目录）
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
     * 用于删除笔记后同步更新桌面小部件
     */
    public HashSet<AppWidgetAttribute> getSelectedWidget() {
        HashSet<AppWidgetAttribute> itemSet = new HashSet<AppWidgetAttribute>();
        for (Integer position : mSelectedIndex.keySet()) {
            if (mSelectedIndex.get(position) == true) {
                Cursor c = (Cursor) getItem(position);
                if (c != null) {
                    AppWidgetAttribute widget = new AppWidgetAttribute();
                    NoteItemData item = new NoteItemData(mContext, c);
                    // 读取笔记绑定的小部件ID和类型
                    widget.widgetId = item.getWidgetId();
                    widget.widgetType = item.getWidgetType();
                    itemSet.add(widget);
                    /**
                     * 此处不要关闭游标，游标由适配器统一管理
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
     * 获取当前选中的笔记数量
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
     * 判断是否已经全选了所有笔记
     * 选中数量 == 总笔记数 即为全选
     */
    public boolean isAllSelected() {
        int checkedCount = getSelectedCount();
        return (checkedCount != 0 && checkedCount == mNotesCount);
    }

    /**
     * 判断指定位置的列表项是否被选中
     */
    public boolean isSelectedItem(final int position) {
        if (null == mSelectedIndex.get(position)) {
            return false;
        }
        return mSelectedIndex.get(position);
    }

    /**
     * 数据内容发生变化时回调
     * 重新计算笔记总数
     */
    @Override
    protected void onContentChanged() {
        super.onContentChanged();
        calcNotesCount();
    }

    /**
     * 切换游标（数据源变化）时回调
     * 重新计算笔记总数
     */
    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        calcNotesCount();
    }

    /**
     * 计算当前列表中【普通笔记】的总数量（排除文件夹）
     * 用于判断是否全选
     */
    private void calcNotesCount() {
        mNotesCount = 0;
        for (int i = 0; i < getCount(); i++) {
            Cursor c = (Cursor) getItem(i);
            if (c != null) {
                // 只统计普通笔记
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
