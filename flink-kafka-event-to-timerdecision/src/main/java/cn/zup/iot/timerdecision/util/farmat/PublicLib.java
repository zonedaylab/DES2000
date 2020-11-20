package cn.zup.iot.timerdecision.util.farmat;

import java.io.IOException;
import java.io.InputStream;

public class PublicLib {
	public static int formatMsecond(int msecond) {
		int retSecond = msecond;
		if (retSecond < 999) {
			retSecond = msecond * 1000000;
		} else if (retSecond > 999 && retSecond < 9999) {
			retSecond = msecond * 100000;
		} else if (retSecond > 9999 && retSecond < 99999) {
			retSecond = msecond * 10000;
		} else if (retSecond > 99999 && retSecond < 999999) {
			retSecond = msecond * 1000;
		} else if (retSecond > 999999 && retSecond < 9999999) {
			retSecond = msecond * 100;
		} else if (retSecond > 9999999 && retSecond < 99999999) {
			retSecond = msecond * 10;
		} else {
			retSecond = msecond;
		}
		return retSecond;
	}

	/***** 辅助函数 ******/
	// 修正Byte[]数组内容，将数组中非占用字节清0，以确保通过Byte[]转成的String变量正确
	public static void emendByte(byte[] by, int cnt) {
		int pos = 0;
		for (int i = 0; i < cnt; i++) { // 从第三个字节开始
			if (by[i] == 0) {
				pos = i;
				break;
			}
		}
		for (int j = pos; j < cnt; j++) {
			by[j] = 0;
		}
	}

	public static int emendByte2(byte[] by, int cnt) {
		int pos = cnt;
		for (int i = 0; i < cnt; i++) { // 从第三个字节开始
			if (by[i] == 0) {
				return i;
			}
		}
		return pos;
	}

	// 获得short型变量函数

	public static short getShort(InputStream inStream) {
		short javaShort;
		try {
			int b1 = inStream.read();
			int b2 = inStream.read();
			javaShort = (short) (b1 + (b2 << 8));
			// javaShort = (short) (inStream.read() + ((inStream.read()) << 8));
		} catch (IOException e) {
			return -1;
		}
		return javaShort;
	}

	// 获得int型变量函数
	public static int getInt(InputStream inStream) {
		try {
			return inStream.read() + ((inStream.read()) << 8)
					+ ((inStream.read()) << 16) + ((inStream.read()) << 24);
		} catch (Exception e) {
			return -1;
		}
	}

	public static int byteToInt(byte[] b) {
		int val = 0;
		for (int i = 0; i < b.length; i++) {
			val += (b[i] << (i * 8));
		}
		return val;
	}

	// 获得long型变量函数
	public static long getLong(InputStream inStream) {
		long javaLong;
		try {
			javaLong = (long) (inStream.read() + ((inStream.read()) << 8)
					+ ((inStream.read()) << 16) + ((inStream.read()) << 24)
					+ ((inStream.read()) << 32) + ((inStream.read()) << 40)
					+ ((inStream.read()) << 48) + ((inStream.read()) << 56));
		} catch (IOException e) {
			return -1;
		}
		return javaLong;
	}

	// EMS-Mend< baizy 2007.7.31 18:02
	// 修改原因:增加控制小数位数的函数
	public static float subFloat(float f, int lenght) {
		float returnf;
		String fStr = String.valueOf(f);

		int i = fStr.indexOf('.');
		if (i > 0 && i < fStr.length() - lenght - 1) {
			String returnStr = fStr.substring(0, i + 1 + lenght);
			returnf = (Float.valueOf(returnStr)).floatValue();
		} else {
			returnf = (Float.valueOf(fStr)).floatValue();
		}
		return returnf;
	}

	// 获取有效字符个数，丢弃非法字符
	public static int getValidLength(String str) {
		// baizytmp
		byte[] CSMing = str.getBytes();
		int ibyte = 0;
		boolean bok = true;
		for (ibyte = 0; ibyte < 16 && bok; ibyte++) {
			if (CSMing[ibyte] >= 0) {
				if (CSMing[ibyte] >= '0' && CSMing[ibyte] <= '9') {
					bok = true;
				} else if (CSMing[ibyte] >= 'a' && CSMing[ibyte] <= 'z') {
					bok = true;
				} else if (CSMing[ibyte] >= 'A' && CSMing[ibyte] <= 'Z') {
					bok = true;
				} else {
					int jbyte = ibyte;
					for (; jbyte < 16; jbyte++) {
						CSMing[jbyte] = 0;
					}
					bok = false;
					break;
				}
			}
		}
		// emendByte(CSMing,16);
		return ibyte;
	}
	// EMS-Mend>
	
	public static int sizeOf(String type){
		int size = 1;
		if("byte".equals(type)){
			size = 1;
		}
		else if("short".equals(type)){
			size = 2;
		}
		else if("int".equals(type)){
			size = 4;
		}
		else if("long".equals(type)){
			size = 8;
		}
		return size;
	}
}
