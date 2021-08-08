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

package net.officefloor.compile.section;

import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.spi.office.OfficeSectionFunction;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.frame.api.team.Team;

/**
 * <code>Type definition</code> of the {@link OfficeSectionFunction}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFunctionType {

	/**
	 * <p>
	 * Obtains the name of the {@link OfficeSectionFunction}.
	 * <p>
	 * This aids the {@link OfficeSource} in deciding the {@link Team}
	 * responsible for this {@link OfficeSectionFunction}.
	 * 
	 * @return Name of the {@link OfficeSectionFunction}.
	 */
	String getOfficeFunctionName();

	/**
	 * Obtains the {@link OfficeSubSectionType} directly containing this
	 * {@link OfficeFunctionType}.
	 * 
	 * @return {@link OfficeSubSectionType} directly containing this
	 *         {@link OfficeFunctionType}.
	 */
	OfficeSubSectionType getOfficeSubSectionType();

	/**
	 * <p>
	 * Obtains the {@link ObjectDependencyType} instances that this
	 * {@link OfficeSectionFunction} is dependent upon.
	 * <p>
	 * This aids the {@link OfficeSource} in deciding the {@link Team}
	 * responsible for this {@link OfficeSectionFunction}.
	 * 
	 * @return {@link ObjectDependencyType} instances that this
	 *         {@link OfficeSectionFunction} is dependent upon.
	 */
	ObjectDependencyType[] getObjectDependencies();

}
