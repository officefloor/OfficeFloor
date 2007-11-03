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
package net.officefloor.eclipse.common.dialog;

import org.eclipse.swt.widgets.Composite;

/**
 * Context for the {@link PropertyInput}.
 * 
 * @author Daniel
 */
public interface PropertyInputContext {

	/**
	 * Obtains the initial value of the property.
	 * 
	 * @return Initial value of the property.
	 */
	Object getInitialValue();

	/**
	 * Obtains the {@link Composite}.
	 * 
	 * @return {@link Composite}.
	 */
	Composite getParent();

	/**
	 * Obtains the attribute for the name.
	 * 
	 * @param name
	 *            Name of the attribute.
	 * @return Attribute by name, or <code>null</code> if not specified.
	 */
	Object getAttribute(String name);

	/**
	 * Adds an attribute to the context of the property.
	 * 
	 * @param name
	 *            Name of the attribute.
	 * @param value
	 *            Value of the attribute.
	 */
	void setAttribute(String name, Object value);

	/**
	 * <p>
	 * Invoked by the {@link PropertyInput} of the value changing.
	 * <p>
	 * This allows for validation of the property value to determine if value is
	 * valid.
	 * 
	 * @param value
	 *            New value of the property.
	 */
	void notifyValueChanged(String value);

}
