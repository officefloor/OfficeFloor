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
package net.officefloor.building.process;

import java.io.IOException;
import java.rmi.registry.Registry;

import javax.management.MBeanServer;

import net.officefloor.console.OfficeBuilding;

/**
 * MBean interface for the {@link ProcessManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcessManagerMBean {

	/**
	 * <p>
	 * Obtains the name given to the {@link Process}.
	 * <p>
	 * The name may <i>not</i> be unique as more than one {@link Process} may be
	 * started with the same name.
	 * <p>
	 * See {@link #getProcessNamespace()} for a unique name.
	 * 
	 * @return Name given to the {@link Process}.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link ProcessManagerMBean}.
	 * 
	 * @see #getProcessNamespace()
	 */
	String getProcessName() throws IOException;

	/**
	 * <p>
	 * Obtains the unique name space for {@link Process}.
	 * <p>
	 * This is the unique name that identifies the {@link Process} running
	 * within the {@link OfficeBuilding}. The name is derived from the
	 * {@link #getProcessName()} however to maintain uniqueness the process name
	 * may be adjusted (typically by providing a unique suffix).
	 * 
	 * @return Unique name space for the {@link Process}.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link ProcessManagerMBean}.
	 */
	String getProcessNamespace() throws IOException;

	/**
	 * Obtains the host name that the {@link Process} is running on.
	 * 
	 * @return Host name that the {@link Process} is running on.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link ProcessManagerMBean}.
	 */
	String getProcessHostName() throws IOException;

	/**
	 * Obtains the port to access the {@link Registry} containing the
	 * {@link MBeanServer} for this {@link Process}.
	 * 
	 * @return Port to access the {@link Registry} containing the
	 *         {@link MBeanServer} for this {@link Process}.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link ProcessManagerMBean}.
	 */
	int getProcessPort() throws IOException;

	/**
	 * <p>
	 * Triggers for a graceful shutdown of the {@link Process}.
	 * <p>
	 * This is a non-blocking call to allow a timeout on graceful shutdown.
	 * 
	 * @throws ProcessException
	 *             If fails to stop the {@link Process}.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link ProcessManagerMBean}.
	 */
	void triggerStopProcess() throws ProcessException, IOException;

	/**
	 * Forcibly destroys the {@link Process}.
	 * 
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link ProcessManagerMBean}.
	 */
	void destroyProcess() throws IOException;

	/**
	 * Indicates if the {@link ManagedProcess} is complete.
	 * 
	 * @return <code>true</code> if the {@link ManagedProcess} is complete.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link ProcessManagerMBean}.
	 */
	boolean isProcessComplete() throws IOException;

}