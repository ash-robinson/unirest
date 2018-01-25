package com.mashape.unirest.test.arr;

import com.arr.rest.*;
import com.arr.rest.CUnirest;
import com.arr.rest.CUnirestFactory;

import org.apache.http.*;
import org.junit.*;
import org.mockserver.client.proxy.*;
import org.mockserver.client.server.*;
import org.mockserver.integration.*;
import org.mockserver.junit.*;
import org.mockserver.model.*;
import org.mockserver.verify.*;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.options.*;
import com.mashape.unirest.request.*;
import com.mashape.unirest.request.HttpRequest;

public class CMockServerProxyTest
{
	private ClientAndProxy proxy;
	private ClientAndServer mockServer;

	private int statusCode = 200;
	private String body = "chainsaw charlie";
	
	@Before
	public void startProxy() {
	    mockServer = ClientAndServer.startClientAndServer(9080);
	    proxy = ClientAndProxy.startClientAndProxy(9000);

		addBasicRules();
	}
	
	@After
	public void stopProxy() {
	    proxy.stop();
	    mockServer.stop();
	}
	
	private void addBasicRules()
	{
		//Forwarding rule for the proxy server
		proxy.when(org.mockserver.model.HttpRequest.request("/[a-zA-Z0-9]+"))
				.forward(HttpForward.forward().withHost("127.0.0.1").withPort(9080));
		
		//server response/s

		mockServer.when(org.mockserver.model.HttpRequest.request().withHeader("hooligan", "back"))
			.respond(org.mockserver.model.HttpResponse.response(body).withStatusCode(666));
	
		
		mockServer.when(org.mockserver.model.HttpRequest.request("/[a-zA-Z0-9]+"))
						.respond(org.mockserver.model.HttpResponse.response(body).withStatusCode(statusCode));
	}
	
	@Test
	public void test() throws Exception
	{				
		CUnirest unirest1 = new CUnirestFactory().getStandardInstance();
		unirest1.getClient().setDefaultHeader("hooligan", "back");
		HttpResponse<String> resp1 = unirest1.getClient().get("http://127.0.0.1:9000/api1")
											.asString();		
		Assert.assertEquals(666, resp1.getStatus());
		Assert.assertEquals(body, resp1.getBody());
		proxy.verify(org.mockserver.model.HttpRequest.request("/[a-zA-Z0-9]+"), VerificationTimes.once());
		mockServer.verify(org.mockserver.model.HttpRequest.request("/[a-zA-Z0-9]+"), VerificationTimes.once());
		
		CUnirest unirest2 = new CUnirestFactory().getCustomTimeoutInstance(1000000, 1000000);
		HttpResponse<String> resp2 = unirest2.getClient().get("http://127.0.0.1:9000/api2").asString();		
		Assert.assertEquals(200, resp2.getStatus());
		Assert.assertEquals(body, resp2.getBody());
		proxy.verify(org.mockserver.model.HttpRequest.request("/[a-zA-Z0-9]+"), VerificationTimes.exactly(2));
		mockServer.verify(org.mockserver.model.HttpRequest.request("/[a-zA-Z0-9]+"), VerificationTimes.exactly(2));
		
		HttpResponse<String> resp3 = unirest1.getClient().get("http://127.0.0.1:9000/api3").asString();		
		Assert.assertEquals(666, resp3.getStatus());
		Assert.assertEquals(body, resp3.getBody());
		proxy.verify(org.mockserver.model.HttpRequest.request("/[a-zA-Z0-9]+"), VerificationTimes.exactly(3));
		mockServer.verify(org.mockserver.model.HttpRequest.request("/[a-zA-Z0-9]+"), VerificationTimes.exactly(3));
		
	}
}
