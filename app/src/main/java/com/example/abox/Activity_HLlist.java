package com.example.abox;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Activity_HLlist extends AppCompatActivity {

    private SQLiteDatabase db;
    private ListView lV4_HLlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hllist);

        // 创建标题栏返回键
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        lV4_HLlist = findViewById(R.id.lV4_HLlist);

        // 初始化数据库，获取读写权限
        MySQLiteOpenHelper mySQLiteOH = new MySQLiteOpenHelper(Activity_HLlist.this, "appdata.db", null, 1);
        db = mySQLiteOH.getWritableDatabase();

        // 遍历数据库
        Cursor cursor = db.rawQuery("SELECT * FROM HLdata", null);
        // 若数据库表不为空
        if (cursor.getCount() != 0) {
            List<String> strlist = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    // 读取时间列
                    String dateT = cursor.getString(cursor.getColumnIndex("time"));

                    // 读取人体活动列
                    String humanT = cursor.getString(
                            cursor.getColumnIndex("human")
                    ).equals("1") ? "有人活动" : "无人活动";

                    // 读取光感列
                    String lightT = cursor.getString(cursor.getColumnIndex("light"));
                    switch (lightT){
                        case "1":
                            lightT = "光感亮";
                            break;
                        case "2":
                            lightT = "光感正常";
                            break;
                        case "3":
                            lightT = "光感昏暗";
                            break;
                        case "4":
                            lightT = "光感黑";
                            break;
                    }

                    // 汇总数据
                    strlist.add(dateT + " | " + humanT + " | " + lightT);
                    
                } while (cursor.moveToNext());
            }
            cursor.close();

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    Activity_HLlist.this,
                    R.layout.list_item1,
                    strlist);
            lV4_HLlist.setAdapter(adapter);
        }
    }

    // 清空数据按钮
    public void clearData(View view) {
        // 删除数据库HLdata表内容
        db.delete("HLdata", null, null);
        db.execSQL("update sqlite_sequence set seq=0 where name='HLdata'");

        lV4_HLlist.setAdapter(null);
        Toast.makeText(Activity_HLlist.this, "已清空历史数据", Toast.LENGTH_LONG).show();
    }

    // 标题栏返回键点击事件
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
