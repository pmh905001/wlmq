package com.pmh.wlmq.https;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtils {
	public static String getFormatedDate(Date date, int delayDays) {
		Calendar instance = Calendar.getInstance();
		instance.setTime(date);
		instance.add(Calendar.DATE, delayDays);

		String formatedDate = new SimpleDateFormat("yyyy-MM-dd").format(instance.getTime());
		return formatedDate;
	}

	public static String getFormatedTime(Date date) {
		String formatedDate = new SimpleDateFormat("HH:mm:ss").format(date);
		return formatedDate;
	}

	public static String getFormatedDate(Date date) {
		return getFormatedDate(date, 0);
	}

	public static String getFormatedDate() {
		return getFormatedDate(new Date());
	}
	
	public static String getFormatedDateTime() {
		Calendar instance = Calendar.getInstance();
		instance.setTime(new Date());

		String formatedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(instance.getTime());
		return formatedDate;
	}


}