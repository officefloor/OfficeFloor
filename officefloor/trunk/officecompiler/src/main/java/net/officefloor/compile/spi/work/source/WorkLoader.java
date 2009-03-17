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
package net.officefloor.compile.spi.work.source;

import net.officefloor.model.work.WorkModel;

/**
 * Loads the {@link WorkModel}.
 * 
 * @author Daniel
 */
public interface WorkLoader {

	/**
	 * <p>
	 * Obtains the {@link WorkSpecification} for this {@link WorkLoader}.
	 * <p>
	 * This enables the {@link WorkLoaderContext} to be populated with the
	 * necessary details as per this {@link WorkSpecification} in loading the
	 * {@link WorkModel}.
	 * 
	 * @return {@link WorkSpecification}.
	 */
	WorkSpecification getSpecification();

	/**
	 * Loads the {@link WorkModel} from configuration.
	 * 
	 * @param context
	 *            {@link WorkLoaderContext} to source details to load the
	 *            {@link WorkModel}.
	 * @throws Exception
	 *             If fails.
	 */
	WorkModel<?> loadWork(WorkLoaderContext context) throws Exception;

}