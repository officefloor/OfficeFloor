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
package net.officefloor.compile.officefloor;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceProperty;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceSpecification;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * Loads the {@link OfficeFloor} from the {@link OfficeFloorSource}.
 * 
 * @author Daniel
 */
public interface OfficeFloorLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link OfficeFloorSourceSpecification} for the {@link OfficeFloorSource}.
	 * 
	 * @param officeFloorSourceClass
	 *            Class of the {@link OfficeFloorSource}.
	 * @param issues
	 *            {@link CompilerIssues} to report issues in loading the
	 *            {@link OfficeFloorSourceSpecification} and obtaining the
	 *            {@link PropertyList}.
	 * @return {@link PropertyList} of the {@link OfficeFloorSourceProperty}
	 *         instances of the {@link OfficeFloorSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<OF extends OfficeFloorSource> PropertyList loadSpecification(
			Class<OF> officeFloorSourceClass, CompilerIssues issues);

	/**
	 * <p>
	 * Loads the required {@link PropertyList} for the {@link OfficeFloorSource}
	 * configuration.
	 * <p>
	 * These are additional {@link Property} instances over and above the
	 * {@link OfficeFloorSourceSpecification} that are required by the
	 * {@link OfficeFloorSource} to load the {@link OfficeFloor}. Typically
	 * these will be {@link Property} instances required by the configuration of
	 * the {@link OfficeFloor}.
	 * 
	 * @param officeFloorSourceClass
	 *            Class of the {@link OfficeFloorSource}.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties as per the
	 *            {@link OfficeFloorSourceSpecification}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param classLoader
	 *            {@link ClassLoader} that the {@link OfficeFloorSource} may use
	 *            in obtaining necessary class path resources.
	 * @param issues
	 *            {@link CompilerIssues} to report issues in initialising.
	 * @return Required {@link PropertyList} or <code>null</code> if issues,
	 *         which are reported to the {@link CompilerIssues}.
	 */
	<OF extends OfficeFloorSource> PropertyList loadRequiredProperties(
			Class<OF> officeFloorSourceClass, String officeFloorLocation,
			PropertyList propertyList,
			ConfigurationContext configurationContext, ClassLoader classLoader,
			CompilerIssues issues);

	/**
	 * Loads the {@link OfficeFloor}.
	 * 
	 * @param officeFloorSourceClass
	 *            Class of the {@link OfficeFloorSource}.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param propertyList
	 *            {@link PropertyList} containing both the
	 *            {@link OfficeFloorSourceProperty} and the required
	 *            {@link Property} instances.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param classLoader
	 *            {@link ClassLoader} that the {@link OfficeFloorSource} may use
	 *            in obtaining necessary class path resources.
	 * @param issues
	 *            {@link CompilerIssues} to report issues in loading the
	 *            {@link OfficeFloor}.
	 * @param officeFrame
	 *            {@link OfficeFrame} to use to build the {@link OfficeFloor}.
	 * @return {@link OfficeFloor} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<OF extends OfficeFloorSource> OfficeFloor loadOfficeFloor(
			Class<OF> officeFloorSourceClass, String officeFloorLocation,
			PropertyList propertyList,
			ConfigurationContext configurationContext, ClassLoader classLoader,
			CompilerIssues issues, OfficeFrame officeFrame);

}