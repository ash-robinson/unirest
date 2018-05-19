# Unirest for Java

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

This fork provides an extension to the Unirest project, primarily for allowing it to handle multiple unique instances for use in the multithreaded projects. 

This project has maintained the basic functionality of the Unirest project referenced [here](https://github.com/Kong/unirest-java)

The only new step is to create an instance of Unirest using the new UnirestBuilder class as shown below:

```java
// Only one time
UnirestInstance unirest = UnirestFactory.getStandardInstance();	

// Then call unirest.getClient() before your request
HttpResponse<JsonElement> resp = unirest.getClient().get("http://127.0.0.1:9000/api")
					.asJson();		
											
```


## Features
* Use of Gson libraries for returned objects
* Preconfiguration for HTTPS requests
* Supports multi-instance
* Same great user-friendly, efficient API callings as the original Unirest project.
