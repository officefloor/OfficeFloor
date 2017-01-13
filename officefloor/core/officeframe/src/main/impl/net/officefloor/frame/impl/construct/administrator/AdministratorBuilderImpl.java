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
package net.officefloor.frame.impl.construct.administrator;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.administration.source.AdministratorSource;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.Duty;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.DutyBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.DutyConfiguration;

/**
 * Implementation of the {@link AdministrationBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorBuilderImpl<I, A extends Enum<A>, AS extends AdministratorSource<I, A>>
		implements AdministrationBuilder<A>,
		AdministrationConfiguration<A, AS> {

	/**
	 * Name of this {@link Administration}.
	 */
	private final String administratorName;

	/**
	 * {@link Class} of the {@link AdministratorSource}.
	 */
	private final Class<AS> administratorSourceClass;

	/**
	 * {@link SourceProperties} for the {@link AdministratorSource}.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/**
	 * Name of the {@link Team} responsible for the {@link AdministrationDuty} instances of
	 * this {@link Administration}.
	 */
	private String officeTeamName;

	/**
	 * Listing of the scope names of the {@link ManagedObject} instances.
	 */
	private final List<String> administeredManagedObjectNames = new LinkedList<String>();

	/**
	 * Listing of {@link DutyConfiguration} instances.
	 */
	private final List<DutyConfiguration<A>> duties = new LinkedList<DutyConfiguration<A>>();

	/**
	 * Initiate.
	 * 
	 * @param administratorName
	 *            Name of the {@link Administration}.
	 * @param administratorSourceClass
	 *            {@link Class} of the {@link AdministratorSource}.
	 */
	public AdministratorBuilderImpl(String administratorName,
			Class<AS> administratorSourceClass) {
		this.administratorName = administratorName;
		this.administratorSourceClass = administratorSourceClass;
	}

	/*
	 * ================ AdministratorBuilder ==============================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name, value);
	}

	@Override
	public void setTeam(String officeTeamName) {
		this.officeTeamName = officeTeamName;
	}

	@Override
	public void administerManagedObject(String scopeManagedObjectName) {
		this.administeredManagedObjectNames.add(scopeManagedObjectName);
	}

	@Override
	public DutyBuilder addDuty(String dutyName) {
		DutyBuilderImpl<A> dutyBuilder = new DutyBuilderImpl<A>(dutyName);
		this.duties.add(dutyBuilder);
		return dutyBuilder;
	}

	/*
	 * ============= AdministratorSourceConfiguration =====================
	 */

	@Override
	public String getAdministratorName() {
		return this.administratorName;
	}

	@Override
	public Class<AS> getAdministratorSourceClass() {
		return this.administratorSourceClass;
	}

	@Override
	public SourceProperties getProperties() {
		return this.properties;
	}

	@Override
	public String getOfficeTeamName() {
		return this.officeTeamName;
	}

	@Override
	public String[] getAdministeredManagedObjectNames() {
		return this.administeredManagedObjectNames.toArray(new String[0]);
	}

	@Override
	@SuppressWarnings("unchecked")
	public DutyConfiguration<A>[] getDutyConfiguration() {
		return this.duties.toArray(new DutyConfiguration[0]);
	}

}