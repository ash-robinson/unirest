package com.github.ashrobinson.rest.unirest;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

import com.github.ashrobinson.rest.http.*;
import com.github.ashrobinson.rest.http.async.utils.*;
import com.github.ashrobinson.rest.http.options.*;
import com.github.ashrobinson.rest.http.utils.*;
import com.github.ashrobinson.rest.request.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Unirest {

	private Options options = null;
	
	public Unirest()
	{
		super();
		options = new Options();
		options.refresh(); //on init reload with default config
	}
	
	/**
	 * Set the HttpClient implementation to use for every synchronous request
	 * 
	 * @param httpClient a custom httpclient for use with unirest operations
	 */
	public void setHttpClient(HttpClient httpClient) {
		options.setOption(Option.HTTPCLIENT, httpClient);
		options.customClientSet();		
	}

	/**
	 * Set the asynchronous AbstractHttpAsyncClient implementation to use for every asynchronous request
	 * 
	 * @param asyncHttpClient a custom asynHttpclient for use with unirest operations
	 */
	public void setAsyncHttpClient(CloseableHttpAsyncClient asyncHttpClient) {
		options.setOption(Option.ASYNCHTTPCLIENT, asyncHttpClient);
		options.customClientSet();
	}

	/**
	 * Set a proxy
	 * 
	 * @param proxy set a proxy for standard client operations
	 */
	public void setProxy(HttpHost proxy) {
		options.setOption(Option.PROXY, proxy);
	}

	/**
	 * Set the ObjectMapper implementation to use for Response to Object binding
	 *
	 * @param objectMapper Custom implementation of ObjectMapper interface
	 */
	public void setObjectMapper(ObjectMapper objectMapper) {
		options.setOption(Option.OBJECT_MAPPER, objectMapper);
	}

	/**
	 * Set the connection timeout and socket timeout
	 * 
	 * @param connectionTimeout The timeout until a connection with the server is established (in milliseconds). Default is 10000. Set to zero to disable the timeout.
	 * @param socketTimeout The timeout to receive data (in milliseconds). Default is 60000. Set to zero to disable the timeout.
	 */
	public void setTimeouts(long connectionTimeout, long socketTimeout) {
		options.setOption(Option.CONNECTION_TIMEOUT, connectionTimeout);
		options.setOption(Option.SOCKET_TIMEOUT, socketTimeout);
	}

	/**
	 * Set the concurrency levels
	 * 
	 * @param maxTotal Defines the overall connection limit for a connection pool. Default is 200.
	 * @param maxPerRoute Defines a connection limit per one HTTP route (this can be considered a per target host limit). Default is 20.
	 */
	public void setConcurrency(int maxTotal, int maxPerRoute) {
		options.setOption(Option.MAX_TOTAL, maxTotal);
		options.setOption(Option.MAX_PER_ROUTE, maxPerRoute);
	}

	/**
	 * Clear default headers
	 */
	public void clearDefaultHeaders() {
		options.setOption(Option.DEFAULT_HEADERS, null);
	}

	/**
	 * Set default header
	 * 
	 * @param name header name
	 * @param value header value
	 */
	@SuppressWarnings("unchecked")
	public void setDefaultHeader(String name, String value) {
		Object headers = options.getOption(Option.DEFAULT_HEADERS);
		if (headers == null) {
			headers = new HashMap<String, String>();
		}
		((Map<String, String>) headers).put(name, value);
		options.setOption(Option.DEFAULT_HEADERS, headers);
	}

	/**
	 * Close the asynchronous client and its event loop. Use this method to close all the threads and allow an application to exit.
	 */
	public void shutdown() throws IOException {
		// Closing the Sync HTTP client
		CloseableHttpClient syncClient = (CloseableHttpClient) options.getOption(Option.HTTPCLIENT);
		if (syncClient != null) {
			syncClient.close();
		}

		SyncIdleConnectionMonitorThread syncIdleConnectionMonitorThread = (SyncIdleConnectionMonitorThread) options.getOption(Option.SYNC_MONITOR);
		if (syncIdleConnectionMonitorThread != null) {
			syncIdleConnectionMonitorThread.interrupt();
		}

		// Closing the Async HTTP client (if running)
		CloseableHttpAsyncClient asyncClient = (CloseableHttpAsyncClient) options.getOption(Option.ASYNCHTTPCLIENT);
		if (asyncClient != null && asyncClient.isRunning()) {
			asyncClient.close();
		}

		AsyncIdleConnectionMonitorThread asyncMonitorThread = (AsyncIdleConnectionMonitorThread) options.getOption(Option.ASYNC_MONITOR);
		if (asyncMonitorThread != null) {
			asyncMonitorThread.interrupt();
		}
	}

	public GetRequest get(String url) {
		return new GetRequest(HttpMethod.GET, url, options);
	}

	public GetRequest head(String url) {
		return new GetRequest(HttpMethod.HEAD, url, options);
	}

	public HttpRequestWithBody options(String url) {
		return new HttpRequestWithBody(HttpMethod.OPTIONS, url, options);
	}

	public HttpRequestWithBody post(String url) {
		return new HttpRequestWithBody(HttpMethod.POST, url, options);
	}

	public HttpRequestWithBody delete(String url) {
		return new HttpRequestWithBody(HttpMethod.DELETE, url, options);
	}

	public HttpRequestWithBody patch(String url) {
		return new HttpRequestWithBody(HttpMethod.PATCH, url, options);
	}

	public HttpRequestWithBody put(String url) {
		return new HttpRequestWithBody(HttpMethod.PUT, url, options);
	}

}
