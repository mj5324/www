package com.app.mj.weather.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.app.mj.weather.R;
import com.app.mj.weather.db.WeatherDB;

import java.util.ArrayList;
import java.util.List;


public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
    private int currentLevel;
    private WeatherDB db;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();
	private String  selectedItem;
    private List<String> ProList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);

        db=WeatherDB.getInstance(this);

        initListView();  //初始化ListView和点击事件

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                queryProvinces();
            }
        });


//        //先判断。如果是第一次启动，显示城市选择页面；否则直接显示天气
//        SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
//        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        boolean isFromWeatherActivity=getIntent().getBooleanExtra("isFromWeatherActivity",false);
//
//        if (isFirstRun  )
//        {   //首次启动，更改标记位
//            editor.putBoolean("isFirstRun", false);
//            editor.commit();
//
//            //执行相应的操作方法
//
//
//            //判断是否需要从服务器获取城市数据，即判断数据库中是否存在city
//                boolean tt=db.dataIsExist("city");
//                if(tt){
//                    Log.e("已经存在", "直接读取");
//                    queryProvinces();  // 加载省级数据
//                }else{
//                    Log.e("没有数据", "从服务器获取");
//                    queryCityFromServer();  //先保存
//                    Log.e("保存完成", "queryCityFromServer");
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            queryProvinces();      //再查询
//                            Log.e("读取完成", "queryProvinces（）");
//                        }
//                    });
//
//                }
//        }
//        else
//        {
//            if(isFromWeatherActivity){
//                queryProvinces();
//            }else{
//                //如果不是第一次打开APP(即已经设定好城市)，而且也不是从weatherActivity跳转过来的。直接显示天气
//                Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        }
	}


    //初始化ListView方法，添加点击事件
    private void initListView() {
        listView = (ListView) findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        Log.e("initListView（）", "setAdapter（）函数");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedItem=dataList.get(index);
                    queryCities(selectedItem);
                }
                else if(currentLevel==LEVEL_CITY){
                    selectedItem=dataList.get(index);
                    queryCounties(selectedItem);
                }else if(currentLevel==LEVEL_COUNTY){
                    String nn=dataList.get(index);
                    Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("parm", nn);
                    intent.putExtra("isFromChooseActivity",true);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

//	  查询全国所有的省。
	private void queryProvinces() {
        ProList = db.loadProvinces(); //数据初始化，即得到省级别的数据
        Log.e("queryProvinces（）", "个数是"+String.valueOf(ProList.size()));
        dataList.clear();
            for(String str: ProList){
                dataList.add(str);
            }
            adapter.notifyDataSetChanged();  //当listview中的数据发生变化时，刷新listview。
            listView.setSelection(0);        //让ListView定位到指定Item的位置
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;

	}

//	 查询选中省内所有的市
	private void queryCities(String pro) {
        List<String> cityList = db.loadCity(pro); //数据初始化，即得到数据
        dataList.clear();
        for(String str: cityList){
            dataList.add(str);
        }
        adapter.notifyDataSetChanged();
        listView.setSelection(0);
        titleText.setText(pro);
        currentLevel=LEVEL_CITY;
	}

//	 查询选中市内所有的县
	private void queryCounties(String ci) {
        List<String> CouList = db.loadCountry(ci);
			dataList.clear();
			for (String cou : CouList) {
				dataList.add(cou);
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(ci);
			currentLevel = LEVEL_COUNTY;
	}

////	 从服务器上查询省、市、县数据，并存到数据库中
//	private void queryCityFromServer() {
//		String address;
//			address = "http://v.juhe.cn/weather/citys?key=c4f76dc961a62a4ea7ac8ba9f6193d3d";
//        Log.e("queryCityFromServer（）",address);
//		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
//            //以下是在子线程中操作的
//            @Override
//            public void onFinish(String response) {
//                Log.e("queryCityFromServer（）", "onFinish（）方法中");
//                analysisAndSave(response);//解析数据，并保存到数据库
//                Log.e("analysisAndSave()成功", "");
//            }
//
//            @Override
//            public void onError(Exception e) {
//            }
//        });
//	}
//
//    //解析数据，并保存到数据库
//    public void analysisAndSave(String response) {
//        JSONObject jsonObject = null;
//        try {
//            jsonObject = new JSONObject(response);
//            String result = jsonObject.getString("result");
//            JSONArray arr= new JSONArray(result);
//            for(int i=0;i<arr.length();i++){
//                JSONObject oj=arr.getJSONObject(i);
//                City city =new City();
//                city.setProvinceName(oj.getString("province"));
//                city.setCityName(oj.getString("city"));
//                city.setDistrictName(oj.getString("district"));
//
//                db.save(city);//存到数据库
//            }
//            Log.e("save()保存成功", "");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

//	 捕获Back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出。
	@Override
	public void onBackPressed() {
            if (currentLevel == LEVEL_COUNTY) {
                String city= (String) titleText.getText();
                String pro = db.getPro(city);
                queryCities(pro);
            } else if (currentLevel == LEVEL_CITY) {
                queryProvinces();
            }else{
                finish();
            }
		}

	}

