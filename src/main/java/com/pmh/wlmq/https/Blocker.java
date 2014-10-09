package com.pmh.wlmq.https;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import lombok.extern.log4j.Log4j;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;

@Log4j
public class Blocker implements Runnable {

	public static void main(String args[]) throws InterruptedException {
		Blocker.block();

	}

	public static void block() throws InterruptedException {

		for (int i = 0; i < 100; i++) {

			Thread thread = new Thread(new Blocker());
			thread.start();
			thread.join();
		}

	}

	public void run() {

		try {
			HttpClient client = HttpsUtils.generateHttpsClient();
			while (true) {
				accessLoginPage(client);
				Thread.sleep(50);
				log.info("send request");
			}
		} catch (Exception e) {
			log.error(e);
		}

	}

	private static String accessLoginPage(HttpClient httpClient) throws IOException, ClientProtocolException {
		String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/login_bur.jsp";
		HttpResponse response = HttpsUtils.getMethodSend(httpClient, url);
		String html = EntityUtils.toString(response.getEntity());
		return html;
	}

}
