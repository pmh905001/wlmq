package com.pmh.wlmq.https;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

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
public class TrainBook {

	private static HttpClient httpClient;

	public static void main(String[] args) throws Exception {

		
		//
//		TrainBook trainBook = new TrainBook(0, new BookDispatcher());
//		trainBook.bookWithoutEx();

		// TrainBook.loginStepWithMultipleThread();
		// trainBook.bookWithEx();
	}

	private long timeToAdd;

	private BookDispatcher bookDispatcher;

	public TrainBook(long time, BookDispatcher bookDispatcher) {
		this.timeToAdd = time;
		this.bookDispatcher = bookDispatcher;
	}

	public void book() throws KeyManagementException, NoSuchAlgorithmException, ClientProtocolException, UnsupportedEncodingException, IOException,
			JSONException, InterruptedException {
		bookWithEx();
	}

//	public void bookWithoutEx() throws NoSuchAlgorithmException, KeyManagementException, IOException, ClientProtocolException, JSONException,
//			UnsupportedEncodingException, InterruptedException {
//
//		loginStep();
//
//		Config instance = Config.getInstance();
//		String loadDateStr = instance.getProperty("load.date");
//		String keyMatched = instance.getProperty("key.matched");
//		String trainNumber = instance.getProperty("train.number");
//		String totalWeight = String.valueOf(Integer.valueOf(trainNumber) * 60);
//		String location = instance.getProperty("location");
//		String canReceiveMsg = instance.getProperty("can.receive.msg");
//		String mobile = instance.getProperty("mobile");
//		String uuid = "";
//
//		JSONObject keyWordSearchResult = keyWordSearch(loadDateStr, keyMatched);
//		JSONObject fillResult = postFillPage(keyWordSearchResult);
//		JSONObject addResult = add(keyWordSearchResult, loadDateStr, trainNumber, totalWeight, location, canReceiveMsg, mobile, uuid, fillResult);
//
//		// bookDispatcher.setCurrntFinished(this,addResult);
//		JSONObject submitResult = submit(addResult);
//	}

//	private static void loginStep() throws NoSuchAlgorithmException, KeyManagementException, IOException, ClientProtocolException {
//		httpClient = HttpsUtils.generateHttpsClient();
//		String captcha = tryToGetCaptcha();
//		login(captcha);
//	}

	public void bookWithEx() throws NoSuchAlgorithmException, KeyManagementException, IOException, ClientProtocolException, JSONException,
			UnsupportedEncodingException, InterruptedException {
		// loginStepWithMultipleThread();

		Config instance = Config.getInstance();
		String loadDateStr = instance.getProperty("load.date");
		String keyMatched = instance.getProperty("key.matched");

		JSONObject keyWordSearchResult = keyWordSearchWithEx(loadDateStr, keyMatched);
		JSONObject fillResult = postFillPageWithEx(keyWordSearchResult);
		while (System.currentTimeMillis() < timeToAdd) {
			Thread.sleep(50);
		}
		JSONObject addResult = addWithEx(loadDateStr, keyWordSearchResult, fillResult);

		bookDispatcher.setCurrntFinished(this, addResult);
		// submitWithEx(addResult);
	}

	public static void loginStepWithMultipleThread() throws NoSuchAlgorithmException, KeyManagementException, InterruptedException {
		httpClient = HttpsUtils.generateHttpsClient();

		loginWithEx();
	}

	private static void loginWithEx() throws InterruptedException {
		boolean isLoginException = false;
		do {
			try {
				String captcha = tryToGetCaptcha();
				login(captcha);
			} catch (Throwable ex) {
				log.error("login exception", ex);
				isLoginException = true;
			}
		} while (isLoginException);
	}

	private JSONObject keyWordSearchWithEx(String loadDateStr, String keyMatched) throws InterruptedException {
		JSONObject keyWordSearchResult = null;
		boolean keyWordSearchException = true;
		while (keyWordSearchException) {
			try {

				keyWordSearchResult = keyWordSearch(loadDateStr, keyMatched);
				keyWordSearchException = false;
			} catch (Throwable ex) {
				log.error("key word search exception", ex);
			}
		}
		return keyWordSearchResult;
	}

	private JSONObject postFillPageWithEx(JSONObject keyWordSearchResult) throws InterruptedException {
		JSONObject fillResult = null;
		boolean fillException = true;
		while (fillException) {
			try {

				fillResult = postFillPage(keyWordSearchResult);
				fillException = false;
			} catch (Throwable ex) {
				log.error("fill exception", ex);
			}
		}
		return fillResult;
	}

	private JSONObject addWithEx(String loadDateStr, JSONObject keyWordSearchResult, JSONObject fillResult) throws InterruptedException {
		JSONObject addResult = null;
		boolean addException = true;
		while (addException) {
			try {

				Config instance = Config.getInstance();
				String trainNumber = instance.getProperty("train.number");
				String totalWeight = String.valueOf(Integer.valueOf(trainNumber) * 60);
				String location = instance.getProperty("location");
				String canReceiveMsg = instance.getProperty("can.receive.msg");
				String mobile = instance.getProperty("mobile");
				String uuid = "";

				addResult = add(keyWordSearchResult, loadDateStr, trainNumber, totalWeight, location, canReceiveMsg, mobile, uuid, fillResult);
				
				
				if (addResult.getBoolean("success")) {
					addException = false;
				} else {
					throw new IllegalStateException("add failed");
				}
				
			} catch (Throwable ex) {
				log.error("add exception", ex);
			}
		}
		return addResult;
	}

	public void submitWithEx(JSONObject addResult) throws InterruptedException {
		JSONObject submitResult = null;
		boolean submitException = true;
		while (submitException) {
			try {

				submitResult = submit(addResult);
				submitException = false;
			} catch (Throwable ex) {
				log.error("add exception", ex);
			}
		}

	}

	private JSONObject submit(JSONObject addResult) throws UnsupportedEncodingException, ClientProtocolException, IOException, JSONException {
		String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/ZcrbjhAction_operateZcrbjh";
		String[] data = new String[] { "op", "10", "uuids", addResult.getJSONObject("object").getString("uuid"), "mor_dzsw_security_info",
				"mor_dzsw_security_disabled" };

		return ajaxPostSend(url, data);
	}

	private JSONObject add(JSONObject keyWordSearchResult, String loadDateStr, String qqcs, String qqds, String hqhw, String dddxtz, String shdwdh,
			String uuid, JSONObject fillResult) throws ClientProtocolException, IOException, JSONException {

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

		return ajaxPostSend(url, data);
	}

	private JSONObject postFillPage(JSONObject keyWordSearchResult) throws ClientProtocolException, IOException, JSONException {
		String item = keyWordSearchResult.getString("PZYCFH");
		String result = Config.getInstance().getProperty("key.word." + item);
		log.info("fill result" + result);
		return new JSONObject(result);
	}

	private JSONObject ajaxPostSend(String url, String[] data) throws UnsupportedEncodingException, IOException, ClientProtocolException, JSONException {
		HttpResponse response = HttpsUtils.postMethodSend(httpClient, url, data);
		String ret = EntityUtils.toString(response.getEntity());
		log.info("ajax post response:" + ret);
		JSONObject result = new JSONObject(ret);
		return result;
	}

	private JSONObject keyWordSearch(String loadDateStr, String keyMatched) throws ClientProtocolException, IOException, JSONException {
		String keyWordResult = Config.getInstance().getProperty("key.word.result.array");
		return findByKeyWord(keyWordResult);
	}

	private JSONObject findByKeyWord(String list) throws JSONException {
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

	private static void login(String captcha) throws ClientProtocolException, IOException {
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
					String captchaNew = tryToGetCaptcha();
					login(captchaNew);
				} else {
					log.info("login success");
					return;
				}
			}
		}
	}

	private static String tryToGetCaptcha() throws IOException {
		String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/security/jcaptcha.jpg";
		log.info("try to get captcha,request url:" + url);
		HttpGet get = new HttpGet(url);
		HttpResponse response = httpClient.execute(get);

		log.info("try to get captcha,response ok,write to a file");

		String captchaFilePath = Config.getInstance().getProperty("captcha.file.path");
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

}