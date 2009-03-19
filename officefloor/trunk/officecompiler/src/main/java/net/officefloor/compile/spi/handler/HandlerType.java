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
package net.officefloor.compile.spi.handler;

import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link Handler}.
 * 
 * @author Daniel
 */
public interface HandlerType<F extends Enum<F>> {

	/**
	 * Obtains the {@link HandlerFactory} to create the {@link Handler}.
	 * 
	 * @return {@link HandlerFactory} to create the {@link Handler}.
	 */
	HandlerFactory<F> getHandlerFactory();

	/**
	 * Obtains the {@link Enum} providing the keys for the {@link Flow}
	 * instances instigated by the {@link Handler}.
	 * 
	 * @return {@link Enum} providing instigated {@link Flow} keys or
	 *         <code>null</code> if {@link Indexed} or no instigated
	 *         {@link Flow} instances.
	 */
	Class<F> getFlowKeyClass();

	/**
	 * Obtains the {@link HandlerFlowType} definitions for the possible
	 * {@link Flow} instances instigated by the {@link Handler}.
	 * 
	 * @return {@link HandlerFlowType} definitions for the possible {@link Flow}
	 *         instances instigated by the {@link Handler}.
	 */
	HandlerFlowType<F>[] getFlowTypes();

}