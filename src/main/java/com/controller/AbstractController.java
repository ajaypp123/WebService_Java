package com.controller;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import com.responce.ServerResponse;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;

public abstract class AbstractController implements IController {
	
	static Logger log = Logger.getLogger(GetQueryController.class.getName());

	ChannelHandlerContext ctx;
	FullHttpRequest httpRequest;
	Boolean keepAlive;
	String UUID;
	
	public AbstractController() {}

    public void init(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
		this.ctx = ctx;
		this.httpRequest = httpRequest;
		keepAlive = HttpUtil.isKeepAlive(httpRequest);
    }

    public abstract void execute();
    public ServerResponse getServerResponce() {return new ServerResponse();}
    
	protected void returnHTTPResponse(ServerResponse requestResponse) {
		String responseBody = requestResponse.getResponseBody();
		FullHttpResponse response;

		try {
			response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, requestResponse.getResponseStatus(),
					Unpooled.wrappedBuffer(responseBody.getBytes("UTF-8")));

			log.info("HTTP Response [" + response.status() + "]");
		} catch (UnsupportedEncodingException ex) {
			log.error("UTF-8 encoding is not supported.");
			response = null;
		}
		if (response != null) {
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/xml");
			response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

			if (!keepAlive) {
				ctx.write(response).addListener(ChannelFutureListener.CLOSE);

				log.debug("Connection closed.");
			} else {
				response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
				ctx.writeAndFlush(response);

				log.debug("Connection kept alive.");
			}
		}
	}
	
}
