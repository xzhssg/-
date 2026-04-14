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

package net.micode.notes.gtask.remote;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

/**
 * GTask同步服务
 * <p>
 * 该服务负责在后台执行与Google Task的同步操作。它是一个可被启动的服务（Started Service），
 * 通过Intent传递操作类型（开始同步或取消同步）。服务内部使用一个异步任务（GTaskASyncTask）
 * 来执行实际的同步工作，并通过广播将同步状态和进度信息发送给UI组件。
 * </p>
 */
public class GTaskSyncService extends Service {

    /**
     * Intent中用于传递操作类型的键名
     */
    public final static String ACTION_STRING_NAME = "sync_action_type";

    /**
     * 操作类型：开始同步
     */
    public final static int ACTION_START_SYNC = 0;

    /**
     * 操作类型：取消同步
     */
    public final static int ACTION_CANCEL_SYNC = 1;

    /**
     * 操作类型：无效操作（默认值）
     */
    public final static int ACTION_INVALID = 2;

    /**
     * 服务状态广播的Action名称
     */
    public final static String GTASK_SERVICE_BROADCAST_NAME = "net.micode.notes.gtask.remote.gtask_sync_service";

    /**
     * 广播中用于表示是否正在同步的键名
     */
    public final static String GTASK_SERVICE_BROADCAST_IS_SYNCING = "isSyncing";

    /**
     * 广播中用于传递进度信息的键名
     */
    public final static String GTASK_SERVICE_BROADCAST_PROGRESS_MSG = "progressMsg";

    /**
     * 当前正在执行的同步任务实例，静态变量确保全局只有一个同步任务在运行
     */
    private static GTaskASyncTask mSyncTask = null;

    /**
     * 当前的同步进度信息字符串
     */
    private static String mSyncProgress = "";

    /**
     * 启动同步任务
     * <p>
     * 如果当前没有正在执行的同步任务，则创建一个新的GTaskASyncTask并开始执行。
     * 任务完成时会自动清理mSyncTask引用并停止服务。
     * </p>
     */
    private void startSync() {
        if (mSyncTask == null) {
            mSyncTask = new GTaskASyncTask(this, new GTaskASyncTask.OnCompleteListener() {
                @Override
                public void onComplete() {
                    mSyncTask = null;               // 任务完成，清空引用
                    sendBroadcast("");               // 发送广播通知同步结束
                    stopSelf();                      // 停止服务自身
                }
            });
            sendBroadcast("");                       // 发送广播通知同步开始
            mSyncTask.execute();                     // 启动异步任务
        }
    }

    /**
     * 取消当前正在执行的同步任务
     */
    private void cancelSync() {
        if (mSyncTask != null) {
            mSyncTask.cancelSync();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSyncTask = null;   // 确保服务创建时没有残留的同步任务引用
    }

    /**
     * 服务启动时的回调
     * <p>
     * 根据Intent中携带的操作类型决定执行开始同步或取消同步操作。
     * 返回START_STICKY表示如果服务被系统杀死，会尝试重新创建。
     * </p>
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.containsKey(ACTION_STRING_NAME)) {
            switch (bundle.getInt(ACTION_STRING_NAME, ACTION_INVALID)) {
                case ACTION_START_SYNC:
                    startSync();
                    break;
                case ACTION_CANCEL_SYNC:
                    cancelSync();
                    break;
                default:
                    break;
            }
            return START_STICKY;    // 服务被终止后自动重启
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 系统内存不足时的回调
     * <p>
     * 为了释放资源，如果当前有正在运行的同步任务，则取消它。
     * </p>
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mSyncTask != null) {
            mSyncTask.cancelSync();
        }
    }

    /**
     * 该服务不支持绑定，因此返回null
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 发送同步状态广播
     * <p>
     * 广播中包含两个关键信息：
     * <ul>
     *   <li>isSyncing: 当前是否正在同步（通过mSyncTask是否为null判断）</li>
     *   <li>progressMsg: 当前的同步进度描述信息</li>
     * </ul>
     * </p>
     *
     * @param msg 同步进度信息字符串
     */
    public void sendBroadcast(String msg) {
        mSyncProgress = msg;
        Intent intent = new Intent(GTASK_SERVICE_BROADCAST_NAME);
        intent.putExtra(GTASK_SERVICE_BROADCAST_IS_SYNCING, mSyncTask != null);
        intent.putExtra(GTASK_SERVICE_BROADCAST_PROGRESS_MSG, msg);
        sendBroadcast(intent);
    }

    /**
     * 静态方法：从Activity中启动同步服务
     *
     * @param activity 发起同步的Activity，用于设置GTaskManager的上下文
     */
    public static void startSync(Activity activity) {
        GTaskManager.getInstance().setActivityContext(activity);
        Intent intent = new Intent(activity, GTaskSyncService.class);
        intent.putExtra(GTaskSyncService.ACTION_STRING_NAME, GTaskSyncService.ACTION_START_SYNC);
        activity.startService(intent);
    }

    /**
     * 静态方法：取消当前的同步操作
     *
     * @param context 用于启动服务的上下文
     */
    public static void cancelSync(Context context) {
        Intent intent = new Intent(context, GTaskSyncService.class);
        intent.putExtra(GTaskSyncService.ACTION_STRING_NAME, GTaskSyncService.ACTION_CANCEL_SYNC);
        context.startService(intent);
    }

    /**
     * 静态方法：判断当前是否正在进行同步
     *
     * @return true表示正在同步，false表示未同步
     */
    public static boolean isSyncing() {
        return mSyncTask != null;
    }

    /**
     * 静态方法：获取当前的同步进度描述信息
     *
     * @return 进度字符串
     */
    public static String getProgressString() {
        return mSyncProgress;
    }
}