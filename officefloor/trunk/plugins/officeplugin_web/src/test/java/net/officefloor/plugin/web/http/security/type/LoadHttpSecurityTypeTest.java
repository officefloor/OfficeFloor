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
package net.officefloor.plugin.web.http.security.type;

import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.security.HttpAuthenticateContext;
import net.officefloor.plugin.web.http.security.HttpChallengeContext;
import net.officefloor.plugin.web.http.security.HttpCredentials;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecurityDependencyMetaData;
import net.officefloor.plugin.web.http.security.HttpSecurityFlowMetaData;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceContext;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceMetaData;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceSpecification;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Tests loading the {@link HttpSecurityType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadHttpSecurityTypeTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	/**
	 * {@link HttpSecuritySourceMetaData}.
	 */
	private final HttpSecuritySourceMetaData<?, ?, ?, ?> metaData = this
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
		this.record_issue("Missing property 'missing'");

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
				assertEquals("Ensure get defaulted property", "DEFAULT",
						context.getProperty("missing", "DEFAULT"));
				assertEquals("Ensure get property ONE", "1",
						context.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2",
						context.getProperty("TWO"));
				Properties properties = context.getProperties();
				assertEquals("Incorrect number of properties", 2,
						properties.size());
				assertEquals("Incorrect property ONE", "1",
						properties.get("ONE"));
				assertEquals("Incorrect property TWO", "2",
						properties.get("TWO"));
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
		final String objectPath = Object.class.getName().replace('.', '/')
				+ ".class";

		// Attempt to load
		this.loadHttpSecurityType(true, new Init() {
			@Override
			public void init(HttpSecuritySourceContext context) {
				assertEquals("Incorrect resource locator",
						LoadHttpSecurityTypeTest.class.getClassLoader()
								.getResource(objectPath), context
								.getClassLoader().getResource(objectPath));
			}
		});
	}

	/**
	 * Ensure issue if fails to init the {@link HttpSecuritySource}.
	 */
	public void testFailInitHttpSecuritySource() {

		final NullPointerException failure = new NullPointerException(
				"Fail init HttpSecuritySource");

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
		this.record_issue("Failed to get ManagedObjectSourceMetaData", failure);

		// Attempt to load
		this.loadHttpSecurityType(false, new Init() {
			@Override
			public void init(HttpSecuritySourceContext context) {
				MockHttpSecuritySource.metaDataFailure = failure;
			}
		});
	}

	/**
	 * Ensure issue if no security class from meta-data.
	 */
	public void testNoSecurityClass() {

		// Record no object class
		this.recordReturn(this.metaData, this.metaData.getSecurityClass(), null);
		this.record_issue("No Object type provided");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link HttpSecurityDependencyMetaData}
	 * in array.
	 */
	public void testNullDependencyMetaData() {

		// Record no dependency type
		this.record_securityClass();
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

		final HttpSecurityDependencyMetaData<?> dependency = this
				.createMock(HttpSecurityDependencyMetaData.class);

		// Record no dependency type
		this.record_securityClass();
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

		final HttpSecurityDependencyMetaData<?> dependencyOne = this
				.createMock(HttpSecurityDependencyMetaData.class);
		final HttpSecurityDependencyMetaData<?> dependencyTwo = this
				.createMock(HttpSecurityDependencyMetaData.class);

		// Record missing dependency key
		this.record_securityClass();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new HttpSecurityDependencyMetaData[] { dependencyOne,
						dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyOne, dependencyOne.getType(),
				Connection.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(),
				InvalidKey.INVALID);
		this.record_issue("Dependencies identified by different key types ("
				+ TwoKey.class.getName() + ", " + InvalidKey.class.getName()
				+ ")");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if dependency mixing using keys and indexes.
	 */
	public void testDependencyMixingKeyAndIndexes() {

		final HttpSecurityDependencyMetaData<?> dependencyOne = this
				.createMock(HttpSecurityDependencyMetaData.class);
		final HttpSecurityDependencyMetaData<?> dependencyTwo = this
				.createMock(HttpSecurityDependencyMetaData.class);

		// Record missing dependency key
		this.record_securityClass();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new HttpSecurityDependencyMetaData[] { dependencyOne,
						dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyOne, dependencyOne.getType(),
				Connection.class);
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

		final HttpSecurityDependencyMetaData<?> dependencyOne = this
				.createMock(HttpSecurityDependencyMetaData.class);
		final HttpSecurityDependencyMetaData<?> dependencyTwo = this
				.createMock(HttpSecurityDependencyMetaData.class);

		// Record missing dependency key
		this.record_securityClass();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new HttpSecurityDependencyMetaData[] { dependencyOne,
						dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyOne, dependencyOne.getType(),
				Connection.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), TwoKey.ONE);
		this.recordReturn(dependencyTwo, dependencyTwo.getType(), String.class);
		this.recordReturn(dependencyTwo, dependencyTwo.getTypeQualifier(), null);
		this.record_issue("Must have exactly one dependency per key (key="
				+ TwoKey.ONE + ")");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if not have meta-data for each dependency key.
	 */
	public void testNotAllDependencyKeys() {

		final HttpSecurityDependencyMetaData<?> dependency = this
				.createMock(HttpSecurityDependencyMetaData.class);

		// Record not all dependency keys
		this.record_securityClass();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new HttpSecurityDependencyMetaData[] { dependency });
		this.recordReturn(dependency, dependency.getLabel(), null);
		this.recordReturn(dependency, dependency.getKey(), TwoKey.ONE);
		this.recordReturn(dependency, dependency.getType(), Connection.class);
		this.recordReturn(dependency, dependency.getTypeQualifier(), null);
		this.record_issue("Missing dependency meta-data (keys=" + TwoKey.TWO
				+ ")");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link HttpSecurityFlowMetaData} in
	 * array.
	 */
	public void testNullFlowMetaData() {

		// Record no flow type
		this.record_securityClass();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new HttpSecurityFlowMetaData[] { null });
		this.record_issue("Null ManagedObjectFlowMetaData for flow 0");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure able to default the flow argument type.
	 */
	public void testDefaultFlowArgumentType() {

		final HttpSecurityFlowMetaData<?> flowDefaulted = this
				.createMock(HttpSecurityFlowMetaData.class);
		final HttpSecurityFlowMetaData<?> flowProvided = this
				.createMock(HttpSecurityFlowMetaData.class);

		// Record no flow argument type
		this.record_securityClass();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new HttpSecurityFlowMetaData[] { flowDefaulted, flowProvided });
		this.recordReturn(flowDefaulted, flowDefaulted.getLabel(), "DEFAULTED");
		this.recordReturn(flowDefaulted, flowDefaulted.getKey(), null);
		this.recordReturn(flowDefaulted, flowDefaulted.getArgumentType(), null);
		this.recordReturn(flowProvided, flowProvided.getLabel(), null);
		this.recordReturn(flowProvided, flowProvided.getKey(), null);
		this.recordReturn(flowProvided, flowProvided.getArgumentType(),
				Connection.class);
		this.record_credentialsClass();

		// Attempt to load
		HttpSecurityType<?, ?, ?, ?> securityType = this.loadHttpSecurityType(
				true, null);

		// Validate argument types of flows
		HttpSecurityFlowType<?>[] flowTypes = securityType.getFlowTypes();
		assertEquals("Incorrect number of flows", 2, flowTypes.length);
		HttpSecurityFlowType<?> defaulted = flowTypes[0];
		assertEquals("Incorrect name for defaulted argument flow", "DEFAULTED",
				defaulted.getFlowName());
		assertEquals("Incorrect defaulted argument type", Void.class,
				defaulted.getArgumentType());
		HttpSecurityFlowType<?> provided = flowTypes[1];
		assertEquals("Incorrect name for provided argument flow", "1",
				provided.getFlowName());
		assertEquals("Incorrect provided argument type", Connection.class,
				provided.getArgumentType());
	}

	/**
	 * Ensure issue if flow keys of different types.
	 */
	public void testInvalidFlowKey() {

		final HttpSecurityFlowMetaData<?> flowOne = this
				.createMock(HttpSecurityFlowMetaData.class);
		final HttpSecurityFlowMetaData<?> flowTwo = this
				.createMock(HttpSecurityFlowMetaData.class);

		// Record missing flow key
		this.record_securityClass();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new HttpSecurityFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.ONE);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Connection.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), InvalidKey.INVALID);
		this.record_issue("Meta-data flows identified by different key types ("
				+ TwoKey.class.getName() + ", " + InvalidKey.class.getName()
				+ ")");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if flow mixing using keys and indexes.
	 */
	public void testFlowMixingKeyAndIndexes() {

		final HttpSecurityFlowMetaData<?> flowOne = this
				.createMock(HttpSecurityFlowMetaData.class);
		final HttpSecurityFlowMetaData<?> flowTwo = this
				.createMock(HttpSecurityFlowMetaData.class);

		// Record missing flow key
		this.record_securityClass();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
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

		final HttpSecurityFlowMetaData<?> flowOne = this
				.createMock(HttpSecurityFlowMetaData.class);
		final HttpSecurityFlowMetaData<?> flowTwo = this
				.createMock(HttpSecurityFlowMetaData.class);

		// Record missing flow key
		this.record_securityClass();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new HttpSecurityFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.ONE);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Connection.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), TwoKey.ONE);
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.record_issue("Must have exactly one flow per key (key="
				+ TwoKey.ONE + ")");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure issue if not have meta-data for each flow key.
	 */
	public void testNotAllFlowKeys() {

		final HttpSecurityFlowMetaData<?> flow = this
				.createMock(HttpSecurityFlowMetaData.class);

		// Record not all flow keys
		this.record_securityClass();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new HttpSecurityFlowMetaData[] { flow });
		this.recordReturn(flow, flow.getLabel(), null);
		this.recordReturn(flow, flow.getKey(), TwoKey.ONE);
		this.recordReturn(flow, flow.getArgumentType(), Connection.class);
		this.record_issue("Missing flow meta-data (keys=" + TwoKey.TWO + ")");

		// Attempt to load
		this.loadHttpSecurityType(false, null);
	}

	/**
	 * Ensure {@link HttpSecurityType} correct with keyed dependencies and
	 * flows.
	 */
	public void testKeyedDependenciesAndFlows() {

		final HttpSecurityDependencyMetaData<?> dependencyOne = this
				.createMock(HttpSecurityDependencyMetaData.class);
		final HttpSecurityDependencyMetaData<?> dependencyTwo = this
				.createMock(HttpSecurityDependencyMetaData.class);
		final HttpSecurityFlowMetaData<?> flowOne = this
				.createMock(HttpSecurityFlowMetaData.class);
		final HttpSecurityFlowMetaData<?> flowTwo = this
				.createMock(HttpSecurityFlowMetaData.class);

		// Record keyed dependencies and flows
		this.record_securityClass();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new HttpSecurityDependencyMetaData[] { dependencyOne,
						dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), TwoKey.TWO); // order
		this.recordReturn(dependencyOne, dependencyOne.getType(), Integer.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(),
				"QUALIFIED");
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), TwoKey.ONE); // order
		this.recordReturn(dependencyTwo, dependencyTwo.getType(),
				Connection.class);
		this.recordReturn(dependencyTwo, dependencyTwo.getTypeQualifier(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new HttpSecurityFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.TWO); // order
		this.recordReturn(flowOne, flowOne.getArgumentType(), Long.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), TwoKey.ONE); // order
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.record_credentialsClass();

		// Attempt to load
		HttpSecurityType<?, ?, ?, ?> securityType = this.loadHttpSecurityType(
				true, null);

		// Validate dependencies ordered and correct values
		HttpSecurityDependencyType<?>[] dependencyTypes = securityType
				.getDependencyTypes();
		assertEquals("Incorrect number of dependencies", 2,
				dependencyTypes.length);
		HttpSecurityDependencyType<?> dependencyTypeOne = dependencyTypes[0];
		assertEquals("Keys should be ordered", TwoKey.ONE,
				dependencyTypeOne.getKey());
		assertEquals("Incorrect first dependency index", TwoKey.ONE.ordinal(),
				dependencyTypeOne.getIndex());
		assertEquals("Incorrect first dependency name", TwoKey.ONE.toString(),
				dependencyTypeOne.getDependencyName());
		assertEquals("Incorrect first dependency type", Connection.class,
				dependencyTypeOne.getDependencyType());
		assertNull("First dependency type should not be qualified",
				dependencyTypeOne.getTypeQualifier());
		HttpSecurityDependencyType<?> dependencyTypeTwo = dependencyTypes[1];
		assertEquals("Keys should be ordered", TwoKey.TWO,
				dependencyTypeTwo.getKey());
		assertEquals("Incorrect second dependency index", TwoKey.TWO.ordinal(),
				dependencyTypeTwo.getIndex());
		assertEquals("Incorrect second dependency name", TwoKey.TWO.toString(),
				dependencyTypeTwo.getDependencyName());
		assertEquals("Incorrect second dependency type", Integer.class,
				dependencyTypeTwo.getDependencyType());
		assertEquals("Incorrect second dependency type qualification",
				"QUALIFIED", dependencyTypeTwo.getTypeQualifier());

		// Validate flows ordered and correct values
		HttpSecurityFlowType<?>[] flowTypes = securityType.getFlowTypes();
		assertEquals("Incorrect number of dependencies", 2, flowTypes.length);
		HttpSecurityFlowType<?> flowTypeOne = flowTypes[0];
		assertEquals("Keys should be ordered", TwoKey.ONE, flowTypeOne.getKey());
		assertEquals("Incorrect first flow index", TwoKey.ONE.ordinal(),
				flowTypeOne.getIndex());
		assertEquals("Incorrect first flow name", TwoKey.ONE.toString(),
				flowTypeOne.getFlowName());
		assertEquals("Incorrect first flow argument type", String.class,
				flowTypeOne.getArgumentType());
		HttpSecurityFlowType<?> flowTypeTwo = flowTypes[1];
		assertEquals("Keys should be ordered", TwoKey.TWO, flowTypeTwo.getKey());
		assertEquals("Incorrect second flow index", TwoKey.TWO.ordinal(),
				flowTypeTwo.getIndex());
		assertEquals("Incorrect second flow name", TwoKey.TWO.toString(),
				flowTypeTwo.getFlowName());
		assertEquals("Incorrect second flow argument type", Long.class,
				flowTypeTwo.getArgumentType());
	}

	/**
	 * Ensure {@link HttpSecurityType} correct with indexed dependencies and
	 * flows.
	 */
	public void testIndexedDependenciesAndFlows() {

		final HttpSecurityDependencyMetaData<?> dependencyOne = this
				.createMock(HttpSecurityDependencyMetaData.class);
		final HttpSecurityDependencyMetaData<?> dependencyTwo = this
				.createMock(HttpSecurityDependencyMetaData.class);
		final HttpSecurityFlowMetaData<?> flowOne = this
				.createMock(HttpSecurityFlowMetaData.class);
		final HttpSecurityFlowMetaData<?> flowTwo = this
				.createMock(HttpSecurityFlowMetaData.class);

		// Record keyed dependencies and flows
		this.record_securityClass();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				new HttpSecurityDependencyMetaData[] { dependencyOne,
						dependencyTwo });
		this.recordReturn(dependencyOne, dependencyOne.getLabel(), null);
		this.recordReturn(dependencyOne, dependencyOne.getKey(), null);
		this.recordReturn(dependencyOne, dependencyOne.getType(), Integer.class);
		this.recordReturn(dependencyOne, dependencyOne.getTypeQualifier(),
				"QUALIFIED");
		this.recordReturn(dependencyTwo, dependencyTwo.getLabel(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getKey(), null);
		this.recordReturn(dependencyTwo, dependencyTwo.getType(),
				Connection.class);
		this.recordReturn(dependencyTwo, dependencyTwo.getTypeQualifier(), null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new HttpSecurityFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), null);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Long.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), null);
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.record_credentialsClass();

		// Attempt to load
		HttpSecurityType<?, ?, ?, ?> securityType = this.loadHttpSecurityType(
				true, null);

		// Validate dependencies
		HttpSecurityDependencyType<?>[] dependencyTypes = securityType
				.getDependencyTypes();
		assertEquals("Incorrect number of dependencies", 2,
				dependencyTypes.length);
		HttpSecurityDependencyType<?> dependencyTypeOne = dependencyTypes[0];
		assertEquals("Incorrect first dependency index", 0,
				dependencyTypeOne.getIndex());
		assertNull("Should be no dependency key", dependencyTypeOne.getKey());
		assertEquals("Incorrect first dependency name",
				Integer.class.getSimpleName(),
				dependencyTypeOne.getDependencyName());
		assertEquals("Incorrect first dependency type", Integer.class,
				dependencyTypeOne.getDependencyType());
		assertEquals("Incorrect first dependency type qualification",
				"QUALIFIED", dependencyTypeOne.getTypeQualifier());
		HttpSecurityDependencyType<?> dependencyTypeTwo = dependencyTypes[1];
		assertEquals("Incorrect second dependency index", 1,
				dependencyTypeTwo.getIndex());
		assertNull("Should be no dependency key", dependencyTypeTwo.getKey());
		assertEquals("Incorrect second dependency name",
				Connection.class.getSimpleName(),
				dependencyTypeTwo.getDependencyName());
		assertEquals("Incorrect second dependency type", Connection.class,
				dependencyTypeTwo.getDependencyType());
		assertNull("Second dependency should not be qualified",
				dependencyTypeTwo.getTypeQualifier());

		// Validate flows
		HttpSecurityFlowType<?>[] flowTypes = securityType.getFlowTypes();
		assertEquals("Incorrect number of dependencies", 2, flowTypes.length);
		HttpSecurityFlowType<?> flowTypeOne = flowTypes[0];
		assertEquals("Incorrect first flow index", 0, flowTypeOne.getIndex());
		assertNull("Should be no flow key", flowTypeOne.getKey());
		assertEquals("Incorrect first flow name", "0",
				flowTypeOne.getFlowName());
		assertEquals("Incorrect first flow argument type", Long.class,
				flowTypeOne.getArgumentType());
		HttpSecurityFlowType<?> flowTypeTwo = flowTypes[1];
		assertEquals("Incorrect second flow index", 1, flowTypeTwo.getIndex());
		assertNull("Should be no dependency key", flowTypeTwo.getKey());
		assertEquals("Incorrect second flow name", "1",
				flowTypeTwo.getFlowName());
		assertEquals("Incorrect second flow argument type", String.class,
				flowTypeTwo.getArgumentType());
	}

	/**
	 * Ensure obtain appropriate security and credentials.
	 */
	public void testSecurityAndCredentials() {
		this.record_basicMetaData();

		// Load the security type
		HttpSecurityType<?, ?, ?, ?> securityType = this.loadHttpSecurityType(
				true, null);

		// Ensure correct security and credentials
		assertEquals("Incorrect security type", HttpSecurity.class,
				securityType.getSecurityClass());
		assertEquals("Incorrect credentials type", HttpCredentials.class,
				securityType.getCredentialsClass());
	}

	/**
	 * Ensure may have <code>null</code> (no) credentials required from the
	 * application specific behaviour.
	 */
	public void testNullCredentials() {
		this.record_securityClass();
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.recordReturn(this.metaData, this.metaData.getCredentialsClass(),
				null);

		// Load the security type
		HttpSecurityType<?, ?, ?, ?> securityType = this.loadHttpSecurityType(
				true, null);

		// Ensure correct security and no credentials
		assertEquals("Incorrect security type", HttpSecurity.class,
				securityType.getSecurityClass());
		assertNull("Should be no credentials required",
				securityType.getCredentialsClass());
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
	 * Records obtaining the security class from the
	 * {@link HttpSecuritySourceMetaData}.
	 */
	private void record_securityClass() {
		this.recordReturn(this.metaData, this.metaData.getSecurityClass(),
				HttpSecurity.class);
	}

	/**
	 * Records obtaining the credentials class from the
	 * {@link HttpSecuritySourceMetaData}.
	 */
	private void record_credentialsClass() {
		this.recordReturn(this.metaData, this.metaData.getCredentialsClass(),
				HttpCredentials.class);
	}

	/**
	 * Records obtaining simple meta-data from the
	 * {@link HttpSecuritySourceMetaData}.
	 * 
	 * @param flowKeys
	 *            Flow keys to be defined in meta-data. Provide
	 *            <code>null</code> values for indexing.
	 */
	private <F extends Enum<?>> void record_basicMetaData(F... flowKeys) {
		this.record_securityClass();

		// Return no dependencies
		this.recordReturn(this.metaData, this.metaData.getDependencyMetaData(),
				null);

		// Record the flows
		if (flowKeys.length == 0) {
			// Record no flows
			this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
					null);
		} else {
			// Create the meta-data for each flow
			HttpSecurityFlowMetaData<?>[] flowMetaDatas = new HttpSecurityFlowMetaData[flowKeys.length];
			for (int i = 0; i < flowMetaDatas.length; i++) {
				flowMetaDatas[i] = this
						.createMock(HttpSecurityFlowMetaData.class);
			}

			// Record the flow meta-data
			this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
					flowMetaDatas);
			for (int i = 0; i < flowMetaDatas.length; i++) {
				HttpSecurityFlowMetaData<?> flowMetaData = flowMetaDatas[i];
				this.recordReturn(flowMetaData, flowMetaData.getLabel(), null);
				this.recordReturn(flowMetaData, flowMetaData.getKey(),
						flowKeys[i]);
				this.recordReturn(flowMetaData, flowMetaData.getArgumentType(),
						Integer.class);
			}
		}

		// Record obtaining the credentials
		this.record_credentialsClass();
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(null, null, AssetType.MANAGED_OBJECT, null,
				issueDescription);
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 */
	private void record_issue(String issueDescription, Throwable cause) {
		this.issues.addIssue(null, null, AssetType.MANAGED_OBJECT, null,
				issueDescription, cause);
	}

	/**
	 * Loads the {@link HttpSecurityType}.
	 * 
	 * @param isExpectedToLoad
	 *            Flag indicating if expecting to load the {@link WorkType}.
	 * @param init
	 *            {@link Init}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return Loaded {@link HttpSecurityType}.
	 */
	public HttpSecurityType<?, ?, ?, ?> loadHttpSecurityType(
			boolean isExpectedToLoad, Init init,
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
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		ManagedObjectLoader managedObjectLoader = compiler
				.getManagedObjectLoader();
		HttpSecurityLoader securityLoader = new HttpSecurityLoaderImpl(
				managedObjectLoader);
		MockHttpSecuritySource.init = init;
		HttpSecurityType<?, ?, ?, ?> securityType = securityLoader
				.loadHttpSecurityType(new MockHttpSecuritySource(),
						propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the HTTP security type",
					securityType);
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
		 * @param context
		 *            {@link HttpSecuritySourceContext}.
		 * @param util
		 *            {@link InitUtil}.
		 */
		void init(HttpSecuritySourceContext context);
	}

	/**
	 * Mock {@link HttpSecuritySource}.
	 */
	@TestSource
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class MockHttpSecuritySource implements
			HttpSecuritySource<HttpSecurity, HttpCredentials, None, None> {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * {@link Init} to init the {@link HttpSecuritySource}.
		 */
		public static Init init = null;

		/**
		 * Failure to obtain the {@link HttpSecurityMetaData}.
		 */
		public static Error metaDataFailure = null;

		/**
		 * {@link HttpSecuritySourceSpecification}.
		 */
		public static HttpSecuritySourceMetaData metaData;

		/**
		 * Resets the state for next test.
		 * 
		 * @param metaData
		 *            {@link HttpSecuritySourceMetaData}.
		 * @param initUtil
		 *            {@link InitUtil}.
		 */
		public static void reset(HttpSecuritySourceMetaData<?, ?, ?, ?> metaData) {
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
		public void init(HttpSecuritySourceContext context) throws Exception {
			// Run the init if available
			if (init != null) {
				init.init(context);
			}
		}

		@Override
		public HttpSecuritySourceMetaData getMetaData() {

			// Throw meta-data failure
			if (metaDataFailure != null) {
				throw metaDataFailure;
			}

			// Return the meta-data
			return metaData;
		}

		@Override
		public void challenge(HttpChallengeContext<None, None> context)
				throws IOException {
			fail("Should not be invoked for loading type");
		}

		@Override
		public HttpSecurity retrieveCached(HttpSession session) {
			fail("Should not be invoked for loading type");
			return null;
		}

		@Override
		public void authenticate(
				HttpAuthenticateContext<HttpSecurity, HttpCredentials, None> context) {
			fail("Should not be invoked for loading type");
		}
	}

}