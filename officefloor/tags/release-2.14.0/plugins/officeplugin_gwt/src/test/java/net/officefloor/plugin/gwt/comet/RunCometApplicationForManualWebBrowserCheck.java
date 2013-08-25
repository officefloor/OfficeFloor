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
package net.officefloor.plugin.gwt.comet;

import net.officefloor.plugin.gwt.comet.api.OfficeFloorComet;
import net.officefloor.plugin.gwt.comet.web.http.section.CometHttpTemplateSectionExtensionTest;

/**
 * Main class to run the Comet Application for manually checking
 * {@link OfficeFloorComet} functionality within a Web Browser.
 * 
 * @author Daniel Sagenschneider
 */
public class RunCometApplicationForManualWebBrowserCheck {

	/**
	 * Main method to manually test with a browser to test
	 * {@link OfficeFloorComet} interaction.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String... args) throws Exception {

		// Start the server
		CometHttpTemplateSectionExtensionTest.main(args);
	}

}