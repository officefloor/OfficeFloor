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
package net.officefloor.plugin.web.http.session.attribute;

import java.io.Serializable;

import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Access to a particular {@link Object} within the {@link HttpSession}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSessionAttribute<T extends Serializable> {

	/**
	 * Sets the {@link Object} into the {@link HttpSession}.
	 * 
	 * @param sessionObject
	 *            {@link HttpSession} {@link Object}.
	 */
	void setSessionObject(T sessionObject);

	/**
	 * Obtains the {@link Object} from the {@link HttpSession}.
	 * 
	 * @return {@link Object} from the {@link HttpSession}. May be
	 *         <code>null</code> if not within the {@link HttpSession}.
	 */
	T getSessionObject();

}