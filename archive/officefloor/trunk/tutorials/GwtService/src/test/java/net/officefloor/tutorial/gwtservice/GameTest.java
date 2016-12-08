/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.tutorial.gwtservice;

import junit.framework.TestCase;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;
import net.officefloor.tutorial.gwtservice.client.HighLowGame;
import net.officefloor.tutorial.gwtservice.client.Result;

import com.gdevelop.gwt.syncrpc.SyncProxy;

/**
 * Tests the Game service.
 * 
 * @author Daniel Sagenschneider
 */
public class GameTest extends TestCase {

	@Override
	protected void tearDown() throws Exception {
		WoofOfficeFloorSource.stop();
	}

	/**
	 * Ensure GWT Service available.
	 */
	// START SNIPPET: test
	public void testCallGwtService() throws Exception {

		// Start Server
		WoofOfficeFloorSource.start();

		// Invoke the service
		HighLowGame game = (HighLowGame) SyncProxy.newProxyInstance(
				HighLowGame.class, "http://localhost:7878/game/", "highlow");
		Result result = game.attempt(new Integer(50));

		// Ensure provide result from attempt
		assertNotNull("Should be successful", result);
	}
	// END SNIPPET: test

}