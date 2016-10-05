package discordbot.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import static org.apache.http.protocol.HTTP.USER_AGENT;

/**
 * Created on 8-9-2016
 */
public class HttpHelper {

	/**
	 * @param url the url to request to
	 * @return a string containing the response
	 */
	public static String doRequest(String url) {
		return doRequest(url, null);
	}

	/**
	 * @param url     the url to request to
	 * @param headers map of headers
	 * @return a string containing the response
	 */
	public static String doRequest(String url, Map<String, String> headers) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		request.addHeader("User-Agent", USER_AGENT);
		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				request.addHeader(entry.getKey(), entry.getValue());
			}
		}
		try {
			HttpResponse response = client.execute(request);
			InputStreamReader inputstream = new InputStreamReader(response.getEntity().getContent());
			BufferedReader rd = new BufferedReader(inputstream);
			StringBuilder result = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
			return result.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";

	}
}
