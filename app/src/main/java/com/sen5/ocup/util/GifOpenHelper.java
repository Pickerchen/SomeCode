package com.sen5.ocup.util;

import java.io.InputStream;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sen5.ocup.struct.FaceGif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.TextView;

/**
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :处理.gif动画
 */
public class GifOpenHelper {

	// to store *.gif data, Bitmap & delay
	class GifFrame {
		// to access image & delay w/o interface
		public Bitmap image;
		public int delay;

		public GifFrame(Bitmap im, int del) {
			image = im;
			delay = del;
		}

	}

	// to define some error type
	public static final int STATUS_OK = 0;
	public static final int STATUS_FORMAT_ERROR = 1;
	public static final int STATUS_OPEN_ERROR = 2;

	protected int status;

	protected InputStream in;

	protected int width; // full image width
	protected int height; // full image height
	protected boolean gctFlag; // global color table used
	protected int gctSize; // size of global color table
	protected int loopCount = 1; // iterations; 0 = repeat forever

	protected int[] gct; // global color table
	protected int[] lct; // local color table
	protected int[] act; // active color table

	protected int bgIndex; // background color index
	protected int bgColor; // background color
	protected int lastBgColor; // previous bg color
	protected int pixelAspect; // pixel aspect ratio

	protected boolean lctFlag; // local color table flag
	protected boolean interlace; // interlace flag
	protected int lctSize; // local color table size

	protected int ix, iy, iw, ih; // current image rectangle
	protected int lrx, lry, lrw, lrh;
	protected Bitmap image; // current frame
	protected Bitmap lastImage; // previous frame
	protected int frameindex = 0;

	public int getFrameindex() {
		return frameindex;
	}

	public void setFrameindex(int frameindex) {
		this.frameindex = frameindex;
		if (frameindex > frames.size() - 1) {
			frameindex = 0;
		}
	}

	protected byte[] block = new byte[256]; // current data block
	protected int blockSize = 0; // block size

	// last graphic control extension info
	protected int dispose = 0;
	// 0=no action; 1=leave in place; 2=restore to bg; 3=restore to prev
	protected int lastDispose = 0;
	protected boolean transparency = false; // use transparent color
	protected int delay = 0; // delay in milliseconds
	protected int transIndex; // transparent color index

	protected static final int MaxStackSize = 4096;
	// max decoder pixel stack size
	private static final String TAG = "GifOpenHelper";

	// LZW decoder working arrays
	protected short[] prefix;
	protected byte[] suffix;
	protected byte[] pixelStack;
	protected byte[] pixels;

	protected Vector<GifFrame> frames; // frames read from current file
	protected int frameCount;

	protected boolean mRunning = true;
	private SpannableString spannableString;
	// 正则表达式比配字符串里是否含有表情，如： 我好[开心]啊
	private String zhengze = "\\[[^\\]]+\\]";
	// 通过传入的正则表达式来生成一个pattern
	private Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);

	// private AnimationDrawable mSmile;
	// private byte mFrame ;

	// to get its Width / Height
	public int getWidth() {
		return width;
	}

	public int getHeigh() {
		return height;
	}

	/**
	 * Gets display duration for specified frame.
	 * 
	 * @param n
	 *            int index of frame
	 * @return delay in milliseconds
	 */
	public int getDelay(int n) {
		delay = -1;
		if ((n >= 0) && (n < frameCount)) {
			delay = ((GifFrame) frames.elementAt(n)).delay;
		}
		return delay;
	}

	public int getFrameCount() {
		return frameCount;
	}

	public Bitmap getImage() {
		return getFrame(0);
	}

	public int getLoopCount() {
		return loopCount;
	}

	protected void setPixels() {
		int[] dest = new int[width * height];
		// fill in starting image contents based on last image's dispose code
		if (lastDispose > 0) {
			if (lastDispose == 3) {
				// use image before last
				int n = frameCount - 2;
				if (n > 0) {
					lastImage = getFrame(n - 1);
				} else {
					lastImage = null;
				}
			}
			if (lastImage != null) {
				lastImage.getPixels(dest, 0, width, 0, 0, width, height);
				// copy pixels
				if (lastDispose == 2) {
					// fill last image rect area with background color
					int c = 0;
					if (!transparency) {
						c = lastBgColor;
					}
					for (int i = 0; i < lrh; i++) {
						int n1 = (lry + i) * width + lrx;
						int n2 = n1 + lrw;
						for (int k = n1; k < n2; k++) {
							dest[k] = c;
						}
					}
				}
			}
		}

		// copy each source line to the appropriate place in the destination
		int pass = 1;
		int inc = 8;
		int iline = 0;
		for (int i = 0; i < ih; i++) {
			int line = i;
			if (interlace) {
				if (iline >= ih) {
					pass++;
					switch (pass) {
					case 2:
						iline = 4;
						break;
					case 3:
						iline = 2;
						inc = 4;
						break;
					case 4:
						iline = 1;
						inc = 2;
					}
				}
				line = iline;
				iline += inc;
			}
			line += iy;
			if (line < height) {
				int k = line * width;
				int dx = k + ix; // start of line in dest
				int dlim = dx + iw; // end of dest line
				if ((k + width) < dlim) {
					dlim = k + width; // past dest edge
				}
				int sx = i * iw; // start of line in source
				while (dx < dlim) {
					// map color and insert in destination
					int index = ((int) pixels[sx++]) & 0xff;
					int c = act[index];
					if (c != 0) {
						dest[dx] = c;
					}
					dx++;
				}
			}
		}
		image = Bitmap.createBitmap(dest, width, height, Config.ARGB_8888);
	}

	public Bitmap getFrame(int n) {
		Bitmap im = null;
		if ((n >= 0) && (n < frameCount)) {
			im = ((GifFrame) frames.elementAt(n)).image;
		}
		return im;
	}

	public Bitmap nextBitmap() {
		frameindex++;
		if (frameindex > frames.size() - 1) {
			frameindex = 0;
		}
		return ((GifFrame) frames.elementAt(frameindex)).image;
	}

	public int nextDelay() {
		return ((GifFrame) frames.elementAt(frameindex)).delay;
	}

	// to read & parse all *.gif stream
	public int read(InputStream is) {
		init();
		if (is != null) {
			in = is;

			readHeader();
			if (!err()) {
				readContents();
				if (frameCount < 0) {
					status = STATUS_FORMAT_ERROR;
				}
			}
		} else {
			status = STATUS_OPEN_ERROR;
		}
		try {
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}

	protected void decodeImageData() {
		int NullCode = -1;
		int npix = iw * ih;
		int available, clear, code_mask, code_size, end_of_information, in_code, old_code, bits, code, count, i, datum, data_size, first, top, bi, pi;

		if ((pixels == null) || (pixels.length < npix)) {
			pixels = new byte[npix]; // allocate new pixel array
		}
		if (prefix == null) {
			prefix = new short[MaxStackSize];
		}
		if (suffix == null) {
			suffix = new byte[MaxStackSize];
		}
		if (pixelStack == null) {
			pixelStack = new byte[MaxStackSize + 1];
		}
		// Initialize GIF data stream decoder.
		data_size = read();
		clear = 1 << data_size;
		end_of_information = clear + 1;
		available = clear + 2;
		old_code = NullCode;
		code_size = data_size + 1;
		code_mask = (1 << code_size) - 1;
		for (code = 0; code < clear; code++) {
			prefix[code] = 0;
			suffix[code] = (byte) code;
		}

		// Decode GIF pixel stream.
		datum = bits = count = first = top = pi = bi = 0;
		for (i = 0; i < npix;) {
			if (top == 0) {
				if (bits < code_size) {
					// Load bytes until there are enough bits for a code.
					if (count == 0) {
						// Read a new data block.
						count = readBlock();
						if (count <= 0) {
							break;
						}
						bi = 0;
					}
					datum += (((int) block[bi]) & 0xff) << bits;
					bits += 8;
					bi++;
					count--;
					continue;
				}
				// Get the next code.
				code = datum & code_mask;
				datum >>= code_size;
				bits -= code_size;

				// Interpret the code
				if ((code > available) || (code == end_of_information)) {
					break;
				}
				if (code == clear) {
					// Reset decoder.
					code_size = data_size + 1;
					code_mask = (1 << code_size) - 1;
					available = clear + 2;
					old_code = NullCode;
					continue;
				}
				if (old_code == NullCode) {
					pixelStack[top++] = suffix[code];
					old_code = code;
					first = code;
					continue;
				}
				in_code = code;
				if (code == available) {
					pixelStack[top++] = (byte) first;
					code = old_code;
				}
				while (code > clear) {
					pixelStack[top++] = suffix[code];
					code = prefix[code];
				}
				first = ((int) suffix[code]) & 0xff;
				// Add a new string to the string table,
				if (available >= MaxStackSize) {
					break;
				}
				pixelStack[top++] = (byte) first;
				prefix[available] = (short) old_code;
				suffix[available] = (byte) first;
				available++;
				if (((available & code_mask) == 0) && (available < MaxStackSize)) {
					code_size++;
					code_mask += available;
				}
				old_code = in_code;
			}

			// Pop a pixel off the pixel stack.
			top--;
			pixels[pi++] = pixelStack[top];
			i++;
		}
		for (i = pi; i < npix; i++) {
			pixels[i] = 0; // clear missing pixels
		}
	}

	protected boolean err() {
		return status != STATUS_OK;
	}

	// to initia variable
	public void init() {
		status = STATUS_OK;
		frameCount = 0;
		frames = new Vector<GifFrame>();
		gct = null;
		lct = null;
	}

	protected int read() {
		int curByte = 0;
		try {
			curByte = in.read();
		} catch (Exception e) {
			status = STATUS_FORMAT_ERROR;
		}
		return curByte;
	}

	protected int readBlock() {
		blockSize = read();
		int n = 0;
		if (blockSize > 0) {
			try {
				int count = 0;
				while (n < blockSize) {
					count = in.read(block, n, blockSize - n);
					if (count == -1) {
						break;
					}
					n += count;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (n < blockSize) {
				status = STATUS_FORMAT_ERROR;
			}
		}
		return n;
	}

	// Global Color Table
	protected int[] readColorTable(int ncolors) {
		int nbytes = 3 * ncolors;
		int[] tab = null;
		byte[] c = new byte[nbytes];
		int n = 0;
		try {
			n = in.read(c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (n < nbytes) {
			status = STATUS_FORMAT_ERROR;
		} else {
			tab = new int[256]; // max size to avoid bounds checks
			int i = 0;
			int j = 0;
			while (i < ncolors) {
				int r = ((int) c[j++]) & 0xff;
				int g = ((int) c[j++]) & 0xff;
				int b = ((int) c[j++]) & 0xff;
				tab[i++] = 0xff000000 | (r << 16) | (g << 8) | b;
			}
		}
		return tab;
	}

	// Image Descriptor
	protected void readContents() {
		// read GIF file content blocks
		boolean done = false;
		while (!(done || err())) {
			int code = read();
			switch (code) {
			case 0x2C: // image separator
				readImage();
				break;
			case 0x21: // extension
				code = read();
				switch (code) {
				case 0xf9: // graphics control extension
					readGraphicControlExt();
					break;

				case 0xff: // application extension
					readBlock();
					String app = "";
					for (int i = 0; i < 11; i++) {
						app += (char) block[i];
					}
					if (app.equals("NETSCAPE2.0")) {
						readNetscapeExt();
					} else {
						skip(); // don't care
					}
					break;
				default: // uninteresting extension
					skip();
				}
				break;

			case 0x3b: // terminator
				done = true;
				break;

			case 0x00: // bad byte, but keep going and see what happens
				break;
			default:
				status = STATUS_FORMAT_ERROR;
			}
		}
	}

	protected void readGraphicControlExt() {
		read(); // block size
		int packed = read(); // packed fields
		dispose = (packed & 0x1c) >> 2; // disposal method
		if (dispose == 0) {
			dispose = 1; // elect to keep old image if discretionary
		}
		transparency = (packed & 1) != 0;
		delay = readShort() * 10; // delay in milliseconds
		transIndex = read(); // transparent color index
		read(); // block terminator
	}

	// to get Stream - Head
	protected void readHeader() {
		String id = "";
		for (int i = 0; i < 6; i++) {
			id += (char) read();
		}
		if (!id.startsWith("GIF")) {
			status = STATUS_FORMAT_ERROR;
			return;
		}
		readLSD();
		if (gctFlag && !err()) {
			gct = readColorTable(gctSize);
			bgColor = gct[bgIndex];
		}
	}

	protected void readImage() {
		// offset of X
		ix = readShort(); // (sub)image position & size
		// offset of Y
		iy = readShort();
		// width of bitmap
		iw = readShort();
		// height of bitmap
		ih = readShort();

		// Local Color Table Flag
		int packed = read();
		lctFlag = (packed & 0x80) != 0; // 1 - local color table flag

		// Interlace Flag, to array with interwoven if ENABLE, with order
		// otherwise
		interlace = (packed & 0x40) != 0; // 2 - interlace flag
		// 3 - sort flag
		// 4-5 - reserved
		lctSize = 2 << (packed & 7); // 6-8 - local color table size
		if (lctFlag) {
			lct = readColorTable(lctSize); // read table
			act = lct; // make local table active
		} else {
			act = gct; // make global table active
			if (bgIndex == transIndex) {
				bgColor = 0;
			}
		}
		int save = 0;
		if (transparency) {
			save = act[transIndex];
			act[transIndex] = 0; // set transparent color if specified
		}
		if (act == null) {
			status = STATUS_FORMAT_ERROR; // no color table defined
		}
		if (err()) {
			return;
		}
		decodeImageData(); // decode pixel data
		skip();
		if (err()) {
			return;
		}
		frameCount++;
		// create new image to receive frame data
		image = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		// createImage(width, height);
		setPixels(); // transfer pixel data to image
		frames.addElement(new GifFrame(image, delay)); // add image to frame
		// list
		if (transparency) {
			act[transIndex] = save;
		}
		resetFrame();
	}

	// Logical Screen Descriptor
	protected void readLSD() {
		// logical screen size
		width = readShort();
		height = readShort();
		// packed fields
		int packed = read();
		gctFlag = (packed & 0x80) != 0; // 1 : global color table flag
		// 2-4 : color resolution
		// 5 : gct sort flag
		gctSize = 2 << (packed & 7); // 6-8 : gct size
		bgIndex = read(); // background color index
		pixelAspect = read(); // pixel aspect ratio
	}

	protected void readNetscapeExt() {
		do {
			readBlock();
			if (block[0] == 1) {
				// loop count sub-block
				int b1 = ((int) block[1]) & 0xff;
				int b2 = ((int) block[2]) & 0xff;
				loopCount = (b2 << 8) | b1;
			}
		} while ((blockSize > 0) && !err());
	}

	// read 8 bit data
	protected int readShort() {
		// read 16-bit value, LSB first
		return read() | (read() << 8);
	}

	protected void resetFrame() {
		lastDispose = dispose;
		lrx = ix;
		lry = iy;
		lrw = iw;
		lrh = ih;
		lastImage = image;
		lastBgColor = bgColor;
		dispose = 0;
		transparency = false;
		delay = 0;
		lct = null;
	}

	/**
	 * Skips variable length blocks up to and including next zero length block.
	 */
	protected void skip() {
		do {
			readBlock();
		} while ((blockSize > 0) && !err());
	}

	public void startGif() {
		mRunning = true;
	}

	public void stopGif() {
		mRunning = false;
	}

	/**
	 * 得到一个SpanableString对象，通过传入的字符串,并进行正则判断
	 * 
	 * @param context
	 * @param str
	 * @return
	 */
	public SpannableString getExpressionString(Context context, TextView textview, String str) {
			spannableString = new SpannableString(str);
//			 //正则表达式比配字符串里是否含有表情，如： 我好[开心]啊
//			String zhengze = "\\[[^\\]]+\\]";
//			// 通过传入的正则表达式来生成一个pattern
//			Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
			try {
				dealExpression(context, textview, spannableString, sinaPatten, 0);
			} catch (Exception e) {
				Log.e("dealExpression", e.getMessage());
			}
			return spannableString;
	}

	/**
	 * 对spanableString进行正则判断，如果符合要求，则以表情图片代替
	 * 
	 * @param context
	 * @param spannableString
	 * @param patten
	 * @param start
	 * @throws Exception
	 */
	private ImageSpan imageSpan;
	private InputStream is;
	private Options opts;
	private Bitmap btp;
	private String value;
	private String key;
	private GifOpenHelper gHelper;
	private BitmapDrawable bd;
	private AnimationDrawable smile;

	private void dealExpression(Context context, final TextView textview, SpannableString spannableString, Pattern patten, int start) throws Exception {
		Matcher matcher = patten.matcher(spannableString);
		while (matcher.find()) {
			key = matcher.group();
			Log.d(TAG, "dealExpression)------  key=" + key);
			// 返回第一个字符的索引的文本匹配整个正则表达式,ture 则继续递归
			if (matcher.start() < start) {
				continue;
			}
			value = FaceConversionUtil.getInstace().emojiMap.get(key);
			if (TextUtils.isEmpty(value)) {
				continue;
			}

			if (key.startsWith("[--")) {
				smile = new AnimationDrawable();
				Log.d(TAG, "dealExpression)-----------matcher.find()  dynamic face");
				gHelper = new GifOpenHelper();
				gHelper.read(context.getResources().openRawResource(FaceGif.lookup(key)));
				// 得到第一张图片
				bd = new BitmapDrawable(gHelper.getImage());
				smile.addFrame(bd, gHelper.getDelay(0));
				for (int i = 1; i < gHelper.getFrameCount(); i++) {
					smile.addFrame(new BitmapDrawable(gHelper.nextBitmap()), gHelper.getDelay(i));
				}

				smile.setBounds(0, 0, Tools.dip2px(context, 40), Tools.dip2px(context, 40));
				smile.setOneShot(false);

				bd.setBounds(0, 0, Tools.dip2px(context, 40), Tools.dip2px(context, 40));

				// 要让图片替代指定的文字就要用ImageSpan
				ImageSpan span = new ImageSpan(smile, ImageSpan.ALIGN_BASELINE);
				// 计算该图片名字的长度，也就是要替换的字符串的长度
				int end = matcher.start() + key.length();
				spannableString.setSpan(span, matcher.start(), end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				smile.invalidateSelf();

				new Thread(new Runnable() {
					byte frame = 0;

					@Override
					public void run() {
						while (mRunning) {
							smile.selectDrawable(frame++);
							if (frame == smile.getNumberOfFrames()) {
								frame = 0;
							}
							textview.postInvalidate();
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();
				if (end < spannableString.length()) {
					// 如果整个字符串还未验证完，则继续。。
					dealExpression(context, textview, spannableString, patten, end);
				}
			} else {
				Log.d(TAG, "setGif2TextView)-----------matcher.find()  static face");
				int resId = context.getResources().getIdentifier(value, "drawable", context.getPackageName());
				// int resId = R.raw.ic_fish;
				// 通过上面匹配得到的字符串来生成图片资源id
				if (resId != 0) {
					is = context.getResources().openRawResource(resId);
					// 2.为位图设置100K的缓存
					opts = new BitmapFactory.Options();
					opts.inTempStorage = new byte[100 * 1024];
					// 3.设置位图颜色显示优化方式
					// ALPHA_8：每个像素占用1byte内存（8位）
					// ARGB_4444:每个像素占用2byte内存（16位）
					// ARGB_8888:每个像素占用4byte内存（32位）
					// RGB_565:每个像素占用2byte内存（16位）
					// Android默认的颜色模式为ARGB_8888，这个颜色模式色彩最细腻，显示质量最高。但同样的，占用的内存//也最大。也就意味着一个像素点占用4个字节的内存。我们来做一个简单的计算题：3200*2400*4
					// bytes //=30M。如此惊人的数字！哪怕生命周期超不过10s，Android也不会答应的。
					opts.inPreferredConfig = Bitmap.Config.RGB_565;
					// 4.设置图片可以被回收，创建Bitmap用于存储Pixel的内存空间在系统内存不足时可以被回收
					opts.inPurgeable = true;
					// 5.设置位图缩放比例
					// width，hight设为原来的四分一（该参数请使用2的整数倍）,这也减小了位图占用的内存大小；例如，一张//分辨率为2048*1536px的图像使用inSampleSize值为4的设置来解码，产生的Bitmap大小约为//512*384px。相较于完整图片占用12M的内存，这种方式只需0.75M内存(假设Bitmap配置为//ARGB_8888)。
					// opts.inSampleSize = 2;
					// 6.设置解码位图的尺寸信息
					opts.inInputShareable = true;
					// 7.解码位图
					btp = BitmapFactory.decodeStream(is, null, opts);
					is.close();
					// Bitmap bitmap =
					// BitmapFactory.decodeResource(context.getResources(),
					// resId);
					// bitmap = Bitmap.createScaledBitmap(bitmap,
					// Tools.dip2px(context, 40), Tools.dip2px(context, 40),
					// true);
					// 通过图片资源id来得到bitmap，用一个ImageSpan来包装

					imageSpan = new ImageSpan(context, btp);
					// 计算该图片名字的长度，也就是要替换的字符串的长度
					int end = matcher.start() + key.length();
					// 将该图片替换字符串中规定的位置中
					spannableString.setSpan(imageSpan, matcher.start(), end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

					Log.d(TAG, "dealExpression      end==  " + end + "  spannableString.length()==" + spannableString.length());
					if (end < spannableString.length()) {
						// 如果整个字符串还未验证完，则继续。。
						dealExpression(context, textview, spannableString, patten, end);
					}
				}
			}
			break;
		}
	}

}
