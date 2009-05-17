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
package net.officefloor.compile.administrator;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceProperty;
import net.officefloor.frame.spi.administration.source.AdministratorSourceSpecification;

/**
 * Loads the {@link AdministratorType} from the {@link AdministratorSource}.
 * 
 * @author Daniel
 */
public interface AdministratorLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link AdministratorSourceSpecification} for the
	 * {@link AdministratorSource}.
	 * 
	 * @param administratorSourceClass
	 *            {@link AdministratorSource} class.
	 * @return {@link PropertyList} of the {@link AdministratorSourceProperty}
	 *         instances of the {@link AdministratorSourceSpecification} or
	 *         <code>null</code> if issues, which are reported to the
	 *         {@link CompilerIssues}.
	 */
	<I, A extends Enum<A>, AS extends AdministratorSource<I, A>> PropertyList loadSpecification(
			Class<AS> administratorSourceClass);

	/**
	 * Loads and returns the {@link AdministratorType} sourced from the
	 * {@link AdministratorSource}.
	 * 
	 * @param administratorSourceClass
	 *            Class of the {@link AdministratorSource}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link AdministratorType}.
	 * @return {@link AdministratorType} or <code>null</code> if issues, which
	 *         are reported to the {@link CompilerIssues}.
	 */
	// TODO rename to loadAdministratorType
	<I, A extends Enum<A>, AS extends AdministratorSource<I, A>> AdministratorType<I, A> loadAdministrator(
			Class<AS> administratorSourceClass, PropertyList propertyList);

}