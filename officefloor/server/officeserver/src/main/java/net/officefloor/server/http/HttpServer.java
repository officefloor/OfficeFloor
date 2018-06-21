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
package net.officefloor.server.http;

import java.net.Socket;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceCleanupEscalationHandler;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.server.http.impl.DateHttpHeaderClock;
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
	 * @param propertyName
	 *            Name of {@link Property}.
	 * @param context
	 *            {@link SourceContext}.
	 * @param defaultValue
	 *            {@link Supplier} of the default value.
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
	 * @param propertyName
	 *            Name of {@link Property}.
	 * @param context
	 *            {@link SourceContext}.
	 * @param defaultValue
	 *            {@link Supplier} of the default value.
	 * @return Value for the {@link Property}.
	 */
	public static int getPropertyInteger(String propertyName, SourceContext context, Supplier<Integer> defaultValue) {
		return Integer.parseInt(getPropertyString(propertyName, context, () -> String.valueOf(defaultValue.get())));
	}

	/**
	 * Obtains the {@link SSLContext} from configuration.
	 * 
	 * @param context
	 *            {@link SourceContext}.
	 * @return {@link SSLContext}.
	 * @throws Exception
	 *             If fails to load the {@link SSLContext} from configuration.
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
		SslContextSource sslContextSource = (SslContextSource) sslContextSourceClass.newInstance();
		return sslContextSource.createSslContext(context);
	}

	/**
	 * Convenience method to obtain the <code>Server</code> {@link HttpHeaderValue}.
	 * 
	 * @param context
	 *            {@link HttpServerImplementationContext}.
	 * @param suffix
	 *            Optional suffix. May be <code>null</code>.
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
	 * {@link DateHttpHeaderClock} {@link Timer}.
	 */
	private volatile Timer dateTimer;

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
	 * {@link SSLContext}.
	 */
	private final SSLContext sslContext;

	/**
	 * Instantiates the {@link HttpServer} from configuration.
	 * 
	 * @param serviceInput
	 *            {@link DeployedOfficeInput} servicing the
	 *            {@link ServerHttpConnection}.
	 * @param officeFloorDeployer
	 *            {@link OfficeFloorDeployer}.
	 * @param context
	 *            {@link OfficeFloorSourceContext}.
	 * @throws Exception
	 *             If fails to create the {@link HttpServer} from configuration.
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
			DateHttpHeaderCockImpl clock = new DateHttpHeaderCockImpl();
			this.dateHttpHeaderClock = clock;

			// Trigger updates to date header
			officeFloorDeployer.addOfficeFloorListener(new OfficeFloorListener() {
				@Override
				public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
					// Start tracking time (update every second)
					HttpServer.this.dateTimer = new Timer(true);
					HttpServer.this.dateTimer.schedule(clock, 0, 1000);
				}

				@Override
				public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
					// Stop timer
					HttpServer.this.dateTimer.cancel();
				}
			});

		} else {
			// No date header
			this.dateHttpHeaderClock = null;
		}

		// Load whether to include stack traces
		this.isIncludeEscalationStackTrace = Boolean
				.parseBoolean(getPropertyString(PROPERTY_INCLUDE_STACK_TRACE, context, () -> Boolean.TRUE.toString()));

		// Obtain the server implementation
		List<HttpServerImplementation> implementations = new ArrayList<>();
		for (HttpServerImplementation implementation : ServiceLoader.load(HttpServerImplementation.class)) {
			implementations.add(implementation);
		}
		HttpServerImplementation implementation;
		switch (implementations.size()) {
		case 0:
			// Use default implementation
			implementation = (HttpServerImplementation) context.loadClass(DEFAULT_HTTP_SERVER_IMPLEMENTATION_CLASS_NAME)
					.newInstance();
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
		this.sslContext = getSslContext(context);

		// Configure the HTTP server
		this.configure(serviceInput, officeFloorDeployer, context);
	}

	/**
	 * Instantiates the {@link HttpServer} from direct configuration.
	 * 
	 * @param implementation
	 *            {@link HttpServerImplementation}.
	 * @param serverLocation
	 *            {@link HttpServerLocation}.
	 * @param serverName
	 *            Server name. May be <code>null</code>.
	 * @param dateHttpHeaderClock
	 *            {@link DateHttpHeaderClock}. May be <code>null</code>.
	 * @param isIncludeEscalationStackTrace
	 *            Indicates whether to include {@link Escalation} stack trace in
	 *            {@link HttpResponse}.
	 * @param sslContext
	 *            {@link SSLContext}.
	 * @param serviceInput
	 *            {@link DeployedOfficeInput} servicing the
	 *            {@link ServerHttpConnection}.
	 * @param officeFloorDeployer
	 *            {@link OfficeFloorDeployer}.
	 * @param context
	 *            {@link OfficeFloorSourceContext}.
	 */
	public HttpServer(HttpServerImplementation implementation, HttpServerLocation serverLocation, String serverName,
			DateHttpHeaderClock dateHttpHeaderClock, boolean isIncludeEscalationStackTrace, SSLContext sslContext,
			DeployedOfficeInput serviceInput, OfficeFloorDeployer officeFloorDeployer,
			OfficeFloorSourceContext context) {
		this.serverLocation = serverLocation;
		this.serverName = serverName;
		this.dateHttpHeaderClock = dateHttpHeaderClock;
		this.isIncludeEscalationStackTrace = isIncludeEscalationStackTrace;
		this.serverImplementation = implementation;
		this.sslContext = sslContext;

		// Configure the HTTP server
		this.configure(serviceInput, officeFloorDeployer, context);
	}

	/**
	 * Configures the {@link HttpServer}.
	 * 
	 * @param serviceInput
	 *            {@link DeployedOfficeInput} servicing the
	 *            {@link ServerHttpConnection}.
	 * @param officeFloorDeployer
	 *            {@link OfficeFloorDeployer}.
	 * @param context
	 *            {@link OfficeFloorSourceContext}.
	 */
	private void configure(DeployedOfficeInput serviceInput, OfficeFloorDeployer officeFloorDeployer,
			OfficeFloorSourceContext context) {

		// Configure the HTTP server
		this.serverImplementation.configureHttpServer(new HttpServerImplementationContext() {

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
			public SSLContext getSslContext() {
				return HttpServer.this.sslContext;
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
	private static class DateHttpHeaderCockImpl extends TimerTask implements DateHttpHeaderClock {

		/**
		 * <code>Date</code> {@link HttpHeaderValue}.
		 */
		private volatile HttpHeaderValue httpHeader;

		/**
		 * Instantiate.
		 */
		public DateHttpHeaderCockImpl() {
			// Set initial date
			this.run();
		}

		/*
		 * ============== DateHttpHeaderClock =================
		 */

		@Override
		public HttpHeaderValue getDateHttpHeaderValue() {
			return this.httpHeader;
		}

		/*
		 * ==================== TimerTask ======================
		 */

		@Override
		public void run() {
			// Update to new time
			String now = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
			this.httpHeader = new HttpHeaderValue(now);
		}
	}

}