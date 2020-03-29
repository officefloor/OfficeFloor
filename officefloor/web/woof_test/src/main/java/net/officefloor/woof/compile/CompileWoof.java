/*-
 * #%L
 * Web on OfficeFloor Testing
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

package net.officefloor.woof.compile;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.officefloor.CompileOfficeExtension;
import net.officefloor.compile.test.officefloor.CompileOfficeFloorExtension;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.compile.CompileWebContextImpl;
import net.officefloor.web.compile.CompileWebExtension;
import net.officefloor.woof.WoOF;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Enables compiling {@link MockWoofServer} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileWoof {

	/**
	 * {@link CompileOfficeFloorExtension} instances.
	 */
	private final List<CompileOfficeFloorExtension> officeFloorExtensions = new LinkedList<>();

	/**
	 * {@link CompileOfficeExtension} instances.
	 */
	private final List<CompileOfficeExtension> officeExtensions = new LinkedList<>();

	/**
	 * {@link CompileWebExtension} instances.
	 */
	private final List<CompileWebExtension> webExtensions = new LinkedList<>();

	/**
	 * {@link CompileWoofExtension} instances.
	 */
	private final List<CompileWoofExtension> woofExtensions = new LinkedList<>();

	/**
	 * Indicates whether to load.
	 */
	private final boolean isLoad;

	/**
	 * Default constructor to not load.
	 */
	public CompileWoof() {
		this(false);
	}

	/**
	 * Instantiate indicating whether to load {@link WoOF}.
	 * 
	 * @param isLoad <code>true</code> to load.
	 */
	public CompileWoof(boolean isLoad) {
		this.isLoad = isLoad;
	}

	/**
	 * Adds a {@link CompileOfficeFloorExtension}.
	 * 
	 * @param extension {@link CompileOfficeFloorExtension}.
	 */
	public void officeFloor(CompileOfficeFloorExtension extension) {
		this.officeFloorExtensions.add(extension);
	}

	/**
	 * Adds a {@link CompileOfficeExtension}.
	 * 
	 * @param extension {@link CompileOfficeExtension}.
	 */
	public void office(CompileOfficeExtension extension) {
		this.officeExtensions.add(extension);
	}

	/**
	 * Adds a {@link CompileWebExtension}.
	 * 
	 * @param extension {@link CompileWebExtension}.
	 */
	public void web(CompileWebExtension extension) {
		this.webExtensions.add(extension);
	}

	/**
	 * Adds a {@link CompileWoofExtension}.
	 * 
	 * @param extension {@link CompileWoofExtension}.
	 */
	public void woof(CompileWoofExtension extension) {
		this.woofExtensions.add(extension);
	}

	/**
	 * Opens the {@link MockWoofServer}.
	 * 
	 * @return {@link MockWoofServer}.
	 * @throws Exception If fails to open {@link MockWoofServer}.
	 */
	public MockWoofServer open(String... propertyNameValuePairs) throws Exception {
		return MockWoofServer.open((context, compiler) -> {

			// Determine if load
			if (!this.isLoad) {
				context.notLoad();
			}

			// Do not load external configuration
			context.notLoadExternal();

			// Load the properties
			for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
				String name = propertyNameValuePairs[i];
				String value = propertyNameValuePairs[i + 1];
				compiler.getOfficeFloorCompiler().addProperty(name, value);
			}

			// Load the configurations
			for (CompileOfficeFloorExtension extension : this.officeFloorExtensions) {
				compiler.officeFloor(extension);
			}
			for (CompileOfficeExtension extension : this.officeExtensions) {
				compiler.office(extension);
			}

			// Capture the compile context for loading web extensions
			CompileOfficeContext[] compileOfficeContext = new CompileOfficeContext[1];
			compiler.office(officeContext -> compileOfficeContext[0] = officeContext);

			// Load the web extension
			for (CompileWebExtension extension : this.webExtensions) {
				context.extend((web) -> {
					WebArchitect webArchitect = web.getWebArchitect();
					CompileWebContext webContext = new CompileWebContextImpl(compileOfficeContext[0], webArchitect);
					extension.extend(webContext);
				});
			}

			// Load the WoOF extension
			for (CompileWoofExtension extension : this.woofExtensions) {
				context.extend((extendContext) -> {
					extension.extend(extendContext);
				});
			}
		});
	}

}
