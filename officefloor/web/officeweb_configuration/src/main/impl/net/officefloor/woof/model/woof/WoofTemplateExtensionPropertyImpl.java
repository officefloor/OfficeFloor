/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.woof.model.woof;

/**
 * {@link WoofTemplateExtensionProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionPropertyImpl implements
		WoofTemplateExtensionProperty {

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Value.
	 */
	private final String value;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public WoofTemplateExtensionPropertyImpl(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/*
	 * ================== WoofTemplateExtensionProperty =====================
	 */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getValue() {
		return this.value;
	}

}
