/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.autowire;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.source.SectionSource;

/**
 * Section for configuring auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireSection {

	/**
	 * Name of section.
	 */
	private final String name;

	/**
	 * {@link SectionSource} class.
	 */
	private final Class<?> sourceClass;

	/**
	 * Location of section.
	 */
	private final String location;

	/**
	 * Properties for the section.
	 */
	private final PropertyList properties;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name of section.
	 * @param sourceClass
	 *            {@link SectionSource} class.
	 * @param location
	 *            Location of section.
	 * @param properties
	 *            Properties for the section.
	 */
	public AutoWireSection(String name, Class<?> sourceClass, String location,
			PropertyList properties) {
		this.name = name;
		this.sourceClass = sourceClass;
		this.location = location;
		this.properties = properties;
	}

	/**
	 * Allow for extending this to provide additional functionality for an
	 * section.
	 * 
	 * @param section
	 *            Section to copy in state.
	 */
	protected AutoWireSection(AutoWireSection section) {
		this(section.getSectionName(), section.getSectionSourceClass(), section
				.getSectionLocation(), section.getSectionProperties());
	}

	/**
	 * Obtains the section name.
	 * 
	 * @return Section name.
	 */
	public String getSectionName() {
		return this.name;
	}

	/**
	 * Obtains the {@link SectionSource} class.
	 * 
	 * @return {@link SectionSource} class.
	 */
	public Class<?> getSectionSourceClass() {
		return this.sourceClass;
	}

	/**
	 * Obtains the section location.
	 * 
	 * @return Section location.
	 */
	public String getSectionLocation() {
		return this.location;
	}

	/**
	 * Obtains the {@link PropertyList}
	 * 
	 * @return {@link PropertyList}.
	 */
	public PropertyList getSectionProperties() {
		return this.properties;
	}

	/**
	 * Convenience method to add a {@link Property}.
	 * 
	 * @param name
	 *            Name of the {@link Property}.
	 * @param value
	 *            Value of the {@link Property}.
	 */
	public void addSectionProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

}