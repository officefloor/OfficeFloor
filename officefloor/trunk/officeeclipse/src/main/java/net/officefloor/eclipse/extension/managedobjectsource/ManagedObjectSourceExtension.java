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
package net.officefloor.eclipse.extension.managedobjectsource;

import java.util.List;

import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for extension to provide enriched {@link ManagedObjectSource}
 * usage.
 * 
 * @author Daniel
 * 
 * @see ExtensionClasspathProvider
 */
public interface ManagedObjectSourceExtension {

	/**
	 * Extension ID.
	 */
	public static final String EXTENSION_ID = ExtensionUtil
			.getExtensionId("managedobjectsource");

	/**
	 * Obtains the class of the {@link ManagedObjectSource} being enriched in
	 * its usage.
	 * 
	 * @return Class of the {@link ManagedObjectSource} being enriched in its
	 *         usage.
	 */
	Class<? extends ManagedObjectSource<?, ?>> getManagedObjectSourceClass();

	/**
	 * Flags if the {@link ManagedObjectSource} represented by this
	 * {@link ManagedObjectSourceExtension} is usable. <code>false</code>
	 * indicates the {@link ManagedObjectSource} is likely a mock/test that
	 * should not be used.
	 * 
	 * @return <code>true</code> if the {@link ManagedObjectSource} may be used
	 *         in an application.
	 */
	boolean isUsable();

	/**
	 * Obtains the display name. This is a descriptive name that can be used
	 * other than the fully qualified name of the {@link ManagedObjectSource}.
	 * 
	 * @return Display name.
	 */
	String getDisplayName();

	/**
	 * Populates the input page with the necessary {@link Control} instances to
	 * obtain the properties. Also allows notifying of changes to properties via
	 * the {@link ManagedObjectSourceExtensionContext}.
	 * 
	 * @param page
	 *            Page to be setup for obtaining the properties.
	 * @param context
	 *            {@link ManagedObjectSourceExtensionContext} to notify of
	 *            changes to the properties.
	 * @return Initial set of properties. Providing <code>null</code> or an
	 *         empty list will initiate on the
	 *         {@link ManagedObjectSourceProperty} instances obtained from the
	 *         {@link ManagedObjectSourceSpecification}.
	 */
	List<InitiateProperty> createControl(Composite page,
			ManagedObjectSourceExtensionContext context);

	/**
	 * Obtains the suggested name of the {@link ManagedObjectSource}.
	 * 
	 * @param properties
	 *            Listing of populated {@link InitiateProperty} instances.
	 * @return Suggested {@link ManagedObjectSource} name or <code>null</code>
	 *         if no suggestion.
	 */
	String getSuggestedManagedObjectSourceName(List<InitiateProperty> properties);

}
