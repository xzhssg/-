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

package net.micode.notes.gtask.data;

import android.database.Cursor;
import android.util.Log;

import net.micode.notes.tool.GTaskStringUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * MetaData类表示GTask同步中的元数据任务。
 * 它继承自Task，用于存储与特定便签关联的元信息（如关联的GTask ID）。
 * 在GTask中，元数据以特殊任务的形式存在于一个特定的任务列表中。
 */
public class MetaData extends Task {
    private final static String TAG = MetaData.class.getSimpleName();

    // 关联的GTask ID
    private String mRelatedGid = null;

    /**
     * 设置元数据信息
     * @param gid 关联的GTask ID
     * @param metaInfo 包含元信息的JSON对象
     */
    public void setMeta(String gid, JSONObject metaInfo) {
        try {
            metaInfo.put(GTaskStringUtils.META_HEAD_GTASK_ID, gid);
        } catch (JSONException e) {
            Log.e(TAG, "failed to put related gid");
        }
        setNotes(metaInfo.toString()); // 将整个JSON作为Task的notes字段存储
        setName(GTaskStringUtils.META_NOTE_NAME); // 固定名称，用于识别
    }

    public String getRelatedGid() {
        return mRelatedGid;
    }

    @Override
    public boolean isWorthSaving() {
        return getNotes() != null;
    }

    /**
     * 根据远程JSON设置内容，解析出关联的GTask ID
     */
    @Override
    public void setContentByRemoteJSON(JSONObject js) {
        super.setContentByRemoteJSON(js);
        if (getNotes() != null) {
            try {
                JSONObject metaInfo = new JSONObject(getNotes().trim());
                mRelatedGid = metaInfo.getString(GTaskStringUtils.META_HEAD_GTASK_ID);
            } catch (JSONException e) {
                Log.w(TAG, "failed to get related gid");
                mRelatedGid = null;
            }
        }
    }

    /**
     * 此方法不应被调用，因为MetaData不由本地JSON生成
     */
    @Override
    public void setContentByLocalJSON(JSONObject js) {
        // this function should not be called
        throw new IllegalAccessError("MetaData:setContentByLocalJSON should not be called");
    }

    /**
     * 此方法不应被调用
     */
    @Override
    public JSONObject getLocalJSONFromContent() {
        throw new IllegalAccessError("MetaData:getLocalJSONFromContent should not be called");
    }

    /**
     * 此方法不应被调用
     */
    @Override
    public int getSyncAction(Cursor c) {
        throw new IllegalAccessError("MetaData:getSyncAction should not be called");
    }
}