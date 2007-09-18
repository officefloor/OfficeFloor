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
package net.officefloor.frame.api.build;

/**
 * Builds the {@link net.officefloor.frame.spi.managedobject.ManagedObject}
 * instances should be administered.
 * 
 * @author Daniel
 */
public interface AdministrationBuilder {

	/**
	 * <p>
	 * Specifies the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} instances
	 * to be administerred by this
	 * {@link net.officefloor.frame.spi.administration.Duty} instances of the
	 * {@link net.officefloor.frame.spi.administration.Administrator}.
	 * 
	 * @param workManagedObjectNames
	 *            Names of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            instances local to the
	 *            {@link net.officefloor.frame.api.execute.Work} listed in order
	 *            to be administerred.
	 * @throws BuildException
	 *             Build failure.
	 */
	void setManagedObjects(String[] workManagedObjectNames)
			throws BuildException;

}
