package com.example.abox;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.app.AppCompatActivity;

import java.util.Objects;

// APP账号注册页
public class Activity_signup extends AppCompatActivity {

    private EditText eT3_ID;
    private EditText eT3_PW;
    private EditText eT3_rePW;
    private InputMethodManager imm;
    private SQLiteDatabase db;
    private MySQLiteOpenHelper mySQLiteOH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 隐藏标题栏
        Objects.requireNonNull(getSupportActionBar()).hide();

        eT3_ID = findViewById(R.id.eT3_ID);
        eT3_PW = findViewById(R.id.eT3_PW);
        eT3_rePW = findViewById(R.id.eT3_rePW);

        // 获取输入法管理器对象（为实现点击输入框外时收起键盘）
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // 初始化数据库
        mySQLiteOH = new MySQLiteOpenHelper(Activity_signup.this, "appdata.db", null, 1);
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
        eT3_ID.clearFocus();
        eT3_PW.clearFocus();
        eT3_rePW.clearFocus();

        imm.hideSoftInputFromWindow(eT3_ID.getWindowToken(),0);
        imm.hideSoftInputFromWindow(eT3_PW.getWindowToken(),0);
        imm.hideSoftInputFromWindow(eT3_rePW.getWindowToken(),0);
        return super.onTouchEvent(event);
    }

    // 按"注册"按钮
    public void signup_new(View view) {
        // 清除输入框焦点
        eT3_ID.clearFocus();
        eT3_PW.clearFocus();
        eT3_rePW.clearFocus();

        TextView tV3_err = findViewById(R.id.tV3_err);
        tV3_err.setText("");

        // 若任一输入框为空
        if(TextUtils.isEmpty(eT3_ID.getText().toString())
                || TextUtils.isEmpty(eT3_PW.getText().toString())
                || TextUtils.isEmpty(eT3_rePW.getText().toString())){
            tV3_err.setText("输入不能为空！");

        // 若两次输入的密码不一致
        } else if(!TextUtils.equals(eT3_PW.getText().toString(), eT3_rePW.getText().toString())){
            tV3_err.setText("两次输入的密码不一致！");

        // 若填写内容符合要求
        } else {
            // 查询数据库现有账号信息
            Cursor cursor = db.query(
                    "users",
                    new String[]{"username"},
                    "username = ?",
                    new String[]{eT3_ID.getText().toString()},
                    null, null, null);

            // 若账号信息不存在
            if(cursor.getCount() == 0) {
                // 收起键盘
                imm.hideSoftInputFromWindow(eT3_ID.getWindowToken(),0);
                imm.hideSoftInputFromWindow(eT3_PW.getWindowToken(),0);
                imm.hideSoftInputFromWindow(eT3_rePW.getWindowToken(),0);

                // 将信息存入数据库
                ContentValues values = new ContentValues();
                values.put("username", eT3_ID.getText().toString());
                values.put("password", eT3_PW.getText().toString());
                db.insert("users", null, values);
                Toast.makeText(Activity_signup.this, "注册成功", Toast.LENGTH_LONG).show();
                finish();

            // 若账号信息已存在
            } else {
                tV3_err.setText("该账号信息已存在！");
            }
            cursor.close();
        }
    }

    // 按"返回"按钮
    public void signup_back(View view) {
        finish();
    }

    // Activity销毁时关闭数据库
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}