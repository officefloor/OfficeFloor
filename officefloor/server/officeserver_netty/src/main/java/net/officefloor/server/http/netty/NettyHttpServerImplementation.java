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
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.Supplier;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
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
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.HttpResponseWriter;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.SerialisableHttpHeader;
import net.officefloor.server.http.impl.ServerHttpConnectionImpl;
import net.officefloor.server.http.impl.WritableHttpHeader;
import net.officefloor.server.stream.PooledBuffer;
import net.officefloor.server.stream.impl.ByteSequence;

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
	private ExternalServiceInput<ServerHttpConnection, ServerHttpConnectionImpl> serviceInput;

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
		this.serviceInput = context.getExternalServiceInput(ServerHttpConnectionImpl.class);

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

			// Supply the method
			Supplier<HttpMethod> methodSupplier = () -> {
				io.netty.handler.codec.http.HttpMethod nettyHttpMethod = request.method();
				String methodName = nettyHttpMethod.asciiName().toString();
				switch (methodName) {
				case "GET":
					return HttpMethod.GET;
				case "POST":
					return HttpMethod.POST;
				case "PUT":
					return HttpMethod.PUT;
				case "DELETE":
					return HttpMethod.DELETE;
				case "CONNECT":
					return HttpMethod.CONNECT;
				case "HEAD":
					return HttpMethod.HEAD;
				case "OPTIONS":
					return HttpMethod.OPTIONS;
				default:
					return new HttpMethod(methodName);
				}
			};

			// Supply the request URI
			Supplier<String> requestUriSupplier = () -> request.uri();

			// Obtain the version
			HttpVersion version;
			String versionName = request.protocolVersion().protocolName();
			switch (versionName) {
			case "HTTP/1.0":
				version = HttpVersion.HTTP_1_0;
			case "HTTP/1.1":
				version = HttpVersion.HTTP_1_1;
			default:
				version = new HttpVersion(versionName);
			}

			// Obtain the request headers
			NonMaterialisedHttpHeaders requestHeaders = new NonMaterialisedHttpHeaders() {

				@Override
				public Iterator<NonMaterialisedHttpHeader> iterator() {
					Iterator<Entry<CharSequence, CharSequence>> iterator = request.headers().iteratorCharSequence();
					return new Iterator<NonMaterialisedHttpHeader>() {

						@Override
						public boolean hasNext() {
							return iterator.hasNext();
						}

						@Override
						public NonMaterialisedHttpHeader next() {
							Entry<CharSequence, CharSequence> entry = iterator.next();
							CharSequence entryName = entry.getKey();
							return new NonMaterialisedHttpHeader() {

								@Override
								public boolean isNameEqual(CharSequence name) {
									if (entryName.length() == name.length()) {
										for (int i = 0; i < name.length(); i++) {
											if (entryName.charAt(i) != name.charAt(i)) {
												return false; // different
											}
										}
										return true; // as here, equal
									}
									return false; // not equal on length
								}

								@Override
								public HttpHeader materialiseHttpHeader() {
									return new SerialisableHttpHeader(entry.getKey().toString(),
											entry.getValue().toString());
								}
							};
						}
					};
				}

				@Override
				public int length() {
					return request.headers().size();
				}
			};

			// Obtain the request entity content
			ByteSequence requestEntity = null;
			if (request instanceof FullHttpRequest) {
				FullHttpRequest fullRequest = (FullHttpRequest) request;
				ByteBuf entityByteBuf = fullRequest.content();
				requestEntity = new ByteSequence() {

					@Override
					public byte byteAt(int index) {
						return entityByteBuf.getByte(index);
					}

					@Override
					public int length() {
						return entityByteBuf.capacity();
					}
				};
			}

			// Create the response
			FullHttpResponse response = new DefaultFullHttpResponse(io.netty.handler.codec.http.HttpVersion.HTTP_1_1,
					HttpResponseStatus.OK, false);

			// Handle response
			HttpResponseWriter<ByteBuf> responseWriter = (responseVersion, status, responseHttpHeaders,
					responseHttpEntity) -> {

				// Specify the status
				HttpResponseStatus nettyStatus = HttpResponseStatus.valueOf(status.getStatusCode());
				response.setStatus(nettyStatus);

				// Create and write the response
				HttpHeaders headers = response.headers();
				for (WritableHttpHeader header : responseHttpHeaders) {
					headers.add(header.getName(), header.getValue());
				}

				// Write the response
				ByteBuf responseBuffer = response.content();
				for (PooledBuffer<ByteBuf> pooledBuffer : responseHttpEntity) {
					if (pooledBuffer.isReadOnly()) {
						responseBuffer.writeBytes(pooledBuffer.getReadOnlyByteBuffer());
					} else {
						responseBuffer.writeBytes(pooledBuffer.getBuffer());
					}
				}

				// Send the response
				context.write(response);
			};

			// Create the Netty buffer pool
			NettyBufferPool bufferPool = new NettyBufferPool(response);

			// Create the Server HTTP connection
			ServerHttpConnectionImpl connection = new ServerHttpConnectionImpl(false, methodSupplier,
					requestUriSupplier, version, requestHeaders, requestEntity, responseWriter, bufferPool);

			// Service the request
			NettyHttpServerImplementation.this.serviceInput.service(connection, (escalation) -> {

				if (escalation != null) {
					escalation.printStackTrace();
				}

				// Ensure send response
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