package com.responce;

import io.netty.handler.codec.http.HttpResponseStatus;

public class ServerResponse {

	String responseBody;
	HttpResponseStatus responseStatus;

	public ServerResponse() {
		this.responseBody = "";
		this.responseStatus = HttpResponseStatus.FORBIDDEN;
	}

	public ServerResponse(HttpResponseStatus responseStatus, String responseBody) {
		this.responseBody = responseBody;
		this.responseStatus = responseStatus;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseStatus(HttpResponseStatus status) {
		responseStatus = status;
	}

	public void setResponseBody(String body) {
		responseBody = body;
	}

	public HttpResponseStatus getResponseStatus() {
		return responseStatus;
	}
}