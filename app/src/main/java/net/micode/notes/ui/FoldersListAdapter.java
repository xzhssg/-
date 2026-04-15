/*
 * Copyright (c) 2010-2011, The MiCode Open Source (www.micode.net)
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;

/**
 * 文件夹列表适配器
 * 功能：用于将数据库中的文件夹数据显示在列表中（移动笔记时选择文件夹）
 */
public class FoldersListAdapter extends CursorAdapter {
    // 查询文件夹需要的字段：ID、名称（摘要字段存储文件夹名）
    public static final String [] PROJECTION = {
        NoteColumns.ID,        // 文件夹ID
        NoteColumns.SNIPPET    // 文件夹名称
    };

    // 字段对应的索引
    public static final int ID_COLUMN   = 0; // ID列索引
    public static final int NAME_COLUMN = 1; // 名称列索引

    /**
     * 构造方法
     * @param context 上下文
     * @param c 文件夹数据游标
     */
    public FoldersListAdapter(Context context, Cursor c) {
        super(context, c);
    }

    /**
     * 创建新的列表项视图
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new FolderListItem(context);
    }

    /**
     * 绑定数据到列表项
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof FolderListItem) {
            // 如果是根文件夹，显示“返回上级目录”，否则显示文件夹名称
            String folderName = (cursor.getLong(ID_COLUMN) == Notes.ID_ROOT_FOLDER) ? context
                    .getString(R.string.menu_move_parent_folder) : cursor.getString(NAME_COLUMN);
            ((FolderListItem) view).bind(folderName);
        }
    }

    /**
     * 根据位置获取文件夹名称
     * @param context 上下文
     * @param position 列表位置
     * @return 文件夹名称
     */
    public String getFolderName(Context context, int position) {
        Cursor cursor = (Cursor) getItem(position);
        // 根文件夹特殊处理
        return (cursor.getLong(ID_COLUMN) == Notes.ID_ROOT_FOLDER) ? context
                .getString(R.string.menu_move_parent_folder) : cursor.getString(NAME_COLUMN);
    }

    /**
     * 文件夹列表项自定义视图
     */
    private class FolderListItem extends LinearLayout {
        private TextView mName; // 文件夹名称文本

        public FolderListItem(Context context) {
            super(context);
            // 加载列表项布局
            inflate(context, R.layout.folder_list_item, this);
            // 获取名称控件
            mName = (TextView) findViewById(R.id.tv_folder_name);
        }

        /**
         * 绑定名称到视图
         * @param name 文件夹名称
         */
        public void bind(String name) {
            mName.setText(name);
        }
    }

}
