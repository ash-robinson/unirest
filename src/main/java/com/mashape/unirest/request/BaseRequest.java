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

package com.mashape.unirest.request;

import java.io.*;
import java.util.concurrent.*;

import com.google.gson.*;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.async.*;
import com.mashape.unirest.http.exceptions.*;
import com.mashape.unirest.http.options.*;

public abstract class BaseRequest {

	protected static final String UTF_8 = "UTF-8";

	protected HttpRequest httpRequest;
	protected Options options;
	protected HttpClientHelper httpClientHelper;

	protected BaseRequest(HttpRequest httpRequest) {
		this();
		this.httpRequest = httpRequest;
		this.options = httpRequest.getOptions();
	}
	
	protected BaseRequest(HttpRequest httpRequest, Options options) {
		this();
		this.httpRequest = httpRequest;
		this.options = options;
	}

	protected BaseRequest() {
		super();
		this.httpClientHelper = new HttpClientHelper();
	}
	
	public HttpRequest getHttpRequest() {
		return this.httpRequest;
	}
	
	public void setHttpRequest(HttpRequest request) {
		this.httpRequest = request;
	}	

	public HttpResponse<String> asString() throws UnirestException {
		return this.httpClientHelper.request(httpRequest, String.class, options);
	}

	public Future<HttpResponse<String>> asStringAsync() throws UnirestException {
		return httpClientHelper.requestAsync(httpRequest, String.class, null, options);
	}

	public Future<HttpResponse<String>> asStringAsync(Callback<String> callback) throws UnirestException {
		return httpClientHelper.requestAsync(httpRequest, String.class, callback, options);
	}

	public HttpResponse<JsonElement> asJson() throws UnirestException {
		return httpClientHelper.request(httpRequest, JsonElement.class, options);
	}

	public Future<HttpResponse<JsonElement>> asJsonAsync() throws UnirestException {
		return httpClientHelper.requestAsync(httpRequest, JsonElement.class, null, options);
	}

	public Future<HttpResponse<JsonElement>> asJsonAsync(Callback<JsonElement> callback) throws UnirestException {
		return httpClientHelper.requestAsync(httpRequest, JsonElement.class, callback, options);
	}

	public <T> HttpResponse<T> asObject(Class<? extends T> responseClass) throws UnirestException {
		return httpClientHelper.request(httpRequest, (Class) responseClass, options);
	}

	public <T> Future<HttpResponse<T>> asObjectAsync(Class<? extends T> responseClass) throws UnirestException {
		return httpClientHelper.requestAsync(httpRequest, (Class) responseClass, null, options);
	}

	public <T> Future<HttpResponse<T>> asObjectAsync(Class<? extends T> responseClass, Callback<T> callback) throws UnirestException {
		return httpClientHelper.requestAsync(httpRequest, (Class) responseClass, callback, options);
	}

	public HttpResponse<InputStream> asBinary() throws UnirestException {
		return httpClientHelper.request(httpRequest, InputStream.class, options);
	}

	public Future<HttpResponse<InputStream>> asBinaryAsync() throws UnirestException {
		return httpClientHelper.requestAsync(httpRequest, InputStream.class, null, options);
	}

	public Future<HttpResponse<InputStream>> asBinaryAsync(Callback<InputStream> callback) throws UnirestException {
		return httpClientHelper.requestAsync(httpRequest, InputStream.class, callback, options);
	}

}
