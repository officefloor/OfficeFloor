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
package net.officefloor.autowire;

import net.officefloor.compile.spi.section.source.SectionSource;

/**
 * Section for configuring auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireSection extends AutoWireProperties {

	/**
	 * Obtains the section name.
	 * 
	 * @return Section name.
	 */
	String getSectionName();

	/**
	 * <p>
	 * Obtains the class name of the {@link SectionSource}.
	 * <p>
	 * This may be an alias.
	 * 
	 * @return Class name of the {@link SectionSource}.
	 */
	String getSectionSourceClassName();

	/**
	 * Obtains the section location.
	 * 
	 * @return Section location.
	 */
	String getSectionLocation();

	/**
	 * Specifies the {@link AutoWireSection} to inherit the link configuration.
	 * 
	 * @param section
	 *            {@link AutoWireSection} to inherit link configuration.
	 */
	void setSuperSection(AutoWireSection section);

	/**
	 * Obtains the super {@link AutoWireSection}.
	 * 
	 * @return Super {@link AutoWireSection}.
	 */
	AutoWireSection getSuperSection();

}