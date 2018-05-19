package com.github.ashrobinson.rest.unirest;

import java.io.*;
import java.security.*;
import java.security.cert.*;

import javax.net.ssl.*;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.io.*;
import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.*;
import org.apache.http.client.config.*;
import org.apache.http.config.*;
import org.apache.http.conn.socket.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.auth.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.*;

import com.github.ashrobinson.rest.http.*;

/**
 * Note: Always call {@link #InitSSLClient()} before using in an ssl context to avoid cert problems in Unirest HTTP implementation
 * Note: Added proxy {@link #InitProxy(String, String, String, int)}
 * @author ARR
 *
 */
public class UnirestInstance implements Serializable
{
	static private String CERT_DEFAULT_FILENAME = "cacerts";
	static private String CERT_DEFAULT_PASSWORD = "changeit";
	static private final char[] HEXDIGITS = "0123456789abcdef".toCharArray();
	static private int CONNECTION_MANAGER_INACTIVITY_TIMEOUT = 10; //ms

	private Unirest client = null;
	private String certFilename = null;

	UnirestInstance()
	{
		super();
		client = new Unirest();
		certFilename = CERT_DEFAULT_FILENAME;
	}

	public Unirest getClient()
	{
		return client;
	}
	
	/**
	 * Assumed at root folder
	 * 
	 * @param name the filename of the certificate
	 */
	public void setCertificateFilename(final String name)
	{
		certFilename = name;
	}

	/**
	 * Return instance using proxy
	 * 
	 * @param host the proxy host address
	 * @param port the port to connect to via proxy
	 */
	public void InitProxy(final String host, final int port)
	{
		final SSLConnectionSocketFactory sslsf;
		try
		{
			sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault(), NoopHostnameVerifier.INSTANCE);
		}
		catch (NoSuchAlgorithmException ae)
		{
			throw new RuntimeException(ae);
		}

		final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", new PlainConnectionSocketFactory()).register("https", sslsf).build();
		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		cm.setMaxTotal(100);
		cm.setValidateAfterInactivity(CONNECTION_MANAGER_INACTIVITY_TIMEOUT);

		//proxy specific stuff
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		httpClientBuilder.useSystemProperties();
		httpClientBuilder.setProxy(new HttpHost(host, port));
		httpClientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
		httpClientBuilder.setSSLSocketFactory(sslsf);
		httpClientBuilder.setConnectionManager(cm);
		httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(3, false));
		CloseableHttpClient httpClient = httpClientBuilder.build();
		client.setHttpClient(httpClient);
	}

	/**
	 * Return instance using proxy with auth
	 * 
	 * @param username username for proxy authentication
	 * @param password password for proxy authentication
	 * @param host the proxy host address
	 * @param port the port to connect to via proxy
	 */
	public void InitProxy(final String username, final String password, final String host, final int port)
	{
		final SSLConnectionSocketFactory sslsf;
		try
		{
			sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault(), NoopHostnameVerifier.INSTANCE);
		}
		catch (NoSuchAlgorithmException ae)
		{
			throw new RuntimeException(ae);
		}

		final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", new PlainConnectionSocketFactory()).register("https", sslsf).build();
		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		cm.setMaxTotal(100);
		cm.setValidateAfterInactivity(CONNECTION_MANAGER_INACTIVITY_TIMEOUT);

		//proxy specific stuff
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
		httpClientBuilder.useSystemProperties();
		httpClientBuilder.setProxy(new HttpHost(host, port));
		httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
		httpClientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
		Lookup<AuthSchemeProvider> authProviders = RegistryBuilder.<AuthSchemeProvider> create().register(AuthSchemes.BASIC, new BasicSchemeFactory()).build();
		httpClientBuilder.setDefaultAuthSchemeRegistry(authProviders);
		httpClientBuilder.setSSLSocketFactory(sslsf);
		httpClientBuilder.setConnectionManager(cm);
		httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(3, false));
		CloseableHttpClient httpClient = httpClientBuilder.build();
		client.setHttpClient(httpClient);
	}

	/**
	 * Return standard instance with fix to SSL connection problems in base Unirest
	 */
	public void InitSSLClient()
	{
		final SSLConnectionSocketFactory sslsf;
		try
		{
			sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault(), NoopHostnameVerifier.INSTANCE);
		}
		catch (NoSuchAlgorithmException ae)
		{
			throw new RuntimeException(ae);
		}

		final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", new PlainConnectionSocketFactory()).register("https", sslsf).build();
		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		cm.setMaxTotal(100);
		cm.setValidateAfterInactivity(CONNECTION_MANAGER_INACTIVITY_TIMEOUT);
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).setConnectionManager(cm)
				.setDefaultRequestConfig(RequestConfig.DEFAULT)
				.setRetryHandler(new DefaultHttpRequestRetryHandler(3, false)).build();
		client.setHttpClient(httpClient);
	}

	/**
	 * Return instance with custom timeout
	 * @param connectionTimeout connection timeout override for client
	 * @param socketTimeout socket timeout override for client
	 */
	public void InitCustomTimeoutSSLClient(final long connectionTimeout, final long socketTimeout)
	{
		final SSLConnectionSocketFactory sslsf;
		try
		{
			sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault(), NoopHostnameVerifier.INSTANCE);
		}
		catch (NoSuchAlgorithmException ae)
		{
			throw new RuntimeException(ae);
		}

		final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", new PlainConnectionSocketFactory()).register("https", sslsf).build();
		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		cm.setMaxTotal(100);
		cm.setValidateAfterInactivity(CONNECTION_MANAGER_INACTIVITY_TIMEOUT);
		
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Math.toIntExact(connectionTimeout))
				.setSocketTimeout(Math.toIntExact(socketTimeout))
				.setConnectionRequestTimeout(CONNECTION_MANAGER_INACTIVITY_TIMEOUT).build();
		
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).setConnectionManager(cm)
				.setDefaultRequestConfig(requestConfig)
				.setRetryHandler(new DefaultHttpRequestRetryHandler(3, false)).build();
		
		client.setHttpClient(httpClient);
	}
	

	/**
	 * Return instance using proxy with custom timeout
	 * @param host the proxy host address
	 * @param port the port to connect to via proxy
	 * @param connectionTimeout connection timeout override for client
	 * @param socketTimeout socket timeout override for client
	 */
	public void InitProxyWithCustomTimeout(final String host, final int port, final long connectionTimeout, final long socketTimeout)
	{
		final SSLConnectionSocketFactory sslsf;
		try
		{
			sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault(), NoopHostnameVerifier.INSTANCE);
		}
		catch (NoSuchAlgorithmException ae)
		{
			throw new RuntimeException(ae);
		}

		final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", new PlainConnectionSocketFactory()).register("https", sslsf).build();
		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		cm.setMaxTotal(100);
		cm.setValidateAfterInactivity(CONNECTION_MANAGER_INACTIVITY_TIMEOUT);

		//proxy specific stuff
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		httpClientBuilder.useSystemProperties();
		httpClientBuilder.setProxy(new HttpHost(host, port));
		httpClientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
		httpClientBuilder.setSSLSocketFactory(sslsf);
		httpClientBuilder.setConnectionManager(cm);
		httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(3, false));
		
		//custom timeout
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Math.toIntExact(connectionTimeout))
				.setSocketTimeout(Math.toIntExact(socketTimeout))
				.setConnectionRequestTimeout(CONNECTION_MANAGER_INACTIVITY_TIMEOUT).build();		
		httpClientBuilder.setDefaultRequestConfig(requestConfig);
		
		CloseableHttpClient httpClient = httpClientBuilder.build();
		client.setHttpClient(httpClient);
	}
	
	/**
	 * Return instance using proxy using auth and custom timeout
	 * @param username username for proxy authentication
	 * @param password password for proxy authentication
	 * @param host the proxy host address
	 * @param port the port to connect to via proxy
	 * @param connectionTimeout connection timeout override for client
	 * @param socketTimeout socket timeout override for client
	 */
	public void InitProxyWithCustomTimeout(final String username, final String password, final String host, final int port, final long connectionTimeout, final long socketTimeout)
	{
		final SSLConnectionSocketFactory sslsf;
		try
		{
			sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault(), NoopHostnameVerifier.INSTANCE);
		}
		catch (NoSuchAlgorithmException ae)
		{
			throw new RuntimeException(ae);
		}

		final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", new PlainConnectionSocketFactory()).register("https", sslsf).build();
		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		cm.setMaxTotal(100);
		cm.setValidateAfterInactivity(CONNECTION_MANAGER_INACTIVITY_TIMEOUT);

		//proxy specific stuff
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
		httpClientBuilder.useSystemProperties();
		httpClientBuilder.setProxy(new HttpHost(host, port));
		httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
		httpClientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
		Lookup<AuthSchemeProvider> authProviders = RegistryBuilder.<AuthSchemeProvider> create().register(AuthSchemes.BASIC, new BasicSchemeFactory()).build();
		httpClientBuilder.setDefaultAuthSchemeRegistry(authProviders);
		httpClientBuilder.setSSLSocketFactory(sslsf);
		httpClientBuilder.setConnectionManager(cm);
		httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(3, false));
		
		//custom timeout
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Math.toIntExact(connectionTimeout))
				.setSocketTimeout(Math.toIntExact(socketTimeout))
				.setConnectionRequestTimeout(CONNECTION_MANAGER_INACTIVITY_TIMEOUT).build();		
		httpClientBuilder.setDefaultRequestConfig(requestConfig);
		
		CloseableHttpClient httpClient = httpClientBuilder.build();
		client.setHttpClient(httpClient);
	}
	

	/**
	 * Dynamically adds the cert at the location [host]:[port] to the cacerts keystore 
	 * Uses default passphrase if unspecified
	 * @param host the proxy host address
	 * @param port the port to connect to via proxy
	 * @throws Exception encompasses various exceptions thrown during the process
	 */
	public void InstallCertificate(final String host, final int port) throws Exception
	{
		InstallCertificate(host, port, CERT_DEFAULT_PASSWORD);
	}

	/**
	 * Dynamically adds the cert at the location [host]:[port] to the cacerts keystore 
	 * @param host the proxy host address
	 * @param port the port to connect to via proxy
	 * @param passphrase specified keystore passphrase
	 * @throws Exception encompasses various exceptions thrown during the process
	 */
	public void InstallCertificate(final String host, final int port, final String passphrase) throws Exception
	{
		//import keystore
		File file = new File(certFilename);
		//if not found use default java truststore 'java.home'
		if (file.isFile() == false)
		{
			File jre_cert = new File(System.getProperty("java.home") + "\\lib\\security\\cacerts");
			System.out.println("checking cert: [" + jre_cert.getAbsolutePath() + "]");
			if (jre_cert.isFile())
			{
				FileUtils.copyFile(jre_cert, file);
			}
			else
			{
				throw new IOException("No java keystore can be found");
			}			
		}
		System.out.println("Loading KeyStore " + file + "...");
		InputStream in = new FileInputStream(file);
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(in, passphrase.toCharArray());
		in.close();

		SSLContext context = SSLContext.getInstance("TLS");
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
		SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
		context.init(null, new TrustManager[] { tm }, null);
		SSLSocketFactory factory = context.getSocketFactory();

		System.out.println("Opening connection to " + host + ":" + port + "...");
		SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
		socket.setSoTimeout(10000);

		try
		{
			System.out.println("Starting SSL handshake...");
			socket.startHandshake();
			socket.close();
			System.out.println();
			System.out.println("No errors, certificate is already trusted");
			return;
		}
		catch (SSLException e)
		{
			System.out.println();
			e.printStackTrace(System.out);
		}

		X509Certificate[] chain = tm.chain;
		if (chain == null)
		{
			System.out.println("Could not obtain server certificate chain");
			return;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		System.out.println();
		System.out.println("Server sent " + chain.length + " certificate(s):");
		System.out.println();
		MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		for (int i = 0; i < chain.length; i++)
		{
			X509Certificate cert = chain[i];
			System.out.println(" " + (i + 1) + " Subject " + cert.getSubjectDN());
			System.out.println("   Issuer  " + cert.getIssuerDN());
			sha1.update(cert.getEncoded());
			System.out.println("   sha1    " + toHexString(sha1.digest()));
			md5.update(cert.getEncoded());
			System.out.println("   md5     " + toHexString(md5.digest()));
			System.out.println();
		}

		int k = 0;
		X509Certificate cert = chain[k];
		String alias = host + "-" + (k + 1);
		ks.setCertificateEntry(alias, cert);

		OutputStream out = new FileOutputStream(certFilename);
		ks.store(out, passphrase.toCharArray());
		out.close();

		System.out.println();
		System.out.println(cert);
		System.out.println();
		System.out.println("Added certificate to keystore '" + certFilename + "' using alias '" + alias + "'");
	}

	private String toHexString(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder(bytes.length * 3);
		for (int b : bytes)
		{
			b &= 0xff;
			sb.append(HEXDIGITS[b >> 4]);
			sb.append(HEXDIGITS[b & 15]);
			sb.append(' ');
		}
		return sb.toString();
	}

	private class SavingTrustManager implements X509TrustManager
	{

		private final X509TrustManager tm;
		private X509Certificate[] chain;

		SavingTrustManager(X509TrustManager tm)
		{
			this.tm = tm;
		}

		public X509Certificate[] getAcceptedIssuers()
		{
			return new X509Certificate[0];
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
		{
			throw new UnsupportedOperationException();
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
		{
			this.chain = chain;
			tm.checkServerTrusted(chain, authType);
		}

	}
}
