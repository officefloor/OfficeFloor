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
package net.officefloor.mock;

import java.net.Socket;
import java.sql.Connection;

import net.officefloor.work.clazz.Flow;

/**
 * Mock work class providing many task methods for unit testing.
 * 
 * @author Daniel
 */
public class MockWork {

	public Object taskMethodOne(Object parameter, Flow flow, Connection connection) {
		return null;
	}

	public Object taskMethodTwo(Object parameter, Socket socket) {
		return null;
	}

	public Object taskMethodThree(Flow flowOne, Flow flowTwo) {
		return null;
	}

}
