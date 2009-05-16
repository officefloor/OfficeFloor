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

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.administration.Duty;

/**
 * <p>
 * Builder to build a {@link Duty}.
 * <p>
 * All linked {@link Flow} instances will be instigated in parallel.
 * 
 * @author Daniel
 */
public interface DutyBuilder {

	/**
	 * Links in a {@link Flow} by specifying the first {@link Task} of the
	 * {@link Flow}.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow}.
	 * @param workName
	 *            Name of the {@link Work} that the first {@link Task} of the
	 *            {@link Flow} resides on.
	 * @param taskName
	 *            Name of {@link Task} that resides on the {@link Work}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link Flow}. May be
	 *            <code>null</code> to indicate no argument.
	 */
	<F extends Enum<F>> void linkFlow(F key, String workName, String taskName,
			Class<?> argumentType);

	/**
	 * Links in a {@link Flow} by specifying the first {@link Task} of the
	 * {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow}.
	 * @param workName
	 *            Name of the {@link Work} that the first {@link Task} of the
	 *            {@link Flow} resides on.
	 * @param taskName
	 *            Name of {@link Task} that resides on the {@link Work}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link Flow}. May be
	 *            <code>null</code> to indicate no argument.
	 */
	void linkFlow(int flowIndex, String workName, String taskName,
			Class<?> argumentType);

}