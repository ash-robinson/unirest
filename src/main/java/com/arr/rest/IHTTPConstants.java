package com.arr.rest;

public interface IHTTPConstants
{
	static final public String			USE_HTTPS_PROPERTY			= "plugin.http.usehttps";
	static final public String			HOST_PROPERTY				= "plugin.http.host";
	static final public String			PORT_PROPERTY				= "plugin.http.port";
	static final public String			URL_PROPERTY				= "plugin.http.url";
	static final public String			USER_PROPERTY				= "plugin.http.user";
	static final public String			PWD_PROPERTY				= "plugin.http.password";
	static final public String			SEC_PROPERTY				= "plugin.http.sec";
	static final public String			CERT_FILENAME_PROPERTY		= "plugin.http.certfilename";
	static final public String			ADDITIONAL_PARAMS_PROPERTY	= "plugin.http.additional";	//a semicolon-delimited list of key=value pairs to add to the header
	static final public String			PARSERCLASS_PROPERTY		= "plugin.http.parserclass";
	static final public String			DATA_PROPERTY				= "plugin.http.data";
	static final public String			POST_PROPERTY				= "plugin.http.post";
	static final public String			URL_METHOD_PROPERTY			= "plugin.http.method";
	static final public String			PROXY_HOST_PROPERTY			= "plugin.http.proxyhost";
	static final public String			PROXY_PORT_PROPERTY			= "plugin.http.proxyport";
	static final public String			PROXY_USER_PROPERTY			= "plugin.http.proxyuser";
	static final public String			PROXY_PASSWORD_PROPERTY		= "plugin.http.proxypassword";
	static final public String			CONNECT_TIMEOUT_PROPERTY	= "plugin.http.connecttimeout";
	static final public String			READ_TIMEOUT_PROPERTY		= "plugin.http.readtimeout";
	static final public String			CHARSET_PROPERTY			= "plugin.http.charset";
}

