/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.spi.handler.source;

/**
 * <p>
 * Indicates a property was not configured within the
 * {@link HandlerSourceContext}.
 * <p>
 * This is a serious error as the {@link HandlerSource} is requiring this
 * property to initialise.
 * 
 * @author Daniel
 */
public class HandlerUnknownPropertyError extends Error {

	/**
	 * Name of the unknown property.
	 */
	private final String unknownPropertyName;

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Message.
	 * @param unknownPropertyName
	 *            Name of the unknown property.
	 */
	public HandlerUnknownPropertyError(String message,
			String unknownPropertyName) {
		super(message);
		this.unknownPropertyName = unknownPropertyName;
	}

	/**
	 * Obtains the name of the unknown property.
	 * 
	 * @return Name of the unknown property.
	 */
	public String getUnknonwnPropertyName() {
		return this.unknownPropertyName;
	}

}