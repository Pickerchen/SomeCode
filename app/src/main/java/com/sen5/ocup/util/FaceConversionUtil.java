package com.sen5.ocup.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.TextView;

import com.sen5.ocup.R;
import com.sen5.ocup.struct.ChatEmoji;
import com.sen5.ocup.struct.FaceGif;

/**
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :表情转换工具
 */
public class FaceConversionUtil {

	private static final String TAG = "FaceConversionUtil";

	/** 每一页表情的个数 */
	private int pageSize = 12;

	private static FaceConversionUtil mFaceConversionUtil;

	/** 保存于内存中的表情HashMap */
	public  HashMap<String, String> emojiMap = new HashMap<String, String>();

	/** 保存于内存中的表情集合 */
	private List<ChatEmoji> emojis = new ArrayList<ChatEmoji>();

	/** 表情分页的结果集合 */
	public List<List<ChatEmoji>> emojiLists = new ArrayList<List<ChatEmoji>>();

	private FaceConversionUtil() {

	}

	public static FaceConversionUtil getInstace() {
		if (mFaceConversionUtil == null) {
			mFaceConversionUtil = new FaceConversionUtil();
		}
		return mFaceConversionUtil;
	}

	

	/**
	 * 添加表情
	 * 
	 * @param context
	 * @param imgId
	 * @param spannableString
	 * @return
	 */
	public SpannableString addFace(Context context, int imgId, String spannableString) {
		if (TextUtils.isEmpty(spannableString)) {
			return null;
		}
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imgId);
		bitmap = Bitmap.createScaledBitmap(bitmap, Tools.dip2px(context, 30), Tools.dip2px(context, 30), true);
		ImageSpan imageSpan = new ImageSpan(context, bitmap);
		SpannableString spannable = new SpannableString(spannableString);
		spannable.setSpan(imageSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}

	

	public void getFileText(Context context) {
		ParseData(Tools.getEmojiFile(context), context);
	}

	/**
	 * 解析字符
	 * 
	 * @param data
	 */
	private void ParseData(List<String> data, Context context) {
		emojiMap.clear();
		emojis.clear();
		emojiLists.clear();
		if (data == null) {
			return;
		}
		Log.d(TAG, "ParseData      data.size()==== "+data.size());
		ChatEmoji emojEentry;
		try {
			for (String str : data) {
				String[] text = str.split(",");
				String fileName = text[0].substring(0, text[0].lastIndexOf("."));
				emojiMap.put(text[1], fileName);
				int resID = context.getResources().getIdentifier(fileName, "drawable", context.getPackageName());

				if (resID != 0) {
					emojEentry = new ChatEmoji();
					emojEentry.setId(resID);
					emojEentry.setCharacter(text[1]);
					emojEentry.setFaceName(fileName);
					emojis.add(emojEentry);
				}
			}
			Log.d(TAG, "ParseData     static face  data.size()==== "+data.size());
			// 加载动态表情
			List<ChatEmoji> emoji_gif = new ArrayList<ChatEmoji>();
			Integer[] gifFaceId = FaceGif.gitDrawableID;
			String[] gifFaceCharacter = FaceGif.gifFaceCharacter;
			String[] gifFaceName = FaceGif.gifFaceName;
			for (int i = 0; i < gifFaceId.length; i++) {
				ChatEmoji chatemoji = new ChatEmoji();
				chatemoji.setId(gifFaceId[i]);
				chatemoji.setCharacter(gifFaceCharacter[i]);
				chatemoji.setFaceName(gifFaceName[i]);
				emoji_gif.add(chatemoji);

				emojiMap.put(gifFaceCharacter[i], gifFaceName[i]);
			}
			emojis.addAll(emoji_gif);

			int pageCount = (int) Math.ceil(emojis.size() / (pageSize - 1) + 0.1);

			for (int i = 0; i < pageCount; i++) {
				emojiLists.add(getData(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取分页数据
	 * 
	 * @param page
	 * @return
	 */
	private List<ChatEmoji> getData(int page) {
		int startIndex = page * pageSize-page;
		int endIndex = startIndex + pageSize - 1;

		if (endIndex > emojis.size()) {
			endIndex = emojis.size();
		}
		// 不这么写，会在viewpager加载中报集合操作异常，我也不知道为什么
		List<ChatEmoji> list = new ArrayList<ChatEmoji>();
		list.addAll(emojis.subList(startIndex, endIndex));
		// if (list.size() < pageSize) {
		// for (int i = list.size(); i < pageSize; i++) {
		// ChatEmoji object = new ChatEmoji();
		// list.add(object);
		// }
		// }
		// if (list.size() == pageSize) {
		// ChatEmoji object = new ChatEmoji();
		// object.setId(R.drawable.face_del_icon);
		// list.add(object);
		// }
		ChatEmoji object = new ChatEmoji();
		object.setId(R.drawable.face_del_icon);
		list.add(object);
		return list;
	}
}