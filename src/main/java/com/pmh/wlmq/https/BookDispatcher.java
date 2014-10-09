package com.pmh.wlmq.https;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

public class BookDispatcher {

	public static void main(String args[]) throws KeyManagementException, NoSuchAlgorithmException, InterruptedException {
		new BookDispatcher().assign();
	}

	private Config config = Config.getInstance();
	private TrainBook trainBook = null;

	public void assign() throws KeyManagementException, NoSuchAlgorithmException, InterruptedException {
		
		TrainBook.loginStepWithMultipleThread();

		String startTimeConfig = config.getProperty("thread.start.time");
		String endTimeConfig = config.getProperty("thread.end.time");
		String intervalConfig = config.getProperty("thread.interval");
		String onceThreadNumberConfig = config.getProperty("once.thread.number");

		Date startTime = getTime(startTimeConfig);
		Date endTime = getTime(endTimeConfig);
		int interval = Integer.valueOf(intervalConfig);
		int onceThreadNumber = Integer.valueOf(onceThreadNumberConfig);
		List<Thread> threads = createThreads(startTime, endTime, interval, onceThreadNumber);
		for (Thread thread : threads) {
			thread.start();
			thread.join();
		}
		

	}

	private Date getTime(String timeConfig) {

		Calendar calendar = Calendar.getInstance();
		String[] arr = timeConfig.split(":");

		int hourOfDay = Integer.valueOf(arr[0]);
		int minute = Integer.valueOf(arr[1]);
		int second = Integer.valueOf(arr[2]);
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);

		return calendar.getTime();
	}

	private List<Thread> createThreads(Date startTime, Date endTime, int interval, int oneceThreadNumber) {

		int times = (int) ((endTime.getTime() - startTime.getTime()) / interval);
		List<Thread> result = new ArrayList<Thread>();

		long time = startTime.getTime();
		for (int i = 0; i < times; i++) {
			time += interval;
			Date targetTime = new Date(time);

			for (int j = 0; j < oneceThreadNumber; j++) {
				BookDispatcher BookDispatcher=this;
				Runnable target = new BookTask(time,BookDispatcher);
				Thread thread = new Thread(target);
				thread.setName(String.format("%s_%s_%s", DateTimeUtils.getFormatedTime(targetTime), i, j));
				result.add(thread);
			}

		}

		return result;

	}

	public void setCurrntFinished(TrainBook trainBook, JSONObject addResult) throws InterruptedException {
		synchronized (this) {
			if (this.trainBook == null) {
				this.trainBook = trainBook;
				this.trainBook.submitWithEx(addResult);
			}

		}

	}
}
