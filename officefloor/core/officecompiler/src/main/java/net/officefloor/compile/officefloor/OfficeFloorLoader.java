/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
	 * @param                        <OF> {@link OfficeFloorSource} type.
	 * @param officeFloorSourceClass Class of the {@link OfficeFloorSource}.
	 * @return {@link PropertyList} of the {@link OfficeFloorSourceProperty}
	 *         instances of the {@link OfficeFloorSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<OF extends OfficeFloorSource> PropertyList loadSpecification(Class<OF> officeFloorSourceClass);

	/**
	 * <p>
	 * Loads the required {@link PropertyList} for the {@link OfficeFloorSource}
	 * configuration.
	 * <p>
	 * These are additional {@link Property} instances over and above the
	 * {@link OfficeFloorSourceSpecification} that are required by the
	 * {@link OfficeFloorSource} to load the {@link OfficeFloor}. Typically these
	 * will be {@link Property} instances required by the configuration of the
	 * {@link OfficeFloor}.
	 * 
	 * @param                        <OF> {@link OfficeFloorSource} type.
	 * @param officeFloorSourceClass Class of the {@link OfficeFloorSource}.
	 * @param officeFloorLocation    Location of the {@link OfficeFloor}.
	 * @param propertyList           {@link PropertyList} containing the properties
	 *                               as per the
	 *                               {@link OfficeFloorSourceSpecification}.
	 * @return Required {@link PropertyList} or <code>null</code> if issues, which
	 *         are reported to the {@link CompilerIssues}.
	 */
	<OF extends OfficeFloorSource> PropertyList loadRequiredProperties(Class<OF> officeFloorSourceClass,
			String officeFloorLocation, PropertyList propertyList);

	/**
	 * Loads the {@link OfficeFloorType}.
	 * 
	 * @param                        <OF> {@link OfficeFloorSource} type.
	 * @param officeFloorSourceClass Class of the {@link OfficeFloorSource}.
	 * @param officeFloorLocation    Location of the {@link OfficeFloor}.
	 * @param propertyList           {@link PropertyList} containing both the
	 *                               {@link OfficeFloorSourceProperty} and required
	 *                               {@link Property} instances.
	 * @return {@link OfficeFloorType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<OF extends OfficeFloorSource> OfficeFloorType loadOfficeFloorType(Class<OF> officeFloorSourceClass,
			String officeFloorLocation, PropertyList propertyList);

	/**
	 * Loads the {@link OfficeFloorType}.
	 * 
	 * @param                     <OF> {@link OfficeFloorSource} type.
	 * @param officeFloorSource   {@link OfficeFloorSource}.
	 * @param officeFloorLocation Location of the {@link OfficeFloor}.
	 * @param propertyList        {@link PropertyList} containing both the
	 *                            {@link OfficeFloorSourceProperty} and required
	 *                            {@link Property} instances.
	 * @return {@link OfficeFloorType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<OF extends OfficeFloorSource> OfficeFloorType loadOfficeFloorType(OF officeFloorSource, String officeFloorLocation,
			PropertyList propertyList);

}
