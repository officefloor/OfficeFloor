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
package net.officefloor.compile.office;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceProperty;
import net.officefloor.compile.spi.office.source.OfficeSourceSpecification;
import net.officefloor.frame.api.manage.Office;

/**
 * Loads the {@link OfficeType} from the {@link OfficeSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link OfficeSourceSpecification} for the {@link OfficeSource}.
	 * 
	 * @param <O>
	 *            {@link OfficeSource} type.
	 * @param officeSourceClass
	 *            Class of the {@link OfficeSource}.
	 * @return {@link PropertyList} of the {@link OfficeSourceProperty}
	 *         instances of the {@link OfficeSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<O extends OfficeSource> PropertyList loadSpecification(
			Class<O> officeSourceClass);

	/**
	 * Loads and returns the {@link OfficeType} from the {@link OfficeSource}.
	 * 
	 * @param <O>
	 *            {@link OfficeSource} type.
	 * @param officeSourceClass
	 *            Class of the {@link OfficeSource}.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link OfficeType}.
	 * @return {@link OfficeType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<O extends OfficeSource> OfficeType loadOfficeType(
			Class<O> officeSourceClass, String officeLocation,
			PropertyList propertyList);

	/**
	 * Loads and returns the {@link OfficeType} from the {@link OfficeSource}.
	 * 
	 * @param officeSource
	 *            {@link OfficeSource} instance.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link OfficeType}.
	 * @return {@link OfficeType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	OfficeType loadOfficeType(OfficeSource officeSource, String officeLocation,
			PropertyList propertyList);

}