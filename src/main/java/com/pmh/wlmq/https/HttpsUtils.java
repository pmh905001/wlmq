package com.pmh.wlmq.https;

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

@Log4j
public class HttpsUtils {

	public static HttpResponse getMethodSend(HttpClient httpClient, String url) throws IOException, ClientProtocolException {
		log.info("get method url:" + url);
		HttpGet get = new HttpGet(url);
		HttpResponse response = httpClient.execute(get);
		return response;
	}

	public static HttpResponse postMethodSend(HttpClient httpClient, String url, String[] data) throws UnsupportedEncodingException, IOException,
			ClientProtocolException {
		log.info("post method url:" + url);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (int i = 0; i < data.length; i = i + 2) {
			log.info(String.format("request data,%s:%s", data[i], data[i + 1]));
			params.add(new BasicNameValuePair(data[i], data[i + 1]));
		}
		HttpPost post = new HttpPost(url);
		HttpResponse response = postMethodSend(httpClient, params, post);
		return response;
	}

	private static HttpResponse postMethodSend(HttpClient httpClient, List<NameValuePair> params, HttpPost post) throws UnsupportedEncodingException,
			IOException, ClientProtocolException {
		post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		HttpResponse response = httpClient.execute(post);
		return response;
	}

	public static HttpClient generateHttpsClient() throws NoSuchAlgorithmException, KeyManagementException {

		X509TrustManager tm = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] xcs, String string) {
			}

			public void checkServerTrusted(X509Certificate[] xcs, String string) {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		SSLContext sslcontext = SSLContext.getInstance("TLS");

		sslcontext.init(null, new TrustManager[] { tm }, null);

		SSLSocketFactory socketFactory = new SSLSocketFactory(sslcontext, SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
		Scheme sch = new Scheme("https", 443, socketFactory);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(sch);

		HttpParams params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000);
		// params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 1000);

		// ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		HttpClient httpClient = new DefaultHttpClient(cm, params);

		log.info("generate http client!");

		return httpClient;
	}

}