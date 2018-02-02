package com.mashape.unirest.test.arr;

import java.util.*;

import org.junit.*;

import com.google.gson.*;
import com.mashape.unirest.arr.*;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.*;

public class CQRadarTest
{
	/**
	 * fill in fields in {@link #setUp()} as appropriate
	 */	
	private static String username = null;
	private static String password = null;
	private static String trustStorePath = null;
	private static String trustStorePassword = null;
	private static String host = null;
	private static String protocol = null;
	private static String token = null;
	
	private static CUnirest unirest = null;
	
	private static void setProperties()
	{
		username = "admin";
		password = "Midiman1";
		trustStorePath = "cacerts";
		trustStorePassword = "changeit";
		host = "10.30.30.247";
		protocol = "https";
		token = "074c6009-140e-45fb-a308-1b6087481997";
	}
	
	@BeforeClass
	public static void setUp()
	{
		setProperties();
		//set appropriate cert
		System.setProperty("javax.net.ssl.trustStore", trustStorePath);
		System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
	}
	
	@Ignore
	@Test
	public void authTest() throws UnirestException
	{		
		unirest = CUnirestFactory.getStandardInstance();
		/**
		 * basic auth
		 */
		HttpResponse<String> test = unirest.getClient().get(protocol + "://" + host + 
				"/api/siem/offenses")
				.header("Accept", "application/json")
				.basicAuth("admin", "Midiman1")
				.asString();				
		/**
		 * sec token auth
		 */
		test = unirest.getClient().get(protocol + "://" + host + 
				"/api/siem/offenses")
				.header("Accept", "application/json")
				.header("SEC", token)
				.asString();		
	}
	
	@Test
	public void getOffenseTest() throws Exception
	{		
		unirest = CUnirestFactory.getStandardInstance();	
		
		HttpResponse<JsonElement> test = unirest.getClient().get(protocol + "://" + host + 
				"/api/siem/offenses/38")
				.header("Accept", "application/json")
				.header("SEC", token)
				.asJson();

		Assert.assertEquals(200, test.getStatus());
//		System.out.println("TEST: " + test.getBody());
	}
}
