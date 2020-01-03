package net.officefloor.web.resource.source;

import java.io.IOException;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceStore;

/**
 * {@link ManagedFunction} to send the {@link HttpFile} from the
 * {@link HttpResourceStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class SendStoreHttpFileFunction extends AbstractSendHttpFileFunction<HttpResourceStore> {

	/**
	 * Instantiate.
	 * 
	 * @param contextPath
	 *            Context path.
	 */
	public SendStoreHttpFileFunction(String contextPath) {
		super(contextPath);
	}

	/*
	 * ================ AbstractSendHttpFileFunction =======================
	 */

	@Override
	protected HttpResource getHttpResource(HttpResourceStore resources, String resourcePath) throws IOException {
		return resources.getHttpResource(resourcePath);
	}

}