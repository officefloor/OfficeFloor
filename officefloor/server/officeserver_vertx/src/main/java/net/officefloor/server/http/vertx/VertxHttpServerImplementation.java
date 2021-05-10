package net.officefloor.server.http.vertx;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.impl.SSLHelper;
import io.vertx.core.net.impl.TCPServerBase;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpServerImplementationFactory;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;

/**
 * {@link Vertx} {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class VertxHttpServerImplementation
		implements HttpServerImplementation, HttpServerImplementationFactory, OfficeFloorListener {

	/**
	 * System property name to configure the {@link Vertx} time outs for starting /
	 * stopping.
	 */
	public static final String SYSTEM_PROPERTY_VERTX_TIMEOUT = "officefloor.vertx.timeout";

	/**
	 * {@link Vertx} start / stop timeout.
	 */
	private static final long VERTX_TIMEOUT = Long.getLong(SYSTEM_PROPERTY_VERTX_TIMEOUT, 10 * 1000);

	/**
	 * {@link Vertx} asynchronous operation.
	 */
	@FunctionalInterface
	public interface VertxAsyncOperation<T> {
		void trigger(Handler<AsyncResult<T>> handler);
	}

	/**
	 * Blocks on the {@link Vertx} operation.
	 * 
	 * @param <T>       Result of {@link Vertx} operation.
	 * @param operation {@link VertxAsyncOperation} to block on.
	 * @return Result of {@link Vertx} operation.
	 * @throws Exception If {@link VertxAsyncOperation} fails or times out.
	 */
	public static <T> T block(VertxAsyncOperation<T> operation) throws Exception {

		boolean[] isComplete = new boolean[] { false };
		@SuppressWarnings("unchecked")
		T[] returnValue = (T[]) new Object[] { null };
		Exception[] failure = new Exception[] { null };

		// Trigger operation
		operation.trigger((result) -> {
			synchronized (returnValue) {
				try {
					if (result.succeeded()) {
						returnValue[0] = result.result();
					} else {
						Throwable cause = result.cause();
						if (cause instanceof Exception) {
							failure[0] = (Exception) cause;
						} else {
							failure[0] = new OfficeFloorVertxException(cause);
						}
					}
				} finally {
					isComplete[0] = true;
					returnValue.notifyAll();
				}
			}
		});

		// Wait until complete, fails or times out
		long endTime = System.currentTimeMillis() + VERTX_TIMEOUT;
		synchronized (returnValue) {
			for (;;) {

				// Determine if complete
				if (isComplete[0]) {

					// Determine if failure
					if (failure[0] != null) {
						throw failure[0];
					}

					// No failure, so provide result
					return returnValue[0];
				}

				// Determine if timed out
				if (System.currentTimeMillis() > endTime) {
					throw new TimeoutException("Vertx operation took too long (" + VERTX_TIMEOUT + " milliseconds)");
				}

				// Wait some time for completion
				returnValue.wait(10);
			}
		}
	}

	/**
	 * Blocks on {@link Void} {@link VertxAsyncOperation}.
	 * 
	 * @param operation {@link VertxAsyncOperation}.
	 * @throws Exception If {@link VertxAsyncOperation} fails or times out.
	 */
	public static void blockVoid(VertxAsyncOperation<Void> operation) throws Exception {
		block(operation);
	}

	/**
	 * Wrap non-{@link Exception}.
	 */
	private static class OfficeFloorVertxException extends Exception {

		/**
		 * Serialisaion verstion.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Instantiate.
		 * 
		 * @param cause Cause.
		 */
		private OfficeFloorVertxException(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * {@link HttpServerImplementationContext}.
	 */
	private HttpServerImplementationContext context;

	/**
	 * {@link ExternalServiceInput}.
	 */
	@SuppressWarnings("rawtypes")
	private ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> serviceInput;

	/**
	 * {@link Vertx}.
	 */
	private Vertx vertx;

	/*
	 * ===================== HttpServerImplementationFactory =====================
	 */

	@Override
	public HttpServerImplementation createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== HttpServerImplementation =========================
	 */

	@Override
	public void configureHttpServer(HttpServerImplementationContext context) throws Exception {
		this.context = context;

		// Obtain the service input for handling requests
		this.serviceInput = context.getExternalServiceInput(ProcessAwareServerHttpConnectionManagedObject.class,
				ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());

		// Register to start Vertx on OfficeFloor start
		context.getOfficeFloorDeployer().addOfficeFloorListener(this);
	}

	/*
	 * =========================== OfficeFloorListener =============================
	 */

	@Override
	public void officeFloorOpened(OfficeFloorEvent event) throws Exception {

		// Establish vertx server
		this.vertx = Vertx.vertx();

		// Obtain the server location
		HttpServerLocation serverLocation = this.context.getHttpServerLocation();

		// Create the handler
		HttpHeaderValue serverName = net.officefloor.server.http.HttpServer.getServerHttpHeaderValue(context, "Vertx");
		OfficeFloorVertxHandler handler = new OfficeFloorVertxHandler(serverLocation, serverName,
				this.context.getDateHttpHeaderClock(), this.context.isIncludeEscalationStackTrace(), this.serviceInput);

		// Start the HTTP server
		int httpPort = serverLocation.getHttpPort();
		HttpServer httpServer = vertx.createHttpServer(new HttpServerOptions().setPort(httpPort));
		httpServer.requestHandler(handler);
		httpServer = block(httpServer::listen);

		// Determine if start HTTPS server
		int httpsPort = serverLocation.getHttpsPort();
		if (httpsPort > 0) {

			// Start the HTTPS server
			SSLContext sslContext = this.context.getSslContext();
			SslContext nettyContext = new JdkSslContext(sslContext, false, null,
					(requestedCiphers, defaultCiphers, supportedCiphers) -> {
						if (requestedCiphers == null) {
							return defaultCiphers.toArray(String[]::new);
						} else {
							List<String> ciphers = new ArrayList<>(defaultCiphers.size());
							requestedCiphers.forEach((cipher) -> {
								if (defaultCiphers.contains(cipher)) {
									ciphers.add(cipher);
								}
							});
							return ciphers.toArray(String[]::new);
						}
					}, null, ClientAuth.NONE, new String[] { sslContext.getProtocol() }, true);

			// Create the server
			HttpServer httpsServer = vertx.createHttpServer(new HttpServerOptions().setSsl(true).setPort(7979));
			httpsServer.requestHandler(handler);

			// Specify the SSL context
			Field sslHelperField = TCPServerBase.class.getDeclaredField("sslHelper");
			sslHelperField.setAccessible(true);
			SSLHelper sslHelper = (SSLHelper) sslHelperField.get(httpsServer);
			Field sslContextField = SSLHelper.class.getDeclaredField("sslContext");
			sslContextField.setAccessible(true);
			sslContextField.set(sslHelper, nettyContext);

			// Start the HTTPS server
			httpsServer = block(httpsServer::listen);
		}
	}

	@Override
	public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
		if (this.vertx != null) {
			blockVoid(this.vertx::close);
		}
	}

}
