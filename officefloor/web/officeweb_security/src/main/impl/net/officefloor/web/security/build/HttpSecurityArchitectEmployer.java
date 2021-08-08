/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.ExecutionManagedObject;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectDependency;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.web.accept.AcceptNegotiator;
import net.officefloor.web.build.AcceptNegotiatorBuilder;
import net.officefloor.web.build.NoAcceptHandlersException;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.security.AuthenticationRequiredException;
import net.officefloor.web.security.HttpAccess;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.build.office.HttpOfficeSecurer;
import net.officefloor.web.security.build.office.HttpOfficeSecurerContext;
import net.officefloor.web.security.build.section.HttpFlowSecurer;
import net.officefloor.web.security.impl.AccessControlManagedObjectSource;
import net.officefloor.web.security.impl.AuthenticationContextManagedObjectSource;
import net.officefloor.web.security.impl.AuthenticationManagedObjectSource;
import net.officefloor.web.security.impl.DefaultHttpAccessControlManagedObjectSource;
import net.officefloor.web.security.impl.DefaultHttpAccessControlManagedObjectSource.Dependencies;
import net.officefloor.web.security.impl.DefaultHttpAuthenticationManagedObjectSource;
import net.officefloor.web.security.impl.HandleAuthenticationRequiredSectionSource;
import net.officefloor.web.security.impl.HttpAccessAdministrationSource;
import net.officefloor.web.security.impl.HttpAccessControlManagedObjectSource;
import net.officefloor.web.security.impl.HttpAuthenticationManagedObjectSource;
import net.officefloor.web.security.impl.HttpChallengeContextManagedObjectSource;
import net.officefloor.web.security.impl.HttpSecurityConfiguration;
import net.officefloor.web.security.impl.HttpSecurityExecuteManagedObjectSource;
import net.officefloor.web.security.impl.HttpSecuritySectionSource;
import net.officefloor.web.security.scheme.AnonymousHttpSecuritySource;
import net.officefloor.web.security.section.HttpFlowSecurerManagedFunction;
import net.officefloor.web.security.section.HttpFlowSecurerManagedFunction.Flows;
import net.officefloor.web.security.type.HttpSecurityFlowType;
import net.officefloor.web.security.type.HttpSecurityLoader;
import net.officefloor.web.security.type.HttpSecurityLoaderImpl;
import net.officefloor.web.security.type.HttpSecuritySupportingManagedObjectDependencyContext;
import net.officefloor.web.security.type.HttpSecuritySupportingManagedObjectDependencyType;
import net.officefloor.web.security.type.HttpSecuritySupportingManagedObjectType;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.HttpChallengeContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;

/**
 * Employs the {@link HttpSecurityArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityArchitectEmployer implements HttpSecurityArchitect {

	/**
	 * Employs the {@link HttpSecurityArchitect}.
	 * 
	 * @param webArchitect        {@link WebArchitect}.
	 * @param officeArchitect     {@link OfficeArchitect}.
	 * @param officeSourceContext {@link OfficeSourceContext}.
	 * @return {@link HttpSecurityArchitect}.
	 */
	public static HttpSecurityArchitect employHttpSecurityArchitect(WebArchitect webArchitect,
			OfficeArchitect officeArchitect, OfficeSourceContext officeSourceContext) {
		return new HttpSecurityArchitectEmployer(webArchitect, officeArchitect, officeSourceContext);
	}

	/**
	 * Employs the {@link HttpSecurityLoader}.
	 * 
	 * @param compiler {@link OfficeFloorCompiler}.
	 * @return {@link HttpSecurityLoader}.
	 * @throws Exception If fails to load the {@link HttpSecurityLoader}.
	 */
	public static HttpSecurityLoader employHttpSecurityLoader(OfficeFloorCompiler compiler) throws Exception {
		return compiler.run(HttpSecurityLoaderImpl.class);
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
	 * {@link HttpSecurerBuilderImpl} instances.
	 */
	private final List<HttpSecurerBuilderImpl> securers = new LinkedList<>();

	/**
	 * {@link HttpSecurityExplorer} instances.
	 */
	private final List<HttpSecurityExplorer> explorers = new LinkedList<>();

	/**
	 * Index of the next section to ensure unique names.
	 */
	private int nextSectionIndex = 1;

	/**
	 * Instantiate.
	 * 
	 * @param webArchitect        {@link WebArchitect}.
	 * @param officeArchitect     {@link OfficeArchitect}.
	 * @param officeSourceContext {@link OfficeSourceContext}.
	 */
	private HttpSecurityArchitectEmployer(WebArchitect webArchitect, OfficeArchitect officeArchitect,
			OfficeSourceContext officeSourceContext) {
		this.webArchitect = webArchitect;
		this.officeArchitect = officeArchitect;
		this.officeSourceContext = officeSourceContext;
	}

	/**
	 * Obtains the name of the next section.
	 * 
	 * @return Name of the next section.
	 */
	private String nextUniqueName() {
		return "_secure_" + (this.nextSectionIndex++);
	}

	/*
	 * ================ HttpSecurityArchitect ======================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityBuilder addHttpSecurity(
			String securityName, String httpSecuritySourceClassName) {

		// Obtain the class
		Class<?> httpSecuritySourceClass = this.officeSourceContext.loadClass(httpSecuritySourceClassName);

		// Instantiate the HTTP security source
		HttpSecuritySource<A, AC, C, O, F> httpSecuritySource;
		try {
			httpSecuritySource = (HttpSecuritySource<A, AC, C, O, F>) this.officeSourceContext
					.loadClass(httpSecuritySourceClass.getName()).getDeclaredConstructor().newInstance();
		} catch (Exception ex) {
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
	public HttpSecurer createHttpSecurer(HttpSecurable securable) {
		HttpSecurerBuilderImpl builder = new HttpSecurerBuilderImpl(securable, null);
		HttpSecurityArchitectEmployer.this.securers.add(builder);
		return builder;
	}

	@Override
	public void addHttpSecurityExplorer(HttpSecurityExplorer explorer) {
		this.explorers.add(explorer);
	}

	@Override
	public void informWebArchitect() {

		// Provide anonymous security (if no security configured)
		if (this.securities.size() == 0) {
			this.addHttpSecurity("anonymous", new AnonymousHttpSecuritySource());
		}

		// Configure the HTTP challenge context
		OfficeManagedObjectSource httpChallengeContextMos = this.officeArchitect.addOfficeManagedObjectSource(
				HttpChallengeContext.class.getSimpleName(), new HttpChallengeContextManagedObjectSource());
		OfficeManagedObject httpChallengeContext = httpChallengeContextMos
				.addOfficeManagedObject(HttpChallengeContext.class.getSimpleName(), ManagedObjectScope.PROCESS);

		// Configure the HTTP security
		String[] httpSecurityNames = new String[this.securities.size()];
		Map<String, HttpSecurityBuilderImpl<?, ?, ?, ?, ?>> nameToHttpSecurity = new HashMap<>();
		Map<String, Integer> httpSecurityNameToFlowIndex = new HashMap<>();
		long maxTimeout = -1;
		for (int i = 0; i < this.securities.size(); i++) {
			HttpSecurityBuilderImpl<?, ?, ?, ?, ?> security = this.securities.get(i);
			httpSecurityNames[i] = security.name;
			nameToHttpSecurity.put(security.name, security);
			httpSecurityNameToFlowIndex.put(security.name, i);
			if (security.timeout > maxTimeout) {
				maxTimeout = security.timeout;
			}
			security.build(httpChallengeContext);
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

		// Create the negotiator
		AcceptNegotiatorBuilder<int[]> negotiatorBuilder = this.webArchitect.createAcceptNegotiator();
		for (HttpSecurityBuilderImpl<?, ?, ?, ?, ?> security : applicationSecurities) {
			int flowIndex = httpSecurityNameToFlowIndex.get(security.name);
			if (security.contentTypes.size() == 0) {
				// No configured content types, so use default all
				negotiatorBuilder.addHandler("*/*", new int[] { flowIndex });
			} else {
				// Set up for configured content types
				for (String contentType : security.contentTypes) {
					negotiatorBuilder.addHandler(contentType, new int[] { flowIndex });
				}
			}
		}
		Map<String, List<Integer>> contentTypesToChallengeFlowIndexes = new HashMap<>();
		for (HttpSecurityBuilderImpl<?, ?, ?, ?, ?> security : httpChallengeSecurities) {
			Consumer<String> loader = (contentType) -> {
				int flowIndex = httpSecurityNameToFlowIndex.get(security.name);
				List<Integer> flowIndexes = contentTypesToChallengeFlowIndexes.get(contentType);
				if (flowIndexes == null) {
					flowIndexes = new LinkedList<>();
					contentTypesToChallengeFlowIndexes.put(contentType, flowIndexes);
				}
				flowIndexes.add(flowIndex);
			};
			if (security.contentTypes.size() == 0) {
				// No configured content types, so use default all
				loader.accept("*/*");
			} else {
				// Load for configured content types
				for (String contentType : security.contentTypes) {
					loader.accept(contentType);
				}
			}
		}
		for (String contentType : contentTypesToChallengeFlowIndexes.keySet()) {
			List<Integer> flowIndexes = contentTypesToChallengeFlowIndexes.get(contentType);
			int[] indexes = flowIndexes.stream().mapToInt((index) -> index).toArray();
			negotiatorBuilder.addHandler(contentType, indexes);
		}
		AcceptNegotiator<int[]> negotiator;
		try {
			negotiator = negotiatorBuilder.build();
		} catch (NoAcceptHandlersException ex) {
			throw this.officeArchitect
					.addIssue("Failed to create " + HttpSecurity.class.getSimpleName() + " negotiator", ex);
		}

		// Add the authentication required handling
		OfficeEscalation authenticationRequiredEscalation = this.officeArchitect
				.addOfficeEscalation(AuthenticationRequiredException.class.getName());
		OfficeSection handleAuthenticationRequiredSection = this.officeArchitect.addOfficeSection(
				"AuthenticationRequiredHandler",
				new HandleAuthenticationRequiredSectionSource(httpSecurityNames, negotiator), null);
		this.officeArchitect.link(
				handleAuthenticationRequiredSection.getOfficeSectionObject(HttpChallengeContext.class.getSimpleName()),
				httpChallengeContext);
		this.officeArchitect.link(authenticationRequiredEscalation, handleAuthenticationRequiredSection
				.getOfficeSectionInput(HandleAuthenticationRequiredSectionSource.HANDLE_INPUT));
		for (HttpSecurityBuilderImpl<?, ?, ?, ?, ?> security : this.securities) {
			this.officeArchitect.link(handleAuthenticationRequiredSection.getOfficeSectionOutput(security.name),
					security.section.getOfficeSectionInput(HttpSecuritySectionSource.INPUT_CHALLENGE));
		}

		// Obtain the default HTTP authentication and HTTP access control
		OfficeManagedObject defaultHttpAuthentication;
		OfficeManagedObject defaultHttpAccessControl;
		if (this.securities.size() == 1) {
			// Only the one security
			HttpSecurityBuilderImpl<?, ?, ?, ?, ?> security = this.securities.get(0);
			defaultHttpAuthentication = security.httpAuthentication;
			defaultHttpAccessControl = security.httpAccessControl;
		} else {
			// Load the default HTTP authentication
			String defaultHttpAuthenticationName = "Default" + HttpAuthentication.class.getSimpleName();
			OfficeManagedObjectSource defaultHttpAuthenticationMos = this.officeArchitect.addOfficeManagedObjectSource(
					defaultHttpAuthenticationName,
					new DefaultHttpAuthenticationManagedObjectSource(negotiator, httpSecurityNames));
			defaultHttpAuthentication = defaultHttpAuthenticationMos
					.addOfficeManagedObject(defaultHttpAuthenticationName, ManagedObjectScope.PROCESS);
			for (HttpSecurityBuilderImpl<?, ?, ?, ?, ?> security : this.securities) {
				this.officeArchitect.link(defaultHttpAuthentication.getOfficeManagedObjectDependency(security.name),
						security.httpAuthentication);
			}

			// Load the default HTTP access control
			String defaultHttpAccessControlName = "Default" + HttpAccessControl.class.getSimpleName();
			OfficeManagedObjectSource defaultHttpAccessControlMos = this.officeArchitect.addOfficeManagedObjectSource(
					defaultHttpAccessControlName, new DefaultHttpAccessControlManagedObjectSource());
			defaultHttpAccessControlMos.setTimeout(maxTimeout);
			defaultHttpAccessControl = defaultHttpAccessControlMos.addOfficeManagedObject(defaultHttpAccessControlName,
					ManagedObjectScope.PROCESS);
			this.officeArchitect.link(
					defaultHttpAccessControl.getOfficeManagedObjectDependency(Dependencies.HTTP_AUTHENTICATION.name()),
					defaultHttpAuthentication);
		}

		// Obtain the default security
		final OfficeManagedObject finalAuthenticationManagedObject = defaultHttpAuthentication;
		final OfficeManagedObject finalAccessControlManagedObject = defaultHttpAccessControl;
		final Map<HttpAccessKey, OfficeAdministration> httpAccessAdministrators = new HashMap<>();

		// Secure remaining aspects of application
		for (HttpSecurerBuilderImpl securer : this.securers) {
			for (HttpOfficeSecurer httpOfficeSecurer : securer.httpOfficeSecurers) {
				this.secure(securer.getHttpSecurityName(), securer.getAnyRoles(), securer.getRequiredRoles(),
						finalAccessControlManagedObject, httpAccessAdministrators, nameToHttpSecurity,
						httpOfficeSecurer);
			}
		}

		// Augment functions with HTTP access administration
		this.officeArchitect.addManagedFunctionAugmentor((context) -> {

			// Determine if HTTP Access annotation
			ManagedFunctionType<?, ?> type = context.getManagedFunctionType();
			for (Object annotation : type.getAnnotations()) {
				if (annotation instanceof HttpAccess) {
					HttpAccess httpAccess = (HttpAccess) annotation;

					// Secure access to function
					this.secure(httpAccess.withHttpSecurity(), httpAccess.ifRole(), httpAccess.ifAllRoles(),
							finalAccessControlManagedObject, httpAccessAdministrators, nameToHttpSecurity,
							(securerContext) -> context.addPreAdministration(securerContext.getAdministration()));

				} else if (annotation instanceof HttpFlowSecurerAnnotation) {
					HttpFlowSecurerAnnotation httpFlowSecurer = (HttpFlowSecurerAnnotation) annotation;

					// Obtain the HTTP authentication
					OfficeManagedObject httpAuthenticationManagedObject;
					if (httpFlowSecurer.qualifier == null) {
						// Use default HTTP security
						httpAuthenticationManagedObject = finalAuthenticationManagedObject;
					} else {
						// Obtain based on qualifier
						HttpSecurityBuilderImpl<?, ?, ?, ?, ?> security = nameToHttpSecurity
								.get(httpFlowSecurer.qualifier);
						if (security == null) {
							throw context.addIssue("No " + HttpSecurity.class.getSimpleName() + " configured by name '"
									+ httpFlowSecurer.qualifier + "'");
						}
						httpAuthenticationManagedObject = security.httpAuthentication;
					}

					// Link authentication and pass through argument to function
					context.link(context.getFunctionObject("0"), httpAuthenticationManagedObject);
					if (httpFlowSecurer.argumentType != null) {
						context.getFunctionObject("1").flagAsParameter();
					}
				}
			}
		});
	}

	/**
	 * Undertakes securing.
	 * 
	 * @param qualifier                      {@link HttpSecurity} qualifier. May be
	 *                                       <code>null</code>.
	 * @param anyRoles                       Any roles.
	 * @param allRoles                       All roles.
	 * @param httpAccessControlManagedObject {@link HttpAccessControl}
	 *                                       {@link OfficeManagedObject}.
	 * @param administrators                 Already created
	 *                                       {@link OfficeAdministration}.
	 * @param nameToHttpSecurity             {@link HttpSecurerBuilderImpl}
	 *                                       instances by their name.
	 * @param securer                        {@link HttpOfficeSecurer}.
	 */
	private void secure(String qualifier, String[] anyRoles, String[] allRoles,
			OfficeManagedObject httpAccessControlManagedObject, Map<HttpAccessKey, OfficeAdministration> administrators,
			Map<String, HttpSecurityBuilderImpl<?, ?, ?, ?, ?>> nameToHttpSecurity, HttpOfficeSecurer securer) {

		// Obtain the administration
		HttpAccessKey key = new HttpAccessKey(anyRoles, allRoles, qualifier);
		OfficeAdministration administration = administrators.get(key);
		if (administration == null) {

			// Create and register the administration
			administration = this.officeArchitect.addOfficeAdministration(key.getName(),
					new HttpAccessAdministrationSource(anyRoles, allRoles));
			administrators.put(key, administration);

			// Obtain the HTTP access control
			String httpSecurityName = qualifier == null ? "" : qualifier;
			OfficeManagedObject httpAccessControl;
			if ("".equals(httpSecurityName)) {
				// Use default HTTP access control
				httpAccessControl = httpAccessControlManagedObject;
			} else {
				// Use the qualified HTTP access control
				HttpSecurityBuilderImpl<?, ?, ?, ?, ?> security = nameToHttpSecurity.get(httpSecurityName);
				if (security == null) {
					throw this.officeArchitect.addIssue("No " + HttpSecurity.class.getSimpleName()
							+ " configured for qualifier '" + httpSecurityName + "'");
				}
				httpAccessControl = security.httpAccessControl;
			}

			// Provide the HTTP access control
			administration.administerManagedObject(httpAccessControl);
		}

		// Run securer
		final OfficeAdministration finalAdministration = administration;
		securer.secure(new HttpOfficeSecurerContext() {
			@Override
			public OfficeAdministration getAdministration() {
				return finalAdministration;
			}

			@Override
			public OfficeFlowSinkNode secureFlow(Class<?> argumentType, OfficeFlowSinkNode secureFlowSink,
					OfficeFlowSinkNode insecureFlowSink) {

				// Obtain the office architect
				OfficeArchitect office = HttpSecurityArchitectEmployer.this.officeArchitect;

				// Create the section to handle access logic
				String sectionName = HttpSecurityArchitectEmployer.this.nextUniqueName();
				OfficeSection section = office.addOfficeSection(sectionName, new HttpFlowSecurerSectionSource(
						new HttpFlowSecurerAnnotation(qualifier, anyRoles, allRoles, argumentType)), null);

				// Link
				office.link(section.getOfficeSectionOutput(HttpFlowSecurerSectionSource.SECURE_OUTPUT_NAME),
						secureFlowSink);
				office.link(section.getOfficeSectionOutput(HttpFlowSecurerSectionSource.INSECURE_OUTPUT_NAME),
						insecureFlowSink);

				// Return the sink to secure flow decision
				return section.getOfficeSectionInput(HttpFlowSecurerSectionSource.INPUT_NAME);
			}
		});
	}

	/**
	 * Key to {@link HttpAccess} to determine uniqueness.
	 */
	private static class HttpAccessKey {

		/**
		 * Any roles.
		 */
		private final String[] anyRoles;

		/**
		 * All roles.
		 */
		private final String[] allRoles;

		/**
		 * Qualifier.
		 */
		private final String qualifier;

		/**
		 * Instantiate.
		 * 
		 * @param anyRoles  Any roles.
		 * @param allRoles  All roles.
		 * @param qualifier Qualifier. May be <code>null</code>.
		 */
		private HttpAccessKey(String[] anyRoles, String[] allRoles, String qualifier) {
			this.anyRoles = anyRoles;
			this.allRoles = allRoles;
			this.qualifier = qualifier == null ? "" : qualifier;
		}

		/**
		 * Obtains the name for the {@link OfficeAdministration}.
		 * 
		 * @return Name for the {@link OfficeAdministration}.
		 */
		private String getName() {
			StringBuilder name = new StringBuilder();
			name.append(HttpAccess.class.getSimpleName());
			if (!("".equals(this.qualifier))) {
				name.append("-" + qualifier);
			}
			boolean isFirst = true;
			for (String role : this.anyRoles) {
				if (isFirst) {
					name.append("-");
					isFirst = false;
				} else {
					name.append("|");
				}
				name.append(role);
			}
			isFirst = true;
			for (String role : this.allRoles) {
				if (isFirst) {
					name.append("-");
					isFirst = false;
				} else {
					name.append("&");
				}
				name.append(role);
			}
			return name.toString();
		}

		/*
		 * =============== Object =====================
		 */

		@Override
		public int hashCode() {
			int hash = this.qualifier.hashCode();
			for (String role : this.anyRoles) {
				hash += role.hashCode();
			}
			for (String role : this.allRoles) {
				hash += role.hashCode();
			}
			return hash;
		}

		@Override
		public boolean equals(Object obj) {

			// Ensure key
			if (!(obj instanceof HttpAccessKey)) {
				return false;
			}
			HttpAccessKey that = (HttpAccessKey) obj;

			// Ensure match on qualifier
			if (!(this.qualifier.equals(that.qualifier))) {
				return false;
			}

			// Ensure match on roles
			String[] thisRoles = this.anyRoles;
			String[] thatRoles = that.anyRoles;
			if (thisRoles.length != thatRoles.length) {
				return false;
			}
			NEXT_ROLE: for (String thisRole : thisRoles) {
				for (String thatRole : thatRoles) {
					if (thisRole.equals(thatRole)) {
						continue NEXT_ROLE;
					}
				}
				return false; // did not match on role
			}

			// Ensure match on all roles
			String[] thisAllRoles = this.allRoles;
			String[] thatAllRoles = that.allRoles;
			if (thisAllRoles.length != thatAllRoles.length) {
				return false;
			}
			NEXT_ALL_ROLE: for (String thisAllRole : thisAllRoles) {
				for (String thatAllRole : thatAllRoles) {
					if (thisAllRole.equals(thatAllRole)) {
						continue NEXT_ALL_ROLE;
					}
				}
				return false; // did not match on role
			}

			// As here, match
			return true;
		}
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
		private long timeout = 10 * 1000;

		/**
		 * <code>Content-Type</code> values.
		 */
		private List<String> contentTypes = new LinkedList<>();

		/**
		 * {@link Flow} key {@link Enum} {@link Class}.
		 */
		private Class<F> flowKeys;

		/**
		 * {@link HttpSecurityType}.
		 */
		private HttpSecurityType<A, AC, C, O, F> type;

		/**
		 * {@link HttpSecurity}.
		 */
		private HttpSecurity<A, AC, C, O, F> security;

		/**
		 * {@link OfficeManagedObject} for the {@link HttpAuthentication}.
		 */
		private OfficeManagedObject httpAuthentication;

		/**
		 * {@link OfficeManagedObject} for the {@link HttpAccessControl}.
		 */
		private OfficeManagedObject httpAccessControl;

		/**
		 * Instantiate.
		 * 
		 * @param securityName {@link HttpSecurity} name.
		 * @param properties   {@link PropertyList}.
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
		 * @param httpChallengeContext {@link HttpChallengeContext}.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void build(OfficeManagedObject httpChallengeContext) {

			// Easy access to employer and this
			final HttpSecurityArchitectEmployer employer = HttpSecurityArchitectEmployer.this;
			final HttpSecurityBuilderImpl<A, AC, C, O, F> securityBuilder = this;

			// Create the HTTP Security loader
			HttpSecurityLoader loader = new HttpSecurityLoaderImpl(employer.officeArchitect,
					employer.officeSourceContext, this.name);

			// Load the security type
			this.type = loader.loadHttpSecurityType(this.source, this.properties);

			// Load the flow keys
			HttpSecurityFlowType<?>[] flowTypes = this.type.getFlowTypes();
			if (flowTypes.length > 0) {
				// Determine from key
				Enum<?> flowKey = flowTypes[0].getKey();
				this.flowKeys = (Class) ((flowKey != null) ? flowKey.getClass() : Indexed.class);
			} else {
				// No flows
				this.flowKeys = (Class) None.class;

			}

			// Load the security
			this.security = this.source.sourceHttpSecurity(this);

			// Obtain easy access to office architect
			OfficeArchitect office = employer.officeArchitect;

			// Determine if require type qualification (as multiple securities)
			boolean isRequireTypeQualification = (employer.securities.size() > 1);

			// Add the authentication context managed object
			String authenticationContextName = this.name + "_AuthenticationContext";
			OfficeManagedObjectSource authenticationContextMos = office.addOfficeManagedObjectSource(
					authenticationContextName,
					new AuthenticationContextManagedObjectSource<>(this.name, this.security));
			authenticationContextMos.setTimeout(this.timeout);
			office.link(authenticationContextMos.getOfficeManagedObjectFlow("AUTHENTICATE"),
					this.section.getOfficeSectionInput("ManagedObjectAuthenticate"));
			office.link(authenticationContextMos.getOfficeManagedObjectFlow("LOGOUT"),
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
			office.link(authentication.getOfficeManagedObjectDependency("AUTHENTICATION_CONTEXT"),
					authenticationContext);

			// Add the HTTP authentication
			Class<A> authenticationType = this.type.getAuthenticationType();
			if (HttpAuthentication.class.isAssignableFrom(authenticationType)) {
				this.httpAuthentication = authentication;
			} else {
				String httpAuthenticationName = this.name + "_HttpAuthentication";
				OfficeManagedObjectSource httpAuthenticationMos = office.addOfficeManagedObjectSource(
						httpAuthenticationName, new HttpAuthenticationManagedObjectSource<>(this.type));
				this.httpAuthentication = httpAuthenticationMos.addOfficeManagedObject(httpAuthenticationName,
						ManagedObjectScope.PROCESS);
				office.link(this.httpAuthentication.getOfficeManagedObjectDependency("AUTHENTICATION"), authentication);
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
			office.link(accessControl.getOfficeManagedObjectDependency("AUTHENTICATION_CONTEXT"),
					authenticationContext);

			// Add the HTTP access control
			if (HttpAccessControl.class.isAssignableFrom(accessControlType)) {
				this.httpAccessControl = accessControl;
			} else {
				String httpAccessControlName = this.name + "_HttpAccessControl";
				OfficeManagedObjectSource httpAccessControlMos = office.addOfficeManagedObjectSource(
						httpAccessControlName, new HttpAccessControlManagedObjectSource<>(this.type));
				this.httpAccessControl = httpAccessControlMos.addOfficeManagedObject(httpAccessControlName,
						ManagedObjectScope.PROCESS);
				office.link(httpAccessControl.getOfficeManagedObjectDependency("ACCESS_CONTROL"), accessControl);
				if (isRequireTypeQualification) {
					httpAccessControl.addTypeQualification(this.name, HttpAccessControl.class.getName());
				}
			}

			// Wire up the section
			office.link(this.section.getOfficeSectionObject(AuthenticationContext.class.getSimpleName()),
					authenticationContext);
			office.link(this.section.getOfficeSectionObject("AccessControl"), accessControl);

			// Wire up the execution start/stop
			String executionName = this.name + "_HttpSecurityExecuteContext";
			OfficeManagedObjectSource executeMos = office.addOfficeManagedObjectSource(executionName,
					new HttpSecurityExecuteManagedObjectSource<>(this.source, this.type));
			for (HttpSecurityFlowType<?> flowType : this.type.getFlowTypes()) {
				office.link(executeMos.getOfficeManagedObjectFlow(flowType.getFlowName()), this.section
						.getOfficeSectionInput(HttpSecuritySectionSource.INPUT_FLOW_PREFIX + flowType.getFlowName()));
			}

			// Provide execution managed object and register exploring
			OfficeManagedObject executeMo = executeMos.addOfficeManagedObject(executionName,
					ManagedObjectScope.PROCESS);
			executeMo.addExecutionExplorer((explore) -> {
				for (HttpSecurityExplorer explorer : employer.explorers) {
					explorer.explore(new HttpSecurityExplorerContext() {

						@Override
						public String getHttpSecurityName() {
							return securityBuilder.name;
						}

						@Override
						public HttpSecuritySource<?, ?, ?, ?, ?> getHttpSecuritySource() {
							return securityBuilder.source;
						}

						@Override
						public HttpSecurityType<?, ?, ?, ?, ?> getHttpSecurityType() {
							return securityBuilder.type;
						}

						@Override
						public ExecutionManagedFunction getManagedFunction(HttpSecurityFlowType<?> flowType) {
							ExecutionManagedObject managedObject = explore.getInitialManagedObject();
							ManagedObjectFlowType<?> moFlowType = this.getType(flowType.getFlowName(),
									managedObject.getManagedObjectType().getFlowTypes(), (type) -> type.getFlowName());
							return managedObject.getManagedFunction(moFlowType);
						}

						private <T> T getType(String name, T[] types, Function<T, String> nameExtractor) {
							return Arrays.asList(types).stream().filter(type -> name.equals(nameExtractor.apply(type)))
									.findFirst().orElse(null);
						}
					});
				}
			});

			// Provide application credentials linking
			Class<C> credentialsType = this.type.getCredentialsType();
			if (credentialsType != null) {
				employer.webArchitect
						.reroute(this.section.getOfficeSectionOutput(HttpSecuritySectionSource.OUTPUT_RECONTINUE));
			}

			// Provide the supporting managed objects
			Map<HttpSecuritySupportingManagedObjectType, OfficeManagedObject> dependencies = new HashMap<>();
			Map<Class<?>, List<OfficeManagedObject>> supportingByType = new HashMap<>();
			for (HttpSecuritySupportingManagedObjectType supportingManagedObjectType : this.type
					.getSupportingManagedObjectTypes()) {

				// Add the supporting managed object source
				String supportingManagedObjectName = this.name + "_SupportingManagedObject_"
						+ supportingManagedObjectType.getSupportingManagedObjectName();
				OfficeManagedObjectSource supportingManagedObjectSource = office.addOfficeManagedObjectSource(
						supportingManagedObjectName, supportingManagedObjectType.getManagedObjectSource());
				supportingManagedObjectType.getProperties().configureProperties(supportingManagedObjectSource);

				// Add the supporting managed object
				ManagedObjectScope supportingManagedObjectScope = supportingManagedObjectType.getManagedObjectScope();
				OfficeManagedObject supportingManagedObject = supportingManagedObjectSource
						.addOfficeManagedObject(supportingManagedObjectName, supportingManagedObjectScope);
				if (isRequireTypeQualification) {
					Class<?> objectType = supportingManagedObjectType.getObjectType();
					supportingManagedObject.addTypeQualification(this.name, objectType.getName());

					// Register for unique
					List<OfficeManagedObject> supporting = supportingByType.get(objectType);
					if (supporting == null) {
						supporting = new LinkedList<>();
						supportingByType.put(objectType, supporting);
					}
					supporting.add(supportingManagedObject);
				}

				// Register the dependency
				dependencies.put(supportingManagedObjectType, supportingManagedObject);
			}

			// Unique supporting managed objects loaded without type qualification
			supportingByType.forEach((objectType, supporting) -> {
				if (supporting.size() == 1) {
					supporting.get(0).addTypeQualification(null, objectType.getName());
				}
			});

			// Create the dependency context
			HttpSecuritySupportingManagedObjectDependencyContext dependencyContext = new HttpSecuritySupportingManagedObjectDependencyContext() {

				@Override
				public OfficeManagedObject getAuthentication() {
					return authentication;
				}

				@Override
				public OfficeManagedObject getHttpAuthentication() {
					return securityBuilder.httpAuthentication;
				}

				@Override
				public OfficeManagedObject getAccessControl() {
					return accessControl;
				}

				@Override
				public OfficeManagedObject getHttpAccessControl() {
					return securityBuilder.httpAccessControl;
				}

				@Override
				public OfficeManagedObject getSupportingManagedObject(
						HttpSecuritySupportingManagedObject<?> supportingManagedObject) {
					HttpSecuritySupportingManagedObjectType type = (HttpSecuritySupportingManagedObjectType) supportingManagedObject;
					return dependencies.get(type);
				}
			};

			// Link the dependencies
			for (HttpSecuritySupportingManagedObjectType supportingManagedObjectType : this.type
					.getSupportingManagedObjectTypes()) {

				// Obtain the managed object
				OfficeManagedObject managedObject = dependencies.get(supportingManagedObjectType);

				// Link the dependencies
				NEXT_DEPENDENCY: for (HttpSecuritySupportingManagedObjectDependencyType<?> dependencyType : supportingManagedObjectType
						.getDependencyTypes()) {

					// Obtain the dependency
					String dependencyName = dependencyType.getKey().name();
					OfficeManagedObjectDependency dependency = managedObject
							.getOfficeManagedObjectDependency(dependencyName);

					// Obtain the dependency to link
					OfficeManagedObject dependent = dependencyType.getOfficeManagedObject(dependencyContext);
					if (dependent == null) {
						employer.officeArchitect
								.addIssue("No dependency for " + dependencyName + " of supporting object "
										+ supportingManagedObjectType.getSupportingManagedObjectName());
						continue NEXT_DEPENDENCY;
					}

					// Link the dependency
					employer.officeArchitect.link(dependency, dependent);
				}
			}
		}

		/*
		 * ================== HttpSecurityBuilder =======================
		 */

		@Override
		public void addProperty(String name, String value) {
			this.properties.addProperty(name).setValue(value);
		}

		@Override
		public void setTimeout(long timeout) {
			this.timeout = timeout;
		}

		@Override
		public void addContentType(String contentType) {
			this.contentTypes.add(contentType);
		}

		@Override
		public OfficeSectionInput getAuthenticateInput() {
			return this.section.getOfficeSectionInput(HttpSecuritySectionSource.INPUT_AUTHENTICATE);
		}

		@Override
		public OfficeSectionOutput getOutput(String outputName) {
			return this.section.getOfficeSectionOutput(outputName);
		}

		@Override
		public HttpSecurer createHttpSecurer(HttpSecurable securable) {

			// Create securer, qualifying to this security
			HttpSecurerBuilderImpl builder = new HttpSecurerBuilderImpl(securable, this.name);

			// Register and return securer builder
			HttpSecurityArchitectEmployer.this.securers.add(builder);
			return builder;
		}

		/*
		 * =============== HttpSecurityConfiguration ====================
		 */

		@Override
		public String getHttpSecurityName() {
			return this.name;
		}

		@Override
		public HttpSecurity<A, AC, C, O, F> getHttpSecurity() {
			return this.security;
		}

		@Override
		public Class<F> getFlowKeyClass() {
			return this.flowKeys;
		}

		@Override
		public HttpSecurityType<A, AC, C, O, F> getHttpSecurityType() {
			return this.type;
		}
	}

	/**
	 * {@link HttpSecurer} implementation.
	 */
	private class HttpSecurerBuilderImpl implements HttpSecurer {

		/**
		 * {@link HttpSecurable}.
		 */
		private final HttpSecurable securable;

		/**
		 * Name of the {@link HttpSecurity}. May be <code>null</code>.
		 */
		private final String httpSecurityName;

		/**
		 * {@link HttpOfficeSecurer} instances.
		 */
		private final List<HttpOfficeSecurer> httpOfficeSecurers = new LinkedList<>();

		/**
		 * Instantiate.
		 * 
		 * @param securable        {@link HttpSecurable}.
		 * @param httpSecurityName Name of the {@link HttpSecurity}. May be
		 *                         <code>null</code>.
		 */
		private HttpSecurerBuilderImpl(HttpSecurable securable, String httpSecurityName) {
			this.securable = securable;
			this.httpSecurityName = httpSecurityName;
		}

		/**
		 * Obtains the name of the {@link HttpSecurity}.
		 * 
		 * @return Name of the {@link HttpSecurity}. May be <code>null</code>.
		 */
		private String getHttpSecurityName() {
			String name = this.securable != null ? this.securable.getHttpSecurityName() : null;
			if (name == null) {
				name = this.httpSecurityName;
			}
			return name;
		}

		/**
		 * Obtains the any roles.
		 * 
		 * @return Any roles.
		 */
		private String[] getAnyRoles() {
			String[] roles = this.securable != null ? this.securable.getAnyRoles() : null;
			if (roles == null) {
				roles = new String[0];
			}
			return roles;
		}

		/**
		 * Obtains the required roles.
		 * 
		 * @return Required roles.
		 */
		private String[] getRequiredRoles() {
			String[] roles = this.securable != null ? this.securable.getRequiredRoles() : null;
			if (roles == null) {
				roles = new String[0];
			}
			return roles;
		}

		/*
		 * ================ HttpSecurerBuilder ==========================
		 */

		@Override
		public void secure(HttpOfficeSecurer securer) {
			this.httpOfficeSecurers.add(securer);
		}

		@Override
		public HttpFlowSecurer createFlowSecurer() {
			return new HttpFlowSecurerImpl(this);
		}
	}

	/**
	 * {@link HttpFlowSecurer} implementation.
	 */
	private class HttpFlowSecurerImpl implements HttpFlowSecurer {

		/**
		 * {@link HttpSecurerBuilderImpl} for access configuration.
		 */
		private final HttpSecurerBuilderImpl httpSecurerBuilder;

		/**
		 * Instantiate.
		 * 
		 * @param annotation {@link HttpFlowSecurerAnnotation}.
		 */
		private HttpFlowSecurerImpl(HttpSecurerBuilderImpl httpSecurerBuilder) {
			this.httpSecurerBuilder = httpSecurerBuilder;
		}

		/*
		 * =============== HttpFlowSecurer =============================
		 */

		@Override
		public SectionFlowSinkNode secureFlow(SectionDesigner designer, Class<?> argumentType,
				SectionFlowSinkNode secureFlowSink, SectionFlowSinkNode insecureFlowSink) {

			// Create the annotation
			HttpFlowSecurerAnnotation annotation = new HttpFlowSecurerAnnotation(
					this.httpSecurerBuilder.getHttpSecurityName(), this.httpSecurerBuilder.getAnyRoles(),
					this.httpSecurerBuilder.getRequiredRoles(), argumentType);

			// Configure the function
			String functionName = HttpSecurityArchitectEmployer.this.nextUniqueName();
			SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(functionName,
					new HttpFlowSecurerManagedFunctionSource(annotation));
			SectionFunction function = namespace.addSectionFunction(functionName,
					HttpFlowSecurerManagedFunctionSource.FUNCTION_NAME);

			// Configure the function
			designer.link(function.getFunctionFlow(Flows.SECURE.name()), secureFlowSink, false);
			designer.link(function.getFunctionFlow(Flows.INSECURE.name()), insecureFlowSink, false);

			// Return the decision flow sink
			return function;
		}
	}

	/**
	 * {@link HttpFlowSecurer} {@link SectionSource}.
	 */
	@PrivateSource
	private class HttpFlowSecurerSectionSource extends AbstractSectionSource {

		/**
		 * {@link HttpFlowSecurerAnnotation} for {@link ManagedFunction} annotation
		 * processing.
		 */
		private final HttpFlowSecurerAnnotation annotation;

		/**
		 * {@link SectionInput} name.
		 */
		private static final String INPUT_NAME = "input";

		/**
		 * Secure {@link SectionOutput} name.
		 */
		private static final String SECURE_OUTPUT_NAME = "secure";

		/**
		 * Insecure {@link SectionOutput} name.
		 */
		private static final String INSECURE_OUTPUT_NAME = "insecure";

		/**
		 * Instantiate.
		 * 
		 * @param annotation {@link HttpFlowSecurerAnnotation}.
		 */
		private HttpFlowSecurerSectionSource(HttpFlowSecurerAnnotation annotation) {
			this.annotation = annotation;
		}

		/*
		 * =================== SectionSource ============================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
			SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("secure",
					new HttpFlowSecurerManagedFunctionSource(this.annotation));
			SectionFunction function = namespace.addSectionFunction("secure",
					HttpFlowSecurerManagedFunctionSource.FUNCTION_NAME);
			designer.link(designer.addSectionInput(INPUT_NAME, null), function);
			designer.link(function.getFunctionFlow(Flows.SECURE.name()),
					designer.addSectionOutput(SECURE_OUTPUT_NAME, null, false), false);
			designer.link(function.getFunctionFlow(Flows.INSECURE.name()),
					designer.addSectionOutput(INSECURE_OUTPUT_NAME, null, false), false);
		}
	}

	/**
	 * {@link HttpFlowSecurer} {@link ManagedFunctionSource}.
	 */
	@PrivateSource
	private class HttpFlowSecurerManagedFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * Name of the secure {@link ManagedFunction}.
		 */
		private static final String FUNCTION_NAME = "secure";

		/**
		 * {@link HttpFlowSecurerAnnotation} for {@link ManagedFunction} annotation
		 * processing.
		 */
		private final HttpFlowSecurerAnnotation annotation;

		/**
		 * Instantiate.
		 *
		 * @param annotation {@link HttpFlowSecurerAnnotation}.
		 */
		private HttpFlowSecurerManagedFunctionSource(HttpFlowSecurerAnnotation annotation) {
			this.annotation = annotation;
		}

		/*
		 * ================= ManagedFunctionSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Obtain the access roles
			String[] anyRoles = this.annotation.anyRoles;
			String[] allRoles = this.annotation.allRoles;

			// Register the function with the annotation
			ManagedFunctionTypeBuilder<Indexed, Flows> function = functionNamespaceTypeBuilder
					.addManagedFunctionType(FUNCTION_NAME, Indexed.class, Flows.class)
					.setFunctionFactory(new HttpFlowSecurerManagedFunction(this.annotation.argumentType != null,
							anyRoles, allRoles));
			function.addFlow().setKey(Flows.SECURE);
			function.addFlow().setKey(Flows.INSECURE);
			function.addObject(HttpAuthentication.class);
			if (this.annotation.argumentType != null) {
				function.addObject(this.annotation.argumentType);
			}
			function.addAnnotation(this.annotation);
		}
	}

	/**
	 * Annotation on the {@link HttpFlowSecurer} {@link ManagedFunction}.
	 */
	private static class HttpFlowSecurerAnnotation {

		/**
		 * Qualifier. May be <code>null</code>.
		 */
		private final String qualifier;

		/**
		 * Any roles.
		 */
		private final String[] anyRoles;

		/**
		 * All roles.
		 */
		private final String[] allRoles;

		/**
		 * Type of argument to pass through on secure {@link Flow}. May be
		 * <code>null</code> for no argument.
		 */
		private final Class<?> argumentType;

		/**
		 * Instantiate.
		 * 
		 * @param qualifier    Qualifier. May be <code>null</code>.
		 * @param anyRoles     Any roles.
		 * @param allRoles     All Roles.
		 * @param argumentType Type of argument to pass through on secure {@link Flow}.
		 *                     May be <code>null</code> for no argument.
		 */
		private HttpFlowSecurerAnnotation(String qualifier, String[] anyRoles, String[] allRoles,
				Class<?> argumentType) {
			this.qualifier = qualifier;
			this.anyRoles = anyRoles;
			this.allRoles = allRoles;
			this.argumentType = argumentType;
		}
	}

}
