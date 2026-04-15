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
import android.text.format.DateUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.tool.DataUtils;
import net.micode.notes.tool.ResourceParser.NoteItemBgResources;

/**
 * 笔记列表中的每一项条目视图
 * 对应列表里的一个笔记 / 文件夹 / 通话记录
 * 负责显示标题、时间、提醒图标、背景样式、多选框
 */
public class NotesListItem extends LinearLayout {
    // 提醒图标（闹钟/通话记录图标）
    private ImageView mAlert;
    // 标题/内容预览文本
    private TextView mTitle;
    // 修改时间文本
    private TextView mTime;
    // 通话记录联系人名称
    private TextView mCallName;
    // 当前列表项对应的数据实体（笔记/文件夹）
    private NoteItemData mItemData;
    // 多选模式下的勾选框
    private CheckBox mCheckBox;

    /**
     * 构造方法：加载列表项布局，初始化控件
     */
    public NotesListItem(Context context) {
        super(context);
        // 加载列表项布局 note_item.xml
        inflate(context, R.layout.note_item, this);
        // 初始化控件
        mAlert = (ImageView) findViewById(R.id.iv_alert_icon);
        mTitle = (TextView) findViewById(R.id.tv_title);
        mTime = (TextView) findViewById(R.id.tv_time);
        mCallName = (TextView) findViewById(R.id.tv_name);
        mCheckBox = (CheckBox) findViewById(android.R.id.checkbox);
    }

    /**
     * 绑定数据到当前列表项
     * @param context 上下文
     * @param data 列表项数据（笔记/文件夹）
     * @param choiceMode 是否开启多选模式
     * @param checked 是否选中
     */
    public void bind(Context context, NoteItemData data, boolean choiceMode, boolean checked) {
        // 如果是多选模式 且 当前项是普通笔记 → 显示勾选框
        if (choiceMode && data.getType() == Notes.TYPE_NOTE) {
            mCheckBox.setVisibility(View.VISIBLE);
            mCheckBox.setChecked(checked);
        } else {
            mCheckBox.setVisibility(View.GONE);
        }

        // 保存数据实体
        mItemData = data;

        // ====================== 1. 处理【通话记录文件夹】 ======================
        if (data.getId() == Notes.ID_CALL_RECORD_FOLDER) {
            mCallName.setVisibility(View.GONE);
            mAlert.setVisibility(View.VISIBLE);
            mTitle.setTextAppearance(context, R.style.TextAppearancePrimaryItem);
            // 设置标题：通话记录 + 包含条数
            mTitle.setText(context.getString(R.string.call_record_folder_name)
                    + context.getString(R.string.format_folder_files_count, data.getNotesCount()));
            // 设置通话记录图标
            mAlert.setImageResource(R.drawable.call_record);

        // ====================== 2. 处理【通话记录】子条目 ======================
        } else if (data.getParentId() == Notes.ID_CALL_RECORD_FOLDER) {
            mCallName.setVisibility(View.VISIBLE);
            mCallName.setText(data.getCallName());          // 显示联系人名称
            mTitle.setTextAppearance(context,R.style.TextAppearanceSecondaryItem);
            mTitle.setText(DataUtils.getFormattedSnippet(data.getSnippet())); // 显示内容预览

            // 设置提醒图标
            if (data.hasAlert()) {
                mAlert.setImageResource(R.drawable.clock);
                mAlert.setVisibility(View.VISIBLE);
            } else {
                mAlert.setVisibility(View.GONE);
            }

        // ====================== 3. 处理【普通文件夹/笔记】 ======================
        } else {
            mCallName.setVisibility(View.GONE);
            mTitle.setTextAppearance(context, R.style.TextAppearancePrimaryItem);

            // 如果是文件夹
            if (data.getType() == Notes.TYPE_FOLDER) {
                // 标题：文件夹名 + 包含条数
                mTitle.setText(data.getSnippet()
                        + context.getString(R.string.format_folder_files_count, data.getNotesCount()));
                mAlert.setVisibility(View.GONE); // 文件夹不显示提醒图标

            // 如果是普通笔记
            } else {
                mTitle.setText(DataUtils.getFormattedSnippet(data.getSnippet()));
                // 设置提醒图标
                if (data.hasAlert()) {
                    mAlert.setImageResource(R.drawable.clock);
                    mAlert.setVisibility(View.VISIBLE);
                } else {
                    mAlert.setVisibility(View.GONE);
                }
            }
        }

        // 设置最后修改时间（相对时间：刚刚、1小时前等）
        mTime.setText(DateUtils.getRelativeTimeSpanString(data.getModifiedDate()));

        // 设置列表项背景样式
        setBackground(data);
    }

    /**
     * 根据笔记位置（第一条/最后一条/中间/单条）设置不同背景
     * 实现列表项的圆角、间隔等视觉效果
     */
    private void setBackground(NoteItemData data) {
        int id = data.getBgColorId(); // 获取笔记背景色ID

        // 如果是【笔记】，根据位置设置不同背景
        if (data.getType() == Notes.TYPE_NOTE) {
            if (data.isSingle() || data.isOneFollowingFolder()) {
                // 单条笔记 / 文件夹后只有一条笔记
                setBackgroundResource(NoteItemBgResources.getNoteBgSingleRes(id));
            } else if (data.isLast()) {
                // 最后一条笔记
                setBackgroundResource(NoteItemBgResources.getNoteBgLastRes(id));
            } else if (data.isFirst() || data.isMultiFollowingFolder()) {
                // 第一条笔记
                setBackgroundResource(NoteItemBgResources.getNoteBgFirstRes(id));
            } else {
                // 中间普通笔记
                setBackgroundResource(NoteItemBgResources.getNoteBgNormalRes(id));
            }
        } else {
            // 文件夹使用统一背景
            setBackgroundResource(NoteItemBgResources.getFolderBgRes());
        }
    }

    /**
     * 获取当前列表项对应的数据实体
     */
    public NoteItemData getItemData() {
        return mItemData;
    }
}
