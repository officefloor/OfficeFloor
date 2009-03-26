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
package net.officefloor.frame.integrate.stress;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Tests {@link AsynchronousManagedObject} and {@link Task} instances waiting on
 * it to be ready for use.
 * 
 * @author Daniel
 */
public class AsynchronousManagedObjectStressTest extends
		AbstractOfficeConstructTestCase {

	// TODO stress test Asynchronous Managed Object start/complete
	@StressTest
	public void test_TODO_implement() {
		fail("TODO implement");
	}

}