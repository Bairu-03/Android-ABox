package com.example.abox;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dhc.absdk.ABRet;
import com.dhc.absdk.ABSDK;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

// APP信息显示页（主）
public class MainActivity extends AppCompatActivity {
    Boolean socket_state;
    private Button btn_socket;
    private Drawable btn_socket_draw;
    private Button btn_tem_hum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        socket_state = true;
        btn_socket = findViewById(R.id.btn_socket);
        btn_socket_draw = getResources().getDrawable(R.drawable.ic_btn_socket_on);
        btn_socket.setCompoundDrawablesWithIntrinsicBounds(null, btn_socket_draw, null, null);

        btn_tem_hum = findViewById(R.id.btn_tem_hum);
        btn_tem_hum.setText(getString(R.string.btn_tem_hum, 0, 0));
    }

//    public void buttonClick(View view) {
//        new LoginAsyncTask().execute("a", "a");
//    }

//    public void button2Click(View view) {
//        new TempAsyncTask().execute();
//    }

    // 温湿度按钮点击动作
    public void btn_tem_hum(View view) {
        btn_tem_hum = findViewById(R.id.btn_tem_hum);
        btn_tem_hum.setText(getString(R.string.btn_tem_hum, 25, 50));
    }

    // 插座按钮点击动作
    public void btn_socket(View view) {
        // 若插座已开启，改为关闭
        if(socket_state){
            btn_socket_draw = getResources().getDrawable(R.drawable.ic_btn_socket_off);
            btn_socket.setCompoundDrawablesWithIntrinsicBounds(null, btn_socket_draw, null, null);
            socket_state = false;
        // 若插座已关闭，改为开启
        } else {
            btn_socket_draw = getResources().getDrawable(R.drawable.ic_btn_socket_on);
            btn_socket.setCompoundDrawablesWithIntrinsicBounds(null, btn_socket_draw, null, null);
            socket_state = true;
        }
    }

    // 人体活动按钮点击动作
    public void btn_human(View view) {
    }

    // 光照强度按钮点击动作
    public void btn_light(View view) {
    }


    public class LoginAsyncTask extends AsyncTask<String, Void, ABRet> {
        @Override
        protected ABRet doInBackground(String... strings) {
            System.out.println(Arrays.toString(strings));
            return ABSDK.getInstance().loginWithUsername("a", "a");
        }

        @Override
        protected void onPostExecute(ABRet abRet) {
            super.onPostExecute(abRet);
            System.out.println(abRet.getCode());
        }
    }

    public class TempAsyncTask extends AsyncTask<String, Void, ABRet> {
        @Override
        protected ABRet doInBackground(String... strings) {
            return ABSDK.getInstance().getThStatus("温湿度传感器");
        }

        @Override
        protected void onPostExecute(ABRet abRet) {
            super.onPostExecute(abRet);
            System.out.println(abRet.getCode());
            System.out.println(abRet.getDicDatas());
        }
    }

    public class UrlAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            try {
                url = new URL("http://www.baidu.com");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream in = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                in.close();
                conn.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private AlertDialog alertDialog;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 标题栏菜单-关于按钮
        if(item.getItemId() == R.id.m_about){
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("关于APP")
                    .setMessage("\n    大连海洋大学 - 电子21-1班 - 吕柏儒")
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
