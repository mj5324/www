package com.app.mj.weather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.app.mj.weather.receiver.AutoUpdateReceiver;
import com.app.mj.weather.util.HttpCallbackListener;
import com.app.mj.weather.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mj on 2015/11/14.
 */
public class AutoUpdateService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //开启子线程
        new Thread(new Runnable() {
            @Override
            public void run() {
               updateWeather();
            }
        });
        //在后台执行定定时任务
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);  //获得定时器的实例

//		int anHour = 8 * 60 * 60 * 1000; // 这是8小时的毫秒数
        int anHour = 3 * 1000; // 这是8小时的毫秒数    //设置间隔时间
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;//定义触发时间

        Intent i = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);  //能够执行广播的Intent

        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);//从系统开计算器，唤醒CPU;

        return super.onStartCommand(intent, flags, startId);
    }




    private void updateWeather() {
        SharedPreferences asp= PreferenceManager.getDefaultSharedPreferences(this);
        String parName=asp.getString("city_name","");
        String address="http://v.juhe.cn/weather/index?format=1&cityname="
                +parName+"&key=10d36b60f225e52ccc4ae12dbcd81481";
        HttpUtil.sendHttpRequest(address,new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    JSONObject result = jsonObject.getJSONObject("result");
                    JSONObject today = result.getJSONObject("today");
                    String city = today.getString("city");  //地点"杭州"e
                    queryAndSaveWeather(AutoUpdateService.this,city);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    //   天气信息： 查询+保存+UI展示
    public void queryAndSaveWeather(final Context context,String cityName) {
        String address;
        address = "http://v.juhe.cn/weather/index?format=1&cityname="
                +cityName+"&key=9a8c8a15b0733820618df8a7549ec7c5";

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("queryAndSaveWeather,看看有没有获取到", String.valueOf(jsonObject));

                    JSONObject result = jsonObject.getJSONObject("result");
                    JSONObject sk = result.getJSONObject("sk");
                    String time = sk.getString("time");//发布时间"14:12"
                    JSONObject today = result.getJSONObject("today");
                    String temperature = today.getString("temperature");//温度"13℃~18℃"
                    String weather = today.getString("weather");    //天气"小雨转阴"
                    String date_y = today.getString("date_y");//发布日期"2015年11月13日"
                    String city = today.getString("city");  //地点"杭州"

                    saveWeatherInfo(context, city, date_y, time, temperature, weather); //保存到saveWeatherInfo
                    Log.e("queryAndSaveWeather保存数据", "保存完成");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(Exception e) {
            }
        });
    }

    //保存天气数据
    public void saveWeatherInfo(Context context, String city,
                                String date_y, String time,
                                String temperature, String weather){
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putString("city_name", city);
        editor.putString("weather", weather);
        editor.putString("temperature", temperature);
        editor.putString("time", time);
        editor.putString("date_y", date_y);
        editor.commit();
        Log.e("调用saveWeatherInfo", "保存成功");
    }
}
