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

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Builder to build a {@link Handler}.
 * 
 * @author Daniel
 */
public interface HandlerBuilder<F extends Enum<F>> {

	/**
	 * Specifies the {@link HandlerFactory}.
	 * 
	 * @param factory
	 *            {@link HandlerFactory}.
	 */
	void setHandlerFactory(HandlerFactory<F> factory);

	/**
	 * Links in a {@link ProcessState} by specifying the first {@link Task} of
	 * the {@link ProcessState}.
	 * 
	 * @param key
	 *            Key identifying flow being invoked by the {@link Handler}.
	 * @param workName
	 *            Name of the {@link Work} that the {@link Task} resides on.
	 * @param taskName
	 *            Name of {@link Task}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link Flow} of the
	 *            invoked {@link ProcessState}. May be <code>null</code> to
	 *            indicate no argument.
	 */
	void linkProcess(F key, String workName, String taskName,
			Class<?> argumentType);

	/**
	 * Links in a {@link ProcessState} by specifying the first {@link Task} of
	 * the {@link ProcessState}.
	 * 
	 * @param processIndex
	 *            Index identifying the {@link ProcessState}.
	 * @param workName
	 *            Name of the {@link Work} that the {@link Task} resides on.
	 * @param taskName
	 *            Name of {@link Task}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link Flow} of the
	 *            invoked {@link ProcessState}. May be <code>null</code> to
	 *            indicate no argument.
	 */
	void linkProcess(int processIndex, String workName, String taskName,
			Class<?> argumentType);

}