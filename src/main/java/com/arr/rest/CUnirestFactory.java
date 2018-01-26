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
	 * @param connectionTimeout - connection timeout override for client
	 * @param socketTimeout - socket timeout override for client
	 * @return a SSL enabled client with custom timeouts
	 */
	public static CUnirest getCustomTimeoutInstance(final long connectionTimeout, final long socketTimeout)
	{
		CUnirest unirest = new CUnirest();
		unirest.InitCustomTimeoutSSLClient(connectionTimeout, socketTimeout);
		return unirest;
	}
	
	/**
	 * @param host - the proxy host address
	 * @param port - the port to connect to via proxy
	 * @return a SSL enabled client with standard proxy
	 */
	public static CUnirest getProxyInstance(final String host, final int port)
	{
		CUnirest unirest = new CUnirest();
		unirest.InitProxy(host, port);
		return unirest;
	}
	
	/**
	 * @param username - username for proxy authentication
	 * @param password - password for proxy authentication
	 * @param host - the proxy host address
	 * @param port - the port to connect to via proxy
	 * @return a SSL enabled client with authenticated proxy
	 */
	public static CUnirest getProxyInstance(final String username, final String password, final String host, final int port)
	{
		CUnirest unirest = new CUnirest();
		unirest.InitProxy(username, password, host, port);
		return unirest;
	}
	
	/**
	 * @param host - the proxy host address
	 * @param port - the port to connect to via proxy
	 * @param connectionTimeout - connection timeout override for client
	 * @param socketTimeout - socket timeout override for client
	 * @return a SSL enabled client with standard proxy and custom timeout
	 */
	public static CUnirest getProxyInstanceWithCustomTimeoutInstance(final String host, final int port, final long connectionTimeout, final long socketTimeout)
	{
		CUnirest unirest = new CUnirest();
		unirest.InitProxyWithCustomTimeout(host, port, connectionTimeout, socketTimeout);
		return unirest;
	}
	
	/**
	 * @param host - the proxy host address
	 * @param port - the port to connect to via proxy
	 * @param username - username for proxy authentication
	 * @param password - password for proxy authentication
	 * @param connectionTimeout - connection timeout override for client
	 * @param socketTimeout - socket timeout override for client
	 * @return a SSL enabled client with authenticated proxy and custom timeout
	 */
	public static CUnirest getProxyInstanceWithCustomTimeoutInstance(final String host, final int port, final String username, final String password, final long connectionTimeout, final long socketTimeout)
	{
		CUnirest unirest = new CUnirest();
		unirest.InitProxy(host, port);
		return unirest;
	}	
}
