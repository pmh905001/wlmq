package com.pmh.wlmq.https;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
public class TrainBookFromConfig implements Runnable {

	private DailyRecord dailyRecord = null;

	public TrainBookFromConfig(DailyRecord dailyRecord) {
		this.dailyRecord = dailyRecord;
	}

	public static void main(String[] args) throws Exception {
		List<DailyRecord> dailyRecords = ExcelUtils.read();
		List<Thread> threadList = new ArrayList<Thread>();
		for (DailyRecord dailyRecord : dailyRecords) {
			// bookWithoutEx();
			// bookWithEx(dailyRecord);

			Thread thread = new Thread(new TrainBookFromConfig(dailyRecord));
			threadList.add(thread);
			thread.start();
		}

		for (Thread thread : threadList) {
			thread.join();
		}

	}

	public static void bookWithEx(DailyRecord dailyRecord) throws NoSuchAlgorithmException, KeyManagementException, IOException, ClientProtocolException,
			JSONException, UnsupportedEncodingException, InterruptedException {
		HttpClient httpClient = HttpsUtils.generateHttpsClient();

		loginWithEx(httpClient);

		String loadDateStr = dailyRecord.getLoadDateStr();
		String keyMatched = dailyRecord.getKeyMatched();

		JSONObject keyWordSearchResult = keyWordSearchWithEx(httpClient, loadDateStr, keyMatched);
		JSONObject fillResult = postFillPageWithEx(httpClient, keyWordSearchResult);

		waitTo7Clock();

		JSONObject addResult = addWithEx(httpClient, dailyRecord, keyWordSearchResult, fillResult);
		JSONObject submitResult = submitWithEx(httpClient, addResult);

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

	private static JSONObject addWithEx(HttpClient httpClient, DailyRecord dailyRecord, JSONObject keyWordSearchResult, JSONObject fillResult)
			throws InterruptedException {
		JSONObject addResult = null;
		boolean addException = true;
		while (addException) {
			try {

				String totalWeight = String.valueOf(Integer.valueOf(dailyRecord.getTrainNumber()) * 60);
				String uuid = "";

				addResult = add(httpClient, keyWordSearchResult, dailyRecord.getLoadDateStr(), dailyRecord.getTrainNumber(), totalWeight,
						dailyRecord.getLocation(), dailyRecord.getCanReceiveMsg(), dailyRecord.getMobile(), uuid, fillResult);

				// {"message":"提报请求车数不能超过最大可用车数，请确认!","object":null,"success":false}
				// {"message":"838502","object":{"PK":"8a60899f48c62c440148df93d978200b","auditUnitId":",FJ,","bqyy":"","bureauSourceId":"R00","bureauTargetId":"#00,W00","cbxxtohd":"","cc":"","czjy":0,"czmemo":"","czsh":0,"datasourceIp":"120.69.181.226","dcf":0,"dddxtz":"1","dfj":"76","dfjhz":"成都","dj":"W","djhz":"成","dyjdjh":0,"dyjdjhds":0,"dz":"QUW","dzhzzm":"青龙场","dztmism":"47699","expfile":"","exptime":"","fabj":"","fhbm":"65","fhbmm":"新","fhdwdh":"13579582810","fhdwdm":"3085233","fhdwmc":"彭山县凯达粮油经营部","fhdwsrm":"","fhdwtz":"","fhdwtzm":"","fj":"95","flag":"Y","flagCancelbook":"N","fz":"BLR","fzhzzm":"布列开","fztmism":"43112","fzyx":"43112   ","fzyxhz":"铁路货场","hpsr":0,"hqhw":"货1","hscs":0,"hsmemo":"","hsoperator":"","hstime":null,"hsxxtohd":"","htddh":"","hysr":0,"hzg":"","hzghz":"","hzjy":"","hzpl":"粮食","hzpm":"玉米","ifOverload":"","iftohd":"N","ifzzjg":"","jddz1":0,"jddz2":0,"jddz3":0,"jddz4":0,"jdtz1":0,"jdtzds1":0,"jdzc1":0,"jdzc2":0,"jdzc3":0,"jdzc4":0,"jdzcds1":0,"jdzcds2":0,"jdzcds3":0,"jdzcds4":0,"jhid":"","jhtz":"","jsjj":0,"jy":"","khdm":"3085233","khmc":"彭山县凯达粮油经营部管理员","khmemo":"","lastModifyTime":{"date":5,"day":0,"hours":17,"minutes":11,"month":9,"seconds":21,"time":1412500281717,"timezoneOffset":-480,"year":114},"lastModifyUser":"3085233","lastSyncTime":null,"ljmemo":"","lkfx":"","loadfile":"","loadtime":"","lsbhflag":"Y","lwdw":"KFR00","lxhz":"","ope1":"","ope2":"","ope3":"","ope4":"","opmemo":"","otherflag":"","pm":"1150002","pzcs":0,"pzcz":"","pzds":0,"pzycfh":"10R00477263","qqcs":2,"qqcz":"C","qqczhz":"敞","qqds":120,"qqlx":"0","rjhh":"","rksj":"20141005 17:11:21","sbbz":"","seqnum":267697,"shbm":"51","shbmm":"川","shdwdh":"13778866180","shdwdm":"","shdwmc":"四川青龙场物流有限公司","shdwsrm":"","shdwtz":"","shdwtzm":"","sprdm":"","srhcode":"838502","sskhdm":"3085233","sskhmc":"彭山县凯达粮油经营部管理员","ssqydm":"3085233","ssqymc":"彭山县凯达粮油经营部","stateLevel":"","submitStatus":false,"tbqydm":"3085233","tbqymc":"彭山县凯达粮油经营部","th":"","transtate":"","txzbz":"","type":"03","uuid":"8a60899f48c62c440148df93d978200b","wpyyhz":"","xqslh":"201409RY288408","ybjh":0,"ybjhds":0,"ybpc":"","yjcs1":0,"yjcs2":0,"yjcs3":0,"yjds1":0,"yjds2":0,"yjds3":0,"yjqr":0,"yjqrds":0,"yl1":0,"yl2":"","yl3":"","yl4":0,"yl5":0,"yl6":0,"yl7":0,"yl8":"","yl9":0,"ystz":"","ystzhz":"","zccz":"","zcrq":"20141015","zdg":"","zdghz":"","ztgj":"00","ztgjjc":"待上报","zyxm":""},"success":true}

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

	private static JSONObject submitWithEx(HttpClient httpClient, JSONObject addResult) throws InterruptedException {
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
		return submitResult;
	}

	private static JSONObject submit(HttpClient httpClient, JSONObject addResult) throws UnsupportedEncodingException, ClientProtocolException, IOException,
			JSONException {

		if (addResult.getBoolean("success")) {
			String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/ZcrbjhAction_operateZcrbjh";
			String[] data = new String[] { "op", "10", "uuids", addResult.getJSONObject("object").getString("uuid"), "mor_dzsw_security_info",
					"mor_dzsw_security_disabled" };
			// {"message":"操作成功","object":null,"success":true}
			return ajaxPostSend(httpClient, url, data);
		} else {
			return null;
		}

	}

	private static JSONObject add(HttpClient httpClient, JSONObject keyWordSearchResult, String loadDateStr, String qqcs, String qqds, String hqhw,
			String dddxtz, String shdwdh, String uuid, JSONObject fillResult) throws ClientProtocolException, IOException, JSONException {

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

		return ajaxPostSend(httpClient, url, data);
	}

	private static JSONObject postFillPage(HttpClient httpClient, JSONObject keyWordSearchResult) throws ClientProtocolException, IOException, JSONException {
		String item = keyWordSearchResult.getString("PZYCFH");
		String result = Config.getInstance().getProperty("key.word." + item);
		log.info("fill result" + result);
		return new JSONObject(result);
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

	public void run() {
		try {
			bookWithEx(this.dailyRecord);
		} catch (Exception e) {

			log.error("book train error", e);
		}
	}

}