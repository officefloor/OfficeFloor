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
import java.util.Properties;

import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.DutyBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeScope;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.DutyConfiguration;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Implementation of the {@link AdministratorBuilder}.
 * 
 * @author Daniel
 */
public class AdministratorBuilderImpl<I, A extends Enum<A>, AS extends AdministratorSource<I, A>>
		implements AdministratorBuilder<A>,
		AdministratorSourceConfiguration<A, AS> {

	/**
	 * {@link Class} of the {@link AdministratorSource}.
	 */
	protected Class<AS> administratorSourceClass;

	/**
	 * Registry of {@link DutyBuilder} instances.
	 */
	protected final Map<A, DutyBuilderImpl<A, ?>> duties = new HashMap<A, DutyBuilderImpl<A, ?>>();

	/**
	 * {@link Properties} for the {@link AdministratorSource}.
	 */
	protected final Properties properties = new Properties();

	/**
	 * Name of this {@link Administrator}.
	 */
	protected String administratorName;

	/**
	 * Scope of the {@link Administrator}.
	 */
	protected OfficeScope administratorScope = OfficeScope.WORK;

	/**
	 * Name of the {@link Team} responsible for the {@link Duty} instances of
	 * this {@link Administrator}.
	 */
	protected String teamName;

	/**
	 * Initiate.
	 * 
	 * @param administratorSourceClass
	 *            {@link Class} of the {@link AdministratorSource}.
	 */
	public AdministratorBuilderImpl(Class<AS> administratorSourceClass) {
		this.administratorSourceClass = administratorSourceClass;
	}

	/**
	 * Specifies the name for the {@link Administrator}.
	 * 
	 * @param name
	 *            Name for the {@link Administrator}.
	 */
	protected void setAdministratorName(String name) {
		this.administratorName = name;
	}

	/*
	 * ====================================================================
	 * AdministratorBuilder
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.AdministratorBuilder#setAdministratorScope(net.officefloor.frame.api.build.ManagedObjectScope)
	 */
	@Override
	public void setAdministratorScope(OfficeScope scope) {
		this.administratorScope = scope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.AdministratorBuilder#addProperty(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void addProperty(String name, String value) throws BuildException {
		this.properties.setProperty(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.AdministratorBuilder#setTeam(java.lang.String)
	 */
	@Override
	public void setTeam(String teamName) {
		this.teamName = teamName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.AdministratorBuilder#registerDutyBuilder(A,
	 *      java.lang.Class)
	 */
	@Override
	public <F extends Enum<F>> DutyBuilder<F> registerDutyBuilder(A dutyKey,
			Class<F> flowListingEnum) throws BuildException {

		// Create the duty builder
		DutyBuilderImpl<A, F> dutyBuilder = new DutyBuilderImpl<A, F>(dutyKey);

		// Register the duty builder
		this.duties.put(dutyKey, dutyBuilder);

		// Return the duty builder
		return dutyBuilder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.AdministratorBuilder#registerDutyBuilder(A)
	 */
	@Override
	public DutyBuilder<Indexed> registerDutyBuilder(A dutyKey)
			throws BuildException {

		// Create the duty builder
		DutyBuilderImpl<A, Indexed> dutyBuilder = new DutyBuilderImpl<A, Indexed>(
				dutyKey);

		// Register the duty builder
		this.duties.put(dutyKey, dutyBuilder);

		// Return the duty builder
		return dutyBuilder;
	}

	/*
	 * ====================================================================
	 * AdministratorSourceConfiguration
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration#getAdministratorName()
	 */
	@Override
	public String getAdministratorName() {
		return this.administratorName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration#getAdministratorSourceClass()
	 */
	@Override
	public Class<AS> getAdministratorSourceClass()
			throws ConfigurationException {
		return this.administratorSourceClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration#getProperties()
	 */
	@Override
	public Properties getProperties() {
		return this.properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration#getAdministratorScope()
	 */
	@Override
	public OfficeScope getAdministratorScope() {
		return this.administratorScope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration#getTeamName()
	 */
	@Override
	public String getTeamName() throws ConfigurationException {
		return this.teamName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration#getDutyConfiguration()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public DutyConfiguration<A>[] getDutyConfiguration() {
		return this.duties.values().toArray(new DutyConfiguration[0]);
	}

}
