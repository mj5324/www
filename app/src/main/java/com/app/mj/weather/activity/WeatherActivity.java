package com.app.mj.weather.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.app.mj.weather.R;
import com.app.mj.weather.service.AutoUpdateService;
import com.app.mj.weather.util.HttpCallbackListener;
import com.app.mj.weather.util.HttpUtil;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;

public class WeatherActivity extends Activity implements OnClickListener{

	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherText;
	private TextView tempText;
	private TextView currentDateText;

	private Button switchCity;
	private Button refreshWeather;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);

        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherText = (TextView) findViewById(R.id.weather);
        tempText = (TextView) findViewById(R.id.temp);
        currentDateText = (TextView) findViewById(R.id.current_date);

//        Log.e("从SP读取数据,初始化显示","showWeather（）");
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                showWeather(); //从SP中读取数据，并显示
//            }
//        });
//
//

        boolean dd1=getIntent().getBooleanExtra("isFromWelcome",false);
        boolean dd2=getIntent().getBooleanExtra("isFromChooseActivity",false);
        if(dd1){
            //从SP中，读取cityName
            SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(this);
            String cityName = prefs1.getString("city_name", "");

            queryAndSaveAndShow(this,cityName);
            Log.e("isFromWelcome",cityName);

        }
        else if(dd2) {
            //从intent中，读取cityName
            String country_name=getIntent().getStringExtra("parm");//取到传递过来的intent里面的数据
            queryAndSaveAndShow(this, country_name); //得到数据，并保存在SharedPreferences
            Log.e("isFromChooseActivity",country_name);
        }





        //点击事件
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);

        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
	}



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("isFromWeatherActivity",true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(this);
                String cityName = prefs1.getString("city_name", "");
                Log.e("点击refresh刷新的时候，得到城市名的参数",cityName);

                Toast.makeText(WeatherActivity.this,
                        "刷新天气", Toast.LENGTH_SHORT).show();

                queryAndSaveAndShow(this,cityName);

                break;
            default:
                break;
        }
    }


//   天气信息： 查询+保存+UI展示
    public void queryAndSaveAndShow(final Context context,String cityName) {
        String address;
        address = "http://v.juhe.cn/weather/index?format=1&cityname="
                +cityName+"&key=c4f76dc961a62a4ea7ac8ba9f6193d3d";

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

                    //回到主线程，进行weather展示
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                            Log.e("回到主线程，完成数据展示", "展示天气");
                        }
                    });
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
        Log.d("调用saveWeatherInfo", "保存成功");
    }


    //UI更新，数据展示
    private void showWeather() {


        long time=System.currentTimeMillis();
        final Calendar mCalendar=Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
        String mHour= String.valueOf(mCalendar.get(Calendar.HOUR));
        String mMinuts= String.valueOf(mCalendar.get(Calendar.MINUTE));
        String  mSec= String.valueOf(mCalendar.get(Calendar.SECOND));
        String curTime= mHour+":"+mMinuts+":"+mSec;


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText( prefs.getString("city_name", ""));
        tempText.setText(prefs.getString("temperature", ""));
        weatherText.setText(prefs.getString("weather", ""));
        currentDateText.setText(prefs.getString("date_y","")+"  "+prefs.getString("time",""));
        publishText.setText(curTime);


        Intent intent =new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

}