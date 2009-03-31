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
package net.officefloor.compile.spi.work;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSourceProperty;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceSpecification;
import net.officefloor.frame.api.execute.Work;

/**
 * Loads the {@link WorkType} from the {@link WorkSource}.
 * 
 * @author Daniel
 */
public interface WorkLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link WorkSourceSpecification} for the {@link WorkSource}.
	 * 
	 * @param workSourceClass
	 *            Class of the {@link WorkSource}.
	 * @param issues
	 *            {@link CompilerIssues} to report issues in loading the
	 *            {@link WorkSourceSpecification} and obtaining the
	 *            {@link PropertyList}.
	 * @return {@link PropertyList} of the {@link WorkSourceProperty} instances
	 *         of the {@link WorkSourceSpecification} or <code>null</code> if
	 *         issue, which is reported to the {@link CompilerIssues}.
	 */
	<W extends Work, WS extends WorkSource<W>> PropertyList loadSpecification(
			Class<WS> workSourceClass, CompilerIssues issues);

	/**
	 * Loads and returns the {@link WorkType} from the {@link WorkSource}.
	 * 
	 * @param workSourceClass
	 *            Class of the {@link WorkSource}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link WorkType}.
	 * @param classLoader
	 *            {@link ClassLoader} that the {@link WorkSource} may use in
	 *            obtaining necessary class path resources.
	 * @param issues
	 *            {@link CompilerIssues} to report issues in loading the
	 *            {@link WorkType}.
	 * @return {@link WorkType} or <code>null</code> if issues, which is
	 *         reported to the {@link CompilerIssues}.
	 */
	<W extends Work, WS extends WorkSource<W>> WorkType<W> loadWorkType(
			Class<WS> workSourceClass, PropertyList propertyList,
			ClassLoader classLoader, CompilerIssues issues);

}