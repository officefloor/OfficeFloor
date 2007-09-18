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
package net.officefloor.frame.impl.construct;

import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.internal.configuration.WorkAdministratorConfiguration;

/**
 * Implementation of the
 * {@link net.officefloor.frame.api.build.AdministrationBuilder}.
 * 
 * @author Daniel
 */
public class AdministrationBuilderImpl implements AdministrationBuilder,
		WorkAdministratorConfiguration {

	/**
	 * Name of the
	 * {@link net.officefloor.frame.spi.administration.Administrator} local to
	 * the {@link net.officefloor.frame.api.execute.Work}.
	 */
	protected final String workAdminName;

	/**
	 * Id of the
	 * {@link net.officefloor.frame.spi.administration.source.AdministratorSource}.
	 */
	protected final String administratorId;

	/**
	 * Order to administer the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} instances.
	 */
	protected String[] workManagedObjectNames;

	/**
	 * Initiate.
	 * 
	 * @param workAdminName
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}
	 *            local to the {@link net.officefloor.frame.api.execute.Work}.
	 * @param administratorId
	 *            Id of the
	 *            {@link net.officefloor.frame.spi.administration.source.AdministratorSource}.
	 */
	public AdministrationBuilderImpl(String workAdminName,
			String administratorId) {
		this.workAdminName = workAdminName;
		this.administratorId = administratorId;
	}

	/*
	 * ====================================================================
	 * AdministrationDutyBuilder
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.AdministrationDutyBuilder#setManagedObjects(java.lang.String[])
	 */
	public void setManagedObjects(String[] workManagedObjectNames)
			throws BuildException {
		this.workManagedObjectNames = workManagedObjectNames;
	}

	/*
	 * ====================================================================
	 * WorkAdministratorConfiguration
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.WorkAdministratorConfiguration#getAdministratorId()
	 */
	public String getAdministratorId() {
		return this.administratorId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.WorkAdministratorConfiguration#getWorkAdministratorName()
	 */
	public String getWorkAdministratorName() {
		return this.workAdminName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.WorkAdministratorConfiguration#getWorkManagedObjectNames()
	 */
	public String[] getWorkManagedObjectNames() {
		return this.workManagedObjectNames;
	}

}