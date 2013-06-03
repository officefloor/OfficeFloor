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
package net.officefloor.eclipse.jpa;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.jpa.JpaEntityManagerManagedObjectSource;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link ManagedObjectSourceExtension} for the
 * {@link JpaEntityManagerManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class JpaEntityManagerManagedObjectSourceExtension
		implements
		ManagedObjectSourceExtension<None, None, JpaEntityManagerManagedObjectSource> {

	/*
	 * ===================== ManagedObjectSourceExtension ======================
	 */

	@Override
	public Class<JpaEntityManagerManagedObjectSource> getManagedObjectSourceClass() {
		return JpaEntityManagerManagedObjectSource.class;
	}

	@Override
	public String getManagedObjectSourceLabel() {
		return "JPA EntityManager";
	}

	@Override
	public void createControl(Composite page,
			ManagedObjectSourceExtensionContext context) {

		// Specify layout of page
		SourceExtensionUtil.loadPropertyLayout(page);

		// Provide property for persistence unit
		SourceExtensionUtil
				.createPropertyText(
						"Persistence Unit",
						JpaEntityManagerManagedObjectSource.PROPERTY_PERSISTENCE_UNIT_NAME,
						null, page, context, null);

		// Provide additional dynamic properties for the Entity Manager
		SourceExtensionUtil
				.createPropertyList(
						"Properties",
						page,
						context,
						JpaEntityManagerManagedObjectSource.PROPERTY_PERSISTENCE_UNIT_NAME);
	}

	@Override
	public String getSuggestedManagedObjectSourceName(PropertyList properties) {
		return "EntityManager";
	}

}
// END SNIPPET: tutorial