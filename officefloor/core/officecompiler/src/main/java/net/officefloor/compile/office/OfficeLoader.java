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
	 * @param <O>               {@link OfficeSource} type.
	 * @param officeSourceClass Class of the {@link OfficeSource}.
	 * @return {@link PropertyList} of the {@link OfficeSourceProperty} instances of
	 *         the {@link OfficeSourceSpecification} or <code>null</code> if issue,
	 *         which is reported to the {@link CompilerIssues}.
	 */
	<O extends OfficeSource> PropertyList loadSpecification(Class<O> officeSourceClass);

	/**
	 * Loads and returns the {@link OfficeType} from the {@link OfficeSource}.
	 * 
	 * @param <O>               {@link OfficeSource} type.
	 * @param officeSourceClass Class of the {@link OfficeSource}.
	 * @param officeLocation    Location of the {@link Office}.
	 * @param propertyList      {@link PropertyList} containing the properties to
	 *                          source the {@link OfficeType}.
	 * @return {@link OfficeType} or <code>null</code> if issues, which are reported
	 *         to the {@link CompilerIssues}.
	 */
	<O extends OfficeSource> OfficeType loadOfficeType(Class<O> officeSourceClass, String officeLocation,
			PropertyList propertyList);

	/**
	 * Loads and returns the {@link OfficeType} from the {@link OfficeSource}.
	 * 
	 * @param officeSource   {@link OfficeSource} instance.
	 * @param officeLocation Location of the {@link Office}.
	 * @param propertyList   {@link PropertyList} containing the properties to
	 *                       source the {@link OfficeType}.
	 * @return {@link OfficeType} or <code>null</code> if issues, which are reported
	 *         to the {@link CompilerIssues}.
	 */
	OfficeType loadOfficeType(OfficeSource officeSource, String officeLocation, PropertyList propertyList);

}
