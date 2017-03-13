package com.sen5.ocup.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.alarm.Time_show;
import com.sen5.ocup.blutoothstruct.DrinkData;
import com.sen5.ocup.blutoothstruct.DrinkYield;
import com.sen5.ocup.struct.ChatMsgEntity;
import com.sen5.ocup.struct.CupInfo;
import com.sen5.ocup.struct.Teas;
import com.sen5.ocup.struct.Tips;
import com.sen5.ocup.yili.FriendInfo;
import com.sen5.ocup.yili.UserInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 封装所有的业务方法
 * 
 */
public class DBManager {
	private static final String TAG = "DBManager";
	private DBHelper helper;
	public static SQLiteDatabase db;

	public final String tab_tip = "tip";
	public final String tab_tip_mark = "tip_mark";


	public DBManager(Context context) {
		helper = new DBHelper(context);
		// 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
		// mFactory);
		// 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
		if (db == null) {
			synchronized (DBManager.class) {
				if (db == null) {
					db = helper.getWritableDatabase();
				}
			}
		}
	}

	/**
	 * add tips
	 * 
	 * @param tips
	 */
	public void addTipsOrTipsmark(String tableName, ArrayList<Tips> tips) {
		Cursor c = null;
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				for (Tips tip : tips) {
					c = db.query(tableName, null, "date=?", new String[] { tip.getDate() }, null, null, null);
					if (null != c) {
						if (c.getCount() <= 0) {
							Cursor cursor = db.query(tableName, null, "id=? ", new String[] { tip.getId() }, null, null, null);
							if (null != cursor) {
								if (cursor.getCount() > 0) {
									while (cursor.moveToNext()) {
										if (Long.parseLong(cursor.getString(cursor.getColumnIndex("date"))) < Long.parseLong(tip.getDate())) {
											ContentValues values = new ContentValues();
											values.put("date", tip.getDate());
											values.put("title", tip.getTitle());
											values.put("brief", tip.getBrief());
											values.put("imgName", tip.getImgName());
											values.put("imgUrl", tip.getImgUrl());
											values.put("isMarked", tip.getIsMarked());
											db.update(tableName, values, "id=?", new String[] { tip.getId() });
										}
									}
								} else {
									db.execSQL(
											"INSERT INTO " + tableName + " VALUES(?,?, ?, ?, ?, ?, ?,?,?)",
											new Object[] { null, tip.getId(), tip.getDate(), tip.getTitle(), tip.getBrief(), tip.getImgName(), tip.getImgUrl(), null,
													tip.getIsMarked() });
								}
								cursor.close();
							}
						}
						c.close();
					}
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				if (null != c && !c.isClosed()) {
					c.close();
				}
				db.endTransaction(); // 结束事务
			}
		}
	}

	/**
	 * add tip
	 * 
	 */
	public void addTipOrTipmark(String tableName, Tips tip) {
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				db.execSQL("INSERT INTO " + tableName + " VALUES(?,?, ?, ?, ?, ?, ?,?,?)",
						new Object[] { null, tip.getId(), tip.getDate(), tip.getTitle(), tip.getBrief(), tip.getImgName(), tip.getImgUrl(), tip.getIsMarked() });
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}

	public void deleteTip(String tableName, Tips tip) {
		if (db.isOpen()) {
			db.delete(tableName, "date=?", new String[] { tip.getDate() });
		}
	}

	/**
	 * 判断表中是否有时间为date的数据
	 * 
	 * @param date
	 * @return
	 */
	public boolean queryTipsmark(String date) {
		// tip_mark
		Cursor c = null;

		if (db.isOpen()) {
			c = db.query("tip_mark", new String[] { "date" }, "date=" + date, null, null, null, null);
		}
		if (null != c) {
			if (c.getCount() > 0) {
				c.close();
				return true;
			} else {
				c.close();
				return false;
			}
		}
		return false;

	}

	/**
	 * query all tips, return list
	 * 
	 * @return ArrayList<Tips>
	 */
	public ArrayList<Tips> queryTipsOrTipsmark(String tableName) {
		ArrayList<Tips> tips = new ArrayList<Tips>();
		ArrayList<String> ids = new ArrayList<String>();
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			Cursor c = queryTheCursor(tableName);
			
			try {
				if(null != c){
					while (c.moveToNext()) {
						String id = c.getString(c.getColumnIndex("id"));
						if (!ids.contains(id)) {
							ids.add(id);
	
							Tips tip = new Tips();
							tip.setId(id);
							tip.setDate(c.getString(c.getColumnIndex("date")));
							tip.setTitle(c.getString(c.getColumnIndex("title")));
							tip.setBrief(c.getString(c.getColumnIndex("brief")));
							tip.setImgName(c.getString(c.getColumnIndex("imgName")));
							tip.setImgUrl(c.getString(c.getColumnIndex("imgUrl")));
							tip.setIsMarked(c.getInt(c.getColumnIndex("isMarked")));
	
							tips.add(tip);
						}
					}
					c.close();
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				if (null != c && !c.isClosed()) {
					c.close();
				}
				db.endTransaction(); // 结束事务
			}
		}

		return tips;
	}

	/**
	 * delete table
	 * 
	 */
	public void deleteAll(String tableName) {
		if (db.isOpen()) {
			db.delete(tableName, null, null);
		}
	}

	/**
	 * query all tip, return cursor
	 * 
	 * @return Cursor
	 */
	public Cursor queryTheCursor(String tableName) {
		Cursor c = null;
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				if (tableName.equals(tab_tip)) {
					c = db.rawQuery("SELECT * FROM " + tableName + " order by date DESC", null);
				} else if (tableName.equals(tab_tip_mark)) {
					c = db.rawQuery("SELECT * FROM " + tableName + " order by _id DESC", null);
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
		return c;
	}

	/**
	 * close database
	 */
	public void closeDB() {
		if (db.isOpen()) {
			db.close();
		}
	}

	/**
	 * 添加杯子实体到cup表中,若表中已经存在此cupID,则更新此cup信息
	 */
	public void addCup(CupInfo cup) {

		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				Cursor c = db.query("Owncup", null, "cupID='" + cup.getCupID() + "'", null, null, null, null);
				if (c != null) {
					Log.d(TAG, "addCup=======c.getCount()==" + c.getCount());
					if (c.getCount() <= 0) {
						db.execSQL("INSERT INTO Owncup  VALUES(?, ?, ?, ?,?,?,?,?,?)",
								new Object[] { cup.getCupID(), cup.getBlueAdd(), cup.getName(), cup.getHuanxin_userid(), cup.getHuanxin_pwd(), cup.getEmail(), cup.getAvatorPath(),
										cup.getMood(), cup.getIntakegoal() });
					} else {
					}
					c.close();
					c = null;
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}

	public void updateOwnCup(CupInfo cup) {
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				Cursor c = db.query("Owncup", null, "cupID='" + cup.getCupID() + "'", null, null, null, null);
				if (c != null) {
					if (c.getCount() > 0) {
						ContentValues values = new ContentValues();
						values.put("blueAdd", cup.getBlueAdd());
						
						Log.e(TAG, "---------------11nichenCup = " +cup.getName() );
						values.put("name", cup.getName());
						values.put("huanxin_userid", cup.getHuanxin_userid());
						values.put("huanxin_pwd", cup.getHuanxin_pwd());
						values.put("email", cup.getEmail());
						values.put("avatorPath", cup.getAvatorPath());
						values.put("mood", cup.getMood());
						values.put("intakegoal", cup.getIntakegoal());
						db.update("Owncup", values, "cupID='" + cup.getCupID() + "'", null);
					}
					c.close();
					c = null;
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}

	/**
	 * 根据cupid查询杯子信息
	 * 
	 * @return
	 */
	public CupInfo queryOwnCup(String cupID) {
		Log.d(TAG, "queryOwnCup------cupID==" + cupID);
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				Cursor c = db.query("Owncup", null, "cupID='" + cupID + "'", null, null, null, null);
				if (null != c) {
					Log.d(TAG, "queryOwnCup----------c.getCount()=" + c.getCount());
					if (c.getCount() > 0) {
						c.moveToFirst();
						OcupApplication.getInstance().mOwnCup.setCupID(c.getString(c.getColumnIndex("cupID")));
						OcupApplication.getInstance().mOwnCup.setBlueAdd(c.getString(c.getColumnIndex("blueAdd")));
						OcupApplication.getInstance().mOwnCup.setHuanxin_pwd(c.getString(c.getColumnIndex("huanxin_pwd")));
						OcupApplication.getInstance().mOwnCup.setMood(c.getString(c.getColumnIndex("mood")));
						OcupApplication.getInstance().mOwnCup.setIntakegoal(c.getInt(c.getColumnIndex("intakegoal")));
						OcupApplication.getInstance().mOwnCup.setName(c.getString(c.getColumnIndex("name")),3);
						OcupApplication.getInstance().mOwnCup.setHuanxin_userid(c.getString(c.getColumnIndex("huanxin_userid")));
						OcupApplication.getInstance().mOwnCup.setEmail(c.getString(c.getColumnIndex("email")));
						OcupApplication.getInstance().mOwnCup.setAvatorPath(c.getString(c.getColumnIndex("avatorPath")));
						return OcupApplication.getInstance().mOwnCup;
					}
					c.close();
					c = null;
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
		Log.d(TAG, "queryOwnCup---name=" + OcupApplication.getInstance().mOwnCup.getName());
		return OcupApplication.getInstance().mOwnCup;
	}

	/**
	 * 添加配对杯子信息
	 */
	public void addOtherCup(CupInfo cup) {
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				Cursor c = db.query("cup", null, "huanxin_userid='" + cup.getHuanxin_userid() + "'", null, null, null, null);
				if (c != null) {
					if (c.getCount() > 0) {
						ContentValues values = new ContentValues();
						values.put("nickname", cup.getName());
						// values.put("huanxin_userid",
						// cup.getHuanxin_userid());
						values.put("email", cup.getEmail());
						values.put("avatorPath", cup.getAvatorPath());
						values.put("mood", cup.getMood());
						values.put("intakegoal", cup.getIntakegoal());
						values.put("intake", cup.getIntake());
						db.update("cup", values, "huanxin_userid='" + cup.getHuanxin_userid() + "'", null);
					} else {
						db.execSQL("INSERT INTO cup  VALUES(?, ?, ?, ?,?,?,?)",
								new Object[] { cup.getName(), cup.getHuanxin_userid(), cup.getEmail(), cup.getAvatorPath(), cup.getMood(), cup.getIntakegoal(), cup.getIntake() });
					}
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}

	/**
	 * 根据环信id查询对方杯子信息
	 * 
	 * @return
	 */
	public CupInfo queryOtherCup(String huanxinID) {
		Cursor c = null;
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				c = db.query("cup", null, "huanxin_userid=?", new String[] { huanxinID }, null, null, null);
				if (null != c) {
					if (c.getCount() > 0) {
						c.moveToFirst();
						OcupApplication.getInstance().mOtherCup.setName(c.getString(c.getColumnIndex("nickname")),4);
						OcupApplication.getInstance().mOtherCup.setHuanxin_userid(c.getString(c.getColumnIndex("huanxin_userid")));
						OcupApplication.getInstance().mOtherCup.setEmail(c.getString(c.getColumnIndex("email")));
						OcupApplication.getInstance().mOtherCup.setAvatorPath(c.getString(c.getColumnIndex("avatorPath")));
						OcupApplication.getInstance().mOtherCup.setMood(c.getString(c.getColumnIndex("mood")));
						OcupApplication.getInstance().mOtherCup.setIntakegoal(c.getInt(c.getColumnIndex("intakegoal")));
						OcupApplication.getInstance().mOtherCup.setIntake(c.getInt(c.getColumnIndex("intake")));
					}
					c.close();
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				if (null != c && !c.isClosed()) {
					c.close();
				}
				db.endTransaction(); // 结束事务
			}
		}
		OcupApplication.getInstance().mOtherCup.setBlueAdd("");
		OcupApplication.getInstance().mOwnCup.setHuanxin_pwd("");
		OcupApplication.getInstance().mOtherCup.setCupID("");
		return OcupApplication.getInstance().mOtherCup;
	}

	/**
	 * add chat
	 * 
	 * @param chatMsgEntity
	 */
	public void addChat(ChatMsgEntity chatMsgEntity) {
		Logger.e(TAG, "addChat()----------chatMsgEntity.getHuanxinID()==" + chatMsgEntity.getHuanxinID() + "tohuanxinID"+chatMsgEntity.getToHuanxinID()+"      chatMsgEntity.getDate()==" + chatMsgEntity.getDate()
				+ "   chatMsgEntity.getStatus()===" + chatMsgEntity.getStatus() + "   chatMsgEntity.getFromFlag()===" + chatMsgEntity.getFromFlag());
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				db.execSQL(
						"INSERT INTO chat  VALUES(?,?, ?, ?, ?,?,?,?)",
						new Object[] { null, chatMsgEntity.getHuanxinID(), chatMsgEntity.getToHuanxinID(), chatMsgEntity.getDate(), chatMsgEntity.getText(),
								chatMsgEntity.getStatus(), chatMsgEntity.getType(), chatMsgEntity.getFromFlag() });
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}

	public void deleteChat(long date){
		if (db.isOpen()){
			Logger.e("deleteChat"+"删除 date = "+date+"的聊天记录");
			db.delete("chat","date = ?",new String[]{date+""});
		}
	}

	/**
	 * 更新指定消息的状态
	 * 
	 * @param status
	 */
	public void updateChat(long date, int status) {
		Log.d(TAG, "updateChat()----------status==" + status + "   date==" + date);
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				ContentValues values = new ContentValues();
				values.put("status", status);
				int count = db.update("chat", values, "date=" + date, null);
				db.setTransactionSuccessful(); // 设置事务成功完成
				Log.d(TAG, "updateChat()----------count==" + count);
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}

	/**
	 * 更新消息的未读状态为已读
	 * 
	 */
	public void updateChatStatus(String huanxinId) {
		Log.d(TAG, "updateChat()----------huanxinId==" + huanxinId);
		if (null == huanxinId) {
			return;
		}
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				ContentValues values = new ContentValues();
				values.put("status", 1);
				int count = db.update("chat", values, "status=" + 3 + " and cupid = '" + huanxinId + "'", null);
				db.setTransactionSuccessful(); // 设置事务成功完成
				Log.d(TAG, "updateChat()----------count==" + count);
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}

	/**
	 * 返回huanxinid发过来的未读消息数
	 * 
	 * @param huanxinId
	 * @return
	 */
	public int countUnreadMsg(String huanxinId) {
		Log.d(TAG, "countUnreadMsg()----------huanxinId==" + huanxinId);
		Cursor c = null;
		int count = 0;
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				c = db.query("chat", null, "status=3  and cupid ='" + huanxinId + "'", null, null, null, null);
				if (null != c) {
					count = c.getCount();
					Log.d(TAG, "countUnreadMsg()----------count==" + count);
					c.close();
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
				Log.d(TAG, "updateChat()----------count==" + count);
			} finally {
				if (null != c && !c.isClosed()) {
					c.close();
				}
				db.endTransaction(); // 结束事务
			}
		}

		return count;
	}

	/**
	 * 查询所有聊天记录
	 * 
	 * @param cupId
	 * @param toCupid
	 * @return
	 */
	public ArrayList<ChatMsgEntity> queryChat(String cupId, String toCupid) {
		Cursor c = null;
		ArrayList<ChatMsgEntity> chatList = new ArrayList<ChatMsgEntity>();
		if (db.isOpen()) {
			// c = db.query("chat", null,"cupid=? and to_cupid=?", new
			// String[]{cupId,toCupid}, null, null, "date DESC");
			c = db.query("chat", null, "cupid=? or cupid=?", new String[] { cupId, toCupid }, null, null, "date ASC", "0,4");
			if (null != c) {
				Log.d(TAG, "queryChat()----------count==" + c.getCount());
				while (c.moveToNext()) {
					createChatObj(c, chatList);
				}
				c.close();
			}
		}
		return chatList;
	}

	/**
	 * 查询count条数据
	 * 
	 * @param cupId
	 * @param toCupid
	 * @return
	 */
	public ArrayList<ChatMsgEntity> queryChat(String cupId, String toCupid, int fromId, boolean isFirst) {
		Logger.e("queryChat() -----------");
		ArrayList<ChatMsgEntity> chatList = new ArrayList<ChatMsgEntity>();
		if (null == cupId || null == toCupid || cupId.equals("") || toCupid.equals("")) {
			return chatList;
		}
		Cursor c = null;
		if (db.isOpen()) {
			int id = fromId;
			if (isFirst) {
				ChatMsgEntity lastEntity = queryLastChat(cupId, toCupid);
				if (lastEntity == null) {
					Logger.e("lastEntity == null");
					return chatList;
				}
				chatList.add(lastEntity);
				id = lastEntity.get_id();
			}
			Logger.e("queryChat()   _id==" + id);
			c = db.query("chat", null, "((cupid='" + cupId + "' and to_cupid='" + toCupid + "' and fromflag='" + 0 + "') or (cupid='" + toCupid + "' and to_cupid='" + cupId + "' and fromflag='" + 1 +"' )) and _id<" + id, null,
					null, null, "_id DESC");
			if (null != c) {
				Logger.e("queryChat()----------count==" + c.getCount());
				int count = 0;
				while (c.moveToNext() && count < 8) {//
					createChatObj(c, chatList);
					count++;
				}
				c.close();
			}
			else {
				Logger.e("queryChat() ---- c == null");
			}
		}
		return chatList;
	}

	/**
	 * 查询最后一条记录
	 * 
	 * @param cupId
	 * @param toCupid
	 * @return
	 */
	public ChatMsgEntity queryLastChat(String cupId, String toCupid) {
		Log.d(TAG, " queryLastChat() ---------");
		if (null == cupId || null == toCupid || cupId.equals("") || toCupid.equals("")) {
			return null;
		}
		Cursor c = null;
		ArrayList<ChatMsgEntity> chatList = new ArrayList<ChatMsgEntity>();
		if (db.isOpen()) {
			c = db.query("chat", null, "(cupid='" + cupId + "' and to_cupid='" + toCupid + "') or (cupid='" + toCupid + "' and to_cupid='" + cupId + "' )", null, null, null,
					"_id DESC");
			if (null != c) {
				Log.d(TAG, "queryLastChat()----------count==" + c.getCount());

				if (c.getCount() > 0) {
					c.moveToFirst();
					createChatObj(c, chatList);
				}
				c.close();
			}
		}

		if (chatList.size() > 0) {
			return chatList.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 查询到消息记录，保存到对象中
	 * 
	 * @param c
	 * @param chatList
	 */
	private void createChatObj(Cursor c, ArrayList<ChatMsgEntity> chatList) {
		ChatMsgEntity chat = new ChatMsgEntity();
		chat.setHuanxinID(c.getString(c.getColumnIndex("cupid")));
		chat.setToHuanxinID(c.getString(c.getColumnIndex("to_cupid")));
		chat.setDate(c.getLong(c.getColumnIndex("date")));
		chat.setText(c.getString(c.getColumnIndex("content")));
		chat.setStatus(c.getInt(c.getColumnIndex("status")));
		chat.setType(c.getInt(c.getColumnIndex("type")));
		chat.set_id(c.getInt(c.getColumnIndex("_id")));
		chat.setFromFlag(c.getInt(c.getColumnIndex("fromflag")));
		chatList.add(chat);
		Log.d(TAG, "_id===" + c.getInt(c.getColumnIndex("_id")));
	}

	/**
	 * 添加配对杯子
	 * 
	 */
	public void addCup_mate(String huanxinID, String mate_huanxinID) {
		Log.d(TAG, "addCup_mate---------huanxinID--==" + huanxinID + "  mate_huanxinID==" + mate_huanxinID);
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				if (null == queryCup_mate(huanxinID)) {
					db.execSQL("INSERT INTO cup_mate  VALUES(?, ?)", new Object[] { huanxinID, mate_huanxinID });
				} else {
					db.execSQL("update cup_mate set id='" + huanxinID + "',mate_id='" + mate_huanxinID + "' where id='" + huanxinID + "' or mate_id='" + huanxinID + "'");
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}

	public void deleteCup_mate(String huanxinID) {
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				String[] args = { huanxinID };
				db.delete("cup_mate", "id=?", args);
				db.delete("cup_mate", "mate_id=?", args);
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}

	/**
	 * 查询cupId是否有配对
	 * 
	 *            返回配对的cupid
	 * @return
	 */
	public String queryCup_mate(String huanxinID) {
		Log.d(TAG, "queryCup_mate----huanxinID------" + huanxinID);
		Cursor c = null;
		if (db.isOpen()) {
			c = db.query("cup_mate", null, "id=? or mate_id=?", new String[] { huanxinID, huanxinID }, null, null, null);
			Log.d(TAG, "queryCup_mate----------c.getCount() ==" + c.getCount());
			if (null != c) {
				String cupid = "";
				String tocupid = "";
				if (c.getCount() > 0) {
					c.moveToFirst();
					cupid = c.getString(c.getColumnIndex("id"));
					tocupid = c.getString(c.getColumnIndex("mate_id"));
					c.close();
					if (huanxinID.equals(cupid)) {
						return tocupid;
					} else {
						return cupid;
					}
				}
				c.close();
			}
		}
		return null;
	}

	public void clearAllDrinkData(){
		String cupid = OcupApplication.getInstance().mOwnCup.getCupID();
		Log.d(TAG, "clearAllDrinkData-----------drinkData.getCupid()===" + cupid);
		if (cupid == null || cupid.equals("")) {
			return;
		}
		String date = "";
		if (db.isOpen()) {
			Cursor c = null;
			db.beginTransaction(); // 开始事务
			try {
				
				c = db.query("drinkdata", null, "cupid='" + cupid + "'", null, null, null, null);
				Log.d(TAG, "clearAllDrinkData-----------date===" + date);
				if (c != null) {
					Log.d(TAG, "clearAllDrinkData----c.getCount()==" + c.getCount());
					while (c.moveToNext()) {
						ContentValues values = new ContentValues();
						values.put("water_yield", 0);
						values.put("drink_srv", 0);
						int n = db.update("drinkdata", values, null, null);
						Log.d(TAG, "clearAllDrinkData---n==" + n);
					}
					c.close();
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}
	
	
	/**
	 * 更新当天的喝水记录致0，状态改为未上传到服务器
	 */
	public void updateDrinkData() {
		String cupid = OcupApplication.getInstance().mOwnCup.getCupID();
		Log.d(TAG, "updateDrinkData-----------drinkData.getCupid()===" + cupid);
		if (cupid == null || cupid.equals("")) {
			return;
		}
		String date = "";
		if (db.isOpen()) {
			Cursor c = null;
			db.beginTransaction(); // 开始事务
			try {
				SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd");
				String str_date = f.format(System.currentTimeMillis());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date d2 = null;
				try {
					d2 = sdf.parse(str_date);
					Log.d(TAG, "updateDrinkData---d2===" + d2);
					date = String.valueOf(d2.getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				c = db.query("drinkdata", null, "cupid='" + cupid + "' and drink_date='" + date + "'", null, null, null, null);
				Log.d(TAG, "updateDrinkData-----------date===" + date);
				if (c != null) {
					Log.d(TAG, "updateDrinkData----c.getCount()==" + c.getCount());
					while (c.moveToNext()) {
						ContentValues values = new ContentValues();
						values.put("water_yield", 0);
						values.put("drink_srv", 0);
						int n = db.update("drinkdata", values, "drink_time=" + c.getInt(c.getColumnIndex("drink_time")) + " and drink_date='" + date + "'", null);
						Log.d(TAG, "updateDrinkData---n==" + n);
					}
					c.close();
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}

	/**
	 * 添加喝水记录
	 */
	public void addDrinkData(DrinkData drinkData) {
		Log.d(TAG, "addDrinkData-----------drinkData.getCupid()===" + drinkData.getCupid() + " time==" + drinkData.getDrink_time() + "     date==" + drinkData.getDrink_date()
				+ "  drinkData.getWater_yield==" + drinkData.getWater_yield());
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			Cursor c = null;
			try {
				c = db.query("drinkdata", null,
						"cupid='" + drinkData.getCupid() + "' and drink_time = " + drinkData.getDrink_time() + " and drink_date='" + drinkData.getDrink_date() + "'", null, null,
						null, null);
				Log.d(TAG, "addDrinkData----------- c.getCount()==" + c.getCount());
				if (c != null) {
					if (c.getCount() == 0) {
						db.execSQL("INSERT INTO drinkdata  VALUES(?,?,?,?,?,?)", new Object[] { drinkData.getCupid(), drinkData.getWater_temp(), drinkData.getWater_yield(),
								drinkData.getDrink_date(), drinkData.getDrink_time(), 0 });
					} else {
						ContentValues values = new ContentValues();
						values.put("water_yield", drinkData.getWater_yield());
						values.put("water_temp", drinkData.getWater_temp());
						values.put("drink_srv", 0);
						db.update("drinkdata", values,
								"cupid='" + drinkData.getCupid() + "' and drink_time = " + drinkData.getDrink_time() + " and drink_date = '" + drinkData.getDrink_date() + "'",
								null);
					}
					c.close();
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				if (null != c && !c.isClosed()) {
					c.close();
				}
				db.endTransaction(); // 结束事务
			}
			// 更新到服务器
			ArrayList<DrinkData> list2srv = getDrinkNeedSrv(drinkData.getCupid());
			if (list2srv != null && list2srv.size() > 0) {
				HttpRequest.getInstance().uploadDrink(list2srv);
			}
		}
	}

	/**
	 * 通过服务器获取饮水数据更新服务器
	 */
	public void updateDrinkFromSrv(DrinkData drinkData) {
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			Cursor c = null;
			try {
				c = db.query("drinkdata", null,
						"cupid='" + drinkData.getCupid() + "' and drink_time = " + drinkData.getDrink_time() + " and drink_date='" + drinkData.getDrink_date() + "'", null, null,
						null, null);
				Log.d(TAG,
						"updateDrinkFromSrv----------- c.getCount()==" + c.getCount() + "    drink_date==" + drinkData.getDrink_date() + "   water_yield=="
								+ drinkData.getWater_yield());
				if (c != null) {
					if (c.getCount() == 0) {
						db.execSQL("INSERT INTO drinkdata  VALUES(?,?,?,?,?,?)", new Object[] { drinkData.getCupid(), drinkData.getWater_temp(), drinkData.getWater_yield(),
								drinkData.getDrink_date(), drinkData.getDrink_time(), 1 });
					} else {
						while (c.moveToNext()) {
							if (c.getInt(c.getColumnIndex("drink_srv")) != 0) {
								ContentValues values = new ContentValues();
								// int preWater_yield =
								// c.getInt(c.getColumnIndex("water_yield"));
								// if (preWater_yield <
								// drinkData.getWater_yield()) {
								values.put("water_yield", drinkData.getWater_yield());
								values.put("water_temp", drinkData.getWater_temp());
								values.put("drink_srv", 1);
								db.update("drinkdata", values, "cupid='" + drinkData.getCupid() + "' and drink_time = " + drinkData.getDrink_time() + " and drink_date = '"
										+ drinkData.getDrink_date() + "'", null);
								// }
							} else {
								Log.d(TAG, "updateDrinkFromSrv---drink_srv==0");
							}
						}
					}
					c.close();
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				if (null != c && !c.isClosed()) {
					c.close();
				}
				db.endTransaction(); // 结束事务
			}
		}
	}

	
	
	/**
	 * 查询表中未上传到服务器上的记录
	 * 
	 * @param cupID
	 * @return
	 */
	public ArrayList<DrinkData> getDrinkNeedSrv(String cupID) {
		ArrayList<DrinkData> list = new ArrayList<DrinkData>();
		if (null == cupID) {
			return list;
		}
		Log.d(TAG, "getDrinkNeedSrv-----------cupID===" + cupID);
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				Cursor c = db.query("drinkdata", null, "cupid='" + cupID + "' and drink_srv = " + 0, null, null, null, "drink_date desc,drink_time desc");// drink_srv==0
																																							// 表示记录未上传到服务器
				Log.d(TAG, "getDrinkNeedSrv----------- c.getCount()==" + c.getCount());
				if (c != null) {
					if (c.getCount() > 0) {
						while (c.moveToNext()) {
							DrinkData mDrinkData = new DrinkData();
							mDrinkData.setCupid(c.getString(c.getColumnIndex("cupid")));
							mDrinkData.setWater_temp(c.getInt(c.getColumnIndex("water_temp")));
							mDrinkData.setWater_yield(c.getInt(c.getColumnIndex("water_yield")));
							mDrinkData.setDrink_time(c.getInt(c.getColumnIndex("drink_time")));
							mDrinkData.setDrink_date(c.getLong(c.getColumnIndex("drink_date")));
							list.add(mDrinkData);
						}
					}
					c.close();
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
		return list;
	}

	/**
	 * 更新饮水数据状态为已经上传到服务器
	 * 
	 * @param drinkData
	 */
	public void setDrinkNeedSrv(ArrayList<DrinkData> drinkData) {
		Log.d(TAG, "setDrinkNeedSrv---------");
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				for (int i = 0; i < drinkData.size(); i++) {
					ContentValues values = new ContentValues();
					values.put("drink_srv", 1);
					int count = db.update("drinkdata", values, "cupid='" + drinkData.get(i).getCupid() + "' and drink_time = " + drinkData.get(i).getDrink_time()
							+ " and drink_date = '" + drinkData.get(i).getDrink_date() + "'", null);
					Log.d(TAG, "setDrinkNeedSrv---------count==" + count);
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}

	/**
	 * 查询当天的饮水量
	 * 
	 * @param date
	 *            yyyy-MM-dd 毫秒
	 * @return
	 */
	public DrinkYield queryDrinkYield_day(long date) {

		DrinkYield drinkYield = new DrinkYield();
		ArrayList<DrinkData> drinkList = new ArrayList<DrinkData>();
		Cursor c = null;
		db.beginTransaction();
		try {
			if (db.isOpen()) {
				String cupid = OcupApplication.getInstance().mOwnCup.getCupID();
				if (null != cupid) {
					c = db.query("drinkdata", null, "cupid='" + cupid + "' and drink_date=" + date, null, null, null, null);
				}
				if (null != c) {
					while (c.moveToNext()) {
						DrinkData mDrinkData = new DrinkData();
						mDrinkData.setCupid(c.getString(c.getColumnIndex("cupid")));
						mDrinkData.setWater_temp(c.getInt(c.getColumnIndex("water_temp")));
						mDrinkData.setWater_yield(c.getInt(c.getColumnIndex("water_yield")));
						mDrinkData.setDrink_time(c.getInt(c.getColumnIndex("drink_time")));
						mDrinkData.setDrink_date(c.getLong(c.getColumnIndex("drink_date")));
						drinkList.add(mDrinkData);
					}
				}
			}
			if (drinkList.size() > 0) {
				int water_yield = 0;
				for (DrinkData drinkData : drinkList) {
					water_yield += drinkData.getWater_yield();
				}
				drinkYield.setWater_yield(water_yield);
				drinkYield.setDrink_date(drinkList.get(0).getDrink_date());
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {

		} finally {
			if(null != c){
				c.close();
			}
			db.endTransaction();
		}

		return drinkYield;
	}

	/**
	 * 返回startDate (包含)到 endDate（不包含）之间月每天的饮水总量
	 * 
	 * @param startDate
	 *            yyyy-MM-dd 毫秒
	 * @param endDate
	 *            yyyy-MM-dd 毫秒
	 * @return
	 */
	public ArrayList<DrinkYield> queryDrinkYield_month(long startDate, long endDate) {
		ArrayList<DrinkYield> drinkYieldList = new ArrayList<DrinkYield>();
		ArrayList<DrinkData> drinkList = new ArrayList<DrinkData>();
		Cursor c = null;
		if (db.isOpen()) {
			Log.d(TAG, "queryDrinkYield_month()  ---------");
			String cupid = OcupApplication.getInstance().mOwnCup.getCupID();
			if (null != cupid) {
				c = db.query("drinkdata", null, "cupid='" + cupid + "' and drink_date<" + endDate + " and drink_date>=" + startDate, null, null, null, "drink_time asc");
			}
			if (null != c) {
				Log.d(TAG, "queryDrinkYield_month()----------count==" + c.getCount());
				while (c.moveToNext()) {
					DrinkData mDrinkData = new DrinkData();
					mDrinkData.setCupid(c.getString(c.getColumnIndex("cupid")));
					mDrinkData.setWater_temp(c.getInt(c.getColumnIndex("water_temp")));
					mDrinkData.setWater_yield(c.getInt(c.getColumnIndex("water_yield")));
					mDrinkData.setDrink_time(c.getInt(c.getColumnIndex("drink_time")));
					mDrinkData.setDrink_date(c.getLong(c.getColumnIndex("drink_date")));
					drinkList.add(mDrinkData);
				}
				c.close();
			}
		}
		if (drinkList.size() > 1) {
			DrinkYield mDrinkYield = null;
			int water_yield = 0;
			for (int i = 1; i < drinkList.size(); i++) {
				if (drinkList.get(i).getDrink_date() != drinkList.get(i - 1).getDrink_date()) {
					if (null != mDrinkYield) {
						mDrinkYield.setWater_yield(water_yield);
						drinkYieldList.add(mDrinkYield);
					}
					mDrinkYield = new DrinkYield();
					water_yield = drinkList.get(i).getWater_yield();
					mDrinkYield.setDrink_date(drinkList.get(i).getDrink_date());
				} else {
					water_yield += drinkList.get(i).getWater_yield();
				}

			}
		} else if (drinkList.size() == 1) {
			DrinkYield mDrinkYield = new DrinkYield();
			mDrinkYield.setWater_yield(drinkList.get(0).getWater_yield());
			mDrinkYield.setDrink_date(drinkList.get(0).getDrink_date());
			drinkYieldList.add(mDrinkYield);
		}
		return drinkYieldList;
	}

	/**
	 * 查询date这天的对应每小时的水量
	 * 
	 * @param date
	 *            毫秒
	 * @return ArrayList<Integer> 返回每小时之前的饮水总量 size是24
	 * 
	 *         cupid==2e0017301400454e5830 long date==1415030400000
	 *         cupid==2e0017301400454e5830 long date==1414857600000
	 */
	public ArrayList<Integer> queryDrinkData(long date) {
		ArrayList<Integer> yieldList = new ArrayList<Integer>();
		ArrayList<DrinkData> drinkList = new ArrayList<DrinkData>();
		Cursor c = null;
		if (db.isOpen()) {
			String cupid = OcupApplication.getInstance().mOwnCup.getCupID();
			Log.d(TAG, "queryDrinkData()  ---------cupid==" + cupid + "  long date==" + date);
			if (null != cupid) {
				c = db.query("drinkdata", null, "cupid='" + cupid + "' and drink_date='" + date + "'", null, null, null, "drink_time asc");
			}
			if (null != c) {
				Log.d(TAG, "queryDrinkData()----------count==" + c.getCount());
				while (c.moveToNext()) {
					//
					DrinkData mDrinkData = new DrinkData();
					mDrinkData.setCupid(c.getString(c.getColumnIndex("cupid")));
					mDrinkData.setWater_temp(c.getInt(c.getColumnIndex("water_temp")));
					mDrinkData.setWater_yield(c.getInt(c.getColumnIndex("water_yield")));
					mDrinkData.setDrink_time(c.getInt(c.getColumnIndex("drink_time")));
					mDrinkData.setDrink_date(c.getLong(c.getColumnIndex("drink_date")));
					drinkList.add(mDrinkData);
					Log.d(TAG, "queryDrinkData()-----mDrinkData.getDrink_time()==" + mDrinkData.getDrink_time() + "   mDrinkData.getWater_yield===" + mDrinkData.getWater_yield());
				}
				c.close();
			}
		}

		for (int i = 0; i < 24; i++) {
			int water = 0;
			for (int j = 0; j < drinkList.size(); j++) {
				if (drinkList.get(j).getDrink_time() < (i + 1) * 60 * 60) {
					water += drinkList.get(j).getWater_yield();
				} else if (drinkList.get(j).getDrink_time() == 0) {
					water += drinkList.get(j).getWater_yield();
				}
			}
			yieldList.add(water);
		}

		return yieldList;
	}

	/**
	 * 获取饮水数据表中最大的time
	 * 
	 * @return
	 */
	public int queryDrinkDataMaxTime() {
		int time = 0;
		String date = "";
		Cursor c = null;
		if (db.isOpen()) {
			String cupid = OcupApplication.getInstance().mOwnCup.getCupID();
			Log.d(TAG, "queryDrinkDataMaxTime()  ---------cupid==" + cupid);
			if (null != cupid) {
				SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd");
				String str_date = f.format(System.currentTimeMillis());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date d2 = null;
				try {
					d2 = sdf.parse(str_date);
					Log.d(TAG, "queryDrinkDataMaxTime---d2===" + d2);
					date = String.valueOf(d2.getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				c = db.query("drinkdata", null, "cupid='" + cupid + "' and drink_date='" + date + "'", null, null, null, "drink_time asc");
			}
			if (null != c) {
				Log.d(TAG, "queryDrinkDataMaxTime()----------count==" + c.getCount());
				if (c.moveToLast()) {
					time = c.getInt(c.getColumnIndex("drink_time"));
				}
				c.close();
			}
		}
		Log.d(TAG, "queryDrinkDataMaxTime()----time==" + time);
		return time;
	}

	/**
	 * 查询date这天饮水记录
	 * 
	 * @param date
	 */
	public ArrayList<DrinkData> queryDetailDrinkData(long date) {
		Log.d(TAG, "queryDetailDrinkData()  ----");
		ArrayList<DrinkData> drinkList = new ArrayList<DrinkData>();
		Cursor c = null;
		if (db.isOpen()) {
			String cupid = OcupApplication.getInstance().mOwnCup.getCupID();
			Log.d(TAG, "queryDetailDrinkData()  ---------cupid==" + cupid);
			if (null != cupid) {
				c = db.query("drinkdata", null, "cupid='" + cupid + "' and drink_date='" + date + "'", null, null, null, "drink_time asc");
			}
			if (null != c) {
				Log.d(TAG, "queryDetailDrinkData()----------count==" + c.getCount());
				while (c.moveToNext()) {
					//
					DrinkData mDrinkData = new DrinkData();
					mDrinkData.setCupid(c.getString(c.getColumnIndex("cupid")));
					mDrinkData.setWater_temp(c.getInt(c.getColumnIndex("water_temp")));
					mDrinkData.setWater_yield(c.getInt(c.getColumnIndex("water_yield")));
					mDrinkData.setDrink_time(c.getInt(c.getColumnIndex("drink_time")));
					mDrinkData.setDrink_date(c.getLong(c.getColumnIndex("drink_date")));
					drinkList.add(mDrinkData);
					Log.d(TAG, "queryDetailDrinkData()  ----mDrinkData.getWater_yield()===" + mDrinkData.getWater_yield());
				}
				c.close();
			}
		}

		return drinkList;
	}

	/**
	 * add teas
	 * 
	 * @param listTeas
	 */
	public void addTeas(ArrayList<Teas> listTeas) {
		Log.d(TAG, "addTeas------------------------listTeas.size()====" + listTeas.size());
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				db.delete("tea", null, null);// 先清空数据
				for (Teas tea : listTeas) {
					db.execSQL("INSERT INTO tea VALUES(?, ?, ?, ?, ?, ?, ?)",
							new Object[] { null, tea.getId(), tea.getDate(), tea.getTeaName(), tea.getImgurl(), tea.getDesc(), null });
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}

	/**
	 * 查询tea表中的所有数据
	 * 
	 * @return
	 */
	public ArrayList<Teas> queryTeas() {
		ArrayList<Teas> teas = new ArrayList<Teas>();
		Cursor c = null;
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				c = db.query("tea", null, null, null, null, null, null);
				if (c != null) {
					Log.d(TAG, "queryTeas----------c.getCount()====" + c.getCount());
					while (c.moveToNext()) {
						Teas tea = new Teas();
						tea.setId(c.getString(c.getColumnIndex("id")));
						tea.setDate(c.getString(c.getColumnIndex("date")));
						tea.setImgurl(c.getString(c.getColumnIndex("imgUrl")));
						tea.setTeaName(c.getString(c.getColumnIndex("teaName")));
						tea.setDesc(c.getString(c.getColumnIndex("desc")));

						teas.add(tea);
					}
					c.close();
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}

		}
		return teas;
	}

	/**
	 * 添加闹钟时间
	 * @param timeshow
	 */
	public void add_alarmtime(Time_show timeshow) {
		Log.d(TAG, "add_alarmtime-----------timeshow.isFlag()=" + timeshow.isFlag() + "alarmtime=" + timeshow.getTime());
		if (db.isOpen()) {
			db.beginTransaction();
			try {
				Cursor c = db.query("alarmdata", null, "alarm_time ='" + timeshow.getTime() + "'", null, null, null, null);
				Log.d(TAG, "add_alarmtime-----------------c.getcount()==" + c.getCount());
				if (c != null) {
					if (c.getCount() <= 0) {
						db.execSQL("INSERT INTO alarmdata  VALUES(?,?)", new Object[] { timeshow.getTime(), timeshow.isFlag() });
					} else {
						ContentValues values = new ContentValues();
						values.put("flag", timeshow.isFlag());
						db.update("alarmdata", values, "alarm_time ='" + timeshow.getTime() + "'", null);
					}
					c.close();
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction(); // ????????
			}
		}
	}

	/**
	 *  删除闹钟记录
	 * @param time
	 */
	public void deleteAlarm_time(String time) {
		if (db.isOpen()) {
			db.beginTransaction();
			try {
				String[] times = { time };
				db.delete("alarmdata", "alarm_time=?", times);
				db.setTransactionSuccessful();
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				db.endTransaction();
			}
		}
	}

	/**
	 * 删除所有闹钟记录
	 */
	public void deleteAllAlarm_time() {
		Log.d(TAG, "deleteAllAlarm_time-----");
		if (db.isOpen()) {
			db.beginTransaction();
			try {
				db.delete("alarmdata", null, null);
				db.setTransactionSuccessful();
			} catch (Exception e) {
				Log.d(TAG, "deleteAllAlarm_time----Exception e==" + e);
			} finally {
				db.endTransaction();
			}
		}
	}

	/**
	 * 查询设置的闹钟时间，返回时间列表
	 * @param date
	 * @return
	 */
	public ArrayList<Time_show> queryAlarmData(String date) {

		ArrayList<Time_show> timedata = new ArrayList<Time_show>();
		Cursor c = null;
		if (db.isOpen()) {
			c = db.query("alarmdata", null, null, null, null, null, "alarm_time asc");
		}
		if (null != c) {
			Log.d(TAG, "queryAlarmData-----------count==" + c.getCount());
			while (c.moveToNext()) {
				Time_show time_data = new Time_show();
				time_data.setTime(c.getString(c.getColumnIndex("alarm_time")));
				time_data.setFlag(c.getInt(c.getColumnIndex("flag")));
				timedata.add(time_data);
				Log.d(TAG, "queryAlarmData-------time_data.isFlag()==" + time_data.isFlag());
			}
			c.close();
		}
		return timedata;

	}
	
	/**
	 * 添加或者更新是否提醒更新杯子固件表
	 * @param cupID
	 * @param remind 0--->提醒    1----->不提醒
	 * @param targetVerion
	 */
	public void add_RemindUpdate(String cupID,int remind, int targetVerion) {
		Log.d(TAG, "add_RemindUpdate---------");
		if (db.isOpen()) {
			db.beginTransaction();
			try {
				Cursor c = db.query("updatecup",null, "cupID='" + cupID + "' and targetversion=" + targetVerion, null, null, null, null);
				if (c != null) {
					if (c.getCount() <= 0) {
						db.execSQL("INSERT INTO updatecup  VALUES(?,?,?)", new Object[] { cupID, remind, targetVerion});
					} else {
						ContentValues values = new ContentValues();
						values.put("neverremind", remind);
						db.update("updatecup", values, "cupID ='" + cupID + "' and targetversion=" + targetVerion, null);
					}
					c.close();
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction(); 
			}
		}
	}

	/**
	 * 查询当前杯子是否要提醒更新到目标版本
	 * 
	 * @param cupID
	 * @param targetVerion
	 * @return 
	 */
	public boolean queryRemindUpdate(String cupID, int targetVerion) {
		boolean isRemind = true;
		Cursor c = null;
		if (db.isOpen()) {
			c = db.query("updatecup", new String[] { "neverremind" }, "cupID='" + cupID + "' and targetversion=" + targetVerion, null, null, null, null);
		}
		if (null != c) {
			Log.d(TAG, "queryRemindUpdate-----------count==" + c.getCount());
			if (c.moveToNext()) {
				int remind = c.getInt(c.getColumnIndex("neverremind"));
				Log.d(TAG, "queryRemindUpdate-------isRemind==" + remind);
				if (remind == 1) {
					isRemind = false;
				}
			}
			c.close();
		}
		return isRemind;
	}


	/**
	 * 添加杯子实体到cup表中,若表中已经存在此cupID,则更新此cup信息
	 */
	public void addYiLiCup(UserInfo user) {
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				Cursor c = db.query("OwnYiLicup", null, "userID='" + user.getUserID() + "'", null, null, null, null);
				if (c != null) {
					if (c.getCount() <= 0) {
						Logger.e("执行插入操作");
						db.execSQL("INSERT INTO OwnYiLicup  VALUES(?,?,?,?,?,?,?,?,?,?)",
								new Object[] {user.getUserID()+"",user.getNickname(), user.getPassword(), user.getMood(), user.getEmail(), user.getAvator(), user.getGroup_id(),
										user.getZone(), user.getPhone(),user.getAvatorThumbnail() });
					} else {

					}
					c.close();
					c = null;
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
	}

	//注销后删除杯子实体：代表的是本地用户
	public void deleteYiLiCup(){
		if (db.isOpen()){
			db.delete("OwnYiLicup",null,null);
		}
	}

	//(userID TEXT,nickname TEXT,password TEXT," +"mood TEXT,email TEXT,avator TEXT,group_id TEXT,
	// zone TEXT,phone TEXT, avatorThumbnail TEXT)
	public void updateYiLicup(String userId,UserInfo userInfo){
		Logger.e("updateYiLicup","come in  "+userInfo.getMood()+"---nickname = "+userInfo.getNickname());
		if (db.isOpen()){
			ContentValues values = new ContentValues();
			values.put("userID",userInfo.getUserID()+"");
			values.put("nickname",userInfo.getNickname());
			values.put("password",userInfo.getPassword());
			values.put("mood",userInfo.getMood());
			values.put("email",userInfo.getEmail());
			values.put("avator",userInfo.getAvator());
			values.put("group_id",userInfo.getGroup_id());
			values.put("zone",userInfo.getZone());
			values.put("phone",userInfo.getPhone());
			values.put("avatorThumbnail",userInfo.getAvatorThumbnail());
			db.update("OwnYiLicup",values,"userId = ?",new String[]{userId});
			UserInfo userInfo2 = queryYiLiCup();
			Logger.e("updateNickNameAndMood","complement"+userInfo2.getNickname()+"---mood"+userInfo2.getMood());
		}
	}

	public void updateNickNameAndMood(String userId,String nickName,String mood){
		if (db.isOpen()){
			Logger.e("updateNickNameAndMood","come in"+"userId"+userId+"nickName = "+nickName+"---mood = "+mood);
			ContentValues values = new ContentValues();
			values.put("nickname",nickName);
			values.put("mood",mood);
			db.update("OwnYiLicup",values,"userId = ?",new String[]{userId});
			UserInfo userInfo = queryYiLiCup();
			Logger.e("updateNickNameAndMood","complement"+userInfo.getNickname()+"---mood"+userInfo.getMood());
		}
	}


	/*查询yili水杯*/
	public UserInfo queryYiLiCup() {
		UserInfo userInfo = null;
		if (db.isOpen()) {
			db.beginTransaction(); // 开始事务
			try {
				Cursor c = db.query("OwnYiLicup", null, null, null, null, null, null);
				if (null != c) {
					Logger.e("c!="+"null");
					while (c.moveToNext()){
						Logger.e("moveToNext");
						userInfo = new UserInfo();
						String id = c.getString(c.getColumnIndex("userID"));
						userInfo.setUserID(Integer.parseInt(id));
						userInfo.setMood(c.getString(c.getColumnIndex("mood")));
						userInfo.setNickname(c.getString(c.getColumnIndex("nickname")));
						userInfo.setAvator(c.getString(c.getColumnIndex("avator")));
					}
					c.close();
					c = null;
				}
				db.setTransactionSuccessful(); // 设置事务成功完成
			} finally {
				db.endTransaction(); // 结束事务
			}
		}
		Log.d(TAG, "queryOwnCup---name=" + OcupApplication.getInstance().mOwnCup.getName());
		return userInfo;
	}

	//添加好友列表到数据库,同时做验证和更新操作。
	public void addYiLiFriend(List<FriendInfo> infos) {
		if (db.isOpen()){
			db.beginTransaction();
			List<FriendInfo> friendInfos = queryFriends();
			Logger.e("好友数据库列表数目为："+friendInfos.size());
			//本地数据库中的好友
			List<String> contact_ids = new ArrayList<>();
			//服务器好友
			List<String> contact_ids2 = new ArrayList<>();
			for (int i =0; i<friendInfos.size(); i++){
				contact_ids.add(friendInfos.get(i).getContact_id());
			}
			for (int i = 0; i<infos.size(); i++){
				contact_ids2.add(infos.get(i).getContact_id());
			}
			for (int i =0; i<infos.size();i++){
				Logger.e("addYiLiFriend","contact_id = "+infos.get(i).getContact_id()+"LastMsgTime = "+infos.get(i).getLastMsgTime());
				if (!contact_ids.contains(infos.get(i).getContact_id())){
					String contact_id = infos.get(i).getContact_id();
					String group_id = infos.get(i).getGroup_id();
					String avator = infos.get(i).getAvator();
					String nickName = infos.get(i).getNickname();
					String phoneNum = infos.get(i).getPhoneNum();
					long time = infos.get(i).getLastMsgTime();
					int haveCup;
					int lastUPTime = 0;
					int onLine = 0;
					int openData = 0;
					if (group_id != null || group_id != ""){
						 haveCup = 1;
					}
					else {
						 haveCup = 0;
					}
					List<FriendInfo> friendInfos2 =  queryFriends();
					if (friendInfos2.size() != 0){
						for (FriendInfo friendInfo : friendInfos2){
							if (! (friendInfo.getLastMsgTime() > 0)){
								lastUPTime = 0;
							}
						}
					}
					Logger.e("addYiLiFriend","lastMsgTime"+time+"haveCup,online,opendata");
					db.execSQL("INSERT INTO YiLiFriends"+" VALUES(?,?,?,?,?,?,?,?,?,?)",new Object[]{time,contact_id,group_id,avator,nickName,phoneNum,haveCup,onLine,openData,lastUPTime});
				}
				else {
					//该好友已经添加到数据库中，但是可能信息有更改所以进行更新操作
					Logger.e(TAG,"该好友已添加,进行更新操作");
					updateFriendsInfo(infos.get(i));
				}
			}
			if (infos.size() < friendInfos.size()){
				for (int i =0; i<friendInfos.size(); i++){
					//服务器好友列表中已被删除，本地数据库还未删除的数据
					if (! contact_ids2.contains(friendInfos.get(i).getContact_id())){
						deleteOneFriend(friendInfos.get(i).getContact_id());
					}
				}
			}
			db.setTransactionSuccessful();
		}
		db.endTransaction();
	}

	//查询所有所有的好友
	public List<FriendInfo> queryFriends(){
		Cursor cursor = null;
		List<FriendInfo> friends = new ArrayList<>();
		if (db.isOpen()){
			db.beginTransaction();
			cursor = db.query("YiLiFriends",null,null,null,null,null,"time desc");
			if (cursor != null){
				while (cursor.moveToNext()){
					//contact_id TEXT,group_id TEXT,avator TEXT,nickname TEXT
					String contact_id = cursor.getString(cursor.getColumnIndex("contact_id"));
					String group_id = cursor.getString(cursor.getColumnIndex("group_id"));
					String avator = cursor.getString(cursor.getColumnIndex("avator"));
					String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
					long time = cursor.getInt(cursor.getColumnIndex("time"));
					String phoneNum = cursor.getString(cursor.getColumnIndex("phonenum"));
					int havecup = cursor.getInt(cursor.getColumnIndex("havecup"));
					int isonline = cursor.getInt(cursor.getColumnIndex("isonline"));
					int dataopen = cursor.getInt(cursor.getColumnIndex("dataopen"));
					int lastuptime = cursor.getInt(cursor.getColumnIndex("lastuptime"));
					if (contact_id != null){
						FriendInfo friendInfo = new FriendInfo(contact_id,group_id,avator,nickname,time,phoneNum);
						friendInfo.setLastuptime(lastuptime);
						if (havecup == 1){
							friendInfo.setHaveCup(true);
						}
						if (isonline == 1){
							friendInfo.setOnLine(true);
						}
						if (dataopen > 0){//温度的值大于零表示数据公开
							friendInfo.setOpenData(dataopen);
						}
						friends.add(friendInfo);
						Logger.e("queryFriends","friendInfo lastMsgTime = "+friendInfo.getLastMsgTime());
					}
				}
			}
			cursor.close();
			db.setTransactionSuccessful();
		}
		db.endTransaction();
		return friends;
	}

	//更新一个朋友信息，不带状态值
	public void updateFriendsInfo(FriendInfo friendInfo){
		if (db.isOpen()){
			Logger.e("updateFriendsInfo",friendInfo.getNickname());
			ContentValues values = new ContentValues();
			values.put("contact_id",friendInfo.getContact_id());
			values.put("group_id",friendInfo.getGroup_id());
			values.put("avator",friendInfo.getAvator());
			values.put("nickname",friendInfo.getNickname());
			values.put("phonenum",friendInfo.getPhoneNum());
			db.update("YiLiFriends",values,"contact_id = ?",new String[]{friendInfo.getContact_id()});
		}
	}
	//更新一个好友信息，带状态值
	public void updateFriendsInfo2(FriendInfo friendInfo){
		if (db.isOpen()){
			Logger.e("updateFriendsInfo",friendInfo.getNickname());
			ContentValues values = new ContentValues();
			values.put("contact_id",friendInfo.getContact_id());
			values.put("group_id",friendInfo.getGroup_id());
			values.put("avator",friendInfo.getAvator());
			values.put("nickname",friendInfo.getNickname());
			values.put("phonenum",friendInfo.getPhoneNum());
			if (friendInfo.isOnLine()){
				values.put("isonline",1);
			}
			else {
				values.put("isonline",0);
			}
			db.update("YiLiFriends",values,"contact_id = ?",new String[]{friendInfo.getContact_id()});
		}
	}
	//更新一个朋友的状态信息
	public void updateLastUpTime(int time,String contact_id){
		if (db.isOpen()){
			Logger.e(TAG,"updateLastUpTime is coming");
			ContentValues values = new ContentValues();
			values.put("lastuptime",time);
			db.update("YiLiFriends",values,"contact_id = ?",new String[]{contact_id});
		}
	}
	//更新所有朋友的状态信息
	public void updateAllUpTime(int time){
		if (db.isOpen()){
			ContentValues values = new ContentValues();
			values.put("lastuptime",time);
			db.update("YiLiFriends",values,null,null);
		}
	}
	//更新一个朋友的状态信息信息（是否拥有水杯，是否在线，是否公开数据）
	public void updateFriendsStatus(String contact_id,int[] status,int lastUpTime){
		Logger.e("updateFriendsStatus","contact_id is"+contact_id+"lastUptime is"+lastUpTime);
		if (db.isOpen()){
//			havecup INTEGER,isonline INTEGER,dataopen INTEGER
			Logger.e("updateFriendsStatus","contact_id = "+contact_id);
			ContentValues values = new ContentValues();
			values.put("havecup",status[0]);
			values.put("isonline",status[1]);
			values.put("dataopen",status[2]);
			values.put("lastuptime",lastUpTime);
			db.update("YiLiFriends",values,"contact_id = ?",new String[]{contact_id});
		}
	}
	//查询所有好友上次发送状态的时间
	public List<Integer> queryFriendTimes(){
		List<Integer> times = new ArrayList<>();
		Cursor mcursor = null;
		if (db.isOpen()){
			db.beginTransaction();
			mcursor = db.query("YiLiFriends",null,null,null,null,null,null);
			if (mcursor != null){
				while (mcursor.moveToNext()){
					int time = mcursor.getInt(mcursor.getColumnIndex("lastuptime"));
					times.add(time);
					Log.e(TAG,"queryFriendTimes");
				}
			}
			mcursor.close();
			db.setTransactionSuccessful();
		}
		db.endTransaction();
		return times;
	}

	//更新一个朋友信息(只更新时间)
	public void updateFriendsInfoTime(long time, String id){
		if (db.isOpen()){
			Logger.e("updateFriendsInfoTime","time = "+time+"id = "+id);
			ContentValues values = new ContentValues();
			values.put("time",time);
			db.update("YiLiFriends",values,"contact_id = ?",new String[]{id});
			List<FriendInfo> friendInfos = queryFriends();
			for(FriendInfo friendInfo : friendInfos){
				if (friendInfo.getContact_id().equals(id)){
					Logger.e("updateFriendsInfoTime","更新后的friendInfo time = "+friendInfo.getLastMsgTime());
				}
			}
		}
	}

	//切换账号时需要把所有好友删除
	public void deleteFriends(){
		if(db.isOpen()){
			Logger.e("deleteFriends","删除全部好友列表");
			db.delete("YiLiFriends",null,null);
		}
	}
	public void deleteOneFriend(String userId){
		if (db.isOpen()){
			Logger.e("deleteOneFriend"+"删除 userId = "+userId+"好友");
			db.delete("YiLiFriends","contact_id = ?",new String[]{userId});
		}
	}
}
