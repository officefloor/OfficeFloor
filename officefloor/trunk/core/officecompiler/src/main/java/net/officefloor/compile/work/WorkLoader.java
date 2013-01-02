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
package net.officefloor.compile.work;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceProperty;
import net.officefloor.compile.spi.work.source.WorkSourceSpecification;
import net.officefloor.frame.api.execute.Work;

/**
 * Loads the {@link WorkType} from the {@link WorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WorkLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link WorkSourceSpecification} for the {@link WorkSource}.
	 * 
	 * @param workSourceClass
	 *            Class of the {@link WorkSource}.
	 * @return {@link PropertyList} of the {@link WorkSourceProperty} instances
	 *         of the {@link WorkSourceSpecification} or <code>null</code> if
	 *         issue, which is reported to the {@link CompilerIssues}.
	 */
	<W extends Work, WS extends WorkSource<W>> PropertyList loadSpecification(
			Class<WS> workSourceClass);

	/**
	 * Loads and returns the {@link WorkType} from the {@link WorkSource} class.
	 * 
	 * @param workSourceClass
	 *            Class of the {@link WorkSource}.
	 * @param properties
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link WorkType}.
	 * @return {@link WorkType} or <code>null</code> if issues, which is
	 *         reported to the {@link CompilerIssues}.
	 */
	<W extends Work, WS extends WorkSource<W>> WorkType<W> loadWorkType(
			Class<WS> workSourceClass, PropertyList properties);

}