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
package net.officefloor.frame.impl.construct.work;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.configuration.LinkedWorkAdministratorConfiguration;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.administration.Administrator;

/**
 * {@link LinkedWorkAdministratorConfiguration} implementation.
 * 
 * @author Daniel
 */
public class LinkedWorkAdministratorConfigurationImpl implements
		LinkedWorkAdministratorConfiguration {

	/**
	 * Name of {@link Administrator} within {@link Work}.
	 */
	private final String workAdministratorName;

	/**
	 * Name of {@link ThreadState} or {@link ProcessState} bound
	 * {@link Administrator}.
	 */
	private final String boundAdministratorName;

	/**
	 * Initiate.
	 * 
	 * @param workAdministratorName
	 *            Name of {@link Administrator} within {@link Work}.
	 * @param boundAdministratorName
	 *            Name of {@link ThreadState} or {@link ProcessState} bound
	 *            {@link Administrator}.
	 */
	public LinkedWorkAdministratorConfigurationImpl(
			String workAdministratorName, String boundAdministratorName) {
		this.workAdministratorName = workAdministratorName;
		this.boundAdministratorName = boundAdministratorName;
	}

	/*
	 * ============== LinkedWorkAdministratorConfiguration ==============
	 */

	@Override
	public String getWorkAdministratorName() {
		return this.workAdministratorName;
	}

	@Override
	public String getBoundAdministratorName() {
		return this.boundAdministratorName;
	}

}
