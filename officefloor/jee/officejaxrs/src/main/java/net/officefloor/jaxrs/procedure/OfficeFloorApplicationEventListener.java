package net.officefloor.jaxrs.procedure;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link ApplicationEventListener}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorApplicationEventListener implements ApplicationEventListener {

	/**
	 * Property name for the path parameters.
	 */
	static final String PATH_PARAMETERS_PROPERTY_NAME = "OFFICE_FLOOR_PATH_PARAMETERS";

	/**
	 * Override HTTP method to match to {@link Procedure}.
	 */
	private final String httpMethod;

	/**
	 * Override path to match to {@link Procedure}.
	 */
	private final String path;

	/**
	 * Instantiate.
	 * 
	 * @param httpMethod Override HTTP method.
	 * @param path       Override path.
	 */
	public OfficeFloorApplicationEventListener(String httpMethod, String path) {
		this.httpMethod = httpMethod;
		this.path = path;
	}

	/*
	 * ================== ApplicationEventListener ======================
	 */

	@Override
	public void onEvent(ApplicationEvent event) {
		// Not interested in application events
	}

	@Override
	public RequestEventListener onRequest(RequestEvent requestEvent) {

		// Allow override the request to match procedure's method
		return new OfficeFloorRequestEventListener();
	}

	/**
	 * {@link OfficeFloor} {@link RequestEventListener}.
	 */
	private class OfficeFloorRequestEventListener implements RequestEventListener {

		/*
		 * ============= RequestEventListener =====================
		 */

		@Override
		public void onEvent(RequestEvent event) {

			// Obtain the request to modify
			ContainerRequest request = event.getContainerRequest();

			// Handle event
			switch (event.getType()) {
			case MATCHING_START:

				// Match method configured for JAX-RS method
				request.setMethod(OfficeFloorApplicationEventListener.this.httpMethod);

				// Obtain the possible path parameters
				@SuppressWarnings("unchecked")
				Map<String, String> pathParameters = (Map<String, String>) request
						.getProperty(PATH_PARAMETERS_PROPERTY_NAME);

				// Translate path with path parameters
				String translatedPath = OfficeFloorApplicationEventListener.this.path;
				if (pathParameters != null) {
					for (String pathParamName : pathParameters.keySet()) {
						String pathParamValue = pathParameters.get(pathParamName);
						translatedPath = translatedPath.replace("{" + pathParamName + "}", pathParamValue);
					}
				}

				// Determine new request URI that matches configured JAX-RS method
				URI baseUri;
				URI requestUri;
				try {
					baseUri = request.getBaseUri().resolve(new URI(null, null, "/", null, null));
					requestUri = baseUri
							.resolve(new URI(null, null, translatedPath, request.getRequestUri().getQuery(), null));
				} catch (URISyntaxException ex) {
					throw new IllegalStateException(ex);
				}

				// Override to match to JAX-RS method
				request.setRequestUri(baseUri, requestUri);

				break;

			default:
				// Do nothing
				break;
			}
		}
	}

}