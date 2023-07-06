package net.officefloor.server.google.function;

import java.util.logging.Logger;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;

/**
 * {@link OfficeFloor} {@link HttpFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorHttpFunction implements HttpFunction {

	/*
	 * ==================== HttpFunction ========================
	 */

	@Override
	public void service(HttpRequest request, HttpResponse response) throws Exception {
		response.getWriter().write("{\"text\":\"MOCK RESPONSE\"}");
	}

	/**
	 * Running {@link OfficeFloor}.
	 */
	private static class GoogleFunctionInstance {

		/**
		 * {@link OfficeFloor}.
		 */
		private final OfficeFloor officeFloor;

		/**
		 * {@link HttpServerLocation}.
		 */
		private final HttpServerLocation location;

		/**
		 * {@link ExternalServiceInput}.
		 */
		@SuppressWarnings("rawtypes")
		private final ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> input;

		/**
		 * Indicates if include stack trace in HTTP response.
		 */
		private final boolean isIncludeEscalationStackTrace;

		/**
		 * {@link Logger}.
		 */
		private final Logger logger;

		/**
		 * Instantiate.
		 * 
		 * @param officeFloor                            {@link OfficeFloor}.
		 * @param googleFunctionHttpServerImplementation {@link GoogleFunctionHttpServerImplementation}.
		 */
		private GoogleFunctionInstance(OfficeFloor officeFloor,
				GoogleFunctionHttpServerImplementation googleFunctionHttpServerImplementation) {
			this.officeFloor = officeFloor;

			// Load remaining from server implementation
			this.location = googleFunctionHttpServerImplementation.getHttpServerLocation();
			this.input = googleFunctionHttpServerImplementation.getInput();
			this.isIncludeEscalationStackTrace = googleFunctionHttpServerImplementation.isIncludeEscalationStackTrace();
			this.logger = googleFunctionHttpServerImplementation.getLogger();
		}
	}

}