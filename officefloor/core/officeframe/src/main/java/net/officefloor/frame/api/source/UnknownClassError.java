/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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