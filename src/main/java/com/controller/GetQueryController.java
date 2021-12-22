package com.controller;

import java.util.*;

import org.apache.log4j.Logger;

import com.responce.ServerResponse;
import com.utility.UUIDUtil;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;

public class GetQueryController extends AbstractController {

	static Logger log = Logger.getLogger(GetQueryController.class.getName());

	@Override
	public void execute() {
		UUID = UUIDUtil.getUUID();
		log.info("GET URI: " + this.httpRequest.uri() +", ReqId: " + UUID);
		
		QueryStringDecoder queryParams = new QueryStringDecoder(httpRequest.uri());
		ServerResponse responce = getServerResponce();
		log.info("Query: " + queryParams.parameters().toString());
		log.info("Query: " + queryParams.path());
		
		Map<String, List<String>> data = queryParams.parameters();
		responce.setResponseBody(data.toString());
		responce.setResponseStatus(HttpResponseStatus.OK);
		returnHTTPResponse(responce);
	}

}
