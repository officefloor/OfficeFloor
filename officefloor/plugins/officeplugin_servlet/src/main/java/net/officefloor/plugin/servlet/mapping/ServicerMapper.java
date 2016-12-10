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
package net.officefloor.plugin.servlet.mapping;

import net.officefloor.plugin.servlet.container.HttpServletServicer;
import net.officefloor.plugin.socket.server.http.HttpRequest;

/**
 * Maps a {@link HttpRequest} to a {@link HttpServletServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServicerMapper {

	/**
	 * Matches the best {@link ServicerMapping} for the path.
	 * 
	 * @param path
	 *            Path for mapping.
	 * @return Best {@link ServicerMapping} or <code>null</code> if no
	 *         appropriate {@link HttpServletServicer}.
	 */
	ServicerMapping mapPath(String path);

	/**
	 * Obtains the {@link HttpServletServicer} by its name.
	 * 
	 * @param name
	 *            Name of the {@link HttpServletServicer}.
	 * @return {@link HttpServletServicer} or <code>null</code> if no
	 *         {@link HttpServletServicer} by name.
	 */
	HttpServletServicer mapName(String name);

}