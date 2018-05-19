# Unirest for Java [![Build Status][travis-image]][travis-url]

![][unirest-logo]


This fork provides an extension to the Unirest project allowing it to handle multiple unique instances, for use in the multithreaded projects.
This project has maintained the basic functionality of the Unirest project referenced [here](https://github.com/Kong/unirest-java)

The only new step is create an instance of Unirest using the builder

```java
// Only one time
UnirestInstance unirest = UnirestFactory.getStandardInstance();	

// Then call unirest.getClient() before your request
HttpResponse<JsonElement> resp = unirest.getClient().get("http://127.0.0.1:9000/api1")
											.asJson();		
											
```

The UnirestBuilder class comes with pre-built configuration for HTTPS and Proxy calls.
