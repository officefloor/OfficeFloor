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
 * <p>
 * Indicates a service was not available from the {@link SourceContext}.
 * <p>
 * This is a critical error as the source is requiring the service to initialise
 * and subsequently start.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownServiceError extends AbstractSourceError {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link ServiceFactory} type that is not configured.
	 */
	private final Class<?> unknownServiceFactoryType;

	/**
	 * Initiate.
	 * 
	 * @param unknownServiceFactoryType {@link ServiceFactory} type that is not
	 *                                  configured.
	 */
	public UnknownServiceError(Class<?> unknownServiceFactoryType) {
		super("No services configured for " + unknownServiceFactoryType.getName());
		this.unknownServiceFactoryType = unknownServiceFactoryType;
	}

	/**
	 * Obtains the {@link ServiceFactory} type that is not configured.
	 * 
	 * @return {@link ServiceFactory} type that is not configured.
	 */
	public Class<?> getUnknownServiceFactoryType() {
		return this.unknownServiceFactoryType;
	}

}
