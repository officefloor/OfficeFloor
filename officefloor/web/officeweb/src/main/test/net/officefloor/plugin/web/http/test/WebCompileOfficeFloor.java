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
package net.officefloor.plugin.web.http.test;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.HttpInputBuilder;
import net.officefloor.web.HttpUrlContinuation;
import net.officefloor.web.WebArchitect;
import net.officefloor.web.WebArchitectEmployer;

/**
 * Provides {@link WebArchitect} and server configuration for testing web
 * applications.
 * 
 * @author Daniel Sagenschneider
 */
public class WebCompileOfficeFloor extends CompileOfficeFloor {

	/**
	 * Adds a {@link CompileWebExtension}.
	 * 
	 * @param extension
	 *            {@link CompileWebExtension}.
	 */
	public void web(CompileWebExtension extension) {
		// Wrap web extension into office extension
		this.office((context) -> {
			CompileWebContextImpl web = new CompileWebContextImpl(context);
			if (extension != null) {
				// Allow no configuration except default web
				extension.extend(web);
			}
			web.webArchitect.informOfficeArchitect();
		});
	}

	/**
	 * {@link CompileWebContext} implementation.
	 */
	private static class CompileWebContextImpl implements CompileWebContext {

		/**
		 * {@link CompileOfficeContext}.
		 */
		private final CompileOfficeContext officeContext;

		/**
		 * {@link WebArchitect}.
		 */
		private final WebArchitect webArchitect;

		/**
		 * Instantiate.
		 * 
		 * @param officeContext
		 *            {@link CompileOfficeContext}.
		 */
		public CompileWebContextImpl(CompileOfficeContext officeContext) {
			this.officeContext = officeContext;

			// Always employ the web architect
			this.webArchitect = WebArchitectEmployer.employWebArchitect(this.officeContext.getOfficeArchitect(),
					this.officeContext.getOfficeSourceContext());
		}

		/*
		 * ================== CompileWebContext ==================
		 */

		@Override
		public WebArchitect getWebArchitect() {
			return this.webArchitect;
		}

		/*
		 * ================== CompileOfficeContext ==================
		 */

		@Override
		public OfficeArchitect getOfficeArchitect() {
			return this.officeContext.getOfficeArchitect();
		}

		@Override
		public OfficeSourceContext getOfficeSourceContext() {
			return this.officeContext.getOfficeSourceContext();
		}

		@Override
		public OfficeManagedObject addManagedObject(String managedObjectName, Class<?> managedObjectClass,
				ManagedObjectScope scope) {
			return this.officeContext.addManagedObject(managedObjectName, managedObjectClass, scope);
		}

		@Override
		public OfficeSection addSection(String sectionName, Class<?> sectionClass) {
			return this.officeContext.addSection(sectionName, sectionClass);
		}

		@Override
		public OfficeSection getOfficeSection() {
			return this.officeContext.getOfficeSection();
		}

		@Override
		public OfficeSection overrideSection(Class<? extends SectionSource> sectionSourceClass,
				String sectionLocation) {
			return this.officeContext.overrideSection(sectionSourceClass, sectionLocation);
		}

		@Override
		public HttpInputBuilder link(boolean isSecure, HttpMethod httpMethod, String applicationPath, Class<?> sectionClass) {

			// Add the section
			OfficeSection section = this.addSection(httpMethod.getName() + "_" + applicationPath, sectionClass);

			// Create the link to the section service method
			return this.webArchitect.link(isSecure, httpMethod, applicationPath,
					section.getOfficeSectionInput("service"));
		}

		@Override
		public HttpUrlContinuation link(boolean isSecure, String applicationPath, Class<?> sectionClass) {

			// Add the section
			OfficeSection section = this.addSection("GET_" + applicationPath, sectionClass);

			// Return the link to the section service method
			return this.webArchitect.link(isSecure, applicationPath, section.getOfficeSectionInput("service"));
		}
	}

}