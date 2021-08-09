/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server.http;

import java.net.Socket;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceCleanupEscalationHandler;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.server.http.impl.HttpServerLocationImpl;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;
import net.officefloor.server.ssl.SslContextSource;

/**
 * HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServer {

	/**
	 * Name of {@link Property} specifying the name for the <code>Server</code>
	 * {@link HttpHeader}.
	 */
	public static final String PROPERTY_HTTP_SERVER_NAME = "http.server.name";

	/**
	 * Name of {@link Property} specifying whether to send the <code>Date</code>
	 * {@link HttpHeader}. Value is <code>true</code>/<code>false</code>.
	 */
	public static final String PROPERTY_HTTP_DATE_HEADER = "http.date.header";

	/**
	 * Name of {@link Property} to include the stack trace.
	 */
	public static final String PROPERTY_INCLUDE_STACK_TRACE = "http.include.stacktrace";

	/**
	 * Name of {@link Property} for the {@link SslContextSource}.
	 */
	public static final String PROPERTY_SSL_CONTEXT_SOURCE = "ssl.source";

	/**
	 * Value for {@link #PROPERTY_SSL_CONTEXT_SOURCE} to indicate that this server
	 * is behind a reverse proxy. This enables the reverse proxy to handle SSL and
	 * communicate with this server via insecure {@link Socket} (but stll appear as
	 * a secure {@link ServerHttpConnection}).
	 */
	public static final String SSL_REVERSE_PROXIED = "reverse-proxied";

	/**
	 * {@link Class} name of the default {@link HttpServerImplementation}.
	 */
	public static String DEFAULT_HTTP_SERVER_IMPLEMENTATION_CLASS_NAME = "net.officefloor.server.http.OfficeFloorHttpServerImplementation";

	/**
	 * Obtains the {@link Property} value.
	 * 
	 * @param propertyName Name of {@link Property}.
	 * @param context      {@link SourceContext}.
	 * @param defaultValue {@link Supplier} of the default value.
	 * @return Value for the {@link Property}.
	 */
	public static String getPropertyString(String propertyName, SourceContext context, Supplier<String> defaultValue) {

		// Attempt to obtain specific configuration from context
		String value = context.getProperty(propertyName, null);
		if (CompileUtil.isBlank(value)) {

			// Not configured in context, so attempt System properties
			value = System.getProperty(propertyName, null);
			if (CompileUtil.isBlank(value)) {

				// Not configured, so use default
				value = defaultValue.get();
			}
		}

		// Return the value
		return value;
	}

	/**
	 * Obtains the {@link Property} value.
	 * 
	 * @param propertyName Name of {@link Property}.
	 * @param context      {@link SourceContext}.
	 * @param defaultValue {@link Supplier} of the default value.
	 * @return Value for the {@link Property}.
	 */
	public static int getPropertyInteger(String propertyName, SourceContext context, Supplier<Integer> defaultValue) {
		return Integer.parseInt(getPropertyString(propertyName, context, () -> String.valueOf(defaultValue.get())));
	}

	/**
	 * Obtains the {@link SSLContext} from configuration.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link SSLContext}.
	 * @throws Exception If fails to load the {@link SSLContext} from configuration.
	 */
	public static SSLContext getSslContext(SourceContext context) throws Exception {

		// Obtain the source context
		String sslContextSourceClassName = getPropertyString(PROPERTY_SSL_CONTEXT_SOURCE, context, () -> null);
		if (sslContextSourceClassName == null) {
			// Not configured, so default to OfficeFloor test
			sslContextSourceClassName = OfficeFloorDefaultSslContextSource.class.getName();
		}
		if (SSL_REVERSE_PROXIED.equals(sslContextSourceClassName)) {
			return null; // let be secure, without SSL
		}

		// Create the SSL context
		Class<?> sslContextSourceClass = context.loadClass(sslContextSourceClassName);
		SslContextSource sslContextSource = (SslContextSource) sslContextSourceClass.getDeclaredConstructor()
				.newInstance();
		return sslContextSource.createSslContext(context);
	}

	/**
	 * Convenience method to obtain the <code>Server</code> {@link HttpHeaderValue}.
	 * 
	 * @param context {@link HttpServerImplementationContext}.
	 * @param suffix  Optional suffix. May be <code>null</code>.
	 * @return <code>Server</code> {@link HttpHeaderValue} or <code>null</code> if
	 *         not configured.
	 */
	public static HttpHeaderValue getServerHttpHeaderValue(HttpServerImplementationContext context, String suffix) {
		String serverName = context.getServerName();
		HttpHeaderValue serverHttpHeaderValue = (serverName == null) ? null
				: new HttpHeaderValue(serverName + (suffix == null ? "" : " " + suffix));
		return serverHttpHeaderValue;
	}

	/**
	 * {@link HttpServerLocation}.
	 */
	private final HttpServerLocation serverLocation;

	/**
	 * Name of the server. May be <code>null</code>.
	 */
	private final String serverName;

	/**
	 * {@link DateHttpHeaderClock}. May be <code>null</code>.
	 */
	private final DateHttpHeaderClock dateHttpHeaderClock;

	/**
	 * Indicates whether to include {@link Escalation} stack trace in the
	 * {@link HttpResponse}.
	 */
	private final boolean isIncludeEscalationStackTrace;

	/**
	 * {@link HttpServerImplementation}.
	 */
	private final HttpServerImplementation serverImplementation;

	/**
	 * Indicates if can create the {@link SSLContext}.
	 */
	private final boolean isCreateSslContext;

	/**
	 * {@link SSLContext}.
	 */
	private final SSLContext sslContext;

	/**
	 * Instantiates the {@link HttpServer} from configuration.
	 * 
	 * @param serviceInput        {@link DeployedOfficeInput} servicing the
	 *                            {@link ServerHttpConnection}.
	 * @param officeFloorDeployer {@link OfficeFloorDeployer}.
	 * @param context             {@link OfficeFloorSourceContext}.
	 * @throws Exception If fails to create the {@link HttpServer} from
	 *                   configuration.
	 */
	public HttpServer(DeployedOfficeInput serviceInput, OfficeFloorDeployer officeFloorDeployer,
			OfficeFloorSourceContext context) throws Exception {

		// Load the server location
		this.serverLocation = new HttpServerLocationImpl(context);

		// Obtain the server name
		this.serverName = getPropertyString(PROPERTY_HTTP_SERVER_NAME, context, () -> null);

		// Determine if date HTTP header
		boolean isDateHttpHeader = Boolean
				.parseBoolean(getPropertyString(PROPERTY_HTTP_DATE_HEADER, context, () -> Boolean.toString(false)));
		if (isDateHttpHeader) {
			// Create date header clock
			this.dateHttpHeaderClock = new DateHttpHeaderCockImpl(context.getClock((time) -> {
				String now = DateTimeFormatter.RFC_1123_DATE_TIME
						.format(Instant.ofEpochSecond(time).atZone(ZoneOffset.UTC));
				return new HttpHeaderValue(now);
			}));
		} else {
			// No date header
			this.dateHttpHeaderClock = null;
		}

		// Load whether to include stack traces
		this.isIncludeEscalationStackTrace = Boolean
				.parseBoolean(getPropertyString(PROPERTY_INCLUDE_STACK_TRACE, context, () -> Boolean.TRUE.toString()));

		// Obtain the server implementation
		List<HttpServerImplementation> implementations = new ArrayList<>();
		for (HttpServerImplementation implementation : context
				.loadOptionalServices(HttpServerImplementationFactory.class)) {
			implementations.add(implementation);
		}
		HttpServerImplementation implementation;
		switch (implementations.size()) {
		case 0:
			// Use default implementation
			implementation = (HttpServerImplementation) context.loadClass(DEFAULT_HTTP_SERVER_IMPLEMENTATION_CLASS_NAME)
					.getDeclaredConstructor().newInstance();
			break;

		case 1:
			// Use the implementation configured on class path
			implementation = implementations.get(0);
			break;

		default:
			// Illegal to have more than one server implementation
			StringBuilder message = new StringBuilder();
			boolean isFirst = true;
			for (HttpServerImplementation instance : implementations) {
				if (!isFirst) {
					message.append(", ");
				}
				isFirst = false;
				message.append(instance.getClass());
			}
			String issueMessage = "More than one " + HttpServerImplementation.class.getSimpleName() + " available ("
					+ message.toString() + ").  May only have one configured on class path.";
			officeFloorDeployer.addIssue(issueMessage);
			throw new IllegalStateException(issueMessage);
		}
		this.serverImplementation = implementation;

		// Obtain the SSL context
		this.isCreateSslContext = true;
		this.sslContext = null;

		// Configure the HTTP server
		this.configure(serviceInput, officeFloorDeployer, context);
	}

	/**
	 * Instantiates the {@link HttpServer} from direct configuration.
	 * 
	 * @param implementation                {@link HttpServerImplementation}.
	 * @param serverLocation                {@link HttpServerLocation}.
	 * @param serverName                    Server name. May be <code>null</code>.
	 * @param dateHttpHeaderClock           {@link DateHttpHeaderClock}. May be
	 *                                      <code>null</code>.
	 * @param isIncludeEscalationStackTrace Indicates whether to include
	 *                                      {@link Escalation} stack trace in
	 *                                      {@link HttpResponse}.
	 * @param sslContext                    {@link SSLContext}.
	 * @param serviceInput                  {@link DeployedOfficeInput} servicing
	 *                                      the {@link ServerHttpConnection}.
	 * @param officeFloorDeployer           {@link OfficeFloorDeployer}.
	 * @param context                       {@link OfficeFloorSourceContext}.
	 * @throws Exception If fails to configure the {@link HttpServerImplementation}.
	 */
	public HttpServer(HttpServerImplementation implementation, HttpServerLocation serverLocation, String serverName,
			DateHttpHeaderClock dateHttpHeaderClock, boolean isIncludeEscalationStackTrace, SSLContext sslContext,
			DeployedOfficeInput serviceInput, OfficeFloorDeployer officeFloorDeployer, OfficeFloorSourceContext context)
			throws Exception {
		this.serverLocation = serverLocation;
		this.serverName = serverName;
		this.dateHttpHeaderClock = dateHttpHeaderClock;
		this.isIncludeEscalationStackTrace = isIncludeEscalationStackTrace;
		this.serverImplementation = implementation;
		this.isCreateSslContext = false;
		this.sslContext = sslContext;

		// Configure the HTTP server
		this.configure(serviceInput, officeFloorDeployer, context);
	}

	/**
	 * Configures the {@link HttpServer}.
	 * 
	 * @param serviceInput        {@link DeployedOfficeInput} servicing the
	 *                            {@link ServerHttpConnection}.
	 * @param officeFloorDeployer {@link OfficeFloorDeployer}.
	 * @param context             {@link OfficeFloorSourceContext}.
	 * @throws Exception If fails to configure the {@link HttpServerImplementation}.
	 */
	private void configure(DeployedOfficeInput serviceInput, OfficeFloorDeployer officeFloorDeployer,
			OfficeFloorSourceContext context) throws Exception {

		// Configure the HTTP server
		this.serverImplementation.configureHttpServer(new HttpServerImplementationContext() {

			/**
			 * Cached {@link SSLContext} to return.
			 */
			private SSLContext sslContext = null;

			/*
			 * ================= HttpServerImplementationContext ==============
			 */

			@Override
			public HttpServerLocation getHttpServerLocation() {
				return HttpServer.this.serverLocation;
			}

			@Override
			public String getServerName() {
				return HttpServer.this.serverName;
			}

			@Override
			public DateHttpHeaderClock getDateHttpHeaderClock() {
				return HttpServer.this.dateHttpHeaderClock;
			}

			@Override
			public boolean isIncludeEscalationStackTrace() {
				return HttpServer.this.isIncludeEscalationStackTrace;
			}

			@Override
			public SSLContext getSslContext() throws Exception {

				// Lazy obtain the SSL context
				if (this.sslContext == null) {
					if (HttpServer.this.isCreateSslContext) {
						// Create the SSL context
						this.sslContext = HttpServer.getSslContext(context);
					} else {
						// Use configured SSL context
						this.sslContext = HttpServer.this.sslContext;
					}
				}

				// Return the SSL context
				return this.sslContext;
			}

			@Override
			public DeployedOfficeInput getInternalServiceInput() {
				return serviceInput;
			}

			@Override
			public <M extends ManagedObject> ExternalServiceInput<ServerHttpConnection, M> getExternalServiceInput(
					Class<M> managedObjectType,
					ExternalServiceCleanupEscalationHandler<? super M> cleanupEscalationHandler) {
				return serviceInput.addExternalServiceInput(ServerHttpConnection.class, managedObjectType,
						cleanupEscalationHandler);
			}

			@Override
			public OfficeFloorDeployer getOfficeFloorDeployer() {
				return officeFloorDeployer;
			}

			@Override
			public OfficeFloorSourceContext getOfficeFloorSourceContext() {
				return context;
			}
		});
	}

	/**
	 * Obtains the {@link HttpServerLocation}.
	 * 
	 * @return {@link HttpServerLocation}.
	 */
	public HttpServerLocation getHttpServerLocation() {
		return this.serverLocation;
	}

	/**
	 * Obtains the {@link HttpServerImplementation}.
	 * 
	 * @return {@link HttpServerImplementation}.
	 */
	public HttpServerImplementation getHttpServerImplementation() {
		return this.serverImplementation;
	}

	/**
	 * Obtains the {@link SSLContext}.
	 * 
	 * @return {@link SSLContext}.
	 */
	public SSLContext getSslContext() {
		return this.sslContext;
	}

	/**
	 * {@link DateHttpHeaderClock} implementation.
	 */
	private static class DateHttpHeaderCockImpl implements DateHttpHeaderClock {

		/**
		 * <code>Date</code> {@link HttpHeaderValue} {@link Clock}.
		 */
		private final Clock<HttpHeaderValue> httpHeaderClock;

		/**
		 * Instantiate.
		 * 
		 * @param httpHeaderClock {@link Clock} for the <code>Date</code>
		 *                        {@link HttpHeaderValue}.
		 */
		private DateHttpHeaderCockImpl(Clock<HttpHeaderValue> httpHeaderClock) {
			this.httpHeaderClock = httpHeaderClock;
		}

		/*
		 * ============== DateHttpHeaderClock =================
		 */

		@Override
		public HttpHeaderValue getDateHttpHeaderValue() {
			return this.httpHeaderClock.getTime();
		}
	}

}
