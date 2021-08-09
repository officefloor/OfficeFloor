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
 * Indicates a property was not configured within the {@link SourceProperties}.
 * <p>
 * This is a critical error as the source is requiring this property to
 * initialise and subsequently start.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownPropertyError extends AbstractSourceError {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the unknown property.
	 */
	private final String unknownPropertyName;

	/**
	 * Initiate.
	 * 
	 * @param unknownPropertyName Name of the unknown property.
	 */
	public UnknownPropertyError(String unknownPropertyName) {
		super("Must specify property '" + unknownPropertyName + "'");
		this.unknownPropertyName = unknownPropertyName;
	}

	/**
	 * Instantiate.
	 * 
	 * @param unknownPropertyError Triggering {@link UnknownPropertyError}.
	 * @param serviceFactory       {@link ServiceFactory} requiring the property.
	 */
	public UnknownPropertyError(UnknownPropertyError unknownPropertyError, ServiceFactory<?> serviceFactory) {
		super(unknownPropertyError, serviceFactory);
		this.unknownPropertyName = unknownPropertyError.unknownPropertyName;
	}

	/**
	 * Obtains the name of the unknown property.
	 * 
	 * @return Name of the unknown property.
	 */
	public String getUnknownPropertyName() {
		return this.unknownPropertyName;
	}

}
