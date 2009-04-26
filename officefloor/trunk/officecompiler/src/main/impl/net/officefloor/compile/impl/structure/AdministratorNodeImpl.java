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
package net.officefloor.compile.impl.structure;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.impl.administrator.AdministratorLoaderImpl;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.internal.structure.AdministratorNode;
import net.officefloor.compile.internal.structure.DutyNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.AdministerableManagedObject;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.compile.spi.office.OfficeDuty;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;

/**
 * {@link AdministratorNode} implementation.
 * 
 * @author Daniel
 */
public class AdministratorNodeImpl implements AdministratorNode {

	/**
	 * Name of this {@link OfficeAdministrator}.
	 */
	private final String administratorName;

	/**
	 * Class name of the {@link AdministratorSource}.
	 */
	private final String administratorSourceClassName;

	/**
	 * {@link PropertyList} to source the {@link Administrator}.
	 */
	private final PropertyList properties = new PropertyListImpl();

	/**
	 * Location of the {@link Office} containing this
	 * {@link OfficeAdministrator}.
	 */
	private final String officeLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link DutyNode} instances by their {@link OfficeDuty} name.
	 */
	private final Map<String, DutyNode> duties = new HashMap<String, DutyNode>();

	/**
	 * Listing of {@link AdministerableManagedObject} instances to administer.
	 */
	private final List<AdministerableManagedObject> administeredManagedObjects = new LinkedList<AdministerableManagedObject>();

	/**
	 * Initiate.
	 * 
	 * @param administratorName
	 *            Name of this {@link OfficeAdministrator}.
	 * @param administratorSourceClassName
	 *            Class name of the {@link AdministratorSource}.
	 * @param officeLocation
	 *            Location of the {@link Office} containing this
	 *            {@link OfficeAdministrator}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public AdministratorNodeImpl(String administratorName,
			String administratorSourceClassName, String officeLocation,
			NodeContext context) {
		this.administratorName = administratorName;
		this.administratorSourceClassName = administratorSourceClassName;
		this.officeLocation = officeLocation;
		this.context = context;
	}

	/*
	 * ======================= OfficeAdministrator ========================
	 */

	@Override
	public String getOfficeAdministratorName() {
		return this.administratorName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	@Override
	public OfficeDuty getDuty(String dutyName) {
		// Obtain and return the duty for the name
		DutyNode duty = this.duties.get(dutyName);
		if (duty == null) {
			// Add the duty
			duty = new DutyNodeImpl(dutyName);
			this.duties.put(dutyName, duty);
		}
		return duty;
	}

	@Override
	public void administerManagedObject(
			AdministerableManagedObject managedObject) {

		// Administer the managed object
		this.administeredManagedObjects.add(managedObject);

		// Make office object aware of administration
		if (managedObject instanceof OfficeObjectNode) {
			((OfficeObjectNode) managedObject).addAdministrator(this);
		}
	}

	/*
	 * ===================== AdministratorNode ==============================
	 */

	/**
	 * Lazy loaded {@link AdministratorType}.
	 */
	private AdministratorType<?, ?> administratorType = null;

	@Override
	public AdministratorType<?, ?> getAdministratorType() {

		// Lazy load the administrator type
		if (this.administratorType == null) {

			// Obtain the administrator source class
			Class<? extends AdministratorSource<?, ?>> administratorSourceClass = this.context
					.getAdministratorSourceClass(
							this.administratorSourceClassName,
							this.officeLocation, this.administratorName);
			if (administratorSourceClass == null) {
				return null; // must obtain source class
			}

			// Load the administrator type
			AdministratorLoader loader = new AdministratorLoaderImpl(
					LocationType.OFFICE, this.officeLocation,
					this.administratorName, this.context);
			this.administratorType = loader.loadAdministrator(
					administratorSourceClass, this.properties);
		}

		// Return administrator type
		return this.administratorType;
	}

}