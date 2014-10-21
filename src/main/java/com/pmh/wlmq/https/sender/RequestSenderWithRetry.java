package com.pmh.wlmq.https.sender;

import lombok.extern.log4j.Log4j;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.json.JSONObject;

@Log4j
public class RequestSenderWithRetry implements IRequestSender {

	private IRequestSender request;

	public RequestSenderWithRetry(IRequestSender request) {
		this.request = request;
	}

	public JSONObject submit(HttpClient httpClient, JSONObject addResult) {
		while (true) {
			try {
				return request.submit(httpClient, addResult);
			} catch (Throwable ex) {
				log.error("submit exception,retry!", ex);
			}
		}
	}

	public JSONObject add(HttpClient httpClient, JSONObject keyWordSearchResult, String loadDateStr, String qqcs, String qqds, String hqhw, String dddxtz,
			String shdwdh, String uuid, JSONObject fillResult) {
		while (true) {
			try {
				JSONObject addResult = request.add(httpClient, keyWordSearchResult, loadDateStr, qqcs, qqds, hqhw, dddxtz, shdwdh, uuid, fillResult);
				if (!addResult.getBoolean("success")) {
					throw new IllegalStateException("add failed!");
				}
				return addResult;
			} catch (Throwable ex) {
				log.error("add exception,retry!", ex);
			}
		}
	}

	public JSONObject postFillPage(HttpClient httpClient, JSONObject keyWordSearchResult) {

		while (true) {
			try {
				return request.postFillPage(httpClient, keyWordSearchResult);
			} catch (Throwable ex) {
				log.error("add exception,retry!", ex);
			}
		}

	}

	public JSONObject keyWordSearch(HttpClient httpClient, String loadDateStr, String keyMatched) {

		while (true) {
			try {
				return request.keyWordSearch(httpClient, loadDateStr, keyMatched);
			} catch (Throwable ex) {
				log.error("add exception,retry!", ex);
			}
		}
	}

	public void login(HttpClient httpClient, String captcha) {

		while (true) {
			try {
				request.login(httpClient, captcha);
				return;
			} catch (Throwable ex) {
				captcha = tryToGetCaptcha(httpClient);
				log.error("login exception,retry!", ex);
			}
		}
	}

	public String tryToGetCaptcha(HttpClient client) {

		while (true) {
			try {
				return request.tryToGetCaptcha(client);
			} catch (Throwable ex) {
				log.error("get captcha exception,retry!", ex);
			}
		}
	}

	public void login(HttpClient httpClient) {

		while (true) {
			try {
				String captcha = request.tryToGetCaptcha(httpClient);
				request.login(httpClient, captcha);
				return;
			} catch (Throwable ex) {
				log.error("login exception,retry!", ex);
			}
		}
	}

	public HttpResponse home(HttpClient httpClient) {

		HttpResponse result = null;
		try {
			result = request.home(httpClient);
		} catch (Throwable ex) {
			log.error("go home exception", ex);
		}
		return result;

	}

}