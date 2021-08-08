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

package net.officefloor.compile.test.officefloor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.officefloor.OfficeFloorType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableManagedObjectSource;
import net.officefloor.plugin.variable.VariableOfficeExtensionService;

/**
 * Provides easier compiling of {@link OfficeFloor} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOfficeFloor extends AbstractOfficeFloorSource {

	/**
	 * Convenience method to invoke the {@link ProcessState} for the
	 * {@link ManagedFunction} within the default {@link Office}.
	 * 
	 * @param officeFloor  {@link OfficeFloor}.
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @param parameter    Parameter to the {@link ManagedFunction}.
	 * @throws Throwable If fails to invoke the {@link ProcessState}.
	 */
	public static void invokeProcess(OfficeFloor officeFloor, String functionName, Object parameter) throws Throwable {
		invokeProcess(officeFloor, functionName, parameter, 3000);
	}

	/**
	 * Convenience method to invoke the {@link ProcessState} for the
	 * {@link ManagedFunction} within the default {@link Office}.
	 * 
	 * @param officeFloor  {@link OfficeFloor}.
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @param parameter    Parameter to the {@link ManagedFunction}.
	 * @param waitTime     Time in milliseconds to wait for {@link ProcessState} to
	 *                     complete.
	 * @throws Throwable If fails to invoke the {@link ProcessState}.
	 */
	public static void invokeProcess(OfficeFloor officeFloor, String functionName, Object parameter, long waitTime)
			throws Throwable {
		invokeProcess(officeFloor, "OFFICE", functionName, parameter, waitTime);
	}

	/**
	 * Convenience method to invoke the {@link ProcessState} for the
	 * {@link ManagedFunction}.
	 * 
	 * @param officeFloor  {@link OfficeFloor}.
	 * @param officeName   Name of the {@link Office} containing the
	 *                     {@link ManagedFunction}.
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @param parameter    Parameter to the {@link ManagedFunction}.
	 * @param waitTime     Time in milliseconds to wait for {@link ProcessState} to
	 *                     complete.
	 * @throws Throwable If fails to invoke the {@link ProcessState}.
	 */
	public static void invokeProcess(OfficeFloor officeFloor, String officeName, String functionName, Object parameter,
			long waitTime) throws Throwable {

		// Obtain the function
		FunctionManager function = officeFloor.getOffice(officeName).getFunctionManager(functionName);

		// Invoke the function (ensuring completes within reasonable time)
		long startTimestamp = System.currentTimeMillis();
		boolean[] isComplete = new boolean[] { false };
		Throwable[] failure = new Throwable[] { null };
		function.invokeProcess(parameter, (exception) -> {
			synchronized (isComplete) {
				failure[0] = exception;
				isComplete[0] = true;
				isComplete.notifyAll(); // wake up immediately
			}
		});
		synchronized (isComplete) {
			while (!isComplete[0]) {

				// Determine if timed out
				long currentTimestamp = System.currentTimeMillis();
				if ((startTimestamp + waitTime) < currentTimestamp) {
					throw new Exception("Timed out waiting on process (" + officeName + "." + functionName
							+ ") to complete (" + (currentTimestamp - startTimestamp) + " milliseconds)");
				}

				// Sleep some time
				isComplete.wait(100);
			}

			// Determine if failure
			if (failure[0] != null) {
				throw failure[0];
			}
		}
	}

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * {@link Var} decorators.
	 */
	private final Map<String, Consumer<Var<?>>> variableDecorators = new HashMap<>();

	/**
	 * {@link CompileOfficeFloorExtension} instances.
	 */
	private final List<CompileOfficeFloorExtension> officeFloorExtensions = new LinkedList<>();

	/**
	 * {@link CompileOfficeExtension} instances.
	 */
	private final List<CompileOfficeExtension> officeExtensions = new LinkedList<>();

	/**
	 * {@link CompileSectionExtension} instances.
	 */
	private final List<CompileSectionExtension> sectionExtensions = new LinkedList<>();

	/**
	 * Instantiate.
	 */
	public CompileOfficeFloor() {
		this.compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		this.compiler.setCompilerIssues(new FailTestCompilerIssues());
	}

	/**
	 * <p>
	 * Obtains the {@link OfficeFloorCompiler}.
	 * <p>
	 * Note the {@link OfficeFloorSource} is overridden on compiling to ensure the
	 * extensions are available.
	 * 
	 * @return {@link OfficeFloorCompiler}.
	 */
	public OfficeFloorCompiler getOfficeFloorCompiler() {
		return this.compiler;
	}

	/**
	 * Adds a {@link CompileOfficeFloorExtension}.
	 * 
	 * @param extension {@link CompileOfficeFloorExtension}.
	 */
	public void officeFloor(CompileOfficeFloorExtension extension) {
		this.officeFloorExtensions.add(extension);
	}

	/**
	 * Adds a {@link CompileOfficeExtension}.
	 * 
	 * @param extension {@link CompileOfficeExtension}.
	 */
	public void office(CompileOfficeExtension extension) {
		this.officeExtensions.add(extension);
	}

	/**
	 * Adds a {@link CompileSectionExtension}.
	 * 
	 * @param extension {@link CompileSectionExtension}.
	 */
	public void section(CompileSectionExtension extension) {
		this.sectionExtensions.add(extension);
	}

	/**
	 * Compiles the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails to compile the {@link OfficeFloor}.
	 */
	public OfficeFloor compileOfficeFloor() throws Exception {
		return VariableOfficeExtensionService.runInContext(this.variableDecorators, () -> {

			// Ensure use this to source OfficeFloor with extensions
			this.compiler.setOfficeFloorSource(this);

			// Compile and return the OfficeFloor
			return this.compiler.compile("OfficeFloor");
		});
	}

	/**
	 * Compiles and opens the {@link Office}.
	 * 
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails to compile and open the {@link OfficeFloor}.
	 */
	public OfficeFloor compileAndOpenOfficeFloor() throws Exception {

		// Compile the OfficeFloor
		OfficeFloor officeFloor = this.compileOfficeFloor();
		if (officeFloor == null) {
			return null; // failed to compile
		}

		// Open the OfficeFloor
		officeFloor.openOfficeFloor();

		// Return the OfficeFloor
		return officeFloor;
	}

	/**
	 * Loads the {@link OfficeFloorType}.
	 * 
	 * @return {@link OfficeFloorType}.
	 */
	public OfficeFloorType loadOfficeFloorType() {

		// Obtain the loader
		OfficeFloorLoader loader = this.getOfficeFloorCompiler().getOfficeFloorLoader();

		// Load the OfficeFloor type
		PropertyList properties = this.compiler.createPropertyList();
		OfficeFloorType officeFloorType = loader.loadOfficeFloorType(this, null, properties);

		// Return the OfficeFloor type
		return officeFloorType;
	}

	/*
	 * ======================= OfficeFloorSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public void specifyConfigurationProperties(RequiredProperties requiredProperties, OfficeFloorSourceContext context)
			throws Exception {
	}

	@Override
	public void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {

		// Add the default office (if office configuration)
		DeployedOffice office = null;
		if (this.officeExtensions.size() > 0) {
			office = deployer.addDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME, new CompileOfficeSource(),
					null);
		}
		final DeployedOffice deployedOffice = office;

		// Run the extensions
		for (CompileOfficeFloorExtension extension : this.officeFloorExtensions) {
			extension.extend(new CompileOfficeFloorContext() {

				@Override
				public OfficeFloorSourceContext getOfficeFloorSourceContext() {
					return context;
				}

				@Override
				public OfficeFloorDeployer getOfficeFloorDeployer() {
					return deployer;
				}

				@Override
				public DeployedOffice getDeployedOffice() {
					return deployedOffice;
				}

				@Override
				public OfficeFloorManagedObject addManagedObject(String managedObjectName, Class<?> managedObjectClass,
						ManagedObjectScope scope) {
					OfficeFloorManagedObjectSource mos = deployer.addManagedObjectSource(managedObjectName + "_SOURCE",
							ClassManagedObjectSource.class.getName());
					mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, managedObjectClass.getName());
					return mos.addOfficeFloorManagedObject(managedObjectName, scope);
				}
			});
		}
	}

	/**
	 * {@link OfficeSource} that allows {@link CompileOfficeExtension} instances to
	 * configure it.
	 */
	@PrivateSource
	private class CompileOfficeSource extends AbstractOfficeSource {

		/**
		 * {@link OfficeSection}.
		 */
		private OfficeSection section = null;

		/**
		 * Obtains the {@link OfficeSection}.
		 * 
		 * @param architect {@link OfficeArchitect}.
		 * @return {@link OfficeSection}.
		 */
		private OfficeSection getSection(OfficeArchitect architect) {
			if (this.section == null) {
				if (CompileOfficeFloor.this.sectionExtensions.size() > 0) {
					this.section = architect.addOfficeSection("SECTION", new CompileSectionSource(), null);
				}
			}
			return this.section;
		}

		/*
		 * ====================== OfficeSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {

			// Auto-wire
			officeArchitect.enableAutoWireObjects();

			// Load the extensions
			for (CompileOfficeExtension extension : CompileOfficeFloor.this.officeExtensions) {
				extension.extend(new CompileOfficeContext() {

					@Override
					public OfficeSection getOfficeSection() {
						return CompileOfficeSource.this.getSection(officeArchitect);
					}

					@Override
					public OfficeSection overrideSection(Class<? extends SectionSource> sectionSourceClass,
							String sectionLocation) {
						if (CompileOfficeSource.this.section != null) {
							throw new IllegalStateException("OfficeSection already specified");
						}
						CompileOfficeSource.this.section = officeArchitect.addOfficeSection("SECTION",
								sectionSourceClass.getName(), sectionLocation);
						return CompileOfficeSource.this.section;
					}

					@Override
					public OfficeSourceContext getOfficeSourceContext() {
						return context;
					}

					@Override
					public OfficeArchitect getOfficeArchitect() {
						return officeArchitect;
					}

					@Override
					public OfficeManagedObject addManagedObject(String managedObjectName, Class<?> managedObjectClass,
							ManagedObjectScope scope) {
						OfficeManagedObjectSource mos = officeArchitect.addOfficeManagedObjectSource(
								managedObjectName + "_SOURCE", ClassManagedObjectSource.class.getName());
						mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
								managedObjectClass.getName());
						return mos.addOfficeManagedObject(managedObjectName, scope);
					}

					@Override
					public OfficeSection addSection(String sectionName, Class<?> sectionClass) {
						return officeArchitect.addOfficeSection(sectionName, ClassSectionSource.class.getName(),
								sectionClass.getName());
					}

					@Override
					@SuppressWarnings({ "rawtypes", "unchecked" })
					public <T> void variable(String qualifier, Class<T> type, Consumer<Var<T>> compileVar) {

						// Obtain the variable name
						String variableName = VariableManagedObjectSource.name(qualifier, type.getName());

						// Capture the variable
						CompileOfficeFloor.this.variableDecorators.put(variableName, (Consumer) compileVar);
					}
				});
			}

			// Ensure have section
			this.getSection(officeArchitect);
		}
	}

	/**
	 * {@link SectionSource} that allows {@link CompileSectionExtension} instances
	 * to configure it.
	 */
	@PrivateSource
	private class CompileSectionSource extends AbstractSectionSource {

		/*
		 * ====================== SectionSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

			// Load the extensions
			for (CompileSectionExtension extension : CompileOfficeFloor.this.sectionExtensions) {
				extension.extend(new CompileSectionContext() {

					@Override
					public SectionSourceContext getSectionSourceContext() {
						return context;
					}

					@Override
					public SectionDesigner getSectionDesigner() {
						return designer;
					}
				});
			}
		}
	}

}
