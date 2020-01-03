package net.officefloor.web.resource.source;

import java.io.IOException;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceCache;

/**
 * {@link ManagedFunction} to send the {@link HttpFile} from
 * {@link HttpResourceCache}.
 * 
 * @author Daniel Sagenschneider
 */
public class SendCachedHttpFileFunction extends AbstractSendHttpFileFunction<HttpResourceCache> {

	/**
	 * Instantiate.
	 * 
	 * @param contextPath
	 *            Context path.
	 */
	public SendCachedHttpFileFunction(String contextPath) {
		super(contextPath);
	}

	/*
	 * ================ AbstractSendHttpFileFunction =======================
	 */

	@Override
	protected HttpResource getHttpResource(HttpResourceCache resources, String resourcePath) throws IOException {
		return resources.getHttpResource(resourcePath);
	}

}