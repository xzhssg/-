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

package net.micode.notes.tool;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.CallNote;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.ui.NotesListAdapter.AppWidgetAttribute;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * 数据操作工具类
 * 提供笔记应用的核心数据增删改查、批量操作、数据校验等静态工具方法
 * 所有方法均为静态方法，直接通过类名调用
 */
public class DataUtils {
    // 日志TAG常量
    public static final String TAG = "DataUtils";

    /**
     * 批量删除笔记/文件夹
     * @param resolver 内容解析器，用于访问ContentProvider
     * @param ids 要删除的笔记/文件夹ID集合
     * @return 删除成功返回true，失败返回false
     */
    public static boolean batchDeleteNotes(ContentResolver resolver, HashSet<Long> ids) {
        // 参数校验：ID集合为空，直接返回成功
        if (ids == null) {
            Log.d(TAG, "the ids is null");
            return true;
        }
        // 参数校验：ID集合无数据，直接返回成功
        if (ids.size() == 0) {
            Log.d(TAG, "no id is in the hashset");
            return true;
        }

        // 创建批量操作列表，用于批量执行删除
        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        for (long id : ids) {
            // 系统根文件夹禁止删除，跳过
            if(id == Notes.ID_ROOT_FOLDER) {
                Log.e(TAG, "Don't delete system folder root");
                continue;
            }
            // 构建删除操作，根据ID删除对应笔记
            ContentProviderOperation.Builder builder = ContentProviderOperation
                    .newDelete(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, id));
            operationList.add(builder.build());
        }
        try {
            // 执行批量操作
            ContentProviderResult[] results = resolver.applyBatch(Notes.AUTHORITY, operationList);
            // 校验操作结果
            if (results == null || results.length == 0 || results[0] == null) {
                Log.d(TAG, "delete notes failed, ids:" + ids.toString());
                return false;
            }
            return true;
        } catch (RemoteException e) {
            // 捕获跨进程异常
            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
        } catch (OperationApplicationException e) {
            // 捕获批量操作异常
            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
        }
        return false;
    }

    /**
     * 将单个笔记从源文件夹移动到目标文件夹
     * @param resolver 内容解析器
     * @param id 笔记ID
     * @param srcFolderId 源文件夹ID
     * @param desFolderId 目标文件夹ID
     */
    public static void moveNoteToFoler(ContentResolver resolver, long id, long srcFolderId, long desFolderId) {
        // 封装要更新的字段数据
        ContentValues values = new ContentValues();
        values.put(NoteColumns.PARENT_ID, desFolderId);          // 更新父文件夹ID
        values.put(NoteColumns.ORIGIN_PARENT_ID, srcFolderId);  // 记录原始父文件夹ID
        values.put(NoteColumns.LOCAL_MODIFIED, 1);              // 标记本地数据已修改
        // 执行更新操作
        resolver.update(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, id), values, null, null);
    }

    /**
     * 批量移动笔记到指定文件夹
     * @param resolver 内容解析器
     * @param ids 要移动的笔记ID集合
     * @param folderId 目标文件夹ID
     * @return 移动成功返回true，失败返回false
     */
    public static boolean batchMoveToFolder(ContentResolver resolver, HashSet<Long> ids,
            long folderId) {
        // 参数校验：ID集合为空，直接返回成功
        if (ids == null) {
            Log.d(TAG, "the ids is null");
            return true;
        }

        // 创建批量更新操作列表
        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        for (long id : ids) {
            // 构建更新操作
            ContentProviderOperation.Builder builder = ContentProviderOperation
                    .newUpdate(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, id));
            builder.withValue(NoteColumns.PARENT_ID, folderId);    // 设置目标父文件夹
            builder.withValue(NoteColumns.LOCAL_MODIFIED, 1);      // 标记修改状态
            operationList.add(builder.build());
        }

        try {
            // 执行批量移动操作
            ContentProviderResult[] results = resolver.applyBatch(Notes.AUTHORITY, operationList);
            // 校验操作结果
            if (results == null || results.length == 0 || results[0] == null) {
                Log.d(TAG, "delete notes failed, ids:" + ids.toString());
                return false;
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
        } catch (OperationApplicationException e) {
            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
        }
        return false;
    }

    /**
     * 获取用户创建的文件夹数量（排除系统文件夹和垃圾箱）
     * @param resolver 内容解析器
     * @return 用户文件夹总数
     */
    public static int getUserFolderCount(ContentResolver resolver) {
        // 查询：统计类型为文件夹、且不在垃圾箱中的数据数量
        Cursor cursor =resolver.query(Notes.CONTENT_NOTE_URI,
                new String[] { "COUNT(*)" },
                NoteColumns.TYPE + "=? AND " + NoteColumns.PARENT_ID + "<>?",
                new String[] { String.valueOf(Notes.TYPE_FOLDER), String.valueOf(Notes.ID_TRASH_FOLER)},
                null);

        int count = 0;
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                try {
                    // 获取统计数量
                    count = cursor.getInt(0);
                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG, "get folder count failed:" + e.toString());
                } finally {
                    // 关闭游标，释放资源
                    cursor.close();
                }
            }
        }
        return count;
    }

    /**
     * 判断指定ID的笔记是否在数据库中可见（存在且不在垃圾箱）
     * @param resolver 内容解析器
     * @param noteId 笔记ID
     * @param type 笔记类型
     * @return 可见返回true，不可见返回false
     */
    public static boolean visibleInNoteDatabase(ContentResolver resolver, long noteId, int type) {
        // 查询指定ID、指定类型，且不在垃圾箱的笔记
        Cursor cursor = resolver.query(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId),
                null,
                NoteColumns.TYPE + "=? AND " + NoteColumns.PARENT_ID + "<>" + Notes.ID_TRASH_FOLER,
                new String [] {String.valueOf(type)},
                null);

        boolean exist = false;
        if (cursor != null) {
            // 查询结果数量>0表示存在
            if (cursor.getCount() > 0) {
                exist = true;
            }
            cursor.close();
        }
        return exist;
    }

    /**
     * 判断指定ID的笔记是否存在于数据库中
     * @param resolver 内容解析器
     * @param noteId 笔记ID
     * @return 存在返回true，不存在返回false
     */
    public static boolean existInNoteDatabase(ContentResolver resolver, long noteId) {
        // 根据ID直接查询笔记
        Cursor cursor = resolver.query(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId),
                null, null, null, null);

        boolean exist = false;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                exist = true;
            }
            cursor.close();
        }
        return exist;
    }

    /**
     * 判断指定ID的数据详情是否存在于数据库中
     * @param resolver 内容解析器
     * @param dataId 数据详情ID
     * @return 存在返回true，不存在返回false
     */
    public static boolean existInDataDatabase(ContentResolver resolver, long dataId) {
        // 根据ID查询数据详情表
        Cursor cursor = resolver.query(ContentUris.withAppendedId(Notes.CONTENT_DATA_URI, dataId),
                null, null, null, null);

        boolean exist = false;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                exist = true;
            }
            cursor.close();
        }
        return exist;
    }

    /**
     * 校验文件夹名称是否已存在（可见的文件夹）
     * @param resolver 内容解析器
     * @param name 要校验的文件夹名称
     * @return 已存在返回true，不存在返回false
     */
    public static boolean checkVisibleFolderName(ContentResolver resolver, String name) {
        // 查询：类型为文件夹、不在垃圾箱、名称匹配的记录
        Cursor cursor = resolver.query(Notes.CONTENT_NOTE_URI, null,
                NoteColumns.TYPE + "=" + Notes.TYPE_FOLDER +
                " AND " + NoteColumns.PARENT_ID + "<>" + Notes.ID_TRASH_FOLER +
                " AND " + NoteColumns.SNIPPET + "=?",
                new String[] { name }, null);
        boolean exist = false;
        if(cursor != null) {
            if(cursor.getCount() > 0) {
                exist = true;
            }
            cursor.close();
        }
        return exist;
    }

    /**
     * 获取指定文件夹绑定的桌面小部件信息
     * @param resolver 内容解析器
     * @param folderId 文件夹ID
     * @return 小部件属性集合
     */
    public static HashSet<AppWidgetAttribute> getFolderNoteWidget(ContentResolver resolver, long folderId) {
        // 查询文件夹对应的小部件ID和类型
        Cursor c = resolver.query(Notes.CONTENT_NOTE_URI,
                new String[] { NoteColumns.WIDGET_ID, NoteColumns.WIDGET_TYPE },
                NoteColumns.PARENT_ID + "=?",
                new String[] { String.valueOf(folderId) },
                null);

        HashSet<AppWidgetAttribute> set = null;
        if (c != null) {
            if (c.moveToFirst()) {
                set = new HashSet<AppWidgetAttribute>();
                do {
                    try {
                        // 封装小部件信息
                        AppWidgetAttribute widget = new AppWidgetAttribute();
                        widget.widgetId = c.getInt(0);
                        widget.widgetType = c.getInt(1);
                        set.add(widget);
                    } catch (IndexOutOfBoundsException e) {
                        Log.e(TAG, e.toString());
                    }
                } while (c.moveToNext());
            }
            c.close();
        }
        return set;
    }

    /**
     * 根据笔记ID查询通话笔记的电话号码
     * @param resolver 内容解析器
     * @param noteId 笔记ID
     * @return 电话号码，无结果返回空字符串
     */
    public static String getCallNumberByNoteId(ContentResolver resolver, long noteId) {
        // 查询通话记录类型数据的电话号码
        Cursor cursor = resolver.query(Notes.CONTENT_DATA_URI,
                new String [] { CallNote.PHONE_NUMBER },
                CallNote.NOTE_ID + "=? AND " + CallNote.MIME_TYPE + "=?",
                new String [] { String.valueOf(noteId), CallNote.CONTENT_ITEM_TYPE },
                null);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                return cursor.getString(0);
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "Get call number fails " + e.toString());
            } finally {
                cursor.close();
            }
        }
        return "";
    }

    /**
     * 根据电话号码和通话日期，查询对应的通话笔记ID
     * @param resolver 内容解析器
     * @param phoneNumber 电话号码
     * @param callDate 通话日期
     * @return 笔记ID，无结果返回0
     */
    public static long getNoteIdByPhoneNumberAndCallDate(ContentResolver resolver, String phoneNumber, long callDate) {
        // 条件查询：通话日期、类型、电话号码匹配
        Cursor cursor = resolver.query(Notes.CONTENT_DATA_URI,
                new String [] { CallNote.NOTE_ID },
                CallNote.CALL_DATE + "=? AND " + CallNote.MIME_TYPE + "=? AND PHONE_NUMBERS_EQUAL("
                + CallNote.PHONE_NUMBER + ",?)",
                new String [] { String.valueOf(callDate), CallNote.CONTENT_ITEM_TYPE, phoneNumber },
                null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                try {
                    return cursor.getLong(0);
                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG, "Get call note id fails " + e.toString());
                }
            }
            cursor.close();
        }
        return 0;
    }

    /**
     * 根据笔记ID获取笔记摘要信息
     * @param resolver 内容解析器
     * @param noteId 笔记ID
     * @return 笔记摘要
     */
    public static String getSnippetById(ContentResolver resolver, long noteId) {
        // 根据ID查询笔记摘要
        Cursor cursor = resolver.query(Notes.CONTENT_NOTE_URI,
                new String [] { NoteColumns.SNIPPET },
                NoteColumns.ID + "=?",
                new String [] { String.valueOf(noteId)},
                null);

        if (cursor != null) {
            String snippet = "";
            if (cursor.moveToFirst()) {
                snippet = cursor.getString(0);
            }
            cursor.close();
            return snippet;
        }
        // 无对应笔记，抛出异常
        throw new IllegalArgumentException("Note is not found with id: " + noteId);
    }

    /**
     * 格式化笔记摘要：只保留第一行文本
     * @param snippet 原始摘要
     * @return 格式化后的单行摘要
     */
    public static String getFormattedSnippet(String snippet) {
        if (snippet != null) {
            // 去除首尾空格
            snippet = snippet.trim();
            // 找到第一个换行符，只截取第一行
            int index = snippet.indexOf('\n');
            if (index != -1) {
                snippet = snippet.substring(0, index);
            }
        }
        return snippet;
    }
}
