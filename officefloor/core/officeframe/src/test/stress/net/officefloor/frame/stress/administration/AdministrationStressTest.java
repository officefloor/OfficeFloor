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
package net.officefloor.frame.stress.administration;

import junit.framework.TestSuite;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.stress.AbstractStressTestCase;

/**
 * Stress tests the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(AdministrationStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {
		fail("TODO implement");
	}

}
