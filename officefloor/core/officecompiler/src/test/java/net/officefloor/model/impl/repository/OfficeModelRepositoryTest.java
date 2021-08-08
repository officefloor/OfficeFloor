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

package net.officefloor.model.impl.repository;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.FileSystemConfigurationContext;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.office.AdministrationEscalationModel;
import net.officefloor.model.office.AdministrationFlowModel;
import net.officefloor.model.office.AdministrationModel;
import net.officefloor.model.office.AdministrationToExternalManagedObjectModel;
import net.officefloor.model.office.AdministrationToOfficeManagedObjectModel;
import net.officefloor.model.office.AdministrationToOfficeSectionManagedObjectModel;
import net.officefloor.model.office.AdministrationToOfficeTeamModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToPreLoadAdministrationModel;
import net.officefloor.model.office.GovernanceAreaModel;
import net.officefloor.model.office.GovernanceEscalationModel;
import net.officefloor.model.office.GovernanceFlowModel;
import net.officefloor.model.office.GovernanceModel;
import net.officefloor.model.office.GovernanceToExternalManagedObjectModel;
import net.officefloor.model.office.GovernanceToOfficeManagedObjectModel;
import net.officefloor.model.office.GovernanceToOfficeSectionManagedObjectModel;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeFunctionModel;
import net.officefloor.model.office.OfficeFunctionToGovernanceModel;
import net.officefloor.model.office.OfficeFunctionToOfficeTeamModel;
import net.officefloor.model.office.OfficeFunctionToPostAdministrationModel;
import net.officefloor.model.office.OfficeFunctionToPreAdministrationModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectFunctionDependencyModel;
import net.officefloor.model.office.OfficeManagedObjectFunctionDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectFunctionDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectPoolModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectSourceStartAfterOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectSourceStartBeforeOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamToOfficeTeamModel;
import net.officefloor.model.office.OfficeManagedObjectSourceToOfficeManagedObjectPoolModel;
import net.officefloor.model.office.OfficeManagedObjectSourceToOfficeSupplierModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectToPreLoadAdministrationModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionManagedObjectModel;
import net.officefloor.model.office.OfficeSectionManagedObjectTeamModel;
import net.officefloor.model.office.OfficeSectionManagedObjectToPreLoadAdministrationModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeStartModel;
import net.officefloor.model.office.OfficeStartToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeSubSectionToGovernanceModel;
import net.officefloor.model.office.OfficeSupplierModel;
import net.officefloor.model.office.OfficeSupplierThreadLocalModel;
import net.officefloor.model.office.OfficeSupplierThreadLocalToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSupplierThreadLocalToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.office.PropertyModel;
import net.officefloor.model.office.TypeQualificationModel;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the marshaling/unmarshaling of the {@link OfficeModel} via the
 * {@link ModelRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeModelRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationItem} containing the {@link OfficeModel}.
	 */
	private ConfigurationItem configurationItem;

	@Override
	protected void setUp() throws Exception {

		// Specify location of the configuration
		File configurationFile = this.findFile(this.getClass(), "TestOffice.office.xml");
		this.configurationItem = FileSystemConfigurationContext.createWritableConfigurationItem(configurationFile);
	}

	/**
	 * Ensure retrieve the {@link OfficeModel}.
	 */
	public void testRetrieveOffice() throws Exception {

		// Load the Office
		ModelRepository repository = new ModelRepositoryImpl();
		OfficeModel office = new OfficeModel();
		repository.retrieve(office, this.configurationItem);

		// ----------------------------------------
		// Validate the office attributes
		// ----------------------------------------
		assertProperties(new OfficeModel(true, true), office, "getIsAutoWireObjects", "getIsAutoWireTeams");

		// ---------------------------------------
		// Validate the OfficeFloor suppliers
		// ---------------------------------------
		assertList(new String[] { "getOfficeSupplierName", "getSupplierSourceClassName", "getX", "getY" },
				office.getOfficeSuppliers(),
				new OfficeSupplierModel("SUPPLIER", "net.example.ExampleSupplierSource", 100, 101));
		OfficeSupplierModel supplier = office.getOfficeSuppliers().get(0);
		assertList(new String[] { "getName", "getValue" }, supplier.getProperties(),
				new PropertyModel("SUPPLIER_ONE", "VALUE_ONE"), new PropertyModel("SUPPLIER_TWO", "VALUE_TWO"));
		assertList(new String[] { "getQualifier", "getType" }, supplier.getOfficeSupplierThreadLocals(),
				new OfficeSupplierThreadLocalModel(null, "java.sql.GenericConnection"),
				new OfficeSupplierThreadLocalModel("QUALIFIED", "java.sql.Connection"));
		OfficeSupplierThreadLocalModel threadLocalOne = supplier.getOfficeSupplierThreadLocals().get(0);
		assertProperties(threadLocalOne.getOfficeManagedObject(),
				new OfficeSupplierThreadLocalToOfficeManagedObjectModel("MANAGED_OBJECT_ONE"),
				"getOfficeManagedObjectName");
		OfficeSupplierThreadLocalModel threadLocalTwo = supplier.getOfficeSupplierThreadLocals().get(1);
		assertProperties(threadLocalTwo.getExternalManagedObject(),
				new OfficeSupplierThreadLocalToExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT"),
				"getExternalManagedObjectName");

		// ----------------------------------------
		// Validate the external managed objects
		// ----------------------------------------
		assertList(new String[] { "getExternalManagedObjectName", "getObjectType", "getX", "getY" },
				office.getExternalManagedObjects(),
				new ExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT", Connection.class.getName(), 200, 201));
		ExternalManagedObjectModel extMo = office.getExternalManagedObjects().get(0);
		assertList(new String[] { "getAdministrationName", "getOrder" }, extMo.getAdministrations(),
				new AdministrationToExternalManagedObjectModel("ADMINISTRATION", "1"));
		assertList(new String[] { "getGovernanceName" }, extMo.getGovernances(),
				new GovernanceToExternalManagedObjectModel("GOVERNANCE"));
		assertList(new String[] { "getAdministrationName" }, extMo.getPreLoadAdministrations(),
				new ExternalManagedObjectToPreLoadAdministrationModel("ADMINISTRATION"));

		// ----------------------------------------
		// Validate the managed object sources
		// ----------------------------------------
		assertList(
				new String[] { "getOfficeManagedObjectSourceName", "getManagedObjectSourceClassName", "getObjectType",
						"getTimeout", "getX", "getY" },
				office.getOfficeManagedObjectSources(),
				new OfficeManagedObjectSourceModel("MANAGED_OBJECT_SOURCE", "net.example.ExampleManagedObjectSource",
						"net.orm.Session", "10", 300, 301),
				new OfficeManagedObjectSourceModel("SUPPLIED_MANAGED_OBJECT_SOURCE", null, "net.orm.Session", null, 310,
						311));
		List<OfficeManagedObjectSourceModel> moSources = office.getOfficeManagedObjectSources();
		OfficeManagedObjectSourceModel mos = moSources.get(0);
		assertOfficeManagedObjectSource(mos);
		assertProperties(new OfficeManagedObjectSourceToOfficeManagedObjectPoolModel("MANAGED_OBJECT_POOL"),
				mos.getOfficeManagedObjectPool(), "getOfficeManagedObjectPoolName");

		// Validate the supplied managed object source
		OfficeManagedObjectSourceModel suppliedMoSource = moSources.get(1);
		assertOfficeManagedObjectSource(suppliedMoSource);
		assertProperties(
				new OfficeManagedObjectSourceToOfficeSupplierModel("SUPPLIER", "QUALIFIER", "net.orm.SpecificSession"),
				suppliedMoSource.getOfficeSupplier(), "getOfficeSupplierName", "getQualifier", "getType");

		// Validate the start before
		OfficeManagedObjectSourceModel sourcedMoSource = moSources.get(0);
		assertList(new String[] { "getOfficeManagedObjectSourceName", "getManagedObjectType" },
				sourcedMoSource.getStartBeforeEarliers(),
				new OfficeManagedObjectSourceStartBeforeOfficeManagedObjectSourceModel("SUPPLIED_MANAGED_OBJECT_SOURCE",
						null),
				new OfficeManagedObjectSourceStartBeforeOfficeManagedObjectSourceModel(null, "net.orm.Session"));

		// Validate the start after
		assertList(new String[] { "getOfficeManagedObjectSourceName", "getManagedObjectType" },
				suppliedMoSource.getStartAfterLaters(),
				new OfficeManagedObjectSourceStartAfterOfficeManagedObjectSourceModel("MANAGED_OBJECT_SOURCE", null),
				new OfficeManagedObjectSourceStartAfterOfficeManagedObjectSourceModel(null, "net.orm.Session"));

		// ----------------------------------------
		// Validate the managed objects
		// ----------------------------------------
		assertList(new String[] { "getOfficeManagedObjectName", "getManagedObjectScope", "getX", "getY" },
				office.getOfficeManagedObjects(),
				new OfficeManagedObjectModel("MANAGED_OBJECT_ONE", "THREAD", 400, 401),
				new OfficeManagedObjectModel("MANAGED_OBJECT_TWO", "PROCESS", 410, 411));
		OfficeManagedObjectModel mo = office.getOfficeManagedObjects().get(0);
		assertProperties(new OfficeManagedObjectToOfficeManagedObjectSourceModel("MANAGED_OBJECT_SOURCE"),
				mo.getOfficeManagedObjectSource(), "getOfficeManagedObjectSourceName");
		assertList(new String[] { "getAdministrationName", "getOrder" }, mo.getAdministrations(),
				new AdministrationToOfficeManagedObjectModel("ADMINISTRATION", "1"));
		assertList(new String[] { "getGovernanceName" }, mo.getGovernances(),
				new GovernanceToOfficeManagedObjectModel("GOVERNANCE"));
		assertList(new String[] { "getOfficeManagedObjectDependencyName", "getDependencyType" },
				mo.getOfficeManagedObjectDependencies(),
				new OfficeManagedObjectDependencyModel("DEPENDENCY_ONE", Connection.class.getName()),
				new OfficeManagedObjectDependencyModel("DEPENDENCY_TWO", Connection.class.getName()));
		assertList(new String[] { "getAdministrationName" }, mo.getPreLoadAdministrations(),
				new OfficeManagedObjectToPreLoadAdministrationModel("ADMINISTRATION"));
		assertList(new String[] { "getQualifier", "getType" }, mo.getTypeQualifications(),
				new TypeQualificationModel("QUALIFIED", "java.sql.SpecificConnection"),
				new TypeQualificationModel(null, "java.sql.GenericConnection"));
		OfficeManagedObjectDependencyModel dependencyOne = mo.getOfficeManagedObjectDependencies().get(0);
		assertProperties(new OfficeManagedObjectDependencyToOfficeManagedObjectModel("MANAGED_OBJECT_TWO"),
				dependencyOne.getOfficeManagedObject(), "getOfficeManagedObjectName");
		OfficeManagedObjectDependencyModel dependencyTwo = mo.getOfficeManagedObjectDependencies().get(1);
		assertProperties(new OfficeManagedObjectDependencyToExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT"),
				dependencyTwo.getExternalManagedObject(), "getExternalManagedObjectName");

		// ----------------------------------------------
		// Validate the managed object pools
		// ----------------------------------------------
		assertList(
				new String[] { "getOfficeManagedObjectPoolName", "getManagedObjectPoolSourceClassName", "getX",
						"getY" },
				office.getOfficeManagedObjectPools(), new OfficeManagedObjectPoolModel("MANAGED_OBJECT_POOL",
						"net.example.ExampleManagedObjectPoolSource", null, null, 500, 501));
		OfficeManagedObjectPoolModel pool = office.getOfficeManagedObjectPools().get(0);
		assertList(new String[] { "getName", "getValue" }, pool.getProperties(),
				new PropertyModel("POOL_ONE", "VALUE_ONE"), new PropertyModel("POOL_TWO", "VALUE_TWO"));

		// ----------------------------------------
		// Validate the teams
		// ----------------------------------------
		assertList(new String[] { "getOfficeTeamName", "getX", "getY" }, office.getOfficeTeams(),
				new OfficeTeamModel("TEAM", 600, 601));
		OfficeTeamModel team = office.getOfficeTeams().get(0);
		assertList(new String[] { "getQualifier", "getType" }, team.getTypeQualifications(),
				new TypeQualificationModel("QUALIFIED", "java.sql.SpecificStatement"),
				new TypeQualificationModel(null, "java.sql.GenericStatement"));

		// ----------------------------------------
		// Validate the escalations
		// ----------------------------------------
		assertList(new String[] { "getEscalationType", "getX", "getY" }, office.getOfficeEscalations(),
				new OfficeEscalationModel(Exception.class.getName(), null, 700, 701));
		OfficeEscalationModel escalation = office.getOfficeEscalations().get(0);
		assertProperties(new OfficeEscalationToOfficeSectionInputModel("SECTION", "INPUT_B"),
				escalation.getOfficeSectionInput(), "getOfficeSectionName", "getOfficeSectionInputName");

		// ----------------------------------------
		// Validate the administrations
		// ----------------------------------------
		assertList(
				new String[] { "getAdministrationName", "getAdministrationSourceClassName", "getIsAutoWireExtensions",
						"getX", "getY" },
				office.getAdministrations(),
				new AdministrationModel("ADMINISTRATION", "net.example.ExampleAdministrationSource", true, 800, 801));
		AdministrationModel admin = office.getAdministrations().get(0);
		assertList(new String[] { "getName", "getValue" }, admin.getProperties(),
				new PropertyModel("ADMIN_ONE", "VALUE_ONE"), new PropertyModel("ADMIN_TWO", "VALUE_TWO"));
		assertProperties(new AdministrationToOfficeTeamModel("TEAM"), admin.getOfficeTeam(), "getOfficeTeamName");
		assertList(new String[] { "getFlowName", "getArgumentType" }, admin.getAdministrationFlows(),
				new AdministrationFlowModel("FLOW_ONE", null, "java.lang.Integer"),
				new AdministrationFlowModel("FLOW_TWO", null, null));
		assertList(new String[] { "getEscalationType" }, admin.getAdministrationEscalations(),
				new AdministrationEscalationModel("java.sql.SQLException"),
				new AdministrationEscalationModel("java.io.IOException"));

		// ----------------------------------------
		// Validate the governances
		// ----------------------------------------
		assertList(
				new String[] { "getGovernanceName", "getGovernanceSourceClassName", "getIsAutoWireExtensions", "getX",
						"getY" },
				office.getGovernances(),
				new GovernanceModel("GOVERNANCE", "net.example.ExampleGovernanceSource", true, 900, 901));
		GovernanceModel gov = office.getGovernances().get(0);
		assertList(new String[] { "getName", "getValue" }, gov.getProperties(),
				new PropertyModel("GOV_ONE", "VALUE_ONE"), new PropertyModel("GOV_TWO", "VALUE_TWO"));
		assertProperties(new AdministrationToOfficeTeamModel("TEAM"), gov.getOfficeTeam(), "getOfficeTeamName");
		assertList(new String[] { "getX", "getY", "getHeight", "getWidth" }, gov.getGovernanceAreas(),
				new GovernanceAreaModel(920, 921, 910, 911));
		assertList(new String[] { "getFlowName", "getArgumentType" }, gov.getGovernanceFlows(),
				new GovernanceFlowModel("FLOW_A", null, "java.lang.String"),
				new GovernanceFlowModel("FLOW_B", null, null));
		assertList(new String[] { "getEscalationType" }, gov.getGovernanceEscalations(),
				new GovernanceEscalationModel("java.lang.NullPointerException"),
				new GovernanceEscalationModel("java.lang.RuntimeException"));

		// ----------------------------------------
		// Validate the start triggers
		// ----------------------------------------
		assertList(new String[] { "getStartName", "getX", "getY" }, office.getOfficeStarts(),
				new OfficeStartModel("START", null, 1000, 1001));
		OfficeStartModel start = office.getOfficeStarts().get(0);
		assertProperties(new OfficeStartToOfficeSectionInputModel("SECTION", "INPUT_A"), start.getOfficeSectionInput(),
				"getOfficeSectionName", "getOfficeSectionInputName");

		// ----------------------------------------
		// Validate the sections
		// ----------------------------------------
		assertList(
				new String[] { "getOfficeSectionName", "getSectionSourceClassName", "getSectionLocation", "getX",
						"getY" },
				office.getOfficeSections(),
				new OfficeSectionModel("SECTION", "net.example.ExampleSectionSource", "SECTION_LOCATION", 1100, 1101),
				new OfficeSectionModel("SECTION_TARGET", "net.example.ExampleSectionSource", "SECTION_LOCATION", 1110,
						1111));
		OfficeSectionModel section = office.getOfficeSections().get(0);
		assertList(new String[] { "getName", "getValue" }, section.getProperties(),
				new PropertyModel("PROP_ONE", "VALUE_ONE"), new PropertyModel("PROP_TWO", "VALUE_TWO"));

		// Inputs of section
		assertList(new String[] { "getOfficeSectionInputName", "getParameterType" }, section.getOfficeSectionInputs(),
				new OfficeSectionInputModel("INPUT_A", Integer.class.getName()),
				new OfficeSectionInputModel("INPUT_B", Exception.class.getName()));

		// Outputs of section
		assertList(new String[] { "getOfficeSectionOutputName", "getArgumentType", "getEscalationOnly" },
				section.getOfficeSectionOutputs(),
				new OfficeSectionOutputModel("OUTPUT_ONE", Float.class.getName(), false),
				new OfficeSectionOutputModel("OUTPUT_TWO", Exception.class.getName(), true));
		OfficeSectionOutputModel output = section.getOfficeSectionOutputs().get(0);
		assertProperties(new OfficeSectionOutputToOfficeSectionInputModel("SECTION_TARGET", "INPUT"),
				output.getOfficeSectionInput(), "getOfficeSectionName", "getOfficeSectionInputName");

		// Objects of section
		assertList(new String[] { "getOfficeSectionObjectName", "getObjectType" }, section.getOfficeSectionObjects(),
				new OfficeSectionObjectModel("OBJECT_ONE", Connection.class.getName()),
				new OfficeSectionObjectModel("OBJECT_TWO", "net.orm.Session"));
		OfficeSectionObjectModel objectOne = section.getOfficeSectionObjects().get(0);
		assertProperties(new OfficeSectionObjectToExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT"),
				objectOne.getExternalManagedObject(), "getExternalManagedObjectName");
		OfficeSectionObjectModel objectTwo = section.getOfficeSectionObjects().get(1);
		assertProperties(new OfficeSectionObjectToOfficeManagedObjectModel("MANAGED_OBJECT"),
				objectTwo.getOfficeManagedObject(), "getOfficeManagedObjectName");

		// Sub section details for the top level office section
		assertNotNull("Must have sub section of office section", section.getOfficeSubSection());
		OfficeSubSectionModel officeSection = section.getOfficeSubSection();
		assertList(new String[] { "getGovernanceName" }, officeSection.getGovernances(),
				new OfficeSubSectionToGovernanceModel("GOVERNANCE"));
		assertList(new String[] { "getOfficeSectionManagedObjectName" }, officeSection.getOfficeSectionManagedObjects(),
				new OfficeSectionManagedObjectModel("SECTION_MANAGED_OBJECT"));
		OfficeSectionManagedObjectModel officeSectionMo = officeSection.getOfficeSectionManagedObjects().get(0);
		assertList(new String[] { "getAdministrationName", "getOrder" }, officeSectionMo.getAdministrations(),
				new AdministrationToOfficeSectionManagedObjectModel("ADMINISTRATION", "1"));
		assertList(new String[] { "getGovernanceName" }, officeSectionMo.getGovernances(),
				new GovernanceToOfficeSectionManagedObjectModel("GOVERNANCE"));
		assertList(new String[] { "getOfficeSectionManagedObjectTeamName" },
				officeSectionMo.getOfficeSectionManagedObjectTeams(),
				new OfficeSectionManagedObjectTeamModel("MO_TEAM"));
		assertList(new String[] { "getAdministrationName" }, officeSectionMo.getPreLoadAdministrations(),
				new OfficeSectionManagedObjectToPreLoadAdministrationModel("ADMINISTRATION"));
		assertList(new String[] { "getOfficeFunctionName" }, officeSection.getOfficeFunctions(),
				new OfficeFunctionModel("FUNCTION"));
		OfficeFunctionModel officeSectionFunction = officeSection.getOfficeFunctions().get(0);
		assertProperties(new OfficeFunctionToOfficeTeamModel("TEAM"), officeSectionFunction.getOfficeTeam(),
				"getOfficeTeamName");
		assertList(new String[] { "getAdministrationName" }, officeSectionFunction.getPreAdministrations(),
				new OfficeFunctionToPreAdministrationModel("ADMINISTRATION"));
		assertList(new String[] { "getGovernanceName" }, officeSectionFunction.getGovernances(),
				new OfficeFunctionToGovernanceModel("GOVERNANCE"));

		// Sub section
		assertList(new String[] { "getOfficeSubSectionName" }, officeSection.getOfficeSubSections(),
				new OfficeSubSectionModel("SUB_SECTION"));
		OfficeSubSectionModel subSection = officeSection.getOfficeSubSections().get(0);
		assertList(new String[] { "getOfficeSectionManagedObjectName" }, subSection.getOfficeSectionManagedObjects(),
				new OfficeSectionManagedObjectModel("SUB_SECTION_MANAGED_OBJECT"));
		assertList(new String[] { "getOfficeFunctionName" }, subSection.getOfficeFunctions(),
				new OfficeFunctionModel("SUB_SECTION_FUNCTION"));
		OfficeFunctionModel subSectionFunction = subSection.getOfficeFunctions().get(0);
		assertProperties(new OfficeFunctionToOfficeTeamModel("TEAM"), subSectionFunction.getOfficeTeam(),
				"getOfficeTeamName");
		assertList(new String[] { "getAdministrationName" }, subSectionFunction.getPostAdministrations(),
				new OfficeFunctionToPostAdministrationModel("ADMINISTRATION"));
		assertList(new String[] { "getGovernanceName" }, subSectionFunction.getGovernances(),
				new OfficeFunctionToGovernanceModel("GOVERNANCE"));
		assertList(new String[] { "getOfficeSubSectionName" }, subSection.getOfficeSubSections(),
				new OfficeSubSectionModel("SUB_SUB_SECTION"));
	}

	/**
	 * Asserts the {@link OfficeManagedObjectSourceModel} is correct.
	 * 
	 * @param mos {@link OfficeManagedObjectSourceModel}.
	 */
	private static void assertOfficeManagedObjectSource(OfficeManagedObjectSourceModel mos) {

		// Validate properties
		assertList(new String[] { "getName", "getValue" }, mos.getProperties(),
				new PropertyModel("MO_ONE", "VALUE_ONE"), new PropertyModel("MO_TWO", "VALUE_TWO"));

		// Validate the input dependencies
		assertList(new String[] { "getOfficeInputManagedObjectDependencyName", "getDependencyType" },
				mos.getOfficeInputManagedObjectDependencies(),
				new OfficeInputManagedObjectDependencyModel("INPUT_DEPENDENCY_ONE", Connection.class.getName()),
				new OfficeInputManagedObjectDependencyModel("INPUT_DEPENDENCY_TWO", Connection.class.getName()));
		assertProperties(new OfficeInputManagedObjectDependencyToOfficeManagedObjectModel("MANAGED_OBJECT_TWO"),
				mos.getOfficeInputManagedObjectDependencies().get(0).getOfficeManagedObject(),
				"getOfficeManagedObjectName");
		assertProperties(new OfficeInputManagedObjectDependencyToExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT"),
				mos.getOfficeInputManagedObjectDependencies().get(1).getExternalManagedObject(),
				"getExternalManagedObjectName");

		// Validate the function dependencies
		assertList(new String[] { "getOfficeManagedObjectFunctionDependencyName", "getDependencyType" },
				mos.getOfficeManagedObjectFunctionDependencies(),
				new OfficeManagedObjectFunctionDependencyModel("FUNCTION_DEPENDENCY_ONE", Connection.class.getName()),
				new OfficeManagedObjectFunctionDependencyModel("FUNCTION_DEPENDENCY_TWO", Connection.class.getName()));
		assertProperties(new OfficeManagedObjectFunctionDependencyToOfficeManagedObjectModel("MANAGED_OBJECT_TWO"),
				mos.getOfficeManagedObjectFunctionDependencies().get(0).getOfficeManagedObject(),
				"getOfficeManagedObjectName");
		assertProperties(
				new OfficeManagedObjectFunctionDependencyToExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT"),
				mos.getOfficeManagedObjectFunctionDependencies().get(1).getExternalManagedObject(),
				"getExternalManagedObjectName");

		// Validate the flows
		assertList(new String[] { "getOfficeManagedObjectSourceFlowName", "getArgumentType" },
				mos.getOfficeManagedObjectSourceFlows(),
				new OfficeManagedObjectSourceFlowModel("FLOW", Integer.class.getName()));
		OfficeManagedObjectSourceFlowModel flow = mos.getOfficeManagedObjectSourceFlows().get(0);
		assertProperties(new OfficeManagedObjectSourceFlowToOfficeSectionInputModel("SECTION", "INPUT_A"),
				flow.getOfficeSectionInput(), "getOfficeSectionName", "getOfficeSectionInputName");

		// Validate the teams
		assertList(new String[] { "getOfficeManagedObjectSourceTeamName" }, mos.getOfficeManagedObjectSourceTeams(),
				new OfficeManagedObjectSourceTeamModel("MO_TEAM"));
		OfficeManagedObjectSourceTeamModel moTeam = mos.getOfficeManagedObjectSourceTeams().get(0);
		assertProperties(new OfficeManagedObjectSourceTeamToOfficeTeamModel("TEAM"), moTeam.getOfficeTeam(),
				"getOfficeTeamName");
	}

	/**
	 * Ensure able to round trip storing and retrieving the {@link OfficeModel}.
	 */
	public void testRoundTripStoreRetrieveOffice() throws Exception {

		// Load the Office
		ModelRepository repository = new ModelRepositoryImpl();
		OfficeModel office = new OfficeModel();
		repository.retrieve(office, this.configurationItem);

		// Store the Office
		WritableConfigurationItem configuration = MemoryConfigurationContext.createWritableConfigurationItem("test");
		repository.store(office, configuration);

		// Reload the Office
		OfficeModel reloadedOffice = new OfficeModel();
		repository.retrieve(reloadedOffice, configuration);

		// Validate round trip
		assertGraph(office, reloadedOffice, RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}
