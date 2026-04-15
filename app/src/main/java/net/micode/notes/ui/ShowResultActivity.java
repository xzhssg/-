package net.micode.notes.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.micode.notes.R;

import java.util.List;

/**
 * 搜索结果展示页面
 * 用于接收并显示便签搜索后的结果列表
 */
public class ShowResultActivity extends AppCompatActivity {
    /**
     * 日志TAG，用于调试打印
     */
    private String TAG="ShowResultActivity";

    /**
     * 忽略系统抛出的控件绑定类型错误警告
     */
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置页面布局为搜索结果列表布局
        setContentView(R.layout.search_list);
        // 打印日志，标记进入搜索结果页面
        Log.e(TAG,"search");

        // 找到布局中的ListView控件，用于展示搜索结果
        ListView listView = findViewById(R.id.listview);

        // 获取启动当前页面的意图（包含传递过来的数据）
        Intent intent = getIntent();
        // 判断意图是否为空
        if (intent != null) {
            // 从意图中获取传递过来的搜索结果字符串列表
            List<String> searchResult = intent.getStringArrayListExtra("searchResult");
            // 判断搜索结果是否不为null且不为空
            if (searchResult != null && !searchResult.isEmpty()) {
                // 创建数组适配器，将搜索结果绑定到系统自带的简单列表项布局
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, searchResult);
                // 给ListView设置适配器，显示搜索结果数据
                listView.setAdapter(adapter);
            }
        }
    }
}
