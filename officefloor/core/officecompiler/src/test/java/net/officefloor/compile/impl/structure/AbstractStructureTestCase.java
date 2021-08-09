/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.structure;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkOfficeNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.administration.source.impl.AbstractAdministrationSource;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionEscalationTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceSpecification;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.OfficeSourceSpecification;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceSpecification;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.TeamSourceSpecification;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Abstract functionality for testing loading the {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractStructureTestCase extends OfficeFrameTestCase {

	/**
	 * Location of the top level {@link OfficeSection}.
	 */
	protected static final String SECTION_LOCATION = "SECTION_LOCATION";

	/**
	 * {@link ResourceSource}.
	 */
	protected final ResourceSource resourceSource = this.createMock(ResourceSource.class);

	/**
	 * {@link ClassLoader}.
	 */
	protected final ClassLoader classLoader = this.getClass().getClassLoader();

	/**
	 * {@link CompilerIssues}.
	 */
	protected final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link NodeContext}.
	 */
	protected final NodeContext nodeContext;

	/**
	 * Initiate.
	 */
	public AbstractStructureTestCase() {
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(this.classLoader);
		compiler.setCompilerIssues(this.issues);
		compiler.addResources(this.resourceSource);
		this.nodeContext = (NodeContext) compiler;
	}

	@Override
	protected void setUp() throws Exception {
		MakerSectionSource.reset();
		MakerManagedObjectSource.reset();
		MakerSupplierSource.reset();
		MakerManagedFunctionSource.reset();
		MakerGovernanceSource.reset();
		MakerAdministrationSource.reset();
		MakerTeamSource.reset();
		MakerOfficeSource.reset(this);
		MakerOfficeFloorSource.reset(this);
	}

	/**
	 * Asserts the {@link LinkFlowNode} source is linked to the target
	 * {@link LinkFlowNode}.
	 * 
	 * @param msg        Message.
	 * @param linkSource Source {@link LinkFlowNode}.
	 * @param linkTarget Target {@link LinkFlowNode}.
	 */
	protected static void assertFlowLink(String msg, Object linkSource, Object linkTarget) {
		assertTrue(msg + ": source must be " + LinkFlowNode.class.getSimpleName(), linkSource instanceof LinkFlowNode);
		assertEquals(msg, linkTarget, ((LinkFlowNode) linkSource).getLinkedFlowNode());
	}

	/**
	 * Asserts the {@link LinkObjectNode} source is linked to the target
	 * {@link LinkObjectNode}.
	 * 
	 * @param msg        Message.
	 * @param linkSource Source {@link LinkObjectNode}.
	 * @param linkTarget Target {@link LinkObjectNode}.
	 */
	protected static void assertObjectLink(String msg, Object linkSource, Object linkTarget) {
		assertTrue(msg + ": source must be " + LinkObjectNode.class.getSimpleName(),
				linkSource instanceof LinkObjectNode);
		assertEquals(msg, linkTarget, ((LinkObjectNode) linkSource).getLinkedObjectNode());
	}

	/**
	 * Asserts the {@link LinkTeamNode} source is linked to the target
	 * {@link LinkTeamNode}.
	 * 
	 * @param msg        Message.
	 * @param linkSource Source {@link LinkTeamNode}.
	 * @param linkTarget Target {@link LinkTeamNode}.
	 */
	protected static void assertTeamLink(String msg, Object linkSource, Object linkTarget) {
		assertTrue(msg + ": source must be " + LinkTeamNode.class.getSimpleName(), linkSource instanceof LinkTeamNode);
		assertEquals(msg, linkTarget, ((LinkTeamNode) linkSource).getLinkedTeamNode());
	}

	/**
	 * Asserts the {@link LinkOfficeNode} source is linked to the target
	 * {@link LinkOfficeNode}.
	 * 
	 * @param msg        Message.
	 * @param linkSource Source {@link LinkOfficeNode}.
	 * @param linkTarget Target {@link LinkOfficeNode}.
	 */
	protected static void assertOfficeLink(String msg, Object linkSource, Object linkTarget) {
		assertTrue(msg + ": source must be " + LinkOfficeNode.class.getSimpleName(),
				linkSource instanceof LinkOfficeNode);
		assertEquals(msg, linkTarget, ((LinkOfficeNode) linkSource).getLinkedOfficeNode());
	}

	/**
	 * Asserts the {@link ManagedObjectSourceNode} is linked to the
	 * {@link InputManagedObjectNode}.
	 * 
	 * @param msg                     Message.
	 * @param managedObjectSourceNode {@link ManagedObjectSourceNode}.
	 * @param inputManagedObjectNode  Linked {@link InputManagedObjectNode}.
	 */
	protected static void assertSourceInput(String msg, Object managedObjectSourceNode, Object inputManagedObjectNode) {
		assertTrue(msg + ": source must be " + ManagedObjectSourceNode.class.getSimpleName(),
				managedObjectSourceNode instanceof ManagedObjectSourceNode);
		assertEquals(msg, inputManagedObjectNode,
				((ManagedObjectSourceNode) managedObjectSourceNode).getInputManagedObjectNode());
	}

	/**
	 * Loads the {@link OfficeSectionType}.
	 * 
	 * @param sectionName Name of the {@link OfficeSection}.
	 * @param maker       {@link SectionMaker} to make the {@link OfficeSection}.
	 * @return Loaded {@link OfficeSectionType}.
	 */
	protected OfficeSectionType loadOfficeSectionType(String sectionName, SectionMaker maker) {
		return this.loadOfficeSectionType(true, sectionName, maker);
	}

	/**
	 * Loads the {@link OfficeSectionType}.
	 * 
	 * @param isHandleMockState <code>true</code> for method to handle mock states.
	 * @param sectionName       Name of the {@link OfficeSection}.
	 * @param maker             {@link SectionMaker} to make the
	 *                          {@link OfficeSection}.
	 * @return Loaded {@link OfficeSectionType}.
	 */
	protected OfficeSectionType loadOfficeSectionType(boolean isHandleMockState, String sectionName,
			SectionMaker maker) {

		// Register the section maker
		PropertyList propertyList = MakerSectionSource.register(maker);

		// Replay the mocks if handling mock state
		if (isHandleMockState) {
			this.replayMockObjects();
		}

		// Load the section
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(this.classLoader);
		compiler.setCompilerIssues(this.issues);
		compiler.addResources(this.resourceSource);
		SectionLoader loader = compiler.getSectionLoader();
		OfficeSectionType sectionType = loader.loadOfficeSectionType(sectionName, MakerSectionSource.class,
				SECTION_LOCATION, propertyList);

		// Verify the mocks if handling mock state
		if (isHandleMockState) {
			this.verifyMockObjects();
		}

		// Return the section type
		return sectionType;
	}

	/**
	 * Adds the {@link OfficeSection} to the {@link OfficeArchitect}.
	 * 
	 * @param officeArchitect {@link OfficeArchitect}.
	 * @param sectionName     Name of the {@link OfficeSection}.
	 * @param maker           {@link SectionMaker}.
	 * @return Added {@link OfficeSection}.
	 */
	protected OfficeSection addSection(OfficeArchitect officeArchitect, String sectionName, SectionMaker maker) {

		// Register the section maker
		PropertyList propertyList = MakerSectionSource.register(maker);

		// Add and return the section
		OfficeSection section = officeArchitect.addOfficeSection(sectionName, MakerSectionSource.class.getName(),
				sectionName);
		for (Property property : propertyList) {
			section.addProperty(property.getName(), property.getValue());
		}
		return section;
	}

	/**
	 * Adds an {@link OfficeManagedObjectSource} to the {@link OfficeArchitect}.
	 * 
	 * @param officeArchitect         {@link OfficeArchitect}.
	 * @param managedObjectSourceName Name of the {@link OfficeManagedObjectSource}.
	 * @param maker                   {@link ManagedObjectMaker}.
	 * @return {@link OfficeManagedObjectSource}.
	 */
	protected OfficeManagedObjectSource addManagedObjectSource(OfficeArchitect officeArchitect,
			String managedObjectSourceName, ManagedObjectMaker maker) {

		// Register the managed object maker
		PropertyList propertyList = MakerManagedObjectSource.register(maker);

		// Add and return the managed object source
		OfficeManagedObjectSource moSource = officeArchitect.addOfficeManagedObjectSource(managedObjectSourceName,
				MakerManagedObjectSource.class.getName());
		for (Property property : propertyList) {
			moSource.addProperty(property.getName(), property.getValue());
		}
		return moSource;
	}

	/**
	 * Adds an {@link OfficeGovernance} to the {@link OfficeArchitect}.
	 * 
	 * @param officeArchitect {@link OfficeArchitect}.
	 * @param governanceName  Name of the {@link OfficeGovernance}.
	 * @param maker           {@link GovernanceMaker}.
	 * @return {@link OfficeGovernance}.
	 */
	protected OfficeGovernance addGovernance(OfficeArchitect officeArchitect, String governanceName,
			GovernanceMaker maker) {

		// Register the governance maker
		PropertyList propertyList = MakerGovernanceSource.register(maker);

		// Add and return the governance
		OfficeGovernance gov = officeArchitect.addOfficeGovernance(governanceName,
				MakerGovernanceSource.class.getName());
		for (Property property : propertyList) {
			gov.addProperty(property.getName(), property.getValue());
		}
		return gov;
	}

	/**
	 * Adds a simple {@link OfficeAdministration} with the supplied extension
	 * interface, allowing specification of the {@link AdministrationFactory}.
	 * 
	 * @param officeArchitect       {@link OfficeArchitect}.
	 * @param administrationName    Name of the {@link OfficeAdministration}.
	 * @param extensionInterface    Extension interface.
	 * @param administrationFactory {@link AdministrationFactory}.
	 * @param maker                 {@link AdministrationMaker}.
	 * @return {@link OfficeAdministration}.
	 */
	protected OfficeAdministration addAdministration(OfficeArchitect officeArchitect, String administrationName,
			final Class<?> extensionInterface, final AdministrationFactory<?, ?, ?> administrationFactory,
			final AdministrationMaker maker) {

		// Create the administration maker
		AdministrationMaker adminMaker = new AdministrationMaker() {
			@Override
			public void make(AdministrationMakerContext context) {
				// Specify administration details
				context.setAdministrationFactory(administrationFactory);
				context.setExtensionInterface(extensionInterface);

				// Make the rest of administration
				if (maker != null) {
					maker.make(context);
				}
			}
		};

		// Register the administration maker
		PropertyList propertyList = MakerAdministrationSource.register(adminMaker);

		// Add and return the administration
		OfficeAdministration admin = officeArchitect.addOfficeAdministration(administrationName,
				MakerAdministrationSource.class.getName());
		for (Property property : propertyList) {
			admin.addProperty(property.getName(), property.getValue());
		}
		return admin;
	}

	/**
	 * Adds an {@link OfficeFloorTeam} to the {@link OfficeFloorDeployer}.
	 * 
	 * @param officeFloorDeployer {@link OfficeFloorDeployer}.
	 * @param teamName            Name of the {@link OfficeFloorTeam}.
	 * @param maker               {@link TeamMaker}.
	 * @return {@link OfficeFloorTeam}.
	 */
	protected OfficeFloorTeam addTeam(OfficeFloorDeployer officeFloorDeployer, String teamName, TeamMaker maker) {

		// Register the team maker
		PropertyList propertyList = MakerTeamSource.register(maker);

		// Add and return the team
		OfficeFloorTeam team = officeFloorDeployer.addTeam(teamName, MakerTeamSource.class.getName());
		for (Property property : propertyList) {
			team.addProperty(property.getName(), property.getValue());
		}
		return team;
	}

	/**
	 * Adds an {@link OfficeFloorManagedObjectSource} to the
	 * {@link OfficeFloorDeployer}.
	 * 
	 * @param officeFloorDeployer     {@link OfficeFloorDeployer}.
	 * @param managedObjectSourceName Name of the
	 *                                {@link OfficeFloorManagedObjectSource}.
	 * @param maker                   {@link ManagedObjectMaker}.
	 * @return {@link OfficeFloorManagedObjectSource}.
	 */
	protected OfficeFloorManagedObjectSource addManagedObjectSource(OfficeFloorDeployer officeFloorDeployer,
			String managedObjectSourceName, ManagedObjectMaker maker) {

		// Register the managed object maker
		PropertyList propertyList = MakerManagedObjectSource.register(maker);

		// Add and return the managed object source
		OfficeFloorManagedObjectSource moSource = officeFloorDeployer.addManagedObjectSource(managedObjectSourceName,
				MakerManagedObjectSource.class.getName());
		for (Property property : propertyList) {
			moSource.addProperty(property.getName(), property.getValue());
		}
		return moSource;
	}

	/**
	 * Adds an {@link OfficeFloorSupplier} to the {@link OfficeFloorDeployer}.
	 * 
	 * @param officeFloorDeployer {@link OfficeFloorDeployer}.
	 * @param supplierName        Name of the {@link OfficeFloorSupplier}.
	 * @param maker               {@link SupplierMaker}.
	 * @return {@link OfficeFloorSupplier}.
	 */
	protected OfficeFloorSupplier addSupplier(OfficeFloorDeployer officeFloorDeployer, String supplierName,
			SupplierMaker maker) {

		// Register the supplier maker
		PropertyList propertyList = MakerSupplierSource.register(maker);

		// Add and return the supplier
		OfficeFloorSupplier supplier = officeFloorDeployer.addSupplier(supplierName,
				MakerSupplierSource.class.getName());
		for (Property property : propertyList) {
			supplier.addProperty(property.getName(), property.getValue());
		}
		return supplier;
	}

	/**
	 * Adds a {@link DeployedOffice}.
	 * 
	 * @param officeFloorDeployer {@link OfficeFloorDeployer}.
	 * @param officeName          Name of the {@link DeployedOffice}. Also used as
	 *                            location of the {@link DeployedOffice}.
	 * @param maker               {@link OfficeMaker}.
	 * @return {@link DeployedOffice}.
	 */
	protected DeployedOffice addDeployedOffice(OfficeFloorDeployer officeFloorDeployer, String officeName,
			OfficeMaker maker) {

		// Register the office maker
		PropertyList propertyList = MakerOfficeSource.register(maker);

		// Add and return the deployed office
		DeployedOffice office = officeFloorDeployer.addDeployedOffice(officeName, MakerOfficeSource.class.getName(),
				officeName);
		for (Property property : propertyList) {
			office.addProperty(property.getName(), property.getValue());
		}
		return office;
	}

	/**
	 * Makes the {@link SubSection}.
	 */
	@FunctionalInterface
	protected static interface SectionMaker {

		/**
		 * Test logic to make the {@link SubSection}.
		 * 
		 * @param context {@link SectionMakerContext}.
		 */
		void make(SectionMakerContext context);
	}

	/**
	 * Context for the {@link SectionMaker}.
	 */
	protected static interface SectionMakerContext {

		/**
		 * Obtains the {@link SectionDesigner}.
		 * 
		 * @return {@link SectionDesigner}.
		 */
		SectionDesigner getBuilder();

		/**
		 * Obtains the {@link SectionSourceContext}.
		 * 
		 * @return {@link SectionSourceContext}.
		 */
		SectionSourceContext getContext();

		/**
		 * Adds a {@link SubSection}.
		 * 
		 * @param subSectionName Name of the {@link SubSection} which is also used as
		 *                       the {@link SubSection} location.
		 * @param maker          {@link SectionMaker}.
		 * @return Added {@link SubSection}.
		 */
		SubSection addSubSection(String subSectionName, SectionMaker maker);

		/**
		 * Adds a {@link SectionManagedObjectSource}.
		 * 
		 * @param managedObjectSourceName Name of the
		 *                                {@link SectionManagedObjectSource}.
		 * @param maker                   {@link ManagedObjectMaker}.
		 * @return Added {@link SectionManagedObjectSource}.
		 */
		SectionManagedObjectSource addManagedObjectSource(String managedObjectSourceName, ManagedObjectMaker maker);

		/**
		 * Adds a {@link SectionFunctionNamespace}.
		 * 
		 * @param namespaceName Name of the {@link SectionFunctionNamespace}.
		 * @param maker         {@link NamespaceMaker}.
		 * @return {@link SectionFunctionNamespace}.
		 */
		SectionFunctionNamespace addFunctionNamespace(String namespaceName, NamespaceMaker maker);

		/**
		 * Adds a {@link SectionFunction}.
		 * 
		 * @param namespace       Name of the {@link SectionFunctionNamespace}.
		 * @param functionName    Name of the {@link SectionFunction}.
		 * @param functionFactory {@link ManagedFunctionFactory}.
		 * @param functionMaker   {@link FunctionMaker} for the {@link SectionFunction}.
		 * @return {@link SectionFunction}.
		 */
		SectionFunction addFunction(String namespace, String functionName, ManagedFunctionFactory<?, ?> functionFactory,
				FunctionMaker functionMaker);

		/**
		 * Adds a {@link FunctionFlow}.
		 * 
		 * @param namespace       Name of the {@link SectionFunctionNamespace}.
		 * @param functionName    Name of the {@link SectionFunction}.
		 * @param functionFactory {@link ManagedFunctionFactory}.
		 * @param flowName        Name of the {@link ManagedFunctionFlowType}.
		 * @param argumentType    Argument type.
		 * @return {@link FunctionFlow}.
		 */
		FunctionFlow addFunctionFlow(String namespace, String functionName,
				ManagedFunctionFactory<?, ?> functionFactory, String flowName, Class<?> argumentType);

		/**
		 * Adds a {@link FunctionObject}.
		 * 
		 * @param namespace       Name of the {@link SectionFunctionNamespace}.
		 * @param functionName    Name of the {@link SectionFunction}.
		 * @param functionFactory {@link ManagedFunctionFactory}.
		 * @param objectName      Name of the {@link ManagedFunctionObjectType}.
		 * @param objectType      Object type.
		 * @param typeQualifier   Type qualifier.
		 * @return {@link FunctionObject}.
		 */
		FunctionObject addFunctionObject(String namespace, String functionName,
				ManagedFunctionFactory<?, ?> functionFactory, String objectName, Class<?> objectType,
				String typeQualifier);

		/**
		 * Adds a {@link FunctionFlow} for a {@link ManagedFunctionEscalationType}.
		 * 
		 * @param namespace       Name of the {@link SectionFunctionNamespace}.
		 * @param functionName    Name of the {@link SectionFunction}.
		 * @param functionFactory {@link ManagedFunctionFactory}.
		 * @param escalationName  Name of the {@link ManagedFunctionEscalationType}.
		 * @param escalationType  Escalation type.
		 * @return {@link FunctionFlow}.
		 */
		<E extends Throwable> FunctionFlow addFunctionEscalation(String namespace, String functionName,
				ManagedFunctionFactory<?, ?> functionFactory, String escalationName, Class<E> escalationType);
	}

	/**
	 * Maker {@link SectionSource}.
	 */
	@TestSource
	public static class MakerSectionSource implements SectionSource, SectionMakerContext {

		/**
		 * Property name to obtain the {@link SectionMaker} identifier.
		 */
		private static final String MAKER_IDENTIFIER_PROPERTY_NAME = "section.maker";

		/**
		 * {@link SectionMaker} instances by their identifier.
		 */
		private static Map<String, SectionMaker> sectionMakers;

		/**
		 * Resets for another test.
		 */
		public static void reset() {
			sectionMakers = new HashMap<String, SectionMaker>();
		}

		/**
		 * Registers a {@link SectionMaker}.
		 * 
		 * @param maker {@link SectionMaker}.
		 * @return {@link PropertyList} to use to load the {@link MakerSectionSource}.
		 */
		public static PropertyList register(SectionMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						// Empty section
					}
				};
			}

			// Register the section maker
			String identifier = String.valueOf(sectionMakers.size());
			sectionMakers.put(identifier, maker);

			// Create and return the properties to load
			PropertyList propertyList = new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME, identifier);
			return propertyList;
		}

		/**
		 * {@link SectionDesigner}.
		 */
		private SectionDesigner builder;

		/**
		 * {@link SectionSourceContext}.
		 */
		private SectionSourceContext context;

		/*
		 * ===================== SectionSource =============================
		 */

		@Override
		public SectionSourceSpecification getSpecification() {
			fail("Should not require specification");
			return null;
		}

		@Override
		public void sourceSection(SectionDesigner sectionBuilder, SectionSourceContext context) throws Exception {

			// Store details to load
			this.builder = sectionBuilder;
			this.context = context;

			// Obtain the section maker
			String identifier = context.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			SectionMaker maker = sectionMakers.get(identifier);

			// Make the section
			maker.make(this);
		}

		/*
		 * ======================= SectionMakerContext ====================
		 */

		@Override
		public SectionDesigner getBuilder() {
			return this.builder;
		}

		@Override
		public SectionSourceContext getContext() {
			return this.context;
		}

		@Override
		public SubSection addSubSection(String subSectionName, SectionMaker maker) {

			// Register the section source
			PropertyList properties = MakerSectionSource.register(maker);

			// Return the created sub section (using name as location)
			SubSection subSection = this.builder.addSubSection(subSectionName, MakerSectionSource.class.getName(),
					subSectionName);
			for (Property property : properties) {
				subSection.addProperty(property.getName(), property.getValue());
			}
			return subSection;
		}

		@Override
		public SectionManagedObjectSource addManagedObjectSource(String managedObjectSourceName,
				ManagedObjectMaker maker) {

			// Register the managed object source
			PropertyList propertyList = MakerManagedObjectSource.register(maker);

			// Create and return the section managed object
			SectionManagedObjectSource moSource = this.builder.addSectionManagedObjectSource(managedObjectSourceName,
					MakerManagedObjectSource.class.getName());
			for (Property property : propertyList) {
				moSource.addProperty(property.getName(), property.getValue());
			}
			return moSource;
		}

		@Override
		public SectionFunctionNamespace addFunctionNamespace(String namespaceName, NamespaceMaker maker) {

			// Register the namespace maker
			PropertyList properties = MakerManagedFunctionSource.register(maker);

			// Return the created namespace
			SectionFunctionNamespace namespace = this.builder.addSectionFunctionNamespace(namespaceName,
					MakerManagedFunctionSource.class.getName());
			for (Property property : properties) {
				namespace.addProperty(property.getName(), property.getValue());
			}
			return namespace;
		}

		@Override
		public SectionFunction addFunction(String namespace, final String functionName,
				final ManagedFunctionFactory<?, ?> functionFactory, final FunctionMaker functionMaker) {

			// Create the section namespace containing the single function type
			NamespaceMaker namespaceMaker = new NamespaceMaker() {
				@Override
				public void make(NamespaceMakerContext context) {
					// Create the function type and make if required
					FunctionMakerContext maker = context.addFunction(functionName, functionFactory);
					if (functionMaker != null) {
						functionMaker.make(maker);
					}
				}
			};
			SectionFunctionNamespace sectionNamespace = this.addFunctionNamespace(namespace, namespaceMaker);

			// Return the section function
			return sectionNamespace.addSectionFunction(functionName, functionName);
		}

		@Override
		public FunctionFlow addFunctionFlow(String namespace, String functionName,
				ManagedFunctionFactory<?, ?> functionFactory, final String flowName, final Class<?> argumentType) {

			// Create the section function containing of a single flow
			FunctionMaker flowMaker = new FunctionMaker() {
				@Override
				public void make(FunctionMakerContext maker) {
					// Create the flow
					maker.addFlow(flowName, argumentType);
				}
			};
			SectionFunction function = this.addFunction(namespace, functionName, functionFactory, flowMaker);

			// Return the function flow
			return function.getFunctionFlow(flowName);
		}

		@Override
		public FunctionObject addFunctionObject(String namespace, String functionName,
				ManagedFunctionFactory<?, ?> functionFactory, final String objectName, final Class<?> objectType,
				final String typeQualifier) {

			// Create the section function containing of a single object
			FunctionMaker objectMaker = new FunctionMaker() {
				@Override
				public void make(FunctionMakerContext maker) {
					// Create the object
					maker.addObject(objectName, objectType, typeQualifier);
				}
			};
			SectionFunction function = this.addFunction(namespace, functionName, functionFactory, objectMaker);

			// Return the function object
			return function.getFunctionObject(objectName);
		}

		@Override
		public <E extends Throwable> FunctionFlow addFunctionEscalation(String namespace, String functionName,
				ManagedFunctionFactory<?, ?> functionFactory, final String escalationName,
				final Class<E> escalationType) {

			// Create the section function containing of a single escalation
			FunctionMaker escalationMaker = new FunctionMaker() {
				@Override
				public void make(FunctionMakerContext maker) {
					// Create the escalation
					maker.addEscalation(escalationName, escalationType);
				}
			};
			SectionFunction function = this.addFunction(namespace, functionName, functionFactory, escalationMaker);

			// Return the function escalation
			return function.getFunctionEscalation(escalationName);
		}
	}

	/**
	 * Makes the {@link SectionManagedObject}.
	 */
	protected static interface ManagedObjectMaker {

		/**
		 * Makes the {@link SectionManagedObject}.
		 * 
		 * @param context {@link ManagedObjectMakerContext}.
		 */
		void make(ManagedObjectMakerContext context);
	}

	/**
	 * Context for the {@link ManagedObjectMaker}.
	 */
	protected static interface ManagedObjectMakerContext {

		/**
		 * Obtains the {@link MetaDataContext}.
		 * 
		 * @return {@link MetaDataContext}.
		 */
		MetaDataContext<Indexed, Indexed> getContext();

		/**
		 * Adds an extension interface supported by the {@link ManagedObject}.
		 * 
		 * @param extensionInterface Extension interface supported.
		 */
		void addExtensionInterface(Class<?> extensionInterface);
	}

	/**
	 * Maker {@link ManagedObjectSource}.
	 */
	@TestSource
	public static class MakerManagedObjectSource extends AbstractManagedObjectSource<Indexed, Indexed>
			implements ManagedObjectMakerContext {

		/**
		 * Name of property holding the identifier for the {@link ManagedObjectMaker}.
		 */
		private static final String MAKER_IDENTIFIER_PROPERTY_NAME = "managed.object.maker";

		/**
		 * {@link ManagedObjectMaker} instances by their identifiers.
		 */
		private static Map<String, ManagedObjectMaker> managedObjectMakers;

		/**
		 * Clears the {@link ManagedObjectMaker} instances for the next test.
		 */
		public static void reset() {
			managedObjectMakers = new HashMap<String, ManagedObjectMaker>();
		}

		/**
		 * Registers a {@link ManagedObjectMaker}.
		 * 
		 * @param maker {@link ManagedObjectMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(ManagedObjectMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new ManagedObjectMaker() {
					@Override
					public void make(ManagedObjectMakerContext context) {
						// Empty managed object
					}
				};
			}

			// Register the managed object maker
			String identifier = String.valueOf(managedObjectMakers.size());
			managedObjectMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME, identifier);
		}

		/**
		 * {@link MetaDataContext}.
		 */
		private MetaDataContext<Indexed, Indexed> context;

		/*
		 * ================= AbstractManagedObjectSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not require specification");
		}

		@Override
		protected void loadMetaData(MetaDataContext<Indexed, Indexed> context) throws Exception {

			// Provide default object type
			context.setObjectClass(Object.class);

			// Store details to load
			this.context = context;

			// Obtain the managed object maker
			String identifier = context.getManagedObjectSourceContext().getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			ManagedObjectMaker managedObjectMaker = managedObjectMakers.get(identifier);

			// Make the managed object
			managedObjectMaker.make(this);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not require to source managed object");
			return null;
		}

		/*
		 * ==================== ManagedObjectMakerContext ====================
		 */

		@Override
		public MetaDataContext<Indexed, Indexed> getContext() {
			return this.context;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void addExtensionInterface(Class<?> extensionInterface) {
			// Add the extension
			this.context.addManagedObjectExtension(extensionInterface, new ExtensionFactory() {
				@Override
				public Object createExtension(ManagedObject managedObject) {
					fail("Should not require to create extension interface");
					return null;
				}
			});
		}
	}

	/**
	 * Makes the {@link OfficeFloorSupplier}.
	 */
	protected static interface SupplierMaker {

		/**
		 * As per {@link SupplierSource}.
		 * 
		 * @param context {@link SupplierSourceContext}.
		 * @throws Exception If fails to supply the {@link ManagedObjectSource}
		 *                   instances.
		 */
		void supply(SupplierSourceContext context) throws Exception;
	}

	/**
	 * Maker {@link SupplierSource}.
	 */
	@TestSource
	public static class MakerSupplierSource extends AbstractSupplierSource {

		/**
		 * Name of property holding the identifier for the {@link SupplierMaker} .
		 */
		private static final String MAKER_IDENTIFIER_PROPERTY_NAME = "supplier.maker";

		/**
		 * {@link SupplierMaker} instances by their identifiers.
		 */
		private static Map<String, SupplierMaker> supplierMakers;

		/**
		 * Clears the {@link SupplierMaker} instances for the next test.
		 */
		public static void reset() {
			supplierMakers = new HashMap<String, SupplierMaker>();
		}

		/**
		 * Registers a {@link SupplierMaker}.
		 * 
		 * @param maker {@link SupplierMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(SupplierMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new SupplierMaker() {
					@Override
					public void supply(SupplierSourceContext context) throws Exception {
						// No managed objects
					}
				};
			}

			// Register the managed object maker
			String identifier = String.valueOf(supplierMakers.size());
			supplierMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME, identifier);
		}

		/*
		 * ================== SupplierSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not require specification");
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Obtain the supplier maker
			String identifier = context.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			SupplierMaker maker = supplierMakers.get(identifier);
			assertNotNull("Unknown supplier maker " + identifier, maker);

			// Make the supplier
			maker.supply(context);
		}

		@Override
		public void terminate() {
			// nothing to clean up
		}
	}

	/**
	 * Makes the {@link SectionFunctionNamespace}.
	 */
	protected static interface NamespaceMaker {

		/**
		 * Makes the {@link SectionFunctionNamespace}.
		 * 
		 * @param context {@link NamespaceMakerContext}.
		 */
		void make(NamespaceMakerContext context);
	}

	/**
	 * Context for the {@link NamespaceMaker}.
	 */
	protected static interface NamespaceMakerContext {

		/**
		 * Obtains the {@link FunctionNamespaceBuilder}.
		 * 
		 * @return {@link FunctionNamespaceBuilder}.
		 */
		FunctionNamespaceBuilder getBuilder();

		/**
		 * Obtains the {@link ManagedFunctionSourceContext}.
		 * 
		 * @return {@link ManagedFunctionSourceContext}.
		 */
		ManagedFunctionSourceContext getContext();

		/**
		 * Adds a {@link ManagedFunctionType}.
		 * 
		 * @param functionTypeName Name of the {@link ManagedFunctionType}.
		 * @param functionFactory  {@link ManagedFunctionFactory}.
		 * @return {@link FunctionMakerContext} to make the {@link ManagedFunctionType}.
		 */
		FunctionMakerContext addFunction(String functionTypeName, ManagedFunctionFactory<?, ?> functionFactory);
	}

	/**
	 * Makes the {@link ManagedFunctionType}.
	 */
	protected static interface FunctionMaker {

		/**
		 * Makes the {@link ManagedFunctionType}.
		 * 
		 * @param context {@link FunctionMakerContext}.
		 */
		void make(FunctionMakerContext context);
	}

	/**
	 * Context for the {@link FunctionMaker}.
	 */
	protected static interface FunctionMakerContext {

		/**
		 * Adds a {@link ManagedFunctionFlowType}.
		 * 
		 * @param flowName     Name of the {@link ManagedFunctionFlowType}.
		 * @param argumentType Argument type.
		 * @return {@link ManagedFunctionFlowTypeBuilder}.
		 */
		ManagedFunctionFlowTypeBuilder<?> addFlow(String flowName, Class<?> argumentType);

		/**
		 * Adds a {@link ManagedFunctionObjectType}.
		 * 
		 * @param objectName    Name of the {@link ManagedFunctionObjectType}.
		 * @param objectType    Object type.
		 * @param typeQualifier Type qualifier.
		 * @return {@link ManagedFunctionObjectTypeBuilder}.
		 */
		ManagedFunctionObjectTypeBuilder<?> addObject(String objectName, Class<?> objectType, String typeQualifier);

		/**
		 * Adds a {@link ManagedFunctionEscalationTypeBuilder}.
		 * 
		 * @param escalationName Name of the {@link ManagedFunctionEscalationType}.
		 * @param escalationType Escalation type.
		 * @return {@link ManagedFunctionEscalationTypeBuilder}.
		 */
		<E extends Throwable> ManagedFunctionEscalationTypeBuilder addEscalation(String escalationName,
				Class<E> escalationType);
	}

	/**
	 * Maker {@link ManagedFunctionSource}.
	 */
	@TestSource
	public static class MakerManagedFunctionSource implements ManagedFunctionSource, NamespaceMakerContext {

		/**
		 * Property name for the {@link NamespaceMaker} identifier.
		 */
		public static final String MAKER_IDENTIFIER_PROPERTY_NAME = "namespace.maker";

		/**
		 * {@link NamespaceMaker} instances by their identifiers.
		 */
		private static Map<String, NamespaceMaker> namespaceMakers;

		/**
		 * Resets for the next load.
		 */
		public static void reset() {
			namespaceMakers = new HashMap<String, NamespaceMaker>();
		}

		/**
		 * Registers a {@link NamespaceMaker}.
		 * 
		 * @param maker {@link NamespaceMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(NamespaceMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new NamespaceMaker() {
					@Override
					public void make(NamespaceMakerContext context) {
						// Empty namespace
					}
				};
			}

			// Register the namespace maker and factory
			String identifier = String.valueOf(namespaceMakers.size());
			namespaceMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME, identifier);
		}

		/**
		 * {@link FunctionNamespaceBuilder}.
		 */
		private FunctionNamespaceBuilder builder;

		/**
		 * {@link ManagedFunctionSourceContext}.
		 */
		private ManagedFunctionSourceContext context;

		/*
		 * ==================== ManagedFuntionSource ====================
		 */

		@Override
		public ManagedFunctionSourceSpecification getSpecification() {
			fail("Should not require specification");
			return null;
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Obtain the namespace maker
			String identifier = context.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			NamespaceMaker namespaceMaker = namespaceMakers.get(identifier);

			// Store details to load
			this.builder = namespaceTypeBuilder;
			this.context = context;

			// Make the namespace
			namespaceMaker.make(this);
		}

		/*
		 * ================== NamespaceMakerContext ===========================
		 */

		@Override
		public FunctionNamespaceBuilder getBuilder() {
			return this.builder;
		}

		@Override
		public ManagedFunctionSourceContext getContext() {
			return this.context;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public FunctionMakerContext addFunction(String functionName, ManagedFunctionFactory functionFactory) {
			// Create and return the function type maker
			return new FunctionTypeMakerImpl(functionName, functionFactory, this.builder);
		}

		/**
		 * {@link FunctionMakerContext} implementation.
		 */
		private class FunctionTypeMakerImpl implements FunctionMakerContext {

			/**
			 * {@link ManagedFunctionFactory}.
			 */
			private final ManagedFunctionFactory<?, ?> functionFactory;

			/**
			 * {@link ManagedFunctionTypeBuilder}.
			 */
			@SuppressWarnings("rawtypes")
			private final ManagedFunctionTypeBuilder functionTypeBuilder;

			/**
			 * Initiate.
			 * 
			 * @param functionName         Name of the {@link SectionFunction}.
			 * @param functionFactory      {@link ManagedFunctionFactory}.
			 * @param namespaceTypeBuilder {@link FunctionNamespaceBuilder}.
			 */
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public FunctionTypeMakerImpl(String functionName, ManagedFunctionFactory<?, ?> functionFactory,
					FunctionNamespaceBuilder namespaceTypeBuilder) {
				this.functionFactory = functionFactory;

				// Create the function type builder
				this.functionTypeBuilder = namespaceTypeBuilder.addManagedFunctionType(functionName, null, null)
						.setFunctionFactory((ManagedFunctionFactory) this.functionFactory);
			}

			/*
			 * ================ FunctionTypeMaker ================
			 */

			@Override
			public ManagedFunctionFlowTypeBuilder<?> addFlow(String flowName, Class<?> argumentType) {
				// Create and return the function flow type builder
				ManagedFunctionFlowTypeBuilder<?> flowBuilder = this.functionTypeBuilder.addFlow();
				flowBuilder.setArgumentType(argumentType);
				flowBuilder.setLabel(flowName);
				return flowBuilder;
			}

			@Override
			@SuppressWarnings("unchecked")
			public ManagedFunctionObjectTypeBuilder<?> addObject(String objectName, Class<?> objectType,
					String typeQualifier) {
				// Create and return the function object type builder
				ManagedFunctionObjectTypeBuilder<?> objectBuilder = this.functionTypeBuilder.addObject(objectType);
				objectBuilder.setLabel(objectName);
				if (typeQualifier != null) {
					objectBuilder.setTypeQualifier(typeQualifier);
				}
				return objectBuilder;
			}

			@Override
			@SuppressWarnings("unchecked")
			public <E extends Throwable> ManagedFunctionEscalationTypeBuilder addEscalation(String escalationName,
					Class<E> escalationType) {
				// Create and return the function escalation type builder
				ManagedFunctionEscalationTypeBuilder escalationBuilder = this.functionTypeBuilder
						.addEscalation(escalationType);
				escalationBuilder.setLabel(escalationName);
				return escalationBuilder;
			}
		}
	}

	/**
	 * Maker of {@link Governance}.
	 */
	protected static interface GovernanceMaker {

		/**
		 * Makes the {@link Governance}.
		 * 
		 * @param context {@link GovernanceMakerContext}.
		 */
		void make(GovernanceMakerContext context);
	}

	/**
	 * Context for the {@link GovernanceMaker}.
	 */
	protected static interface GovernanceMakerContext {

		/**
		 * Specifies the extension interface.
		 * 
		 * @param extensionInterface Extension interface.
		 */
		void setExtensionInterface(Class<?> extensionInterface);
	}

	/**
	 * Maker {@link GovernanceSource}.
	 */
	@TestSource
	public static class MakerGovernanceSource extends AbstractGovernanceSource<Object, Indexed>
			implements GovernanceMakerContext, GovernanceFactory<Object, Indexed> {

		/**
		 * Property name to obtain the {@link GovernanceMaker} identifier.
		 */
		private static final String MAKER_IDENTIFIER_PROPERTY_NAME = "governance.maker";

		/**
		 * {@link GovernanceMaker} instances by their identifiers.
		 */
		private static Map<String, GovernanceMaker> governanceMakers;

		/**
		 * Resets for the next test.
		 */
		public static void reset() {
			governanceMakers = new HashMap<String, GovernanceMaker>();
		}

		/**
		 * Registers a {@link GovernanceMaker}.
		 * 
		 * @param maker {@link GovernanceMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(GovernanceMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new GovernanceMaker() {
					@Override
					public void make(GovernanceMakerContext context) {
						// Empty governance
					}
				};
			}

			// Register the governance maker
			String identifier = String.valueOf(governanceMakers.size());
			governanceMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME, identifier);
		}

		/**
		 * {@link MetaDataContext}.
		 */
		@SuppressWarnings("rawtypes")
		private MetaDataContext metaDataContext;

		/*
		 * ================ AbstractAdministratorSource ========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not require specification");
		}

		@Override
		protected void loadMetaData(MetaDataContext<Object, Indexed> context) throws Exception {

			// Store details to load
			this.metaDataContext = context;

			// Provide the governance factory
			context.setGovernanceFactory(this);

			// Obtain the governance maker
			String identifier = context.getGovernanceSourceContext().getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			GovernanceMaker governanceMaker = governanceMakers.get(identifier);

			// Make the governance
			governanceMaker.make(this);
		}

		/*
		 * ================= GovernanceMakerContext =========================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public void setExtensionInterface(Class<?> extensionInterface) {
			this.metaDataContext.setExtensionInterface(extensionInterface);
		}

		/*
		 * ===================== GovernanceFactory ===========================
		 */

		@Override
		public Governance<Object, Indexed> createGovernance() throws Throwable {
			TestCase.fail("Should not require to create Governance for compiling");
			return null;
		}
	}

	/**
	 * Maker of an {@link Administration}.
	 */
	protected static interface AdministrationMaker {

		/**
		 * Makes the {@link Administration}.
		 * 
		 * @param context {@link AdministratorMakerContext}.
		 */
		void make(AdministrationMakerContext context);
	}

	/**
	 * Context for the {@link AdministrationMaker}.
	 */
	protected static interface AdministrationMakerContext {

		/**
		 * Specifies the {@link AdministrationFactory}.
		 * 
		 * @param administrationFactory {@link AdministrationFactory}.
		 */
		void setAdministrationFactory(AdministrationFactory<?, ?, ?> administrationFactory);

		/**
		 * Specifies the extension interface.
		 * 
		 * @param extensionInterface Extension interface.
		 */
		void setExtensionInterface(Class<?> extensionInterface);

		/**
		 * Adds a {@link Flow}.
		 * 
		 * @param flowName     Name of {@link Flow}.
		 * @param argumentType Type of argument.
		 */
		void addFlow(String flowName, Class<?> argumentType);

		/**
		 * Adds an {@link Escalation}.
		 * 
		 * @param escalationName Name of {@link Escalation}.
		 * @param escalationType Type of {@link Escalation}.
		 */
		void addEscalation(String escalationName, Class<? extends Throwable> escalationType);

		/**
		 * Adds {@link Governance}.
		 * 
		 * @param governanceName Name of {@link Governance}.
		 */
		void addGovernance(String governanceName);
	}

	/**
	 * Maker {@link AdministrationSource}.
	 */
	@TestSource
	public static class MakerAdministrationSource extends AbstractAdministrationSource<Object, Indexed, Indexed>
			implements AdministrationMakerContext {

		/**
		 * Property name to obtain the {@link AdministratorMaker} identifier.
		 */
		private static final String MAKER_IDENTIFIER_PROPERTY_NAME = "administration.maker";

		/**
		 * {@link AdministrationMaker} instances by their identifiers.
		 */
		private static Map<String, AdministrationMaker> adminMakers;

		/**
		 * Resets for the next test.
		 */
		public static void reset() {
			adminMakers = new HashMap<String, AdministrationMaker>();
		}

		/**
		 * Registers a {@link AdministrationMaker}.
		 * 
		 * @param maker {@link AdministrationMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(AdministrationMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new AdministrationMaker() {
					@Override
					public void make(AdministrationMakerContext context) {
						// Empty administrator
					}
				};
			}

			// Register the administration maker
			String identifier = String.valueOf(adminMakers.size());
			adminMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME, identifier);
		}

		/**
		 * {@link MetaDataContext}.
		 */
		@SuppressWarnings("rawtypes")
		private MetaDataContext metaDataContext;

		/*
		 * ================ AbstractAdministratorSource ========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not require specification");
		}

		@Override
		protected void loadMetaData(MetaDataContext<Object, Indexed, Indexed> context) throws Exception {

			// Store details to load
			this.metaDataContext = context;

			// Obtain the administrator maker
			String identifier = context.getAdministrationSourceContext().getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			AdministrationMaker adminMaker = adminMakers.get(identifier);

			// Make the administrator
			adminMaker.make(this);
		}

		/*
		 * ================= AdministrationMakerContext =================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public void setAdministrationFactory(AdministrationFactory<?, ?, ?> administrationFactory) {
			this.metaDataContext.setAdministrationFactory(administrationFactory);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void setExtensionInterface(Class<?> extensionInterface) {
			this.metaDataContext.setExtensionInterface(extensionInterface);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void addFlow(String flowName, Class<?> argumentType) {
			this.metaDataContext.addFlow(argumentType).setLabel(flowName);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void addEscalation(String escalationName, Class<? extends Throwable> escalationType) {
			this.metaDataContext.addEscalation(escalationType).setLabel(escalationName);
		}

		@Override
		public void addGovernance(String governanceName) {
			this.metaDataContext.addGovernance().setLabel(governanceName);
		}
	}

	/**
	 * Makes the {@link Team}.
	 */
	protected static interface TeamMaker {

		/**
		 * Makes the {@link Team}.
		 * 
		 * @param context {@link TeamMakerContext}.
		 */
		void make(TeamMakerContext context);
	}

	/**
	 * Context for the {@link TeamMaker}.
	 */
	protected static interface TeamMakerContext {
	}

	/**
	 * Maker {@link TeamSource}.
	 */
	@TestSource
	public static class MakerTeamSource implements TeamSource, TeamMakerContext, Team {

		/**
		 * Property name to obtain the {@link TeamMaker} identifier.
		 */
		public static final String MAKER_IDENTIFIER_PROPERTY_NAME = "team.maker";

		/**
		 * {@link TeamMaker} instances by their identifiers.
		 */
		private static Map<String, TeamMaker> teamMakers;

		/**
		 * Resets for the next test.
		 */
		public static void reset() {
			teamMakers = new HashMap<String, TeamMaker>();
		}

		/**
		 * Registers a {@link TeamMaker}.
		 * 
		 * @param maker {@link TeamMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(TeamMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new TeamMaker() {
					@Override
					public void make(TeamMakerContext context) {
						// Empty team
					}
				};
			}

			// Register the team maker
			String identifier = String.valueOf(teamMakers.size());
			teamMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME, identifier);
		}

		/*
		 * ==================== TeamSource ================================
		 */

		@Override
		public TeamSourceSpecification getSpecification() {
			fail("Should not require specification");
			return null;
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {

			// Obtain the team maker
			String identifier = context.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			TeamMaker teamMaker = teamMakers.get(identifier);

			// Make the team
			teamMaker.make(this);

			// Return the team
			return this;
		}

		/*
		 * ======================== Team ===================================
		 */

		@Override
		public void startWorking() {
			fail("Should not require the Team");
		}

		@Override
		public void assignJob(Job job) {
			fail("Should not require the Team");
		}

		@Override
		public void stopWorking() {
			fail("Should not require the Team");
		}
	}

	/**
	 * Makes an {@link Office}.
	 */
	protected static interface OfficeMaker {

		/**
		 * Makes an {@link Office}.
		 * 
		 * @param context {@link OfficeMakerContext}.
		 */
		void make(OfficeMakerContext context);
	}

	/**
	 * Context for the {@link OfficeMaker}.
	 */
	protected static interface OfficeMakerContext {

		/**
		 * Obtains the {@link OfficeArchitect}.
		 * 
		 * @return {@link OfficeArchitect}.
		 */
		OfficeArchitect getArchitect();

		/**
		 * Adds a {@link OfficeSection} to the {@link Office}.
		 * 
		 * @param sectionName  Name of the {@link OfficeSection}.
		 * @param sectionMaker {@link SectionMaker}.
		 * @return Added {@link OfficeSection}.
		 */
		OfficeSection addSection(String sectionName, SectionMaker sectionMaker);
	}

	/**
	 * Maker {@link OfficeSource}.
	 */
	@TestSource
	public static class MakerOfficeSource implements OfficeSource, OfficeMakerContext {

		/**
		 * Property name to obtain the {@link OfficeMaker} identifier.
		 */
		private static final String MAKER_IDENTIFIER_PROPERTY_NAME = "office.maker";

		/**
		 * {@link OfficeMaker} instances by their identifiers.
		 */
		private static Map<String, OfficeMaker> officeMakers;

		/**
		 * {@link AbstractStructureTestCase}.
		 */
		private static AbstractStructureTestCase testCase;

		/**
		 * Resets for the next test.
		 * 
		 * @param testCase {@link AbstractStructureTestCase}.
		 */
		public static void reset(AbstractStructureTestCase testCase) {
			officeMakers = new HashMap<String, OfficeMaker>();
			MakerOfficeSource.testCase = testCase;
		}

		/**
		 * Registers a {@link OfficeMaker}.
		 * 
		 * @param maker {@link OfficeMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(OfficeMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new OfficeMaker() {
					@Override
					public void make(OfficeMakerContext context) {
						// Empty office
					}
				};
			}

			// Register the office maker
			String identifier = String.valueOf(officeMakers.size());
			officeMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME, identifier);
		}

		/**
		 * {@link OfficeArchitect}.
		 */
		private OfficeArchitect architect;

		/*
		 * ===================== OfficeSource ================================
		 */

		@Override
		public OfficeSourceSpecification getSpecification() {
			fail("Should not require specification");
			return null;
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {

			// Store details to make available
			this.architect = officeArchitect;

			// Obtain the office maker
			String identifier = context.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			OfficeMaker officeMaker = officeMakers.get(identifier);

			// Make the office
			officeMaker.make(this);
		}

		/*
		 * ==================== OfficeMakerContext ==========================
		 */

		@Override
		public OfficeArchitect getArchitect() {
			return this.architect;
		}

		@Override
		public OfficeSection addSection(String sectionName, SectionMaker sectionMaker) {
			return testCase.addSection(this.architect, sectionName, sectionMaker);
		}
	}

	/**
	 * Makes the {@link OfficeFloor}.
	 */
	protected static interface OfficeFloorMaker {

		/**
		 * Makes the {@link OfficeFloor}.
		 * 
		 * @param context {@link OfficeFloor}.
		 * @throws Exception If fails to make the {@link OfficeFloor}.
		 */
		void make(OfficeFloorMakerContext context) throws Exception;
	}

	/**
	 * Context for the {@link OfficeFloorMaker}.
	 */
	protected static interface OfficeFloorMakerContext {

		/**
		 * Obtains the {@link OfficeFloorDeployer}.
		 * 
		 * @return {@link OfficeFloorDeployer}.
		 */
		OfficeFloorDeployer getDeployer();

		/**
		 * Obtains the {@link OfficeFloorSourceContext}.
		 * 
		 * @return {@link OfficeFloorSourceContext}.
		 */
		OfficeFloorSourceContext getContext();

		/**
		 * Adds an {@link Office}.
		 * 
		 * @param officeName  Name of the {@link Office}.
		 * @param officeMaker {@link OfficeMaker}.
		 * @return Added {@link DeployedOffice}.
		 */
		DeployedOffice addOffice(String officeName, OfficeMaker officeMaker);

		/**
		 * Adds an {@link OfficeFloorTeam}.
		 * 
		 * @param teamName  Name of the {@link OfficeFloorTeam}.
		 * @param teamMaker {@link TeamMaker}.
		 * @return Added {@link OfficeFloorTeam}.
		 */
		OfficeFloorTeam addTeam(String teamName, TeamMaker teamMaker);
	}

	/**
	 * Maker {@link OfficeFloorSource}.
	 */
	public static class MakerOfficeFloorSource implements OfficeFloorSource, OfficeFloorMakerContext {

		/**
		 * Property name to obtain the {@link OfficeFloorMaker} identifier.
		 */
		public static final String MAKER_IDENTIFIER_PROPERTY_NAME = "officefloor.maker";

		/**
		 * {@link OfficeFloorMaker} instances by their identifiers.
		 */
		private static Map<String, OfficeFloorMaker> officeFloorMakers;

		/**
		 * {@link AbstractStructureTestCase}.
		 */
		private static AbstractStructureTestCase testCase;

		/**
		 * Failure to instantiate this {@link MakerOfficeFloorSource}.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Resets for the next test.
		 * 
		 * @param testCase Current {@link AbstractStructureTestCase} running.
		 */
		public static void reset(AbstractStructureTestCase testCase) {
			officeFloorMakers = new HashMap<String, OfficeFloorMaker>();
			MakerOfficeFloorSource.testCase = testCase;
			instantiateFailure = null;
		}

		/**
		 * Registers a {@link OfficeFloorMaker}.
		 * 
		 * @param maker {@link OfficeFloorMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(OfficeFloorMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new OfficeFloorMaker() {
					@Override
					public void make(OfficeFloorMakerContext context) {
						// Empty office floor
					}
				};
			}

			// Register the office floor maker
			String identifier = String.valueOf(officeFloorMakers.size());
			officeFloorMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME, identifier);
		}

		/**
		 * {@link OfficeFloorDeployer}.
		 */
		private OfficeFloorDeployer deployer = null;

		/**
		 * {@link OfficeFloorSourceContext}.
		 */
		private OfficeFloorSourceContext context = null;

		/**
		 * Default constructor that may through instantiate failure if specified to do
		 * so.
		 */
		public MakerOfficeFloorSource() {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ===================== OfficeFloorSource ============================
		 */

		@Override
		public OfficeFloorSourceSpecification getSpecification() {
			fail("Should not require specification");
			return null;
		}

		@Override
		public void specifyConfigurationProperties(RequiredProperties requiredProperties,
				OfficeFloorSourceContext context) throws Exception {
			fail("Should not required to specify configuration properties");
		}

		@Override
		public void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {

			// Store details to make available
			this.deployer = deployer;
			this.context = context;

			// Obtain the office floor maker
			String identifier = context.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			OfficeFloorMaker officeFloorMaker = officeFloorMakers.get(identifier);

			// Make the office floor
			officeFloorMaker.make(this);
		}

		/*
		 * =================== OfficeFloorMakerContext ========================
		 */

		@Override
		public OfficeFloorDeployer getDeployer() {
			return this.deployer;
		}

		@Override
		public OfficeFloorSourceContext getContext() {
			return this.context;
		}

		@Override
		public DeployedOffice addOffice(String officeName, OfficeMaker officeMaker) {
			return testCase.addDeployedOffice(this.deployer, officeName, officeMaker);
		}

		@Override
		public OfficeFloorTeam addTeam(String teamName, TeamMaker teamMaker) {
			return testCase.addTeam(this.deployer, teamName, teamMaker);
		}
	}

}
