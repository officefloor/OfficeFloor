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
 * Indicates a resource was not available from the {@link SourceContext}.
 * <p>
 * This is a critical error as the source is requiring the resource to
 * initialise and subsequently start.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownResourceError extends AbstractSourceError {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Location of the unknown resource.
	 */
	private final String unknownResourceLocation;

	/**
	 * Initiate.
	 * 
	 * @param unknownResourceLocation Location of the unknown resource.
	 */
	public UnknownResourceError(String unknownResourceLocation) {
		super("Can not obtain resource at location '" + unknownResourceLocation + "'");
		this.unknownResourceLocation = unknownResourceLocation;
	}

	/**
	 * Instantiate.
	 * 
	 * @param error          Triggering {@link UnknownResourceError} from
	 *                       {@link ServiceFactory}.
	 * @param serviceFactory {@link ServiceFactory}.
	 */
	public UnknownResourceError(UnknownResourceError error, ServiceFactory<?> serviceFactory) {
		super(error, serviceFactory);
		this.unknownResourceLocation = error.unknownResourceLocation;
	}

	/**
	 * Obtains the location of the unknown resource.
	 * 
	 * @return Location of the unknown resource.
	 */
	public String getUnknownResourceLocation() {
		return this.unknownResourceLocation;
	}

}
