package com.pmh.wlmq.https;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import lombok.extern.log4j.Log4j;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;

import com.pmh.wlmq.https.sender.RequestSender;
import com.pmh.wlmq.https.sender.RequestSenderWithRetry;

@Log4j
public class SessionTimer {

	public static void main(String args[]) throws InterruptedException, KeyManagementException, NoSuchAlgorithmException {

		HttpClient httpClient = HttpsUtils.generateHttpsClient();
		RequestSender request = new RequestSender();
		RequestSenderWithRetry requestSenderWithRetry = new RequestSenderWithRetry(request);
		requestSenderWithRetry.login(httpClient);

		log.info("session timer start");
		long startTime = System.currentTimeMillis();

		try {
			Thread.sleep(1000 * 60 * 20);

			HttpResponse response = request.home(httpClient);
			HttpEntity entity = response.getEntity();

			log.info(EntityUtils.toString(entity));

		} catch (Exception e) {
			log.error("go home occurred exception");
		}

		log.info("session timer end");
		log.info("cost time is:" + (System.currentTimeMillis() - startTime));

	}

}
