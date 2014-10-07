package com.pmh.wlmq.https;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.extern.log4j.Log4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONObject;

@Log4j
public class ExcelUtils {
	private static final String CharEncoding = Config.getInstance().getProperty("excel.file.encoding","GBK");
	private static final File File = new File(Config.getInstance().getProperty("excel.file.path","./每日提交记录.csv"));

	public static List<DailyRecord> read() throws IOException {
		List<String> lines = FileUtils.readLines(File, CharEncoding);

		List<DailyRecord> result = new ArrayList<DailyRecord>();
		for (int row = 1; row < lines.size(); row++) {
			String line = lines.get(row);
			if (StringUtils.isBlank(line)) {
				break;
			}
			String[] columns = line.split(",");

			DailyRecord record = new DailyRecord();
			if (columns.length == 6) {
				// 装车日期,关键字查询,订车数,货区货位,收货人接收到货短信,收货人手机号,提交日期,是否成功
				String loadDateStr = columns[0].replace("/", "-");
				record.setLoadDateStr(loadDateStr);
				record.setKeyMatched(columns[1]);
				record.setTrainNumber(columns[2]);
				record.setLocation(columns[3]);
				record.setCanReceiveMsg(columns[4]);
				record.setMobile(columns[5]);

				record.setRow(row);
				result.add(record);
			}

		}

		return result;
	}

	public synchronized static void markFinished(DailyRecord dailyRecord, JSONObject submitResult) throws IOException {

		List<String> lines = FileUtils.readLines(File, CharEncoding);

		List<String> result = new ArrayList<String>();
		for (int row = 0; row < lines.size(); row++) {
			String line = lines.get(row);
			if (row == dailyRecord.getRow()) {
				line = line + "," + DateTimeUtils.getFormatedDateTime() + "," + ((submitResult == null) ? "失败" : "成功");
			}
			result.add(line);
		}
		FileUtils.writeLines(File, CharEncoding, result);

	}

}