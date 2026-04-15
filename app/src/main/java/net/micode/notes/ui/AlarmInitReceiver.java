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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;

/**
 * 广播接收器：用于手机开机或重启后，重新恢复所有未触发的便签提醒
 * 作用：防止重启后闹钟失效
 */
public class AlarmInitReceiver extends BroadcastReceiver {

    /**
     * 查询数据库需要的字段：便签ID、提醒时间
     */
    private static final String [] PROJECTION = new String [] {
        NoteColumns.ID,            // 便签唯一ID
        NoteColumns.ALERTED_DATE    // 提醒时间
    };

    /** 字段索引：对应PROJECTION数组的位置，方便获取数据 */
    private static final int COLUMN_ID                = 0;  // ID列索引
    private static final int COLUMN_ALERTED_DATE      = 1;  // 提醒时间列索引

    /**
     * 接收到系统广播（开机/重启）时执行
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取当前系统时间
        long currentDate = System.currentTimeMillis();

        // 查询数据库：获取所有【提醒时间大于当前时间】的【普通便签】
        Cursor c = context.getContentResolver().query(
                Notes.CONTENT_NOTE_URI,       // 便签内容URI
                PROJECTION,                   // 要查询的字段
                // 查询条件：提醒时间 > 当前时间 并且 类型是普通便签
                NoteColumns.ALERTED_DATE + ">? AND " + NoteColumns.TYPE + "=" + Notes.TYPE_NOTE,
                new String[] { String.valueOf(currentDate) }, // 替换查询条件中的?
                null);

        // 如果查询结果不为空
        if (c != null) {
            // 移动到第一条数据
            if (c.moveToFirst()) {
                do {
                    // 获取便签的提醒时间
                    long alertDate = c.getLong(COLUMN_ALERTED_DATE);

                    // 创建意图：用于触发提醒广播
                    Intent sender = new Intent(context, AlarmReceiver.class);
                    // 给意图绑定当前便签的URI，用于区分哪个便签触发提醒
                    sender.setData(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, c.getLong(COLUMN_ID)));

                    // 创建延迟意图：闹钟到时后触发
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, sender, 0);

                    // 获取系统闹钟服务
                    AlarmManager alermManager = (AlarmManager) context
                            .getSystemService(Context.ALARM_SERVICE);

                    // 设置闹钟：RTC_WAKEUP 表示到时间唤醒CPU执行提醒
                    alermManager.set(AlarmManager.RTC_WAKEUP, alertDate, pendingIntent);

                } while (c.moveToNext()); // 循环遍历所有符合条件的便签
            }
            c.close(); // 关闭游标，释放资源
        }
    }
}
