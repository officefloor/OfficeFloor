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
package net.officefloor.plugin.web.http.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.security.scheme.BasicHttpSecuritySource;
import net.officefloor.plugin.web.http.security.scheme.DigestHttpSecuritySource;
import net.officefloor.plugin.web.http.security.scheme.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.scheme.HttpSecuritySourceContext;
import net.officefloor.plugin.web.http.security.scheme.NoneHttpSecuritySource;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link ManagedObjectSource} for a {@link HttpSecurity}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityServiceManagedObjectSource extends
		AbstractManagedObjectSource<Indexed, None> {

	/**
	 * Name of property specifying the authentication scheme.
	 */
	public static final String PROPERTY_AUTHENTICATION_SCHEME = "http.security.service.authentication.scheme";

	/**
	 * Authentication scheme <code>None</code>.
	 */
	public static final String NONE_AUTHENTICATION_SCHEME = "None";

	/**
	 * {@link NoneHttpSecuritySource} class.
	 */
	private static final Class<NoneHttpSecuritySource> NONE_HTTP_SECURITY_SOURCE = NoneHttpSecuritySource.class;

	/**
	 * Authentication scheme <code>Basic</code>.
	 */
	public static final String BASIC_AUTHENTICATION_SCHEME = "Basic";

	/**
	 * {@link BasicHttpSecuritySource} class.
	 */
	private static final Class<BasicHttpSecuritySource> BASIC_HTTP_SECURITY_SOURCE = BasicHttpSecuritySource.class;

	/**
	 * Authentication scheme <code>Digest</code>.
	 */
	public static final String DIGEST_AUTHENTICATION_SCHEME = "Digest";

	/**
	 * {@link DigestHttpSecuritySource} class.
	 */
	private static final Class<DigestHttpSecuritySource> DIGEST_HTTP_SECURITY_SOURCE = DigestHttpSecuritySource.class;

	/**
	 * Name of property specifying the {@link HttpSecuritySource} class if not
	 * defined authentication scheme.
	 */
	public static final String PROPERTY_HTTP_SECURITY_SOURCE_CLASS_NAME = "http.security.service.source.class.name";

	/**
	 * {@link HttpSecuritySource}.
	 */
	private HttpSecuritySource<?> source;

	/**
	 * Mapping of additional dependency key to object dependency index.
	 */
	private Enum<?>[] dependencyKeyMapping;

	/*
	 * ======================== ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// Always require authentication scheme
		context.addProperty(PROPERTY_AUTHENTICATION_SCHEME,
				"Authentication Scheme");
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadMetaData(final MetaDataContext<Indexed, None> context)
			throws Exception {
		final ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the authentication scheme
		String authenticationScheme = mosContext
				.getProperty(PROPERTY_AUTHENTICATION_SCHEME);

		// Obtain the security source
		Class<?> httpSecuritySourceClass;
		if (NONE_AUTHENTICATION_SCHEME.equalsIgnoreCase(authenticationScheme)) {
			httpSecuritySourceClass = NONE_HTTP_SECURITY_SOURCE;
		} else if (BASIC_AUTHENTICATION_SCHEME
				.equalsIgnoreCase(authenticationScheme)) {
			httpSecuritySourceClass = BASIC_HTTP_SECURITY_SOURCE;
		} else if (DIGEST_AUTHENTICATION_SCHEME
				.equalsIgnoreCase(authenticationScheme)) {
			httpSecuritySourceClass = DIGEST_HTTP_SECURITY_SOURCE;
		} else {
			// Custom authentication scheme
			String httpSecuritySourceClassName = mosContext
					.getProperty(PROPERTY_HTTP_SECURITY_SOURCE_CLASS_NAME);
			httpSecuritySourceClass = mosContext
					.loadClass(httpSecuritySourceClassName);
		}

		// Instantiate the security source
		Object instance = httpSecuritySourceClass.newInstance();
		if (!(instance instanceof HttpSecuritySource<?>)) {
			throw new IllegalArgumentException("Property '"
					+ PROPERTY_HTTP_SECURITY_SOURCE_CLASS_NAME
					+ "' must specify a class of type "
					+ HttpSecuritySource.class.getName() + " (specified "
					+ httpSecuritySourceClass.getName() + ")");
		}
		this.source = (HttpSecuritySource<?>) instance;

		// Register the standard dependencies
		context.addDependency(ServerHttpConnection.class).setLabel(
				"HTTP_CONNECTION");
		context.addDependency(HttpSession.class).setLabel("HTTP_SESSION");

		// Initialise the source (obtaining necessary dependencies)
		final Map<Enum, Class> additionalDependencies = new HashMap<Enum, Class>();
		this.source.init(new HttpSecuritySourceContextImpl(mosContext,
				additionalDependencies));

		// Obtains the dependency keys
		this.dependencyKeyMapping = additionalDependencies.keySet().toArray(
				new Enum[0]);
		if (this.dependencyKeyMapping.length > 0) {

			// Ensure all dependency keys registered (by checking length)
			Class<?> dependencyKeyClass = this.dependencyKeyMapping[0]
					.getDeclaringClass();
			Object[] expectedKeys = dependencyKeyClass.getEnumConstants();
			if (expectedKeys.length != this.dependencyKeyMapping.length) {
				throw new IllegalArgumentException(
						"Not all dependency keys registered");
			}

			// Register the additional dependencies in sorted order
			Arrays.sort(this.dependencyKeyMapping);
			for (Enum key : this.dependencyKeyMapping) {
				// Register the dependency
				Class<?> dependencyType = additionalDependencies.get(key);
				Labeller labeller = context.addDependency(dependencyType);
				labeller.setLabel("Dependency-" + key.name());
			}
		}

		// Provide the meta-data
		context.setObjectClass(HttpSecurityService.class);
		context.setManagedObjectClass(HttpSecurityServiceManagedObject.class);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected ManagedObject getManagedObject() throws Throwable {
		// Create and return the managed object
		return new HttpSecurityServiceManagedObject(this.source,
				this.dependencyKeyMapping);
	}

	/**
	 * {@link HttpSecuritySourceContext} implementation.
	 */
	private static class HttpSecuritySourceContextImpl<D extends Enum<D>>
			extends SourcePropertiesImpl implements
			HttpSecuritySourceContext<D> {

		/**
		 * Additional dependencies.
		 */
		private final Map<Enum<D>, Class<?>> additionalDependencies;

		/**
		 * Initiate.
		 * 
		 * @param properties
		 *            {@link SourceProperties}.
		 * @param additionalDependencies
		 *            Additional dependencies.
		 */
		public HttpSecuritySourceContextImpl(SourceProperties properties,
				Map<Enum<D>, Class<?>> additionalDependencies) {
			super(properties);
			this.additionalDependencies = additionalDependencies;
		}

		/*
		 * ================= HttpSecuritySourceContext ===================
		 */

		@Override
		public void requireDependency(D key, Class<?> dependencyType) {
			// Register the required dependency
			this.additionalDependencies.put(key, dependencyType);
		}
	}

}