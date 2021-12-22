package com.webapp;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public class GenericRequestDecoder extends HttpPostRequestDecoder {
	static Logger log = Logger.getLogger(GenericRequestDecoder.class.getName());
	
    public GenericRequestDecoder(FullHttpRequest fullHttpRequest) {
        super(new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE),
                fullHttpRequest);
    }

    public Map<String, String> getRequestBodyAsMap() {
        return parseRequestBody(getBodyHttpDatas());
    }

    private Map<String, String> parseRequestBody(List<InterfaceHttpData> datas) {
        Map<String, String> requestBody;
        requestBody = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        String key;
        String value;

        log.info("Request body attributes:");
        log.info(datas.size());
        for (InterfaceHttpData data : datas) {
        	log.info(data.getHttpDataType());
            if (data.getHttpDataType() == HttpDataType.Attribute) {
                Attribute attribute = (Attribute) data;
                key = attribute.getName();
                try {
                    value = attribute.getValue();
                    if (requestBody.containsKey(key) && key.toLowerCase().startsWith(".")) {
                        value = requestBody.get(key) + "," + value;
                    }
                    requestBody.put(key, value);
                    log.info("{" + key + "} , {" + value+ "}");
                } catch (IOException e) {
                	log.info("Failed to get value of " + key + " attribute. Error: " + e.getMessage());
                }
            }
        }

        return requestBody;
    }
}