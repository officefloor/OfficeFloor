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
 * Indicates a {@link Class} was not available from the {@link SourceContext}.
 * <p>
 * This is a critical error as the source is requiring the {@link Class} to
 * initialise and subsequently start.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownClassError extends Error {

	/**
	 * Name of the unknown {@link Class}.
	 */
	private final String unknownClassName;

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Message.
	 * @param unknownClassName
	 *            Name of the unknown {@link Class}.
	 */
	public UnknownClassError(String message, String unknownClassName) {
		super(message);
		this.unknownClassName = unknownClassName;
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