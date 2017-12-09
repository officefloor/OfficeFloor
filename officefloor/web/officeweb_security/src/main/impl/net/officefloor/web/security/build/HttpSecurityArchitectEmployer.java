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
package net.officefloor.web.security.build;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.impl.HttpAccessControlManagedObjectSource;
import net.officefloor.web.security.impl.HttpAuthenticationManagedObjectSource;
import net.officefloor.web.security.impl.HttpSecurityConfiguration;
import net.officefloor.web.security.impl.HttpSecuritySectionSource;
import net.officefloor.web.security.type.HttpSecurityLoader;
import net.officefloor.web.security.type.HttpSecurityLoaderImpl;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpAccessControlFactory;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Employs the {@link HttpSecurityArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityArchitectEmployer {

	/**
	 * Employs the {@link HttpSecurityArchitect}.
	 * 
	 * @param webArchitect
	 *            {@link WebArchitect}.
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param officeSourceContext
	 *            {@link OfficeSourceContext}.
	 * @return {@link HttpSecurityArchitect}.
	 */
	public static HttpSecurityArchitect employHttpSecurityArchitect(WebArchitect webArchitect,
			OfficeArchitect officeArchitect, OfficeSourceContext officeSourceContext) {
		return new HttpSecurityArchitectImpl(webArchitect, officeArchitect, officeSourceContext);
	}

	/**
	 * {@link HttpSecurityArchitect} implementation.
	 */
	private static class HttpSecurityArchitectImpl implements HttpSecurityArchitect {

		/**
		 * {@link WebArchitect}.
		 */
		private final WebArchitect webArchitect;

		/**
		 * {@link OfficeArchitect}.
		 */
		private final OfficeArchitect officeArchitect;

		/**
		 * {@link OfficeSourceContext}.
		 */
		private final OfficeSourceContext officeSourceContext;

		/**
		 * Added {@link HttpSecurityArchitectImpl} instances.
		 */
		private List<HttpSecurityBuilderImpl<?, ?, ?, ?, ?>> securities = new ArrayList<>();

		/**
		 * Instantiate.
		 * 
		 * @param webArchitect
		 *            {@link WebArchitect}.
		 * @param officeArchitect
		 *            {@link OfficeArchitect}.
		 * @param officeSourceContext
		 *            {@link OfficeSourceContext}.
		 */
		private HttpSecurityArchitectImpl(WebArchitect webArchitect, OfficeArchitect officeArchitect,
				OfficeSourceContext officeSourceContext) {
			this.webArchitect = webArchitect;
			this.officeArchitect = officeArchitect;
			this.officeSourceContext = officeSourceContext;
		}

		/*
		 * ================ HttpSecurityArchitect ======================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public <A, AC, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityBuilder addHttpSecurity(String securityName,
				Class<? extends HttpSecuritySource<A, AC, C, O, F>> httpSecuritySourceClass) {

			// Instantiate the HTTP security source
			HttpSecuritySource<A, AC, C, O, F> httpSecuritySource;
			try {
				httpSecuritySource = (HttpSecuritySource<A, AC, C, O, F>) this.officeSourceContext
						.loadClass(httpSecuritySourceClass.getName()).newInstance();
			} catch (IllegalAccessException | InstantiationException ex) {
				// Must be able to instantiate instance
				throw new LoadTypeError(HttpSecuritySource.class, httpSecuritySourceClass.getName(), null);
			}

			// Add and return the HTTP Security
			return this.addHttpSecurity(securityName, httpSecuritySource);
		}

		@Override
		public <A, AC, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityBuilder addHttpSecurity(String securityName,
				HttpSecuritySource<A, AC, C, O, F> httpSecuritySource) {

			// Create properties to configure the HTTP Security Source
			PropertyList properties = this.officeSourceContext.createPropertyList();

			// Create the HTTP security builder
			HttpSecurityBuilderImpl<A, AC, C, O, F> security = new HttpSecurityBuilderImpl<>(securityName,
					httpSecuritySource, properties, this.officeArchitect);

			// Register the HTTP security builder
			this.securities.add(security);

			// Return the HTTP security builder
			return security;
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void informWebArchitect() {

			// Configure the HTTP security
			for (HttpSecurityBuilderImpl<?, ?, ?, ?, ?> security : this.securities) {

				// Create the HTTP Security loader
				HttpSecurityLoader loader = new HttpSecurityLoaderImpl(this.officeSourceContext, security.name);

				// Load the security type
				security.type = (HttpSecurityType) loader.loadHttpSecurityType(security.source, security.properties);

				// Load the security
				security.security = (HttpSecurity) security.source.sourceHttpSecurity(security);

				// Add the HTTP Authentication Managed Object
				String authenticationName = security.name + "_Authentication";
				OfficeManagedObjectSource httpAuthenticationMos = this.officeArchitect.addOfficeManagedObjectSource(
						authenticationName, new HttpAuthenticationManagedObjectSource(security.security, null));
				httpAuthenticationMos.setTimeout(security.timeout);
				this.officeArchitect.link(httpAuthenticationMos.getManagedObjectFlow("AUTHENTICATE"),
						security.section.getOfficeSectionInput("ManagedObjectAuthenticate"));
				this.officeArchitect.link(httpAuthenticationMos.getManagedObjectFlow("LOGOUT"),
						security.section.getOfficeSectionInput("ManagedObjectLogout"));
				httpAuthenticationMos.addOfficeManagedObject(authenticationName, ManagedObjectScope.PROCESS);

				// Add the HTTP access control Managed Object
				String accessControlName = security.name + "_AccessControl";
				OfficeManagedObjectSource httpSecurityMos = this.officeArchitect
						.addOfficeManagedObjectSource(accessControlName, new HttpAccessControlManagedObjectSource());
				httpSecurityMos.setTimeout(security.timeout);
				httpSecurityMos.addProperty(HttpAccessControlManagedObjectSource.PROPERTY_ACCESS_CONTROL_TYPE,
						HttpAccessControl.class.getName());
				httpSecurityMos.addOfficeManagedObject(accessControlName, ManagedObjectScope.PROCESS);
			}
		}
	}

	/**
	 * {@link HttpSecurityBuilder} implementation.
	 */
	private static class HttpSecurityBuilderImpl<A, AC, C, O extends Enum<O>, F extends Enum<F>>
			implements HttpSecurityBuilder, HttpSecurityContext, HttpSecurityConfiguration<A, AC, C, O, F> {

		/**
		 * Name of the {@link HttpSecurity}.
		 */
		private final String name;

		/**
		 * {@link HttpSecuritySource}.
		 */
		private final HttpSecuritySource<A, AC, C, O, F> source;

		/**
		 * {@link PropertyList}.
		 */
		private final PropertyList properties;

		/**
		 * {@link OfficeSection}.
		 */
		private final OfficeSection section;

		/**
		 * Timeout.
		 */
		private int timeout = -1;

		/**
		 * {@link HttpSecurityType}.
		 */
		private HttpSecurityType<A, AC, C, O, F> type = null;

		/**
		 * {@link HttpSecurity}.
		 */
		private HttpSecurity<A, AC, C, O, F> security = null;

		/**
		 * Instantiate.
		 * 
		 * @param securityName
		 *            {@link HttpSecurity} name.
		 * @param properties
		 *            {@link PropertyList}.
		 * @param officeArchitect
		 *            {@link OfficeArchitect}.
		 */
		private HttpSecurityBuilderImpl(String securityName, HttpSecuritySource<A, AC, C, O, F> securitySource,
				PropertyList properties, OfficeArchitect officeArchitect) {
			this.name = securityName;
			this.source = securitySource;
			this.properties = properties;

			// Create the section for the HTTP Security
			this.section = officeArchitect.addOfficeSection(securityName, new HttpSecuritySectionSource(this), null);
		}

		/*
		 * ================== HttpSecurityBuiler =======================
		 */

		@Override
		public void addProperty(String name, String value) {
			// TODO Auto-generated method stub
		}

		@Override
		public void addContentType(String contentType) {
			// TODO Auto-generated method stub
		}

		@Override
		public OfficeSectionInput getInput(String inputName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public OfficeSectionOutput getOutput(String outputName) {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * =============== HttpSecurityConfiguration ====================
		 */

		@Override
		public HttpSecurity<A, AC, C, O, F> getHttpSecurity() {
			return this.security;
		}

		@Override
		public HttpAccessControlFactory<AC> getAccessControlFactory() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HttpSecurityType<A, AC, C, O, F> getHttpSecurityType() {
			return this.type;
		}
	}

}