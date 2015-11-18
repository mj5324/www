package com.app.mj.weather.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.app.mj.weather.model.City;

import java.util.ArrayList;
import java.util.List;

//将数据库的常用操作封装卡起来，使用单例模式获得实体。
public class WeatherDB {

	public static final int VERSION = 1;

	private SQLiteDatabase db;

    //使用单例模式
    private static WeatherDB coolWeatherDB;

//	 1、将构造方法私有化
	private WeatherDB(Context context) {
		dbHelper dbH = new com.app.mj.weather.db.dbHelper(context,
                "weather.db", null, VERSION);
		db = dbH.getWritableDatabase();
	}
//	 2、获取CoolWeatherDB的实例。
	public synchronized static WeatherDB getInstance(Context context) {
		if (coolWeatherDB == null) {
			coolWeatherDB = new WeatherDB(context);
		}
		return coolWeatherDB;
	}


    //存 “省、市、县” 数据 到 数据库，使用city实体类
	public void save(City city) {
		if (city != null) {
            db.execSQL("insert into city(province_name,city_name,county_name) values(?,?,?)",
                    new String[] {city.getProvinceName(),city.getCityName(),city.getDistrictName()});
		}else{
            Log.e("保存出错","错误");
        }

	}

//	 数据库 中 读 “省” 信息，返回List<String>
	public List<String> loadProvinces() {
        List<String> list = new ArrayList<String>();
        Cursor cursor = db.rawQuery("select distinct province_name from city", null);
        if (cursor.moveToFirst()) {
			do {
                int index=cursor.getColumnIndex("province_name"); //获得查询列的下标
                String name=cursor.getString(index);              //获得该下标处的值
				list.add(name); //数据添加到list中
			} while (cursor.moveToNext());
		}
		return list;
	}

    //	 数据库 中 读 “市” 信息，返回List<String>
    public List<String> loadCity(String Pprovince_name) {
        List<String> listCity = new ArrayList<String>();
        Cursor cursor = db.
                rawQuery("select distinct city_name from city where province_name = ?",
                        new String[]{Pprovince_name});
        if (cursor.moveToFirst()) {
                do {
                    int index=cursor.getColumnIndex("city_name");
                    String name=cursor.getString(index);
//                    Log.e("读取城市数据阶段",name);
                    listCity.add(name); //数据添加到list中
                } while (cursor.moveToNext());
        }
        return listCity;
    }

    //	 数据库 中 读 “县” 信息，返回List<String>
    public List<String> loadCountry(String Pcity_name) {
        List<String> listC = new ArrayList<String>();
        Cursor cursor = db.
                rawQuery("select distinct county_name from city where city_name = ?",
                        new String[]{Pcity_name});
        if (cursor.moveToFirst()) {
            do {
                int index=cursor.getColumnIndex("county_name");
                String name=cursor.getString(index);
                listC.add(name); //数据添加到list中
            } while (cursor.moveToNext());
        }
        return listC;
    }

//通过城市名，得到所在的省名称
    public String getPro(String city){
        Cursor cursor = db.
                rawQuery("select distinct province_name from city where city_name =?",
                        new String[]{city});

        cursor.moveToFirst(); //只有一条数据时，先一定游标到0
        int index=cursor.getColumnIndex("province_name");
        String pro=cursor.getString(index);
        return pro;
    }

//判断数据库中是否存在数据
    public boolean dataIsExist(String tabName){
        boolean result = false;
        Cursor cursor = null;
        try {
            String sql = "select count(id) from city ";
            cursor = db.rawQuery(sql, null);
            if(cursor.moveToNext()){
                int count = cursor.getInt(0);
                Log.e("dataIsExist", String.valueOf(count));
                if(count>1){
                    result = true;
                }
            }
        } catch (Exception e) {
        }
        return result;
    }
}