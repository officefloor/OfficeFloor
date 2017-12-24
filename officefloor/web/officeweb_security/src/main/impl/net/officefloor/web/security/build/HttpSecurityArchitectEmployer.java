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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.security.AuthenticationRequiredException;
import net.officefloor.web.security.HttpAccess;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.impl.AccessControlManagedObjectSource;
import net.officefloor.web.security.impl.AuthenticationContextManagedObjectSource;
import net.officefloor.web.security.impl.AuthenticationManagedObjectSource;
import net.officefloor.web.security.impl.HandleAuthenticationRequiredSectionSource;
import net.officefloor.web.security.impl.HttpAccessAdministrationSource;
import net.officefloor.web.security.impl.HttpAccessControlManagedObjectSource;
import net.officefloor.web.security.impl.HttpAuthenticationManagedObjectSource;
import net.officefloor.web.security.impl.HttpSecurityConfiguration;
import net.officefloor.web.security.impl.HttpSecuritySectionSource;
import net.officefloor.web.security.type.HttpSecurityLoader;
import net.officefloor.web.security.type.HttpSecurityLoaderImpl;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Employs the {@link HttpSecurityArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityArchitectEmployer implements HttpSecurityArchitect {

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
		return new HttpSecurityArchitectEmployer(webArchitect, officeArchitect, officeSourceContext);
	}

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
	private HttpSecurityArchitectEmployer(WebArchitect webArchitect, OfficeArchitect officeArchitect,
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
	public <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityBuilder addHttpSecurity(
			String securityName, Class<? extends HttpSecuritySource<A, AC, C, O, F>> httpSecuritySourceClass) {

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
	public <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityBuilder addHttpSecurity(
			String securityName, HttpSecuritySource<A, AC, C, O, F> httpSecuritySource) {

		// Create properties to configure the HTTP Security Source
		PropertyList properties = this.officeSourceContext.createPropertyList();

		// Create the HTTP security builder
		HttpSecurityBuilderImpl<A, AC, C, O, F> security = new HttpSecurityBuilderImpl<>(securityName,
				httpSecuritySource, properties);

		// Register the HTTP security builder
		this.securities.add(security);

		// Return the HTTP security builder
		return security;
	}

	@Override
	public void informWebArchitect() {

		// Configure the HTTP security
		OfficeManagedObject httpAccessControl = null;
		String[] httpSecurityNames = new String[this.securities.size()];
		for (int i = 0; i < this.securities.size(); i++) {
			HttpSecurityBuilderImpl<?, ?, ?, ?, ?> security = this.securities.get(i);
			httpSecurityNames[i] = security.name;
			httpAccessControl = security.build();
		}

		// Group the security into handling
		List<HttpSecurityBuilderImpl<?, ?, ?, ?, ?>> httpChallengeSecurities = new LinkedList<>();
		List<HttpSecurityBuilderImpl<?, ?, ?, ?, ?>> applicationSecurities = new LinkedList<>();
		for (HttpSecurityBuilderImpl<?, ?, ?, ?, ?> security : this.securities) {

			// Determine if HTTP Challenge security
			boolean isHttpChallenge = ((security.type.getCredentialsType() == null)
					&& (security.type.getFlowTypes().length == 0));
			if (isHttpChallenge) {
				httpChallengeSecurities.add(security);
			} else {
				applicationSecurities.add(security);
			}
		}

		// Add the authentication required handling
		OfficeEscalation authenticationRequiredEscalation = this.officeArchitect
				.addOfficeEscalation(AuthenticationRequiredException.class.getName());
		OfficeSection handleAuthenticationRequiredSection = this.officeArchitect.addOfficeSection(
				"AuthenticationRequiredHandler", new HandleAuthenticationRequiredSectionSource(httpSecurityNames),
				null);
		this.officeArchitect.link(authenticationRequiredEscalation, handleAuthenticationRequiredSection
				.getOfficeSectionInput(HandleAuthenticationRequiredSectionSource.HANDLE_INPUT));
		for (HttpSecurityBuilderImpl<?, ?, ?, ?, ?> security : this.securities) {
			this.officeArchitect.link(handleAuthenticationRequiredSection.getOfficeSectionOutput(security.name),
					security.section.getOfficeSectionInput(HttpSecuritySectionSource.INPUT_CHALLENGE));
		}

		// Augment functions with HTTP access administration
		final OfficeManagedObject finalAccessControlManagedObject = httpAccessControl;
		this.officeArchitect.addManagedFunctionAugmentor((context) -> {

			// Determine if HTTP Access annotation
			ManagedFunctionType<?, ?> type = context.getManagedFunctionType();
			for (Object annotation : type.getAnnotations()) {
				if (annotation instanceof HttpAccess) {
					HttpAccess httpAccess = (HttpAccess) annotation;

					// Configure access administration
					OfficeAdministration administration = this.officeArchitect.addOfficeAdministration("HttpAccess",
							new HttpAccessAdministrationSource(httpAccess));
					context.addPreAdministration(administration);
					administration.administerManagedObject(finalAccessControlManagedObject);
				}
			}
		});
	}

	/**
	 * {@link HttpSecurityBuilder} implementation.
	 */
	private class HttpSecurityBuilderImpl<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>>
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
		private int timeout = 10 * 1000;

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
		 */
		private HttpSecurityBuilderImpl(String securityName, HttpSecuritySource<A, AC, C, O, F> securitySource,
				PropertyList properties) {
			this.name = securityName;
			this.source = securitySource;
			this.properties = properties;

			// Create the section for the HTTP Security
			this.section = HttpSecurityArchitectEmployer.this.officeArchitect
					.addOfficeSection(securityName + "_HttpSecurity", new HttpSecuritySectionSource<>(this), null);
		}

		/**
		 * Builds this {@link HttpSecurity}.
		 * 
		 * @return {@link OfficeManagedObject} for the
		 *         {@link HttpAccessControl}.
		 */
		private OfficeManagedObject build() {

			// Create the HTTP Security loader
			HttpSecurityLoader loader = new HttpSecurityLoaderImpl(HttpSecurityArchitectEmployer.this.officeArchitect,
					HttpSecurityArchitectEmployer.this.officeSourceContext, this.name);

			// Load the security type
			this.type = loader.loadHttpSecurityType(this.source, this.properties);

			// Load the security
			this.security = this.source.sourceHttpSecurity(this);

			// Obtain easy access to office architect
			OfficeArchitect office = HttpSecurityArchitectEmployer.this.officeArchitect;

			// Determine if require type qualification (as multiple securities)
			boolean isRequireTypeQualification = (HttpSecurityArchitectEmployer.this.securities.size() > 1);

			// Add the authentication context managed object
			String authenticationContextName = this.name + "_AuthenticationContext";
			OfficeManagedObjectSource authenticationContextMos = office.addOfficeManagedObjectSource(
					authenticationContextName,
					new AuthenticationContextManagedObjectSource<>(this.security, this.type));
			authenticationContextMos.setTimeout(this.timeout);
			office.link(authenticationContextMos.getManagedObjectFlow("AUTHENTICATE"),
					this.section.getOfficeSectionInput("ManagedObjectAuthenticate"));
			office.link(authenticationContextMos.getManagedObjectFlow("LOGOUT"),
					this.section.getOfficeSectionInput("ManagedObjectLogout"));
			OfficeManagedObject authenticationContext = authenticationContextMos
					.addOfficeManagedObject(authenticationContextName, ManagedObjectScope.PROCESS);
			if (isRequireTypeQualification) {
				authenticationContext.addTypeQualification(this.name, AuthenticationContext.class.getName());
			}

			// Add the authentication managed object
			String authenticationName = this.name + "_Authentication";
			OfficeManagedObjectSource authenticationMos = office.addOfficeManagedObjectSource(authenticationName,
					new AuthenticationManagedObjectSource<>(this.name, this.security, this.type));
			OfficeManagedObject authentication = authenticationMos.addOfficeManagedObject(authenticationName,
					ManagedObjectScope.PROCESS);
			office.link(authentication.getManagedObjectDependency("AUTHENTICATION_CONTEXT"), authenticationContext);

			// Add the HTTP authentication
			Class<A> authenticationType = this.type.getAuthenticationType();
			OfficeManagedObject httpAuthentication;
			if (HttpAuthentication.class.isAssignableFrom(authenticationType)) {
				httpAuthentication = authentication;
			} else {
				String httpAuthenticationName = this.name + "_HttpAuthentication";
				OfficeManagedObjectSource httpAuthenticationMos = office.addOfficeManagedObjectSource(
						httpAuthenticationName, new HttpAuthenticationManagedObjectSource<>(this.type));
				httpAuthentication = httpAuthenticationMos.addOfficeManagedObject(httpAuthenticationName,
						ManagedObjectScope.PROCESS);
				office.link(httpAuthentication.getManagedObjectDependency("AUTHENTICATION"), authentication);
			}

			// Add the access control managed object
			String accessControlName = this.name + "_AccessControl";
			Class<AC> accessControlType = this.type.getAccessControlType();
			OfficeManagedObjectSource accessControlMos = office.addOfficeManagedObjectSource(accessControlName,
					new AccessControlManagedObjectSource<>(this.name, accessControlType));
			accessControlMos.setTimeout(this.timeout);
			OfficeManagedObject accessControl = accessControlMos.addOfficeManagedObject(accessControlName,
					ManagedObjectScope.PROCESS);
			if (isRequireTypeQualification) {
				accessControl.addTypeQualification(this.name, accessControlType.getName());
			}
			office.link(accessControl.getManagedObjectDependency("AUTHENTICATION_CONTEXT"), authenticationContext);

			// Add the HTTP access control
			OfficeManagedObject httpAccessControl;
			if (HttpAccessControl.class.isAssignableFrom(accessControlType)) {
				httpAccessControl = accessControl;
			} else {
				String httpAccessControlName = this.name + "_HttpAccessControl";
				OfficeManagedObjectSource httpAccessControlMos = office.addOfficeManagedObjectSource(
						httpAccessControlName, new HttpAccessControlManagedObjectSource<>(this.type));
				httpAccessControl = httpAccessControlMos.addOfficeManagedObject(httpAccessControlName,
						ManagedObjectScope.PROCESS);
				office.link(httpAccessControl.getManagedObjectDependency("ACCESS_CONTROL"), accessControl);
				if (isRequireTypeQualification) {
					httpAccessControl.addTypeQualification(this.name, HttpAccessControl.class.getSimpleName());
				}
			}

			// Wire up the section
			office.link(this.section.getOfficeSectionObject(AuthenticationContext.class.getSimpleName()),
					authenticationContext);
			office.link(this.section.getOfficeSectionObject("AccessControl"), accessControl);

			// Provide application credentials linking
			Class<C> credentialsType = this.type.getCredentialsType();
			if (credentialsType != null) {
				HttpSecurityArchitectEmployer.this.webArchitect
						.reroute(this.section.getOfficeSectionOutput(HttpSecuritySectionSource.OUTPUT_RECONTINUE));
			}

			// Return the HTTP Access Control managed object
			return httpAccessControl;
		}

		/*
		 * ================== HttpSecurityBuiler =======================
		 */

		@Override
		public void addProperty(String name, String value) {
			this.properties.addProperty(name).setValue(value);
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
			return this.section.getOfficeSectionOutput(outputName);
		}

		/*
		 * =============== HttpSecurityConfiguration ====================
		 */

		@Override
		public HttpSecurity<A, AC, C, O, F> getHttpSecurity() {
			return this.security;
		}

		@Override
		public HttpSecurityType<A, AC, C, O, F> getHttpSecurityType() {
			return this.type;
		}
	}

}