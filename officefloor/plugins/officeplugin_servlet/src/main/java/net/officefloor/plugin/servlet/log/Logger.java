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
package net.officefloor.plugin.servlet.log;

import net.officefloor.plugin.servlet.container.HttpServletContainer;

/**
 * Logger for the {@link HttpServletContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Logger {

	/**
	 * Logs the message.
	 * 
	 * @param message
	 *            Message.
	 */
	void log(String message);

	/**
	 * Logs the message.
	 * 
	 * @param message
	 *            Message.
	 * @param failure
	 *            Failure to also log with message.
	 */
	void log(String message, Throwable failure);

}