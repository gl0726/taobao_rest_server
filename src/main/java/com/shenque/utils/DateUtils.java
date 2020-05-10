package com.shenque.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * xiao.py
 */
public class DateUtils {
	private String FORMAT_DT = "yyyy-MM-dd HH:mm:ss";
	private String FORMAT_T = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	private String YMD = "yyyy-MM-dd";

	private String year = "yyyy";

	private String month = "MM";

	private String day = "dd";

	private String hour = "HH";

	private String minute = "mm";

	private String second = "ss";

	public  SimpleDateFormat yearSDF = new SimpleDateFormat(year);

	public  SimpleDateFormat monthSDF = new SimpleDateFormat(month);

	public  SimpleDateFormat daySDF = new SimpleDateFormat(day);

	public  SimpleDateFormat hourSDF = new SimpleDateFormat(hour);

	public  SimpleDateFormat minuteSDF = new SimpleDateFormat(minute);

	public  SimpleDateFormat secondSDF = new SimpleDateFormat(second);
	/**
	 * 当前时间转换为yyyy-MM-dd HH:mm:ss
	 * @return 返回日期格式为yyyy-MM-dd HH:mm:ss字符串
	 */
	public  String formatDatetime(){
		return formatDatetime(new Date(),FORMAT_DT);
	}

	public  String formatDatetime(Date date){
		return formatDatetime(new Date(),FORMAT_DT);
	}
	/**
	 * 当前时间根据传递日期格式进行转换
	 * @param format  转换为格式
	 * @return 传递日期格式的字符串
	 */
	public  String formatDatetime(String format) {
		return formatDatetime(new Date(), format);
	}

	/**
	 * @param date  日期参数
	 * @return 返回日期格式为yyyy-MM-dd HH:mm:ss字符串
	 */


	/**
	 * 格式化日期
	 * @param date 日期
	 * @param format  转换格式
	 * @return String类型
	 */
	public  String formatDatetime(Date date, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}
	
	/**
	 * 毫秒转日期
	 * @param millis  long毫秒日期
	 * @return  返回Date日期
	 */
	public  Date millisToDate(long millis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar.getTime();
	}


	/**
	 *
	 * @param data  yyyy-MM-dd'T'HH:mm:ss.SSSZ这种格式的日期
	 * @return  返回Date类型
	 * @throws ParseException
	 */
	public  Date stringToDate(String data) throws ParseException {
		data = data.toUpperCase().replace("Z","");
		SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_T);
		Date date = formatter.parse(data);
		return date;
	}

	/**
	 *
	 * @param startDate  开始时间
	 * @param endDate 结束时间
	 * @return 返回秒，因为除以1000
	 */
	public long getIntervalDays(Date startDate, Date endDate) {
	       long intervalMilli = (long)endDate.getTime() - startDate.getTime();
	       // (intervalMilli / (24 * 60 * 60 * 1000));
	       return (long) (intervalMilli /1000 );

	}

	/**
	 * 获取小时
	 * @param data  字符串日期
	 * @param format  日期格式
	 * @return 返回小时
	 * @throws ParseException
	 */
	public String getHour(String data,String format) throws ParseException {
		Date date  = new SimpleDateFormat(format).parse(data);
		return new SimpleDateFormat("HH").format(date);

	}


	/**
	 * 根据传递的日期计算此日期往后推迟一小时的日期
	 * @param date  日期
	 * @param formats  日期格式
	 * @param tastExecuteTime  根据日期格式返回对应的日期字符串
	 * @return
	 */
	public String getOneHoursAgoTime (Date date,String formats,int tastExecuteTime) {
		Calendar cal = Calendar. getInstance ();
		cal.setTime(date);
		cal.add(Calendar.HOUR , - tastExecuteTime) ; //把时间设置为当前时间-1小时，同理，也可以设置其他时间
		String oneHoursAgoTime =  new  SimpleDateFormat(formats).format(cal.getTime());//获取到完整的时间
		return  oneHoursAgoTime;
	}

	/**
	 * 根据传递的日期计算此日期往后推迟7天的日期（但是是凌晨日期  yyyy-MM-dd 00:00:00）
	 * @param date 日期格式
	 * @param tastExecuteTime  往后推迟的天数
	 * @return  整点日期  yyyy-MM-dd 00:00:00
	 */

	public String getDayAgoTime (Date date,int tastExecuteTime) {
		Calendar cal = Calendar.getInstance ();
		cal.setTime(date);
		cal.add(Calendar.DATE , - 7) ; //把日期设置为当前时间-7天
		String dayAgoTime =  new  SimpleDateFormat(YMD).format(cal.getTime());//获取到完整的时间
		return  dayAgoTime + " 00:00:00";
	}


	/**
	 * 根据传递的日期计算此日期增加多少天的日期
	 * @param date 日期
	 * @param num  增加的天数
	 * @return 返回日期(但是不是整点的，而是根据你传递的日期纯粹的加上一天)
	 */
	public Date addDay(Date date, int num) {
		Calendar startDT = Calendar.getInstance();
		startDT.setTime(date);
		startDT.add(Calendar.DAY_OF_MONTH, num);
		return startDT.getTime();
	}

	/**
	 * 获取当前日期 + 添加的想要的时间,比如：17:00:00 ,从而获取对应日期的的毫秒数
	 * @param time "HH:mm:ss" 这是传递的时分秒的格式，比如传递传递时间为：17:00:00
	 * @return
	 */
	public long getTimeMillis(String time)  throws ParseException{
		DateFormat dateFormat = new SimpleDateFormat(FORMAT_DT);
		DateFormat dayFormat = new SimpleDateFormat(YMD);
		Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
		return curDate.getTime();
	}


	/**
	 * 获取当前日期的年、月、日
	 *
	 * @return String
	 */
	public String getCurrentYear() {
		return this.yearSDF.format(new Date());
	}

	public  String getCurrentMonth() {
		return this.monthSDF.format(new Date());
	}

	public String getCurrentDay() {
		return this.daySDF.format(new Date());
	}

	/**
	 * 获取当前的时、分、秒
	 *
	 * @return
	 */
	public String getCurrentHour() {
		return this.hourSDF.format(new Date());
	}

	public String getCurrentMinute() {
		return this.minuteSDF.format(new Date());
	}

	public String getCurrentSecond() {
		return this.secondSDF.format(new Date());
	}

	/**
	 * 获得今天零点
	 *
	 * @return Date
	 */
	public Date getTodayZeroHour() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR, 0);
		return cal.getTime();
	}


	/**
	 * 获得指定日期所在的自然周的第一天，即周日
	 *
	 * @param date
	 *            日期
	 * @return 自然周的第一天
	 */
	public Date getStartDayOfWeek(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_WEEK, 1);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期所在的自然周的最后一天，即周六
	 *
	 * @param date
	 * @return
	 */
	public Date getLastDayOfWeek(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_WEEK, 7);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期所在当月第一天
	 *
	 * @param date
	 * @return
	 */
	public Date getStartDayOfMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, 1);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期所在当月最后一天
	 *
	 * @param date
	 * @return
	 */
	public Date getLastDayOfMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DATE, 1);
		c.add(Calendar.MONTH, 1);
		c.add(Calendar.DATE, -1);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期的下一个月的第一天
	 *
	 * @param date
	 * @return
	 */
	public Date getStartDayOfNextMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, 1);
		c.set(Calendar.DAY_OF_MONTH, 1);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期的下一个月的最后一天
	 *
	 * @param date
	 * @return
	 */
	public Date getLastDayOfNextMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DATE, 1);
		c.add(Calendar.MONTH, 2);
		c.add(Calendar.DATE, -1);
		date = c.getTime();
		return date;
	}


	/**
	 * 把String 日期转换成long型日期；---OK
	 * @param date
	 *            String 型日期；
	 * @param format
	 *            日期格式；
	 * @return
	 */
	public long stringToLong(String date, String format) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date dt2 = null;
		long lTime = 0;
		dt2 = sdf.parse(date);
		// 继续转换得到秒数的long型
		lTime = dt2.getTime();
		return lTime;
	}

	public String getBeforeDay(){
		Date dNow = new Date();   //当前时间
		Calendar calendar = Calendar.getInstance();  //得到日历
		calendar.setTime(dNow);//把当前时间赋给日历
		calendar.add(Calendar.DAY_OF_MONTH, -1);  //设置为前一天
		Date dBefore = calendar.getTime();   //得到前一天的时间
		return  formatDatetime(dBefore,"yyyyMMdd");
	}

	public long getSchule(String time){
		try {
			DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
			DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
			Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
			return curDate.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void main(String[] args) {
		String h = "2018-03-06T02:50:39.941Z";
		try {
			System.out.println(new DateUtils().getBeforeDay());
			//System.out.println(new DateUtils().getIntervalDays(d,new Date()));

			//System.out.println(new DateUtils().getDayAgoTime(new Date(),7));
			//System.out.println(getTodayZeroHour());
			//System.out.println(getStartDayOfMonth(new Date()));

			System.out.println(new DateUtils().addDay(new Date(),-1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
