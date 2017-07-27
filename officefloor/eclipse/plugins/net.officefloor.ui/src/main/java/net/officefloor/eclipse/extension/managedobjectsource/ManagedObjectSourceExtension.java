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
package net.officefloor.eclipse.extension.managedobjectsource;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.open.ExtensionOpener;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Interface for extension to provide enriched {@link ManagedObjectSource}
 * usage.
 * 
 * @author Daniel Sagenschneider
 * 
 * @see ExtensionClasspathProvider
 * @see ExtensionOpener
 */
public interface ManagedObjectSourceExtension<D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> {

	/**
	 * Extension ID.
	 */
	public static final String EXTENSION_ID = ExtensionUtil.getExtensionId("managedobjectsources");

	/**
	 * Obtains the class of the {@link ManagedObjectSource} being enriched in
	 * its usage.
	 * 
	 * @return Class of the {@link ManagedObjectSource} being enriched in its
	 *         usage.
	 */
	Class<S> getManagedObjectSourceClass();

	/**
	 * Obtains the display name. This is a descriptive name that can be used
	 * other than the fully qualified name of the {@link ManagedObjectSource}.
	 * 
	 * @return Display name.
	 */
	String getManagedObjectSourceLabel();

	/**
	 * Populates the input page with the necessary {@link Control} instances to
	 * obtain the properties. Also allows notifying of changes to properties via
	 * the {@link ManagedObjectSourceExtensionContext}.
	 * 
	 * @param page
	 *            Page to be setup for obtaining the properties.
	 * @param context
	 *            {@link ManagedObjectSourceExtensionContext}.
	 */
	void createControl(Composite page, ManagedObjectSourceExtensionContext context);

	/**
	 * Obtains the suggested name of the {@link ManagedObjectSource}.
	 * 
	 * @param properties
	 *            Populate {@link PropertyList}.
	 * @return Suggested {@link ManagedObjectSource} name or <code>null</code>
	 *         if no suggestion.
	 */
	String getSuggestedManagedObjectSourceName(PropertyList properties);

}