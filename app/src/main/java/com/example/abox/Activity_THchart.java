package com.example.abox;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Activity_THchart extends AppCompatActivity {
    private LineChart lc_temp, lc_hum;
    private MySQLiteOpenHelper mySQLiteOH;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thchart);

        // 创建标题栏返回键
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 折线图控件初始化
        lc_temp = (LineChart) findViewById(R.id.lc_temp);
        lc_hum = (LineChart) findViewById((R.id.lc_hum));
        LineChart_Init(lc_temp);
        LineChart_Init(lc_hum);

        // 初始化数据库
        mySQLiteOH = new MySQLiteOpenHelper(Activity_THchart.this, "appdata.db", null, 1);

        db = mySQLiteOH.getWritableDatabase();
        Log.d("Activity_THchart", "获取数据库权限");

        // 遍历数据库
        Cursor cursor = db.rawQuery("SELECT * FROM data", null);
        // 若数据库不为空
        if(cursor.getCount() > 1){
            List<Entry> tempList = new ArrayList<>();
            List<Entry> humList = new ArrayList<>();
            final Map<Integer, String> dateMap = new HashMap<>();
            if(cursor.moveToFirst()){
                int i = 0;
                do {
                    // 读取时间列
                    int id = cursor.getInt(cursor.getColumnIndex("id"));
                    String datet = cursor.getString(cursor.getColumnIndex("time"));
                    Log.d("Activity_THchart", datet);
                    dateMap.put(id, datet);
                    // 读取温度列
                    String tempt = cursor.getString(cursor.getColumnIndex("temperature"));
                    tempList.add(new Entry(i, Float.parseFloat(tempt)));
                    // 读取湿度列
                    String humt = cursor.getString(cursor.getColumnIndex("humidity"));
                    humList.add(new Entry(i, Float.parseFloat(humt)));
                    i++;
                } while (cursor.moveToNext());
            }
            cursor.close();

            // 温度曲线
            LineData tempLine = setLine(tempList, "温度");
            lc_temp.setData(tempLine);
            lc_temp.requestLayout();
            // 设置温度图X轴为时间
            lc_temp.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return dateMap.get((int)value + 1);
                }
            });

            // 湿度曲线
            LineData humLine = setLine(humList, "湿度");
            lc_hum.setData(humLine);
            lc_hum.requestLayout();
            // 设置湿度图X轴为时间
            lc_hum.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return dateMap.get((int)value + 1);
                }
            });
        }
    }

    public void clearData(View view) {
        db.delete("data", null, null);
        db.execSQL("update sqlite_sequence set seq=0 where name='data'");

        // 清空图表
        lc_hum.clear(); lc_temp.clear();
        lc_hum.invalidate(); lc_temp.invalidate();

        Toast.makeText(Activity_THchart.this, "已清空历史数据", Toast.LENGTH_LONG).show();
    }

    // 设置图表折线
    // datalist: 要显示的数据列表
    // linename: 折线名
    private static @NonNull LineData setLine(List<Entry> datalist, String linename) {
        LineDataSet line = new LineDataSet(datalist, linename);
        line.setColor(Color.YELLOW); // 设置曲线颜色
        line.setCircleColor(Color.YELLOW);  // 设置数据点圆形的颜色
        line.setDrawCircleHole(false);// 设置曲线值的圆点是否是空心
        line.setLineWidth(2f); // 设置折线宽度
        line.setCircleRadius(5f); // 设置折现点圆点半径
        line.setValueTextSize(15f); // 设置数据点文字字号
        line.setValueTextColor(Color.WHITE); // 设置数据点文字颜色

        return new LineData(line);
    }

    // 初始化折线图样式
    // chart: 折线图控件
    public void LineChart_Init(LineChart chart){
        //显示边框，白色
        chart.setDrawBorders(true);
        chart.setBorderColor(Color.WHITE);

        // 无数据时显示的文本，白色
        chart.setNoDataText("暂无数据");
        chart.setNoDataTextColor(Color.WHITE);

        // X轴显示在下方，字号16，白色，底部偏移30
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setTextSize(16f);
        chart.getXAxis().setTextColor(Color.WHITE);
        chart.getXAxis().setGranularity(1f);
        chart.setExtraBottomOffset(30f);

        // 只显示左侧Y轴，字号16，白色
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setTextSize(16f);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisLeft().setAxisMinimum(0f);

        // 禁用图例
        chart.getLegend().setEnabled(false);

        // 隐藏图表描述文字
        Description description = new Description();
        description.setEnabled(false);
        chart.setDescription(description);
    }

    // 标题栏返回键点击事件
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // Activity销毁时关闭数据库
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }


}
