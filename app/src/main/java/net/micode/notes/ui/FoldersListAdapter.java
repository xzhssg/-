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
 * See the License for the License for the specific language governing permissions and
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
 * 用于将数据库中的文件夹数据绑定到列表视图（ListView）中
 */
public class FoldersListAdapter extends CursorAdapter {
    /**
     * 查询数据库需要的字段：文件夹ID、文件夹名称
     */
    public static final String [] PROJECTION = {
        NoteColumns.ID,        // 文件夹ID
        NoteColumns.SNIPPET    // 文件夹名称（摘要字段复用）
    };

    /** 列索引定义，方便从Cursor中获取数据 */
    public static final int ID_COLUMN   = 0;  // ID列索引
    public static final int NAME_COLUMN = 1;  // 名称列索引

    /**
     * 构造方法
     * @param context 上下文
     * @param c 数据库查询游标
     */
    public FoldersListAdapter(Context context, Cursor c) {
        super(context, c);
        // TODO Auto-generated constructor stub
    }

    /**
     * 创建新的列表项视图
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new FolderListItem(context);
    }

    /**
     * 将数据绑定到列表项视图
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof FolderListItem) {
            // 判断是否为根文件夹，是则显示“上级目录”，否则显示实际文件夹名
            String folderName = (cursor.getLong(ID_COLUMN) == Notes.ID_ROOT_FOLDER) ? context
                    .getString(R.string.menu_move_parent_folder) : cursor.getString(NAME_COLUMN);
            // 绑定文件夹名称到视图
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
        // 根文件夹显示“上级目录”，其他显示实际名称
        return (cursor.getLong(ID_COLUMN) == Notes.ID_ROOT_FOLDER) ? context
                .getString(R.string.menu_move_parent_folder) : cursor.getString(NAME_COLUMN);
    }

    /**
     * 自定义文件夹列表项
     * 包含一个文本视图，用于显示文件夹名称
     */
    private class FolderListItem extends LinearLayout {
        private TextView mName;  // 显示文件夹名称的文本控件

        /**
         * 构造方法：加载列表项布局并初始化控件
         */
        public FolderListItem(Context context) {
            super(context);
            // 加载列表项布局
            inflate(context, R.layout.folder_list_item, this);
            // 绑定文件夹名称文本控件
            mName = (TextView) findViewById(R.id.tv_folder_name);
        }

        /**
         * 绑定数据：设置文件夹名称
         */
        public void bind(String name) {
            mName.setText(name);
        }
    }

}
