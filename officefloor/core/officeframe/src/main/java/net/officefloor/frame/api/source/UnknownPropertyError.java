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