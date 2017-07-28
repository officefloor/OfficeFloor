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
package net.officefloor.eclipse.wizard.administrationsource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.frame.api.administration.Administration;

/**
 * Instance of a {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationInstance {

	/**
	 * Name of this {@link Administration}.
	 */
	private final String administrationName;

	/**
	 * {@link AdministrationSource} class name.
	 */
	private final String administrationSourceClassName;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link AdministrationType}.
	 */
	private final AdministrationType<?, ?, ?> administrationType;

	/**
	 * Initiate for public use.
	 * 
	 * @param administrationName
	 *            Name of the {@link Administration}.
	 * @param administrationSourceClassName
	 *            {@link AdministrationSource} class name.
	 */
	public AdministrationInstance(String administrationName, String administrationSourceClassName) {
		this.administrationName = administrationName;
		this.administrationSourceClassName = administrationSourceClassName;
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		this.administrationType = null;
	}

	/**
	 * Initiate from {@link AdministrationSourceInstance}.
	 * 
	 * @param administrationName
	 *            Name of the {@link Administration}.
	 * @param administrationSourceClassName
	 *            {@link AdministrationSource} class name.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param administrationType
	 *            {@link AdministrationType}.
	 */
	AdministrationInstance(String administrationName, String administrationSourceClassName, PropertyList propertyList,
			AdministrationType<?, ?, ?> administrationType) {
		this.administrationName = administrationName;
		this.administrationSourceClassName = administrationSourceClassName;
		this.propertyList = propertyList;
		this.administrationType = administrationType;
	}

	/**
	 * Obtains the name of the {@link Administration}.
	 * 
	 * @return Name of the {@link Administration}.
	 */
	public String getAdministrationName() {
		return this.administrationName;
	}

	/**
	 * Obtains the {@link AdministrationSource} class name.
	 * 
	 * @return {@link AdministrationSource} class name.
	 */
	public String getAdministratorSourceClassName() {
		return this.administrationSourceClassName;
	}

	/**
	 * Obtains the {@link PropertyList}.
	 * 
	 * @return {@link PropertyList}.
	 */
	public PropertyList getPropertylist() {
		return this.propertyList;
	}

	/**
	 * Obtains the {@link AdministrationType}.
	 * 
	 * @return {@link AdministrationType} if obtained from
	 *         {@link AdministrationSourceInstance} or <code>null</code> if
	 *         initiated by <code>public</code> constructor.
	 */
	public AdministrationType<?, ?, ?> getAdministrationType() {
		return this.administrationType;
	}

}