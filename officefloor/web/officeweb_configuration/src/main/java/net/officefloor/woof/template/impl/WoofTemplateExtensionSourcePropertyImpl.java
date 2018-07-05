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
package net.officefloor.woof.template.impl;

import net.officefloor.woof.template.WoofTemplateExtensionSourceProperty;

/**
 * Implementation of the {@link WoofTemplateExtensionSourceProperty}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionSourcePropertyImpl implements
		WoofTemplateExtensionSourceProperty {

	/**
	 * Name of property.
	 */
	protected final String name;

	/**
	 * Label of property.
	 */
	protected final String label;

	/**
	 * Initiate with name and label of property.
	 * 
	 * @param name
	 *            Name of property.
	 * @param label
	 *            Label of property.
	 */
	public WoofTemplateExtensionSourcePropertyImpl(String name, String label) {
		this.name = name;
		this.label = label;
	}

	/*
	 * =============== WoofTemplateExtensionSourceProperty =================
	 */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}