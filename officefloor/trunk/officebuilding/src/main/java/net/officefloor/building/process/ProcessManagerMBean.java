/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

/**
 * MBean interface for the {@link ProcessManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcessManagerMBean {

	/**
	 * Obtains the name identifying the {@link Process}.
	 * 
	 * @return Name identifying the {@link Process}.
	 */
	String getProcessName();

	/**
	 * Obtains the MBean domain name space for MBeans of the {@link Process}.
	 * 
	 * @return MBean domain name space for MBeans of the {@link Process}.
	 */
	String getMBeanDomainNamespace();

	/**
	 * <p>
	 * Triggers for a graceful shutdown of the {@link Process}.
	 * <p>
	 * This is a non-blocking call to allow a timeout on graceful shutdown.
	 * 
	 * @throws ProcessException
	 *             If fails to communicate with {@link Process}.
	 */
	void triggerStopProcess() throws ProcessException;

	/**
	 * Forcibly destroys the {@link Process}.
	 */
	void destroyProcess();

	/**
	 * Indicates if the {@link ManagedProcess} is complete.
	 * 
	 * @return <code>true</code> if the {@link ManagedProcess} is complete.
	 */
	boolean isProcessComplete();

}