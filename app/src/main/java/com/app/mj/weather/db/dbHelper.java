package com.app.mj.weather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

//数据库Helper类，用于创建数据库和其中的表
public class dbHelper extends SQLiteOpenHelper {

    //定义省、市、县三种字符串常量
	public static final String CREATE_TABLE = "create table city ("
				+ "id integer primary key autoincrement, "
				+ "province_name text,"
                + "city_name text,"
                + "county_name text)";

    //构造方法
	public dbHelper(Context context, String name, CursorFactory factory,
        int version) {
        super(context, name, factory, version);
        }

@Override
public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);  // 创建City表
        }

@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
        }