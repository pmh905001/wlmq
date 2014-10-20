package com.pmh.wlmq.https;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.extern.log4j.Log4j;

import org.json.JSONObject;

@Log4j
public class BookDispatcher {

	public static void main(String args[]) throws KeyManagementException, NoSuchAlgorithmException, InterruptedException {
		
		new BookDispatcher().assign();
		
		
	}

	private Config config = Config.getInstance();
	private TrainBook trainBook = null;

	public void assign() throws KeyManagementException, NoSuchAlgorithmException, InterruptedException {

		TrainBook.loginUnderMultipleThread();
		
//		TrainBook.keepSession();

		String startTimeConfig = config.getProperty("thread.start.time");
		String endTimeConfig = config.getProperty("thread.end.time");
		String intervalConfig = config.getProperty("thread.interval");
		String onceThreadNumberConfig = config.getProperty("once.thread.number");

		Date startTime = DateTimeUtils.getTime(startTimeConfig);
		Date endTime = DateTimeUtils.getTime(endTimeConfig);
		int interval = Integer.valueOf(intervalConfig);
		int onceThreadNumber = Integer.valueOf(onceThreadNumberConfig);
		List<Thread> threads = createThreads(startTime, endTime, interval, onceThreadNumber);
		for (Thread thread : threads) {
			thread.start();
		}
		
		for (Thread thread : threads) {
			thread.join();
		}
	}

	

	private List<Thread> createThreads(Date startTime, Date endTime, int interval, int oneceThreadNumber) {

		int times = (int) ((endTime.getTime() - startTime.getTime()) / interval);
		List<Thread> result = new ArrayList<Thread>();

		long time = startTime.getTime();
		for (int i = 0; i < times; i++) {
			time += interval;
			Date targetTime = new Date(time);

			for (int j = 0; j < oneceThreadNumber; j++) {
				BookDispatcher BookDispatcher = this;
				Runnable target = new BookTask(time, BookDispatcher);
				Thread thread = new Thread(target);
				thread.setName(String.format("%s_%s_%s", DateTimeUtils.getFormatedTime(targetTime), i, j));
				result.add(thread);
			}

		}

		return result;

	}

	public void setCurrntFinished(TrainBook trainBook, JSONObject addResult) throws Exception {
		synchronized (this) {
			if (this.trainBook == null) {
				log.info("submit it");
				this.trainBook = trainBook;
				this.trainBook.submitWithEx(addResult);
				System.exit(0);
			}
			else{
				log.info("ignore submit");
			}
			
		}

	}
}
