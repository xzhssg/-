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
import org.json.JSONObject;

/**
 * 抽象基类Node，代表GTask同步树中的一个节点。
 * 定义了同步操作类型常量以及节点基本属性（Gid、名称、最后修改时间、删除标记）。
 * 子类需要实现与JSON转换和同步动作判断相关的方法。
 */
public abstract class Node {
    // 同步动作常量
    public static final int SYNC_ACTION_NONE = 0;               // 无动作
    public static final int SYNC_ACTION_ADD_REMOTE = 1;         // 添加到远程
    public static final int SYNC_ACTION_ADD_LOCAL = 2;          // 添加到本地
    public static final int SYNC_ACTION_DEL_REMOTE = 3;         // 删除远程
    public static final int SYNC_ACTION_DEL_LOCAL = 4;          // 删除本地
    public static final int SYNC_ACTION_UPDATE_REMOTE = 5;      // 更新远程
    public static final int SYNC_ACTION_UPDATE_LOCAL = 6;       // 更新本地
    public static final int SYNC_ACTION_UPDATE_CONFLICT = 7;    // 冲突
    public static final int SYNC_ACTION_ERROR = 8;              // 错误

    private String mGid;            // Google Task ID
    private String mName;           // 节点名称
    private long mLastModified;     // 最后修改时间
    private boolean mDeleted;       // 是否已删除

    public Node() {
        mGid = null;
        mName = "";
        mLastModified = 0;
        mDeleted = false;
    }

    // 抽象方法，子类必须实现
    public abstract JSONObject getCreateAction(int actionId);
    public abstract JSONObject getUpdateAction(int actionId);
    public abstract void setContentByRemoteJSON(JSONObject js);
    public abstract void setContentByLocalJSON(JSONObject js);
    public abstract JSONObject getLocalJSONFromContent();
    public abstract int getSyncAction(Cursor c);

    // Getter和Setter
    public void setGid(String gid) { this.mGid = gid; }
    public void setName(String name) { this.mName = name; }
    public void setLastModified(long lastModified) { this.mLastModified = lastModified; }
    public void setDeleted(boolean deleted) { this.mDeleted = deleted; }
    public String getGid() { return this.mGid; }
    public String getName() { return this.mName; }
    public long getLastModified() { return this.mLastModified; }
    public boolean getDeleted() { return this.mDeleted; }
}