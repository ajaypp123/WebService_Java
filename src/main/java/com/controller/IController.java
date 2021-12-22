package com.controller;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface IController {
	public void init(ChannelHandlerContext ctx, FullHttpRequest httpRequest);
	public abstract void execute();
}
