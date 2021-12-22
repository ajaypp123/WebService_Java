package com.webapp;

import io.netty.channel.socket.SocketChannel;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.EventExecutorGroup;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

	private final EventExecutorGroup EXECUTOR_GROUP;

    public ServerInitializer(EventExecutorGroup executorGroup) {
        EXECUTOR_GROUP = executorGroup;
    }

	@Override
	protected void initChannel(SocketChannel ch) {
		// SSL Context Provider
		/*
        SslContext sslContext = SSLContextProvider.getServerContext();

        ChannelPipeline p = ch.pipeline();

        if (sslContext != null) {
            p.addLast(sslContext.newHandler(ch.alloc()));
        }
        */

		ChannelPipeline pipeline = ch.pipeline();

		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
		pipeline.addLast(new ChunkedWriteHandler());
		pipeline.addLast(EXECUTOR_GROUP, new ServerHandler());
	}
}
