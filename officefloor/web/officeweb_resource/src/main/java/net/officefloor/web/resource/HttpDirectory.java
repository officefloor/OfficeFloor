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
 * HTTP Directory.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpDirectory extends HttpResource {

	/**
	 * Obtains the default {@link HttpFile} for this directory.
	 * 
	 * @return Default {@link HttpFile}. May be <code>null</code> if no default
	 *         {@link HttpFile} available.
	 */
	HttpFile getDefaultFile();

	/**
	 * Obtain the {@link HttpResource} instances contained directly within this
	 * directory.
	 * 
	 * @return Child {@link HttpResource} instances.
	 */
	HttpResource[] listResources();

}