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

package net.officefloor.web.security.type;

import java.sql.Connection;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.server.http.mock.MockServerHttpConnection;
import net.officefloor.web.security.AuthenticationRequiredException;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.scheme.AnonymousHttpSecuritySource;
import net.officefloor.web.security.scheme.BasicHttpSecuritySource;
import net.officefloor.web.security.scheme.MockAccessControl;
import net.officefloor.web.security.scheme.MockAuthentication;
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.HttpAccessControlFactory;
import net.officefloor.web.spi.security.HttpAuthenticationFactory;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecurityDependencyMetaData;
import net.officefloor.web.spi.security.HttpSecurityExecuteContext;
import net.officefloor.web.spi.security.HttpSecurityFlowMetaData;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySourceContext;
import net.officefloor.web.spi.security.HttpSecuritySourceMetaData;
import net.officefloor.web.spi.security.HttpSecuritySourceSpecification;
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;
import net.officefloor.web.spi.security.LogoutContext;
import net.officefloor.web.spi.security.RatifyContext;

/**
 * Tests loading the {@link HttpSecurityType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadHttpSecurityTypeTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link HttpSecuritySourceMetaData}.
	 */
	private final HttpSecuritySourceMetaData<?, ?, ?, ?, ?> metaData = this
			.createMock(HttpSecuritySourceMetaData.class);

	@Override
	protected void setUp() throws Exception {
		MockHttpSecuritySource.reset(this.metaData);
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.record_issue("Must specify property 'missing'");

		// Attempt to load
		this.loadHttpSecurityType(false, new Init() {
			@Override
			public void init(HttpSecuritySourceContext context) {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Attempt to load
		this.loadHttpSecurityType(true, new Init() {
			@Override
			public void init(HttpSecuritySourceContext context) {
				assertEquals("Ensure get defaulted property", "DEFAULT", context.getProperty("missing", "DEFAULT"));
				assertEquals("Ensure get property ONE", "1", context.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2", context.getProperty("TWO"));
				Properties properties = context.getProperties();
				assertEquals("Incorrect number of properties", 2, properties.size());
				assertEquals("Incorrect property ONE", "1", properties.get("ONE"));
				assertEquals("Incorrect property TWO", "2", properties.get("TWO"));
			}
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.record_issue("Can not load class 'missing'");

		// Attempt to load
		this.loadHttpSecurityType(false, new Init() {
			@Override
			public void init(HttpSecuritySourceContext context) {
				context.loadClass("missing");
			}
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing resource
		this.record_issue("Can not obtain resource at location 'missing'");

		// Attempt to load
		this.loadHttpSecurityType(false, new Init() {
			@Override
			public void init(HttpSecuritySourceContext context) {
				context.getResource("missing");
			}
		});
	}

	/**
	 * Ensure able to get resource.
	 */
	public void testGetResource() {

		// Record basic meta-data
		this.record_basicMetaData();

		// Obtain path
		final String objectPath = Object.class.getName().replace('.', '/') + ".class";

		// Attempt to load
		this.loadHttpSecurityType(true, new Init() {
			@Override
			public void init(HttpSecuritySourceContext context) {
				assertEquals("Incorrect resource locator",
						LoadHttpSecurityTypeTest.class.getClassLoader().getResource(objectPath),
						context.getClassLoader().getResource(objectPath));
			}
		});
	}

	/**
	 * Ensure issue if fails to init the {@link HttpSecuritySource}.
	 */
	public void testFailInitHttpSecuritySource() {

		final NullPointerException failure = new NullPointerException("Fail init HttpSecuritySource");

		// Record failure to init the HTTP security Source
		this.record_issue("Failed to init", failure);

		// Attempt to load
		this.loadHttpSecurityType(false, new Init() {
			@Override
			public void init(HttpSecuritySourceContext context) {
				throw failure;
			}
		});
	}

	/**
	 * Ensure issue if <code>null</code> {@link HttpSecuritySourceMetaData}.
	 */
	public void testNullHttpSecuritySourceMetaData() {

		// Record null the HTTP security Source meta-data
		this.record_issue("Returned null ManagedObjectSourceMetaData");

		// Attempt to load
		this.loadHttpSecurityType(false, new Init() {
			@Override
			public void init(HttpSecuritySourceContext context) {
				MockHttpSecuritySource.metaData = null;
			}
		});
	}

	/**
	 * Ensure issue if fails to obtain the {@link HttpSecuritySourceMetaData}.
	 */
	public void testFailGetHttpSecuritySourceMetaData() {

		final Error failure = new Error("Obtain meta-data failure");

		// Record failure to obtain the meta-data
		this.record_issue("Failed to init", failure);

		// Attempt to load
		this.loadHttpSecurityType(false, new Init() {
			@Override
			public void init(HttpSecuritySourceContext context) {
				MockHttpSecuritySource.metaDataFailure = failure;
			}
		});
	}

	/**
	 * Ensure issue if no authentication class from meta-data.
	 */
	public void testNoAuthenticationClass() {

		// Record no authentication class
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getAuthenticationType(), null);
		this.record_issue("No Authentication type provided");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if no {@link HttpAuthenticationFactory} provided for
	 * authentication not implementing {@link HttpAuthentication}.
	 */
	public void testNoHttpAuthenticationFactory() {

		// Record no HTTP authentication factory
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getAuthenticationType(), MockAuthentication.class);
		this.recordReturn(this.metaData, this.metaData.getHttpAuthenticationFactory(), null);
		this.record_issue(
				"Must provide HttpAuthenticationFactory, as Authentication does not implement HttpAuthentication (Authentication Type: "
						+ MockAuthentication.class.getName() + ")");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if no access control class from meta-data.
	 */
	public void testNoAccessControlClass() {

		// Record no access control class
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getAuthenticationType(), HttpAuthentication.class);
		this.recordReturn(this.metaData, this.metaData.getAccessControlType(), null);
		this.record_issue("No Access Control type provided");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if no {@link HttpAccessControlFactory} provided for access
	 * control not implementing {@link HttpAccessControl}.
	 */
	public void testNoHttpAccessControlFactory() {

		// Record no HTTP access control factory
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getAuthenticationType(), HttpAuthentication.class);
		this.recordReturn(this.metaData, this.metaData.getAccessControlType(), MockAccessControl.class);
		this.recordReturn(this.metaData, this.metaData.getHttpAccessControlFactory(), null);
		this.record_issue(
				"Must provide HttpAccessControlFactory, as Access Control does not implement HttpAccessControl (Access Control Type: "
						+ MockAccessControl.class.getName() + ")");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link HttpSecurityDependencyMetaData} in
	 * array.
	 */
	public void testNullDependencyMetaData() {

		// Record no dependency type
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new HttpSecurityDependencyMetaData[] { null });
		this.record_issue("Null ManagedObjectDependencyMetaData for dependency 0");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if no dependency type.
	 */
	public void testNoDependencyType() {

		final HttpSecurityDependencyMetaData<?> dependency = this.createMock(HttpSecurityDependencyMetaData.class);

		// Record no dependency type
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new HttpSecurityDependencyMetaData[] { dependency });
		this.recordReturn(dependency, dependency.getLabel(), "NO TYPE");
		this.recordReturn(dependency, dependency.getKey(), null);
		this.recordReturn(dependency, dependency.getType(), null);
		this.record_issue("No type for dependency 0 (key=<indexed>, label=NO TYPE)");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if dependency keys of different types.
	 */
	public void testInvalidDependencyKey() {

		final HttpSecurityDependencyMetaData<?> dependencyOne = this.createMock(HttpSecurityDependencyMetaData.class);
		final HttpSecurityDependencyMetaData<?> dependencyTwo = this.createMock(HttpSecurityDependencyMetaData.class);

		// Record missing dependency key
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new HttpSecurityDependencyMetaData[] { dependencyOne, dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyOne, dependencyOne.getType(), Connection.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), InvalidKey.INVALID);
		this.record_issue("Dependencies identified by different key types (" + TwoKey.class.getName() + ", "
				+ InvalidKey.class.getName() + ")");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if dependency mixing using keys and indexes.
	 */
	public void testDependencyMixingKeyAndIndexes() {

		final HttpSecurityDependencyMetaData<?> dependencyOne = this.createMock(HttpSecurityDependencyMetaData.class);
		final HttpSecurityDependencyMetaData<?> dependencyTwo = this.createMock(HttpSecurityDependencyMetaData.class);

		// Record missing dependency key
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new HttpSecurityDependencyMetaData[] { dependencyOne, dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyOne, dependencyOne.getType(), Connection.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), null);
		this.record_issue("Dependencies mixing keys and indexes");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if key used more than once for a dependency.
	 */
	public void testDuplicateDependencyKey() {

		final HttpSecurityDependencyMetaData<?> dependencyOne = this.createMock(HttpSecurityDependencyMetaData.class);
		final HttpSecurityDependencyMetaData<?> dependencyTwo = this.createMock(HttpSecurityDependencyMetaData.class);

		// Record missing dependency key
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new HttpSecurityDependencyMetaData[] { dependencyOne, dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyOne, dependencyOne.getType(), Connection.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyTwo, dependencyTwo.getType(), String.class);
		this.recordReturn(dependencyTwo, dependencyTwo.getTypeQualifier(), null);
		this.record_issue("Must have exactly one dependency per key (key=" + TwoKey.ONE + ")");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if not have meta-data for each dependency key.
	 */
	public void testNotAllDependencyKeys() {

		final HttpSecurityDependencyMetaData<?> dependency = this.createMock(HttpSecurityDependencyMetaData.class);

		// Record not all dependency keys
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new HttpSecurityDependencyMetaData[] { dependency });
		this.recordReturn(dependency, dependency.getLabel(), null);
		this.recordReturn(dependency, dependency.getKey(), TwoKey.ONE);
		this.recordReturn(dependency, dependency.getType(), Connection.class);
		this.recordReturn(dependency, dependency.getTypeQualifier(), null);
		this.record_issue("Missing dependency meta-data (keys=" + TwoKey.TWO + ")");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link HttpSecurityFlowMetaData} in array.
	 */
	public void testNullFlowMetaData() {

		// Record no flow type
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), new HttpSecurityFlowMetaData[] { null });
		this.record_issue("Null ManagedObjectFlowMetaData for flow 0");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure able to default the flow argument type.
	 */
	public void testDefaultFlowArgumentType() {

		final HttpSecurityFlowMetaData<?> flowDefaulted = this.createMock(HttpSecurityFlowMetaData.class);
		final HttpSecurityFlowMetaData<?> flowProvided = this.createMock(HttpSecurityFlowMetaData.class);

		// Record no flow argument type
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new HttpSecurityFlowMetaData[] { flowDefaulted, flowProvided });
		this.recordReturn(flowDefaulted, flowDefaulted.getLabel(), "DEFAULTED");
		this.recordReturn(flowDefaulted, flowDefaulted.getKey(), null);
		this.recordReturn(flowDefaulted, flowDefaulted.getArgumentType(), null);
		this.recordReturn(flowProvided, flowProvided.getLabel(), null);
		this.recordReturn(flowProvided, flowProvided.getKey(), null);
		this.recordReturn(flowProvided, flowProvided.getArgumentType(), Connection.class);
		this.record_authenticationAccessControlAndCredentialsClass();

		// Attempt to load
		HttpSecurityType<?, ?, ?, ?, ?> securityType = this.loadHttpSecurityType(true, null);

		// Validate argument types of flows
		HttpSecurityFlowType<?>[] flowTypes = securityType.getFlowTypes();
		assertEquals("Incorrect number of flows", 2, flowTypes.length);
		HttpSecurityFlowType<?> defaulted = flowTypes[0];
		assertEquals("Incorrect name for defaulted argument flow", "DEFAULTED", defaulted.getFlowName());
		assertNull("Incorrect no argument type", defaulted.getArgumentType());
		HttpSecurityFlowType<?> provided = flowTypes[1];
		assertEquals("Incorrect name for provided argument flow", "1", provided.getFlowName());
		assertEquals("Incorrect provided argument type", Connection.class, provided.getArgumentType());
	}

	/**
	 * Ensure issue if flow keys of different types.
	 */
	public void testInvalidFlowKey() {

		final HttpSecurityFlowMetaData<?> flowOne = this.createMock(HttpSecurityFlowMetaData.class);
		final HttpSecurityFlowMetaData<?> flowTwo = this.createMock(HttpSecurityFlowMetaData.class);

		// Record missing flow key
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new HttpSecurityFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.ONE);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Connection.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), InvalidKey.INVALID);
		this.record_issue("Meta-data flows identified by different key types (" + TwoKey.class.getName() + ", "
				+ InvalidKey.class.getName() + ")");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if flow mixing using keys and indexes.
	 */
	public void testFlowMixingKeyAndIndexes() {

		final HttpSecurityFlowMetaData<?> flowOne = this.createMock(HttpSecurityFlowMetaData.class);
		final HttpSecurityFlowMetaData<?> flowTwo = this.createMock(HttpSecurityFlowMetaData.class);

		// Record missing flow key
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new HttpSecurityFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.ONE);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Connection.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), null);
		this.record_issue("Meta-data flows mixing keys and indexes");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if key used more than once for a flow.
	 */
	public void testDuplicateFlowKey() {

		final HttpSecurityFlowMetaData<?> flowOne = this.createMock(HttpSecurityFlowMetaData.class);
		final HttpSecurityFlowMetaData<?> flowTwo = this.createMock(HttpSecurityFlowMetaData.class);

		// Record missing flow key
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new HttpSecurityFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.ONE);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Connection.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), TwoKey.ONE);
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.record_issue("Must have exactly one flow per key (key=" + TwoKey.ONE + ")");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if not have meta-data for each flow key.
	 */
	public void testNotAllFlowKeys() {

		final HttpSecurityFlowMetaData<?> flow = this.createMock(HttpSecurityFlowMetaData.class);

		// Record not all flow keys
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), new HttpSecurityFlowMetaData[] { flow });
		this.recordReturn(flow, flow.getLabel(), null);
		this.recordReturn(flow, flow.getKey(), TwoKey.ONE);
		this.recordReturn(flow, flow.getArgumentType(), Connection.class);
		this.record_issue("Missing flow meta-data (keys=" + TwoKey.TWO + ")");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure {@link HttpSecurityType} correct with keyed dependencies and flows.
	 */
	public void testKeyedDependenciesAndFlows() {

		final HttpSecurityDependencyMetaData<?> dependencyOne = this.createMock(HttpSecurityDependencyMetaData.class);
		final HttpSecurityDependencyMetaData<?> dependencyTwo = this.createMock(HttpSecurityDependencyMetaData.class);
		final HttpSecurityFlowMetaData<?> flowOne = this.createMock(HttpSecurityFlowMetaData.class);
		final HttpSecurityFlowMetaData<?> flowTwo = this.createMock(HttpSecurityFlowMetaData.class);

		// Record keyed dependencies and flows
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new HttpSecurityDependencyMetaData[] { dependencyOne, dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.TWO); // order
		this.recordReturn(dependencyOne, dependencyOne.getType(), Integer.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), "QUALIFIED");
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), TwoKey.ONE); // order
		this.recordReturn(dependencyTwo, dependencyTwo.getType(), Connection.class);
		this.recordReturn(dependencyTwo, dependencyTwo.getTypeQualifier(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new HttpSecurityFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.TWO); // order
		this.recordReturn(flowOne, flowOne.getArgumentType(), Long.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), TwoKey.ONE); // order
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.record_authenticationAccessControlAndCredentialsClass();

		// Attempt to load
		HttpSecurityType<?, ?, ?, ?, ?> securityType = this.loadHttpSecurityType(true, null);

		// Validate dependencies ordered and correct values
		HttpSecurityDependencyType<?>[] dependencyTypes = securityType.getDependencyTypes();
		assertEquals("Incorrect number of dependencies", 2, dependencyTypes.length);
		HttpSecurityDependencyType<?> dependencyTypeOne = dependencyTypes[0];
		assertEquals("Keys should be ordered", TwoKey.ONE, dependencyTypeOne.getKey());
		assertEquals("Incorrect first dependency index", TwoKey.ONE.ordinal(), dependencyTypeOne.getIndex());
		assertEquals("Incorrect first dependency name", TwoKey.ONE.toString(), dependencyTypeOne.getDependencyName());
		assertEquals("Incorrect first dependency type", Connection.class, dependencyTypeOne.getDependencyType());
		assertNull("First dependency type should not be qualified", dependencyTypeOne.getTypeQualifier());
		HttpSecurityDependencyType<?> dependencyTypeTwo = dependencyTypes[1];
		assertEquals("Keys should be ordered", TwoKey.TWO, dependencyTypeTwo.getKey());
		assertEquals("Incorrect second dependency index", TwoKey.TWO.ordinal(), dependencyTypeTwo.getIndex());
		assertEquals("Incorrect second dependency name", TwoKey.TWO.toString(), dependencyTypeTwo.getDependencyName());
		assertEquals("Incorrect second dependency type", Integer.class, dependencyTypeTwo.getDependencyType());
		assertEquals("Incorrect second dependency type qualification", "QUALIFIED",
				dependencyTypeTwo.getTypeQualifier());

		// Validate flows ordered and correct values
		HttpSecurityFlowType<?>[] flowTypes = securityType.getFlowTypes();
		assertEquals("Incorrect number of dependencies", 2, flowTypes.length);
		HttpSecurityFlowType<?> flowTypeOne = flowTypes[0];
		assertEquals("Keys should be ordered", TwoKey.ONE, flowTypeOne.getKey());
		assertEquals("Incorrect first flow index", TwoKey.ONE.ordinal(), flowTypeOne.getIndex());
		assertEquals("Incorrect first flow name", TwoKey.ONE.toString(), flowTypeOne.getFlowName());
		assertEquals("Incorrect first flow argument type", String.class, flowTypeOne.getArgumentType());
		HttpSecurityFlowType<?> flowTypeTwo = flowTypes[1];
		assertEquals("Keys should be ordered", TwoKey.TWO, flowTypeTwo.getKey());
		assertEquals("Incorrect second flow index", TwoKey.TWO.ordinal(), flowTypeTwo.getIndex());
		assertEquals("Incorrect second flow name", TwoKey.TWO.toString(), flowTypeTwo.getFlowName());
		assertEquals("Incorrect second flow argument type", Long.class, flowTypeTwo.getArgumentType());
	}

	/**
	 * Ensure {@link HttpSecurityType} correct with indexed dependencies and flows.
	 */
	public void testIndexedDependenciesAndFlows() {

		final HttpSecurityDependencyMetaData<?> dependencyOne = this.createMock(HttpSecurityDependencyMetaData.class);
		final HttpSecurityDependencyMetaData<?> dependencyTwo = this.createMock(HttpSecurityDependencyMetaData.class);
		final HttpSecurityFlowMetaData<?> flowOne = this.createMock(HttpSecurityFlowMetaData.class);
		final HttpSecurityFlowMetaData<?> flowTwo = this.createMock(HttpSecurityFlowMetaData.class);

		// Record keyed dependencies and flows
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new HttpSecurityDependencyMetaData[] { dependencyOne, dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), null);
		this.recordReturn(dependencyOne, dependencyOne.getType(), Integer.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), "QUALIFIED");
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getType(), Connection.class);
		this.recordReturn(dependencyTwo, dependencyTwo.getTypeQualifier(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new HttpSecurityFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), null);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Long.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), null);
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.record_authenticationAccessControlAndCredentialsClass();

		// Attempt to load
		HttpSecurityType<?, ?, ?, ?, ?> securityType = this.loadHttpSecurityType(true, null);

		// Validate dependencies
		HttpSecurityDependencyType<?>[] dependencyTypes = securityType.getDependencyTypes();
		assertEquals("Incorrect number of dependencies", 2, dependencyTypes.length);
		HttpSecurityDependencyType<?> dependencyTypeOne = dependencyTypes[0];
		assertEquals("Incorrect first dependency index", 0, dependencyTypeOne.getIndex());
		assertNull("Should be no dependency key", dependencyTypeOne.getKey());
		assertEquals("Incorrect first dependency name", Integer.class.getSimpleName(),
				dependencyTypeOne.getDependencyName());
		assertEquals("Incorrect first dependency type", Integer.class, dependencyTypeOne.getDependencyType());
		assertEquals("Incorrect first dependency type qualification", "QUALIFIED",
				dependencyTypeOne.getTypeQualifier());
		HttpSecurityDependencyType<?> dependencyTypeTwo = dependencyTypes[1];
		assertEquals("Incorrect second dependency index", 1, dependencyTypeTwo.getIndex());
		assertNull("Should be no dependency key", dependencyTypeTwo.getKey());
		assertEquals("Incorrect second dependency name", Connection.class.getSimpleName(),
				dependencyTypeTwo.getDependencyName());
		assertEquals("Incorrect second dependency type", Connection.class, dependencyTypeTwo.getDependencyType());
		assertNull("Second dependency should not be qualified", dependencyTypeTwo.getTypeQualifier());

		// Validate flows
		HttpSecurityFlowType<?>[] flowTypes = securityType.getFlowTypes();
		assertEquals("Incorrect number of dependencies", 2, flowTypes.length);
		HttpSecurityFlowType<?> flowTypeOne = flowTypes[0];
		assertEquals("Incorrect first flow index", 0, flowTypeOne.getIndex());
		assertNull("Should be no flow key", flowTypeOne.getKey());
		assertEquals("Incorrect first flow name", "0", flowTypeOne.getFlowName());
		assertEquals("Incorrect first flow argument type", Long.class, flowTypeOne.getArgumentType());
		HttpSecurityFlowType<?> flowTypeTwo = flowTypes[1];
		assertEquals("Incorrect second flow index", 1, flowTypeTwo.getIndex());
		assertNull("Should be no dependency key", flowTypeTwo.getKey());
		assertEquals("Incorrect second flow name", "1", flowTypeTwo.getFlowName());
		assertEquals("Incorrect second flow argument type", String.class, flowTypeTwo.getArgumentType());
	}

	/**
	 * Ensure obtain appropriate authentication, access control and credentials.
	 */
	public void testAuthenticationAccessControlAndCredentials() {
		this.record_basicMetaData();

		// Load the security type
		HttpSecurityType<?, ?, ?, ?, ?> securityType = this.loadHttpSecurityType(true, null);

		// Ensure correct security and credentials
		assertEquals("Incorrect authentication type", HttpAuthentication.class, securityType.getAuthenticationType());
		assertEquals("Incorrect access control type", HttpAccessControl.class, securityType.getAccessControlType());
		assertEquals("Incorrect credentials type", HttpCredentials.class, securityType.getCredentialsType());
	}

	/**
	 * Ensure may have <code>null</code> (no) credentials required from the
	 * application specific behaviour.
	 */
	public void testNullCredentials() {
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getAuthenticationType(), HttpAuthentication.class);
		this.recordReturn(this.metaData, this.metaData.getAccessControlType(), HttpAccessControl.class);
		this.recordReturn(this.metaData, this.metaData.getCredentialsType(), null);

		// Load the security type
		HttpSecurityType<?, ?, ?, ?, ?> securityType = this.loadHttpSecurityType(true, null);

		// Ensure correct security and no credentials
		assertEquals("Incorrect authentication type", HttpAuthentication.class, securityType.getAuthenticationType());
		assertEquals("Incorrect access control type", HttpAccessControl.class, securityType.getAccessControlType());
		assertNull("Should be no credentials required", securityType.getCredentialsType());
	}

	/**
	 * Ensure able to create {@link AuthenticationContext}.
	 */
	public void testCreateAuthenticationContext() throws Throwable {

		// Create the mocks
		MockServerHttpConnection connection = MockHttpServer.mockConnection();

		// Ensure able to use to handle authentication
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(AnonymousHttpSecuritySource.class);

		// Create the authentication context
		AuthenticationContext<HttpAccessControl, Void> context = HttpSecurityLoaderUtil
				.createAuthenticationContext(connection, security, null);

		// Create the authentication
		HttpAuthentication<Void> authentication = security.createAuthentication(context);
		assertNotNull("Should have authentication", authentication);

		// Validate functionality with authentication
		assertTrue("Should not be authenticated", authentication.isAuthenticated());
		assertSame("Incorrect credentials type", Void.class, authentication.getCredentialsType());
	}

	/**
	 * Ensure able to use the {@link AuthenticationContext}.
	 */
	public void testUseAuthenticationContext() throws Throwable {

		// Create the mocks
		MockHttpRequestBuilder request = MockHttpServer.mockRequest().header("Authorization",
				"Basic " + Base64.getEncoder().encodeToString("Daniel:Test".getBytes()));
		MockServerHttpConnection connection = MockHttpServer.mockConnection(request);
		HttpAccessControl accessControl = this.createMock(HttpAccessControl.class);

		// Ensure able to use to handle authentication
		HttpSecurity<HttpAuthentication<Void>, HttpAccessControl, Void, BasicHttpSecuritySource.Dependencies, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(BasicHttpSecuritySource.class, BasicHttpSecuritySource.PROPERTY_REALM, "test");

		// Create the authentication context
		AuthenticationContext<HttpAccessControl, Void> context = HttpSecurityLoaderUtil.createAuthenticationContext(
				connection, security, (authenticate) -> authenticate.accessControlChange(accessControl, null));

		// Create the authentication
		HttpAuthentication<Void> authentication = security.createAuthentication(context);
		assertNotNull("Should have authentication", authentication);

		// Validate functionality with authentication
		assertTrue("Should be authenticated", authentication.isAuthenticated());
		assertSame("Incorrect access control", accessControl, authentication.getAccessControl());

		// Ensure can log out
		HttpSecurityLoaderUtil.logout(authentication);
		assertFalse("Should not be authentiated", authentication.isAuthenticated());
		try {
			authentication.getAccessControl();
			fail("Should not be successful");
		} catch (AuthenticationRequiredException ex) {
		}

		// Ensure can log back in
		HttpSecurityLoaderUtil.authenticate(authentication, null);
		assertTrue("Should log back in", authentication.isAuthenticated());
		assertSame("Should be same access control again", accessControl, authentication.getAccessControl());
	}

	/**
	 * Ensure {@link HttpSecuritySupportingManagedObjectType} is on
	 * {@link HttpSecurityType}.
	 */
	public void testSupportingManagedObject() throws Throwable {

		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getAuthenticationType(), HttpAuthentication.class);
		this.recordReturn(this.metaData, this.metaData.getAccessControlType(), HttpAccessControl.class);
		this.recordReturn(this.metaData, this.metaData.getCredentialsType(), null);

		// Load type with supporting object
		ClassManagedObjectSource mos = new ClassManagedObjectSource();
		HttpSecurityType<?, ?, ?, ?, ?> type = this.loadHttpSecurityType(true, (context) -> {
			context.addSupportingManagedObject("object", mos, ManagedObjectScope.THREAD).addProperty(
					ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, MockSupportingObject.class.getName());
		});

		// Ensure have supporting object type
		HttpSecuritySupportingManagedObjectType<?>[] supportingObjects = type.getSupportingManagedObjectTypes();
		assertEquals("Incorrect number of supporting objects", 1, supportingObjects.length);
		HttpSecuritySupportingManagedObjectType<?> supportingObject = supportingObjects[0];
		assertEquals("Incorrect name", "object", supportingObject.getSupportingManagedObjectName());
		assertSame("Incorrect managed object source", mos, supportingObject.getManagedObjectSource());
		PropertyList properties = supportingObject.getProperties();
		assertEquals("Incorrect number of properties", 1, properties.getPropertyNames().length);
		assertEquals("Incorrect property", MockSupportingObject.class.getName(),
				properties.getProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).getValue());
		assertSame("Incorrect object type", MockSupportingObject.class, supportingObject.getObjectType());
		assertEquals("Incorrect managed object scope", ManagedObjectScope.THREAD,
				supportingObject.getManagedObjectScope());
		assertEquals("Should be no dependencies", 0, supportingObject.getDependencyTypes().length);
	}

	public static class MockSupportingObject {
	}

	/**
	 * Ensure {@link HttpSecuritySupportingManagedObjectDependencyType} is on
	 * {@link HttpSecuritySupportingManagedObjectType}.
	 */
	@SuppressWarnings("unchecked")
	public void testSupportingManagedObjectDependencies() throws Throwable {

		// Record loading type
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getAuthenticationType(), HttpAuthentication.class);
		this.recordReturn(this.metaData, this.metaData.getAccessControlType(), HttpAccessControl.class);
		this.recordReturn(this.metaData, this.metaData.getCredentialsType(), null);

		// Load type with supporting object
		Closure<HttpSecuritySupportingManagedObject<?>> dependencySupportingObject = new Closure<>();
		HttpSecurityType<?, ?, ?, ?, ?> type = this.loadHttpSecurityType(true, (context) -> {

			// Add the dependency
			HttpSecuritySupportingManagedObject<?> dependency = context.addSupportingManagedObject("object",
					new ClassManagedObjectSource(), ManagedObjectScope.PROCESS);
			dependency.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
					MockSupportingObject.class.getName());
			dependencySupportingObject.value = dependency;

			// Add with dependencies
			HttpSecuritySupportingManagedObject<MockHttpSecuritySupportingManagedObjectDependencies> object = context
					.addSupportingManagedObject("object", new MockHttpSecuritySupportingManagedObjectSource(),
							ManagedObjectScope.FUNCTION);
			object.linkAuthentication(MockHttpSecuritySupportingManagedObjectDependencies.AUTHENTICATION);
			object.linkHttpAuthentication(MockHttpSecuritySupportingManagedObjectDependencies.HTTP_AUTHENTICATION);
			object.linkAccessControl(MockHttpSecuritySupportingManagedObjectDependencies.ACCESS_CONTROL);
			object.linkHttpAccessControl(MockHttpSecuritySupportingManagedObjectDependencies.HTTP_ACCESS_CONTROL);
			object.linkSupportingManagedObject(MockHttpSecuritySupportingManagedObjectDependencies.DEPENDENCY,
					dependency);
		});

		// Ensure correct supporting object
		HttpSecuritySupportingManagedObjectType<MockHttpSecuritySupportingManagedObjectDependencies> supportingObject = (HttpSecuritySupportingManagedObjectType<MockHttpSecuritySupportingManagedObjectDependencies>) type
				.getSupportingManagedObjectTypes()[1];
		assertEquals("Incorrect object", "object", supportingObject.getSupportingManagedObjectName());
		assertEquals("Incorrect scope", ManagedObjectScope.FUNCTION, supportingObject.getManagedObjectScope());

		// Ensure have supporting object dependencies
		Map<MockHttpSecuritySupportingManagedObjectDependencies, HttpSecuritySupportingManagedObjectDependencyType<?>> dependencies = new HashMap<>();
		for (HttpSecuritySupportingManagedObjectDependencyType<MockHttpSecuritySupportingManagedObjectDependencies> dependencyType : supportingObject
				.getDependencyTypes()) {
			dependencies.put(dependencyType.getKey(), dependencyType);
		}
		assertEquals("Incorrect number of dependencies", 5, dependencies.size());

		// Create assertion on getting dependency managed object
		MockHttpSecuritySupportingManagedObjectDependencyContext context = new MockHttpSecuritySupportingManagedObjectDependencyContext(
				dependencySupportingObject.value);
		BiConsumer<MockHttpSecuritySupportingManagedObjectDependencies, OfficeManagedObject> assertDependency = (key,
				expected) -> {
			HttpSecuritySupportingManagedObjectDependencyType<?> dependencyType = dependencies.get(key);
			assertNotNull("No dependency for key " + key, dependencyType);
			assertSame("Incorrect dependency managed object for key " + key, expected,
					dependencyType.getOfficeManagedObject(context));
		};

		// Assert the dependencies
		assertDependency.accept(MockHttpSecuritySupportingManagedObjectDependencies.AUTHENTICATION,
				context.authentication);
		assertDependency.accept(MockHttpSecuritySupportingManagedObjectDependencies.HTTP_AUTHENTICATION,
				context.httpAuthentication);
		assertDependency.accept(MockHttpSecuritySupportingManagedObjectDependencies.ACCESS_CONTROL,
				context.accessControl);
		assertDependency.accept(MockHttpSecuritySupportingManagedObjectDependencies.HTTP_ACCESS_CONTROL,
				context.httpAccessControl);
		assertDependency.accept(MockHttpSecuritySupportingManagedObjectDependencies.DEPENDENCY, context.dependency);
	}

	public static enum MockHttpSecuritySupportingManagedObjectDependencies {
		AUTHENTICATION, HTTP_AUTHENTICATION, ACCESS_CONTROL, HTTP_ACCESS_CONTROL, DEPENDENCY
	}

	@TestSource
	public static class MockHttpSecuritySupportingManagedObjectSource
			extends AbstractManagedObjectSource<MockHttpSecuritySupportingManagedObjectDependencies, None>
			implements ManagedObject {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<MockHttpSecuritySupportingManagedObjectDependencies, None> context)
				throws Exception {
			context.setObjectClass(this.getClass());
			context.addDependency(MockHttpSecuritySupportingManagedObjectDependencies.AUTHENTICATION,
					MockAuthentication.class);
			context.addDependency(MockHttpSecuritySupportingManagedObjectDependencies.HTTP_AUTHENTICATION,
					HttpAuthentication.class);
			context.addDependency(MockHttpSecuritySupportingManagedObjectDependencies.ACCESS_CONTROL,
					MockAccessControl.class);
			context.addDependency(MockHttpSecuritySupportingManagedObjectDependencies.HTTP_ACCESS_CONTROL,
					HttpAccessControl.class);
			context.addDependency(MockHttpSecuritySupportingManagedObjectDependencies.DEPENDENCY,
					MockSupportingObject.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	private class MockHttpSecuritySupportingManagedObjectDependencyContext
			implements HttpSecuritySupportingManagedObjectDependencyContext {

		private final OfficeManagedObject authentication = LoadHttpSecurityTypeTest.this
				.createMock(OfficeManagedObject.class);

		private final OfficeManagedObject httpAuthentication = LoadHttpSecurityTypeTest.this
				.createMock(OfficeManagedObject.class);

		private final OfficeManagedObject accessControl = LoadHttpSecurityTypeTest.this
				.createMock(OfficeManagedObject.class);

		private final OfficeManagedObject httpAccessControl = LoadHttpSecurityTypeTest.this
				.createMock(OfficeManagedObject.class);

		private final OfficeManagedObject dependency = LoadHttpSecurityTypeTest.this
				.createMock(OfficeManagedObject.class);

		private final HttpSecuritySupportingManagedObject<?> dependencySupportingObject;

		private MockHttpSecuritySupportingManagedObjectDependencyContext(
				HttpSecuritySupportingManagedObject<?> dependencySupportingObject) {
			this.dependencySupportingObject = dependencySupportingObject;
		}

		@Override
		public OfficeManagedObject getAuthentication() {
			return this.authentication;
		}

		@Override
		public OfficeManagedObject getHttpAuthentication() {
			return this.httpAuthentication;
		}

		@Override
		public OfficeManagedObject getAccessControl() {
			return this.accessControl;
		}

		@Override
		public OfficeManagedObject getHttpAccessControl() {
			return this.httpAccessControl;
		}

		@Override
		public OfficeManagedObject getSupportingManagedObject(
				HttpSecuritySupportingManagedObject<?> supportingManagedObject) {
			assertSame("Incorrect supporting object", this.dependencySupportingObject, supportingManagedObject);
			return this.dependency;
		}
	}

	/**
	 * Two key {@link Enum}.
	 */
	private enum TwoKey {
		ONE, TWO
	}

	/**
	 * Invalid key {@link Enum}.
	 */
	private enum InvalidKey {
		INVALID
	}

	/**
	 * Records obtaining the authentication, access control and credentials class
	 * from the {@link HttpSecuritySourceMetaData}.
	 */
	private void record_authenticationAccessControlAndCredentialsClass() {
		this.recordReturn(this.metaData, this.metaData.getAuthenticationType(), HttpAuthentication.class);
		this.recordReturn(this.metaData, this.metaData.getAccessControlType(), HttpAccessControl.class);
		this.recordReturn(this.metaData, this.metaData.getCredentialsType(), HttpCredentials.class);
	}

	/**
	 * Records obtaining simple meta-data from the
	 * {@link HttpSecuritySourceMetaData}.
	 * 
	 * @param flowKeys Flow keys to be defined in meta-data. Provide
	 *                 <code>null</code> values for indexing.
	 */
	@SuppressWarnings("unchecked")
	private <F extends Enum<?>> void record_basicMetaData(F... flowKeys) {
		// Return no dependencies
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(), null);

		// Record the flows
		if (flowKeys.length == 0) {
			// Record no flows
			this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		} else {
			// Create the meta-data for each flow
			HttpSecurityFlowMetaData<?>[] flowMetaDatas = new HttpSecurityFlowMetaData[flowKeys.length];
			for (int i = 0; i < flowMetaDatas.length; i++) {
				flowMetaDatas[i] = this.createMock(HttpSecurityFlowMetaData.class);
			}

			// Record the flow meta-data
			this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), flowMetaDatas);
			for (int i = 0; i < flowMetaDatas.length; i++) {
				HttpSecurityFlowMetaData<?> flowMetaData = flowMetaDatas[i];
				this.recordReturn(flowMetaData, flowMetaData.getLabel(), null);
				this.recordReturn(flowMetaData, flowMetaData.getKey(), flowKeys[i]);
				this.recordReturn(flowMetaData, flowMetaData.getArgumentType(), Integer.class);
			}
		}

		// Record obtaining the authentication, access control and credentials
		this.record_authenticationAccessControlAndCredentialsClass();
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.recordIssue(issueDescription);
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription Description of the issue.
	 * @param cause            Cause of the issue.
	 */
	private void record_issue(String issueDescription, Throwable cause) {
		this.issues.recordIssue(issueDescription, cause);
	}

	/**
	 * Loads the {@link HttpSecurityType}.
	 * 
	 * @param isExpectedToLoad       Flag indicating if expecting to load the
	 *                               {@link FunctionNamespaceType}.
	 * @param init                   {@link Init}.
	 * @param propertyNameValuePairs {@link Property} name value pairs.
	 * @return Loaded {@link HttpSecurityType}.
	 */
	@SuppressWarnings("rawtypes")
	public HttpSecurityType<?, ?, ?, ?, ?> loadHttpSecurityType(boolean isExpectedToLoad, Init init,
			String... propertyNameValuePairs) {

		// Replay mock objects
		this.replayMockObjects();

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the HTTP security loader and load the HTTP security type
		HttpSecurityType securityType;
		try {
			OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
			compiler.setCompilerIssues(this.issues);
			HttpSecurityLoader securityLoader = HttpSecurityArchitectEmployer.employHttpSecurityLoader(compiler);
			MockHttpSecuritySource.init = init;
			securityType = securityLoader.loadHttpSecurityType(MockHttpSecuritySource.class, propertyList);
		} catch (Exception ex) {
			throw fail(ex);
		}

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the HTTP security type", securityType);
		} else {
			assertNull("Should not load the HTTP security type", securityType);
		}

		// Return the HTTP security type
		return securityType;
	}

	/**
	 * Implement to initialise the {@link MockHttpSecuritySource}.
	 */
	private static interface Init {

		/**
		 * Implemented to init the {@link HttpSecuritySource}.
		 * 
		 * @param context {@link HttpSecuritySourceContext}.
		 * @param util    {@link InitUtil}.
		 */
		void init(HttpSecuritySourceContext context);
	}

	/**
	 * Mock {@link HttpSecuritySource}.
	 */
	@TestSource
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class MockHttpSecuritySource implements
			HttpSecuritySource<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, None, None>,
			HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, None, None> {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * {@link Init} to init the {@link HttpSecuritySource}.
		 */
		public static Init init = null;

		/**
		 * Failure to obtain the {@link RegisteredHttpSecurity}.
		 */
		public static Error metaDataFailure = null;

		/**
		 * {@link HttpSecuritySourceSpecification}.
		 */
		public static HttpSecuritySourceMetaData metaData;

		/**
		 * Resets the state for next test.
		 * 
		 * @param metaData {@link HttpSecuritySourceMetaData}.
		 * @param initUtil {@link InitUtil}.
		 */
		public static void reset(HttpSecuritySourceMetaData<?, ?, ?, ?, ?> metaData) {
			instantiateFailure = null;
			init = null;
			metaDataFailure = null;
			MockHttpSecuritySource.metaData = metaData;
		}

		/**
		 * Initiate with possible failure.
		 */
		public MockHttpSecuritySource() {
			// Throw instantiate failure
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================== HttpSecuritySource ===========================
		 */

		@Override
		public HttpSecuritySourceSpecification getSpecification() {
			fail("Should not obtain specification");
			return null;
		}

		@Override
		public HttpSecuritySourceMetaData init(HttpSecuritySourceContext context) throws Exception {

			// Run the init if available
			if (init != null) {
				init.init(context);
			}

			// Throw meta-data failure
			if (metaDataFailure != null) {
				throw metaDataFailure;
			}

			// Return the meta-data
			return metaData;
		}

		@Override
		public void start(HttpSecurityExecuteContext<None> context) throws Exception {
			// Should propagate to ManagedObjectSource
		}

		@Override
		public HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, None, None> sourceHttpSecurity(
				HttpSecurityContext context) throws HttpException {
			return this;
		}

		@Override
		public void stop() {
			// Nothing to stop
		}

		/*
		 * =================== HttpSecurity ==================================
		 */

		@Override
		public HttpAuthentication<HttpCredentials> createAuthentication(
				AuthenticationContext<HttpAccessControl, HttpCredentials> context) {
			fail("Should not be invoked for loading type");
			return null;
		}

		@Override
		public boolean ratify(HttpCredentials credentials, RatifyContext<HttpAccessControl> context) {
			fail("Should not be invoked for loading type");
			return false;
		}

		@Override
		public void authenticate(HttpCredentials credentials,
				AuthenticateContext<HttpAccessControl, None, None> context) {
			fail("Should not be invoked for loading type");
		}

		@Override
		public void challenge(ChallengeContext<None, None> context) {
			fail("Should not be invoked for loading type");
		}

		@Override
		public void logout(LogoutContext<None, None> context) {
			fail("Should not be invoked for loading type");
		}
	}

}
