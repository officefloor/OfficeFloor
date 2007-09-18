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

import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.DutyContext;

/**
 * <p>
 * Source to obtain a particular type of
 * {@link net.officefloor.frame.spi.administration.Administrator}.
 * <p>
 * Implemented by the
 * {@link net.officefloor.frame.spi.administration.Administrator} provider.
 * 
 * @author Daniel
 */
public interface AdministratorSource<I extends Object, A extends Enum<A>> {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	AdministratorSourceSpecification getSpecification();

	/**
	 * Called only once after the {@link AdministratorSource} is instantiated.
	 * 
	 * @param context
	 *            {@link AdministratorSourceContext} to initialise this instance
	 *            of the {@link AdministratorSource}.
	 * @throws Exception
	 *             Should the {@link AdministratorSource} fail to configure
	 *             itself from the input properties.
	 */
	void init(AdministratorSourceContext context) throws Exception;

	/**
	 * <p>
	 * Obtains the meta-data to describe this.
	 * <p>
	 * This is called after the {@link #init(DutyContext)} method and therefore
	 * may use the configuration.
	 * <p>
	 * This should always return non-null. If there is a problem due to
	 * incorrect configuration, the {@link #init(AdministratorSourceContext)}
	 * should indicate this via an exception.
	 * 
	 * @return Meta-data to describe this.
	 */
	AdministratorSourceMetaData<I, A> getMetaData();

	/**
	 * <p>
	 * Creates a new {@link Administrator}.
	 * 
	 * @return New {@link Administrator}.
	 */
	Administrator<I, A> createAdministrator();

}
