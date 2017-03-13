package com.sen5.ocup.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.activity.OcupApplication;

/**
 * 作为维护和管理数据库的基类
 * 
 * @author chenqianghua
 * version:6:伊利水杯第一个数据库版本
 * version:7：yilicup表增加三个字段的数据库版本(havecup,isonLine,dataopen)
 *
 */
public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "ocup.db";
	private static final int DATABASE_VERSION = 7;//测试时升到了10，正式版本的apk为7,上个版本是6
	private static final String TAG = "DBHelper";

	//更新字段需要执行的操作
	private String CREATE_YiLiFriends = "create table YiLiFriends(time INTEGER,contact_id TEXT,group_id TEXT,avator TEXT,nickname TEXT,phonenum TEXT,havecup INTEGER,isonline INTEGER,dataopen INTEGER,lastuptime INTEGER);";

	private String CREATE_TEMP_YiLiFriends = "alter table YiLiFriends rename to temp_YiLiFriends";

	private String INSERT_DATA = "insert into YiLiFriends(time,contact_id,group_id,avator,nickname,phonenum) select time,contact_id,group_id,avator,nickname,phonenum from temp_YiLiFriends";
//	private String INSERT_DATA = "insert into YiLiFriends "

	private String DROP_YiLiFriends = "drop table temp_YiLiFriends";

	public DBHelper(Context context) {
		// CursorFactory设置为null,使用默认值
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// 数据库第一次被创建时onCreate会被调用
	@Override
	public void onCreate(SQLiteDatabase db) {
		// db.execSQL("CREATE TABLE IF NOT EXISTS tip" +
		// "(_id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, age INTEGER, info TEXT)");
		db.execSQL("CREATE TABLE IF NOT EXISTS tip"
				+ "(_id integer primary key autoincrement,id TEXT, date TEXT, title TEXT, brief TEXT, imgName TEXT, imgUrl TEXT, bmp BLOB,isMarked integer)");
		db.execSQL("CREATE TABLE IF NOT EXISTS tip_mark"
				+ "(_id integer primary key autoincrement,id TEXT, date TEXT, title TEXT, brief TEXT, imgName TEXT, imgUrl TEXT, bmp BLOB,isMarked integer)");
		db.execSQL("CREATE TABLE IF NOT EXISTS tea" + "(_id integer primary key autoincrement,id TEXT, date TEXT,teaName TEXT, imgUrl TEXT,desc TEXT,bmp BLOB)");
		db.execSQL("CREATE TABLE IF NOT EXISTS cup" + "(nickname TEXT,huanxin_userid TEXT, email TEXT, avatorPath TEXT,mood TEXT, intakegoal integer, intake integer)");
		db.execSQL("CREATE TABLE IF NOT EXISTS Owncup" + "(cupID TEXT,blueAdd TEXT, name TEXT, huanxin_userid TEXT, huanxin_pwd TEXT, email TEXT, avatorPath TEXT, mood TEXT, intakegoal integer)");
		db.execSQL("CREATE TABLE IF NOT EXISTS cup_mate" + "(id TEXT, mate_id TEXT)");
		db.execSQL("CREATE TABLE IF NOT EXISTS chat"
				+ "(_id integer primary key autoincrement,cupid TEXT, to_cupid TEXT, date NUMERIC , content TEXT,status INTEGER,type INTEGER,fromflag INTEGER)");

		db.execSQL("CREATE TABLE IF NOT EXISTS drinkdata" + "(cupid TEXT, water_temp integer, water_yield integer,drink_date TEXT,drink_time integer,drink_srv integer)");
		db.execSQL("CREATE TABLE IF NOT EXISTS alarmdata" + "(alarm_time TEXT, flag integer)");
		db.execSQL("CREATE TABLE IF NOT EXISTS updatecup" + "(cupID TEXT,neverremind integer,targetversion integer)");
		db.execSQL("CREATE TABLE IF NOT EXISTS OwnYiLicup" + "(userID TEXT,nickname TEXT,password TEXT," +
				"mood TEXT,email TEXT,avator TEXT,group_id TEXT,zone TEXT,phone TEXT, avatorThumbnail TEXT)");
		db.execSQL("CREATE TABLE IF NOT EXISTS YiLiFriends" + "(time INTEGER,contact_id TEXT,group_id TEXT,avator TEXT,nickname TEXT,phonenum TEXT,havecup INTEGER,isonline INTEGER,dataopen INTEGER,lastuptime INTEGER)");
	}

	// 如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// db.execSQL("ALTER TABLE tip ADD COLUMN other STRING");
//		Log.d(TAG, "onUpgrade--oldVersion==)"+oldVersion+"  newVersion=="+newVersion);
//		db.execSQL("DROP TABLE IF EXISTS cup");
//		db.execSQL("DROP TABLE IF EXISTS Owncup");
		//伊利水杯：V1.X升级到V2.X时
		Tools.savePreference(OcupApplication.getInstance().getApplicationContext(),UtilContact.OPENDATA,"true");
		Logger.e("onUpgrade","onUpgrade 执行更新操作");
		db.execSQL(CREATE_TEMP_YiLiFriends);
		db.execSQL(CREATE_YiLiFriends);
		db.execSQL(INSERT_DATA);
		db.execSQL(DROP_YiLiFriends);
		onCreate(db);
	}
}