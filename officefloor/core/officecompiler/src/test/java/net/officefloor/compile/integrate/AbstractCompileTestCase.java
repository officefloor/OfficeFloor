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

package net.officefloor.compile.integrate;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import net.officefloor.compile.AbstractModelCompilerTestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.integrate.CompileTestSupport.OfficeFloorValidator;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.ExecutiveBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectPoolBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.build.ThreadDependencyMappingBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Provides abstract functionality for testing compiling the
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractCompileTestCase extends AbstractModelCompilerTestCase {

	/**
	 * {@link CompileTestSupport}.
	 */
	public final CompileTestSupport compileTestSupport = new CompileTestSupport(this.mockTestSupport,
			this.modelTestSupport, this.fileTestSupport) {

		@Override
		protected CompilerIssues enhanceIssues(CompilerIssues issues) {
			return AbstractCompileTestCase.this.enhanceIssues(issues);
		}
	};

	/**
	 * {@link CompilerIssues}.
	 */
	protected final MockCompilerIssues issues;

	/**
	 * {@link OfficeFloorBuilder}.
	 */
	protected final OfficeFloorBuilder officeFloorBuilder;

	/**
	 * Initialise.
	 */
	public AbstractCompileTestCase() {

		// Set up the issues
		this.issues = this.compileTestSupport.getIssues();

		// Create the builder
		this.officeFloorBuilder = this.compileTestSupport.getOfficeFloorBuilder();
	}

	/**
	 * <p>
	 * Allow enhancing the {@link CompilerIssues}. For example allows wrapping with
	 * a {@link StderrCompilerIssuesWrapper}.
	 * <p>
	 * This is available for {@link TestCase} instances to override.
	 * 
	 * @param issues {@link CompilerIssues}.
	 * @return By default returns input {@link CompilerIssues}.
	 */
	protected CompilerIssues enhanceIssues(CompilerIssues issues) {
		return issues;
	}

	/**
	 * Enables the {@link Property} overrides for compiling.
	 */
	protected void enableOverrideProperties() {
		this.compileTestSupport.enableOverrideProperties();
	}

	/**
	 * Adds the {@link OfficeFloorValidator}.
	 * 
	 * @param validator {@link OfficeFloorValidator}.
	 */
	protected void addValidator(OfficeFloorValidator validator) {
		this.compileTestSupport.addValidator(validator);
	}

	/**
	 * Records initialising the {@link SupplierSource} for terminating.
	 */
	protected void record_supplierSetup() {
		this.compileTestSupport.record_supplierSetup();
	}

	/**
	 * Records initialising the {@link OfficeFloorBuilder}.
	 * 
	 * @param resourceSources {@link ResourceSource} instances.
	 */
	protected void record_init(ResourceSource... resourceSources) {
		this.compileTestSupport.record_init(resourceSources);
	}

	/**
	 * Records specifying the {@link Executive} on the {@link OfficeFloorBuilder}.
	 * 
	 * @param executiveSource    {@link ExecutiveSource}.
	 * @param propertyNameValues {@link Property} name/value listing.
	 * @return {@link ExecutiveBuilder} for the {@link Executive}.
	 */
	protected <S extends ExecutiveSource> ExecutiveBuilder<S> record_officeFloorBuilder_setExecutive(S executiveSource,
			String... propertyNameValues) {
		return this.compileTestSupport.record_officeFloorBuilder_setExecutive(executiveSource, propertyNameValues);
	}

	/**
	 * Records adding a {@link Team} to the {@link OfficeFloorBuilder}.
	 * 
	 * @param teamName           Name of the {@link Team}.
	 * @param teamSource         {@link TeamSource} class.
	 * @param propertyNameValues {@link Property} name/value listing.
	 * @return {@link TeamBuilder} for the added {@link Team}.
	 */
	protected <S extends TeamSource> TeamBuilder<S> record_officeFloorBuilder_addTeam(String teamName, S teamSource,
			String... propertyNameValues) {
		return this.compileTestSupport.record_officeFloorBuilder_addTeam(teamName, teamSource, propertyNameValues);
	}

	/**
	 * Records adding a {@link ManagedObjectSource} to the
	 * {@link OfficeFloorBuilder}.
	 * 
	 * @param managedObjectSourceName  Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClass {@link ManagedObjectSource} class.
	 * @param timeout                  Timeout of the {@link ManagedObject}.
	 * @param propertyNameValues       {@link Property} name/value listing.
	 * @param {@link                   ManagedObjectBuilder} for the added
	 *                                 {@link ManagedObjectSource}.
	 */
	protected <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> ManagedObjectBuilder<F> record_officeFloorBuilder_addManagedObject(
			String managedObjectSourceName, Class<S> managedObjectSourceClass, long timeout,
			String... propertyNameValues) {
		return this.compileTestSupport.record_officeFloorBuilder_addManagedObject(managedObjectSourceName,
				managedObjectSourceClass, timeout, propertyNameValues);
	}

	/**
	 * Records adding a {@link ManagedObjectSource} to the
	 * {@link OfficeFloorBuilder}.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSource     {@link ManagedObjectSource}.
	 * @param timeout                 Timeout of the {@link ManagedObject}.
	 * @param propertyNameValues      {@link Property} name/value listing.
	 * @param {@link                  ManagedObjectBuilder} for the added
	 *                                {@link ManagedObjectSource}.
	 */
	protected <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> ManagedObjectBuilder<F> record_officeFloorBuilder_addManagedObject(
			String managedObjectSourceName, S managedObjectSource, long timeout, String... propertyNameValues) {
		return this.compileTestSupport.record_officeFloorBuilder_addManagedObject(managedObjectSourceName,
				managedObjectSource, timeout, propertyNameValues);
	}

	/**
	 * Records specifying the {@link ManagingOffice}.
	 * 
	 * @param officeName Name of the {@link ManagingOffice}.
	 * @return {@link ManagingOfficeBuilder}.
	 */
	protected ManagingOfficeBuilder<?> record_managedObjectBuilder_setManagingOffice(String officeName) {
		return this.compileTestSupport.record_managedObjectBuilder_setManagingOffice(officeName);
	}

	/**
	 * Records specifying the {@link ManagedObjectPool} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectPoolId Identifier of the {@link ManagedObjectPool}.
	 * @return {@link ManagedObjectPoolBuilder}.
	 */
	protected ManagedObjectPoolBuilder record_managedObjectBuilder_setManagedObjectPool(String managedObjectPoolId) {
		return this.compileTestSupport.record_managedObjectBuilder_setManagedObjectPool(managedObjectPoolId);
	}

	/**
	 * Records specifying the Input {@link ManagedObject} name.
	 * 
	 * @param inputManagedObjectName Input {@link ManagedObject} name.
	 * @return {@link ThreadDependencyMappingBuilder} for the Input
	 *         {@link ManagedObject}.
	 */
	protected ThreadDependencyMappingBuilder record_managingOfficeBuilder_setInputManagedObjectName(
			String inputManagedObjectName) {
		return this.compileTestSupport.record_managingOfficeBuilder_setInputManagedObjectName(inputManagedObjectName);
	}

	/**
	 * Records specifying the {@link ManagedObjectFunctionDependency}.
	 * 
	 * @param functionObjectName     Name of the
	 *                               {@link ManagedObjectFunctionDependency}.
	 * @param scopeManagedObjectName Name of the {@link ManagedObject}.
	 */
	protected void record_managingOfficeBuilder_mapFunctionDependency(String functionObjectName,
			String scopeManagedObjectName) {
		this.compileTestSupport.record_managingOfficeBuilder_mapFunctionDependency(functionObjectName,
				scopeManagedObjectName);
	}

	/**
	 * Records adding a {@link OfficeBuilder}.
	 * 
	 * @param officeName Name of the {@link Office}.
	 * @return Added {@link OfficeBuilder}.
	 */
	protected OfficeBuilder record_officeFloorBuilder_addOffice(String officeName) {
		return this.compileTestSupport.record_officeFloorBuilder_addOffice(officeName);
	}

	/**
	 * Records adding a {@link ProcessState} {@link ManagedObject} to the
	 * {@link Office}.
	 * 
	 * @param processManagedObjectName {@link ThreadState} bound name.
	 * @param officeManagedObjectName  {@link Office} registered
	 *                                 {@link ManagedObject} name.
	 */
	protected ThreadDependencyMappingBuilder record_officeBuilder_addProcessManagedObject(
			String processManagedObjectName, String officeManagedObjectName) {
		return this.compileTestSupport.record_officeBuilder_addProcessManagedObject(processManagedObjectName,
				officeManagedObjectName);
	}

	/**
	 * Records adding a {@link ThreadState} {@link ManagedObject} to the
	 * {@link Office}.
	 * 
	 * @param threadManagedObjectName {@link ThreadState} bound name.
	 * @param officeManagedObjectName {@link Office} registered
	 *                                {@link ManagedObject} name.
	 */
	protected ThreadDependencyMappingBuilder record_officeBuilder_addThreadManagedObject(String threadManagedObjectName,
			String officeManagedObjectName) {
		return this.compileTestSupport.record_officeBuilder_addThreadManagedObject(threadManagedObjectName,
				officeManagedObjectName);
	}

	/**
	 * Records adding pre-load {@link Administration}.
	 * 
	 * @param administrationName Name of the {@link Administration}.
	 * @param extensionType      Extension type.
	 * @return {@link AdministrationBuilder} for the added {@link Administration}.
	 */
	protected <E, F extends Enum<F>, G extends Enum<G>, S extends AdministrationSource<E, F, G>> AdministrationBuilder<F, G> record_dependencyMappingBuilder_preLoadAdminister(
			String administrationName, Class<E> extensionType) {
		return this.compileTestSupport.record_dependencyMappingBuilder_preLoadAdminister(administrationName,
				extensionType);
	}

	/**
	 * Records adding a {@link GovernanceSource} to the {@link OfficeBuilder}.
	 * 
	 * @param governanceSourceName  Name of the {@link GovernanceSource}.
	 * @param governanceSourceClass {@link GovernanceSource} class.
	 * @param extensionType         Extension type.
	 * @return {@link GovernanceBuilder} for the added {@link GovernanceSource}.
	 */
	protected <E, F extends Enum<F>, S extends GovernanceSource<E, F>> GovernanceBuilder<F> record_officeBuilder_addGovernance(
			String governanceName, Class<S> governanceSourceClass, Class<?> extensionType) {
		return this.compileTestSupport.record_officeBuilder_addGovernance(governanceName, governanceSourceClass,
				extensionType);
	}

	/**
	 * Records adding a {@link GovernanceSource} to the {@link OfficeBuilder}.
	 * 
	 * @param governanceSourceName  Name of the {@link GovernanceSource}.
	 * @param teamName              Name of {@link Team} responsible for
	 *                              {@link Governance}.
	 * @param governanceSourceClass {@link GovernanceSource} class.
	 * @param extensionType         Extension type.
	 * @return {@link GovernanceBuilder} for the added {@link GovernanceSource}.
	 */
	protected <E, F extends Enum<F>, S extends GovernanceSource<E, F>> GovernanceBuilder<F> record_officeBuilder_addGovernance(
			String governanceName, String teamName, Class<S> governanceSourceClass, Class<?> extensionType) {
		return this.compileTestSupport.record_officeBuilder_addGovernance(governanceName, teamName,
				governanceSourceClass, extensionType);
	}

	/**
	 * Records registering the {@link EscalationProcedure}.
	 * 
	 * @param typeOfCause  Type of cause handled by {@link EscalationProcedure}.
	 * @param functionName Name of {@link ManagedFunction} to handle
	 *                     {@link Escalation}.
	 */
	protected <E extends Throwable> void record_officeBuilder_addEscalation(Class<E> typeOfCause, String functionName) {
		this.compileTestSupport.record_officeBuilder_addEscalation(typeOfCause, functionName);
	}

	/**
	 * Records registering the {@link Team}.
	 * 
	 * @param officeTeamName      {@link Office} {@link Team} name.
	 * @param officeFloorTeamName {@link OfficeFloor} {@link Team} name.
	 */
	protected void record_officeBuilder_registerTeam(String officeTeamName, String officeFloorTeamName) {
		this.compileTestSupport.record_officeBuilder_registerTeam(officeTeamName, officeFloorTeamName);
	}

	/**
	 * Convenience method to both add the {@link Office} and register a {@link Team}
	 * to it.
	 * 
	 * @param officeName          Name of the {@link Office}.
	 * @param officeTeamName      {@link Office} {@link Team} name.
	 * @param officeFloorTeamName {@link OfficeFloor} {@link Team} name.
	 * @return Added {@link OfficeBuilder}.
	 */
	protected OfficeBuilder record_officeFloorBuilder_addOffice(String officeName, String officeTeamName,
			String officeFloorTeamName) {
		return this.compileTestSupport.record_officeFloorBuilder_addOffice(officeName, officeTeamName,
				officeFloorTeamName);
	}

	/**
	 * Records adding a start-up {@link ManagedFunction} to the
	 * {@link OfficeBuilder}.
	 * 
	 * @param functionName Name of start-up {@link ManagedFunction}.
	 * @param objectType   Expected parameter type. May be <code>null</code>.
	 */
	protected void record_officeBuilder_addStartupFunction(String functionName, Class<?> objectType) {
		this.compileTestSupport.record_officeBuilder_addStartupFunction(functionName, objectType);
	}

	/**
	 * Records adding a {@link ManagedFunctionBuilder}.
	 * 
	 * @param namespace    Namespace.
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @return Added {@link ManagedFunctionBuilder}.
	 */
	protected ManagedFunctionBuilder<?, ?> record_officeBuilder_addFunction(String namespace, String functionName) {
		return this.compileTestSupport.record_officeBuilder_addFunction(namespace, functionName);
	}

	/**
	 * Convenience method for recording adding a {@link ManagedFunctionBuilder} and
	 * specifying the {@link Team} for the {@link ManagedFunction}.
	 * 
	 * @param namespace      Namespace.
	 * @param functionName   Name of the {@link ManagedFunction}.
	 * @param officeTeamName {@link Office} {@link Team} name.
	 * @return Added {@link ManagedFunctionBuilder}.
	 */
	protected ManagedFunctionBuilder<?, ?> record_officeBuilder_addFunction(String namespace, String functionName,
			String officeTeamName) {
		return this.compileTestSupport.record_officeBuilder_addFunction(namespace, functionName, officeTeamName);
	}

	/**
	 * Convenience method for recording an {@link OfficeSection} added via
	 * {@link ClassSectionSource} for a {@link Class} with a single {@link Method}.
	 * 
	 * @param officeName   Name of the {@link Office}.
	 * @param sectionPath  {@link OfficeSection} to {@link SubSection} path.
	 * @param sectionClass {@link Class} for the {@link ClassSectionSource}.
	 * @param functionName Name of the {@link Method}.
	 * @return {@link ManagedFunctionBuilder} for the {@link Method}.
	 */
	public ManagedFunctionBuilder<?, ?> record_officeBuilder_addSectionClassFunction(String officeName,
			String sectionPath, Class<?> sectionClass, String functionName) {
		return this.compileTestSupport.record_officeBuilder_addSectionClassFunction(officeName, sectionPath,
				sectionClass, functionName);
	}

	/**
	 * Convenience method for recording an {@link OfficeSection} added via
	 * {@link ClassSectionSource} for a {@link Class} with a single {@link Method}.
	 * 
	 * @param officeName          Name of the {@link Office}.
	 * @param sectionPath         {@link OfficeSection} to {@link SubSection} path.
	 * @param sectionClass        {@link Class} for the {@link ClassSectionSource}.
	 * @param functionName        Name of the {@link Method}.
	 * @param responsibleTeamName Responsible {@link Team} name. May be
	 *                            <code>null</code>.
	 * @return {@link ManagedFunctionBuilder} for the {@link Method}.
	 */
	public ManagedFunctionBuilder<?, ?> record_officeBuilder_addSectionClassFunction(String officeName,
			String sectionPath, Class<?> sectionClass, String functionName, String responsibleTeamName) {
		return this.compileTestSupport.record_officeBuilder_addSectionClassFunction(officeName, sectionPath,
				sectionClass, functionName, responsibleTeamName);
	}

	/**
	 * Specifies the {@link Team} for the {@link ManagedFunction}.
	 * 
	 * @param officeTeamName {@link Office} {@link Team} name.
	 */
	protected void record_functionBuilder_setResponsibleTeam(String officeTeamName) {
		this.compileTestSupport.record_functionBuilder_setResponsibleTeam(officeTeamName);
	}

	/**
	 * Add an annotation for the {@link ManagedFunction}.
	 * 
	 * @param annotation Annotation.
	 */
	protected void record_functionBuilder_addAnnotation(Object annotation) {
		this.compileTestSupport.record_functionBuilder_addAnnotation(annotation);
	}

	/**
	 * Records adding pre {@link Administration}.
	 * 
	 * @param administrationName Name of the {@link Administration}.
	 * @param extensionType      Extension type.
	 * @return {@link AdministrationBuilder} for the added {@link Administration}.
	 */
	protected <E, F extends Enum<F>, G extends Enum<G>, S extends AdministrationSource<E, F, G>> AdministrationBuilder<F, G> record_functionBuilder_preAdministration(
			String administrationName, Class<E> extensionType) {
		return this.compileTestSupport.record_functionBuilder_preAdministration(administrationName, extensionType);
	}

	/**
	 * Records adding post {@link Administration}.
	 * 
	 * @param administrationName Name of the {@link Administration}.
	 * @param extensionType      Extension type.
	 * @return {@link AdministrationBuilder} for the added {@link Administration}.
	 */
	protected <E, F extends Enum<F>, G extends Enum<G>, S extends AdministrationSource<E, F, G>> AdministrationBuilder<F, G> record_functionBuilder_postAdministration(
			String administrationName, Class<E> extensionType) {
		return this.compileTestSupport.record_functionBuilder_postAdministration(administrationName, extensionType);
	}

	/**
	 * Compiles the {@link OfficeFloor} verifying correctly built into the
	 * {@link OfficeFloorBuilder}.
	 * 
	 * @param isExpectBuild      If the {@link OfficeFloor} is expected to be built.
	 * @param propertyNameValues {@link Property} name/value pair listing for the
	 *                           {@link OfficeFloorCompiler}.
	 */
	protected void compile(boolean isExpectBuild, String... propertyNameValues) {
		this.compileTestSupport.compile(isExpectBuild, propertyNameValues);
	}

}
