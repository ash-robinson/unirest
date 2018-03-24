/*
The MIT License

Copyright (c) 2013 Mashape (http://mashape.com)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.mashape.unirest.http;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.concurrent.*;
import org.apache.http.impl.nio.client.*;
import org.apache.http.nio.entity.*;

import com.mashape.unirest.http.async.*;
import com.mashape.unirest.http.async.utils.*;
import com.mashape.unirest.http.exceptions.*;
import com.mashape.unirest.http.options.*;
import com.mashape.unirest.request.HttpRequest;

public class HttpClientHelper {

	private static final String CONTENT_TYPE = "content-type";
	private static final String ACCEPT_ENCODING_HEADER = "accept-encoding";
	private static final String USER_AGENT_HEADER = "user-agent";
	private static final String USER_AGENT = "unirest-java/1.3.11";

	public HttpClientHelper()
	{
		super();
	}
	
	private <T> FutureCallback<org.apache.http.HttpResponse> prepareCallback(final Class<T> responseClass, final Callback<T> callback, final Options options) {
		if (callback == null)
			return null;

		return new FutureCallback<org.apache.http.HttpResponse>() {

			public void cancelled() {
				callback.cancelled();
			}

			public void completed(org.apache.http.HttpResponse arg0) {
				callback.completed(new HttpResponse<T>(arg0, responseClass, options));
			}

			public void failed(Exception arg0) {
				callback.failed(new UnirestException(arg0));
			}

		};
	}

	public <T> Future<HttpResponse<T>> requestAsync(HttpRequest request, final Class<T> responseClass, Callback<T> callback, final Options options) throws UnirestException {
		HttpUriRequest requestObj = prepareRequest(request, true, options);

		CloseableHttpAsyncClient asyncHttpClient = (CloseableHttpAsyncClient) options.getOption(Option.ASYNCHTTPCLIENT);
		if (!asyncHttpClient.isRunning()) {
			asyncHttpClient.start();
			AsyncIdleConnectionMonitorThread asyncIdleConnectionMonitorThread = (AsyncIdleConnectionMonitorThread) options.getOption(Option.ASYNC_MONITOR);
			asyncIdleConnectionMonitorThread.start();
		}

		final Future<org.apache.http.HttpResponse> future = asyncHttpClient.execute(requestObj, prepareCallback(responseClass, callback, options));

		return new Future<HttpResponse<T>>() {

			public boolean cancel(boolean mayInterruptIfRunning) {
				return future.cancel(mayInterruptIfRunning);
			}

			public boolean isCancelled() {
				return future.isCancelled();
			}

			public boolean isDone() {
				return future.isDone();
			}

			public HttpResponse<T> get() throws InterruptedException, ExecutionException {
				org.apache.http.HttpResponse httpResponse = future.get();
				return new HttpResponse<T>(httpResponse, responseClass, options);
			}

			public HttpResponse<T> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				org.apache.http.HttpResponse httpResponse = future.get(timeout, unit);
				return new HttpResponse<T>(httpResponse, responseClass, options);
			}
		};
	}

	public <T> HttpResponse<T> request(HttpRequest request, Class<T> responseClass, final Options options) throws UnirestException {
		HttpRequestBase requestObj = prepareRequest(request, false, options);
		HttpClient client = (HttpClient) options.getOption(Option.HTTPCLIENT); 
															// The
															// DefaultHttpClient
															// is thread-safe
		org.apache.http.HttpResponse response;
		try {
			response = client.execute(requestObj);
			HttpResponse<T> httpResponse = new HttpResponse<T>(response, responseClass, options);
			requestObj.releaseConnection();
			return httpResponse;
		} catch (Exception e) {
			throw new UnirestException(e);
		} finally {
			requestObj.releaseConnection();
		}
	}

	private HttpRequestBase prepareRequest(HttpRequest request, boolean async, Options options) throws UnirestException {
		
		
		Object defaultHeaders = options.getOption(Option.DEFAULT_HEADERS);
		if (defaultHeaders != null) {
			@SuppressWarnings("unchecked")
			Set<Entry<String, String>> entrySet = ((Map<String, String>) defaultHeaders).entrySet();
			for (Entry<String, String> entry : entrySet) {
				request.header(entry.getKey(), entry.getValue());
			}
		}

		if (!request.getHeaders().containsKey(USER_AGENT_HEADER)) {
			request.header(USER_AGENT_HEADER, USER_AGENT);
		}
		if (!request.getHeaders().containsKey(ACCEPT_ENCODING_HEADER)) {
			request.header(ACCEPT_ENCODING_HEADER, "gzip");
		}

		HttpRequestBase reqObj = null;

		String urlToRequest = null;
		try {
			URL url = new URL(request.getUrl());
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), URLDecoder.decode(url.getPath(), "UTF-8"), "", url.getRef());
			urlToRequest = uri.toURL().toString();
			if (url.getQuery() != null && !url.getQuery().trim().equals("")) {
				if (!urlToRequest.substring(urlToRequest.length() - 1).equals("?")) {
					urlToRequest += "?";
				}
				urlToRequest += url.getQuery();
			} else if (urlToRequest.substring(urlToRequest.length() - 1).equals("?")) {
				urlToRequest = urlToRequest.substring(0, urlToRequest.length() - 1);
			}
		} 
		catch (Exception e) 
		{
			throw new UnirestException(e);
		}

		switch (request.getHttpMethod()) {
		case GET:
			reqObj = new HttpGet(urlToRequest);
			break;
		case POST:
			reqObj = new HttpPost(urlToRequest);
			break;
		case PUT:
			reqObj = new HttpPut(urlToRequest);
			break;
		case DELETE:
			reqObj = new HttpDeleteWithBody(urlToRequest);
			break;
		case PATCH:
			reqObj = new HttpPatchWithBody(urlToRequest);
			break;
		case OPTIONS:
			reqObj = new HttpOptions(urlToRequest);
			break;
		case HEAD:
			reqObj = new HttpHead(urlToRequest);
			break;
		}

		Set<Entry<String, List<String>>> entrySet = request.getHeaders().entrySet();
		for (Entry<String, List<String>> entry : entrySet) {
			List<String> values = entry.getValue();
			if (values != null) {
				for (String value : values) {
					reqObj.addHeader(entry.getKey(), value);
				}
			}
		}

		// Set body
		if (!(request.getHttpMethod() == HttpMethod.GET || request.getHttpMethod() == HttpMethod.HEAD)) {
			if (request.getBody() != null) {
				HttpEntity entity = request.getBody().getEntity();
				if (async) {
					if (reqObj.getHeaders(CONTENT_TYPE) == null || reqObj.getHeaders(CONTENT_TYPE).length == 0) {
						reqObj.setHeader(entity.getContentType());
					}
					try {
						ByteArrayOutputStream output = new ByteArrayOutputStream();
						entity.writeTo(output);
						NByteArrayEntity en = new NByteArrayEntity(output.toByteArray());
						((HttpEntityEnclosingRequestBase) reqObj).setEntity(en);
					} catch (IOException e) {
						throw new UnirestException(e);
					}
				} else {
					((HttpEntityEnclosingRequestBase) reqObj).setEntity(entity);
				}
			}
		}

		return reqObj;
	}

}
