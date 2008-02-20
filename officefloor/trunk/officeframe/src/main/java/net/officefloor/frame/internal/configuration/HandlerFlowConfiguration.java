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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Configuration of a {@link Flow} for a {@link Handler}.
 * 
 * @author Daniel
 */
public interface HandlerFlowConfiguration<F extends Enum<F>> {

	/**
	 * Obtains the name to identify this flow.
	 * 
	 * @return Name identifying this flow.
	 */
	String getFlowName();

	/**
	 * Obtains the key for this flow.
	 * 
	 * @return Key for this flow. May be <code>null</code> if not using
	 *         {@link Enum}.
	 */
	F getFlowKey();

	/**
	 * Obtains the {@link TaskNodeReference} for this flow.
	 * 
	 * @return {@link TaskNodeReference} to the flow.
	 */
	TaskNodeReference getTaskNodeReference();

}
