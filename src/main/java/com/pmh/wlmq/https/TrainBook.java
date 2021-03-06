package com.pmh.wlmq.https;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import lombok.extern.log4j.Log4j;

import org.apache.http.client.HttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONObject;

import com.pmh.wlmq.https.sender.IRequestSender;
import com.pmh.wlmq.https.sender.RequestSender;
import com.pmh.wlmq.https.sender.RequestSenderWithRetry;

@Log4j
public class TrainBook {

	private static HttpClient httpClient;

	private long timeToAdd;

	private BookDispatcher bookDispatcher;

	private IRequestSender requestSender = new RequestSenderWithRetry(new RequestSender());

	public TrainBook(long time, BookDispatcher bookDispatcher) throws KeyManagementException, NoSuchAlgorithmException {
		this.timeToAdd = time;
		this.bookDispatcher = bookDispatcher;
	}

	public void book() throws Exception {

		Config instance = Config.getInstance();
		String loadDateStr = instance.getLoadDate();
		String keyMatched = instance.getProperty("key.matched");

		JSONObject keyWordSearchResult = requestSender.keyWordSearch(httpClient, loadDateStr, keyMatched);
		JSONObject fillResult = requestSender.postFillPage(httpClient, keyWordSearchResult);

		log.info("start to wait");
		try {
			while (System.currentTimeMillis() < timeToAdd) {
				try {
					Thread.sleep(50);
				} catch (Throwable e) {
					log.error("thread is interrupted or other runtime exception", e);
				}
			}
		} catch (Throwable e) {
			
			log.error("loop occured exception", e);
		}
		log.info("end to wait");

		String trainNumber = instance.getProperty("train.number");
		String totalWeight = String.valueOf(Integer.valueOf(trainNumber) * 60);
		String location = instance.getProperty("location");
		String canReceiveMsg = instance.getProperty("can.receive.msg");
		String mobile = instance.getProperty("mobile");
		String uuid = "";
		JSONObject addResult = requestSender.add(httpClient, keyWordSearchResult, loadDateStr, trainNumber, totalWeight, location, canReceiveMsg, mobile, uuid,
				fillResult);

		bookDispatcher.setCurrntFinished(this, addResult);
	}

	public JSONObject submitWithEx(JSONObject addResult) throws Exception {
		return requestSender.submit(httpClient, addResult);
	}

	public static void loginUnderMultipleThread() throws KeyManagementException, NoSuchAlgorithmException {

		// httpClient = HttpsUtils.generateMultipleThreadHttpsClient();
		httpClient = HttpsUtils.generateHttpsClient();

		new RequestSenderWithRetry(new RequestSender()).login(httpClient);

	}

	public static void keepSession() {

		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 1000);

		long endTime = DateTimeUtils.getTime(Config.getInstance().getProperty("wait.target.time.after.login")).getTime();
		RequestSenderWithRetry requestSender = new RequestSenderWithRetry(new RequestSender());

		log.info("start to wait");
		try {

			while (System.currentTimeMillis() < endTime) {
				log.info("wait to add!");
				requestSender.home(httpClient);
				try {
					Thread.sleep(1000 * 30);
				} catch (InterruptedException e) {
					log.error("thread exception", e);
				}

			}
		} catch (Throwable e) {
			log.error("occurred run time exception when wait", e);
		}
		log.info("end to wait");
		// https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/WorkPlatformAction_getCurBgMenu

		httpClient.getParams().removeParameter(CoreConnectionPNames.CONNECTION_TIMEOUT);
		httpClient.getParams().removeParameter(CoreConnectionPNames.SO_TIMEOUT);

	}
}