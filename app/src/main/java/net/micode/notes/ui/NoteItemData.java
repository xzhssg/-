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
import android.text.TextUtils;

import net.micode.notes.data.Contact;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.tool.DataUtils;

/**
 * 便签列表项数据封装类
 * 作用：从数据库Cursor中读取一条便签/文件夹数据，并封装成便于UI使用的实体类
 */
public class NoteItemData {
    /**
     * 查询数据库时需要获取的所有字段
     */
    static final String [] PROJECTION = new String [] {
        NoteColumns.ID,
        NoteColumns.ALERTED_DATE,
        NoteColumns.BG_COLOR_ID,
        NoteColumns.CREATED_DATE,
        NoteColumns.HAS_ATTACHMENT,
        NoteColumns.MODIFIED_DATE,
        NoteColumns.NOTES_COUNT,
        NoteColumns.PARENT_ID,
        NoteColumns.SNIPPET,
        NoteColumns.TYPE,
        NoteColumns.WIDGET_ID,
        NoteColumns.WIDGET_TYPE,
    };

    // 数据库字段索引常量定义
    private static final int ID_COLUMN                    = 0;
    private static final int ALERTED_DATE_COLUMN          = 1;
    private static final int BG_COLOR_ID_COLUMN           = 2;
    private static final int CREATED_DATE_COLUMN          = 3;
    private static final int HAS_ATTACHMENT_COLUMN        = 4;
    private static final int MODIFIED_DATE_COLUMN         = 5;
    private static final int NOTES_COUNT_COLUMN           = 6;
    private static final int PARENT_ID_COLUMN             = 7;
    private static final int SNIPPET_COLUMN               = 8;
    private static final int TYPE_COLUMN                  = 9;
    private static final int WIDGET_ID_COLUMN             = 10;
    private static final int WIDGET_TYPE_COLUMN           = 11;

    // 实体数据成员变量
    private long mId;
    private long mAlertDate;
    private int mBgColorId;
    private long mCreatedDate;
    private boolean mHasAttachment;
    private long mModifiedDate;
    private int mNotesCount;
    private long mParentId;
    private String mSnippet;
    private int mType;
    private int mWidgetId;
    private int mWidgetType;
    private String mName;
    private String mPhoneNumber;

    // 列表位置相关标记
    private boolean mIsLastItem;
    private boolean mIsFirstItem;
    private boolean mIsOnlyOneItem;
    private boolean mIsOneNoteFollowingFolder;
    private boolean mIsMultiNotesFollowingFolder;

    /**
     * 构造方法：从Cursor中解析数据并封装
     */
    public NoteItemData(Context context, Cursor cursor) {
        // 从游标读取基础字段
        mId = cursor.getLong(ID_COLUMN);
        mAlertDate = cursor.getLong(ALERTED_DATE_COLUMN);
        mBgColorId = cursor.getInt(BG_COLOR_ID_COLUMN);
        mCreatedDate = cursor.getLong(CREATED_DATE_COLUMN);
        mHasAttachment = (cursor.getInt(HAS_ATTACHMENT_COLUMN) > 0) ? true : false;
        mModifiedDate = cursor.getLong(MODIFIED_DATE_COLUMN);
        mNotesCount = cursor.getInt(NOTES_COUNT_COLUMN);
        mParentId = cursor.getLong(PARENT_ID_COLUMN);
        mSnippet = cursor.getString(SNIPPET_COLUMN);

        // 移除清单模式的勾选符号
        mSnippet = mSnippet.replace(NoteEditActivity.TAG_CHECKED, "").replace(
                NoteEditActivity.TAG_UNCHECKED, "");

        mType = cursor.getInt(TYPE_COLUMN);
        mWidgetId = cursor.getInt(WIDGET_ID_COLUMN);
        mWidgetType = cursor.getInt(WIDGET_TYPE_COLUMN);

        mPhoneNumber = "";
        // 如果是通话记录文件夹，读取电话号码和联系人姓名
        if (mParentId == Notes.ID_CALL_RECORD_FOLDER) {
            mPhoneNumber = DataUtils.getCallNumberByNoteId(context.getContentResolver(), mId);
            if (!TextUtils.isEmpty(mPhoneNumber)) {
                mName = Contact.getContact(context, mPhoneNumber);
                if (mName == null) {
                    mName = mPhoneNumber;
                }
            }
        }

        if (mName == null) {
            mName = "";
        }
        // 检查当前项在列表中的位置状态
        checkPostion(cursor);
    }

    /**
     * 检查当前条目在列表中的位置：是否第一个、最后一个、唯一一项等
     */
    private void checkPostion(Cursor cursor) {
        mIsLastItem = cursor.isLast() ? true : false;
        mIsFirstItem = cursor.isFirst() ? true : false;
        mIsOnlyOneItem = (cursor.getCount() == 1);
        mIsMultiNotesFollowingFolder = false;
        mIsOneNoteFollowingFolder = false;

        // 判断是否是文件夹后面紧跟着的便签（用于UI显示样式）
        if (mType == Notes.TYPE_NOTE && !mIsFirstItem) {
            int position = cursor.getPosition();
            if (cursor.moveToPrevious()) {
                if (cursor.getInt(TYPE_COLUMN) == Notes.TYPE_FOLDER
                        || cursor.getInt(TYPE_COLUMN) == Notes.TYPE_SYSTEM) {
                    if (cursor.getCount() > (position + 1)) {
                        mIsMultiNotesFollowingFolder = true;
                    } else {
                        mIsOneNoteFollowingFolder = true;
                    }
                }
                // 游标移回原位置
                if (!cursor.moveToNext()) {
                    throw new IllegalStateException("cursor move to previous but can't move back");
                }
            }
        }
    }

    /**
     * 判断是否是文件夹后唯一的一条便签
     */
    public boolean isOneFollowingFolder() {
        return mIsOneNoteFollowingFolder;
    }

    /**
     * 判断是否是文件夹后多条便签中的一条
     */
    public boolean isMultiFollowingFolder() {
        return mIsMultiNotesFollowingFolder;
    }

    /**
     * 是否是列表最后一项
     */
    public boolean isLast() {
        return mIsLastItem;
    }

    /**
     * 获取通话记录联系人名称
     */
    public String getCallName() {
        return mName;
    }

    /**
     * 是否是列表第一项
     */
    public boolean isFirst() {
        return mIsFirstItem;
    }

    /**
     * 是否列表只有一项
     */
    public boolean isSingle() {
        return mIsOnlyOneItem;
    }

    // ———————— 以下是各类getter方法 ————————
    public long getId() {
        return mId;
    }

    public long getAlertDate() {
        return mAlertDate;
    }

    public long getCreatedDate() {
        return mCreatedDate;
    }

    public boolean hasAttachment() {
        return mHasAttachment;
    }

    public long getModifiedDate() {
        return mModifiedDate;
    }

    public int getBgColorId() {
        return mBgColorId;
    }

    public long getParentId() {
        return mParentId;
    }

    public int getNotesCount() {
        return mNotesCount;
    }

    public long getFolderId () {
        return mParentId;
    }

    public int getType() {
        return mType;
    }

    public int getWidgetType() {
        return mWidgetType;
    }

    public int getWidgetId() {
        return mWidgetId;
    }

    public String getSnippet() {
        return mSnippet;
    }

    /**
     * 是否设置了提醒
     */
    public boolean hasAlert() {
        return (mAlertDate > 0);
    }

    /**
     * 是否是通话记录便签
     */
    public boolean isCallRecord() {
        return (mParentId == Notes.ID_CALL_RECORD_FOLDER && !TextUtils.isEmpty(mPhoneNumber));
    }

    /**
     * 静态方法：直接从Cursor获取便签类型
     */
    public static int getNoteType(Cursor cursor) {
        return cursor.getInt(TYPE_COLUMN);
    }
}
