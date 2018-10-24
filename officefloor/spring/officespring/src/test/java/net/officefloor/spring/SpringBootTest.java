/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.spring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.compile.impl.structure.SuppliedManagedObjectSourceNodeImpl;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.supplier.SupplierLoaderUtil;
import net.officefloor.compile.test.supplier.SupplierTypeBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.managedfunction.clazz.Qualified;

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

	@Override
	protected void setUp() throws Exception {
		this.context = SpringApplication.run(MockSpringBootConfiguration.class);

		// Indicate the registered beans
		System.out.println("Beans:");
		for (String name : this.context.getBeanDefinitionNames()) {
			System.out.println("  " + name);
		}
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

		// Load all the beans as managed object sources
		Map<String, List<String>> beanNamesByType = new HashMap<>();
		ConfigurableListableBeanFactory beanFactory = this.context.getBeanFactory();
		for (String name : this.context.getBeanDefinitionNames()) {
			BeanDefinition definition = beanFactory.getBeanDefinition(name);
			String beanClassName = definition.getBeanClassName();

			// Determine if factory method
			if (beanClassName == null) {
				if (definition instanceof AnnotatedBeanDefinition) {
					AnnotatedBeanDefinition annotatedDefinition = (AnnotatedBeanDefinition) definition;
					if (annotatedDefinition.getFactoryMethodMetadata() != null) {
						beanClassName = annotatedDefinition.getFactoryMethodMetadata().getReturnTypeName();
					}
				}
			}

			// Add only if have bean class name
			if (beanClassName != null) {
				List<String> beanNames = beanNamesByType.get(beanClassName);
				if (beanNames == null) {
					beanNames = new LinkedList<>();
					beanNamesByType.put(beanClassName, beanNames);
				}
				beanNames.add(name);
			}
		}

		// Obtain the class loader
		ClassLoader loader = this.getClass().getClassLoader();

		// Create the expected type
		SupplierTypeBuilder type = SupplierLoaderUtil.createSupplierTypeBuilder();
		for (String beanClassName : beanNamesByType.keySet()) {
			List<String> beanNames = beanNamesByType.get(beanClassName);
			Class<?> beanClass = loader.loadClass(beanClassName);
			switch (beanNames.size()) {
			case 1:
				// No qualification as only type
				type.addSuppliedManagedObjectSource(null, beanClass,
						new SpringBeanManagedObjectSource(null, beanClass, this.context));
				break;
			default:
				// Load with qualifications
				for (String beanName : beanNames) {
					type.addSuppliedManagedObjectSource(beanName, beanClass,
							new SpringBeanManagedObjectSource(beanName, beanClass, this.context));
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
		Consumer<String> verifyQualifiedBean = (qualifier) -> {
			try {
				String qualifiedName = SuppliedManagedObjectSourceNodeImpl
						.getSuppliedManagedObjectSourceName("qualified" + qualifier, QualifiedBean.class.getName());
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
		verifyQualifiedBean.accept("One");
		verifyQualifiedBean.accept("Two");
	}

	/**
	 * Ensure can integrate in Spring beans to {@link OfficeFloor}.
	 */
	public void testIntegrateSimpleSpringBean() throws Throwable {

		// Configure OfficeFloor to auto-wire in Spring beans
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Add Spring supplier
			office.addSupplier("SPRING", SpringSupplierSource.class.getName()).addProperty(
					SpringSupplierSource.CONFIGURATION_CLASS_NAME, MockSpringBootConfiguration.class.getName());

			// Add the section
			context.addSection("SECTION", IntegrateSimpleSpringBean.class);
		});
		OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor();

		// Ensure pulls in bean via Spring
		IntegrateSimpleSpringBean.simpleBean = null;
		CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.function", null);
		assertNotNull("Should pull in bean from Spring", IntegrateSimpleSpringBean.simpleBean);
		assertEquals("Incorrect simple bean", "SIMPLE", IntegrateSimpleSpringBean.simpleBean.getValue());

		// Clean up
		officeFloor.closeOfficeFloor();
	}

	public static class IntegrateSimpleSpringBean {

		private static SimpleBean simpleBean;

		public void function(SimpleBean bean) {
			simpleBean = bean;
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
			office.addSupplier("SPRING", SpringSupplierSource.class.getName()).addProperty(
					SpringSupplierSource.CONFIGURATION_CLASS_NAME, MockSpringBootConfiguration.class.getName());

			// Add the section
			context.addSection("SECTION", IntegrateQualifiedSpringBean.class);
		});
		OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor();

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

		// Clean up
		officeFloor.closeOfficeFloor();
	}

	public static class IntegrateQualifiedSpringBean {

		private static QualifiedBean qualifiedBean;

		public void functionOne(@Qualified("qualifiedOne") QualifiedBean bean) {
			qualifiedBean = bean;
		}

		public void functionTwo(@Qualified("qualifiedTwo") QualifiedBean bean) {
			qualifiedBean = bean;
		}
	}

}