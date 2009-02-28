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
package net.officefloor.frame.impl.execute.officefloor;

import java.util.Map;

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectExecuteContext} implementation.
 * 
 * @author Daniel
 */
public class ManagedObjectExecuteContextImpl<H extends Enum<H>> implements
		ManagedObjectExecuteContext<H> {

	/**
	 * {@link Handler} instances for the {@link ManagedObjectSource}.
	 */
	private final Map<H, Handler<?>> handlers;

	/**
	 * Initiate.
	 * 
	 * @param handlers
	 *            {@link Handler} instances for the {@link ManagedObjectSource}.
	 */
	public ManagedObjectExecuteContextImpl(Map<H, Handler<?>> handlers) {
		this.handlers = handlers;
	}

	/*
	 * ================ ManagedObjectExecuteContext ==========================
	 */

	@Override
	public Handler<?> getHandler(H key) {
		return this.handlers.get(key);
	}

}