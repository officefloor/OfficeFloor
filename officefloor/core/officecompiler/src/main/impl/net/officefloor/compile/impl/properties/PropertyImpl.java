/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.properties;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;

/**
 * {@link Property} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyImpl implements Property {

	/**
	 * Display label for the property.
	 */
	private final String label;

	/**
	 * Name of the property.
	 */
	private final String name;

	/**
	 * Value of the property.
	 */
	private String value = null;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param label
	 *            Display label for the property. Should this be blank, it is
	 *            defaulted to <code>name</code>.
	 */
	public PropertyImpl(String name, String label) {
		this.name = name;
		this.label = (CompileUtil.isBlank(label) ? name : label);
	}

	/**
	 * Instantiates the label as the name.
	 * 
	 * @param name
	 *            Name of the property which is also used as the display label.
	 */
	public PropertyImpl(String name) {
		this(name, name);
	}

	/*
	 * ==================== Property =======================================
	 */

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

}