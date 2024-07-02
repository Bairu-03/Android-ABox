package com.example.abox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import android.support.v7.app.AppCompatActivity;

import com.dhc.absdk.ABRet;
import com.dhc.absdk.ABSDK;

import java.util.Arrays;
import java.util.Objects;

// APP登录页
public class Activity_login extends AppCompatActivity{
    private TextView tV1_err;
    private EditText eT1_ID;
    private EditText eT1_PW;
    private CheckBox cB1_pro;
    private InputMethodManager imm;
    private MySQLiteOpenHelper mySQLiteOH;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 隐藏标题栏
        Objects.requireNonNull(getSupportActionBar()).hide();

        tV1_err = findViewById(R.id.tV1_err);
        eT1_ID = findViewById(R.id.eT1_ID);
        eT1_PW = findViewById(R.id.eT1_PW);
        cB1_pro = findViewById(R.id.cB1_pro);

        // 获取输入法管理器对象（为实现点击输入框外时收起键盘）
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // 初始化数据库
        mySQLiteOH = new MySQLiteOpenHelper(Activity_login.this, "appdata.db", null, 1);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 获取数据库读写权限
        db = mySQLiteOH.getWritableDatabase();
    }

    // 点击页面空白处清除输入框焦点，收起键盘
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        eT1_ID.clearFocus();
        eT1_PW.clearFocus();
        imm.hideSoftInputFromWindow(eT1_ID.getWindowToken(),0);
        imm.hideSoftInputFromWindow(eT1_PW.getWindowToken(),0);
        return super.onTouchEvent(event);
    }

    // 点击"登录"按钮
    @SuppressLint("Range")
    public void login(View view) {
        // 清除输入框焦点
        eT1_ID.clearFocus();
        eT1_PW.clearFocus();
        tV1_err.setText("");

        // 在数据库中查询输入的账号信息
        Cursor cursor = db.query(
                "users",
                new String[]{"username", "password"},
                "username = ?",
                new String[]{eT1_ID.getText().toString()},
                null, null, null);

        if(cursor.getCount() == 0){
            tV1_err.setText("账号不存在！");

        // 未勾选用户协议
        } else if(!cB1_pro.isChecked()){
            // 收起键盘
            imm.hideSoftInputFromWindow(eT1_ID.getWindowToken(),0);
            imm.hideSoftInputFromWindow(eT1_PW.getWindowToken(),0);

            tV1_err.setText("请查看并同意用户协议");

        // 在数据库中查询对应账号的密码并校验
        } else {
            cursor.moveToFirst();
            String password = cursor.getString(cursor.getColumnIndex("password"));

            // 若密码正确
            if(TextUtils.equals(eT1_PW.getText().toString(), password)){
                // 收起键盘
                imm.hideSoftInputFromWindow(eT1_ID.getWindowToken(),0);
                imm.hideSoftInputFromWindow(eT1_PW.getWindowToken(),0);

                new LoginAsyncTask().execute("a", "a");

                tV1_err.setText("正在登录，请稍候...");

            } else {
                tV1_err.setText("密码错误！");
            }
        }
        cursor.close();
    }

    // 点击"注册"按钮
    public void signup(View view) {
        // 清除输入框焦点
        eT1_ID.clearFocus();
        eT1_PW.clearFocus();
        tV1_err.setText("");

        // 收起键盘
        imm.hideSoftInputFromWindow(eT1_ID.getWindowToken(),0);
        imm.hideSoftInputFromWindow(eT1_PW.getWindowToken(),0);

        // 跳转到注册页
        Intent intent = new Intent(Activity_login.this, Activity_signup.class);
        startActivity(intent);
    }

    // 点击"用户协议"
    private AlertDialog alertDialog;
    public void protocol(View view) {
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("用户协议")
                .setMessage("\n    用户协议正文...")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    public class LoginAsyncTask extends AsyncTask<String, Void, ABRet> {
        @Override
        protected ABRet doInBackground(String... strings) {
            Log.d("loginAbox","ABox账号密码:"+ Arrays.toString(strings));
            return ABSDK.getInstance().loginWithUsername("a", "a");
        }

        @Override
        protected void onPostExecute(ABRet abRet) {
            super.onPostExecute(abRet);

            Log.d("loginAbox", "登录过程状态码: " + abRet.getCode());

            if(TextUtils.equals(abRet.getCode(), "00000")){
                tV1_err.setText("");
                // 跳转到信息显示页面
                Intent intent = new Intent(Activity_login.this, MainActivity.class);
                startActivity(intent);
            } else {
                tV1_err.setText("登录失败(错误码：" + abRet.getCode() + ")");
            }
        }
    }

    // Activity销毁时关闭数据库
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
