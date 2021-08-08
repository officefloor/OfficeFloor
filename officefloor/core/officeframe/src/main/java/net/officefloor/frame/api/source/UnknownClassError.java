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
 * Indicates a {@link Class} was not available from the {@link SourceContext}.
 * <p>
 * This is a critical error as the source is requiring the {@link Class} to
 * initialise and subsequently start.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownClassError extends AbstractSourceError {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the unknown {@link Class}.
	 */
	private final String unknownClassName;

	/**
	 * Initiate.
	 * 
	 * @param unknownClassName Name of the unknown {@link Class}.
	 */
	public UnknownClassError(String unknownClassName) {
		super("Can not load class '" + unknownClassName + "'");
		this.unknownClassName = unknownClassName;
	}

	/**
	 * Instantiate.
	 * 
	 * @param unknownClassError Triggering {@link UnknownClassError}.
	 * @param serviceFactory    {@link ServiceFactory} requiring the property.
	 */
	public UnknownClassError(UnknownClassError unknownClassError, ServiceFactory<?> serviceFactory) {
		super(unknownClassError, serviceFactory);
		this.unknownClassName = unknownClassError.unknownClassName;
	}

	/**
	 * Obtains the name of the unknown {@link Class}.
	 * 
	 * @return Name of the unknown {@link Class}.
	 */
	public String getUnknownClassName() {
		return this.unknownClassName;
	}

}
