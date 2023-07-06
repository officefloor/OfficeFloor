package net.officefloor.server.google.function;

import java.util.logging.Logger;

import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;

/**
 * Google Function {@link HttpServerImplementation}.
 */
public class GoogleFunctionHttpServerImplementation implements HttpServerImplementation {

	/**
	 * {@link ThreadLocal} capture of
	 * {@link GoogleFunctionHttpServerImplementation}.
	 */
	private static final ThreadLocal<GoogleFunctionHttpServerImplementation> captureGoogleFunctionHttpServerImplementation = new ThreadLocal<>();

	/**
	 * Obtains the {@link GoogleFunctionHttpServerImplementation}.
	 * 
	 * @return {@link GoogleFunctionHttpServerImplementation}.
	 */
	public static GoogleFunctionHttpServerImplementation getGoogleFunctionHttpServerImplementation() {
		try {
			return captureGoogleFunctionHttpServerImplementation.get();
		} finally {
			// Ensure remove from thread context
			captureGoogleFunctionHttpServerImplementation.remove();
		}
	}

	/**
	 * {@link HttpServerLocation}.
	 */
	private HttpServerLocation location;

	/**
	 * {@link ExternalServiceInput} to service the AWS API event.
	 */
	@SuppressWarnings("rawtypes")
	private ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> input;

	/**
	 * Indicates if include stack trace in HTTP responses.
	 */
	private boolean isIncludeEscalationStackTrace;

	/**
	 * {@link Logger}.
	 */
	private Logger logger;

	/**
	 * Obtains the {@link HttpServerLocation}.
	 * 
	 * @return {@link HttpServerLocation}.
	 */
	public HttpServerLocation getHttpServerLocation() {
		return this.location;
	}

	/**
	 * Obtains the {@link ExternalServiceInput} to service the AWS API event.
	 * 
	 * @return {@link ExternalServiceInput} to service the AWS API event.
	 */
	@SuppressWarnings("rawtypes")
	public ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> getInput() {
		return this.input;
	}

	/**
	 * Indicates if include stack trace in HTTP responses.
	 * 
	 * @return <code>true</code> to include stack trace.
	 */
	public boolean isIncludeEscalationStackTrace() {
		return this.isIncludeEscalationStackTrace;
	}

	/**
	 * Obtains the {@link Logger}.
	 * 
	 * @return {@link Logger}.
	 */
	public Logger getLogger() {
		return this.logger;
	}

	/*
	 * ================== HttpServerImplementation ======================
	 */

	@Override
	public void configureHttpServer(HttpServerImplementationContext context) throws Exception {

		// Obtain the server details
		this.location = context.getHttpServerLocation();
		this.input = context.getExternalServiceInput(ProcessAwareServerHttpConnectionManagedObject.class,
				ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());
		this.isIncludeEscalationStackTrace = context.isIncludeEscalationStackTrace();
		this.logger = context.getOfficeFloorSourceContext().getLogger();

		// Capture this
		captureGoogleFunctionHttpServerImplementation.set(this);
	}

}
