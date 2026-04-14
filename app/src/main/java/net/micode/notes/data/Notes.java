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

package net.micode.notes.data;

import android.net.Uri;
/**
 * Notes类定义了便签应用中使用的常量，包括权限、URI、数据类型、系统文件夹ID等。
 * 该类作为数据契约，供其他组件引用，确保数据访问的一致性。
 */
public class Notes {
    // ContentProvider的授权标识，与AndroidManifest.xml中声明的authorities一致
    public static final String AUTHORITY = "micode_notes";
    // 日志标签
    public static final String TAG = "Notes";
    // 数据类型常量：便签
    public static final int TYPE_NOTE     = 0;
    // 数据类型常量：文件夹
    public static final int TYPE_FOLDER   = 1;
    // 数据类型常量：系统文件夹（如根目录、呼叫记录文件夹等）
    public static final int TYPE_SYSTEM   = 2;

    /**
     * 以下ID是系统文件夹的标识符
     * {@link Notes#ID_ROOT_FOLDER } 是默认文件夹（根文件夹）
     * {@link Notes#ID_TEMPARAY_FOLDER } 用于存放不属于任何文件夹的便签（临时文件夹）
     * {@link Notes#ID_CALL_RECORD_FOLDER} 用于存储通话记录便签
     */
    public static final int ID_ROOT_FOLDER = 0;          // 根文件夹ID
    public static final int ID_TEMPARAY_FOLDER = -1;     // 临时文件夹ID（注意原拼写为TEMPARAY，可能是笔误）
    public static final int ID_CALL_RECORD_FOLDER = -2;  // 通话记录文件夹ID
    public static final int ID_TRASH_FOLER = -3;         // 回收站文件夹ID（注意拼写为TRASH_FOLER）

    // Intent传递数据的Extra Key定义
    public static final String INTENT_EXTRA_ALERT_DATE = "net.micode.notes.alert_date";
    public static final String INTENT_EXTRA_BACKGROUND_ID = "net.micode.notes.background_color_id";
    public static final String INTENT_EXTRA_WIDGET_ID = "net.micode.notes.widget_id";
    public static final String INTENT_EXTRA_WIDGET_TYPE = "net.micode.notes.widget_type";
    public static final String INTENT_EXTRA_FOLDER_ID = "net.micode.notes.folder_id";
    public static final String INTENT_EXTRA_CALL_DATE = "net.micode.notes.call_date";

    // 桌面小部件类型常量
    public static final int TYPE_WIDGET_INVALIDE      = -1;   // 无效类型
    public static final int TYPE_WIDGET_2X            = 0;    // 2x2大小的小部件
    public static final int TYPE_WIDGET_4X            = 1;    // 4x4大小的小部件

    /**
     * 数据常量类，定义了数据的MIME类型
     */
    public static class DataConstants {
        // 文本便签的MIME类型
        public static final String NOTE = TextNote.CONTENT_ITEM_TYPE;
        // 通话记录便签的MIME类型
        public static final String CALL_NOTE = CallNote.CONTENT_ITEM_TYPE;
    }

    /**
     * 查询所有便签和文件夹的Uri
     */
    public static final Uri CONTENT_NOTE_URI = Uri.parse("content://" + AUTHORITY + "/note");

    /**
     * 查询数据的Uri
     */
    public static final Uri CONTENT_DATA_URI = Uri.parse("content://" + AUTHORITY + "/data");

    /**
     * 便签表（note）的列定义接口
     */
    public interface NoteColumns {
        // 唯一ID，主键，类型：INTEGER (long)
        public static final String ID = "_id";
        // 父文件夹ID，类型：INTEGER (long)
        public static final String PARENT_ID = "parent_id";
        // 创建时间，类型：INTEGER (long)
        public static final String CREATED_DATE = "created_date";
        // 最后修改时间，类型：INTEGER (long)
        public static final String MODIFIED_DATE = "modified_date";
        // 提醒时间，类型：INTEGER (long)
        public static final String ALERTED_DATE = "alert_date";
        // 文件夹名称或便签的文本内容片段，类型：TEXT
        public static final String SNIPPET = "snippet";
        // 便签关联的桌面小部件ID，类型：INTEGER (long)
        public static final String WIDGET_ID = "widget_id";
        // 便签关联的桌面小部件类型，类型：INTEGER (long)
        public static final String WIDGET_TYPE = "widget_type";
        // 背景颜色ID，类型：INTEGER (long)
        public static final String BG_COLOR_ID = "bg_color_id";
        // 是否有附件，类型：INTEGER (0/1)
        public static final String HAS_ATTACHMENT = "has_attachment";
        // 文件夹内的便签数量，类型：INTEGER (long)
        public static final String NOTES_COUNT = "notes_count";
        // 条目类型：文件夹或便签，类型：INTEGER
        public static final String TYPE = "type";
        // 最后同步ID（用于GTask同步），类型：INTEGER (long)
        public static final String SYNC_ID = "sync_id";
        // 本地是否已修改，类型：INTEGER (0/1)
        public static final String LOCAL_MODIFIED = "local_modified";
        // 移动到临时文件夹前的原始父文件夹ID，类型：INTEGER
        public static final String ORIGIN_PARENT_ID = "origin_parent_id";
        // GTask ID，类型：TEXT
        public static final String GTASK_ID = "gtask_id";
        // 版本号，用于乐观锁，类型：INTEGER (long)
        public static final String VERSION = "version";
    }

    /**
     * 数据表（data）的列定义接口
     */
    public interface DataColumns {
        // 唯一ID，主键，类型：INTEGER (long)
        public static final String ID = "_id";
        // MIME类型，标识数据的类型，类型：TEXT
        public static final String MIME_TYPE = "mime_type";
        // 所属便签的ID，外键，类型：INTEGER (long)
        public static final String NOTE_ID = "note_id";
        // 创建时间，类型：INTEGER (long)
        public static final String CREATED_DATE = "created_date";
        // 最后修改时间，类型：INTEGER (long)
        public static final String MODIFIED_DATE = "modified_date";
        // 数据内容，类型：TEXT
        public static final String CONTENT = "content";
        // 通用数据列1，含义由MIME_TYPE决定，用于存储整型数据，类型：INTEGER
        public static final String DATA1 = "data1";
        // 通用数据列2，含义由MIME_TYPE决定，用于存储整型数据，类型：INTEGER
        public static final String DATA2 = "data2";
        // 通用数据列3，含义由MIME_TYPE决定，用于存储文本数据，类型：TEXT
        public static final String DATA3 = "data3";
        // 通用数据列4，含义由MIME_TYPE决定，用于存储文本数据，类型：TEXT
        public static final String DATA4 = "data4";
        // 通用数据列5，含义由MIME_TYPE决定，用于存储文本数据，类型：TEXT
        public static final String DATA5 = "data5";
    }

    /**
     * 文本便签数据类，继承DataColumns，定义了文本便签特有的列和常量
     */
    public static final class TextNote implements DataColumns {
        // 模式：用于指示是否为清单模式，存储在DATA1列。类型：Integer，1：清单模式，0：普通模式
        public static final String MODE = DATA1;
        // 清单模式的值
        public static final int MODE_CHECK_LIST = 1;
        // 目录类型MIME
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/text_note";
        // 条目类型MIME
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/text_note";
        // 文本便签的Uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/text_note");
    }

    /**
     * 通话记录便签数据类，继承DataColumns，定义了通话记录特有的列和常量
     */
    public static final class CallNote implements DataColumns {
        // 通话日期，存储在DATA1列，类型：INTEGER (long)
        public static final String CALL_DATE = DATA1;
        // 电话号码，存储在DATA3列，类型：TEXT
        public static final String PHONE_NUMBER = DATA3;
        // 目录类型MIME
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/call_note";
        // 条目类型MIME
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/call_note";
        // 通话记录便签的Uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/call_note");
    }
}