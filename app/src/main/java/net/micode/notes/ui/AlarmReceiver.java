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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 闹钟提醒广播接收器
 * 当设置的便签提醒时间到达时，系统会触发此接收器
 */
public class AlarmReceiver extends BroadcastReceiver {

    /**
     * 接收到闹钟广播时执行
     * @param context 上下文
     * @param intent 携带便签信息的意图
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 将意图目标页面切换到提醒弹窗页面
        intent.setClass(context, AlarmAlertActivity.class);
        // 添加新任务栈标记：广播接收器中启动Activity必须添加此标志
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 启动提醒页面，显示便签提醒
        context.startActivity(intent);
    }
}
