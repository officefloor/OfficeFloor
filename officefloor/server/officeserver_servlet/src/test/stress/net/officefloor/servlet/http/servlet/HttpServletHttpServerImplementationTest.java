/*-
 * #%L
 * HttpServlet adapter for OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.servlet.http.servlet;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.servlet.HttpServletHttpServerImplementation;
import net.officefloor.server.http.servlet.OfficeFloorFilter;
import net.officefloor.server.servlet.test.AbstractServletHttpServerImplementationTest;

/**
 * Tests the {@link HttpServletHttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletHttpServerImplementationTest extends AbstractServletHttpServerImplementationTest {

	/*
	 * =================== HttpServerImplementationTest =====================
	 */

	@Override
	protected void configureServer(ServerContext context) throws Exception {
		context.getHandler().addFilter(OfficeFloorFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
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
