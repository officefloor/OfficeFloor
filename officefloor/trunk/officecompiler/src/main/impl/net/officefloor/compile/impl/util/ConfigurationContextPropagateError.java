/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.compile.impl.util;

import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Propagates a failure obtaining a {@link ConfigurationItem} from the
 * {@link ConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConfigurationContextPropagateError extends Error {

	/**
	 * Attempted location that {@link ConfigurationContext} failed on.
	 */
	private final String location;

	/**
	 * Initiate.
	 * 
	 * @param location
	 *            Attempted location that {@link ConfigurationContext} failed
	 *            on.
	 * @param cause
	 *            Failure in obtaining the {@link ConfigurationItem}.
	 */
	public ConfigurationContextPropagateError(String location, Throwable cause) {
		super(cause);
		this.location = location;
	}

	/**
	 * Obtains the attempted location that {@link ConfigurationContext} failed
	 * on.
	 * 
	 * @return Attempted location that {@link ConfigurationContext} failed on.
	 */
	public String getLocation() {
		return this.location;
	}
}