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
package net.officefloor.plugin.servlet.context;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.servlet.mapping.ServicerMapper;

/**
 * <p>
 * Reference to a {@link Servlet} {@link ManagedFunction}.
 * <p>
 * This allows for the {@link OfficeServletContext} to provide routing with
 * similar rules to {@link RequestDispatcher} (i.e. {@link ServicerMapper}).
 * 
 * @author Daniel Sagenschneider
 */
public interface ServletTaskReference {

	/**
	 * Obtains the {@link Work} name.
	 * 
	 * @return {@link Work} name.
	 */
	String getWorkName();

	/**
	 * Obtains the {@link ManagedFunction} name.
	 * 
	 * @return {@link ManagedFunction} name.
	 */
	String getTaskName();

}