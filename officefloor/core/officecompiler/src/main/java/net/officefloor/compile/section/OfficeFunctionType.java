/*-
 * #%L
 * OfficeCompiler
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
