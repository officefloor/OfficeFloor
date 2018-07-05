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
 * Indicates a resource was not available from the {@link SourceContext}.
 * <p>
 * This is a critical error as the source is requiring the resource to
 * initialise and subsequently start.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownResourceError extends AbstractSourceError {

	/**
	 * Location of the unknown resource.
	 */
	private final String unknownResourceLocation;

	/**
	 * Initiate.
	 * 
	 * @param unknownResourceLocation
	 *            Location of the unknown resource.
	 */
	public UnknownResourceError(String unknownResourceLocation) {
		super("Can not obtain resource at location '" + unknownResourceLocation + "'");
		this.unknownResourceLocation = unknownResourceLocation;
	}

	/**
	 * Instantiate.
	 * 
	 * @param error
	 *            Triggering {@link UnknownResourceError} from
	 *            {@link ServiceFactory}.
	 * @param serviceFactory
	 *            {@link ServiceFactory}.
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