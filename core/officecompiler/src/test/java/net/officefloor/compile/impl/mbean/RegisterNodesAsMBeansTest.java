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

package net.officefloor.compile.impl.mbean;

import java.lang.management.ManagementFactory;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.JMX;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MXBean;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.mbean.MBeanFactory;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.administration.source.impl.AbstractAdministrationSource;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.mbean.MBeanRegistrator;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectPool;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.pool.source.impl.AbstractManagedObjectPoolSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.extension.CompileOffice;
import net.officefloor.extension.ExtendOfficeFloor;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Ensure able to register the various {@link Node} instances as {@link MXBean}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class RegisterNodesAsMBeansTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloorSource}.
	 */
	private final TestOfficeFloorSource officeFloorSource = new TestOfficeFloorSource();

	/**
	 * Ensure able to register the {@link OfficeFloorSource} as an MBean.
	 */
	public void testRegisterOfficeFloorSource() throws Exception {
		this.doTestInOfficeFloor(OfficeFloorSource.class, "OfficeFloor", null, (objectName) -> {

			// Ensure able to obtain OfficeFloorSource MBean
			TestOfficeFloorSourceMBean mbean = getMBean(objectName, TestOfficeFloorSourceMBean.class);
			assertEquals("Incorrect Mbean value", "OfficeFloor Test", mbean.getOfficeFloorSourceMBeanValue());

			// Change value to ensure correct instance used as MBean
			this.officeFloorSource.mbeanValue = "OfficeFloor changed";
			assertEquals("Incorrect chanaged MBean value", "OfficeFloor changed",
					mbean.getOfficeFloorSourceMBeanValue());
		});
	}

	public static interface TestOfficeFloorSourceMBean {
		String getOfficeFloorSourceMBeanValue();
	}

	@TestSource
	public static class TestOfficeFloorSource extends AbstractOfficeFloorSource implements TestOfficeFloorSourceMBean {

		private String mbeanValue = "OfficeFloor Test";

		@Override
		public String getOfficeFloorSourceMBeanValue() {
			return mbeanValue;
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void specifyConfigurationProperties(RequiredProperties requiredProperties,
				OfficeFloorSourceContext context) throws Exception {
		}

		@Override
		public void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {
		}
	}

	/**
	 * Ensure can register {@link OfficeSource} as MBean.
	 */
	public void testRegisterOfficeSource() throws Exception {
		TestOfficeSource officeSource = new TestOfficeSource();
		this.doTestInOfficeFloor(OfficeSource.class, "OFFICE", (deployer, context) -> {
			deployer.addDeployedOffice("OFFICE", officeSource, null);
		}, (objectName) -> {

			// Ensure able to obtain OfficeSource MBean
			TestOfficeSourceMBean mbean = getMBean(objectName, TestOfficeSourceMBean.class);
			assertEquals("Incorrect Mbean value", "Office Test", mbean.getTest());

			// Change value to ensure correct instance
			officeSource.mbeanValue = "Office changed";
			assertEquals("Incorrect changed MBean value", "Office changed", mbean.getTest());
		});
	}

	public static interface TestOfficeSourceMBean {
		String getTest();
	}

	@TestSource
	public static class TestOfficeSource extends AbstractOfficeSource implements DynamicMBean {

		private String mbeanValue = "Office Test";

		/*
		 * =================== DynamicMBean ===========================
		 */

		@Override
		public Object getAttribute(String attribute)
				throws AttributeNotFoundException, MBeanException, ReflectionException {
			assertEquals("Incorrect attribute", "Test", attribute);
			return this.mbeanValue;
		}

		@Override
		public void setAttribute(Attribute attribute)
				throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		}

		@Override
		public AttributeList getAttributes(String[] attributes) {
			return null;
		}

		@Override
		public AttributeList setAttributes(AttributeList attributes) {
			return null;
		}

		@Override
		public Object invoke(String actionName, Object[] params, String[] signature)
				throws MBeanException, ReflectionException {
			return null;
		}

		@Override
		public MBeanInfo getMBeanInfo() {
			return new MBeanInfo(TestOfficeSource.class.getName(), "",
					new MBeanAttributeInfo[] {
							new MBeanAttributeInfo("test", String.class.getName(), "", true, false, false) },
					null, null, null);
		}

		/*
		 * ======================= OfficeSource =======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {
		}
	}

	/**
	 * Ensure can register {@link ManagedObjectSource} as MBean.
	 */
	public void testRegisterManagedObjectSource() throws Exception {
		TestManagedObjectSource managedObjectSource = new TestManagedObjectSource();
		this.doTestInOfficeFloor(ManagedObjectSource.class, "MANAGED_OBJECT_SOURCE", (deployer, context) -> {
			OfficeFloorManagedObjectSource mos = deployer.addManagedObjectSource("MANAGED_OBJECT_SOURCE",
					managedObjectSource);
			DeployedOffice office = deployer.addDeployedOffice("OFFICE", TestOfficeSource.class.getName(), null);
			deployer.link(mos.getManagingOffice(), office);
		}, (objectName) -> {

			// Ensure able to obtain ManagedObjectSource MBean
			ManagedObjectSourceObjectMBean mbean = getMBean(objectName, ManagedObjectSourceObjectMBean.class);
			assertEquals("Incorrect Mbean value", "ManagedObject Test", mbean.getMBeanValue());

			// Change value to ensure correct instance
			managedObjectSource.mbean.mbeanValue = "ManagedObject changed";
			assertEquals("Incorrect changed MBean value", "ManagedObject changed", mbean.getMBeanValue());
		});
	}

	public static interface ManagedObjectSourceObjectMBean {
		String getMBeanValue();
	}

	public static class ManagedObjectSourceObject implements ManagedObjectSourceObjectMBean {

		private String mbeanValue = "ManagedObject Test";

		@Override
		public String getMBeanValue() {
			return this.mbeanValue;
		}
	}

	@TestSource
	public static class TestManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements MBeanFactory {

		private final ManagedObjectSourceObject mbean = new ManagedObjectSourceObject();

		/*
		 * ================ MBeanFactory =======================
		 */

		@Override
		public Object createMBean() {
			return this.mbean;
		}

		/*
		 * =============== ManagedObjectSource ===================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Object.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not require managed object");
			return null;
		}
	}

	/**
	 * Ensure can register {@link SupplierSource} as MBean.
	 */
	public void testRegisterSupplier() throws Exception {
		TestSupplierSource supplierSource = new TestSupplierSource();
		this.doTestInOfficeFloor(SupplierSource.class, "SUPPLIER", (deployer, context) -> {
			OfficeFloorSupplier supplier = deployer.addSupplier("SUPPLIER", supplierSource);
			OfficeFloorManagedObjectSource mos = supplier.getOfficeFloorManagedObjectSource("MOS", null,
					Object.class.getName());
			DeployedOffice office = deployer.addDeployedOffice("OFFICE", TestOfficeSource.class.getName(), null);
			deployer.link(mos.getManagingOffice(), office);
		}, (objectName) -> {

			// Ensure able to obtain SupplierSource MBean
			TestSupplierSourceMBean mbean = getMBean(objectName, TestSupplierSourceMBean.class);
			assertEquals("Incorrect Mbean value", "Supplier Test", mbean.getSupplierValue());

			// Change value to ensure correct instance
			supplierSource.mbeanValue = "Supplier changed";
			assertEquals("Incorrect changed MBean value", "Supplier changed", mbean.getSupplierValue());

			// Ensure able to obtain ManagedObjectSource MBean
			ManagedObjectSourceObjectMBean mosMBean = getMBean(getObjectName(ManagedObjectSource.class, "MOS"),
					ManagedObjectSourceObjectMBean.class);
			assertEquals("Incorrect Managed Object Source MBean value", "ManagedObject Test", mosMBean.getMBeanValue());
		});
	}

	public static interface TestSupplierSourceMBean {
		String getSupplierValue();
	}

	@TestSource
	public static class TestSupplierSource extends AbstractSupplierSource implements TestSupplierSourceMBean {

		private String mbeanValue = "Supplier Test";

		private TestManagedObjectSource managedObjectSource = new TestManagedObjectSource();

		/*
		 * ================== TestSupplierSourceMBean ===============
		 */

		@Override
		public String getSupplierValue() {
			return this.mbeanValue;
		}

		/*
		 * ====================== SupplierSource ====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {
			context.addManagedObjectSource(null, Object.class, this.managedObjectSource);
		}

		@Override
		public void terminate() {
			// nothing to terminate
		}
	}

	/**
	 * Ensure can register {@link ManagedObjectPool} as MBean.
	 */
	public void testRegisterManagedObjectPool() throws Exception {
		TestManagedObjectPoolSource managedObjectPoolSource = new TestManagedObjectPoolSource();
		this.doTestInOfficeFloor(ManagedObjectPoolSource.class, "POOL", (deployer, context) -> {
			OfficeFloorManagedObjectSource mos = deployer.addManagedObjectSource("MANAGED_OBJECT_SOURCE",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, CompileManagedObject.class.getName());
			DeployedOffice office = deployer.addDeployedOffice("OFFICE", TestOfficeSource.class.getName(), null);
			deployer.link(mos.getManagingOffice(), office);
			OfficeFloorManagedObjectPool pool = deployer.addManagedObjectPool("POOL", managedObjectPoolSource);
			deployer.link(mos, pool);
		}, (objectName) -> {

			// Ensure able to obtain ManagedObjectPoolSource MBean
			TestManagedObjectPoolSourceMBean mbean = getMBean(objectName, TestManagedObjectPoolSourceMBean.class);
			assertEquals("Incorrect Mbean value", "ManagedObjectPool Test", mbean.getPoolValue());

			// Change value to ensure correct instance
			managedObjectPoolSource.mbeanValue = "ManagedObjectPool changed";
			assertEquals("Incorrect changed MBean value", "ManagedObjectPool changed", mbean.getPoolValue());
		});
	}

	public static class CompileManagedObject {
	}

	public static interface TestManagedObjectPoolSourceMBean {
		String getPoolValue();
	}

	@TestSource
	public static class TestManagedObjectPoolSource extends AbstractManagedObjectPoolSource
			implements TestManagedObjectPoolSourceMBean, ManagedObjectPoolFactory {

		private String mbeanValue = "ManagedObjectPool Test";

		/*
		 * ================= MBean =================================
		 */

		@Override
		public String getPoolValue() {
			return this.mbeanValue;
		}

		/*
		 * ============== ManagedObjectPoolSource ===================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext context) throws Exception {
			context.setPooledObjectType(Object.class);
			context.setManagedObjectPoolFactory(this);
		}

		/*
		 * ============== ManagedObjectPoolFactory ===================
		 */

		@Override
		public ManagedObjectPool createManagedObjectPool(ManagedObjectPoolContext managedObjectPoolContext) {
			return null;
		}
	}

	/**
	 * Ensure can register {@link TeamSource} as MBean.
	 */
	public void testRegisterTeamSource() throws Exception {
		TestTeamSource teamSource = new TestTeamSource();
		this.doTestInOfficeFloor(TeamSource.class, "TEAM", (deployer, context) -> {
			deployer.addTeam("TEAM", teamSource);
		}, (objectName) -> {

			// Ensure able to obtain TeamSource MBean
			TestTeamSourceMBean mbean = getMBean(objectName, TestTeamSourceMBean.class);
			assertEquals("Incorrect Mbean value", "Team Test", mbean.getTeamValue());

			// Change value to ensure correct instance
			teamSource.mbeanValue = "Team changed";
			assertEquals("Incorrect changed MBean value", "Team changed", mbean.getTeamValue());
		});
	}

	public static interface TestTeamSourceMBean {
		String getTeamValue();
	}

	@TestSource
	public static class TestTeamSource extends AbstractTeamSource implements TestTeamSourceMBean {

		private String mbeanValue = "Team Test";

		/*
		 * ================ TestTeamSourceMBean =====================
		 */

		@Override
		public String getTeamValue() {
			return this.mbeanValue;
		}

		/*
		 * ================ TeamSource ==============================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			return new PassiveTeamSource().createTeam(context);
		}
	}

	/**
	 * Ensure can register {@link ExecutiveSource} as MBean.
	 */
	public void testRegisterExecutiveSource() throws Exception {
		TestExecutiveSource executiveSource = new TestExecutiveSource();
		this.doTestInOfficeFloor(ExecutiveSource.class, "Executive", (deployer, context) -> {
			deployer.setExecutive(executiveSource);
		}, (objectName) -> {

			// Ensure able to obtain ExecutiveSource MBean
			TestExecutiveSourceMBean mbean = getMBean(objectName, TestExecutiveSourceMBean.class);
			assertEquals("Incorrect Mbean value", "Team Executive", mbean.getExecutiveValue());

			// Change value to ensure correct instance
			executiveSource.mbeanValue = "Executive changed";
			assertEquals("Incorrect changed MBean value", "Executive changed", mbean.getExecutiveValue());
		});
	}

	public static interface TestExecutiveSourceMBean {
		String getExecutiveValue();
	}

	@TestSource
	public static class TestExecutiveSource extends AbstractExecutiveSource implements TestExecutiveSourceMBean {

		private String mbeanValue = "Team Executive";

		/*
		 * ================ TestExecutiveSourceMBean =====================
		 */

		@Override
		public String getExecutiveValue() {
			return this.mbeanValue;
		}

		/*
		 * ================ ExecutiveSource ==============================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			return new DefaultExecutive().createExecutive(context);
		}
	}

	/**
	 * Ensure can register {@link AdministrationSource} as MBean.
	 */
	public void testRegisterAdministrationSource() throws Exception {
		TestAdministrationSource administrationSource = new TestAdministrationSource();
		this.doTestInOffice(AdministrationSource.class, "OFFICE.ADMINISTRATION", (architect, context) -> {
			OfficeAdministration administration = architect.addOfficeAdministration("ADMINISTRATION",
					administrationSource);
			OfficeSection section = architect.addOfficeSection("SECTION", ClassSectionSource.class.getName(),
					SectionFunction.class.getName());
			section.getOfficeSectionFunction("function").addPreAdministration(administration);
		}, (objectName) -> {

			// Ensure able to obtain TeamSource MBean
			TestAdministrationSourceMBean mbean = getMBean(objectName, TestAdministrationSourceMBean.class);
			assertEquals("Incorrect Mbean value", "Administration Test", mbean.getAdministrationValue());

			// Change value to ensure correct instance
			administrationSource.mbeanValue = "Administration changed";
			assertEquals("Incorrect changed MBean value", "Administration changed", mbean.getAdministrationValue());
		});
	}

	public static class SectionFunction {
		public void function() {
		}
	}

	public static interface TestAdministrationSourceMBean {
		String getAdministrationValue();
	}

	@TestSource
	public static class TestAdministrationSource extends AbstractAdministrationSource<Object, None, None>
			implements AdministrationFactory<Object, None, None>, TestAdministrationSourceMBean {

		private String mbeanValue = "Administration Test";

		/*
		 * =============== TestAdministrationSourceMBean ============
		 */

		@Override
		public String getAdministrationValue() {
			return this.mbeanValue;
		}

		/*
		 * =================== AdministrationSource ==================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<Object, None, None> context) throws Exception {
			context.setExtensionInterface(Object.class);
			context.setAdministrationFactory(this);
		}

		/*
		 * ================== AdministrationFactory ==================
		 */

		@Override
		public Administration<Object, None, None> createAdministration() throws Throwable {
			return null;
		}
	}

	/**
	 * Ensure can register {@link GovernanceSource} as MBean.
	 */
	public void testRegisterGovernanceSource() throws Exception {
		TestGovernanceSource governanceSource = new TestGovernanceSource();
		this.doTestInOffice(GovernanceSource.class, "OFFICE.GOVERNANCE", (architect, context) -> {
			architect.addOfficeGovernance("GOVERNANCE", governanceSource);
		}, (objectName) -> {

			// Ensure able to obtain GovernanceSource MBean
			TestGovernanceSourceMBean mbean = getMBean(objectName, TestGovernanceSourceMBean.class);
			assertEquals("Incorrect Mbean value", "Governance Test", mbean.getGovernanceValue());

			// Change value to ensure correct instance
			governanceSource.mbeanValue = "Governance changed";
			assertEquals("Incorrect change MBean value", "Governance changed", governanceSource.getGovernanceValue());
		});
	}

	public static interface TestGovernanceSourceMBean {
		String getGovernanceValue();
	}

	@TestSource
	public static class TestGovernanceSource extends AbstractGovernanceSource<Object, None>
			implements GovernanceFactory<Object, None>, TestGovernanceSourceMBean {

		private String mbeanValue = "Governance Test";

		/*
		 * ================= TestGovernanceSourceMBean ==============
		 */

		@Override
		public String getGovernanceValue() {
			return this.mbeanValue;
		}

		/*
		 * ====================== GovernanceSource ==================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<Object, None> context) throws Exception {
			context.setExtensionInterface(Object.class);
			context.setGovernanceFactory(this);
		}

		/*
		 * ====================== GovernanceFactory =================
		 */

		@Override
		public Governance<Object, None> createGovernance() throws Throwable {
			return null;
		}
	}

	/**
	 * Ensure can register {@link SectionSource} as MBean.
	 */
	public void testRegisterSectionSource() throws Exception {
		TestSectionSource sectionSource = new TestSectionSource();
		this.doTestInOffice(SectionSource.class, "OFFICE.SECTION", (architect, context) -> {
			architect.addOfficeSection("SECTION", sectionSource, null);
		}, (objectName) -> {

			// Ensure able to obtain SectionSource MBean
			TestSectionSourceMBean mbean = getMBean(objectName, TestSectionSourceMBean.class);
			assertEquals("Incorrect Mbean value", "Section Test", mbean.getSectionValue());

			// Change value to ensure correct instance
			sectionSource.mbeanValue = "Section changed";
			assertEquals("Incorrect changed MBean value", "Section changed", mbean.getSectionValue());
		});
	}

	public static interface TestSectionSourceMBean {
		String getSectionValue();
	}

	@TestSource
	public static class TestSectionSource extends AbstractSectionSource implements TestSectionSourceMBean {

		private String mbeanValue = "Section Test";

		/*
		 * ===================== TestSectionSourceMBean ==============
		 */

		@Override
		public String getSectionValue() {
			return this.mbeanValue;
		}

		/*
		 * ========================= SectionSource ===================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
		}
	}

	/**
	 * Ensure can register {@link ManagedFunctionSource} as MBean.
	 */
	public void testRegisterManagedFunctionSource() throws Exception {
		TestManagedFunctionSource managedFunctionSource = new TestManagedFunctionSource();
		this.doTestInOffice(ManagedFunctionSource.class, "OFFICE.SECTION.FUNCTION", (architect, context) -> {
			architect.addOfficeSection("SECTION", new TestFunctionSectionSource(managedFunctionSource), null);
		}, (objectName) -> {

			// Ensure able to obtain SectionSource MBean
			TestManagedFunctionSourceMBean mbean = getMBean(objectName, TestManagedFunctionSourceMBean.class);
			assertEquals("Incorrect Mbean value", "Function Test", mbean.getFunctionValue());

			// Change value to ensure correct instance
			managedFunctionSource.mbeanValue = "Function changed";
			assertEquals("Incorrect changed MBean value", "Function changed", mbean.getFunctionValue());
		});
	}

	public static interface TestManagedFunctionSourceMBean {
		String getFunctionValue();
	}

	public static class TestFunctionSectionSource extends AbstractSectionSource {

		private final TestManagedFunctionSource managedFunctionSource;

		public TestFunctionSectionSource(TestManagedFunctionSource managedFunctionSource) {
			this.managedFunctionSource = managedFunctionSource;
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
			SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("FUNCTION",
					this.managedFunctionSource);
			namespace.addSectionFunction("function", "FUNCTION");
		}
	}

	@TestSource
	public static class TestManagedFunctionSource extends AbstractManagedFunctionSource
			implements TestManagedFunctionSourceMBean, ManagedFunctionFactory<None, None> {

		private String mbeanValue = "Function Test";

		@Override
		public String getFunctionValue() {
			return this.mbeanValue;
		}

		/*
		 * ================ TestManagedFunctionSourceMBean ============
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {
			functionNamespaceTypeBuilder.addManagedFunctionType("FUNCTION", None.class, None.class)
					.setFunctionFactory(this);
		}

		/*
		 * =================== ManagedFunctionFactory ==================
		 */

		@Override
		public ManagedFunction<None, None> createManagedFunction() throws Throwable {
			return null;
		}
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param type              Type of MBean.
	 * @param name              Name of MBean.
	 * @param extendOfficeFloor {@link OfficeFloorExtensionService}.
	 * @param testLogic         {@link TestLogic}.
	 */
	private void doTestInOfficeFloor(Class<?> type, String name, OfficeFloorExtensionService extendOfficeFloor,
			TestLogic testLogic) throws Exception {

		// Obtain the object name
		ObjectName objectName = getObjectName(type, name);

		// Compile and open the OfficeFloor
		ExtendOfficeFloor compile = new ExtendOfficeFloor();
		compile.getOfficeFloorCompiler().setOfficeFloorSource(this.officeFloorSource);
		compile.getOfficeFloorCompiler().setMBeanRegistrator(MBeanRegistrator.getPlatformMBeanRegistrator());
		OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor(extendOfficeFloor);

		try {

			// Execute the test logic
			testLogic.execute(objectName);

		} finally {
			// Close the OfficeFloor (unregistering the MBeans)
			officeFloor.closeOfficeFloor();
			assertMBeanUnregistered(objectName);
		}
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param type         Type of MBean.
	 * @param name         Name of MBean.
	 * @param extendOffice {@link OfficeExtensionService}.
	 * @param testLogic    {@link TestLogic}.
	 */
	private void doTestInOffice(Class<?> type, String name, OfficeExtensionService extendOffice, TestLogic testLogic)
			throws Exception {

		// Obtain the object name
		ObjectName objectName = getObjectName(type, name);

		// Compile and open the Office
		CompileOffice compile = new CompileOffice();
		compile.getOfficeFloorCompiler().setMBeanRegistrator(MBeanRegistrator.getPlatformMBeanRegistrator());
		OfficeFloor officeFloor = compile.compileAndOpenOffice(extendOffice);

		try {

			// Execute the test logic
			testLogic.execute(objectName);

		} finally {
			// Close the OfficeFloor (unregistering the MBeans)
			officeFloor.closeOfficeFloor();
			assertMBeanUnregistered(objectName);
		}
	}

	public static interface TestLogic {
		void execute(ObjectName objectName) throws Exception;
	}

	/**
	 * Obtains the MBean.
	 * 
	 * @param objectName MBean {@link ObjectName}.
	 * @param proxyType  Proxy type for MBean.
	 * @return MBean.
	 * @throws Exception If fails to obtain the MBean.
	 */
	private static <B> B getMBean(ObjectName objectName, Class<B> proxyType) throws Exception {
		MBeanServerConnection connection = ManagementFactory.getPlatformMBeanServer();
		return JMX.newMBeanProxy(connection, objectName, proxyType);
	}

	/**
	 * Ensures the MBean has been unregistered.
	 * 
	 * @param objectName MBean {@link ObjectName}.
	 * @throws Exception If fails to check MBean.
	 */
	private static void assertMBeanUnregistered(ObjectName objectName) throws Exception {
		MBeanServerConnection connection = ManagementFactory.getPlatformMBeanServer();
		assertFalse("MBean should be unregistered", connection.isRegistered(objectName));
	}

	/**
	 * Obtains the {@link ObjectName}.
	 * 
	 * @param type Type of MBean.
	 * @param name Name of MBean.
	 * @return {@link ObjectName}.
	 * @throws Exception If fails to create {@link ObjectName}.
	 */
	private static ObjectName getObjectName(Class<?> type, String name) throws Exception {
		return new ObjectName("net.officefloor:type=" + type.getName() + ",name=" + name);
	}

}
