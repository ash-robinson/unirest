package com.github.ashrobinson.rest.request;

import java.net.URI;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;

/**
 * Allows sending a GET request with a request body.
 */
public class HttpGetWithBody extends HttpEntityEnclosingRequestBase 
{
    public HttpGetWithBody(String uri) {
        super();
        setURI(URI.create(uri));
    }
    
    public HttpGetWithBody(URI uri) {
        super();
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return HttpGet.METHOD_NAME;
    }
}
