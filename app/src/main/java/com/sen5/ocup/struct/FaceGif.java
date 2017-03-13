package com.sen5.ocup.struct;

import java.util.ArrayList;
import java.util.HashMap;

import com.sen5.ocup.R;

public class FaceGif {
	public static String[] gifFaceName = {  "emotion1", "emotion2", "emotion3", "emotion4", "emotion5", "emotion6", "emotion7" };//"emotion0",
	public static String[] gifFaceCharacter = { "[--看--]", "[--双心--]", "[--闪电--]", "[--音乐--]", "[--心碎--]","[--爱心--]" , "[--飞心--]" };//"[--炸弹--]",
	public static Integer[] gitDrawableID = { R.drawable.section0_emotion1, R.drawable.section0_emotion2, R.drawable.section0_emotion3, R.drawable.section0_emotion4, R.drawable.section0_emotion6,R.drawable.section0_emotion5,  R.drawable.section0_emotion7};//R.drawable.section0_emotion0,
	private static Integer[] gifFaceId = { R.drawable.emotion1, R.drawable.emotion2, R.drawable.emotion3, R.drawable.emotion4, R.drawable.emotion5, R.drawable.emotion6, R.drawable.emotion7 };//R.drawable.emotion0, 
	private static HashMap<String, Integer> mapFace = new HashMap<String, Integer>();

	static {
		for (int i = 0; i < gifFaceCharacter.length; i++) {
			mapFace.put(gifFaceCharacter[i], gifFaceId[i]);
		}
	}

	public static int lookup(String character) {
		return mapFace.get(character);
	}
}
