package com.github.ashrobinson.rest.unirest;

import com.github.ashrobinson.rest.http.*;

public enum CUnirestUtils
{
	INSTANCE;
	
	static public void printDetails(HttpResponse response)
	{
		System.out.println("---------------------------------------------------");
		System.out.println("---  Return Code [" + response.getStatus() + "]");
		System.out.println("---  Body: " + response.getBody() + "");
		System.out.println("---------------------------------------------------\n");
	}
}
