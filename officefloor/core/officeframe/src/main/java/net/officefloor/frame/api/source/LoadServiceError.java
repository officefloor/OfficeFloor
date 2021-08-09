/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.api.source;

/**
 * Indicates a service was not able to be loaded.
 * <p>
 * This is a critical error as services should always be able to be loaded.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadServiceError extends AbstractSourceError {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link ServiceFactory} {@link Class} name that failed to load.
	 */
	private final String serviceFactoryClassName;

	/**
	 * Initiate.
	 * 
	 * @param serviceFactoryClassName {@link ServiceFactory} {@link Class} name that
	 *                                failed to load.
	 * @param failure                 Cause.
	 */
	public LoadServiceError(String serviceFactoryClassName, Throwable failure) {
		super("Failed to create service from " + serviceFactoryClassName, failure);
		this.serviceFactoryClassName = serviceFactoryClassName;
	}

	/**
	 * Obtains the {@link ServiceFactory} {@link Class} name.
	 * 
	 * @return {@link ServiceFactory} {@link Class} name.
	 */
	public String getServiceFactoryClassName() {
		return this.serviceFactoryClassName;
	}

}
