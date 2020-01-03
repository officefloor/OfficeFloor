/*-
 * #%L
 * Logger Tutorial
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.tutorial.loggerhttpserver;

import java.util.logging.Logger;

/**
 * Logic for logging {@link LoggedRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class LogLogic {

	// START SNIPPET: tutorial
	public void log(LoggedRequest request, Logger procedureLogger, LogObject object) {

		// Log the request via procedure logger
		procedureLogger.info("PROCEDURE: " + request.getMessage());

		// Have dependency injected object use it's logger
		object.log(request);
	}
	// END SNIPPET: tutorial
}
