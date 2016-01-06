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
package net.officefloor.compile.officefloor;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceProperty;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceSpecification;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Loads the {@link OfficeFloor} from the {@link OfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link OfficeFloorSourceSpecification} for the {@link OfficeFloorSource}.
	 * 
	 * @param <OF>
	 *            {@link OfficeFloorSource} type.
	 * @param officeFloorSourceClass
	 *            Class of the {@link OfficeFloorSource}.
	 * @return {@link PropertyList} of the {@link OfficeFloorSourceProperty}
	 *         instances of the {@link OfficeFloorSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<OF extends OfficeFloorSource> PropertyList loadSpecification(
			Class<OF> officeFloorSourceClass);

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
	 * @param <OF>
	 *            {@link OfficeFloorSource} type.
	 * @param officeFloorSourceClass
	 *            Class of the {@link OfficeFloorSource}.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties as per the
	 *            {@link OfficeFloorSourceSpecification}.
	 * @return Required {@link PropertyList} or <code>null</code> if issues,
	 *         which are reported to the {@link CompilerIssues}.
	 */
	<OF extends OfficeFloorSource> PropertyList loadRequiredProperties(
			Class<OF> officeFloorSourceClass, String officeFloorLocation,
			PropertyList propertyList);

	/**
	 * Loads the {@link OfficeFloorType}.
	 * 
	 * @param <OF>
	 *            {@link OfficeFloorSource} type.
	 * @param officeFloorSourceClass
	 *            Class of the {@link OfficeFloorSource}.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param propertyList
	 *            {@link PropertyList} containing both the
	 *            {@link OfficeFloorSourceProperty} and required
	 *            {@link Property} instances.
	 * @return {@link OfficeFloorType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<OF extends OfficeFloorSource> OfficeFloorType loadOfficeFloorType(
			Class<OF> officeFloorSourceClass, String officeFloorLocation,
			PropertyList propertyList);

	/**
	 * Loads the {@link OfficeFloor}.
	 * 
	 * @param <OF>
	 *            {@link OfficeFloorSource} type.
	 * @param officeFloorSourceClass
	 *            Class of the {@link OfficeFloorSource}.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param propertyList
	 *            {@link PropertyList} containing both the
	 *            {@link OfficeFloorSourceProperty} and the required
	 *            {@link Property} instances.
	 * @return {@link OfficeFloor} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<OF extends OfficeFloorSource> OfficeFloor loadOfficeFloor(
			Class<OF> officeFloorSourceClass, String officeFloorLocation,
			PropertyList propertyList);

	/**
	 * Loads the {@link OfficeFloor}.
	 * 
	 * @param officeFloorSource
	 *            {@link OfficeFloorSource}.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}. {@link PropertyList}
	 *            containing both the {@link OfficeFloorSourceProperty} and the
	 *            required {@link Property} instances.
	 * @param propertyList
	 *            {@link PropertyList} to configure the
	 *            {@link OfficeFloorSource}.
	 * @return {@link OfficeFloor} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	OfficeFloor loadOfficeFloor(OfficeFloorSource officeFloorSource,
			String officeFloorLocation, PropertyList propertyList);

}