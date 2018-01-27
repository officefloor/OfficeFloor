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
package net.officefloor.web.resource;

/**
 * HTTP resource.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResource {

	/**
	 * <p>
	 * Obtains the path to this {@link HttpResource}.
	 * <p>
	 * The path is canonical to allow using it as a key for caching this
	 * {@link HttpResource}.
	 * 
	 * @return Canonical path to this {@link HttpResource}.
	 */
	String getPath();

	/**
	 * <p>
	 * Indicates if this {@link HttpResource} exists. Should this
	 * {@link HttpResource} not exist, only the path will be available.
	 * <p>
	 * This allows for caching of {@link HttpResource} instances not existing.
	 * 
	 * @return <code>true</code> if this {@link HttpResource} exists.
	 */
	boolean isExist();

}