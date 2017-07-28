/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
public class UnknownResourceError extends Error {

	/**
	 * Location of the unknown resource.
	 */
	private final String unknownResourceLocation;

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Message.
	 * @param unknownResourceLocation
	 *            Location of the unknown resource.
	 */
	public UnknownResourceError(String message, String unknownResourceLocation) {
		super(message);
		this.unknownResourceLocation = unknownResourceLocation;
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