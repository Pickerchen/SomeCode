package com.sen5.ocup.struct;

public class ChatMsgEntity {
	
	public final static int TYPE_TXT = 0;
	public final static int TYPE_SCRAWL = 1;
	public final static int TYPE_ANIM_FACE =2;
	public final static int TYPE_ADDED = 3;
	public final static int TYPE_DEMATED = 4;
	public final static int TYPE_SCRAWL_ANIM = 5;
	public final static int TYPE_SHAKE = 6;
	

	public final static int FROM_ME = 0;
	public final static int FROM_OTHER = 1;

	private int _id;//在数据库中的顺序id
	private String name;

	private long date;

	private String text;

	private String huanxinID;

	private String toHuanxinID;
	
	private int status;// 0-----正在发送      1----已查看消息     2------未发送消息   3----离线消息  
	
	private int type;// 0----文本   1------涂鸦    2-----动画表情  3----被加为好友  4-----被解除好友关系   5----------涂鸦动画
	
	private int fromFlag;//消息来自自己or他人   0---->自己  1---->他人
	
	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}


//	private boolean isComMeg = true;

	public int getFromFlag() {
		return fromFlag;
	}

	public void setFromFlag(int fromFlag) {
		this.fromFlag = fromFlag;
	}

	public String getHuanxinID() {
		return huanxinID;
	}

	public void setHuanxinID(String huanxinID) {
		this.huanxinID = huanxinID;
	}

	public String getToHuanxinID() {
		return toHuanxinID;
	}

	public void setToHuanxinID(String toHuanxinID) {
		this.toHuanxinID = toHuanxinID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	/**
	 * 创建消息实体
	 * 
	 * @param cupId
	 * @param toCupId
	 * @param content
	 * @return
	 */
	public static ChatMsgEntity createChatMsgEntity(String huanxinID, String toHuanxinID, String content, String time,int type,int fromFlag) {
		ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
		if (time.equals("")) {
			chatMsgEntity.setDate(System.currentTimeMillis());
		} else {
			chatMsgEntity.setDate(Long.parseLong(time));
		}
		chatMsgEntity.setHuanxinID(huanxinID);
		chatMsgEntity.setToHuanxinID(toHuanxinID);
		chatMsgEntity.setText(content);
		chatMsgEntity.setType(type);
		chatMsgEntity.setFromFlag(fromFlag);
		return chatMsgEntity;
	}

}
