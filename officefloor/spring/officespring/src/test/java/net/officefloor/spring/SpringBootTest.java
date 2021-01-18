/*-
 * #%L
 * Spring Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import net.officefloor.compile.impl.structure.SuppliedManagedObjectSourceNodeImpl;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.state.autowire.AutoWireStateManager;
import net.officefloor.compile.state.autowire.AutoWireStateManagerFactory;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.officefloor.CompileOfficeFloorExtension;
import net.officefloor.compile.test.supplier.SupplierLoaderUtil;
import net.officefloor.compile.test.supplier.SupplierTypeBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.notscan.ExtensionBean;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.spring.extension.SpringSupplierExtension;
import net.officefloor.web.build.HttpInput;
import net.officefloor.woof.WoOF;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure can integrate Spring via boot.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class SpringBootTest {

	/**
	 * {@link MockTestSupport}.
	 */
	private final MockTestSupport mocks = new MockTestSupport();

	/**
	 * {@link ConfigurableApplicationContext}.
	 */
	private ConfigurableApplicationContext context;

	/**
	 * Mock {@link OfficeFloorInterfaceDependency}.
	 */
	private OfficeFloorInterfaceDependency officeFloorInterfaceDependency;

	/**
	 * Mock unqualified {@link OfficeFloorInterfaceDependency}.
	 */
	private OfficeFloorInterfaceDependency unqualifiedInterfaceDependency;

	/**
	 * {@link OfficeFloorObjectDependency}.
	 */
	private final OfficeFloorObjectDependency officeFloorObjectDependency = new OfficeFloorObjectDependency();

	@BeforeEach
	protected void setUp() throws Exception {
		this.officeFloorInterfaceDependency = this.mocks.createMock(OfficeFloorInterfaceDependency.class);
		this.unqualifiedInterfaceDependency = this.mocks.createMock(OfficeFloorInterfaceDependency.class);
		this.context = SpringSupplierSource.runInContext(() -> {
			return SpringApplication.run(MockSpringBootConfiguration.class);
		}, (qualifier, objectType) -> {
			if (OfficeFloorInterfaceDependency.class.equals(objectType)) {
				return "QUALIFIED".equals(qualifier) ? this.officeFloorInterfaceDependency
						: this.unqualifiedInterfaceDependency;
			} else if (OfficeFloorObjectDependency.class.equals(objectType)) {
				return this.officeFloorObjectDependency;
			} else {
				return fail("Incorrect object type " + objectType.getName());
			}
		});
	}

	@AfterEach
	protected void tearDown() throws Exception {
		this.context.close();
	}

	/**
	 * Loads the {@link OfficeFloor} dependencies.
	 * 
	 * @param office {@link OfficeArchitect}.
	 */
	private void loadOfficeFloorDependencies(OfficeArchitect office) {
		Singleton.load(office, "QualifiedInterface", this.officeFloorInterfaceDependency)
				.addTypeQualification("QUALIFIED", OfficeFloorInterfaceDependency.class.getName());
		Singleton.load(office, "UnqualifiedInterface", this.unqualifiedInterfaceDependency);
		Singleton.load(office, this.officeFloorObjectDependency);
	}

	/**
	 * Ensure can configure Spring bean.
	 */
	@Test
	public void springConfiguredBeans() {

		// Ensure can obtain simple bean
		SimpleBean simple = this.context.getBean(SimpleBean.class);
		assertNotNull(simple, "Should obtain simple bean");
		assertEquals("SIMPLE", simple.getValue(), "Incorrect simple bean");

		// Ensure can obtain complex bean
		ComplexBean complex = this.context.getBean(ComplexBean.class);
		assertNotNull(complex, "Should obtain complex bean");
		assertSame(simple, complex.getSimpleBean(), "Should have simple bean injected");

		// Ensure can obtain qualified beans
		Consumer<String> assertQualifiedBean = (qualifier) -> {
			QualifiedBean bean = this.context.getBean("qualified" + qualifier, QualifiedBean.class);
			assertNotNull(bean, "Should obtain qualified bean");
			assertEquals(qualifier, bean.getValue(), "Incorrect qualfiied value");
		};
		assertQualifiedBean.accept("One");
		assertQualifiedBean.accept("Two");
		assertQualifiedBean.accept("Three");
		assertQualifiedBean.accept("Four");

		// Ensure obtain OfficeFloor qualified dependency
		OfficeFloorInterfaceDependency officeFloorQualified = this.context
				.getBean(OfficeFloorInterfaceDependency.class);
		assertSame(this.officeFloorInterfaceDependency, officeFloorQualified,
				"Should pull in the OfficeFloor interface dependency");

		// Ensure obtain OfficeFloor unqualified dependency
		OfficeFloorObjectDependency officeFloorUnqualified = this.context.getBean(OfficeFloorObjectDependency.class);
		assertSame(this.officeFloorObjectDependency, officeFloorUnqualified,
				"Should pull in the OfficeFloor object dependency");
	}

	/**
	 * Ensure correct specification.
	 */
	@Test
	public void specification() {
		SupplierLoaderUtil.validateSpecification(SpringSupplierSource.class, "configuration.class",
				"Configuration Class");
	}

	/**
	 * Ensure correct type.
	 */
	@Test
	public void type() throws Throwable {

		// Indicate the registered beans
		System.out.println("Beans:");
		for (String name : this.context.getBeanDefinitionNames()) {
			System.out.println("  " + name);
		}

		// Load all the beans as managed object sources
		Map<Class<?>, List<String>> beanNamesByType = new HashMap<>();
		for (String name : this.context.getBeanDefinitionNames()) {
			Class<?> beanClass = this.context.getBean(name).getClass();
			List<String> beanNames = beanNamesByType.get(beanClass);
			if (beanNames == null) {
				beanNames = new LinkedList<>();
				beanNamesByType.put(beanClass, beanNames);
			}
			beanNames.add(name);
		}

		// Create the expected type
		SupplierTypeBuilder type = SupplierLoaderUtil.createSupplierTypeBuilder();

		// Load the expected supplier thread local
		type.addSupplierThreadLocal("QUALIFIED", OfficeFloorInterfaceDependency.class);
		type.addSupplierThreadLocal(null, OfficeFloorObjectDependency.class);

		// Load the expected internal supplier
		type.addInternalSupplier();

		// Load the application context
		type.addSuppliedManagedObjectSource(null, AnnotationConfigApplicationContext.class,
				new ApplicationContextManagedObjectSource(null));

		// Load the expected supplied managed object types
		NEXT_BEAN: for (Class<?> beanClass : beanNamesByType.keySet()) {

			// Ignore the OfficeFloor managed objects
			if ((OfficeFloorInterfaceDependency.class.isAssignableFrom(beanClass))
					|| (OfficeFloorObjectDependency.class.isAssignableFrom(beanClass))) {
				continue NEXT_BEAN;
			}

			// Load the Spring beans
			List<String> beanNames = beanNamesByType.get(beanClass);
			switch (beanNames.size()) {
			case 1:
				// No qualification as only type
				type.addSuppliedManagedObjectSource(null, beanClass,
						new SpringBeanManagedObjectSource(null, beanClass, this.context, null));
				break;
			default:
				// Load with qualifications
				for (String beanName : beanNames) {
					type.addSuppliedManagedObjectSource(beanName, beanClass,
							new SpringBeanManagedObjectSource(beanName, beanClass, this.context, null));
				}
			}
		}

		// Validate the type
		SupplierType supplierType = SupplierLoaderUtil.validateSupplierType(type, SpringSupplierSource.class,
				SpringSupplierSource.CONFIGURATION_CLASS_NAME, MockSpringBootConfiguration.class.getName());

		// Create listing of beans to ensure configured beans available
		Map<String, SuppliedManagedObjectSourceType> beans = new HashMap<>();
		for (SuppliedManagedObjectSourceType mosType : supplierType.getSuppliedManagedObjectTypes()) {
			beans.put(SuppliedManagedObjectSourceNodeImpl.getSuppliedManagedObjectSourceName(mosType.getQualifier(),
					mosType.getObjectType().getName()), mosType);
		}

		// Ensure simple bean available
		SuppliedManagedObjectSourceType simpleType = beans.get(SimpleBean.class.getName());
		Object simpleBean = new ManagedObjectUserStandAlone()
				.sourceManagedObject(simpleType.getManagedObjectSource(), false).getObject();
		assertTrue(simpleBean instanceof SimpleBean, "Should obtain simple bean");

		// Ensure complex bean available
		SuppliedManagedObjectSourceType complexType = beans.get(ComplexBean.class.getName());
		Object complexBean = new ManagedObjectUserStandAlone()
				.sourceManagedObject(complexType.getManagedObjectSource(), false).getObject();
		assertTrue(complexBean instanceof ComplexBean, "Should obtain complex bean");
		assertSame(simpleBean, ((ComplexBean) complexBean).getSimpleBean(), "Should follow Spring singletons");

		// Ensure qualified beans available
		BiConsumer<String, Class<?>> verifyQualifiedBean = (qualifier, beanType) -> {
			try {
				String qualifierName = (QualifiedBean.class.equals(beanType)) ? "qualified" + qualifier : null;
				String qualifiedName = SuppliedManagedObjectSourceNodeImpl
						.getSuppliedManagedObjectSourceName(qualifierName, beanType.getName());
				SuppliedManagedObjectSourceType qualifiedType = beans.get(qualifiedName);
				assertNotNull(qualifiedType, "Should have qualified type: " + qualifiedName);
				Object qualifiedBean = new ManagedObjectUserStandAlone()
						.sourceManagedObject(qualifiedType.getManagedObjectSource(), false).getObject();
				assertTrue(qualifiedBean instanceof QualifiedBean, "Should obtain qualified bean: " + qualifier);
				assertEquals(qualifier, ((QualifiedBean) qualifiedBean).getValue(), "Incorrect qualified bean");
			} catch (Throwable ex) {
				fail(ex);
			}
		};
		verifyQualifiedBean.accept("One", QualifiedBean.class);
		verifyQualifiedBean.accept("Two", QualifiedBean.class);
		verifyQualifiedBean.accept("Three", ComponentQualifiedBeanThree.class);
		verifyQualifiedBean.accept("Four", ComponentQualifiedBeanFour.class);

		// Should not have OfficeFloor managed object bean
		assertNull(beans.get(OfficeFloorInterfaceDependency.class.getName()),
				"Should not have OfficeFloor interface supplied objects");
		assertNull(beans.get(OfficeFloorObjectDependency.class.getName()),
				"Should not have OfficeFloor object supplied objects");
	}

	/**
	 * Ensure can integrate in Spring beans to {@link OfficeFloor}.
	 */
	@Test
	public void integrateSimpleSpringBean() throws Throwable {
		this.doIntegrateSimpleSpringBeanTest(SpringSupplierSource.class.getName());
	}

	/**
	 * Ensure {@link SpringSupplierSourceService} registered providing alias.
	 */
	@Test
	public void springSupplierSourceAlias() throws Throwable {
		this.doIntegrateSimpleSpringBeanTest(SpringSupplierSourceService.ALIAS);
	}

	/**
	 * Undertakes test to integrate simple Spring bean.
	 * 
	 * @param supplierSourceName Name of the {@link SupplierSource}.
	 */
	private void doIntegrateSimpleSpringBeanTest(String supplierSourceName) throws Throwable {

		// Configure OfficeFloor to auto-wire in Spring beans
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Add Spring supplier
			office.addSupplier("SPRING", supplierSourceName).addProperty(SpringSupplierSource.CONFIGURATION_CLASS_NAME,
					MockSpringBootConfiguration.class.getName());

			// Add the section
			context.addSection("SECTION", IntegrateSimpleSpringBean.class);

			// Add the OfficeFloor managed objects
			this.loadOfficeFloorDependencies(office);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Ensure pulls in simple bean via Spring
			IntegrateSimpleSpringBean.simpleBean = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.functionSimple", null);
			assertNotNull(IntegrateSimpleSpringBean.simpleBean, "Should pull in simple bean from Spring");
			assertEquals("SIMPLE", IntegrateSimpleSpringBean.simpleBean.getValue(), "Incorrect simple bean");

			// Ensure pulls in complex bean via Spring
			IntegrateSimpleSpringBean.complexBean = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.functionComplex", null);
			assertNotNull(IntegrateSimpleSpringBean.complexBean, "Should pull in complex bean from Spring");
			assertSame(IntegrateSimpleSpringBean.simpleBean, IntegrateSimpleSpringBean.complexBean.getSimpleBean(),
					"Incorrect complex bean");
		}
	}

	public static class IntegrateSimpleSpringBean {

		private static SimpleBean simpleBean;

		private static ComplexBean complexBean;

		public void functionSimple(SimpleBean bean) {
			simpleBean = bean;
		}

		public void functionComplex(ComplexBean bean) {
			complexBean = bean;
		}
	}

	/**
	 * Ensure can integrate in a qualified Spring bean.
	 */
	@Test
	public void integrateQualfiedSpringBean() throws Throwable {

		// Configure OfficeFloor to auto-wire in Spring beans
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Add Spring supplier
			SpringSupplierSource.configure(office, MockSpringBootConfiguration.class);

			// Add the section
			context.addSection("SECTION", IntegrateQualifiedSpringBean.class);

			// Add the OfficeFloor managed objects
			this.loadOfficeFloorDependencies(office);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Ensure pulls in bean via Spring
			Consumer<String> verifyQualification = (qualifier) -> {
				try {
					IntegrateQualifiedSpringBean.qualifiedBean = null;
					CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.function" + qualifier, null);
					assertNotNull(IntegrateQualifiedSpringBean.qualifiedBean, "Should pull in bean from Spring");
					assertEquals(qualifier, IntegrateQualifiedSpringBean.qualifiedBean.getValue(),
							"Incorrect qualified bean");
				} catch (Throwable ex) {
					fail(ex);
				}
			};
			verifyQualification.accept("One");
			verifyQualification.accept("Two");
			verifyQualification.accept("Three");
			verifyQualification.accept("Four");
		}
	}

	public static class IntegrateQualifiedSpringBean {

		private static QualifiedBean qualifiedBean;

		public void functionOne(@Qualified("qualifiedOne") QualifiedBean bean) {
			qualifiedBean = bean;
		}

		public void functionTwo(@Qualified("qualifiedTwo") QualifiedBean bean) {
			qualifiedBean = bean;
		}

		public void functionThree(@Qualified("qualifiedThree") ComponentQualifiedBeanThree bean) {
			qualifiedBean = bean;
		}

		public void functionFour(@Qualified("qualifiedFour") ComponentQualifiedBeanFour bean) {
			qualifiedBean = bean;
		}
	}

	/**
	 * Ensure can integrate in {@link OfficeFloor} {@link ManagedObject} instances
	 * into Spring.
	 */
	@Test
	public void integrateSpringBeanDependencyToManagedObject() throws Throwable {

		// Record obtaining value
		this.mocks.recordReturn(this.officeFloorInterfaceDependency, this.officeFloorInterfaceDependency.getValue(),
				"OfficeFloorInterface");
		this.mocks.replayMockObjects();

		// Configure OfficeFloor to auto-wire in Spring beans
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Add Spring supplier
			office.addSupplier("SPRING", SpringSupplierSource.class.getName()).addProperty(
					SpringSupplierSource.CONFIGURATION_CLASS_NAME, MockSpringBootConfiguration.class.getName());

			// Add the OfficeFloor managed object
			this.loadOfficeFloorDependencies(office);

			// Add the section
			context.addSection("SECTION", IntegrateOfficeFloorManagedObject.class);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Ensure can source the Spring dependency from OfficeFloor
			IntegrateOfficeFloorManagedObject.dependentBean = null;
			IntegrateOfficeFloorManagedObject.officeFloorInterfaceDependency = null;
			IntegrateOfficeFloorManagedObject.officeFloorObjectDependency = null;
			IntegrateOfficeFloorManagedObject.value = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.function", null);
			assertNotNull(IntegrateOfficeFloorManagedObject.dependentBean, "Should pull in dependent bean from Spring");

			// Confirm OfficeFloor interface dependency
			assertNotNull(IntegrateOfficeFloorManagedObject.officeFloorInterfaceDependency,
					"Should have interface dependency available");
			assertNotSame(this.officeFloorInterfaceDependency,
					IntegrateOfficeFloorManagedObject.officeFloorInterfaceDependency,
					"Should proxy interface dependency from OfficeFloor");

			// Confirm OfficeFloor object dependency
			assertNotNull(IntegrateOfficeFloorManagedObject.officeFloorObjectDependency,
					"Should have object dependency available");
			assertNotSame(this.officeFloorObjectDependency,
					IntegrateOfficeFloorManagedObject.officeFloorObjectDependency,
					"Should proxy object dependency from OfficeFloor");

			// Confirm able to get values
			assertEquals("OfficeFloorInterface OfficeFloorObject", IntegrateOfficeFloorManagedObject.value,
					"Should access dependency from OfficeFloor");
		}

		// Ensure OfficeFloor object used
		this.mocks.verifyMockObjects();
	}

	public static class IntegrateOfficeFloorManagedObject {

		private static DependentBean dependentBean;

		private static OfficeFloorInterfaceDependency officeFloorInterfaceDependency;

		private static OfficeFloorObjectDependency officeFloorObjectDependency;

		private static String value;

		public void function(DependentBean bean) {
			dependentBean = bean;
			officeFloorInterfaceDependency = bean.getInterfaceDependency();
			officeFloorObjectDependency = bean.getObjectDependency();
			value = officeFloorInterfaceDependency.getValue() + " " + officeFloorObjectDependency.getMessage();
		}
	}

	/**
	 * Ensure can inject {@link ConfigurableApplicationContext}.
	 */
	@Test
	public void applicationContext() throws Throwable {

		// Configure OfficeFloor to auto-wire in Spring beans
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Add Spring supplier
			office.addSupplier("SPRING", SpringSupplierSource.class.getName()).addProperty(
					SpringSupplierSource.CONFIGURATION_CLASS_NAME, MockSpringBootConfiguration.class.getName());

			// Add the OfficeFloor managed object
			this.loadOfficeFloorDependencies(office);

			// Add the section
			context.addSection("SECTION", IntegrateApplicationContext.class);
		});
		Closure<ConfigurableApplicationContext> applicationContext = new Closure<>();
		SpringSupplierSource.captureApplicationContext(applicationContext, () -> {
			try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

				// Ensure can source the Application Context
				IntegrateApplicationContext.springContext = null;
				IntegrateApplicationContext.simpleBean = null;
				CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.function", null);
			}
			return null;
		});
		assertNotNull(IntegrateApplicationContext.springContext, "Should inject Application Context");
		assertSame(applicationContext.value, IntegrateApplicationContext.springContext,
				"Incorrect Application Context");
		assertNotNull(IntegrateApplicationContext.simpleBean, "Should be able to retrieve beans");
	}

	public static class IntegrateApplicationContext {

		private static ConfigurableApplicationContext springContext;

		private static SimpleBean simpleBean;

		public void function(ConfigurableApplicationContext applicationContext) {
			springContext = applicationContext;
			simpleBean = applicationContext.getBean(SimpleBean.class);
		}
	}

	/**
	 * Ensure can load {@link SpringSupplierExtension}.
	 */
	@Test
	public void springExtension() throws Throwable {

		// Ensure extension bean not on class path scanning
		String[] extensionBeanNames = this.context.getBeanNamesForType(ExtensionBean.class);
		assertEquals(0, extensionBeanNames.length,
				"INVALID TEST: Should not find extension bean in class path scanning");

		// Ensure beans registered
		final String SIMPLE_BEAN_NAME = "simpleBean";
		final String MANAGED_OBJECT_NAME = "officeFloorInterfaceDependency";
		assertNotNull(this.context.getBean(SIMPLE_BEAN_NAME), "Invalid test as no simple bean");
		assertNotNull(this.context.getBean(MANAGED_OBJECT_NAME), "Invalid test as no OfficeFloor managed object");

		// Record obtaining value
		this.mocks.recordReturn(this.unqualifiedInterfaceDependency, this.unqualifiedInterfaceDependency.getValue(),
				"EXTENSION");
		this.mocks.replayMockObjects();

		// Ensure active spring extension
		MockSpringSupplierExtension.isActive = true;
		MockSpringSupplierExtension.springBean = null;
		MockSpringSupplierExtension.officeFloorManagedObject = null;
		MockSpringSupplierExtension.availableTypes = null;
		MockSpringSupplierExtension.decoratedBeanTypes.clear();
		try {

			// Configure OfficeFloor to auto-wire in Spring beans
			CompileOfficeFloor compile = new CompileOfficeFloor();
			compile.officeFloor((context) -> {
				context.getOfficeFloorDeployer().addTeam("TEAM", OnePersonTeamSource.class.getName())
						.addTypeQualification(null, ComplexBean.class.getName());
			});
			compile.office((context) -> {
				OfficeArchitect office = context.getOfficeArchitect();
				office.enableAutoWireTeams();

				// Add Spring supplier
				SpringSupplierSource.configure(office, MockSpringBootConfiguration.class);

				// Add the OfficeFloor managed object
				this.loadOfficeFloorDependencies(office);

				// Add the section
				context.addSection("SECTION", ExtensionSection.class);
			});
			Closure<ConfigurableApplicationContext> applicationContext = new Closure<>();
			SpringSupplierSource.captureApplicationContext(applicationContext, () -> {
				try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

					// Ensure able to get available types
					AvailableType[] availableTypes = MockSpringSupplierExtension.availableTypes;
					assertNotNull(availableTypes, "Should have available types");
					boolean isOfficeFloorInterfaceDependencyListed = false;
					boolean isOfficeFloorObjectDependencyListed = false;
					for (AvailableType availableType : availableTypes) {
						if (("QUALIFIED".equals(availableType.getQualifier()))
								&& (OfficeFloorInterfaceDependency.class.isAssignableFrom(availableType.getType()))) {
							isOfficeFloorInterfaceDependencyListed = true;
						}
						if (OfficeFloorObjectDependency.class.isAssignableFrom(availableType.getType())) {
							isOfficeFloorObjectDependencyListed = true;
						}
					}
					assertTrue(isOfficeFloorInterfaceDependencyListed,
							"Should have OfficeFloorInterfaceDependency listed as available");
					assertTrue(isOfficeFloorObjectDependencyListed,
							"Should have OfficeFloorObjectDependency listed as available");

					// Invoke the function
					ExtensionSection.extension = null;
					ExtensionSection.serviceThread = null;
					ExtensionSection.threadLocalValue = null;
					LoadBean.loadCount.set(0);
					CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", null);

					// Ensure can load spring bean
					ComplexBean bean = applicationContext.value.getBean(ComplexBean.class);
					assertSame(bean, MockSpringSupplierExtension.springBean, "Should be able to load spring bean");

					// Ensure correct value passed
					assertNotNull(ExtensionSection.extension, "Should have extension bean");
					assertEquals("SIMPLE-EXTENSION", ExtensionSection.threadLocalValue, "Incorrect value");

					// Ensure have decorated beans
					assertTrue(MockSpringSupplierExtension.decoratedBeanTypes.size() > 0,
							"Should register decorated beans");
					assertTrue(
							SimpleBean.class.isAssignableFrom(
									MockSpringSupplierExtension.decoratedBeanTypes.get(SIMPLE_BEAN_NAME)),
							"Incorrect simple bean");
					assertNull(MockSpringSupplierExtension.decoratedBeanTypes.get(MANAGED_OBJECT_NAME),
							"Should not decorate officeFloorManagedObject");

					// Ensure after having invoked process, that added dependency loaded
					assertEquals(1, LoadBean.loadCount.get(), "Should load additional dependency");
				}
				return null;
			});

			// Verify
			this.mocks.verifyMockObjects();

		} finally {
			MockSpringSupplierExtension.isActive = false;
		}
	}

	public static class ExtensionSection {

		private static volatile ExtensionBean extension = null;

		private static volatile Thread serviceThread = null;

		private static volatile String threadLocalValue = null;

		@Next("next")
		public void service(SimpleBean bean, ExtensionBean extensionBean) {
			extension = extensionBean;
			serviceThread = Thread.currentThread();
			String value = bean.getValue() + "-" + MockSpringSupplierExtension.officeFloorManagedObject.getValue();
			MockSpringSupplierExtension.threadLocal.set(value);
		}

		public void next(ComplexBean bean) {
			assertNotNull(serviceThread, "Should have service thread");
			assertNotEquals(serviceThread, Thread.currentThread(), "Should be different thread");
			threadLocalValue = MockSpringSupplierExtension.threadLocal.get();
		}
	}

	/**
	 * Ensure able to capture the {@link ConfigurableApplicationContext}.
	 */
	@Test
	public void captureApplicationContext() throws Exception {

		// Capture the application context
		Closure<ConfigurableApplicationContext> applicationContext = new Closure<>();
		try (OfficeFloor officeFloor = SpringSupplierSource.captureApplicationContext(applicationContext, () -> {

			// Configure OfficeFloor to auto-wire in Spring beans
			CompileOfficeFloor compile = new CompileOfficeFloor();
			compile.office((context) -> {
				OfficeArchitect office = context.getOfficeArchitect();

				// Add Spring supplier
				office.addSupplier("SPRING", SpringSupplierSource.class.getName()).addProperty(
						SpringSupplierSource.CONFIGURATION_CLASS_NAME, MockSpringBootConfiguration.class.getName());

				// Add the OfficeFloor managed object
				this.loadOfficeFloorDependencies(office);
			});
			return compile.compileAndOpenOfficeFloor();
		})) {

			// Ensure have Spring application context
			assertNotNull(applicationContext.value, "Should have Spring application context");

			// Ensure able to use application context
			SimpleBean bean = applicationContext.value.getBean(SimpleBean.class);
			assertNotNull(bean, "Should have bean");
		}
	}

	/**
	 * Ensure able to obtain internal objects.
	 */
	@Test
	public void obtainInternalObjects() throws Throwable {

		// Configure OfficeFloor to auto-wire in Spring beans
		Closure<AutoWireStateManagerFactory> stateFactory = new Closure<>();
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.getOfficeFloorCompiler()
				.addAutoWireStateManagerVisitor((office, factory) -> stateFactory.value = factory);
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Add Spring supplier
			office.addSupplier("SPRING", SpringSupplierSource.class.getName()).addProperty(
					SpringSupplierSource.CONFIGURATION_CLASS_NAME, MockSpringBootConfiguration.class.getName());

			// Add the OfficeFloor managed object
			this.loadOfficeFloorDependencies(office);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
			try (AutoWireStateManager stateManager = stateFactory.value.createAutoWireStateManager()) {

				// Ensure indicate if no supplied object
				assertFalse(stateManager.isObjectAvailable(null, NotAvailableBean.class),
						"Should not find non-available object");

				// Internally supplied object should be available
				assertTrue(stateManager.isObjectAvailable(null, InternallySuppliedBean.class),
						"Internally supplied object should be available");

				// Obtain the internal supplied object
				InternallySuppliedBean bean = stateManager.getObject(null, InternallySuppliedBean.class, 0);
				assertNotNull(bean, "Should be able to obtain internally supplied bean");
			}
		}
	}

	public static class NotAvailableBean {
	}

	/**
	 * Ensure able to force starting {@link ConfigurableApplicationContext}.
	 */
	@Test
	public void forceStartSpring() throws Exception {

		// Capture the application context
		Closure<ConfigurableApplicationContext> capturedApplicationContext = new Closure<>();
		Closure<ConfigurableApplicationContext> forcedApplicationContext = new Closure<>();
		try (OfficeFloor officeFloor = SpringSupplierSource.captureApplicationContext(capturedApplicationContext,
				() -> {

					// Configure OfficeFloor to auto-wire in Spring beans
					CompileOfficeFloor compile = new CompileOfficeFloor();
					compile.office((context) -> {
						OfficeArchitect office = context.getOfficeArchitect();

						// Add Spring supplier
						office.addSupplier("SPRING", SpringSupplierSource.class.getName()).addProperty(
								SpringSupplierSource.CONFIGURATION_CLASS_NAME,
								MockSpringBootConfiguration.class.getName());

						// Add the OfficeFloor managed object
						this.loadOfficeFloorDependencies(office);
					});
					compile.section((context) -> {

						// Forced start Spring
						forcedApplicationContext.value = SpringSupplierSource.forceStartSpring(new AvailableType[0]);
					});
					return compile.compileAndOpenOfficeFloor();
				})) {

			// Ensure have Spring application context
			assertNotNull(capturedApplicationContext.value, "Should have Spring application context");

			// Should be the same context for forced start
			assertSame(capturedApplicationContext.value, forcedApplicationContext.value,
					"Should be same context for forced start");
		}
	}

	/**
	 * Ensure can configure Spring profile.
	 */
	@Test
	public void configureSpringProfile() {
		this.doSpringProfileTest("CONFIGURE_OVERRIDE", (context) -> {
			context.getDeployedOffice().addOverrideProperty("SPRING.profiles", "configure");
		});
	}

	/**
	 * Ensure can tie Spring profile to {@link Office} profile.
	 */
	@Test
	public void springProfileLinkedToOfficeProfile() {
		this.doSpringProfileTest("OFFICE_LINK_OVERRIDE", (context) -> {
			context.getDeployedOffice().addAdditionalProfile("office");
		});
	}

	/**
	 * Ensure can unlink Spring profile from {@link Office} profile.
	 */
	@Test
	public void unlinkSpringProfileFromOfficeProfile() {
		this.doSpringProfileTest("NO_PROFILE", (context) -> {
			DeployedOffice office = context.getDeployedOffice();
			office.addAdditionalProfile("office");
			office.addOverrideProperty("SPRING." + SpringSupplierSource.PROPERTY_UNLINK_CONTEXT_PROFILES,
					String.valueOf(true));
		});
	}

	private void doSpringProfileTest(String expectedValue, CompileOfficeFloorExtension extension) {

		// Ensure no profile configured
		ProfileBean noProfile = this.context.getBean(ProfileBean.class);
		assertEquals("NO_PROFILE", noProfile.getValue(), "Should provide default value");

		// Run with Spring profile
		SpringProfileSection.value = null;
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor(extension);
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			office.addSupplier("SPRING", SpringSupplierSource.class.getName()).addProperty(
					SpringSupplierSource.CONFIGURATION_CLASS_NAME, MockSpringBootConfiguration.class.getName());
			context.addSection("SECTION", SpringProfileSection.class);
			this.loadOfficeFloorDependencies(office);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", null);
		} catch (Throwable ex) {
			fail(ex);
		}
		assertEquals(expectedValue, SpringProfileSection.value, "Should have profile value");
	}

	public static class SpringProfileSection {

		private static String value;

		public void service(ProfileBean profile) {
			value = profile.getValue();
		}
	}

	/**
	 * Ensure can integrate with {@link WoOF}.
	 */
	@Test
	public void woofIntegration() throws Exception {
		try (MockWoofServer server = MockWoofServer.open((context, compiler) -> {
			context.addProfile("configure");
			context.extend((extension) -> {
				OfficeArchitect office = extension.getOfficeArchitect();

				// Configure servicing
				OfficeSection section = office.addOfficeSection("Service", ClassSectionSource.class.getName(),
						WoofIntegrationSection.class.getName());
				HttpInput input = extension.getWebArchitect().getHttpInput(false, "/");
				office.link(input.getInput(), section.getOfficeSectionInput("service"));

				// Spring integration
				office.addSupplier("Spring", SpringSupplierSource.class.getName()).addProperty("configuration.class",
						MockSpringBootConfiguration.class.getName());
				this.loadOfficeFloorDependencies(office);
			});
		})) {
			MockHttpResponse response = server.send(MockHttpServer.mockRequest());
			response.assertResponse(200, "CONFIGURE_OVERRIDE");
		}
	}

	public static class WoofIntegrationSection {
		public void service(ProfileBean profile, ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write(profile.getValue());
		}
	}

}