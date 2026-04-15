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

import java.util.Calendar;

import net.micode.notes.R;
import net.micode.notes.ui.DateTimePicker;
import net.micode.notes.ui.DateTimePicker.OnDateTimeChangedListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

/**
 * 日期时间选择对话框
 * 封装了 DateTimePicker 控件，提供弹窗式的日期时间选择
 */
public class DateTimePickerDialog extends AlertDialog implements OnClickListener {

    // 日历对象，用于存储当前选择的日期时间
    private Calendar mDate = Calendar.getInstance();
    // 是否为24小时制
    private boolean mIs24HourView;
    // 时间设置完成的回调监听器
    private OnDateTimeSetListener mOnDateTimeSetListener;
    // 自定义的日期时间选择控件
    private DateTimePicker mDateTimePicker;

    /**
     * 时间设置回调接口
     */
    public interface OnDateTimeSetListener {
        // 当时间设置完成时回调
        void OnDateTimeSet(AlertDialog dialog, long date);
    }

    /**
     * 构造方法：初始化日期时间选择对话框
     * @param context 上下文
     * @param date 初始显示的时间（毫秒值）
     */
    public DateTimePickerDialog(Context context, long date) {
        super(context);
        // 创建日期时间选择控件
        mDateTimePicker = new DateTimePicker(context);
        // 将自定义控件设置为对话框的显示内容
        setView(mDateTimePicker);

        // 为时间选择控件设置时间变化监听
        mDateTimePicker.setOnDateTimeChangedListener(new OnDateTimeChangedListener() {
            public void onDateTimeChanged(DateTimePicker view, int year, int month,
                    int dayOfMonth, int hourOfDay, int minute) {
                // 同步更新日历对象的时间
                mDate.set(Calendar.YEAR, year);
                mDate.set(Calendar.MONTH, month);
                mDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                mDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mDate.set(Calendar.MINUTE, minute);
                // 更新对话框标题为当前选择的时间
                updateTitle(mDate.getTimeInMillis());
            }
        });

        // 设置初始时间
        mDate.setTimeInMillis(date);
        // 秒数置0，便签提醒不需要精确到秒
        mDate.set(Calendar.SECOND, 0);
        // 将初始时间设置到控件上
        mDateTimePicker.setCurrentDate(mDate.getTimeInMillis());

        // 设置对话框【确定】按钮及点击事件
        setButton(context.getString(R.string.datetime_dialog_ok), this);
        // 设置对话框【取消】按钮，无点击事件
        setButton2(context.getString(R.string.datetime_dialog_cancel), (OnClickListener)null);
        // 根据系统设置自动切换24/12小时制
        set24HourView(DateFormat.is24HourFormat(this.getContext()));
        // 初始化对话框标题
        updateTitle(mDate.getTimeInMillis());
    }

    /**
     * 设置是否使用24小时制
     * @param is24HourView true=24小时制 false=12小时制
     */
    public void set24HourView(boolean is24HourView) {
        mIs24HourView = is24HourView;
    }

    /**
     * 设置时间设置完成的回调监听器
     * @param callBack 回调接口实现对象
     */
    public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) {
        mOnDateTimeSetListener = callBack;
    }

    /**
     * 更新对话框标题，显示当前选择的日期时间
     * @param date 时间毫秒值
     */
    private void updateTitle(long date) {
        // 定义时间显示格式：显示年、月、日、时间
        int flag =
            DateUtils.FORMAT_SHOW_YEAR |
            DateUtils.FORMAT_SHOW_DATE |
            DateUtils.FORMAT_SHOW_TIME;
        // 设置24/12小时制（此处代码原逻辑固定使用24小时制）
        flag |= mIs24HourView ? DateUtils.FORMAT_24HOUR : DateUtils.FORMAT_24HOUR;
        // 格式化时间并设置为对话框标题
        setTitle(DateUtils.formatDateTime(this.getContext(), date, flag));
    }

    /**
     * 对话框按钮点击事件
     */
    public void onClick(DialogInterface arg0, int arg1) {
        // 如果设置了回调，则将选择的时间返回
        if (mOnDateTimeSetListener != null) {
            mOnDateTimeSetListener.OnDateTimeSet(this, mDate.getTimeInMillis());
        }
    }

}
