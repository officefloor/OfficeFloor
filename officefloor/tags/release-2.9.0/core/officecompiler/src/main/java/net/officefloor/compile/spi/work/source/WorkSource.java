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
package net.officefloor.compile.spi.work.source;

import net.officefloor.frame.api.execute.Work;

/**
 * Sources the {@link WorkType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WorkSource<W extends Work> {

	/**
	 * <p>
	 * Obtains the {@link WorkSourceSpecification} for this {@link WorkSource}.
	 * <p>
	 * This enables the {@link WorkSourceContext} to be populated with the
	 * necessary details as per this {@link WorkSourceSpecification} in loading the
	 * {@link WorkType}.
	 * 
	 * @return {@link WorkSourceSpecification}.
	 */
	WorkSourceSpecification getSpecification();

	/**
	 * Sources the {@link WorkType} by populating it via the input
	 * {@link WorkTypeBuilder}.
	 * 
	 * @param workTypeBuilder
	 *            {@link WorkTypeBuilder} to be populated with the
	 *            <code>type definition</code> of the {@link Work}.
	 * @param context
	 *            {@link WorkSourceContext} to source details to populate the
	 *            {@link WorkTypeBuilder}.
	 * @throws Exception
	 *             If fails to populate the {@link WorkTypeBuilder}.
	 */
	void sourceWork(WorkTypeBuilder<W> workTypeBuilder,
			WorkSourceContext context) throws Exception;

}