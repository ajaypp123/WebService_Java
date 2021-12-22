package com.controller;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.activation.MimetypesFileTypeMap;

import org.apache.log4j.Logger;

import com.utility.UUIDUtil;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;

public class FileReaderController extends AbstractController {
	
	static Logger log = Logger.getLogger(FileReaderController.class.getName());

	@Override
	public void execute() {
		// controller -> service layer -> DAO layer
		UUID = UUIDUtil.getUUID();
		log.info("GET URI: " + this.httpRequest.uri() +", ReqId: " + UUID);
		
		String path = new File("src/main/resources").getAbsolutePath();
		Path staticFilePath = Paths.get(path + httpRequest.uri());
		File file = staticFilePath.toFile();

		log.info("Req for read file: - " + staticFilePath);

		long fileLength;
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			fileLength = raf.length();
			log.info("Static file length - " + fileLength);
			writeHeader(file, fileLength);
			writeContent(raf, fileLength);
		} catch (FileNotFoundException ex) {
			log.info("File not found.");
			sendErrorResponse(HttpResponseStatus.NOT_FOUND, "Resource not found.");
			return;
		} catch (IOException ex) {
			log.error("Error occurred while reading file.\n" + ex.getMessage());
			sendErrorResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error occurred while reading the file.");
			return;
		}
	}
	
	private void writeContent(RandomAccessFile raf, long fileLength) {
		log.info("Entry readFile Content");
		ChannelFuture sendFileFuture = null;
		ChannelFuture lastContentFuture;

		if (ctx.pipeline().get(SslHandler.class) == null) {
			FileChannel fc = raf.getChannel();
			ctx.write(new DefaultFileRegion(fc, 0, fileLength),
					ctx.newProgressivePromise());
			lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
			log.info("SSL File write");
		} else {
			try {
				sendFileFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 4)),
						ctx.newProgressivePromise());
				lastContentFuture = sendFileFuture;

				log.info("Static resource sent to client.");
			} catch (IOException ex) {
				lastContentFuture = null;
			}
		}

		/**
		 * Add listener to send file future if required.
		 */
		// Decide whether to close the connection or not.
		if (!keepAlive) {
			// Close the connection when the whole content is written out.
			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
			log.debug("Connection closed.");
		}
		log.info("Exit");
	}
	
	private void writeHeader(File file, long fileLength) {
		log.info("Entry readFile Header - " + file.getName());
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		HttpUtil.setContentLength(response, fileLength);
		setContentTypeHeader(response, file);
		if (keepAlive) {
			response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}

		ctx.write(response);
		log.info("Exit");
	}
	
	private void sendErrorResponse(HttpResponseStatus status, String responseBody) {
		FullHttpResponse httpResponse;

		try {
			httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
					Unpooled.wrappedBuffer(responseBody.getBytes("UTF-8")));

			log.debug("Error response sent.");
		} catch (UnsupportedEncodingException ex) {
			log.error("UTF-8 encoding is not supported.");
			httpResponse = null;
		}
		if (httpResponse != null) {
			httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/xml");
			httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());

			if (!keepAlive) {
				ctx.write(httpResponse).addListener(ChannelFutureListener.CLOSE);

				log.debug("Connection closed.");
			} else {
				httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
				ctx.writeAndFlush(httpResponse);

				log.debug("Connection kept alive.");
			}
		}
	}

	private void setContentTypeHeader(HttpResponse response, File file) {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
	}
}
