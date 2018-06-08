package com.github.ashrobinson.rest.http.options;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;

import com.github.ashrobinson.rest.http.async.utils.*;
import com.github.ashrobinson.rest.http.utils.*;

public class Options {

	public static final long CONNECTION_TIMEOUT = 10000;
	private static final long SOCKET_TIMEOUT = 60000;
	public static final int MAX_TOTAL = 200;
	public static final int MAX_PER_ROUTE = 20;

	private Map<Option, Object> optionMap = new HashMap<Option, Object>();

	private static boolean customClientSet = false;

	public Options()
	{
		super();
		optionMap = new HashMap<Option, Object>();
		refresh();
	}
	
	public void customClientSet() {
		customClientSet = true;
	}

	public void setOption(Option option, Object value) {
		if ((option == Option.CONNECTION_TIMEOUT || option == Option.SOCKET_TIMEOUT) && customClientSet) {
			throw new RuntimeException("You can't set custom timeouts when providing custom client implementations. Set the timeouts directly in your custom client configuration instead.");
		}
		optionMap.put(option, value);
	}

	public Object getOption(Option option) {
		if (optionMap.containsKey(option))
			return optionMap.get(option);
		return null;
	}

	public void refresh() {
		// Load timeouts
		Object connectionTimeout = this.getOption(Option.CONNECTION_TIMEOUT);
		if (connectionTimeout == null)
			connectionTimeout = CONNECTION_TIMEOUT;
		Object socketTimeout = this.getOption(Option.SOCKET_TIMEOUT);
		if (socketTimeout == null)
			socketTimeout = SOCKET_TIMEOUT;

		// Load limits
		Object maxTotal = this.getOption(Option.MAX_TOTAL);
		if (maxTotal == null)
			maxTotal = MAX_TOTAL;
		Object maxPerRoute = this.getOption(Option.MAX_PER_ROUTE);
		if (maxPerRoute == null)
			maxPerRoute = MAX_PER_ROUTE;

		// Load proxy if set
		HttpHost proxy = (HttpHost) this.getOption(Option.PROXY);

		// Create common default configuration
		RequestConfig clientConfig = RequestConfig.custom().setConnectTimeout(((Long) connectionTimeout).intValue()).setSocketTimeout(((Long) socketTimeout).intValue()).setConnectionRequestTimeout(((Long) socketTimeout).intValue()).setProxy(proxy).build();

		PoolingHttpClientConnectionManager syncConnectionManager = new PoolingHttpClientConnectionManager();
		syncConnectionManager.setMaxTotal((Integer) maxTotal);
		syncConnectionManager.setDefaultMaxPerRoute((Integer) maxPerRoute);

		// Create clients
		setOption(Option.HTTPCLIENT, HttpClientBuilder.create().setDefaultRequestConfig(clientConfig).setConnectionManager(syncConnectionManager).build());
		SyncIdleConnectionMonitorThread syncIdleConnectionMonitorThread = new SyncIdleConnectionMonitorThread(syncConnectionManager);
		setOption(Option.SYNC_MONITOR, syncIdleConnectionMonitorThread);
		syncIdleConnectionMonitorThread.start();

		DefaultConnectingIOReactor ioreactor;
		PoolingNHttpClientConnectionManager asyncConnectionManager;
		try {
			ioreactor = new DefaultConnectingIOReactor();
			asyncConnectionManager = new PoolingNHttpClientConnectionManager(ioreactor);
			asyncConnectionManager.setMaxTotal((Integer) maxTotal);
			asyncConnectionManager.setDefaultMaxPerRoute((Integer) maxPerRoute);
		} catch (IOReactorException e) {
			throw new RuntimeException(e);
		}

		CloseableHttpAsyncClient asyncClient = HttpAsyncClientBuilder.create().setDefaultRequestConfig(clientConfig).setConnectionManager(asyncConnectionManager).build();
		setOption(Option.ASYNCHTTPCLIENT, asyncClient);
		setOption(Option.ASYNC_MONITOR, new AsyncIdleConnectionMonitorThread(asyncConnectionManager));
	}

}
