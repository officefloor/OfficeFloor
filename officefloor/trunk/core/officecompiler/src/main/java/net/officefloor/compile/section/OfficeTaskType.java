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
package net.officefloor.compile.section;

import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.frame.spi.team.Team;

/**
 * <code>Type definition</code> of the {@link OfficeTask}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeTaskType {

	/**
	 * <p>
	 * Obtains the name of the {@link OfficeTask}.
	 * <p>
	 * This aids the {@link OfficeSource} in deciding the {@link Team}
	 * responsible for this {@link OfficeTask}.
	 * 
	 * @return Name of the {@link OfficeTask}.
	 */
	String getOfficeTaskName();

	/**
	 * Obtains the {@link OfficeSubSectionType} directly containing this
	 * {@link OfficeTaskType}.
	 * 
	 * @return {@link OfficeSubSectionType} directly containing this
	 *         {@link OfficeTaskType}.
	 */
	OfficeSubSectionType getOfficeSubSectionType();

	/**
	 * <p>
	 * Obtains the {@link ObjectDependencyType} instances that this
	 * {@link OfficeTask} is dependent upon.
	 * <p>
	 * This aids the {@link OfficeSource} in deciding the {@link Team}
	 * responsible for this {@link OfficeTask}.
	 * 
	 * @return {@link ObjectDependencyType} instances that this
	 *         {@link OfficeTask} is dependent upon.
	 */
	ObjectDependencyType[] getObjectDependencies();

}