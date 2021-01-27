package net.officefloor.server.aws.sam;

import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpServerImplementationFactory;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;

/**
 * AWS SAM {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class SamHttpServerImplementation implements HttpServerImplementation, HttpServerImplementationFactory {

	/**
	 * {@link ThreadLocal} capture of {@link SamHttpServerImplementation}.
	 */
	private static final ThreadLocal<SamHttpServerImplementation> captureSamHttpServerImplementation = new ThreadLocal<>();

	/**
	 * Obtains the {@link SamHttpServerImplementation}.
	 * 
	 * @return {@link SamHttpServerImplementation}.
	 */
	public static SamHttpServerImplementation getSamHttpServerImplementation() {
		try {
			return captureSamHttpServerImplementation.get();
		} finally {
			// Ensure remove from thread context
			captureSamHttpServerImplementation.remove();
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

	/*
	 * ==================== HttpServerImplementation ===================
	 */

	@Override
	public HttpServerImplementation createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void configureHttpServer(HttpServerImplementationContext context) throws Exception {

		// Obtain the server details
		this.location = context.getHttpServerLocation();
		this.input = context.getExternalServiceInput(ProcessAwareServerHttpConnectionManagedObject.class,
				ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());
		this.isIncludeEscalationStackTrace = context.isIncludeEscalationStackTrace();

		// Capture this
		captureSamHttpServerImplementation.set(this);
	}

}