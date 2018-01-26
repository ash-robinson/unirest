package com.arr.rest;

import java.io.*;

/**
 * Factory for CUnirest class
 * @author ARR
 */
public class CUnirestFactory implements Serializable
{		
	/**
	 * @return a standard SSL enabled client 
	 */
	public static CUnirest getStandardInstance()
	{
		CUnirest unirest = new CUnirest();
		return unirest;
	}
	
	/**
	 * @param connectionTimeout
	 * @param socketTimeout
	 * @return a SSL enabled client with custom timeouts
	 */
	public static CUnirest getCustomTimeoutInstance(final long connectionTimeout, final long socketTimeout)
	{
		CUnirest unirest = new CUnirest();
		unirest.InitCustomTimeoutSSLClient(connectionTimeout, socketTimeout);
		return unirest;
	}
	
	/**
	 * @param host
	 * @param port
	 * @return a SSL enabled client with standard proxy
	 */
	public static CUnirest getProxyInstance(final String host, final int port)
	{
		CUnirest unirest = new CUnirest();
		unirest.InitProxy(host, port);
		return unirest;
	}
	
	/**
	 * @param username
	 * @param password
	 * @param host
	 * @param port
	 * @return a SSL enabled client with authenticated proxy
	 */
	public static CUnirest getProxyInstance(final String username, final String password, final String host, final int port)
	{
		CUnirest unirest = new CUnirest();
		unirest.InitProxy(username, password, host, port);
		return unirest;
	}
	
	/**
	 * @param host
	 * @param port
	 * @param connectionTimeout
	 * @param socketTimeout
	 * @return a SSL enabled client with standard proxy and custom timeout
	 */
	public static CUnirest getProxyInstanceWithCustomTimeoutInstance(final String host, final int port, final long connectionTimeout, final long socketTimeout)
	{
		CUnirest unirest = new CUnirest();
		unirest.InitProxyWithCustomTimeout(host, port, connectionTimeout, socketTimeout);
		return unirest;
	}
	
	/**
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @param connectionTimeout
	 * @param socketTimeout
	 * @return a SSL enabled client with authenticated proxy and custom timeout
	 */
	public static CUnirest getProxyInstanceWithCustomTimeoutInstance(final String host, final int port, final String username, final String password, final long connectionTimeout, final long socketTimeout)
	{
		CUnirest unirest = new CUnirest();
		unirest.InitProxy(host, port);
		return unirest;
	}	
}
