package com.requestdispatcher;

import java.util.HashMap;
import java.util.Map;

import com.controller.IController;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

public class RequestDispatcherFactory {
	// <GET, </uri, GetURIController>>
	public static Map<HttpMethod, HashMap<String, String>> requestMapping;

	public static void init() {
		requestMapping = new HashMap<HttpMethod, HashMap<String, String>>();
		requestMapping.put(HttpMethod.GET, new HashMap<String, String>());
		requestMapping.put(HttpMethod.POST, new HashMap<String, String>());
		requestMapping.get(HttpMethod.GET).put("/readfile",
				"com.controller.FileReaderController");
		requestMapping.get(HttpMethod.GET).put("/",
				"com.controller.GetQueryController");
	}
	
	public static String getURI(String url) {
		if(url.startsWith("/readfile"))
			return "/readfile";
		else
			return "/";
	}

	@SuppressWarnings("deprecation")
	public static IController getController(FullHttpRequest httpRequest) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		// AbstractController controller = new GetQueryController(httpRequest);
		String controllerPath = requestMapping.get(httpRequest.method()).get(getURI(httpRequest.uri()));
		return (IController)Class.forName(controllerPath).newInstance();
	}
}
