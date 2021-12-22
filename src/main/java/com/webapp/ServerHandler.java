package com.webapp;

import org.apache.log4j.Logger;

import com.controller.IController;
import com.requestdispatcher.RequestDispatcherFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	static Logger log = Logger.getLogger(ServerHandler.class.getName());

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
		log.info("Received request for /" + httpRequest.method() + " " + httpRequest.uri());
		IController controller;
		try {
			controller = RequestDispatcherFactory.getController(httpRequest);
			controller.init(ctx, httpRequest);
			controller.execute();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		/*
		if (httpRequest.method().equals(HttpMethod.GET)) {
			new GenericGetHandler(ctx, httpRequest).run();
		} if (httpRequest.method().equals(HttpMethod.POST)) {
			
		}else {
			ByteBuf content = Unpooled.copiedBuffer("Bad Request", CharsetUtil.UTF_8);
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, content);
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
			response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
			ctx.write(response);
		}
		*/
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		ctx.close();
	}
}
