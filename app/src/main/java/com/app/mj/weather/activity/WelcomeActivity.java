package com.app.mj.weather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.app.mj.weather.R;
import com.app.mj.weather.db.WeatherDB;
import com.app.mj.weather.model.City;
import com.app.mj.weather.util.HttpCallbackListener;
import com.app.mj.weather.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mj on 2015/11/16.
 */
public class WelcomeActivity extends Activity{
    private WeatherDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        db= WeatherDB.getInstance(this);
        queryCityFromServer();//从服务器读取数据

        final SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
        final boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        Timer timer = new Timer();
        TimerTask task = new TimerTask(){
            @Override
            public void run() {
                if (isFirstRun )
                {   //首次启动，更改标记位
                    editor.putBoolean("isFirstRun", false);
                    editor.commit();

                    //执行相应的操作方法
                    Intent intent=new Intent(WelcomeActivity.this, ChooseAreaActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    //若不是首次启动，直接显示天气详情页
                    Intent intent = new Intent(WelcomeActivity.this, WeatherActivity.class);
                    intent.putExtra("isFromWelcome",true);
                    startActivity(intent);
                    finish();
                }
            }
        };
        timer.schedule(task, 2000);

    }



    private void queryCityFromServer() {
        String address;
        address = "http://v.juhe.cn/weather/citys?key=c4f76dc961a62a4ea7ac8ba9f6193d3d";
        Log.e("queryCityFromServer（）", address);
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            //以下是在子线程中操作的
            @Override
            public void onFinish(String response) {
                Log.e("queryCityFromServer（）", "onFinish（）方法中");
                analysisAndSave(response);//解析数据，并保存到数据库
                Log.e("analysisAndSave()成功", "");
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }

    //解析数据，并保存到数据库
    public void analysisAndSave(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String result = jsonObject.getString("result");
            JSONArray arr= new JSONArray(result);
            for(int i=0;i<arr.length();i++){
                JSONObject oj=arr.getJSONObject(i);
                City city =new City();
                city.setProvinceName(oj.getString("province"));
                city.setCityName(oj.getString("city"));
                city.setDistrictName(oj.getString("district"));

                db.save(city);//存到数据库
            }
            Log.e("save()保存成功", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
