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
package net.officefloor.eclipse.extension.workloader;

import java.util.List;

import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.WorkSpecification;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for extension to provide enriched {@link WorkLoader} usage.
 * 
 * @author Daniel
 * 
 * @see ExtensionClasspathProvider
 */
public interface WorkLoaderExtension {

	/**
	 * Extension ID.
	 */
	public static final String EXTENSION_ID = ExtensionUtil
			.getExtensionId("workloader");

	/**
	 * Obtains the class of the {@link WorkLoader} being enriched in its usage.
	 * 
	 * @return Class of the {@link WorkLoader} being enriched in its usage.
	 */
	Class<? extends WorkLoader> getWorkLoaderClass();

	/**
	 * Obtains the display name. This is a descriptive name that can be used
	 * other than the fully qualified name of the {@link WorkLoader}.
	 * 
	 * @return Display name.
	 */
	String getDisplayName();

	/**
	 * Populates the input page with the necessary {@link Control} instances to
	 * obtain the properties. Also allows notifying of changes to properties via
	 * the {@link WorkLoaderExtensionContext}.
	 * 
	 * @param page
	 *            Page to be setup for obtaining the properties.
	 * @param context
	 *            {@link WorkLoaderExtensionContext} to notify of changes to the
	 *            properties.
	 * @return Initial set of properties. Providing <code>null</code> or an
	 *         empty list will initiate on the {@link PropertyModel} instances
	 *         obtained from the {@link WorkSpecification}.
	 */
	List<WorkLoaderProperty> createControl(Composite page,
			WorkLoaderExtensionContext context);

	/**
	 * Obtains the suggested name of the {@link WorkModel}.
	 * 
	 * @param properties
	 *            Listing of populated {@link WorkLoaderProperty} instances.
	 * @return Suggested {@link WorkModel} name or <code>null</code> if no
	 *         suggestion.
	 */
	String getSuggestedWorkName(List<WorkLoaderProperty> properties);
}
