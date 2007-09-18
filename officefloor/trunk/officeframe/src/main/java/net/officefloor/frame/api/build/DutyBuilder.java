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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Work;

/**
 * <p>
 * Builder to build a {@link net.officefloor.frame.spi.administration.Duty}.
 * <p>
 * All linked {@link net.officefloor.frame.internal.structure.Flow} instances
 * will instigated in parallel.
 * 
 * @author Daniel
 */
public interface DutyBuilder<F extends Enum<F>> {

	/**
	 * Links in a {@link net.officefloor.frame.internal.structure.Flow} by
	 * specifying the first {@link net.officefloor.frame.api.execute.Task} of
	 * the {@link net.officefloor.frame.internal.structure.Flow}.
	 * 
	 * @param F
	 *            {@link Enum} type for the listing of
	 *            {@link net.officefloor.frame.internal.structure.Flow}
	 *            instances to link to this
	 *            {@link net.officefloor.frame.api.execute.Task}.
	 * @param key
	 *            Key identifying the
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 * @param workName
	 *            Name of the {@link Work} that the first
	 *            {@link net.officefloor.frame.api.execute.Task} of the
	 *            {@link net.officefloor.frame.internal.structure.Flow} resides
	 *            on.
	 * @param taskName
	 *            Name of {@link net.officefloor.frame.api.execute.Task} that
	 *            resides on the {@link Work}.
	 */
	void linkFlow(F key, String workName, String taskName);

	/**
	 * Links in a {@link net.officefloor.frame.internal.structure.Flow} by
	 * specifying the first {@link net.officefloor.frame.api.execute.Task} of
	 * the {@link net.officefloor.frame.internal.structure.Flow}.
	 * 
	 * @param flowIndex
	 *            Index identifying the
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 * @param workName
	 *            Name of the {@link Work} that the first
	 *            {@link net.officefloor.frame.api.execute.Task} of the
	 *            {@link net.officefloor.frame.internal.structure.Flow} resides
	 *            on.
	 * @param taskName
	 *            Name of {@link net.officefloor.frame.api.execute.Task} that
	 *            resides on the {@link Work}.
	 */
	void linkFlow(int flowIndex, String workName, String taskName);

}
