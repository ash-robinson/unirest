package com.arr.rest;

import java.io.*;
import java.util.*;

/**
 * Factory for CUnirest class
 * @author ARR
 */
public class CUnirestFactory implements Serializable
{		
	/**
	 * Returns a standard SSL enabled client 
	 * 
	 * @return a standard SSL enabled client 
	 */
	public static CUnirest getStandardInstance()
	{
		CUnirest unirest = new CUnirest();
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
	public static CUnirest getCustomTimeoutInstance(final long connectionTimeout, final long socketTimeout)
	{
		CUnirest unirest = new CUnirest();
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
	public static CUnirest getProxyInstance(final String host, final int port)
	{
		CUnirest unirest = new CUnirest();
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
	public static CUnirest getProxyInstance(final String username, final String password, final String host, final int port)
	{
		CUnirest unirest = new CUnirest();
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
	public static CUnirest getProxyInstanceWithCustomTimeoutInstance(final String host, final int port, final long connectionTimeout, final long socketTimeout)
	{
		CUnirest unirest = new CUnirest();
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
	public static CUnirest getProxyInstanceWithCustomTimeoutInstance(final String host, final int port, final String username, final String password, final long connectionTimeout, final long socketTimeout)
	{
		CUnirest unirest = new CUnirest();
		unirest.InitProxy(host, port);
		return unirest;
	}	

	/**
	 * When passed a variable map, evaluate and return the correct instance
	 * @param ctx the namespace context containing the variables found in {@link IHTTPConstants}
	 * @return instance specified, if no match will return a standard instance
	 */
	public static CUnirest getClientFromContext(final Map<String, Object> ctx)
	{
		CUnirest unirest = null;
		if (ctx == null)
		{
			return getStandardInstance();
		}
		//if any pair in exist in ctx
		if ((ctx.containsKey(IHTTPConstants.CONNECT_TIMEOUT_PROPERTY) && ctx.containsKey(IHTTPConstants.READ_TIMEOUT_PROPERTY))
				|| (ctx.containsKey(IHTTPConstants.PROXY_HOST_PROPERTY) && ctx.containsKey(IHTTPConstants.PROXY_PORT_PROPERTY))
						|| (ctx.containsKey(IHTTPConstants.PROXY_USER_PROPERTY) && ctx.containsKey(IHTTPConstants.PROXY_PASSWORD_PROPERTY)))
		{
			//if all exist in ctx
			if((ctx.containsKey(IHTTPConstants.CONNECT_TIMEOUT_PROPERTY) && ctx.containsKey(IHTTPConstants.READ_TIMEOUT_PROPERTY))
					&&(ctx.containsKey(IHTTPConstants.PROXY_HOST_PROPERTY) && ctx.containsKey(IHTTPConstants.PROXY_PORT_PROPERTY))
							&& (ctx.containsKey(IHTTPConstants.PROXY_USER_PROPERTY) && ctx.containsKey(IHTTPConstants.PROXY_PASSWORD_PROPERTY)))
			{
				try
				{
					unirest = getProxyInstanceWithCustomTimeoutInstance(ctx.get(IHTTPConstants.PROXY_HOST_PROPERTY).toString(), Integer.parseInt(ctx.get(IHTTPConstants.PROXY_PORT_PROPERTY).toString()),
							ctx.get(IHTTPConstants.PROXY_USER_PROPERTY).toString(), ctx.get(IHTTPConstants.PROXY_PASSWORD_PROPERTY).toString(), 
							Long.parseLong(ctx.get(IHTTPConstants.CONNECT_TIMEOUT_PROPERTY).toString()), Long.parseLong(ctx.get(IHTTPConstants.READ_TIMEOUT_PROPERTY).toString()));
					return unirest;
				} 
				//catch all parse exceptions ect. for noe
				catch (Exception ex)
				{
					//problem creating instance - report then return standard
					return getStandardInstance();		
				}
			}
			else
			//proxy no authentication with custom timeout
			if((ctx.containsKey(IHTTPConstants.CONNECT_TIMEOUT_PROPERTY) && ctx.containsKey(IHTTPConstants.READ_TIMEOUT_PROPERTY))
					&& (ctx.containsKey(IHTTPConstants.PROXY_HOST_PROPERTY) && ctx.containsKey(IHTTPConstants.PROXY_PORT_PROPERTY)))
			{
				try
				{
					unirest = getProxyInstanceWithCustomTimeoutInstance(ctx.get(IHTTPConstants.PROXY_HOST_PROPERTY).toString(), Integer.parseInt(ctx.get(IHTTPConstants.PROXY_PORT_PROPERTY).toString()),
							Long.parseLong(ctx.get(IHTTPConstants.CONNECT_TIMEOUT_PROPERTY).toString()), Long.parseLong(ctx.get(IHTTPConstants.READ_TIMEOUT_PROPERTY).toString()));
					return unirest;
				} 
				//catch all parse exceptions ect. for noe
				catch (Exception ex)
				{
					//problem creating instance - report then return standard
					return getStandardInstance();		
				}
			}
			else
				//proxy with auth
			if((ctx.containsKey(IHTTPConstants.PROXY_HOST_PROPERTY) && ctx.containsKey(IHTTPConstants.PROXY_PORT_PROPERTY))
					&& (ctx.containsKey(IHTTPConstants.PROXY_USER_PROPERTY) && ctx.containsKey(IHTTPConstants.PROXY_PASSWORD_PROPERTY)))
			{
				try
				{
					unirest = getProxyInstance(ctx.get(IHTTPConstants.PROXY_USER_PROPERTY).toString(), ctx.get(IHTTPConstants.PROXY_PASSWORD_PROPERTY).toString(),
							ctx.get(IHTTPConstants.PROXY_HOST_PROPERTY).toString(), Integer.parseInt(ctx.get(IHTTPConstants.PROXY_PORT_PROPERTY).toString()));
					return unirest;
				} 
				//catch all parse exceptions ect. for noe
				catch (Exception ex)
				{
					//problem creating instance - report then return standard
					return getStandardInstance();		
				}
			}
			else
			//proxy without auth
			if(ctx.containsKey(IHTTPConstants.PROXY_HOST_PROPERTY) && ctx.containsKey(IHTTPConstants.PROXY_PORT_PROPERTY))					
			{
				try
				{
					unirest = getProxyInstance(ctx.get(IHTTPConstants.PROXY_HOST_PROPERTY).toString(), Integer.parseInt(ctx.get(IHTTPConstants.PROXY_PORT_PROPERTY).toString()));
					return unirest;
				} 
				//catch all parse exceptions ect. for noe
				catch (Exception ex)
				{
					//problem creating instance - report then return standard
					return getStandardInstance();		
				}
			}
			else
			//else custom timeout instance
			if(ctx.containsKey(IHTTPConstants.CONNECT_TIMEOUT_PROPERTY) && ctx.containsKey(IHTTPConstants.READ_TIMEOUT_PROPERTY))
			{
				try
				{
					unirest = getCustomTimeoutInstance(Long.parseLong(ctx.get(IHTTPConstants.CONNECT_TIMEOUT_PROPERTY).toString()), Long.parseLong(ctx.get(IHTTPConstants.READ_TIMEOUT_PROPERTY).toString()));
					return unirest;
				} 
				//catch all parse exceptions ect. for noe
				catch (Exception ex)
				{
					//problem creating instance - report then return standard
					return getStandardInstance();		
				}
			}
		}	
		//no matches returns standard instance
		return getStandardInstance();		
	}
}
