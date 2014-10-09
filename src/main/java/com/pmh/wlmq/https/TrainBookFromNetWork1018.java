package com.pmh.wlmq.https;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import lombok.extern.log4j.Log4j;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Log4j
public class TrainBookFromNetWork1018 {
	public static void main(String[] args) throws Exception {
		book();
	}

	public static void book() throws NoSuchAlgorithmException, KeyManagementException, IOException, ClientProtocolException, JSONException,
			UnsupportedEncodingException, InterruptedException {
		HttpClient httpClient = HttpsUtils.generateHttpsClient();

		// tryToLoginPage(httpClient);

		loginWithEx(httpClient);
		String loadDateStr = "2014-10-18";
		String keyMatched = "布列开";

		JSONObject keyWordSearchResult = keyWordSearchWithEx(httpClient, loadDateStr, keyMatched);
		JSONObject fillResult = postFillPageWithEx(httpClient, keyWordSearchResult);
//		checkWithEx(httpClient, loadDateStr, keyWordSearchResult, fillResult);
		JSONObject addResult = addWithEx(httpClient, loadDateStr, keyWordSearchResult, fillResult);
		submitWithEx(httpClient, addResult);

	}

	private static void loginWithEx(HttpClient httpClient) throws InterruptedException {
		boolean isLoginException = true;
		while (isLoginException) {
			try {
				String captcha = tryToGetCaptcha(httpClient);
				login(httpClient, captcha);
				isLoginException = false;
			} catch (Throwable ex) {
				log.error("login exception", ex);
			}
		}
	}

	private static JSONObject keyWordSearchWithEx(HttpClient httpClient, String loadDateStr, String keyMatched) throws InterruptedException {
		JSONObject keyWordSearchResult = null;
		boolean keyWordSearchException = true;
		while (keyWordSearchException) {
			try {

				keyWordSearchResult = keyWordSearch(httpClient, loadDateStr, keyMatched);
				keyWordSearchException = false;
			} catch (Throwable ex) {
				log.error("key word search exception", ex);
			}
		}
		return keyWordSearchResult;
	}

	private static JSONObject postFillPageWithEx(HttpClient httpClient, JSONObject keyWordSearchResult) throws InterruptedException {
		JSONObject fillResult = null;
		boolean fillException = true;
		while (fillException) {
			try {

				fillResult = postFillPage(httpClient, keyWordSearchResult);
				fillException = false;
			} catch (Throwable ex) {
				log.error("fill exception", ex);
			}
		}
		return fillResult;
	}

	private static void checkWithEx(HttpClient httpClient, String loadDateStr, JSONObject keyWordSearchResult, JSONObject fillResult)
			throws InterruptedException {
		JSONObject checkOrderResult = null;
		boolean checkException = true;
		while (checkException) {
			try {

				checkOrderResult = checkOrder(httpClient, keyWordSearchResult, fillResult, loadDateStr);
				checkException = false;
			} catch (Throwable ex) {
				log.error("check exception", ex);
			}
		}
	}

	private static JSONObject addWithEx(HttpClient httpClient, String loadDateStr, JSONObject keyWordSearchResult, JSONObject fillResult)
			throws InterruptedException {
		JSONObject addResult = null;
		boolean addException = true;
		while (addException) {
			try {

				addResult = add(httpClient, keyWordSearchResult, loadDateStr, "2", "120", "货1", "1", "13778866180", "", fillResult);
				addException = false;
			} catch (Throwable ex) {
				log.error("add exception", ex);
			}
		}
		return addResult;
	}

	private static void submitWithEx(HttpClient httpClient, JSONObject addResult) throws InterruptedException {
		JSONObject submitResult = null;
		boolean submitException = true;
		while (submitException) {
			try {

				submitResult = submit(httpClient, addResult);
				submitException = false;
			} catch (Throwable ex) {
				log.error("add exception", ex);
			}
		}
	}

	private static JSONObject submit(HttpClient httpClient, JSONObject addResult) throws UnsupportedEncodingException, ClientProtocolException, IOException,
			JSONException {
		String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/ZcrbjhAction_operateZcrbjh";
		String[] data = new String[] { "op", "10", "uuids", addResult.getJSONObject("object").getString("uuid"), "mor_dzsw_security_info",
				"mor_dzsw_security_disabled" };

		return ajaxPostSend(httpClient, url, data);
	}

	/**
	 * 
	 * @param httpClient
	 * @param keyWordSearchResult
	 * @param loadDateStr
	 * @param qqcs
	 *            :车数
	 * @param qqds
	 *            ：吨数 hqhw:货区货位 收货人接收到货短信：
	 * @param fillResult
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	private static JSONObject add(HttpClient httpClient, JSONObject keyWordSearchResult, String loadDateStr, String qqcs, String qqds, String hqhw,
			String dddxtz, String shdwdh, String uuid, JSONObject fillResult) throws ClientProtocolException, IOException, JSONException {

		String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/ZcrbjhAction_add";
		String[] data = new String[] { "currentPosition", "预约", "keyword", "", "po.qqlx", "0", "po.xqslh",
				keyWordSearchResult.getString("XQSLH")/* "201409RY287800" */, "po.pzycfh", keyWordSearchResult.getString("PZYCFH") /* "09R00477233" */, "po.zcrq",
				loadDateStr/* "2014-09-30" */, "minDate", "2014-10-08" /*DateTimeUtils.getFormatedDate()*/ /* "2014-09-28" */, "maxDate",
				"2014-10-18" /*DateTimeUtils.getFormatedDate(new Date(), 10)*/ /* "2014-10-08" */, "po.qqcz", keyWordSearchResult.getString("CZ") /* "C" */, "po.qqcs",
				qqcs /* "17" */, "po.qqds", qqds /* "1020" */, "po.hqhw", hqhw /* "" */, "po.dddxtz", dddxtz/* "1" */, "po.shdwdh", shdwdh /* "13778866180" */,
				"po.uuid", uuid/* "" */, "fzhzzm", keyWordSearchResult.getString("FZHZZM")/* "布列开" */, "fztmism",
				keyWordSearchResult.getString("FZTMISM")/* "43112" */, "fjm", keyWordSearchResult.getString("FJQC")/* "乌鲁木齐局" */, "fhdwmc",
				keyWordSearchResult.getString("FHDWMC") /* "彭山县凯达粮油经营部" */, "dzhzzm", keyWordSearchResult.getString("DZHZZM") /* "青龙场" */, "dztmism",
				keyWordSearchResult.getString("DZTMISM") /* "47699" */, "djm", keyWordSearchResult.getString("DJQC") /* "成都局" */, "shdwmc",
				keyWordSearchResult.getString("SHDWMC")/* "四川青龙物流有限公司" */, "zcdd", fillResult.getString("zcdd")/* "铁路货场" */, "fzyx",
				fillResult.getString("zcdddm") /* "43112   " */, "qqcsMax", "100"/* "17" */, "xcdd", fillResult.getString("xcdd") /* "四川青龙物流有限公司专用线" */, "dzyx",
				fillResult.getString("xcdddm") /* "47699002" */, "hzpm", keyWordSearchResult.getString("HZPM") /* "玉米" */};

		return ajaxPostSend(httpClient, url, data);
	}

	private static JSONObject checkOrder(HttpClient httpClient, JSONObject keyWordSearchResult, JSONObject fillResult, String loadDateStr)
			throws ClientProtocolException, IOException, JSONException {
		// https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/TbInformationRestrictionAction_checkOrder
		// fztmism=43112&dztmism=47699&fzyx=43112+++&dzyx=47699002&startDate=2014-09-30&endDate=2014-09-30
		// {"message":"","object":null,"success":false}
		String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/TbInformationRestrictionAction_checkOrder";
		String data[] = { "fztmism", fillResult.getString("zcdddm"), "dztmism", fillResult.getString("xcdddm"), "fzyx",
				keyWordSearchResult.getString("FZTMISM"), "dzyx", keyWordSearchResult.getString("DZTMISM"), "startDate", loadDateStr, "endDate", loadDateStr };
		return ajaxPostSend(httpClient, url, data);
	}

	private static JSONObject postFillPage(HttpClient httpClient, JSONObject keyWordSearchResult) throws ClientProtocolException, IOException, JSONException {

		// https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/ZcrbjhAction_getZyxByPzycfh
		String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/ZcrbjhAction_getZyxByPzycfh";
		String[] data = new String[] { "pzycfh", keyWordSearchResult.getString("PZYCFH") };
		return ajaxPostSend(httpClient, url, data);

	}

	private static JSONObject ajaxPostSend(HttpClient httpClient, String url, String[] data) throws UnsupportedEncodingException, IOException,
			ClientProtocolException, JSONException {
		HttpResponse response = HttpsUtils.postMethodSend(httpClient, url, data);
		String ret = EntityUtils.toString(response.getEntity());
		log.info("ajax post response:" + ret);
		JSONObject result = new JSONObject(ret);
		return result;
	}

	private static JSONObject keyWordSearch(HttpClient httpClient, String loadDateStr, String keyMatched) throws ClientProtocolException, IOException,
			JSONException {
		// https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/ZcrbjhAction_getYsxq?q=&limit=50&timestamp=1411888277205&zcrq=2014-09-30

//		long timestamp = System.currentTimeMillis();
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 8);
		calendar.set(Calendar.HOUR_OF_DAY,7);
		calendar.set(Calendar.MINUTE,0);
		calendar.set(Calendar.SECOND,0);
		System.out.println(calendar.getTime());
		long timestamp = calendar.getTimeInMillis();
		
		String urlTemplate = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/ZcrbjhAction_getYsxq?q=&limit=50&timestamp=%s&zcrq=%s";
		String url = String.format(urlTemplate, timestamp, loadDateStr);

		log.info("key word search");

		HttpResponse response = HttpsUtils.getMethodSend(httpClient, url);

		String list = EntityUtils.toString(response.getEntity());

		log.info("key word search json response:" + list);

		JSONArray arr = new JSONArray(list);

		JSONObject result = null;
		for (int i = 0; i < arr.length(); i++) {
			JSONObject item = arr.getJSONObject(i);
			if (item.getString("FZHZZM").contains("布列开")&& item.getString("XQSLH").contains("201410RY654740")) {
				result = item;
				break;
			}
		}
		return result;
	}

	private static void tryToLoginPage(HttpClient httpClient) throws IOException, ClientProtocolException, InterruptedException {
		String html = accessLoginPage(httpClient);
		int count = 0;
		while (html.contains("系统正在维护中")) {
			Thread.sleep(1000 * 1);
			html = accessLoginPage(httpClient);
			log.info(++count);
		}

		log.info("go to login,response:" + html);

	}

	private static String accessLoginPage(HttpClient httpClient) throws IOException, ClientProtocolException {
		String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/login_bur.jsp";
		HttpResponse response = HttpsUtils.getMethodSend(httpClient, url);
		String html = EntityUtils.toString(response.getEntity());
		return html;
	}

	private static void login(HttpClient httpClient, String captcha) throws ClientProtocolException, IOException {
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
					String captchaNew = tryToGetCaptcha(httpClient);
					login(httpClient, captchaNew);
				} else {
					log.info("login success");
					return;
				}
			}
		}
	}

	public static String tryToGetCaptcha(HttpClient client) throws IOException {
		String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/security/jcaptcha.jpg";
		log.info("try to get captcha,request url:" + url);
		HttpGet get = new HttpGet(url);
		HttpResponse response = client.execute(get);

		log.info("try to get captcha,response ok,write to a file");

		File storeFile = new File("C:/daily-tasks/2014/09/28/jcaptcha.jpg");
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

}