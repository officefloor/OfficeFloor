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

import static org.junit.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.compile.impl.structure.SuppliedManagedObjectSourceNodeImpl;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.supplier.SupplierLoaderUtil;
import net.officefloor.compile.test.supplier.SupplierTypeBuilder;
import net.officefloor.compile.test.systemproperties.SystemPropertiesRule;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.spring.extension.SpringSupplierExtension;

/**
 * Ensure can integrate Spring via boot.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringBootTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurableApplicationContext}.
	 */
	private ConfigurableApplicationContext context;

	/**
	 * Mock {@link OfficeFloorManagedObject}.
	 */
	private OfficeFloorManagedObject officeFloorManagedObject;

	@Override
	protected void setUp() throws Exception {
		this.officeFloorManagedObject = this.createMock(OfficeFloorManagedObject.class);
		this.context = SpringSupplierSource.runInContext(() -> {
			return SpringApplication.run(MockSpringBootConfiguration.class);
		}, (qualifier, objectType) -> {
			assertEquals("Incorrect object type", OfficeFloorManagedObject.class, objectType);
			return this.officeFloorManagedObject;
		});
	}

	@Override
	protected void tearDown() throws Exception {
		this.context.close();
	}

	/**
	 * Ensure can configure Spring bean.
	 */
	public void testSpringConfiguredBeans() {

		// Ensure can obtain simple bean
		SimpleBean simple = this.context.getBean(SimpleBean.class);
		assertNotNull("Should obtain simple bean", simple);
		assertEquals("Incorrect simple bean", "SIMPLE", simple.getValue());

		// Ensure can obtain complex bean
		ComplexBean complex = this.context.getBean(ComplexBean.class);
		assertNotNull("Should obtain complex bean", complex);
		assertSame("Should have simple bean injected", simple, complex.getSimpleBean());

		// Ensure can obtain qualified beans
		Consumer<String> assertQualifiedBean = (qualifier) -> {
			QualifiedBean bean = this.context.getBean("qualified" + qualifier, QualifiedBean.class);
			assertNotNull("Should obtain qualified bean", bean);
			assertEquals("Incorrect qualfiied value", qualifier, bean.getValue());
		};
		assertQualifiedBean.accept("One");
		assertQualifiedBean.accept("Two");
		assertQualifiedBean.accept("Three");
		assertQualifiedBean.accept("Four");

		// Ensure OfficeFloor managed object not scanned in by Spring
		OfficeFloorManagedObject mo = this.context.getBean(OfficeFloorManagedObject.class);
		assertSame("Should pull in the Spring dependency", this.officeFloorManagedObject, mo);
	}

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		SupplierLoaderUtil.validateSpecification(SpringSupplierSource.class, "configuration.class",
				"Configuration Class");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() throws Throwable {

		// Indicate the registered beans
		System.out.println("Beans:");
		for (String name : this.context.getBeanDefinitionNames()) {
			System.out.println("  " + name);
		}

		// Load all the beans as managed object sources
		Map<String, List<String>> beanNamesByType = new HashMap<>();
		for (String name : this.context.getBeanDefinitionNames()) {
			String beanClassName = this.context.getBean(name).getClass().getName();
			List<String> beanNames = beanNamesByType.get(beanClassName);
			if (beanNames == null) {
				beanNames = new LinkedList<>();
				beanNamesByType.put(beanClassName, beanNames);
			}
			beanNames.add(name);
		}

		// Obtain the class loader
		ClassLoader loader = this.getClass().getClassLoader();

		// Create the expected type
		SupplierTypeBuilder type = SupplierLoaderUtil.createSupplierTypeBuilder();

		// Load the expected supplier thread local
		type.addSupplierThreadLocal(null, OfficeFloorManagedObject.class);

		// Load the expected supplied managed object types
		NEXT_BEAN: for (String beanClassName : beanNamesByType.keySet()) {
			Class<?> beanClass = loader.loadClass(beanClassName);

			// Ignore the OfficeFloor managed objects
			if (OfficeFloorManagedObject.class.isAssignableFrom(beanClass)) {
				continue NEXT_BEAN;
			}

			// Load the Spring beans
			List<String> beanNames = beanNamesByType.get(beanClassName);
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
		assertTrue("Should obtain simple bean", simpleBean instanceof SimpleBean);

		// Ensure complex bean available
		SuppliedManagedObjectSourceType complexType = beans.get(ComplexBean.class.getName());
		Object complexBean = new ManagedObjectUserStandAlone()
				.sourceManagedObject(complexType.getManagedObjectSource(), false).getObject();
		assertTrue("Should obtain complex bean", complexBean instanceof ComplexBean);
		assertSame("Should follow Spring singletons", simpleBean, ((ComplexBean) complexBean).getSimpleBean());

		// Ensure qualified beans available
		BiConsumer<String, Class<?>> verifyQualifiedBean = (qualifier, beanType) -> {
			try {
				String qualifierName = (QualifiedBean.class.equals(beanType)) ? "qualified" + qualifier : null;
				String qualifiedName = SuppliedManagedObjectSourceNodeImpl
						.getSuppliedManagedObjectSourceName(qualifierName, beanType.getName());
				SuppliedManagedObjectSourceType qualifiedType = beans.get(qualifiedName);
				assertNotNull("Should have qualified type: " + qualifiedName, qualifiedType);
				Object qualifiedBean = new ManagedObjectUserStandAlone()
						.sourceManagedObject(qualifiedType.getManagedObjectSource(), false).getObject();
				assertTrue("Should obtain qualified bean: " + qualifier, qualifiedBean instanceof QualifiedBean);
				assertEquals("Incorrect qualified bean", qualifier, ((QualifiedBean) qualifiedBean).getValue());
			} catch (Throwable ex) {
				throw fail(ex);
			}
		};
		verifyQualifiedBean.accept("One", QualifiedBean.class);
		verifyQualifiedBean.accept("Two", QualifiedBean.class);
		verifyQualifiedBean.accept("Three", ComponentQualifiedBeanThree.class);
		verifyQualifiedBean.accept("Four", ComponentQualifiedBeanFour.class);

		// Should not have OfficeFloor managed object bean
		assertNull("Should not have OfficeFloor supplied objects", beans.get(OfficeFloorManagedObject.class.getName()));
	}

	/**
	 * Ensure can integrate in Spring beans to {@link OfficeFloor}.
	 */
	public void testIntegrateSimpleSpringBean() throws Throwable {
		this.doIntegrateSimpleSpringBeanTest(SpringSupplierSource.class.getName());
	}

	/**
	 * Ensure {@link SpringSupplierSourceService} registered providing alias.
	 */
	public void testSpringSupplierSourceAlias() throws Throwable {
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

			// Add the OfficeFloor managed object
			Singleton.load(office, this.createMock(OfficeFloorManagedObject.class));
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Ensure pulls in simple bean via Spring
			IntegrateSimpleSpringBean.simpleBean = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.functionSimple", null);
			assertNotNull("Should pull in simple bean from Spring", IntegrateSimpleSpringBean.simpleBean);
			assertEquals("Incorrect simple bean", "SIMPLE", IntegrateSimpleSpringBean.simpleBean.getValue());

			// Ensure pulls in complex bean via Spring
			IntegrateSimpleSpringBean.complexBean = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.functionComplex", null);
			assertNotNull("Should pull in complex bean from Spring", IntegrateSimpleSpringBean.complexBean);
			assertSame("Incorrect complex bean", IntegrateSimpleSpringBean.simpleBean,
					IntegrateSimpleSpringBean.complexBean.getSimpleBean());
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
	 * 
	 * @throws Throwable
	 */
	public void testIntegrateQualfiedSpringBean() throws Throwable {

		// Configure OfficeFloor to auto-wire in Spring beans
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Add Spring supplier
			SpringSupplierSource.configure(office, MockSpringBootConfiguration.class);

			// Add the section
			context.addSection("SECTION", IntegrateQualifiedSpringBean.class);

			// Add the OfficeFloor managed object
			Singleton.load(office, this.createMock(OfficeFloorManagedObject.class));
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Ensure pulls in bean via Spring
			Consumer<String> verifyQualification = (qualifier) -> {
				try {
					IntegrateQualifiedSpringBean.qualifiedBean = null;
					CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.function" + qualifier, null);
					assertNotNull("Should pull in bean from Spring", IntegrateQualifiedSpringBean.qualifiedBean);
					assertEquals("Incorrect qualified bean", qualifier,
							IntegrateQualifiedSpringBean.qualifiedBean.getValue());
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
	public void testIntegrateSpringBeanDependencyToManagedObject() throws Throwable {

		// Create the managed object
		OfficeFloorManagedObject managedObject = this.createMock(OfficeFloorManagedObject.class);

		// Record obtaining value
		this.recordReturn(managedObject, managedObject.getValue(), "OfficeFloor");
		this.replayMockObjects();

		// Configure OfficeFloor to auto-wire in Spring beans
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Add Spring supplier
			office.addSupplier("SPRING", SpringSupplierSource.class.getName()).addProperty(
					SpringSupplierSource.CONFIGURATION_CLASS_NAME, MockSpringBootConfiguration.class.getName());

			// Add the OfficeFloor managed object
			Singleton.load(office, managedObject);

			// Add the section
			context.addSection("SECTION", IntegrateOfficeFloorManagedObject.class);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Ensure can source the Spring dependency from OfficeFloor
			IntegrateOfficeFloorManagedObject.dependentBean = null;
			IntegrateOfficeFloorManagedObject.officeFloorManagedObject = null;
			IntegrateOfficeFloorManagedObject.value = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.function", null);
			assertNotNull("Should pull in dependent bean from Spring", IntegrateOfficeFloorManagedObject.dependentBean);
			assertNotNull("Should have managed object dependency available",
					IntegrateOfficeFloorManagedObject.officeFloorManagedObject);
			assertNotSame("Should proxy dependency from OfficeFloor", managedObject,
					IntegrateOfficeFloorManagedObject.officeFloorManagedObject);
			assertEquals("Should access dependency from OfficeFloor", "OfficeFloor",
					IntegrateOfficeFloorManagedObject.value);
		}

		// Ensure OfficeFloor object used
		this.verifyMockObjects();
	}

	public static class IntegrateOfficeFloorManagedObject {

		private static DependentBean dependentBean;

		private static OfficeFloorManagedObject officeFloorManagedObject;

		private static String value;

		public void function(DependentBean bean) {
			dependentBean = bean;
			officeFloorManagedObject = bean.getManagedObject();
			value = officeFloorManagedObject.getValue();
		}
	}

	/**
	 * Ensure can load {@link SpringSupplierExtension}.
	 */
	public void testSpringExtension() throws Throwable {

		// Create the managed object
		OfficeFloorManagedObject managedObject = this.createMock(OfficeFloorManagedObject.class);

		// Ensure beans registered
		final String SIMPLE_BEAN_NAME = "simpleBean";
		final String MANAGED_OBJECT_NAME = "officeFloorManagedObject";
		assertNotNull("Invalid test as no simple bean", this.context.getBean(SIMPLE_BEAN_NAME));
		assertNotNull("Invalid test as no managed object", this.context.getBean(MANAGED_OBJECT_NAME));

		// Record obtaining value
		this.recordReturn(managedObject, managedObject.getValue(), "EXTENSION");
		this.replayMockObjects();

		// Ensure active spring extension
		MockSpringSupplierExtension.isActive = true;
		MockSpringSupplierExtension.officeFloorManagedObject = null;
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
				Singleton.load(office, managedObject);

				// Add the section
				context.addSection("SECTION", ExtensionSection.class);
			});
			try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

				// Invoke the function
				ExtensionSection.serviceThread = null;
				ExtensionSection.threadLocalValue = null;
				LoadBean.loadCount.set(0);
				CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", null);

				// Ensure correct value passed
				assertEquals("Incorrect value", "SIMPLE-EXTENSION", ExtensionSection.threadLocalValue);

				// Ensure have decorated beans
				assertTrue("Should register decorated beans",
						MockSpringSupplierExtension.decoratedBeanTypes.size() > 0);
				assertTrue("Incorrect simple bean", SimpleBean.class
						.isAssignableFrom(MockSpringSupplierExtension.decoratedBeanTypes.get(SIMPLE_BEAN_NAME)));
				assertNull("Should not decorate officeFloorManagedObject",
						MockSpringSupplierExtension.decoratedBeanTypes.get(MANAGED_OBJECT_NAME));

				// Ensure after having invoked process, that added dependency loaded
				assertEquals("Should load additional dependency", 1, LoadBean.loadCount.get());
			}

			// Verify
			this.verifyMockObjects();

		} finally {
			MockSpringSupplierExtension.isActive = false;
		}
	}

	public static class ExtensionSection {

		private static volatile Thread serviceThread = null;

		private static volatile String threadLocalValue = null;

		@Next("next")
		public void service(SimpleBean bean) {
			serviceThread = Thread.currentThread();
			String value = bean.getValue() + "-" + MockSpringSupplierExtension.officeFloorManagedObject.getValue();
			MockSpringSupplierExtension.threadLocal.set(value);
		}

		public void next(ComplexBean bean) {
			assertNotNull("Should have service thread", serviceThread);
			assertNotEquals("Should be different thread", serviceThread, Thread.currentThread());
			threadLocalValue = MockSpringSupplierExtension.threadLocal.get();
		}
	}

	/**
	 * Ensure able to capture the {@link ConfigurableApplicationContext}.
	 */
	public void testCaptureApplicationContext() throws Exception {

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
				Singleton.load(office, this.createMock(OfficeFloorManagedObject.class));
			});
			return compile.compileAndOpenOfficeFloor();
		})) {

			// Ensure have Spring application context
			assertNotNull("Should have Spring application context", applicationContext.value);

			// Ensure able to use application context
			SimpleBean bean = applicationContext.value.getBean(SimpleBean.class);
			assertNotNull("Should have bean", bean);

		}
	}

	/**
	 * Ensure can configure Spring profile.
	 */
	public void testSpringProfile() throws Throwable {

		// Ensure no profile configured
		ProfileBean noProfile = this.context.getBean(ProfileBean.class);
		assertEquals("Should provide default value", "NO_PROFILE", noProfile.getValue());

		// Run with Spring profile configured
		SpringProfileSection.value = null;
		new SystemPropertiesRule("OFFICE.SPRING.profiles", "override").run(() -> {

			// Run with System properties (should activate profile)
			CompileOfficeFloor compile = new CompileOfficeFloor();
			compile.office((context) -> {
				OfficeArchitect office = context.getOfficeArchitect();
				office.addSupplier("SPRING", SpringSupplierSource.class.getName()).addProperty(
						SpringSupplierSource.CONFIGURATION_CLASS_NAME, MockSpringBootConfiguration.class.getName());
				context.addSection("SECTION", SpringProfileSection.class);
				Singleton.load(office, this.createMock(OfficeFloorManagedObject.class));
			});
			try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
				CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", null);
			}
		});
		assertEquals("Should have profile value", "PROFILE_OVERRIDE", SpringProfileSection.value);
	}

	public static class SpringProfileSection {

		private static String value;

		public void service(ProfileBean profile) {
			value = profile.getValue();
		}
	}

}