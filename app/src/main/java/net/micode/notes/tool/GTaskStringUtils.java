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

/**
 * Google Task 同步相关字符串常量工具类
 * 作用：统一管理笔记与 Google 任务同步时使用的 JSON 字段名、文件夹名称、元数据标识
 */
public class GTaskStringUtils {

    // ==================== Google Task JSON 关键字段 ====================
    public final static String GTASK_JSON_ACTION_ID = "action_id";                // 操作ID
    public final static String GTASK_JSON_ACTION_LIST = "action_list";            // 操作列表
    public final static String GTASK_JSON_ACTION_TYPE = "action_type";            // 操作类型
    public final static String GTASK_JSON_ACTION_TYPE_CREATE = "create";         // 创建操作
    public final static String GTASK_JSON_ACTION_TYPE_GETALL = "get_all";        // 获取全部操作
    public final static String GTASK_JSON_ACTION_TYPE_MOVE = "move";             // 移动操作
    public final static String GTASK_JSON_ACTION_TYPE_UPDATE = "update";         // 更新操作

    public final static String GTASK_JSON_CREATOR_ID = "creator_id";              // 创建者ID
    public final static String GTASK_JSON_CHILD_ENTITY = "child_entity";          // 子实体
    public final static String GTASK_JSON_CLIENT_VERSION = "client_version";      // 客户端版本
    public final static String GTASK_JSON_COMPLETED = "completed";                // 是否已完成
    public final static String GTASK_JSON_CURRENT_LIST_ID = "current_list_id";    // 当前列表ID
    public final static String GTASK_JSON_DEFAULT_LIST_ID = "default_list_id";    // 默认列表ID
    public final static String GTASK_JSON_DELETED = "deleted";                    // 是否已删除
    public final static String GTASK_JSON_DEST_LIST = "dest_list";                // 目标列表
    public final static String GTASK_JSON_DEST_PARENT = "dest_parent";            // 目标父项
    public final static String GTASK_JSON_DEST_PARENT_TYPE = "dest_parent_type"; // 目标父项类型
    public final static String GTASK_JSON_ENTITY_DELTA = "entity_delta";          // 实体变化量
    public final static String GTASK_JSON_ENTITY_TYPE = "entity_type";            // 实体类型
    public final static String GTASK_JSON_GET_DELETED = "get_deleted";            // 获取已删除项
    public final static String GTASK_JSON_ID = "id";                               // 唯一标识
    public final static String GTASK_JSON_INDEX = "index";                        // 排序索引
    public final static String GTASK_JSON_LAST_MODIFIED = "last_modified";       // 最后修改时间
    public final static String GTASK_JSON_LATEST_SYNC_POINT = "latest_sync_point"; // 最新同步点
    public final static String GTASK_JSON_LIST_ID = "list_id";                    // 列表ID
    public final static String GTASK_JSON_LISTS = "lists";                        // 列表集合
    public final static String GTASK_JSON_NAME = "name";                          // 名称
    public final static String GTASK_JSON_NEW_ID = "new_id";                      // 新ID
    public final static String GTASK_JSON_NOTES = "notes";                        // 笔记内容
    public final static String GTASK_JSON_PARENT_ID = "parent_id";                // 父项ID
    public final static String GTASK_JSON_PRIOR_SIBLING_ID = "prior_sibling_id"; // 上一个兄弟节点ID
    public final static String GTASK_JSON_RESULTS = "results";                    // 结果
    public final static String GTASK_JSON_SOURCE_LIST = "source_list";            // 源列表
    public final static String GTASK_JSON_TASKS = "tasks";                        // 任务集合
    public final static String GTASK_JSON_TYPE = "type";                          // 类型
    public final static String GTASK_JSON_TYPE_GROUP = "GROUP";                   // 分组类型
    public final static String GTASK_JSON_TYPE_TASK = "TASK";                    // 任务类型
    public final static String GTASK_JSON_USER = "user";                          // 用户信息

    // ==================== 文件夹命名规则 ====================
    public final static String MIUI_FOLDER_PREFFIX = "[MIUI_Notes]";             // MIUI 笔记文件夹前缀
    public final static String FOLDER_DEFAULT = "Default";                       // 默认文件夹
    public final static String FOLDER_CALL_NOTE = "Call_Note";                   // 通话记录文件夹
    public final static String FOLDER_META = "METADATA";                         // 元数据文件夹

    // ==================== 元数据（Meta）相关标识 ====================
    public final static String META_HEAD_GTASK_ID = "meta_gid";                  // 元数据：Google Task ID
    public final static String META_HEAD_NOTE = "meta_note";                     // 元数据：笔记信息
    public final static String META_HEAD_DATA = "meta_data";                     // 元数据：数据内容
    public final static String META_NOTE_NAME = "[META INFO] DON'T UPDATE AND DELETE"; // 元数据备注（禁止修改删除）
}
