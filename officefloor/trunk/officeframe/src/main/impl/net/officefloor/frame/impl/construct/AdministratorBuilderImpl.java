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
import net.officefloor.frame.spi.administration.source.AdministratorSource;

/**
 * Implementation of the
 * {@link net.officefloor.frame.api.build.AdministratorBuilder}.
 * 
 * @author Daniel
 */
public class AdministratorBuilderImpl<A extends Enum<A>> implements
		AdministratorBuilder<A>, AdministratorSourceConfiguration {

	/**
	 * Registry of {@link DutyBuilder} instances.
	 */
	protected final Map<A, DutyBuilderImpl<A, ?>> duties = new HashMap<A, DutyBuilderImpl<A, ?>>();

	/**
	 * {@link Properties} for the {@link AdministratorSource}.
	 */
	protected final Properties properties = new Properties();

	/**
	 * Name of this
	 * {@link net.officefloor.frame.spi.administration.Administrator}.
	 */
	protected String administratorName;

	/**
	 * {@link Class} of the {@link AdministratorSource}.
	 */
	@SuppressWarnings("unchecked")
	protected Class administratorSourceClass;

	/**
	 * Scope of the
	 * {@link net.officefloor.frame.spi.administration.Administrator}.
	 */
	protected OfficeScope administratorScope = OfficeScope.WORK;

	/**
	 * Specifies the name for the
	 * {@link net.officefloor.frame.spi.administration.Administrator}.
	 * 
	 * @param name
	 *            Name for the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}.
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
	 * @see net.officefloor.frame.api.build.TaskAdministratorBuilder#setTaskAdministratorSourceClass(java.lang.Class)
	 */
	public <S extends AdministratorSource<?, ?>> void setAdministratorSourceClass(
			Class<S> administratorSourceClass) throws BuildException {
		this.administratorSourceClass = administratorSourceClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.AdministratorBuilder#setAdministratorScope(net.officefloor.frame.api.build.ManagedObjectScope)
	 */
	public void setAdministratorScope(OfficeScope scope) {
		this.administratorScope = scope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskAdministratorBuilder#addProperty(java.lang.String,
	 *      java.lang.String)
	 */
	public void addProperty(String name, String value) throws BuildException {
		this.properties.setProperty(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.AdministratorBuilder#registerDutyBuilder(A,
	 *      java.lang.Class)
	 */
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
	public String getAdministratorName() {
		return this.administratorName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskAdministratorSourceConfiguration#getManagedObjectSourceClass()
	 */
	@SuppressWarnings("unchecked")
	public <TS extends AdministratorSource<?, ?>> Class<TS> getAdministratorSourceClass()
			throws ConfigurationException {
		return this.administratorSourceClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskAdministratorSourceConfiguration#getProperties()
	 */
	public Properties getProperties() {
		return this.properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration#getAdministratorScope()
	 */
	public OfficeScope getAdministratorScope() {
		return this.administratorScope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration#getDutyConfiguration()
	 */
	public DutyConfiguration<?>[] getDutyConfiguration() {
		return this.duties.values().toArray(new DutyConfiguration[0]);
	}

}
