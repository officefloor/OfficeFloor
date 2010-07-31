/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.container;

import java.util.Iterator;

import javax.servlet.ServletContext;

/**
 * Attributes for the {@link ServletContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ContextAttributes {

	/**
	 * Obtains the names of the bound attributes.
	 * 
	 * @return Names of the bound attributes.
	 */
	Iterator<String> getAttributeNames();

	/**
	 * Binds the attribute to this context.
	 * 
	 * @param name
	 *            Name to bind attribute.
	 * @param attribute
	 *            Attribute to bind.
	 */
	void setAttribute(String name, Object attribute);

	/**
	 * Obtains the attribute bound by the name within this context.
	 * 
	 * @param name
	 *            Name of attribute.
	 * @return Attribute bound to the name or <code>null</code> if no attribute
	 *         bound for name.
	 */
	Object getAttribute(String name);

	/**
	 * Unbinds the attribute from this context.
	 * 
	 * @param name
	 *            Name of attribute to unbind.
	 */
	void removeAttribute(String name);

}