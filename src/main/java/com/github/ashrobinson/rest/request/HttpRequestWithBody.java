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

package com.github.ashrobinson.rest.request;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.apache.http.entity.*;
import org.apache.http.entity.mime.content.*;

import com.github.ashrobinson.rest.http.*;
import com.github.ashrobinson.rest.http.options.*;
import com.github.ashrobinson.rest.request.body.*;

public class HttpRequestWithBody extends HttpRequest {

	public HttpRequestWithBody(HttpMethod method, String url, Options options) {
		super(method, url, options);
	}

	@Override
	public HttpRequestWithBody routeParam(String name, String value) {
		super.routeParam(name, value);
		return this;
	}

	@Override
	public HttpRequestWithBody header(String name, String value) {
		return (HttpRequestWithBody) super.header(name, value);
	}

	@Override
	public HttpRequestWithBody headers(Map<String, String> headers) {
		return (HttpRequestWithBody) super.headers(headers);
	}

	@Override
	public HttpRequestWithBody basicAuth(String username, String password) {
		super.basicAuth(username, password);
		return this;
	}

	@Override
	public HttpRequestWithBody queryString(Map<String, Object> parameters) {
		return (HttpRequestWithBody) super.queryString(parameters);
	}

	@Override
	public HttpRequestWithBody queryString(String name, Object value) {
		return (HttpRequestWithBody) super.queryString(name, value);
	}

	public MultipartBody field(String name, Collection<?> value) {
		MultipartBody body = new MultipartBody(this).field(name, value);
		this.body = body;
		return body;
	}

	public MultipartBody field(String name, Object value) {
		return field(name, value, null);
	}

	public MultipartBody field(String name, File file) {
		return field(name, file, null);
	}

	public MultipartBody field(String name, Object value, String contentType) {
		MultipartBody body = new MultipartBody(this).field(name, (value == null) ? "" : value.toString(), contentType);
		this.body = body;
		return body;
	}

	public MultipartBody field(String name, File file, String contentType) {
		MultipartBody body = new MultipartBody(this).field(name, file, contentType);
		this.body = body;
		return body;
	}

	public MultipartBody fields(Map<String, Object> parameters) {
		MultipartBody body = new MultipartBody(this);
		if (parameters != null) {
			for (Entry<String, Object> param : parameters.entrySet()) {
				if (param.getValue() instanceof File) {
					body.field(param.getKey(), (File) param.getValue());
				} else {
					body.field(param.getKey(), (param.getValue() == null) ? "" : param.getValue().toString());
				}
			}
		}
		this.body = body;
		return body;
	}

	public MultipartBody field(String name, InputStream stream, ContentType contentType, String fileName) {
		InputStreamBody inputStreamBody = new InputStreamBody(stream, contentType, fileName);
		MultipartBody body = new MultipartBody(this).field(name, inputStreamBody, true, contentType.toString());
		this.body = body;
		return body;
	}

	public MultipartBody field(String name, InputStream stream, String fileName) {
		MultipartBody body = field(name, stream, ContentType.APPLICATION_OCTET_STREAM, fileName);
		this.body = body;
		return body;
	}

	public RequestBodyEntity body(String body) {
		RequestBodyEntity b = new RequestBodyEntity(this).body(body);
		this.body = b;
		return b;
	}
	
	public RequestBodyEntity body(byte[] sentBytes) {
		RequestBodyEntity b = new RequestBodyEntity(this).body(sentBytes.toString());
		this.body = b;
		return b;
	}
}