package com.arr.rest;

import java.io.*;

/**
 * Factory for CUnirest class
 * @author ARR
 */
public class CUnirestFactory implements Serializable
{	
	public CUnirestFactory() 
	{ 
		super(); 
	}
	
	public CUnirest getStandardInstance()
	{
		CUnirest unirest = new CUnirest();
		return unirest;
	}
	
	public CUnirest getCustomTimeoutInstance(final long connectionTimeout, final long socketTimeout)
	{
		CUnirest unirest = new CUnirest();
		unirest.InitCustomTimeoutSSLClient(connectionTimeout, socketTimeout);
		return unirest;
	}
	
	public CUnirest getProxyInstance(final String host, final int port)
	{
		CUnirest unirest = new CUnirest();
		unirest.InitProxy(host, port);
		return unirest;
	}
	
	public CUnirest getCustomTimeoutInstance(final String username, final String password, final String host, final int port)
	{
		CUnirest unirest = new CUnirest();
		unirest.InitProxy(username, password, host, port);
		return unirest;
	}
	
}
