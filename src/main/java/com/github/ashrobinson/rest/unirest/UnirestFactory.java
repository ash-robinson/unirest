package com.github.ashrobinson.rest.unirest;

import java.io.*;
import java.util.*;

/**
 * Factory for CUnirest class
 * @author ARR
 */
public class UnirestFactory implements Serializable
{		
	/**
	 * Returns a standard SSL enabled client 
	 * 
	 * @return a standard SSL enabled client 
	 */
	public static UnirestInstance getStandardInstance()
	{
		UnirestInstance unirest = new UnirestInstance();
		unirest.InitSSLClient();
		return unirest;
	}
	
	/**
	 * Returns an SSL enabled client with custom timeouts
	 * 
	 * @param connectionTimeout connection timeout override for client
	 * @param socketTimeout socket timeout override for client
	 * @return an SSL enabled client with custom timeouts
	 */
	public static UnirestInstance getCustomTimeoutInstance(final long connectionTimeout, final long socketTimeout)
	{
		UnirestInstance unirest = new UnirestInstance();
		unirest.InitCustomTimeoutSSLClient(connectionTimeout, socketTimeout);
		return unirest;
	}
	
	/**
	 * Returns an SSL enabled client with standard proxy
	 * 
	 * @param host the proxy host address
	 * @param port the port to connect to via proxy
	 * @return an SSL enabled client with standard proxy
	 */
	public static UnirestInstance getProxyInstance(final String host, final int port)
	{
		UnirestInstance unirest = new UnirestInstance();
		unirest.InitProxy(host, port);
		return unirest;
	}
	
	/**
	 * Return an SSL enabled client with authenticated proxy
	 * 
	 * @param username username for proxy authentication
	 * @param password password for proxy authentication
	 * @param host the proxy host address
	 * @param port the port to connect to via proxy
	 * @return an SSL enabled client with authenticated proxy
	 */
	public static UnirestInstance getProxyInstance(final String username, final String password, final String host, final int port)
	{
		UnirestInstance unirest = new UnirestInstance();
		unirest.InitProxy(username, password, host, port);
		return unirest;
	}
	
	/**
	 * Return an SSL enabled client with standard proxy and custom timeout
	 * 
	 * @param host the proxy host address
	 * @param port the port to connect to via proxy
	 * @param connectionTimeout connection timeout override for client
	 * @param socketTimeout socket timeout override for client
	 * @return an SSL enabled client with standard proxy and custom timeout
	 */
	public static UnirestInstance getProxyInstanceWithCustomTimeoutInstance(final String host, final int port, final long connectionTimeout, final long socketTimeout)
	{
		UnirestInstance unirest = new UnirestInstance();
		unirest.InitProxyWithCustomTimeout(host, port, connectionTimeout, socketTimeout);
		return unirest;
	}
	
	/**
	 * Returns an SSL enabled client with authenticated proxy and custom timeout
	 * 
	 * @param host the proxy host address
	 * @param port the port to connect to via proxy
	 * @param username username for proxy authentication
	 * @param password password for proxy authentication
	 * @param connectionTimeout connection timeout override for client
	 * @param socketTimeout socket timeout override for client
	 * @return an SSL enabled client with authenticated proxy and custom timeout
	 */
	public static UnirestInstance getProxyInstanceWithCustomTimeoutInstance(final String host, final int port, final String username, final String password, final long connectionTimeout, final long socketTimeout)
	{
		UnirestInstance unirest = new UnirestInstance();
		unirest.InitProxy(host, port);
		return unirest;
	}	
}
