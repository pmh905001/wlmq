package com.pmh.wlmq.https;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import lombok.extern.log4j.Log4j;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

@Log4j
public class TrainBookFromNetWorkUpdate {
	public static void main(String[] args) throws Exception {
		book();
	}

	public static void book() throws NoSuchAlgorithmException, KeyManagementException, IOException, ClientProtocolException, JSONException,
			UnsupportedEncodingException, InterruptedException {
		HttpClient httpClient = HttpsUtils.generateHttpsClient();
		loginWithEx(httpClient);
		JSONObject updateResult = update(httpClient);
		log.info(updateResult);
		

	}
	
	private static JSONObject update(HttpClient httpClient) throws ClientProtocolException, IOException, JSONException {

		String url = "https://frontier.wulmq.12306.cn/gateway/hydzsw/Dzsw/action/ZcrbjhAction_plUpdateYdfs";
		String[] data = new String[] {
				
				"ydfsmainVO.xqslh","201410RY654740",
				"ydfsmainVO.fhjbrxm","test",
				"ydfsmainVO.fhjbrsfzhm","",
				"ydfsmainVO.fhjbrsj","13778866180",
				"ydfsmainVO.shjbrxm","test",
				"ydfsmainVO.shjbrsfzhm","",
				"ydfsmainVO.shjbrsj","13778866180",
				"ydfsmainVO.hwjs","",
				"ydfsmainVO.tyrqdzl","",
				"ydfsmainVO.hwjg","",
				"zcbzModify","",
				"ydfsmainVO.ydid","",
				"ydfsmainVO.uuid","",
				"hwtzModify","",
				"tbfsModify","",
				"zcrq","20141019",
				"ydfsmainVO.tyrjzsx","",
				"fwlxq.lxrname","",
				"fwlxq.mobile","",
				"fwlxq.address","",
				"fwlxq.qtmemo","",
				"fwlxq.bz","",
				"dwlxq.lxrname","",
				"dwlxq.mobile","",
				"dwlxq.address","",
				"dwlxq.qtmemo","",
				"dwlxq.bz",""
				
				
				
		};

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