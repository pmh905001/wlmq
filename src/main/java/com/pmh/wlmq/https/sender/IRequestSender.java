package com.pmh.wlmq.https.sender;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.json.JSONObject;

public interface IRequestSender {

	public JSONObject submit(HttpClient httpClient, JSONObject addResult) throws Exception;

	public JSONObject add(HttpClient httpClient, JSONObject keyWordSearchResult, String loadDateStr, String qqcs, String qqds, String hqhw, String dddxtz,
			String shdwdh, String uuid, JSONObject fillResult) throws Exception;

	public JSONObject postFillPage(HttpClient httpClient, JSONObject keyWordSearchResult) throws Exception;

	public JSONObject keyWordSearch(HttpClient httpClient, String loadDateStr, String keyMatched) throws Exception;

	public void login(HttpClient httpClient, String captcha) throws Exception;
	
	public void login(HttpClient httpClient) throws Exception;

	public String tryToGetCaptcha(HttpClient client) throws Exception;

	public HttpResponse home(HttpClient httpClient)throws Exception;

}