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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.spi.team.Team;

/**
 * Implementation of {@link net.officefloor.frame.api.build.OfficeFloorBuilder}.
 * 
 * @author Daniel
 */
public class OfficeFloorBuilderImpl implements OfficeFloorBuilder,
		OfficeFloorConfiguration {

	/**
	 * Registry of the {@link ManagedObjectBuilderImpl} instances by their Id.
	 */
	private final Map<String, ManagedObjectBuilderImpl> mangedObjects = new HashMap<String, ManagedObjectBuilderImpl>();

	/**
	 * Registry of the {@link Team} instances by their name.
	 */
	private final Map<String, Team> teams = new HashMap<String, Team>();

	/**
	 * Registry of {@link OfficeBuilderImpl} instances by their name.
	 */
	private final Map<String, OfficeBuilderImpl> offices = new HashMap<String, OfficeBuilderImpl>();

	/**
	 * {@link EscalationProcedure}.
	 */
	private EscalationProcedure escalationProcedure = null;

	/*
	 * ====================================================================
	 * OfficeFloorBuilder
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.OfficeFloorBuilder#addManagedObject(java.lang.String,
	 *      net.officefloor.frame.api.build.ManagedObjectBuilder)
	 */
	public void addManagedObject(String id,
			ManagedObjectBuilder managedObjectBuilder) throws BuildException {

		// Ensure is correct type
		if (!(managedObjectBuilder instanceof ManagedObjectBuilderImpl)) {
			throw new BuildException(ManagedObjectBuilder.class.getName()
					+ " must be a " + ManagedObjectBuilderImpl.class.getName()
					+ " but is a " + managedObjectBuilder.getClass().getName());
		}

		// Specify the Id
		ManagedObjectBuilderImpl impl = (ManagedObjectBuilderImpl) managedObjectBuilder;
		impl.setManagedObjectName(id);

		// Add
		this.mangedObjects.put(id, impl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.OfficeFloorBuilder#addTeam(java.lang.String,
	 *      net.officefloor.frame.spi.team.Team)
	 */
	public void addTeam(String id, Team team) throws BuildException {
		this.teams.put(id, team);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.OfficeFloorBuilder#addOffice(java.lang.String,
	 *      net.officefloor.frame.api.build.OfficeBuilder)
	 */
	public void addOffice(String id, OfficeBuilder officeBuilder)
			throws BuildException {

		// Ensure is correct type
		if (!(officeBuilder instanceof OfficeBuilderImpl)) {
			throw new BuildException(OfficeBuilder.class.getName()
					+ " must be a " + OfficeBuilderImpl.class.getName()
					+ " but is a " + officeBuilder.getClass().getName());
		}

		// Specify the Id
		OfficeBuilderImpl impl = (OfficeBuilderImpl) officeBuilder;
		impl.setOfficeName(id);

		// Add
		this.offices.put(id, impl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.OfficeFloorBuilder#setEscalationProcedure(net.officefloor.frame.internal.structure.EscalationProcedure)
	 */
	@Override
	public void setEscalationProcedure(EscalationProcedure escalationProcedure)
			throws BuildException {
		this.escalationProcedure = escalationProcedure;
	}

	/*
	 * ====================================================================
	 * OfficeFloorConfiguration
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.OfficeFloorConfiguration#getManagedObjectSourceConfiguration()
	 */
	public ManagedObjectSourceConfiguration[] getManagedObjectSourceConfiguration()
			throws ConfigurationException {
		return this.mangedObjects.values().toArray(
				new ManagedObjectSourceConfiguration[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.OfficeFloorConfiguration#getTeamRegistry()
	 */
	public Map<String, Team> getTeamRegistry() throws ConfigurationException {
		return this.teams;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.OfficeFloorConfiguration#getOfficeConfiguration()
	 */
	public OfficeConfiguration[] getOfficeConfiguration()
			throws ConfigurationException {
		return this.offices.values().toArray(new OfficeConfiguration[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.OfficeFloorConfiguration#getEscalationProcedure()
	 */
	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

}
