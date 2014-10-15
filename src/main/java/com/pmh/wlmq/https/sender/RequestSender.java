package com.pmh.wlmq.https.sender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import lombok.extern.log4j.Log4j;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pmh.wlmq.https.Config;
import com.pmh.wlmq.https.DateTimeUtils;
import com.pmh.wlmq.https.HttpsUtils;
import com.pmh.wlmq.https.TesseractOCRUtils;

@Log4j
public class RequestSender implements IRequestSender {

	public static void main(String[] args) throws Exception {
		HttpClient httpClient = HttpsUtils.generateHttpsClient();

		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 1);

		// HttpClient httpClient = HttpsUtils.generateMultipleThreadHttpsClient();
		try {
			new RequestSender().login(httpClient);
		} catch (Exception e) {
			log.error(e);
		}

		httpClient.getParams().removeParameter(CoreConnectionPNames.CONNECTION_TIMEOUT);
		httpClient.getParams().removeParameter(CoreConnectionPNames.SO_TIMEOUT);

		// HttpClient httpClient = HttpsUtils.generateMultipleThreadHttpsClient();
		new RequestSender().login(httpClient);

	}

	public JSONObject submit(HttpClient httpClient, JSONObject addResult) throws Exception {
		String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/ZcrbjhAction_operateZcrbjh";
		String[] data = new String[] { "op", "10", "uuids", addResult.getJSONObject("object").getString("uuid"), "mor_dzsw_security_info",
				"mor_dzsw_security_disabled" };
		// {"message":"操作成功","object":null,"success":true}
		return HttpsUtils.ajaxPostSend(httpClient, url, data);
	}

	public JSONObject add(HttpClient httpClient, JSONObject keyWordSearchResult, String loadDateStr, String qqcs, String qqds, String hqhw, String dddxtz,
			String shdwdh, String uuid, JSONObject fillResult) throws Exception {

		String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/ZcrbjhAction_add";
		String[] data = new String[] { "currentPosition", "预约", "keyword", "", "po.qqlx", "0", "po.xqslh",
				keyWordSearchResult.getString("XQSLH")/* "201409RY287800" */, "po.pzycfh", keyWordSearchResult.getString("PZYCFH") /* "09R00477233" */, "po.zcrq",
				loadDateStr/* "2014-09-30" */, "minDate", DateTimeUtils.getFormatedDate() /* "2014-09-28" */, "maxDate",
				DateTimeUtils.getFormatedDate(new Date(), 10) /* "2014-10-08" */, "po.qqcz", keyWordSearchResult.getString("CZ") /* "C" */, "po.qqcs",
				qqcs /* "17" */, "po.qqds", qqds /* "1020" */, "po.hqhw", hqhw /* "" */, "po.dddxtz", dddxtz/* "1" */, "po.shdwdh", shdwdh /* "13778866180" */,
				"po.uuid", uuid/* "" */, "fzhzzm", keyWordSearchResult.getString("FZHZZM")/* "布列开" */, "fztmism",
				keyWordSearchResult.getString("FZTMISM")/* "43112" */, "fjm", keyWordSearchResult.getString("FJQC")/* "乌鲁木齐局" */, "fhdwmc",
				keyWordSearchResult.getString("FHDWMC") /* "彭山县凯达粮油经营部" */, "dzhzzm", keyWordSearchResult.getString("DZHZZM") /* "青龙场" */, "dztmism",
				keyWordSearchResult.getString("DZTMISM") /* "47699" */, "djm", keyWordSearchResult.getString("DJQC") /* "成都局" */, "shdwmc",
				keyWordSearchResult.getString("SHDWMC")/* "四川青龙物流有限公司" */, "zcdd", fillResult.getString("zcdd")/* "铁路货场" */, "fzyx",
				fillResult.getString("zcdddm") /* "43112   " */, "qqcsMax", qqcs/* "17" */, "xcdd", fillResult.getString("xcdd") /* "四川青龙物流有限公司专用线" */, "dzyx",
				fillResult.getString("xcdddm") /* "47699002" */, "hzpm", keyWordSearchResult.getString("HZPM") /* "玉米" */};

		return HttpsUtils.ajaxPostSend(httpClient, url, data);
	}

	public JSONObject postFillPage(HttpClient httpClient, JSONObject keyWordSearchResult) throws Exception {
		String item = keyWordSearchResult.getString("PZYCFH");
		String result = Config.getInstance().getProperty("key.word." + item);
		log.info("fill result" + result);
		return new JSONObject(result);
	}

	public JSONObject keyWordSearch(HttpClient httpClient, String loadDateStr, String keyMatched) throws Exception {
		String keyWordResult = Config.getInstance().getProperty("key.word.result.array");
		return findByKeyWord(keyWordResult);
	}

	private static JSONObject findByKeyWord(String list) throws JSONException {
		log.info("key word search json response:" + list);

		JSONArray arr = new JSONArray(list);

		JSONObject result = null;
		for (int i = 0; i < arr.length(); i++) {
			JSONObject item = arr.getJSONObject(i);
			if (item.getString("FZHZZM").contains("布列开")) {
				result = item;
				break;
			}
		}
		return result;
	}

	public void login(HttpClient httpClient, String captcha) throws Exception {
		String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/j_spring_security_check";
		String[] data = new String[] { "j_username", "3085233", "j_password", "8721390", "j_captcha", captcha, "fromUrl", "/login_bur.jsp" };
		HttpResponse response = HttpsUtils.postMethodSend(httpClient, url, data);

		String responseBody = EntityUtils.toString(response.getEntity());
		log.info("try to login,response:" + responseBody);

		Header[] headers = response.getAllHeaders();
		for (Header header : headers) {
			if ("Location".equals(header.getName())) {
				String location = header.getValue();
				log.info("redirect url:" + location);
				if ("/gateway/hydzsw/Dzsw/login_bur.jsp".equals(location)) {
					// login failed
					log.info("login failed");
					throw new IllegalStateException();
				} else {
					log.info("login success");
					return;
				}
			}
		}
	}

	public void login(HttpClient httpClient) throws Exception {
		String captcha = this.tryToGetCaptcha(httpClient);
		this.login(httpClient, captcha);
	}

	public String tryToGetCaptcha(HttpClient client) throws Exception {
		String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/security/jcaptcha.jpg";
		log.info("try to get captcha,request url:" + url);
		HttpGet get = new HttpGet(url);
		get.addHeader("Connection", "keep-alive");
		HttpResponse response = client.execute(get);

		log.info("try to get captcha,response ok,write to a file");

		// String captchaFilePath = "C:/daily-tasks/data/wulmqhttps/jcaptcha.jpg";
		String captchaFilePath = Config.getInstance().getProperty("captcha.file.path", "./jcaptcha.jpg");
		File storeFile = new File(captchaFilePath);
		FileOutputStream output = null;
		InputStream instream = null;
		try {
			output = new FileOutputStream(storeFile);
			instream = response.getEntity().getContent();
			byte b[] = new byte[1024];
			int j = 0;
			while ((j = instream.read(b)) != -1) {
				output.write(b, 0, j);
			}
		} finally {
			if (output != null) {
				output.close();
			} else {
				log.error("output file is null");
			}

			if (instream != null) {
				instream.close();
			} else {
				log.error("input stream is null");
			}
		}

		log.info("write to a file finished, try to read it");

		String result = TesseractOCRUtils.ocrImage2Text(storeFile.getAbsolutePath());

		log.info("code is :" + result);

		return result;
	}

	public void home(HttpClient httpClient) throws Exception {
		HttpsUtils.getMethodSend(httpClient, "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/WorkPlatformAction_getCurBgMenu");
	}

}