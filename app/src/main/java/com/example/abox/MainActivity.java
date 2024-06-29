package com.example.abox;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // getSupportActionBar().hide();
    }

    public void buttonClick(View view) {
        new LoginAsyncTask().execute("a", "a");
    }

    public void button2Click(View view) {
        new TempAsyncTask().execute();
    }

    public void imageClick(View view) {
        ImageView imageView = (ImageView)view;
        imageView.setImageResource(R.drawable.photo);
    }

    public void show(View view) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, "" + item.getItemId(), Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }
}
