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

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Context for the {@link ManagedObjectSourceExtension}.
 * 
 * @author Daniel
 */
public interface ManagedObjectSourceExtensionContext {

	/**
	 * Specifies the title.
	 * 
	 * @param title
	 *            Title.
	 */
	void setTitle(String title);

	/**
	 * Notifies of a change to the properties.
	 * 
	 * @param properties
	 *            {@link InitiateProperty} instances.
	 */
	void notifyPropertiesChanged(List<InitiateProperty> properties);

	/**
	 * Specifies an error message. Calling this after notifying of property
	 * changes allows for overriding the error message reported to the user.
	 * 
	 * @param message
	 *            Error message. <code>null</code> indicating no error.
	 */
	void setErrorMessage(String message);

	/**
	 * Obtains the {@link IProject} that is adding the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link IProject} that is adding the {@link ManagedObjectSource}.
	 */
	IProject getProject();

	/**
	 * Obtains the {@link Shell} to enable {@link MessageDialog} or other
	 * graphics requiring it.
	 * 
	 * @return {@link Shell}.
	 */
	Shell getShell();

}
