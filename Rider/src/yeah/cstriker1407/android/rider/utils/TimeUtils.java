package yeah.cstriker1407.android.rider.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.text.format.DateFormat;

public class TimeUtils
{
	/*
	 * ��������ת��Ϊ�ض���ʽ���ַ�����
	 * ע�⣺�����н���������ΪGMTʱ���������Ϳ��Եõ���ȷ��ʱ�䣬�����Ǽ���8Сʱ֮���ʱ�䡣
	 */
	public static String fmtMs2Str(long millisecond, String pattern)
	{
		if (null == pattern)
		{
			return null;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(pattern,Locale.CHINA);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(new Date(millisecond));
	}

	/*
	 * ��ʱ��ת��Ϊ�ض���ʽ���ַ�����
	 * ע�⣺android�����ض��Ĺ����࣬���Լ򻯲�����
	 */
	public static String fmtDate2Str(Date date, String pattern)
	{
		if (null == date || null == pattern)
		{
			return null;
		}
		return DateFormat.format(pattern, date).toString();
		
//		SimpleDateFormat sdf = new SimpleDateFormat(pattern,Locale.CHINA);
//		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//		return sdf.format(date);
	}
	
	/*
	 * ���ض���ʽ���ַ���ת��Ϊʱ�䡣
	 */
	public static Date fmtStr2Date(String pattern, String dateStr)
	{
		if (null == pattern || dateStr == null)
		{
			return null;
		}
		Date result = null;
		try {
			result = new SimpleDateFormat(pattern, Locale.CHINA).parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			result = null;
		}
		return result;
	}
	
	/* ���غ��� */
	public static int msBetweenDates(Date before, Date after)
	{
		if (null == before || null == after)
		{
			return 0;
		}
		return (int)(after.getTime() - before.getTime());
	}
}
