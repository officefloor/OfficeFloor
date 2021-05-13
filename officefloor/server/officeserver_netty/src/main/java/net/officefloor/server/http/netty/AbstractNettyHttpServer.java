/*-
 * #%L
 * Netty HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.http.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import net.officefloor.frame.api.manage.ProcessManager;

/**
 * Abstract Netty HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractNettyHttpServer {

	static {
		ResourceLeakDetector.setLevel(Level.DISABLED);
	}

	/**
	 * {@link ProcessManager} key.
	 */
	private static final AttributeKey<ProcessManager> PROCESS_MANAGER_KEY = AttributeKey.valueOf("KEY");

	/**
	 * Maximum length of the request entity.
	 */
	private final int maxRequestEntityLength;

	/**
	 * {@link EventLoopGroup}.
	 */
	private EventLoopGroup loopGroup = null;

	/**
	 * Instantiate.
	 * 
	 * @param maxRequestEntityLength Maximum length of the request entity.
	 */
	public AbstractNettyHttpServer(int maxRequestEntityLength) {
		this.maxRequestEntityLength = maxRequestEntityLength;
	}

	/**
	 * Starts the HTTP Server.
	 * 
	 * @param httpPort   HTTP port.
	 * @param httpsPort  HTTPS secure port.
	 * @param sslContext {@link SSLContext}. May be <code>null</code>.
	 * @throws Exception If fails to start the HTTP Server.
	 */
	public void startHttpServer(int httpPort, int httpsPort, SSLContext sslContext) throws Exception {

		// Configure the HTTP Server
		if (Epoll.isAvailable()) {
			this.startHttpServer(httpPort, httpsPort, sslContext, new EpollEventLoopGroup(),
					EpollServerSocketChannel.class);
		} else if (KQueue.isAvailable()) {
			this.startHttpServer(httpPort, httpsPort, sslContext, new KQueueEventLoopGroup(),
					KQueueServerSocketChannel.class);
		} else {
			this.startHttpServer(httpPort, httpsPort, sslContext, new NioEventLoopGroup(),
					NioServerSocketChannel.class);
		}
	}

	/**
	 * Stops the HTTP server.
	 * 
	 * @throws Exception If fails to stop the HTTP Server.
	 */
	public void stopHttpServer() throws Exception {
		try {
			// Attempt to stop server gracefully
			this.loopGroup.shutdownGracefully().sync();
		} finally {
			// Ensure shut down
			this.loopGroup.shutdownGracefully(0, 1, TimeUnit.SECONDS).sync();
		}
	}

	/**
	 * Services the {@link HttpRequest}.
	 * 
	 * @param context {@link ChannelHandlerContext}.
	 * @param request {@link HttpRequest}.
	 * @return {@link ProcessManager}.
	 * @throws Exception If fails to service the {@link HttpRequest}.
	 */
	protected abstract ProcessManager service(ChannelHandlerContext context, HttpRequest request) throws Exception;

	/**
	 * Starts the HTTP Server.
	 * 
	 * @param httpPort           HTTP port.
	 * @param httpsPort          HTTPS secure port.
	 * @param sslContext         {@link SSLContext}. May be <code>null</code>.
	 * @param loopGroup          {@link EventLoopGroup}.
	 * @param serverChannelClass {@link ServerChannel} {@link Class}.
	 * @throws InterruptedException If fails to start.
	 */
	private void startHttpServer(int httpPort, int httpsPort, SSLContext sslContext, EventLoopGroup loopGroup,
			Class<? extends ServerChannel> serverChannelClass) throws InterruptedException {
		this.loopGroup = loopGroup;

		// Configure and start the non-secure server
		this.startPortServicing(httpPort, new ServerChannelInitialiser(), serverChannelClass);

		// Configure and start the secure server
		if (httpsPort > 0) {
			ChannelInitializer<SocketChannel> channelInitialiser = (sslContext != null
					? new SecureServerChannelInitialiser(sslContext)
					: new ServerChannelInitialiser());
			this.startPortServicing(httpsPort, channelInitialiser, serverChannelClass);
		}
	}

	/**
	 * Starts servicing the port.
	 * 
	 * @param port               Port.
	 * @param channelInitializer {@link ChannelInitializer}.
	 * @param serverChannelClass {@link ServerChannel} {@link Class}.
	 * @throws InterruptedException If fails to start.
	 */
	private void startPortServicing(int port, ChannelInitializer<SocketChannel> channelInitializer,
			Class<? extends ServerChannel> serverChannelClass) throws InterruptedException {
		ServerBootstrap bootstrap = new ServerBootstrap();
		if (loopGroup instanceof EpollEventLoopGroup) {
			bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
		}
		bootstrap.option(ChannelOption.SO_BACKLOG, 8192);
		bootstrap.option(ChannelOption.SO_REUSEADDR, true);
		bootstrap.group(loopGroup).channel(serverChannelClass).childHandler(channelInitializer);
		bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
		bootstrap.bind(new InetSocketAddress(port)).sync();
	}

	/**
	 * Initialises the {@link ChannelPipeline}.
	 * 
	 * @param pipeline {@link ChannelPipeline}.
	 */
	private void initPipeline(ChannelPipeline pipeline) {
		pipeline.addLast("server", new HttpServerCodec());
		pipeline.addLast("aggregator", new HttpObjectAggregator(this.maxRequestEntityLength));
		pipeline.addLast("handler", new ServiceServerHandler());
	}

	/**
	 * Netty Server {@link ChannelInitializer}.
	 */
	private class ServerChannelInitialiser extends ChannelInitializer<SocketChannel> {

		/*
		 * ================== ChannelInitializer =====================
		 */

		@Override
		protected void initChannel(SocketChannel channel) throws Exception {
			ChannelPipeline pipeline = channel.pipeline();
			AbstractNettyHttpServer.this.initPipeline(pipeline);
		}
	}

	/**
	 * Netty Server {@link ChannelInitializer}.
	 */
	private class SecureServerChannelInitialiser extends ChannelInitializer<SocketChannel> {

		/**
		 * {@link SslContext}.
		 */
		private final SSLContext sslContext;

		/**
		 * Instantiate.
		 * 
		 * @param sslContext {@link SslContext}.
		 */
		private SecureServerChannelInitialiser(SSLContext sslContext) {
			this.sslContext = sslContext;
		}

		/*
		 * ================== ChannelInitializer =====================
		 */

		@Override
		protected void initChannel(SocketChannel channel) throws Exception {

			// Create the engine
			SSLEngine engine = this.sslContext.createSSLEngine();
			engine.setUseClientMode(false);

			// Create the secure pipeline
			ChannelPipeline pipeline = channel.pipeline();
			pipeline.addLast("ssl", new SslHandler(engine, true));
			AbstractNettyHttpServer.this.initPipeline(pipeline);
		}
	}

	/**
	 * {@link ChannelInboundHandlerAdapter} to invoke service method.
	 */
	private class ServiceServerHandler extends ChannelInboundHandlerAdapter {

		/*
		 * ================== ChannelInboundHandlerAdapter ============
		 */

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

			// Ensure flag process manager handling on channel
			Attribute<ProcessManager> attribute = ctx.channel().attr(PROCESS_MANAGER_KEY);
			if (attribute.get() == null) {

				// First request, so load the close listener
				ctx.channel().closeFuture().addListener((future) -> {
					ProcessManager processManager = ctx.channel().attr(PROCESS_MANAGER_KEY).get();
					processManager.cancel();
				});
			}

			// Handler
			if (msg instanceof HttpRequest) {
				try {
					HttpRequest request = (HttpRequest) msg;
					ProcessManager manager = AbstractNettyHttpServer.this.service(ctx, request);

					// Register for cancel handling on close
					attribute.set(manager);

				} finally {
					ReferenceCountUtil.release(msg);
				}
			} else {
				ctx.fireChannelRead(msg);
			}
		}

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			ctx.close();
		}
	}

}
