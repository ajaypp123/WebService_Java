package com.webapp;

import java.util.ArrayList;
import org.apache.log4j.Logger;

import com.requestdispatcher.RequestDispatcherFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class AppServer {
	private static final int HTTP_PORT = 8080;
	private static final int THREAD_COUNT = 3;
	private static EventLoopGroup bossGroup, workerGroup;
	private static EventExecutorGroup executorGroup;
	private static ServerBootstrap httpBootstrap;
	ArrayList<Channel> serverChannels;
	static Logger log = Logger.getLogger(AppServer.class.getName());

	AppServer() {
		// Create the multi threaded event loops for the server
		bossGroup = new NioEventLoopGroup(THREAD_COUNT);
		workerGroup = new NioEventLoopGroup(THREAD_COUNT);
		executorGroup = new DefaultEventExecutorGroup(THREAD_COUNT);
		serverChannels = new ArrayList<Channel>();
		RequestDispatcherFactory.init();
	}

	private static Channel httpServerBootstrap(EventLoopGroup bossGroup, EventLoopGroup workerGroup,
			EventExecutorGroup executorGroup, String host, int port) throws InterruptedException {
		httpBootstrap = new ServerBootstrap();
		httpBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
		httpBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ServerInitializer(executorGroup))
				.childOption(ChannelOption.SO_KEEPALIVE, true);

		// Bind and start to accept incoming connections.
		Channel serverChannel = httpBootstrap.bind(host, port).sync().channel();
		log.info("Bind and start to accept incoming connections.");
		log.info("---- Started lisitining on " + host + ":" + port + " -----------");
		return serverChannel;
	}

	public void run() throws Exception {
		try {
			// A helper class that simplifies server configuration
			Channel serverChannel = httpServerBootstrap(
					bossGroup, workerGroup, executorGroup, 
					"0.0.0.0", HTTP_PORT);
			serverChannels.add(serverChannel);
			
			// Wait until server socket is closed
			for (Channel ch : serverChannels) {
				ch.closeFuture().sync();
			}
		} finally {
			log.info("Shutdown server.");
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		AppServer appserver = new AppServer();
		appserver.run();
	}
}