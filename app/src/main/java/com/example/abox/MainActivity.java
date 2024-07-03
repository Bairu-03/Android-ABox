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
    private Button btn_sock, btn_light, btn_human, btn_tem_hum;
    private Drawable btn_sock_draw;
    private Boolean sock_state;
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

        // 光感强度按钮
        btn_light = findViewById(R.id.btn_light);

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
                        // 获取人体传感器数据
                        new hdAsyncTask().execute();
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
        Intent intent = new Intent(MainActivity.this, Activity_HLlist.class);
        startActivity(intent);
    }

    // 光照强度按钮点击动作
    public void btn_light(View view) {
        Intent intent = new Intent(MainActivity.this, Activity_HLlist.class);
        startActivity(intent);
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
            Log.d("MainActivity", "获取温湿度数据指令错误码：" + abRet.getCode());

            if(TextUtils.equals(abRet.getCode(), "00000")) {
                Log.d("MainActivity", "温湿度数据：" + abRet.getDicDatas());
                Map<String, Object> map = abRet.getDicDatas();
                Object temperature = map.get("temperature");
                Object humidity = map.get("humidity");

                btn_tem_hum.setText(getString(R.string.btn_tem_hum, temperature.toString(), humidity.toString()));

                /* 将获取的数据写入数据库 */
                ContentValues values = new ContentValues();
                values.put("time", getdate());  // 获取当前时间
                values.put("temperature", temperature.toString());
                values.put("humidity", humidity.toString());
                db.insert("THdata", null, values);

            } else {
                Toast.makeText(MainActivity.this, "温湿度数据获取失败", Toast.LENGTH_LONG).show();
                btn_tem_hum.setText(getString(R.string.btn_tem_hum, "0", "0"));
            }
        }
    }

    // 获取系统时间
    private static String getdate() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());
        String ndate = formatter.format(curDate);
        return ndate;
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

    // 获取人体传感器数据
    public class hdAsyncTask extends AsyncTask<String, Void, ABRet> {
        @Override
        protected ABRet doInBackground(String... strings) {
            return ABSDK.getInstance().getHdStatus("人体活动传感器");
        }

        @Override
        protected void onPostExecute(ABRet abRet) {
            super.onPostExecute(abRet);
            Log.d("MainActivity", "获取人体传感器数据指令错误码：" + abRet.getCode());

            Drawable btn_human_draw;
            Drawable btn_light_draw;

            // 数据获取成功
            if(TextUtils.equals(abRet.getCode(), "00000")) {
                Map<String, Object> map = abRet.getDicDatas();
                Object status = map.get("status");
                Object lightIntensity = map.get("lightIntensity");
                Log.d("MainActivity", "人体传感器数据：" + abRet.getDicDatas());

                /* 将获取的数据写入数据库 */
                ContentValues values = new ContentValues();
                values.put("time", getdate());  // 获取当前时间
                values.put("human", status.toString());
                values.put("light", lightIntensity.toString());
                db.insert("HLdata", null, values);

                // 有人体活动
                if (TextUtils.equals(status.toString(), "1")) {
                    btn_human_draw = getResources().getDrawable(R.drawable.ic_btn_human_y);
                // 无人体活动
                } else {
                    btn_human_draw = getResources().getDrawable(R.drawable.ic_btn_human_n);
                }

                // 光感强度数据
                switch (lightIntensity.toString()){
                    case "1":
                        btn_light_draw = getResources().getDrawable(R.drawable.ic_btn_light_1);
                        break;
                    case "2":
                        btn_light_draw = getResources().getDrawable(R.drawable.ic_btn_light_2);
                        break;
                    case "3":
                        btn_light_draw = getResources().getDrawable(R.drawable.ic_btn_light_3);
                        break;
                    case "4":
                        btn_light_draw = getResources().getDrawable(R.drawable.ic_btn_light_4);
                        break;
                    default:
                        btn_light_draw = getResources().getDrawable(R.drawable.ic_btn_light_0);
                }

            } else {
                Toast.makeText(MainActivity.this, "人体活动传感器数据获取失败", Toast.LENGTH_LONG).show();
                btn_human_draw = getResources().getDrawable(R.drawable.ic_btn_human_n);
                btn_light_draw = getResources().getDrawable(R.drawable.ic_btn_light_0);
            }
            btn_human.setCompoundDrawablesWithIntrinsicBounds(null, btn_human_draw, null, null);
            btn_light.setCompoundDrawablesWithIntrinsicBounds(null, null, null, btn_light_draw);
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

    // Activity销毁时关闭数据库
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

}
