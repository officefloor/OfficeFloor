/*-
 * #%L
 * Google AppEngine OfficeFloor Server
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

package net.officefloor.server.appengine;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.servlet.HttpServletHttpServerImplementation;
import net.officefloor.server.http.servlet.OfficeFloorFilter;
import net.officefloor.server.servlet.test.AbstractServletHttpServerImplementationTest;

/**
 * Tests the Google AppEngine integration.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleAppEngineServerImplementationTest extends AbstractServletHttpServerImplementationTest {

	/*
	 * =================== HttpServerImplementationTest =====================
	 */

	@Override
	protected void configureServer(ServerContext context) throws Exception {
		context.getHandler().addFilter(OfficeFloorFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
	}

	@Override
	protected int getRequestCount() {
		return 10000; // request threads, so avoid overload
	}

	@Override
	protected Class<? extends HttpServerImplementation> getHttpServerImplementationClass() {
		return HttpServletHttpServerImplementation.class;
	}

	@Override
	protected String getServerNameSuffix() {
		return "Jetty";
	}
}