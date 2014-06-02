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
package net.officefloor.eclipse.common.dialog.input;

import org.eclipse.swt.widgets.Composite;

/**
 * Context for the {@link Input}.
 * 
 * @author Daniel Sagenschneider
 */
public interface InputContext {

	/**
	 * Obtains the {@link Composite}.
	 * 
	 * @return {@link Composite}.
	 */
	Composite getParent();

	/**
	 * Obtains the initial value.
	 * 
	 * @return Initial value.
	 */
	Object getInitialValue();

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
	 * Invoked by the {@link Input} on the value changing.
	 * <p>
	 * This allows for validation of the value to determine if value is valid.
	 * 
	 * @param value
	 *            New value.
	 */
	void notifyValueChanged(Object value);

	/**
	 * Invoked by the {@link Input} to indicate invalid.
	 * 
	 * @param message
	 *            Reason invalid.
	 */
	void notifyValueInvalid(String message);

}
