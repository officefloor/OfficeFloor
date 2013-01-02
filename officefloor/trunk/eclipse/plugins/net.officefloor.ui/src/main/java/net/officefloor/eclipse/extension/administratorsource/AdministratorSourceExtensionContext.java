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

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceSpecification;

import org.eclipse.core.resources.IProject;

/**
 * Context for the {@link AdministratorSourceExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorSourceExtensionContext {

	/**
	 * Specifies the title.
	 * 
	 * @param title
	 *            Title.
	 */
	void setTitle(String title);

	/**
	 * <p>
	 * Obtains the {@link PropertyList}. All changes to {@link Property}
	 * instances for the {@link AdministratorSource} are to be done on this
	 * {@link PropertyList}.
	 * <p>
	 * The {@link PropertyList} may be in any state:
	 * <ol>
	 * <li>no value {@link PropertyList} populated from the
	 * {@link AdministratorSourceSpecification} (creating a {@link Administrator})</li>
	 * <li>any changed state based on editing of the {@link Administrator} (editing
	 * {@link Administrator})</li>
	 * </ol>
	 * 
	 * @return {@link PropertyList}.
	 */
	PropertyList getPropertyList();

	/**
	 * <p>
	 * Notifies of a change to the {@link PropertyList}.
	 * <p>
	 * This allows for to report issues in attempting to source the
	 * {@link AdministratorType} from the {@link AdministratorSource} with the
	 * {@link PropertyList}.
	 */
	void notifyPropertiesChanged();

	/**
	 * Specifies an error message. Calling this after notifying of property
	 * changes allows for overriding the error message reported to the user.
	 * 
	 * @param message
	 *            Error message. <code>null</code> indicating no error.
	 */
	void setErrorMessage(String message);

	/**
	 * Obtains the {@link IProject} that is adding the {@link Administrator}.
	 * 
	 * @return {@link IProject} that is adding the {@link Administrator}.
	 */
	IProject getProject();

}