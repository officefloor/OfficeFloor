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
package net.officefloor.compile.office;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceProperty;
import net.officefloor.compile.spi.office.source.OfficeSourceSpecification;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * Loads the {@link OfficeType} from the {@link OfficeSource}.
 * 
 * @author Daniel
 */
public interface OfficeLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link OfficeSourceSpecification} for the {@link OfficeSource}.
	 * 
	 * @param officeSourceClass
	 *            Class of the {@link OfficeSource}.
	 * @param issues
	 *            {@link CompilerIssues} to report issues in loading the
	 *            {@link OfficeSourceSpecification} and obtaining the
	 *            {@link PropertyList}.
	 * @return {@link PropertyList} of the {@link OfficeSourceProperty}
	 *         instances of the {@link OfficeSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<O extends OfficeSource> PropertyList loadSpecification(
			Class<O> officeSourceClass, CompilerIssues issues);

	/**
	 * Loads and returns the {@link OfficeType} from the {@link OfficeSource}.
	 * 
	 * @param officeSourceClass
	 *            Class of the {@link OfficeSource}.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link OfficeType}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param classLoader
	 *            {@link ClassLoader} that the {@link OfficeSource} may use in
	 *            obtaining necessary class path resources.
	 * @param issues
	 *            {@link CompilerIssues} to report issues in loading the
	 *            {@link OfficeType}.
	 * @return {@link OfficeType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<O extends OfficeSource> OfficeType loadOfficeType(
			Class<O> officeSourceClass, String officeLocation,
			PropertyList propertyList,
			ConfigurationContext configurationContext, ClassLoader classLoader,
			CompilerIssues issues);

}