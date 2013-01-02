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
package net.officefloor.eclipse.extension.administratorsource;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.extension.open.ExtensionOpener;
import net.officefloor.frame.spi.administration.source.AdministratorSource;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for extension to provide enriched {@link AdministratorSource}
 * usage.
 * 
 * @author Daniel Sagenschneider
 * 
 * @see ExtensionClasspathProvider
 * @see ExtensionOpener
 */
public interface AdministratorSourceExtension<I, A extends Enum<A>, S extends AdministratorSource<I, A>> {

	/**
	 * Extension ID.
	 */
	public static final String EXTENSION_ID = ExtensionUtil
			.getExtensionId("administratorsources");

	/**
	 * Obtains the class of the {@link AdministratorSource} being enriched in
	 * its usage.
	 * 
	 * @return Class of the {@link AdministratorSource} being enriched in its
	 *         usage.
	 */
	Class<S> getAdministratorSourceClass();

	/**
	 * <p>
	 * Obtains the label for the {@link AdministratorSource}.
	 * <p>
	 * This is a descriptive name that can be used other than the fully
	 * qualified name of the {@link AdministratorSource}.
	 * 
	 * @return Label for the {@link AdministratorSource}.
	 */
	String getAdministratorSourceLabel();

	/**
	 * Loads the input page with the necessary {@link Control} instances to
	 * populate the {@link PropertyList}. Also allows notifying of changes to
	 * {@link Property} instances via the
	 * {@link AdministratorSourceExtensionContext}.
	 * 
	 * @param page
	 *            Page to be setup for populating the {@link PropertyList}.
	 * @param context
	 *            {@link AdministratorSourceExtensionContext}.
	 */
	void createControl(Composite page,
			AdministratorSourceExtensionContext context);

}