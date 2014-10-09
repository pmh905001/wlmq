package com.pmh.wlmq.https;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lombok.extern.log4j.Log4j;

import org.apache.http.client.HttpClient;
import org.json.JSONObject;

import com.pmh.wlmq.https.sender.IRequestSender;
import com.pmh.wlmq.https.sender.RequestSender;
import com.pmh.wlmq.https.sender.RequestSenderWithRetry;

@Log4j
public class TrainBookFromConfig implements Runnable {

	private DailyRecord dailyRecord = null;

	private static IRequestSender requestSender = new RequestSenderWithRetry(new RequestSender());

	public TrainBookFromConfig(DailyRecord dailyRecord) {
		this.dailyRecord = dailyRecord;
	}

	public static void main(String[] args) throws Exception {
		List<DailyRecord> dailyRecords = ExcelUtils.read();
		List<Thread> threadList = new ArrayList<Thread>();
		for (DailyRecord dailyRecord : dailyRecords) {
			Thread thread = new Thread(new TrainBookFromConfig(dailyRecord));
			threadList.add(thread);
			thread.start();
		}

		for (Thread thread : threadList) {
			thread.join();
		}

	}

	public static void bookWithEx(DailyRecord dailyRecord) throws Exception {
		HttpClient httpClient = HttpsUtils.generateHttpsClient();

		requestSender.login(httpClient);

		String loadDateStr = dailyRecord.getLoadDateStr();
		String keyMatched = dailyRecord.getKeyMatched();

		JSONObject keyWordSearchResult = requestSender.keyWordSearch(httpClient, loadDateStr, keyMatched);

		JSONObject fillResult = requestSender.postFillPage(httpClient, keyWordSearchResult);

		waitTo7Clock();

		Config instance = Config.getInstance();
		String trainNumber = instance.getProperty("train.number");
		String totalWeight = String.valueOf(Integer.valueOf(trainNumber) * 60);
		String location = instance.getProperty("location");
		String canReceiveMsg = instance.getProperty("can.receive.msg");
		String mobile = instance.getProperty("mobile");
		String uuid = "";
		JSONObject addResult = requestSender.add(httpClient, keyWordSearchResult, loadDateStr, trainNumber, totalWeight, location, canReceiveMsg, mobile, uuid,
				fillResult);

		JSONObject submitResult = requestSender.submit(httpClient, addResult);
		ExcelUtils.markFinished(dailyRecord, submitResult);
	}

	private static void waitTo7Clock() throws InterruptedException {

		String startTimeAfterLogin = Config.getInstance().getProperty("start.time.after.login");
		String[] hourMinutesSecods = startTimeAfterLogin.split(":");

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hourMinutesSecods[0]));
		calendar.set(Calendar.MINUTE, Integer.valueOf(hourMinutesSecods[1]));
		calendar.set(Calendar.SECOND, Integer.valueOf(hourMinutesSecods[2]));
		long targetTime = calendar.getTime().getTime();
		while (System.currentTimeMillis() < targetTime) {
			Thread.sleep(50);
		}

	}

	public void run() {
		try {
			bookWithEx(this.dailyRecord);
		} catch (Exception e) {

			log.error("book train error", e);
		}
	}

}