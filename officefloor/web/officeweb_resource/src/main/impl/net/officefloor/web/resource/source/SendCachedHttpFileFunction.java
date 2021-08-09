/*-
 * #%L
 * Web resources
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
