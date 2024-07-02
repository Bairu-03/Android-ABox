package com.example.abox;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dhc.absdk.ABRet;
import com.dhc.absdk.ABSDK;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

// APP信息显示页（主）
public class MainActivity extends AppCompatActivity {
    private Button btn_sock, btn_human, btn_tem_hum;
    private Drawable btn_sock_draw, btn_human_draw;
    private Boolean human_state, sock_state;
    private MySQLiteOpenHelper mySQLiteOH;
    private SQLiteDatabase db;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 插座控制按钮
        btn_sock = findViewById(R.id.btn_sock);

        // 温湿度显示按钮
        btn_tem_hum = findViewById(R.id.btn_tem_hum);

        // 人体传感器按钮
        btn_human = findViewById(R.id.btn_human);
        human_state = true;
        btn_human_draw = getResources().getDrawable(R.drawable.ic_btn_human_y);
        btn_human.setCompoundDrawablesWithIntrinsicBounds(null, btn_human_draw,null, null);

        // 初始化数据库
        mySQLiteOH = new MySQLiteOpenHelper(MainActivity.this, "appdata.db", null, 1);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 获取数据库读写权限
        db = mySQLiteOH.getWritableDatabase();

        /* 设置定时任务，用于刷新数据 */
        final Handler handler = new Handler();
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 获取插座状态
                        new SockAsyncTask().execute();
                        // 获取温湿度值
                        new TempAsyncTask().execute();
                        Log.d("MainActivity", "定时任务-刷新数据");
                    }
                });
            }
        };
        timer.schedule(task, 2, 15000);
    }

    // Activity停止时关闭定时器
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "关闭定时器");
        timer.cancel();
    }

    // Activity销毁时关闭数据库
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    // 温湿度按钮点击动作
    public void btn_tem_hum(View view) {
        Intent intent = new Intent(MainActivity.this, Activity_THchart.class);
        startActivity(intent);
    }

    // 插座按钮点击动作
    public void btn_socket(View view) {
        if(sock_state != null) {
            if (sock_state) {
                new SockOffAsyncTask().execute();
            } else {
                new SockOnAsyncTask().execute();
            }
        } else {
            Toast.makeText(MainActivity.this, "插座连接失败", Toast.LENGTH_LONG).show();
        }
    }

    // 人体活动按钮点击动作
    public void btn_human(View view) {
        // 当前有人活动
        if(human_state) {
            btn_human_draw = getResources().getDrawable(R.drawable.ic_btn_human_n);
            btn_human.setCompoundDrawablesWithIntrinsicBounds(null, btn_human_draw, null, null);
            human_state = false;
        // 当前无人活动
        } else {
            btn_human_draw = getResources().getDrawable(R.drawable.ic_btn_human_y);
            btn_human.setCompoundDrawablesWithIntrinsicBounds(null, btn_human_draw, null, null);
            human_state = true;
        }
    }

    // 光照强度按钮点击动作
    public void btn_light(View view) {
    }

    // 获取温湿度数据
    public class TempAsyncTask extends AsyncTask<String, Void, ABRet> {
        @Override
        protected ABRet doInBackground(String... strings) {
            return ABSDK.getInstance().getThStatus("温湿度传感器");
        }

        @Override
        protected void onPostExecute(ABRet abRet) {
            super.onPostExecute(abRet);
            Log.d("MainActivity", "获取温湿度指令错误码：" + abRet.getCode());

            if(TextUtils.equals(abRet.getCode(), "00000")) {
                Log.d("MainActivity", "温湿度数据：" + abRet.getDicDatas());
                Map<String, Object> map = abRet.getDicDatas();
                Object temperature = map.get("temperature");
                Object humidity = map.get("humidity");

                if (temperature != null && humidity != null) {
                    btn_tem_hum.setText(getString(R.string.btn_tem_hum, temperature.toString(), humidity.toString()));

                    /* 获取系统时间 */
                    SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
                    Date curDate = new Date(System.currentTimeMillis());
                    String ndate = formatter.format(curDate);
                    Log.d("MainActivity", "获取系统时间:" + ndate);

                    /* 将获取的数据写入数据库 */
                    ContentValues values = new ContentValues();
                    values.put("time", ndate);
                    values.put("temperature", temperature.toString());
                    values.put("humidity", humidity.toString());
                    db.insert("data", null, values);
                } else {
                    Toast.makeText(MainActivity.this, "温湿度数据格式异常", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "温湿度数据获取失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 获取插座状态
    public class SockAsyncTask extends AsyncTask<String, Void, ABRet> {
        @Override
        protected ABRet doInBackground(String... strings) {
            return ABSDK.getInstance().getSockStatus("ZigBee插座控制器");
        }

        @Override
        protected void onPostExecute(ABRet abRet) {
            super.onPostExecute(abRet);
            Log.d("MainActivity", "获取插座状态指令错误码：" + abRet.getCode());

            if(TextUtils.equals(abRet.getCode(), "00000")) {
                Log.d("MainActivity", "插座状态：" + abRet.getDicDatas());
                Map<String, Object> map = abRet.getDicDatas();
                Object status = map.get("status");

                if (status != null) {
                    // 若插座状态为开启
                    if(TextUtils.equals(status.toString(), "1")){
                        sock_state = true;
                        btn_sock_draw = getResources().getDrawable(R.drawable.ic_btn_sock_on);
                        btn_sock.setCompoundDrawablesWithIntrinsicBounds(null, btn_sock_draw, null, null);
                    // 若插座状态为关闭
                    } else {
                        sock_state = false;
                        btn_sock_draw = getResources().getDrawable(R.drawable.ic_btn_sock_off);
                        btn_sock.setCompoundDrawablesWithIntrinsicBounds(null, btn_sock_draw, null, null);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "插座状态数据格式异常", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "插座状态获取失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 控制插座关闭
    public class SockOffAsyncTask extends AsyncTask<String, Void, ABRet> {
        @Override
        protected ABRet doInBackground(String... strings) {
            return ABSDK.getInstance().sockCtrl("ZigBee插座控制器", "0");
        }

        @Override
        protected void onPostExecute(ABRet abRet) {
            super.onPostExecute(abRet);
            Log.d("MainActivity", "控制插座关闭指令错误码：" + abRet.getCode());

            if(TextUtils.equals(abRet.getCode(), "00000")) {
                btn_sock_draw = getResources().getDrawable(R.drawable.ic_btn_sock_off);
                btn_sock.setCompoundDrawablesWithIntrinsicBounds(null, btn_sock_draw, null, null);
                sock_state = false;
            } else {
                Toast.makeText(MainActivity.this, "关闭插座失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 控制插座开启
    public class SockOnAsyncTask extends AsyncTask<String, Void, ABRet> {
        @Override
        protected ABRet doInBackground(String... strings) {
            return ABSDK.getInstance().sockCtrl("ZigBee插座控制器", "1");
        }

        @Override
        protected void onPostExecute(ABRet abRet) {
            super.onPostExecute(abRet);
            Log.d("MainActivity", "控制插座开启指令错误码：" + abRet.getCode());

            if(TextUtils.equals(abRet.getCode(), "00000")) {
                btn_sock_draw = getResources().getDrawable(R.drawable.ic_btn_sock_on);
                btn_sock.setCompoundDrawablesWithIntrinsicBounds(null, btn_sock_draw, null, null);
                sock_state = true;
            } else {
                Toast.makeText(MainActivity.this, "开启插座失败", Toast.LENGTH_LONG).show();
            }
        }
    }



    // 创建标题栏菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // 标题栏菜单项目点击事件
    private AlertDialog alertDialog;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 标题栏菜单-关于按钮
        if(item.getItemId() == R.id.m_about){
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("关于APP")
                    .setMessage(R.string.about_info)
                    .setIcon(R.mipmap.ic_launcher)
                    .setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                        }
                    })
                    .create();
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }
}
