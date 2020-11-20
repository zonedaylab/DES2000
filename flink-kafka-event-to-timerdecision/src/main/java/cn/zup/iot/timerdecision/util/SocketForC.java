package cn.zup.iot.timerdecision.util;

public class SocketForC {
	    // ~~~~~~~~~~~~~~~~~~~
		// Java和C++处理数字的存储顺序不同，所以接收和发送时数据需要进行字节交换
		// ...................
		public static long C_J_Long(long num)
		{
			long lRet=0;
			lRet =   ((num << 56) & 0xFF00000000000000L) | ((num << 40) & 0x00FF000000000000L)
				    |((num << 24) & 0x0000FF0000000000L) | ((num <<  8) & 0x000000FF00000000L)
				    |((num >>  8) & 0x00000000FF000000L) | ((num >> 24) & 0x0000000000FF0000L)
				    |((num >> 40) & 0x000000000000FF00L) | ((num >> 56) & 0x00000000000000FFL);
			return lRet;
		};

		public static int C_J_Int(int num)
		{
			int iRet=0;
			iRet =	 ((num << 24) & 0xFF000000) | ((num << 8)  & 0x00FF0000)
					|((num >> 8)  & 0x0000FF00) | ((num >> 24) & 0x000000FF);
			return iRet;
		};

		public static short C_J_Short(short num)
		{
			short sRet=0;
			sRet = (short)(((num << 8) & 0xFF00) | ((num >> 8)  & 0x00FF));
			return sRet;
		};


		// ~~~~~~~~~~~~~~~~~~~~
		// 将一个整型值以 C 语言习惯放置到发送缓冲区中 <颠倒次序并以byte方式逐个放置到缓冲区中>
		// ....................
		public static void putIntIntoBuf(byte buf[], int iOff, int iVal)
		{
			buf[iOff+3]	= (byte)((iVal>>24) & 0x000000ff);
			buf[iOff+2]	= (byte)((iVal>>16) & 0x000000ff);
			buf[iOff+1]	= (byte)((iVal>> 8) & 0x000000ff);
			buf[iOff]	= (byte)( iVal		& 0x000000ff);
		};

		// 将一个 short 值以 C 语言习惯放置到发送缓冲区中 <颠倒次序并以byte方式逐个放置到缓冲区中>
		public static void putShortIntoBuf(byte buf[], int iOff, short sVal)
		{
			buf[iOff+1]	= (byte)((sVal>>8)	& 0x00ff);
			buf[iOff]	= (byte)((sVal)		& 0x00ff);
		};

		public static void putByteAsInt(byte buf[], int iOff, byte bVal)
		{
			buf[iOff]	= bVal;
			buf[iOff+1]	= 0;
			buf[iOff+2]	= 0;
			buf[iOff+3]	= 0;
		};
}
