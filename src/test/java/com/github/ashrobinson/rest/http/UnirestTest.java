package com.github.ashrobinson.rest.http;

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

import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.apache.commons.io.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.nio.client.*;
import org.junit.*;

import com.github.ashrobinson.rest.helper.*;
import com.github.ashrobinson.rest.http.*;
import com.github.ashrobinson.rest.http.async.*;
import com.github.ashrobinson.rest.http.exceptions.*;
import com.github.ashrobinson.rest.http.options.*;
import com.github.ashrobinson.rest.request.*;
import com.github.ashrobinson.rest.unirest.*;
import com.google.gson.*;

public class UnirestTest {

	private CountDownLatch lock;
	private boolean status;
	private UnirestBuilder unirest = null;
	
	@Before
	public void setUp() {
		lock = new CountDownLatch(1);
		status = false;
		unirest = UnirestFactory.getStandardInstance();
	}

	private String findAvailableIpAddress() throws UnknownHostException, IOException {
		for (int i = 100; i <= 255; i++) {
			String ip = "192.168.1." + i;
			if (!InetAddress.getByName(ip).isReachable(1000)) {
				return ip;
			}
		}

		throw new RuntimeException("Couldn't find an available IP address in the range of 192.168.0.100-255");
	}

	@Test
	public void testRequests() throws UnirestException {
		HttpResponse<JsonElement> JsonResponse = unirest.getClient().post("http://httpbin.org/post").header("accept", "application/Json").field("param1", "value1").field("param2", "bye").asJson();

		assertTrue(JsonResponse.getHeaders().size() > 0);
		assertTrue(JsonResponse.getBody().toString().length() > 0);
		assertFalse(JsonResponse.getRawBody() == null);
		assertEquals(200, JsonResponse.getStatus());

		JsonElement Json = JsonResponse.getBody();
		assertFalse(Json.isJsonArray());
		assertNotNull(Json.getAsJsonObject());
		assertNotNull(Json.getAsJsonArray());
		assertEquals(1, Json.getAsJsonArray().size());
		assertNotNull(Json.getAsJsonArray().get(0));
	}

	@Test
	public void testGet() throws UnirestException {
		HttpResponse<JsonElement> response = unirest.getClient().get("http://httpbin.org/get?name=mark").asJson();
		assertEquals(response.getBody().getAsJsonObject().get("args").getAsJsonObject().get("name"), "mark");

		response = unirest.getClient().get("http://httpbin.org/get").queryString("name", "mark2").asJson();
		assertEquals(response.getBody().getAsJsonObject().get("args").getAsJsonObject().get("name"), "mark2");
	}

	@Test
	public void testGetUTF8() throws UnirestException {
		HttpResponse<JsonElement> response = unirest.getClient().get("http://httpbin.org/get").queryString("param3", "こんにちは").asJson();

		assertEquals(response.getBody().getAsJsonObject().get("args").getAsJsonObject().get("param3"), "こんにちは");
	}

	@Test
	public void testPostUTF8() throws UnirestException {
		HttpResponse<JsonElement> response = unirest.getClient().post("http://httpbin.org/post").field("param3", "こんにちは").asJson();

		assertEquals(response.getBody().getAsJsonObject().get("form").getAsJsonObject().get("param3"), "こんにちは");
	}

	@Test
	public void testPostBinaryUTF8() throws UnirestException, URISyntaxException {
		HttpResponse<JsonElement> response = unirest.getClient().post("http://httpbin.org/post").field("param3", "こんにちは").field("file", new File(getClass().getResource("/test").toURI())).asJson();

		assertEquals("This is a test file", response.getBody().getAsJsonObject().get("files").getAsJsonObject().get("file").getAsString());
		assertEquals("こんにちは", response.getBody().getAsJsonObject().get("form").getAsJsonObject().get("param3").getAsString());
	}

	@Test
	public void testPostRawBody() throws UnirestException, URISyntaxException, IOException {
		String sourceString = "'\"@こんにちは-test-123-" + Math.random();
		byte[] sentBytes = sourceString.getBytes();

		HttpResponse<JsonElement> response = unirest.getClient().post("http://httpbin.org/post").body(sentBytes).asJson();

		assertEquals(sourceString, response.getBody().getAsJsonObject().get("data").getAsString());
	}

	@Test
	public void testCustomUserAgent() throws UnirestException {
		HttpResponse<JsonElement> response = unirest.getClient().get("http://httpbin.org/get?name=mark").header("user-agent", "hello-world").asJson();
		assertEquals("hello-world", response.getBody().getAsJsonObject().get("headers").getAsJsonObject().get("User-Agent").getAsString());

		GetRequest getRequest = unirest.getClient().get("http");
		for (Object current : Arrays.asList(0, 1, 2)) {
			getRequest.queryString("name", current);
		}

	}

	@Test
	public void testGetMultiple() throws UnirestException {
		for (int i = 1; i <= 20; i++) {
			HttpResponse<JsonElement> response = unirest.getClient().get("http://httpbin.org/get?try=" + i).asJson();
			assertEquals(response.getBody().getAsJsonObject().get("args").getAsJsonObject().get("try").getAsString(), ((Integer) i).toString());
		}
	}

	@Test
	public void testGetFields() throws UnirestException {
		HttpResponse<JsonElement> response = unirest.getClient().get("http://httpbin.org/get").queryString("name", "mark").queryString("nick", "thefosk").asJson();
		assertEquals(response.getBody().getAsJsonObject().get("args").getAsJsonObject().get("name").getAsString(), "mark");
		assertEquals(response.getBody().getAsJsonObject().get("args").getAsJsonObject().get("nick").getAsString(), "thefosk");
	}

	@Test
	public void testGetFields2() throws UnirestException {
		HttpResponse<JsonElement> response = unirest.getClient().get("http://httpbin.org/get").queryString("email", "hello@hello.com").asJson();
		assertEquals("hello@hello.com", response.getBody().getAsJsonObject().get("args").getAsJsonObject().get("email").getAsString());
	}

	@Test
	public void testQueryStringEncoding() throws UnirestException {
		String testKey = "email2=someKey&email";
		String testValue = "hello@hello.com";
		HttpResponse<JsonElement> response = unirest.getClient().get("http://httpbin.org/get").queryString(testKey, testValue).asJson();
		assertEquals(testValue, response.getBody().getAsJsonObject().get("args").getAsJsonObject().get(testKey).getAsString());
	}

	@Test
	public void testDelete() throws UnirestException {
		HttpResponse<JsonElement> response = unirest.getClient().delete("http://httpbin.org/delete").asJson();
		assertEquals(200, response.getStatus());

		response = unirest.getClient().delete("http://httpbin.org/delete").field("name", "mark").asJson();
		assertEquals("mark", response.getBody().getAsJsonObject().get("form").getAsJsonObject().get("name").getAsString());
	}

	@Test
	public void testDeleteBody() throws UnirestException {
		String body = "{\"JsonString\":{\"members\":\"members1\"}}";
		HttpResponse<JsonElement> response = unirest.getClient().delete("http://httpbin.org/delete").body(body).asJson();
		assertEquals(200, response.getStatus());
		assertEquals(body, response.getBody().getAsJsonObject().get("data").getAsString());
	}

	@Test
	public void testBasicAuth() throws UnirestException {
		HttpResponse<JsonElement> response = unirest.getClient().get("http://httpbin.org/headers").basicAuth("user", "test").asJson();
		assertEquals("Basic dXNlcjp0ZXN0", response.getBody().getAsJsonObject().get("headers").getAsJsonObject().get("Authorization").getAsString());
	}

	@Test
	public void testAsync() throws InterruptedException, ExecutionException, UnirestException {
		Future<HttpResponse<JsonElement>> future = unirest.getClient().post("http://httpbin.org/post").header("accept", "application/Json").field("param1", "value1").field("param2", "bye").asJsonAsync();

		assertNotNull(future);
		HttpResponse<JsonElement> JsonResponse = future.get();

		assertTrue(JsonResponse.getHeaders().size() > 0);
		assertTrue(JsonResponse.getBody().toString().length() > 0);
		assertFalse(JsonResponse.getRawBody() == null);
		assertEquals(200, JsonResponse.getStatus());

		JsonElement Json = JsonResponse.getBody();
		assertFalse(Json.isJsonArray());
		assertNotNull(Json.getAsJsonObject());
		assertNotNull(Json.getAsJsonArray());
		assertEquals(1, Json.getAsJsonArray().size());
		assertNotNull(Json.getAsJsonArray().get(0));
	}

	@Test
	public void testAsyncCallback() throws InterruptedException, ExecutionException, UnirestException {
		unirest.getClient().post("http://httpbin.org/post").header("accept", "application/Json").field("param1", "value1").field("param2", "bye").asJsonAsync(new Callback<JsonElement>() {

			public void failed(UnirestException e) {
				fail();
			}

			public void completed(HttpResponse<JsonElement> JsonResponse) {
				assertTrue(JsonResponse.getHeaders().size() > 0);
				assertTrue(JsonResponse.getBody().toString().length() > 0);
				assertFalse(JsonResponse.getRawBody() == null);
				assertEquals(200, JsonResponse.getStatus());

				JsonElement Json = JsonResponse.getBody();
				assertFalse(Json.isJsonArray());
				assertNotNull(Json.getAsJsonObject());
				assertNotNull(Json.getAsJsonArray());
				assertEquals(1, Json.getAsJsonArray().size());
				assertNotNull(Json.getAsJsonArray().get(0));

				assertEquals("value1", Json.getAsJsonObject().get("form").getAsJsonObject().get("param1").getAsString());
				assertEquals("bye", Json.getAsJsonObject().get("form").getAsJsonObject().get("param2").getAsString());

				status = true;
				lock.countDown();
			}

			public void cancelled() {
				fail();
			}
		});

		lock.await(10, TimeUnit.SECONDS);
		assertTrue(status);
	}

	@Test
	public void testMultipart() throws  InterruptedException, ExecutionException, URISyntaxException, UnirestException {
		HttpResponse<JsonElement> JsonResponse = unirest.getClient().post("http://httpbin.org/post").field("name", "Mark").field("file", new File(getClass().getResource("/test").toURI())).asJson();
		assertTrue(JsonResponse.getHeaders().size() > 0);
		assertTrue(JsonResponse.getBody().toString().length() > 0);
		assertFalse(JsonResponse.getRawBody() == null);
		assertEquals(200, JsonResponse.getStatus());

		JsonElement Json = JsonResponse.getBody();
		assertFalse(Json.isJsonArray());
		assertNotNull(Json.getAsJsonObject());
		assertNotNull(Json.getAsJsonArray());
		assertEquals(1, Json.getAsJsonArray().size());
		assertNotNull(Json.getAsJsonArray().get(0));
		assertNotNull(Json.getAsJsonObject().get("files").getAsJsonObject());

		assertEquals("This is a test file", Json.getAsJsonObject().get("files").getAsJsonObject().get("file").getAsString());
		assertEquals("Mark", Json.getAsJsonObject().get("form").getAsJsonObject().get("name").getAsString());
	}

	@Test
	public void testMultipartContentType() throws  InterruptedException, ExecutionException, URISyntaxException, UnirestException {
		HttpResponse<JsonElement> JsonResponse = unirest.getClient().post("http://httpbin.org/post").field("name", "Mark").field("file", new File(getClass().getResource("/image.jpg").toURI()), "image/jpeg").asJson();
		assertTrue(JsonResponse.getHeaders().size() > 0);
		assertTrue(JsonResponse.getBody().toString().length() > 0);
		assertFalse(JsonResponse.getRawBody() == null);
		assertEquals(200, JsonResponse.getStatus());

		JsonElement Json = JsonResponse.getBody();
		assertFalse(Json.isJsonArray());
		assertNotNull(Json.getAsJsonObject());
		assertNotNull(Json.getAsJsonArray());
		assertEquals(1, Json.getAsJsonArray().size());
		assertNotNull(Json.getAsJsonArray().get(0));
		assertNotNull(Json.getAsJsonObject().get("files").getAsJsonObject());

		assertTrue(Json.getAsJsonObject().get("files").getAsJsonObject().get("file").getAsString().contains("data:image/jpeg"));
		assertEquals("Mark", Json.getAsJsonObject().get("form").getAsJsonObject().get("name").getAsString());
	}

	@Test
	public void testMultipartInputStreamContentType() throws  InterruptedException, ExecutionException, URISyntaxException, UnirestException, FileNotFoundException {
		HttpResponse<JsonElement> JsonResponse = unirest.getClient().post("http://httpbin.org/post").field("name", "Mark").field("file", new FileInputStream(new File(getClass().getResource("/image.jpg").toURI())), ContentType.APPLICATION_OCTET_STREAM, "image.jpg").asJson();
		assertTrue(JsonResponse.getHeaders().size() > 0);
		assertTrue(JsonResponse.getBody().toString().length() > 0);
		assertFalse(JsonResponse.getRawBody() == null);
		assertEquals(200, JsonResponse.getStatus());

		JsonElement Json = JsonResponse.getBody();
		assertFalse(Json.isJsonArray());
		assertNotNull(Json.getAsJsonObject());
		assertNotNull(Json.getAsJsonArray());
		assertEquals(1, Json.getAsJsonArray().size());
		assertNotNull(Json.getAsJsonArray().get(0));
		assertNotNull(Json.getAsJsonObject().get("files").getAsJsonObject());

		assertTrue(Json.getAsJsonObject().get("files").getAsJsonObject().get("file").getAsString().contains("data:application/octet-stream"));
		assertEquals("Mark", Json.getAsJsonObject().get("form").getAsJsonObject().get("name").getAsString());
	}

	@Test
	public void testMultipartInputStreamContentTypeAsync() throws  InterruptedException, ExecutionException, URISyntaxException, UnirestException, FileNotFoundException {
		unirest.getClient().post("http://httpbin.org/post").field("name", "Mark").field("file", new FileInputStream(new File(getClass().getResource("/test").toURI())), ContentType.APPLICATION_OCTET_STREAM, "test").asJsonAsync(new Callback<JsonElement>() {

			public void failed(UnirestException e) {
				fail();
			}

			public void completed(HttpResponse<JsonElement> response) {
				assertTrue(response.getHeaders().size() > 0);
				assertTrue(response.getBody().toString().length() > 0);
				assertFalse(response.getRawBody() == null);
				assertEquals(200, response.getStatus());

				JsonElement Json = response.getBody();
				assertFalse(Json.isJsonArray());
				assertNotNull(Json.getAsJsonObject());
				assertNotNull(Json.getAsJsonArray());
				assertEquals(1, Json.getAsJsonArray().size());
				assertNotNull(Json.getAsJsonArray().get(0));

				assertEquals("This is a test file", Json.getAsJsonObject().get("files").getAsJsonObject().get("file").getAsString());
				assertEquals("Mark", Json.getAsJsonObject().get("form").getAsJsonObject().get("name").getAsString());

				status = true;
				lock.countDown();
			}

			public void cancelled() {
				fail();
			}

		});

		lock.await(10, TimeUnit.SECONDS);
		assertTrue(status);
	}

	@Test
	public void testMultipartByteContentType() throws  InterruptedException, ExecutionException, URISyntaxException, UnirestException, IOException {
		final InputStream stream = new FileInputStream(new File(getClass().getResource("/image.jpg").toURI()));
		final byte[] bytes = new byte[stream.available()];
		stream.read(bytes);
		stream.close();
		HttpResponse<JsonElement> JsonResponse = unirest.getClient().post("http://httpbin.org/post").field("name", "Mark").field("file", bytes, "image.jpg").asJson();
		assertTrue(JsonResponse.getHeaders().size() > 0);
		assertTrue(JsonResponse.getBody().toString().length() > 0);
		assertFalse(JsonResponse.getRawBody() == null);
		assertEquals(200, JsonResponse.getStatus());

		JsonElement Json = JsonResponse.getBody();
		assertFalse(Json.isJsonArray());
		assertNotNull(Json.getAsJsonObject());
		assertNotNull(Json.getAsJsonArray());
		assertEquals(1, Json.getAsJsonArray().size());
		assertNotNull(Json.getAsJsonArray().get(0));
		assertNotNull(Json.getAsJsonObject().get("files").getAsJsonObject());

		assertTrue(Json.getAsJsonObject().get("files").getAsJsonObject().get("file").getAsString().contains("data:application/octet-stream"));
		assertEquals("Mark", Json.getAsJsonObject().get("form").getAsJsonObject().get("name").getAsString());
	}

	@Test
	public void testMultipartByteContentTypeAsync() throws  InterruptedException, ExecutionException, URISyntaxException, UnirestException, IOException {
		final InputStream stream = new FileInputStream(new File(getClass().getResource("/test").toURI()));
		final byte[] bytes = new byte[stream.available()];
		stream.read(bytes);
		stream.close();
		unirest.getClient().post("http://httpbin.org/post").field("name", "Mark").field("file", bytes, "test").asJsonAsync(new Callback<JsonElement>() {

			public void failed(UnirestException e) {
				fail();
			}

			public void completed(HttpResponse<JsonElement> response) {
				assertTrue(response.getHeaders().size() > 0);
				assertTrue(response.getBody().toString().length() > 0);
				assertFalse(response.getRawBody() == null);
				assertEquals(200, response.getStatus());

				JsonElement Json = response.getBody();
				assertFalse(Json.isJsonArray());
				assertNotNull(Json.getAsJsonObject());
				assertNotNull(Json.getAsJsonArray());
				assertEquals(1, Json.getAsJsonArray().size());
				assertNotNull(Json.getAsJsonArray().get(0));

				assertEquals("This is a test file", Json.getAsJsonObject().get("files").getAsJsonObject().get("file").getAsString());
				assertEquals("Mark", Json.getAsJsonObject().get("form").getAsJsonObject().get("name").getAsString());

				status = true;
				lock.countDown();
			}

			public void cancelled() {
				fail();
			}

		});

		lock.await(10, TimeUnit.SECONDS);
		assertTrue(status);
	}

	@Test
	public void testMultipartAsync() throws  InterruptedException, ExecutionException, URISyntaxException, UnirestException {
		unirest.getClient().post("http://httpbin.org/post").field("name", "Mark").field("file", new File(getClass().getResource("/test").toURI())).asJsonAsync(new Callback<JsonElement>() {

			public void failed(UnirestException e) {
				fail();
			}

			public void completed(HttpResponse<JsonElement> response) {
				assertTrue(response.getHeaders().size() > 0);
				assertTrue(response.getBody().toString().length() > 0);
				assertFalse(response.getRawBody() == null);
				assertEquals(200, response.getStatus());

				JsonElement Json = response.getBody();
				assertFalse(Json.isJsonArray());
				assertNotNull(Json.getAsJsonObject());
				assertNotNull(Json.getAsJsonArray());
				assertEquals(1, Json.getAsJsonArray().size());
				assertNotNull(Json.getAsJsonArray().get(0));

				assertEquals("This is a test file", Json.getAsJsonObject().get("files").getAsJsonObject().get("file").getAsString());
				assertEquals("Mark", Json.getAsJsonObject().get("form").getAsJsonObject().get("name").getAsString());

				status = true;
				lock.countDown();
			}

			public void cancelled() {
				fail();
			}

		});

		lock.await(10, TimeUnit.SECONDS);
		assertTrue(status);
	}

	@Test
	public void testGzip() throws UnirestException {
		HttpResponse<JsonElement> JsonResponse = unirest.getClient().get("http://httpbin.org/gzip").asJson();
		assertTrue(JsonResponse.getHeaders().size() > 0);
		assertTrue(JsonResponse.getBody().toString().length() > 0);
		assertFalse(JsonResponse.getRawBody() == null);
		assertEquals(200, JsonResponse.getStatus());

		JsonElement Json = JsonResponse.getBody();
		assertFalse(Json.isJsonArray());
		assertTrue(Json.getAsJsonObject().get("gzipped").getAsBoolean());
	}

	@Test
	public void testGzipAsync() throws UnirestException,  InterruptedException, ExecutionException {
		HttpResponse<JsonElement> JsonResponse = unirest.getClient().get("http://httpbin.org/gzip").asJsonAsync().get();
		assertTrue(JsonResponse.getHeaders().size() > 0);
		assertTrue(JsonResponse.getBody().toString().length() > 0);
		assertFalse(JsonResponse.getRawBody() == null);
		assertEquals(200, JsonResponse.getStatus());

		JsonElement Json = JsonResponse.getBody();
		assertFalse(Json.isJsonArray());
		assertTrue(Json.getAsJsonObject().get("gzipped").getAsBoolean());
	}

	@Test
	public void testDefaultHeaders() throws UnirestException {
		unirest.getClient().setDefaultHeader("X-Custom-Header", "hello");
		unirest.getClient().setDefaultHeader("user-agent", "foobar");

		HttpResponse<JsonElement> JsonResponse = unirest.getClient().get("http://httpbin.org/headers").asJson();
		assertTrue(JsonResponse.getHeaders().size() > 0);
		assertTrue(JsonResponse.getBody().toString().length() > 0);
		assertFalse(JsonResponse.getRawBody() == null);
		assertEquals(200, JsonResponse.getStatus());

		JsonElement Json = JsonResponse.getBody();
		assertFalse(Json.isJsonArray());
		assertTrue(JsonResponse.getBody().getAsJsonObject().get("headers").getAsJsonObject().has("X-Custom-Header"));
		assertEquals("hello", Json.getAsJsonObject().get("headers").getAsJsonObject().get("X-Custom-Header").getAsString());
		assertTrue(JsonResponse.getBody().getAsJsonObject().get("headers").getAsJsonObject().has("User-Agent"));
		assertEquals("foobar", Json.getAsJsonObject().get("headers").getAsJsonObject().get("User-Agent").getAsString());

		JsonResponse = unirest.getClient().get("http://httpbin.org/headers").asJson();
		assertTrue(JsonResponse.getBody().getAsJsonObject().get("headers").getAsJsonObject().has("X-Custom-Header"));
		assertEquals("hello", JsonResponse.getBody().getAsJsonObject().get("headers").getAsJsonObject().get("X-Custom-Header").getAsString());

		unirest.getClient().clearDefaultHeaders();

		JsonResponse = unirest.getClient().get("http://httpbin.org/headers").asJson();
		assertFalse(JsonResponse.getBody().getAsJsonObject().get("headers").getAsJsonObject().has("X-Custom-Header"));
	}

	@Test
	public void testSetTimeouts() throws UnknownHostException, IOException {
		String address = "http://" + findAvailableIpAddress() + "/";
		long start = System.currentTimeMillis();
		try {
			unirest.getClient().get("http://" + address + "/").asString();
		} catch (Exception e) {
			if (System.currentTimeMillis() - start > Options.CONNECTION_TIMEOUT + 100) { // Add 100ms for code execution
				fail();
			}
		}
		unirest.getClient().setTimeouts(2000, 10000);
		start = System.currentTimeMillis();
		try {
			unirest.getClient().get("http://" + address + "/").asString();
		} catch (Exception e) {
			if (System.currentTimeMillis() - start > 2100) { // Add 100ms for code execution
				fail();
			}
		}
	}

	@Test
	public void testPathParameters() throws UnirestException {
		HttpResponse<JsonElement> JsonResponse = unirest.getClient().get("http://httpbin.org/{method}").routeParam("method", "get").queryString("name", "Mark").asJson();

		assertEquals(200, JsonResponse.getStatus());
		assertEquals(JsonResponse.getBody().getAsJsonObject().get("args").getAsJsonObject().get("name").getAsString(), "Mark");
	}

	@Test
	public void testQueryAndBodyParameters() throws UnirestException {
		HttpResponse<JsonElement> JsonResponse = unirest.getClient().post("http://httpbin.org/{method}").routeParam("method", "post").queryString("name", "Mark").field("wot", "wat").asJson();

		assertEquals(200, JsonResponse.getStatus());
		assertEquals(JsonResponse.getBody().getAsJsonObject().get("args").getAsJsonObject().get("name").getAsString(), "Mark");
		assertEquals(JsonResponse.getBody().getAsJsonObject().get("form").getAsJsonObject().get("wot").getAsString(), "wat");
	}

	@Test
	public void testPathParameters2() throws UnirestException {
		HttpResponse<JsonElement> JsonResponse = unirest.getClient().patch("http://httpbin.org/{method}").routeParam("method", "patch").field("name", "Mark").asJson();

		assertEquals(200, JsonResponse.getStatus());
		assertEquals("OK", JsonResponse.getStatusText());
		assertEquals(JsonResponse.getBody().getAsJsonObject().get("form").getAsJsonObject().get("name").getAsString(), "Mark");
	}

	@Test
	public void testMissingPathParameter() throws UnirestException {
		try {
			unirest.getClient().get("http://httpbin.org/{method}").routeParam("method222", "get").queryString("name", "Mark").asJson();
			fail();
		} catch (RuntimeException e) {
			// OK
		}
	}

	@Test
	public void parallelTest() throws InterruptedException {
		unirest.getClient().setConcurrency(10, 5);

		long start = System.currentTimeMillis();
		makeParallelRequests();
		long smallerConcurrencyTime = (System.currentTimeMillis() - start);

		unirest.getClient().setConcurrency(200, 20);
		start = System.currentTimeMillis();
		makeParallelRequests();
		long higherConcurrencyTime = (System.currentTimeMillis() - start);

		assertTrue(higherConcurrencyTime < smallerConcurrencyTime);
	}

	private void makeParallelRequests() throws InterruptedException {
		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(10);
		final AtomicInteger counter = new AtomicInteger(0);
		for (int i = 0; i < 200; i++) {
			newFixedThreadPool.execute(new Runnable() {
				public void run() {
					try {
						unirest.getClient().get("http://httpbin.org/get").queryString("index", counter.incrementAndGet()).asJson();
					} catch (UnirestException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}

		newFixedThreadPool.shutdown();
		newFixedThreadPool.awaitTermination(10, TimeUnit.MINUTES);
	}

	@Test
	public void testAsyncCustomContentType() throws InterruptedException, UnirestException {
		unirest.getClient().post("http://httpbin.org/post").header("accept", "application/Json").header("Content-Type", "application/Json").body("{\"hello\":\"world\"}").asJsonAsync(new Callback<JsonElement>() {

			public void failed(UnirestException e) {
				fail();
			}

			public void completed(HttpResponse<JsonElement> JsonResponse) {
				JsonElement Json = JsonResponse.getBody();
				assertEquals("{\"hello\":\"world\"}", Json.getAsJsonObject().get("data").getAsString());
				assertEquals("application/Json", Json.getAsJsonObject().get("headers").getAsJsonObject().get("Content-Type").getAsString());

				status = true;
				lock.countDown();
			}

			public void cancelled() {
				fail();
			}
		});

		lock.await(10, TimeUnit.SECONDS);
		assertTrue(status);
	}

	@Test
	public void testAsyncCustomContentTypeAndFormParams() throws InterruptedException, UnirestException {
		unirest.getClient().post("http://httpbin.org/post").header("accept", "application/Json").header("Content-Type", "application/x-www-form-urlencoded").field("name", "Mark").field("hello", "world").asJsonAsync(new Callback<JsonElement>() {

			public void failed(UnirestException e) {
				fail();
			}

			public void completed(HttpResponse<JsonElement> JsonResponse) {
				JsonElement Json = JsonResponse.getBody();
				assertEquals("Mark", Json.getAsJsonObject().get("form").getAsJsonObject().get("name").getAsString());
				assertEquals("world", Json.getAsJsonObject().get("form").getAsJsonObject().get("hello").getAsString());

				assertEquals("application/x-www-form-urlencoded", Json.getAsJsonObject().get("headers").getAsJsonObject().get("Content-Type").getAsString());

				status = true;
				lock.countDown();
			}

			public void cancelled() {
				fail();
			}
		});

		lock.await(10, TimeUnit.SECONDS);
		assertTrue(status);
	}

	@Test
	public void testGetQuerystringArray() throws  UnirestException {
		HttpResponse<JsonElement> response = unirest.getClient().get("http://httpbin.org/get").queryString("name", "Mark").queryString("name", "Tom").asJson();

		JsonArray names = response.getBody().getAsJsonObject().get("args").getAsJsonObject().get("name").getAsJsonArray();
		assertEquals(2, names.size());

		assertEquals("Mark", names.get(0).getAsString());
		assertEquals("Tom", names.get(1).getAsString());
	}

	@Test
	public void testPostMultipleFiles() throws  UnirestException, URISyntaxException {
		HttpResponse<JsonElement> response = unirest.getClient().post("http://httpbin.org/post").field("param3", "wot").field("file1", new File(getClass().getResource("/test").toURI())).field("file2", new File(getClass().getResource("/test").toURI())).asJson();

		JsonObject names = response.getBody().getAsJsonObject().get("files").getAsJsonObject();
		assertEquals(2, names.size());

		assertEquals("This is a test file", names.get("file1").getAsString());
		assertEquals("This is a test file", names.get("file2").getAsString());

		assertEquals("wot", response.getBody().getAsJsonObject().get("form").getAsJsonObject().get("param3").getAsString());
	}

	@Test
	public void testGetArray() throws  UnirestException {
		HttpResponse<JsonElement> response = unirest.getClient().get("http://httpbin.org/get").queryString("name", Arrays.asList("Mark", "Tom")).asJson();

		JsonArray names = response.getBody().getAsJsonObject().get("args").getAsJsonObject().get("name").getAsJsonArray();
		assertEquals(2, names.size());

		assertEquals("Mark", names.get(0).getAsString());
		assertEquals("Tom", names.get(1).getAsString());
	}

	@Test
	public void testPostArray() throws UnirestException {
		HttpResponse<JsonElement> response = unirest.getClient().post("http://httpbin.org/post").field("name", Arrays.asList("Mark", "Tom")).asJson();

		JsonArray names = response.getBody().getAsJsonObject().get("form").getAsJsonObject().get("name").getAsJsonArray();
		assertEquals(2, names.size());

		assertEquals("Mark", names.get(0));
		assertEquals("Tom", names.get(1));
	}

	@Test
	public void testPostCollection() throws UnirestException {
		HttpResponse<JsonElement> response = unirest.getClient().post("http://httpbin.org/post").field("name", Arrays.asList("Mark", "Tom")).asJson();

		JsonArray names = response.getBody().getAsJsonObject().get("form").getAsJsonObject().get("name").getAsJsonArray();
		assertEquals(2, names.size());

		assertEquals("Mark", names.get(0));
		assertEquals("Tom", names.get(1));
	}

	@Test
	public void testCaseInsensitiveHeaders() throws UnirestException {
		GetRequest request = unirest.getClient().get("http://httpbin.org/headers").header("Name", "Marco");
		assertEquals(1, request.getHeaders().size());
		assertEquals("Marco", request.getHeaders().get("name").get(0));
		assertEquals("Marco", request.getHeaders().get("NAme").get(0));
		assertEquals("Marco", request.getHeaders().get("Name").get(0));
		JsonObject headers = request.asJson().getBody().getAsJsonObject().get("headers").getAsJsonObject();
		assertEquals("Marco", headers.get("Name"));

		request = unirest.getClient().get("http://httpbin.org/headers").header("Name", "Marco").header("Name", "John");
		assertEquals(1, request.getHeaders().size());
		assertEquals("Marco", request.getHeaders().get("name").get(0));
		assertEquals("John", request.getHeaders().get("name").get(1));
		assertEquals("Marco", request.getHeaders().get("NAme").get(0));
		assertEquals("John", request.getHeaders().get("NAme").get(1));
		assertEquals("Marco", request.getHeaders().get("Name").get(0));
		assertEquals("John", request.getHeaders().get("Name").get(1));

		headers = request.asJson().getBody().getAsJsonObject().get("headers").getAsJsonObject();
		assertEquals("Marco,John", headers.get("Name"));
	}

	@Test
	public void setTimeoutsAndCustomClient() {
		try {
			unirest.getClient().setTimeouts(1000, 2000);
		} catch (Exception e) {
			fail();
		}

		try {
			unirest.getClient().setAsyncHttpClient(HttpAsyncClientBuilder.create().build());
		} catch (Exception e) {
			fail();
		}

		try {
			unirest.getClient().setAsyncHttpClient(HttpAsyncClientBuilder.create().build());
			unirest.getClient().setTimeouts(1000, 2000);
			fail();
		} catch (Exception e) {
			// Ok
		}

		try {
			unirest.getClient().setHttpClient(HttpClientBuilder.create().build());
			unirest.getClient().setTimeouts(1000, 2000);
			fail();
		} catch (Exception e) {
			// Ok
		}
	}

	@Test
	public void testObjectMapperRead() throws UnirestException, IOException {
		unirest.getClient().setObjectMapper(new JacksonObjectMapper());

		GetResponse getResponseMock = new GetResponse();
		getResponseMock.setUrl("http://httpbin.org/get");

		HttpResponse<GetResponse> getResponse = unirest.getClient().get(getResponseMock.getUrl()).asObject(GetResponse.class);

		assertEquals(200, getResponse.getStatus());
		assertEquals(getResponse.getBody().getUrl(), getResponseMock.getUrl());
	}

	@Test
	public void testObjectMapperWrite() throws UnirestException, IOException {
		unirest.getClient().setObjectMapper(new JacksonObjectMapper());

		GetResponse postResponseMock = new GetResponse();
		postResponseMock.setUrl("http://httpbin.org/post");

		HttpResponse<JsonElement> postResponse = unirest.getClient().post(postResponseMock.getUrl()).header("accept", "application/Json")
				.header("Content-Type", "application/Json").body(postResponseMock.toString()).asJson();

		assertEquals(200, postResponse.getStatus());
		assertEquals(postResponse.getBody().getAsJsonObject().get("data"), "{\"url\":\"http://httpbin.org/post\"}");
	}

	@Test
	public void testPostProvidesSortedParams() throws IOException {
		// Verify that fields are encoded into the body in sorted order.
		HttpRequest httpRequest = unirest.getClient().post("test").field("z", "Z").field("y", "Y").field("x", "X").getHttpRequest();

		InputStream content = httpRequest.getBody().getEntity().getContent();
		String body = IOUtils.toString(content, "UTF-8");
		assertEquals("x=X&y=Y&z=Z", body);
	}

	@Test
	public void testHeaderNamesCaseSensitive() {
		// Verify that header names are the same as server (case sensitive)
		final Headers headers = new Headers();
		headers.put("Content-Type", Arrays.asList("application/Json"));

		assertEquals("Only header \"Content-Type\" should exist", null, headers.getFirst("cOnTeNt-TyPe"));
		assertEquals("Only header \"Content-Type\" should exist", null, headers.getFirst("content-type"));
		assertEquals("Only header \"Content-Type\" should exist", "application/Json", headers.getFirst("Content-Type"));
	}
}
