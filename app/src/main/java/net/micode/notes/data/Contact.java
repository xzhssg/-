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

/**
 * 总结：
 * 该类实现了通过电话号码查询联系人信息的功能，包括：
 * 从联系人数据库中获取与特定电话号码相关联的显示名称。
 * 使用缓存机制减少数据库查询次数，提高性能。
 * 根据电话号码格式化查询条件，并执行数据库查询操作。
 * 处理查询结果，将联系人名称添加到缓存中，以便下次查询时直接获取。
 * 记录日志以便跟踪查询过程中的问题。
 */
package net.micode.notes.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.util.HashMap;

public class Contact {
    // 缓存已查询过的电话号码和对应的联系人名称，以减少数据库查询次数。
    private static HashMap<String, String> sContactCache;
    // 日志标签，用于标识该类的日志输出
    private static final String TAG = "Contact";

    // 用于查询具有完整国际号码格式的电话号码的selection字符串。
    // PHONE_NUMBERS_EQUAL是SQLite中的自定义函数，用于比较电话号码是否相等（忽略格式差异）。
    // 该查询会从Data表中查找MIME类型为Phone.CONTENT_ITEM_TYPE且RAW_CONTACT_ID在phone_lookup表中min_match为'+'的记录。
    // 目的是精确匹配电话号码。
    private static final String CALLER_ID_SELECTION = "PHONE_NUMBERS_EQUAL(" + Phone.NUMBER
            + ",?) AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'"
            + " AND " + Data.RAW_CONTACT_ID + " IN "
            + "(SELECT raw_contact_id "
            + " FROM phone_lookup"
            + " WHERE min_match = '+')";

    /**
     * 根据电话号码获取联系人名称。
     *
     * @param context     上下文对象，用于访问内容解析器。
     * @param phoneNumber 需要查询的电话号码。
     * @return 与电话号码相关联的联系人名称，如果找不到则返回null。
     */
    public static String getContact(Context context, String phoneNumber) {
        // 初始化联系人缓存，使用懒加载方式，第一次调用时创建HashMap
        if(sContactCache == null) {
            sContactCache = new HashMap<String, String>();
        }
        // 如果缓存中已经存在该电话号码的联系人名称，则直接返回缓存值，避免重复查询数据库
        if(sContactCache.containsKey(phoneNumber)) {
            return sContactCache.get(phoneNumber);
        }
        // 如果缓存中不存在，则进行数据库查询
        // 将CALLER_ID_SELECTION中的'+'替换为通过PhoneNumberUtils.toCallerIDMinMatch处理后的最小匹配前缀
        // 这是为了适配不同格式的电话号码匹配
        String selection = CALLER_ID_SELECTION.replace("+",
                PhoneNumberUtils.toCallerIDMinMatch(phoneNumber));
        // 使用ContentResolver查询联系人数据库获取联系人名称
        Cursor cursor = context.getContentResolver().query(
                Data.CONTENT_URI,
                new String [] { Phone.DISPLAY_NAME },
                selection,
                new String[] { phoneNumber },
                null);
        // 如果查询结果不为空，则获取联系人名称并存入缓存
        if (cursor != null && cursor.moveToFirst()) {
            try {
                String name = cursor.getString(0);
                sContactCache.put(phoneNumber, name); // 缓存查询结果
                return name;
            } catch (IndexOutOfBoundsException e) {
                // 记录错误日志，防止cursor字段索引错误导致崩溃
                Log.e(TAG, " Cursor get string error " + e.toString());
                return null;
            } finally {
                cursor.close(); // 确保Cursor被关闭，释放资源
            }
        } else {
            Log.d(TAG, "No contact matched with number:" + phoneNumber);
            return null;
        }
    }
}