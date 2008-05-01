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
package net.officefloor.plugin.impl.socket.server;

import java.io.IOException;
import java.nio.channels.Selector;

/**
 * <p>
 * Factory for the creation of the {@link Selector}.
 * <p>
 * This wraps {@link Selector#open()}.
 * 
 * @author Daniel
 */
public class SelectorFactory {

	/**
	 * Creates the {@link Selector}.
	 * 
	 * @return New {@link Selector}.
	 * @throws IOException
	 *             If fails to create the {@link Selector}.
	 */
	public Selector createSelector() throws IOException {
		return Selector.open();
	}
}
