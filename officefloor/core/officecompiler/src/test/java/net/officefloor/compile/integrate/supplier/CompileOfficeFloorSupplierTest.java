/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.integrate.supplier;

import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure can compile {@link SupplierSource} within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOfficeFloorSupplierTest extends OfficeFrameTestCase {

	/**
	 * Ensure can configure in simple {@link ManagedObjectSource}.
	 */
	public void testSimpleManagedObjectSource() {
		fail("TODO implement");
	}

	/**
	 * Ensure can configure {@link ManagedObjectDependency} for
	 * {@link ManagedObjectSource} supplied by {@link SupplierSource}.
	 */
	public void testManagedObjectSourceWithDependency() {
		fail("TODO implement");
	}

	/**
	 * Ensure can configure {@link Flow} for {@link ManagedObjectSource}
	 * supplied by {@link SupplierSource}.
	 */
	public void testManagedObjectSourceWithFlow() {
		fail("TODO implement");
	}

	/**
	 * Ensure can configure {@link Team} for {@link ManagedObjectSource}
	 * supplied by {@link SupplierSource}.
	 */
	public void testManagedObjectSourceWithTeam() {
		fail("TODO implement");
	}

}