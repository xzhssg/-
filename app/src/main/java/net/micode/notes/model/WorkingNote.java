```java
// 版权声明第一行
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
// 空行
// 包声明
package net.micode.notes.model;
// 空行
// 导入AppWidgetManager
import android.appwidget.AppWidgetManager;
// 导入ContentUris
import android.content.ContentUris;
// 导入Context
import android.content.Context;
// 导入Cursor
import android.database.Cursor;
// 导入TextUtils
import android.text.TextUtils;
// 导入Log
import android.util.Log;
// 空行
// 导入Notes数据类
import net.micode.notes.data.Notes;
// 导入CallNote
import net.micode.notes.data.Notes.CallNote;
// 导入DataColumns
import net.micode.notes.data.Notes.DataColumns;
// 导入DataConstants
import net.micode.notes.data.Notes.DataConstants;
// 导入NoteColumns
import net.micode.notes.data.Notes.NoteColumns;
// 导入TextNote
import net.micode.notes.data.Notes.TextNote;
// 导入NoteBgResources
import net.micode.notes.tool.ResourceParser.NoteBgResources;
// 空行
// 空行
// WorkingNote类的定义
public class WorkingNote {
    // Note for the working note - 当前工作的笔记对象
    private Note mNote;
    // Note Id - 笔记ID
    private long mNoteId;
    // Note content - 笔记内容
    private String mContent;
    // Note mode - 笔记模式（普通/清单）
    private int mMode;
    // 空行
    // 提醒日期
    private long mAlertDate;
    // 空行
    // 修改日期
    private long mModifiedDate;
    // 空行
    // 背景颜色ID
    private int mBgColorId;
    // 空行
    // 小部件ID
    private int mWidgetId;
    // 空行
    // 小部件类型
    private int mWidgetType;
    // 空行
    // 所属文件夹ID
    private long mFolderId;
    // 空行
    // 上下文对象
    private Context mContext;
    // 空行
    // 日志标签
    private static final String TAG = "WorkingNote";
    // 空行
    // 是否已删除标志
    private boolean mIsDeleted;
    // 空行
    // 笔记设置变化监听器
    private NoteSettingChangedListener mNoteSettingStatusListener;
    // 空行
    // 数据查询投影
    public static final String[] DATA_PROJECTION = new String[] {
            DataColumns.ID,
            DataColumns.CONTENT,
            DataColumns.MIME_TYPE,
            DataColumns.DATA1,
            DataColumns.DATA2,
            DataColumns.DATA3,
            DataColumns.DATA4,
    };
    // 空行
    // 笔记查询投影
    public static final String[] NOTE_PROJECTION = new String[] {
            NoteColumns.PARENT_ID,
            NoteColumns.ALERTED_DATE,
            NoteColumns.BG_COLOR_ID,
            NoteColumns.WIDGET_ID,
            NoteColumns.WIDGET_TYPE,
            NoteColumns.MODIFIED_DATE
    };
    // 空行
    // 数据ID列的索引
    private static final int DATA_ID_COLUMN = 0;
    // 空行
    // 数据内容列的索引
    private static final int DATA_CONTENT_COLUMN = 1;
    // 空行
    // 数据MIME类型列的索引
    private static final int DATA_MIME_TYPE_COLUMN = 2;
    // 空行
    // 数据模式列的索引
    private static final int DATA_MODE_COLUMN = 3;
    // 空行
    // 笔记父文件夹ID列的索引
    private static final int NOTE_PARENT_ID_COLUMN = 0;
    // 空行
    // 笔记提醒日期列的索引
    private static final int NOTE_ALERTED_DATE_COLUMN = 1;
    // 空行
    // 笔记背景颜色ID列的索引
    private static final int NOTE_BG_COLOR_ID_COLUMN = 2;
    // 空行
    // 笔记小部件ID列的索引
    private static final int NOTE_WIDGET_ID_COLUMN = 3;
    // 空行
    // 笔记小部件类型列的索引
    private static final int NOTE_WIDGET_TYPE_COLUMN = 4;
    // 空行
    // 笔记修改日期列的索引
    private static final int NOTE_MODIFIED_DATE_COLUMN = 5;
    // 空行
    // 新建笔记的构造方法
    private WorkingNote(Context context, long folderId) {
        mContext = context;
        mAlertDate = 0;
        mModifiedDate = System.currentTimeMillis();
        mFolderId = folderId;
        mNote = new Note();
        mNoteId = 0;
        mIsDeleted = false;
        mMode = 0;
        mWidgetType = Notes.TYPE_WIDGET_INVALIDE;
    }
    // 空行
    // 已有笔记的构造方法
    private WorkingNote(Context context, long noteId, long folderId) {
        mContext = context;
        mNoteId = noteId;
        mFolderId = folderId;
        mIsDeleted = false;
        mNote = new Note();
        loadNote();
    }
    // 空行
    // 加载笔记数据（从数据库）
    private void loadNote() {
        Cursor cursor = mContext.getContentResolver().query(
                ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, mNoteId), NOTE_PROJECTION, null,
                null, null);
        // 空行
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                mFolderId = cursor.getLong(NOTE_PARENT_ID_COLUMN);
                mBgColorId = cursor.getInt(NOTE_BG_COLOR_ID_COLUMN);
                mWidgetId = cursor.getInt(NOTE_WIDGET_ID_COLUMN);
                mWidgetType = cursor.getInt(NOTE_WIDGET_TYPE_COLUMN);
                mAlertDate = cursor.getLong(NOTE_ALERTED_DATE_COLUMN);
                mModifiedDate = cursor.getLong(NOTE_MODIFIED_DATE_COLUMN);
            }
            cursor.close();
        } else {
            Log.e(TAG, "No note with id:" + mNoteId);
            throw new IllegalArgumentException("Unable to find note with id " + mNoteId);
        }
        loadNoteData();
    }
    // 空行
    // 加载笔记的数据内容（文本和通话数据）
    private void loadNoteData() {
        Cursor cursor = mContext.getContentResolver().query(Notes.CONTENT_DATA_URI, DATA_PROJECTION,
                DataColumns.NOTE_ID + "=?", new String[] {
                    String.valueOf(mNoteId)
                }, null);
        // 空行
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(DATA_MIME_TYPE_COLUMN);
                    if (DataConstants.NOTE.equals(type)) {
                        mContent = cursor.getString(DATA_CONTENT_COLUMN);
                        mMode = cursor.getInt(DATA_MODE_COLUMN);
                        mNote.setTextDataId(cursor.getLong(DATA_ID_COLUMN));
                    } else if (DataConstants.CALL_NOTE.equals(type)) {
                        mNote.setCallDataId(cursor.getLong(DATA_ID_COLUMN));
                    } else {
                        Log.d(TAG, "Wrong note type with type:" + type);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            Log.e(TAG, "No data with id:" + mNoteId);
            throw new IllegalArgumentException("Unable to find note's data with id " + mNoteId);
        }
    }
    // 空行
    // 创建一个空的笔记（静态工厂方法）
    public static WorkingNote createEmptyNote(Context context, long folderId, int widgetId,
            int widgetType, int defaultBgColorId) {
        WorkingNote note = new WorkingNote(context, folderId);
        note.setBgColorId(defaultBgColorId);
        note.setWidgetId(widgetId);
        note.setWidgetType(widgetType);
        return note;
    }
    // 空行
    // 加载已有的笔记（静态工厂方法）
    public static WorkingNote load(Context context, long id) {
        return new WorkingNote(context, id, 0);
    }
    // 空行
    // 同步保存笔记
    public synchronized boolean saveNote() {
        if (isWorthSaving()) {
            if (!existInDatabase()) {
                if ((mNoteId = Note.getNewNoteId(mContext, mFolderId)) == 0) {
                    Log.e(TAG, "Create new note fail with id:" + mNoteId);
                    return false;
                }
            }
            // 空行
            mNote.syncNote(mContext, mNoteId);
            // 空行
            /**
             * Update widget content if there exist any widget of this note
             * 如果存在该笔记的小部件，更新小部件内容
             */
            if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                    && mWidgetType != Notes.TYPE_WIDGET_INVALIDE
                    && mNoteSettingStatusListener != null) {
                mNoteSettingStatusListener.onWidgetChanged();
            }
            return true;
        } else {
            return false;
        }
    }
    // 空行
    // 判断笔记是否已存在于数据库
    public boolean existInDatabase() {
        return mNoteId > 0;
    }
    // 空行
    // 判断笔记是否值得保存（有实际更改）
    private boolean isWorthSaving() {
        if (mIsDeleted || (!existInDatabase() && TextUtils.isEmpty(mContent))
                || (existInDatabase() && !mNote.isLocalModified())) {
            return false;
        } else {
            return true;
        }
    }
    // 空行
    // 设置笔记设置变化监听器
    public void setOnSettingStatusChangedListener(NoteSettingChangedListener l) {
        mNoteSettingStatusListener = l;
    }
    // 空行
    // 设置提醒日期
    public void setAlertDate(long date, boolean set) {
        if (date != mAlertDate) {
            mAlertDate = date;
            mNote.setNoteValue(NoteColumns.ALERTED_DATE, String.valueOf(mAlertDate));
        }
        if (mNoteSettingStatusListener != null) {
            mNoteSettingStatusListener.onClockAlertChanged(date, set);
        }
    }
    // 空行
    // 标记删除状态
    public void markDeleted(boolean mark) {
        mIsDeleted = mark;
        if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                && mWidgetType != Notes.TYPE_WIDGET_INVALIDE && mNoteSettingStatusListener != null) {
                mNoteSettingStatusListener.onWidgetChanged();
        }
    }
    // 空行
    // 设置背景颜色ID
    public void setBgColorId(int id) {
        if (id != mBgColorId) {
            mBgColorId = id;
            if (mNoteSettingStatusListener != null) {
                mNoteSettingStatusListener.onBackgroundColorChanged();
            }
            mNote.setNoteValue(NoteColumns.BG_COLOR_ID, String.valueOf(id));
        }
    }
    // 空行
    // 设置清单模式
    public void setCheckListMode(int mode) {
        if (mMode != mode) {
            if (mNoteSettingStatusListener != null) {
                mNoteSettingStatusListener.onCheckListModeChanged(mMode, mode);
            }
            mMode = mode;
            mNote.setTextData(TextNote.MODE, String.valueOf(mMode));
        }
    }
    // 空行
    // 设置小部件类型
    public void setWidgetType(int type) {
        if (type != mWidgetType) {
            mWidgetType = type;
            mNote.setNoteValue(NoteColumns.WIDGET_TYPE, String.valueOf(mWidgetType));
        }
    }
    // 空行
    // 设置小部件ID
    public void setWidgetId(int id) {
        if (id != mWidgetId) {
            mWidgetId = id;
            mNote.setNoteValue(NoteColumns.WIDGET_ID, String.valueOf(mWidgetId));
        }
    }
    // 空行
    // 设置笔记文本内容
    public void setWorkingText(String text) {
        if (!TextUtils.equals(mContent, text)) {
            mContent = text;
            mNote.setTextData(DataColumns.CONTENT, mContent);
        }
    }
    // 空行
    // 转换为通话笔记
    public void convertToCallNote(String phoneNumber, long callDate) {
        mNote.setCallData(CallNote.CALL_DATE, String.valueOf(callDate));
        mNote.setCallData(CallNote.PHONE_NUMBER, phoneNumber);
        mNote.setNoteValue(NoteColumns.PARENT_ID, String.valueOf(Notes.ID_CALL_RECORD_FOLDER));
    }
    // 空行
    // 判断是否有闹钟提醒
    public boolean hasClockAlert() {
        return (mAlertDate > 0 ? true : false);
    }
    // 空行
    // 获取笔记内容
    public String getContent() {
        return mContent;
    }
    // 空行
    // 获取提醒日期
    public long getAlertDate() {
        return mAlertDate;
    }
    // 空行
    // 获取修改日期
    public long getModifiedDate() {
        return mModifiedDate;
    }
    // 空行
    // 获取背景颜色资源ID
    public int getBgColorResId() {
        return NoteBgResources.getNoteBgResource(mBgColorId);
    }
    // 空行
    // 获取背景颜色ID
    public int getBgColorId() {
        return mBgColorId;
    }
    // 空行
    // 获取标题背景资源ID
    public int getTitleBgResId() {
        return NoteBgResources.getNoteTitleBgResource(mBgColorId);
    }
    // 空行
    // 获取清单模式
    public int getCheckListMode() {
        return mMode;
    }
    // 空行
    // 获取笔记ID
    public long getNoteId() {
        return mNoteId;
    }
    // 空行
    // 获取文件夹ID
    public long getFolderId() {
        return mFolderId;
    }
    // 空行
    // 获取小部件ID
    public int getWidgetId() {
        return mWidgetId;
    }
    // 空行
    // 获取小部件类型
    public int getWidgetType() {
        return mWidgetType;
    }
    // 空行
    // 笔记设置变化监听器接口
    public interface NoteSettingChangedListener {
        /**
         * Called when the background color of current note has just changed
         * 当当前笔记的背景颜色刚刚改变时调用
         */
        void onBackgroundColorChanged();
        // 空行
        /**
         * Called when user set clock
         * 当用户设置时钟时调用
         */
        void onClockAlertChanged(long date, boolean set);
        // 空行
        /**
         * Call when user create note from widget
         * 当用户从小部件创建笔记时调用
         */
        void onWidgetChanged();
        // 空行
        /**
         * Call when switch between check list mode and normal mode
         * 当在清单模式和普通模式之间切换时调用
         * @param oldMode is previous mode before change
         * @param newMode is new mode
         */
        void onCheckListModeChanged(int oldMode, int newMode);
    }
}
```
