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
 * distributed under the License is distributed on an "AS IS " BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 闹钟广播接收器
 * 作用：接收系统闹钟触发的广播，启动闹钟提醒弹窗界面
 */
public class AlarmReceiver extends BroadcastReceiver {

    /**
     * 接收广播回调
     * @param context 上下文
     * @param intent 携带笔记ID的广播意图
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 将目标组件切换为闹钟提醒弹窗Activity
        intent.setClass(context, AlarmAlertActivity.class);
        // 添加新任务栈标记（广播接收器启动Activity必须添加此标记）
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 启动闹钟提醒界面
        context.startActivity(intent);
    }
}
