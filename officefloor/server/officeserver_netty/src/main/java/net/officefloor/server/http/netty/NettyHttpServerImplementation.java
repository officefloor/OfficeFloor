/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.server.http.netty;

import java.net.InetSocketAddress;

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
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Netty {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class NettyHttpServerImplementation implements HttpServerImplementation, OfficeFloorListener {

	static {
		ResourceLeakDetector.setLevel(Level.DISABLED);
	}

	/**
	 * {@link HttpServerImplementationContext}.
	 */
	private HttpServerImplementationContext context;

	/**
	 * {@link ExternalServiceInput}.
	 */
	private ExternalServiceInput<ServerHttpConnection> serviceInput;

	/**
	 * {@link EventLoopGroup}.
	 */
	private EventLoopGroup loopGroup = null;

	/*
	 * ================== HttpServerImplementation ==================
	 */

	@Override
	public void configureHttpServer(HttpServerImplementationContext context) {
		this.context = context;

		// Obtain the service input for handling requests
		this.serviceInput = context.getExternalServiceInput();

		// Hook into OfficeFloor life-cycle
		context.getOfficeFloorDeployer().addOfficeFloorListener(this);
	}

	/*
	 * ===================== OfficeFloorListener ====================
	 */

	@Override
	public void officeFloorOpened(OfficeFloorEvent event) throws Exception {

		// Configure the HTTP Server
		if (Epoll.isAvailable()) {
			this.startHttpServer(new EpollEventLoopGroup(), EpollServerSocketChannel.class);
		} else if (KQueue.isAvailable()) {
			this.startHttpServer(new KQueueEventLoopGroup(), KQueueServerSocketChannel.class);
		} else {
			this.startHttpServer(new NioEventLoopGroup(), NioServerSocketChannel.class);
		}
	}

	@Override
	public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
		this.loopGroup.shutdownGracefully().sync();
	}

	/**
	 * Starts the HTTP Server.
	 * 
	 * @param loopGroup
	 *            {@link EventLoopGroup}.
	 * @param serverChannelClass
	 *            {@link ServerChannel} {@link Class}.
	 * @throws InterruptedException
	 *             If fails to start.
	 */
	private void startHttpServer(EventLoopGroup loopGroup, Class<? extends ServerChannel> serverChannelClass)
			throws InterruptedException {
		this.loopGroup = loopGroup;

		// Configure the port
		InetSocketAddress port = new InetSocketAddress(this.context.getHttpPort());

		// Configure and start the server
		ServerBootstrap bootstrap = new ServerBootstrap();
		if (loopGroup instanceof EpollEventLoopGroup) {
			bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
		}
		bootstrap.option(ChannelOption.SO_BACKLOG, 8192);
		bootstrap.option(ChannelOption.SO_REUSEADDR, true);
		bootstrap.group(loopGroup).channel(serverChannelClass).childHandler(new OfficeFloorNettyInitialiser());
		bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
		bootstrap.bind(port).sync().channel();
	}

	/**
	 * {@link OfficeFloor} {@link ChannelInitializer}.
	 */
	private class OfficeFloorNettyInitialiser extends ChannelInitializer<SocketChannel> {

		/*
		 * ================== ChannelInitializer =====================
		 */

		@Override
		protected void initChannel(SocketChannel channel) throws Exception {
			ChannelPipeline pipeline = channel.pipeline();
			pipeline.addLast("encoder", new HttpResponseEncoder());
			pipeline.addLast("decoder", new HttpRequestDecoder(4096, 8192, 8192, false));
			pipeline.addLast("handler", new OfficeFloorServerHandler());
		}
	}

	/**
	 * {@link OfficeFloor} {@link ChannelInboundHandlerAdapter}.
	 */
	private class OfficeFloorServerHandler extends ChannelInboundHandlerAdapter {

		/**
		 * Services the {@link HttpRequest}.
		 * 
		 * @param context
		 *            {@link ChannelHandlerContext}.
		 * @param request
		 *            {@link HttpRequest}.
		 * @throws Exception
		 *             If fail servicing.
		 */
		private void service(ChannelHandlerContext context, HttpRequest request) throws Exception {

			// Service the request
			NettyServerHttpConnection connection = new NettyServerHttpConnection(context, request, false);
			NettyHttpServerImplementation.this.serviceInput.service(connection, (escalation) -> {
				
				if (escalation != null) {
					escalation.printStackTrace();
				}
				
				// Ensure send the response
				connection.getHttpResponse().send();
			});
		}

		/*
		 * ================== ChannelInboundHandlerAdapter ============
		 */

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			if (msg instanceof HttpRequest) {
				try {
					HttpRequest request = (HttpRequest) msg;
					this.service(ctx, request);
				} finally {
					ReferenceCountUtil.release(msg);
				}
			} else {
				ctx.fireChannelRead(msg);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			ctx.close();
		}

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
		}
	}

}