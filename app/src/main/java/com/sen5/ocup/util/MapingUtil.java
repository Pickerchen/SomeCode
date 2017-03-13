package com.sen5.ocup.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;


/**
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :杯子显示屏点正对应表
 */
public class MapingUtil {

	private static final String TAG = "MapingUtil";
	/**
	 * 创建MapingUtil的单例
	 */
	private static MapingUtil mInstance = null;

	public HashMap<Character, byte[]> map_library = new HashMap<Character, byte[]>();
	public HashMap<String, byte[]> map_Face = new HashMap<String, byte[]>();

	public static MapingUtil getInstance() {
		if (mInstance == null) {
			mInstance = new MapingUtil();
		}
		return mInstance;
	}

	public MapingUtil() {
		super();
		map_library.put('0', DataSwitch.hexStringToBytes("3c42423c00"));
		map_library.put('1', DataSwitch.hexStringToBytes("227e0200"));
		map_library.put('2', DataSwitch.hexStringToBytes("264a522200"));
		map_library.put('3', DataSwitch.hexStringToBytes("2442522c00"));
		map_library.put('4', DataSwitch.hexStringToBytes("0c14247e0400"));
		map_library.put('5', DataSwitch.hexStringToBytes("7252524C00"));
		map_library.put('6', DataSwitch.hexStringToBytes("3C52522C00"));
		map_library.put('7', DataSwitch.hexStringToBytes("40404E7000"));
		map_library.put('8', DataSwitch.hexStringToBytes("2C52522C00"));
		map_library.put('9', DataSwitch.hexStringToBytes("324A4A3C00"));

		map_library.put('A', DataSwitch.hexStringToBytes("3E483E00"));
		map_library.put('B', DataSwitch.hexStringToBytes("7E522C00"));
		map_library.put('C', DataSwitch.hexStringToBytes("3C424200"));
		map_library.put('D', DataSwitch.hexStringToBytes("7E423C00"));
		map_library.put('E', DataSwitch.hexStringToBytes("7E4A4A00"));
		map_library.put('F', DataSwitch.hexStringToBytes("7E484800"));
		map_library.put('G', DataSwitch.hexStringToBytes("3C424E00"));
		map_library.put('H', DataSwitch.hexStringToBytes("7E087E00"));
		map_library.put('I', DataSwitch.hexStringToBytes("427E4200"));
		map_library.put('J', DataSwitch.hexStringToBytes("427E4000"));

		map_library.put('K', DataSwitch.hexStringToBytes("7E08186600"));
		map_library.put('L', DataSwitch.hexStringToBytes("7E020200"));
		map_library.put('M', DataSwitch.hexStringToBytes("7E201C207E00"));
		map_library.put('N', DataSwitch.hexStringToBytes("7E300C7E00"));
		map_library.put('O', DataSwitch.hexStringToBytes("3C423C00"));
		map_library.put('P', DataSwitch.hexStringToBytes("7E483000"));
		map_library.put('Q', DataSwitch.hexStringToBytes("3C423E0200"));
		map_library.put('R', DataSwitch.hexStringToBytes("7E484C3200"));
		map_library.put('S', DataSwitch.hexStringToBytes("32524C00"));
		map_library.put('T', DataSwitch.hexStringToBytes("407E4000"));

		map_library.put('U', DataSwitch.hexStringToBytes("7C02027C00"));
		map_library.put('V', DataSwitch.hexStringToBytes("78067800"));
		map_library.put('W', DataSwitch.hexStringToBytes("7E0438047E00"));
		map_library.put('X', DataSwitch.hexStringToBytes("6618186600"));
		map_library.put('Y', DataSwitch.hexStringToBytes("601E6000"));
		map_library.put('Z', DataSwitch.hexStringToBytes("464A7200"));

		map_library.put('-', DataSwitch.hexStringToBytes("101010"));
		map_library.put('>', DataSwitch.hexStringToBytes("442810"));
		map_library.put('<', DataSwitch.hexStringToBytes("102844"));
//		map_library.put('\'', DataSwitch.hexStringToBytes("5C2222"));
		map_library.put(':', DataSwitch.hexStringToBytes("28"));
		map_library.put(' ', DataSwitch.hexStringToBytes("00"));
		map_library.put('.', DataSwitch.hexStringToBytes("02"));
		
		map_Face.put("[微笑]",DataSwitch.hexStringToBytes("004084824101014182844000"));
		map_Face.put("[哭]",DataSwitch.hexStringToBytes("0080f1820404040482f18000"));
		map_Face.put("[沉默]",DataSwitch.hexStringToBytes("00e2a2e20202e2a2e200"));
		map_Face.put("[高兴]",DataSwitch.hexStringToBytes("004cea49090949ea4c00"));
		map_Face.put("[亲吻]",DataSwitch.hexStringToBytes("008080808a15151180808000"));
		map_Face.put("[玩笑]",DataSwitch.hexStringToBytes("00a048a80f09af48a800"));
		map_Face.put("[囧]",DataSwitch.hexStringToBytes("007e91ef8bef917e00"));
		map_Face.put("[猪鼻]",DataSwitch.hexStringToBytes("003c42bdbd8181bdbd423c00"));
		map_Face.put("[嘴巴]",DataSwitch.hexStringToBytes("001038545232325254381000"));
		map_Face.put("[太阳]",DataSwitch.hexStringToBytes("00082a1c771c2a0800"));
		map_Face.put("[云]",DataSwitch.hexStringToBytes("000c121222424a52320c00"));
		map_Face.put("[下雨]",DataSwitch.hexStringToBytes("001824264586b5a6651800"));
		map_Face.put("[吉普]",DataSwitch.hexStringToBytes("0018282c2aca8ec8a4c3800"));
		map_Face.put("[汽车]",DataSwitch.hexStringToBytes("000818181c2a4c48484c3a1c181800"));
		map_Face.put("[便便]",DataSwitch.hexStringToBytes("0001061a6aea1a060100"));
		map_Face.put("[信封]",DataSwitch.hexStringToBytes("007ec1a191898991a1c17e00"));
		map_Face.put("[飞机]",DataSwitch.hexStringToBytes("001010103838345290103000"));
		map_Face.put("[船]",DataSwitch.hexStringToBytes("00080c0a090909f9e9e90a0c00"));
		map_Face.put("[小男孩]",DataSwitch.hexStringToBytes("007cc2c9e1e9e27c00"));
		map_Face.put("[小女孩]",DataSwitch.hexStringToBytes("0078e0c07c42e9e9f9727cc0e07800"));
		map_Face.put("[鱼]",DataSwitch.hexStringToBytes("0018245562dec242422418247e00"));
		map_Face.put("[兔子]",DataSwitch.hexStringToBytes("000c12e1f111e1e1120c00"));
		map_Face.put("[家]",DataSwitch.hexStringToBytes("00081f316fc96f311f0800"));
		map_Face.put("[叹号]",DataSwitch.hexStringToBytes("00fbfb00"));
		map_Face.put("[问号]",DataSwitch.hexStringToBytes("0060e0cbfb6000"));
		map_Face.put("[方向键左]",DataSwitch.hexStringToBytes("0010387cfe3838383800"));
		map_Face.put("[方向键右]",DataSwitch.hexStringToBytes("001c1c1c1c7f3e1c0800"));
		map_Face.put("[方向键上]",DataSwitch.hexStringToBytes("0010307fff7f301000"));
		map_Face.put("[方向键下]",DataSwitch.hexStringToBytes("00080cfefffe0c0800"));


		map_Face.put("[--飞心--]",
				DataSwitch
				.hexStringToBytes("1050884830387c7e3f7e7c38304888501000002c5448307c7e3f7e7c383048885010000000000e112224387c7e3f7e7c382422110e"));
		map_Face.put("[--看--]",
				DataSwitch
				.hexStringToBytes("000000387c7c44380000387c7c443800000000000038447c7c38000038447c7c38000000000000387c7c44380000387c7c443800000000000038447c7c38000038447c7c38000000"));
		//000000387c7c44380000387c7c443800000000000038447c7c38000038447c7c38000000
		map_Face.put("[--闪电--]",
				DataSwitch
				.hexStringToBytes("00000000000000206080000000000000000000000000000010307898100000000000000000000000000011327c981000000000000000"));
		map_Face.put("[--音乐--]",
				DataSwitch
				.hexStringToBytes("000000000003077e6060666e7c00000000000000000000060efcc0c0ccdcf80000000000000000000003077e6060666e7c00000000000000000000060efcc0c0ccdcf80000000000"));
		//000000000003077e6060666e7c00000000000000000000060efcc0c0ccdcf80000000000
		map_Face.put("[--爱心--]",
				DataSwitch
				.hexStringToBytes("0000000000387c7e3f7e7c38000000000000000000000000183c1e3c18000000000000000000000000387c7e3f7e7c38000000000000000000000000183c1e3c1800000000000000"));
		//0000000000387c7e3f7e7c38000000000000000000000000183c1e3c1800000000000000
		map_Face.put("[--心碎--]",
				DataSwitch
				.hexStringToBytes("0000000000387c7e3f7e7c380000000000000000000000387c7e2700387f7e38000000000000000000387c7e3f7e7c380000000000000000000000387c7e2700387f7e3800000000"));
		//0000000000387c7e3f7e7c380000000000000000000000387c7e2700387f7e3800000000
		map_Face.put("[--双心--]",
				DataSwitch
				.hexStringToBytes("000000183c1e3c180000183c1e3c1800000000387c7e3f7f7c380000387c7e3f7f7c3800000000183c1e3c180000183c1e3c1800000000387c7e3f7f7c380000387c7e3f7f7c3800"));
		//000000183c1e3c180000183c1e3c1800000000387c7e3f7f7c380000387c7e3f7f7c3800
	}
	
	
	public ArrayList<FaceBytes> getBytesFace(String str){
		String upperText = str.toUpperCase();
		String zhengze = "\\[[^\\]]+\\]";
		// 通过传入的正则表达式来生成一个pattern
		Pattern patten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
		Matcher matcher = patten.matcher(upperText);
		ArrayList<FaceBytes> list_facebytes = new ArrayList<FaceBytes>();
		while (matcher.find()) {
			String key = matcher.group();
			Log.d(TAG, "getBytesFace)------  key=" + key);
			int start = matcher.start();
			int end = start+key.length();
			Log.d(TAG, "getBytesFace)------  start=" + start+"  end=="+end);
			byte[] bs = map_Face.get(key);
			FaceBytes mFaceBytes = new FaceBytes(start, end, bs);
			list_facebytes.add(mFaceBytes);
		}
		return list_facebytes;
	}
}
