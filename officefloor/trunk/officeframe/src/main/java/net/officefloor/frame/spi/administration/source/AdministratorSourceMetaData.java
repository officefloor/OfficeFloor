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
package net.officefloor.frame.spi.administration.source;

/**
 * Meta-data of the
 * {@link net.officefloor.frame.spi.administration.source.AdministratorSource}.
 * 
 * @author Daniel
 */
public interface AdministratorSourceMetaData<I extends Object, A extends Enum<A>> {

	/**
	 * Obtains the {@link Class} that the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} must
	 * provide to be administrated by this.
	 * 
	 * @return Extension interface for the
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	Class<I> getExtensionInterface();

	/**
	 * Obtains the {@link Enum} specifying the duties that this
	 * {@link net.officefloor.frame.spi.administration.Administrator} may
	 * undertake.
	 * 
	 * @return {@link Enum} specifying the duties of this
	 *         {@link net.officefloor.frame.spi.administration.Administrator}
	 */
	Class<A> getAministratorDutyKeys();

}
