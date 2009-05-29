/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.wizard.administratorsource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;

/**
 * Instance of a {@link Administrator}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorInstance {

	/**
	 * Name of this {@link Administrator}.
	 */
	private final String administratorName;

	/**
	 * {@link AdministratorSource} class name.
	 */
	private final String administratorSourceClassName;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link AdministratorScope}.
	 */
	private final AdministratorScope administratorScope;

	/**
	 * {@link AdministratorType}.
	 */
	private final AdministratorType<?, ?> administratorType;

	/**
	 * Initiate for public use.
	 * 
	 * @param administratorName
	 *            Name of the {@link Administrator}.
	 * @param administratorSourceClassName
	 *            {@link AdministratorSource} class name.
	 * @param administratorScope
	 *            {@link AdministratorScope}.
	 */
	public AdministratorInstance(String administratorName,
			String administratorSourceClassName,
			AdministratorScope administratorScope) {
		this.administratorName = administratorName;
		this.administratorSourceClassName = administratorSourceClassName;
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		this.administratorScope = administratorScope;
		this.administratorType = null;
	}

	/**
	 * Initiate from {@link AdministratorSourceInstance}.
	 * 
	 * @param administratorName
	 *            Name of the {@link Administrator}.
	 * @param administratorSourceClassName
	 *            {@link AdministratorSource} class name.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param administratorScope
	 *            {@link AdministratorScope}.
	 * @param administratorType
	 *            {@link AdministratorType}.
	 */
	AdministratorInstance(String administratorName,
			String administratorSourceClassName, PropertyList propertyList,
			AdministratorScope administratorScope,
			AdministratorType<?, ?> administratorType) {
		this.administratorName = administratorName;
		this.administratorSourceClassName = administratorSourceClassName;
		this.propertyList = propertyList;
		this.administratorScope = administratorScope;
		this.administratorType = administratorType;
	}

	/**
	 * Obtains the name of the {@link Administrator}.
	 * 
	 * @return Name of the {@link Administrator}.
	 */
	public String getAdministratorName() {
		return this.administratorName;
	}

	/**
	 * Obtains the {@link AdministratorSource} class name.
	 * 
	 * @return {@link AdministratorSource} class name.
	 */
	public String getAdministratorSourceClassName() {
		return this.administratorSourceClassName;
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
	 * Obtains the {@link AdministratorScope}.
	 * 
	 * @return {@link AdministratorScope}.
	 */
	public AdministratorScope getAdministratorScope() {
		return this.administratorScope;
	}

	/**
	 * Obtains the {@link AdministratorType}.
	 * 
	 * @return {@link AdministratorType} if obtained from
	 *         {@link AdministratorSourceInstance} or <code>null</code> if
	 *         initiated by <code>public</code> constructor.
	 */
	public AdministratorType<?, ?> getAdministratorType() {
		return this.administratorType;
	}

}