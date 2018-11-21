/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.integrate;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.easymock.AbstractMatcher;

import junit.framework.TestCase;
import net.officefloor.compile.AbstractModelCompilerTestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ExecutiveBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectPoolBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.build.ThreadDependencyMappingBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.match.TypeMatcher;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.SectionClassManagedObjectSource;

/**
 * Provides abstract functionality for testing compiling the
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractCompileTestCase extends AbstractModelCompilerTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	protected final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * Enhances {@link CompilerIssues}.
	 */
	private final CompilerIssues enhancedIssues = this.enhanceIssues(this.issues);

	/**
	 * Indicates whether to override the {@link Property} instances.
	 */
	private boolean isOverrideProperties = false;

	/**
	 * {@link FunctionalInterface} to validate the {@link OfficeFloor}.
	 */
	@FunctionalInterface
	protected static interface OfficeFloorValidator {

		/**
		 * Validates the {@link OfficeFloor}.
		 * 
		 * @param officeFloor {@link OfficeFloor} or <code>null</code> if failed to
		 *                    compile.
		 * @throws Throwable If fails to validate.
		 */
		public void validate(OfficeFloor officeFloor) throws Throwable;
	}

	/**
	 * {@link OfficeFloorValidator} instances.
	 */
	protected final List<OfficeFloorValidator> validators = new LinkedList<>();

	/**
	 * {@link OfficeFloorBuilder}.
	 */
	protected final OfficeFloorBuilder officeFloorBuilder = this.createMock(OfficeFloorBuilder.class);

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
		this.isOverrideProperties = true;
	}

	/**
	 * Adds the {@link OfficeFloorValidator}.
	 * 
	 * @param validator {@link OfficeFloorValidator}.
	 */
	protected void addValidator(OfficeFloorValidator validator) {
		this.validators.add(validator);
	}

	/**
	 * Records initialising the {@link SupplierSource} for terminating.
	 */
	protected void record_supplierSetup() {
		this.officeFloorBuilder.addOfficeFloorListener(null);
		// matcher configured in record_init
	}

	/**
	 * Records initialising the {@link OfficeFloorBuilder}.
	 * 
	 * @param resourceSources {@link ResourceSource} instances.
	 */
	protected void record_init(ResourceSource... resourceSources) {

		// Record adding listener for external service handling
		this.officeFloorBuilder.addOfficeFloorListener(null);
		this.control(this.officeFloorBuilder).setMatcher(new TypeMatcher(OfficeFloorListener.class));

		// Record setting the default class loader
		this.officeFloorBuilder.setClassLoader(Thread.currentThread().getContextClassLoader());

		// Record adding the resources
		for (ResourceSource resourceSource : resourceSources) {
			this.officeFloorBuilder.addResources(resourceSource);
		}
		this.officeFloorBuilder.addResources(this.getResourceSource());
	}

	/**
	 * Records specifying the {@link Executive} on the {@link OfficeFloorBuilder}.
	 * 
	 * @param executiveSource    {@link ExecutiveSource}.
	 * @param propertyNameValues {@link Property} name/value listing.
	 * @return {@link ExecutiveBuilder} for the {@link Executive}.
	 */
	@SuppressWarnings("unchecked")
	protected <S extends ExecutiveSource> ExecutiveBuilder<S> record_officeFloorBuilder_setExecutive(S executiveSource,
			String... propertyNameValues) {
		ExecutiveBuilder<S> builder = this.createMock(ExecutiveBuilder.class);
		this.recordReturn(this.officeFloorBuilder, this.officeFloorBuilder.setExecutive(executiveSource), builder);
		this.control(this.officeFloorBuilder).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				return (expected[0].getClass().equals(actual[0].getClass()));
			}
		});
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			builder.addProperty(name, value);
		}
		return builder;
	}

	/**
	 * Flags if the matcher has been specified to add a {@link Team}.
	 */
	private boolean isMatcherSet_officeFloorBuilder_addTeam = false;

	/**
	 * Records adding a {@link Team} to the {@link OfficeFloorBuilder}.
	 * 
	 * @param teamName           Name of the {@link Team}.
	 * @param teamSource         {@link TeamSource} class.
	 * @param propertyNameValues {@link Property} name/value listing.
	 * @return {@link TeamBuilder} for the added {@link Team}.
	 */
	@SuppressWarnings("unchecked")
	protected <S extends TeamSource> TeamBuilder<S> record_officeFloorBuilder_addTeam(String teamName, S teamSource,
			String... propertyNameValues) {
		TeamBuilder<S> builder = this.createMock(TeamBuilder.class);
		this.recordReturn(this.officeFloorBuilder, this.officeFloorBuilder.addTeam(teamName, teamSource), builder);
		if (!this.isMatcherSet_officeFloorBuilder_addTeam) {
			this.control(this.officeFloorBuilder).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					boolean isMatch = (expected[0].equals(actual[0]));
					isMatch = isMatch && (expected[1].getClass().equals(actual[1].getClass()));
					return isMatch;
				}
			});
			this.isMatcherSet_officeFloorBuilder_addTeam = true;
		}
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			builder.addProperty(name, value);
		}
		return builder;
	}

	/**
	 * Current {@link ManagedObjectBuilder}.
	 */
	@SuppressWarnings("rawtypes")
	private ManagedObjectBuilder managedObjectBuilder = null;

	/**
	 * Current {@link ManagingOfficeBuilder}.
	 */
	private ManagingOfficeBuilder<?> managingOfficeBuilder = null;

	/**
	 * Flags if the matcher has been specified to add a {@link ManagedObject}.
	 */
	private boolean isMatcherSet_officeFloorBuilder_addManagedObject = false;

	/**
	 * Records adding a {@link ManagedObjectSource} to the
	 * {@link OfficeFloorBuilder}.
	 * 
	 * @param managedObjectSourceName  Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClass {@link ManagedObjectSource} class.
	 * @param timeout                  Timeout of the {@link ManagedObject}.
	 * @param propertyNameValues       {@link Property} name/value listing.
	 * @param                          {@link ManagedObjectBuilder} for the added
	 *                                 {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	protected <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> ManagedObjectBuilder<F> record_officeFloorBuilder_addManagedObject(
			String managedObjectSourceName, Class<S> managedObjectSourceClass, long timeout,
			String... propertyNameValues) {

		// Instantiate managed object source
		S managedObjectSource;
		try {
			managedObjectSource = managedObjectSourceClass.getDeclaredConstructor().newInstance();
		} catch (Exception ex) {
			throw fail(ex);
		}

		// Record adding the managed object
		this.managedObjectBuilder = this.createMock(ManagedObjectBuilder.class);
		this.recordReturn(this.officeFloorBuilder,
				this.officeFloorBuilder.addManagedObject(managedObjectSourceName, managedObjectSource),
				this.managedObjectBuilder);
		if (!this.isMatcherSet_officeFloorBuilder_addManagedObject) {
			this.control(this.officeFloorBuilder).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					boolean isMatch = (expected[0].equals(actual[0]));
					isMatch &= (expected[1].getClass().equals(actual[1].getClass()));
					return isMatch;
				}
			});
			this.isMatcherSet_officeFloorBuilder_addManagedObject = true;
		}
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			this.managedObjectBuilder.addProperty(name, value);
		}
		this.managedObjectBuilder.setTimeout(timeout);
		return this.managedObjectBuilder;
	}

	/**
	 * Records adding a {@link ManagedObjectSource} to the
	 * {@link OfficeFloorBuilder}.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSource     {@link ManagedObjectSource}.
	 * @param timeout                 Timeout of the {@link ManagedObject}.
	 * @param propertyNameValues      {@link Property} name/value listing.
	 * @param                         {@link ManagedObjectBuilder} for the added
	 *                                {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	protected <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> ManagedObjectBuilder<F> record_officeFloorBuilder_addManagedObject(
			String managedObjectSourceName, S managedObjectSource, long timeout, String... propertyNameValues) {
		this.managedObjectBuilder = this.createMock(ManagedObjectBuilder.class);
		this.recordReturn(this.officeFloorBuilder,
				this.officeFloorBuilder.addManagedObject(managedObjectSourceName, managedObjectSource),
				this.managedObjectBuilder);
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			this.managedObjectBuilder.addProperty(name, value);
		}
		this.managedObjectBuilder.setTimeout(timeout);
		return this.managedObjectBuilder;
	}

	/**
	 * Records specifying the {@link ManagingOffice}.
	 * 
	 * @param officeName Name of the {@link ManagingOffice}.
	 * @return {@link ManagingOfficeBuilder}.
	 */
	protected ManagingOfficeBuilder<?> record_managedObjectBuilder_setManagingOffice(String officeName) {
		this.managingOfficeBuilder = this.createMock(ManagingOfficeBuilder.class);
		this.recordReturn(this.managedObjectBuilder, this.managedObjectBuilder.setManagingOffice(officeName),
				this.managingOfficeBuilder);
		return this.managingOfficeBuilder;
	}

	/**
	 * Records specifying the {@link ManagedObjectPool} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectPoolId Identifier of the {@link ManagedObjectPool}.
	 * @return {@link ManagedObjectPoolBuilder}.
	 */
	protected ManagedObjectPoolBuilder record_managedObjectBuilder_setManagedObjectPool(String managedObjectPoolId) {

		// Obtain the managed object pool
		TestManagedObjectPoolSource pool = TestManagedObjectPoolSource.getManagedObjectPoolSource(managedObjectPoolId);

		// Record creating the managed object pool
		ManagedObjectPoolBuilder poolBuilder = this.createMock(ManagedObjectPoolBuilder.class);
		this.recordReturn(this.managedObjectBuilder, this.managedObjectBuilder.setManagedObjectPool(pool), poolBuilder);
		poolBuilder.addThreadCompletionListener(pool);
		return poolBuilder;
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
		ThreadDependencyMappingBuilder dependencyMapper = this.createMock(ThreadDependencyMappingBuilder.class);
		this.recordReturn(this.managingOfficeBuilder,
				this.managingOfficeBuilder.setInputManagedObjectName(inputManagedObjectName), dependencyMapper);
		return dependencyMapper;
	}

	/**
	 * Current {@link OfficeBuilder}.
	 */
	private OfficeBuilder officeBuilder = null;

	/**
	 * Records adding a {@link OfficeBuilder}.
	 * 
	 * @param officeName Name of the {@link Office}.
	 * @return Added {@link OfficeBuilder}.
	 */
	protected OfficeBuilder record_officeFloorBuilder_addOffice(String officeName) {

		// Record adding the office
		this.officeBuilder = this.createMock(OfficeBuilder.class);
		this.recordReturn(this.officeFloorBuilder, this.officeFloorBuilder.addOffice(officeName), this.officeBuilder);

		// Reset add function matcher as new mock office builder
		this.isMatcherSet_officeBuilder_addFunction = false;

		// Return the office builder
		return this.officeBuilder;
	}

	/**
	 * Current {@link DependencyMappingBuilder}.
	 */
	private DependencyMappingBuilder dependencyMappingBuilder = null;

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
		this.dependencyMappingBuilder = this.createMock(ThreadDependencyMappingBuilder.class);
		this.recordReturn(this.officeBuilder,
				this.officeBuilder.addProcessManagedObject(processManagedObjectName, officeManagedObjectName),
				this.dependencyMappingBuilder);
		return (ThreadDependencyMappingBuilder) this.dependencyMappingBuilder;
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
		this.dependencyMappingBuilder = this.createMock(ThreadDependencyMappingBuilder.class);
		this.recordReturn(this.officeBuilder,
				this.officeBuilder.addThreadManagedObject(threadManagedObjectName, officeManagedObjectName),
				this.dependencyMappingBuilder);
		return (ThreadDependencyMappingBuilder) this.dependencyMappingBuilder;
	}

	/**
	 * Flags if the matcher has been specified to add pre-load
	 * {@link Administration}.
	 */
	private boolean isMatcherSet_dependencyMappingBuilder_preLoadAdministration = false;

	/**
	 * Records adding pre-load {@link Administration}.
	 * 
	 * @param administrationName Name of the {@link Administration}.
	 * @param extensionType      Extension type.
	 * @return {@link AdministrationBuilder} for the added {@link Administration}.
	 */
	@SuppressWarnings("unchecked")
	protected <E, F extends Enum<F>, G extends Enum<G>, S extends AdministrationSource<E, F, G>> AdministrationBuilder<F, G> record_dependencyMappingBuilder_preLoadAdminister(
			String administrationName, Class<E> extensionType) {

		// Record adding pre-administration
		final AdministrationBuilder<F, G> admin = this.createMock(AdministrationBuilder.class);
		AdministrationFactory<E, F, G> adminFactory = null;
		this.recordReturn(this.dependencyMappingBuilder,
				this.dependencyMappingBuilder.preLoadAdminister(administrationName, extensionType, adminFactory),
				admin);
		if (!this.isMatcherSet_dependencyMappingBuilder_preLoadAdministration) {
			this.control(this.dependencyMappingBuilder).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					assertNotNull("Must have administration factory", actual[2]);
					// Match based on name and extension type
					return expected[0].equals(actual[0]) && expected[1].equals(actual[1]);
				}
			});
			this.isMatcherSet_dependencyMappingBuilder_preLoadAdministration = true;
		}

		// Return the admin builder
		return admin;
	}

	/**
	 * Flags if the matcher has been specified to add a {@link Governance}.
	 */
	private boolean isMatcherSet_officeBuilder_addGovernance = false;

	/**
	 * Records adding a {@link GovernanceSource} to the {@link OfficeBuilder}.
	 * 
	 * @param governanceSourceName  Name of the {@link GovernanceSource}.
	 * @param governanceSourceClass {@link GovernanceSource} class.
	 * @param extensionType         Extension type.
	 * @return {@link GovernanceBuilder} for the added {@link GovernanceSource}.
	 */
	@SuppressWarnings("unchecked")
	protected <E, F extends Enum<F>, S extends GovernanceSource<E, F>> GovernanceBuilder<F> record_officeBuilder_addGovernance(
			String governanceName, Class<S> governanceSourceClass, Class<?> extensionType) {
		GovernanceBuilder<F> governanceBuilder = this.createMock(GovernanceBuilder.class);
		this.recordReturn(this.officeBuilder, this.officeBuilder.addGovernance(governanceName, (Class<E>) extensionType,
				(GovernanceFactory<E, F>) null), governanceBuilder);
		if (!this.isMatcherSet_officeBuilder_addGovernance) {
			this.control(this.officeBuilder).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					// Ensure have governance factory
					assertNotNull("Must have governance factory", actual[2]);

					// Match if name and extension type same
					return this.argumentMatches(expected[0], actual[0]) && this.argumentMatches(expected[1], actual[1]);
				}
			});
			this.isMatcherSet_officeBuilder_addGovernance = true;
		}
		return governanceBuilder;
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
		GovernanceBuilder<F> governanceBuilder = this.record_officeBuilder_addGovernance(governanceName,
				governanceSourceClass, extensionType);
		if (teamName != null) {
			governanceBuilder.setResponsibleTeam(teamName);
		}
		return governanceBuilder;
	}

	/**
	 * Records registering the {@link EscalationProcedure}.
	 * 
	 * @param typeOfCause  Type of cause handled by {@link EscalationProcedure}.
	 * @param functionName Name of {@link ManagedFunction} to handle
	 *                     {@link Escalation}.
	 */
	protected <E extends Throwable> void record_officeBuilder_addEscalation(Class<E> typeOfCause, String functionName) {
		this.officeBuilder.addEscalation(typeOfCause, functionName);
	}

	/**
	 * Records registering the {@link Team}.
	 * 
	 * @param officeTeamName      {@link Office} {@link Team} name.
	 * @param officeFloorTeamName {@link OfficeFloor} {@link Team} name.
	 */
	protected void record_officeBuilder_registerTeam(String officeTeamName, String officeFloorTeamName) {
		this.officeBuilder.registerTeam(officeTeamName, officeFloorTeamName);
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
		this.record_officeFloorBuilder_addOffice(officeName);
		this.record_officeBuilder_registerTeam(officeTeamName, officeFloorTeamName);
		return this.officeBuilder;
	}

	/**
	 * Records adding a start-up {@link ManagedFunction} to the
	 * {@link OfficeBuilder}.
	 * 
	 * @param functionName Name of start-up {@link ManagedFunction}.
	 */
	protected void record_officeBuilder_addStartupFunction(String functionName) {
		this.officeBuilder.addStartupFunction(functionName);
	}

	/**
	 * Current {@link ManagedFunctionBuilder}.
	 */
	private ManagedFunctionBuilder<?, ?> functionBuilder;

	/**
	 * Records adding a {@link ManagedFunctionBuilder}.
	 * 
	 * @param namespace    Namespace.
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @return Added {@link ManagedFunctionBuilder}.
	 */
	protected ManagedFunctionBuilder<?, ?> record_officeBuilder_addFunction(String namespace, String functionName) {
		return this.record_officeBuilder_addFunction(namespace, functionName, null);
	}

	/**
	 * Flags if the matcher has been specified to add a {@link ManagedFunction}.
	 */
	private boolean isMatcherSet_officeBuilder_addFunction = false;

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

		// Create the qualified function name
		String qualifiedFunctionName = namespace + "." + functionName;

		// Record adding the function
		this.functionBuilder = this.createMock(ManagedFunctionBuilder.class);
		ManagedFunctionFactory<Indexed, Indexed> functionFactory = null;
		this.recordReturn(this.officeBuilder,
				this.officeBuilder.addManagedFunction(qualifiedFunctionName, functionFactory), this.functionBuilder);
		if (!this.isMatcherSet_officeBuilder_addFunction) {
			this.control(this.officeBuilder).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					assertNotNull("Must have task factory", actual[1]);
					// Match based on function name
					return expected[0].equals(actual[0]);
				}
			});
			this.isMatcherSet_officeBuilder_addFunction = true;
		}

		// Determine if record specifying the team responsible for task
		if (officeTeamName != null) {
			this.functionBuilder.setResponsibleTeam(officeTeamName);
		}

		// Reset administration for new function
		this.isMatcherSet_functionBuilder_preAdministration = false;
		this.isMatcherSet_functionBuilder_postAdministration = false;

		// Return the task builder
		return this.functionBuilder;
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
		return this.record_officeBuilder_addSectionClassFunction(officeName, sectionPath, sectionClass, functionName,
				null);
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

		// Obtain the qualified section object name
		String qualifiedObjectName = officeName + "." + sectionPath + ".OBJECT";

		// Record the function
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction(sectionPath, functionName);

		// Record the responsible team
		if (responsibleTeamName != null) {
			function.setResponsibleTeam(responsibleTeamName);
		}

		// Record the section object
		function.linkManagedObject(0, qualifiedObjectName, sectionClass);
		this.record_officeFloorBuilder_addManagedObject(qualifiedObjectName, SectionClassManagedObjectSource.class, 0,
				SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, sectionClass.getName());
		this.record_managedObjectBuilder_setManagingOffice(officeName);
		this.officeBuilder.registerManagedObjectSource(qualifiedObjectName, qualifiedObjectName);
		this.record_officeBuilder_addThreadManagedObject(qualifiedObjectName, qualifiedObjectName);

		// Return the function
		return function;
	}

	/**
	 * Specifies the {@link Team} for the {@link ManagedFunction}.
	 * 
	 * @param officeTeamName {@link Office} {@link Team} name.
	 */
	protected void record_functionBuilder_setResponsibleTeam(String officeTeamName) {
		this.functionBuilder.setResponsibleTeam(officeTeamName);
	}

	/**
	 * Add an annotation for the {@link ManagedFunction}.
	 * 
	 * @param annotation Annotation.
	 */
	protected void record_functionBuilder_addAnnotation(Object annotation) {
		this.functionBuilder.addAnnotation(annotation);
	}

	/**
	 * Flags if the matcher has been specified to add pre {@link Administration}.
	 */
	private boolean isMatcherSet_functionBuilder_preAdministration = false;

	/**
	 * Records adding pre {@link Administration}.
	 * 
	 * @param administrationName Name of the {@link Administration}.
	 * @param extensionType      Extension type.
	 * @return {@link AdministrationBuilder} for the added {@link Administration}.
	 */
	@SuppressWarnings("unchecked")
	protected <E, F extends Enum<F>, G extends Enum<G>, S extends AdministrationSource<E, F, G>> AdministrationBuilder<F, G> record_functionBuilder_preAdministration(
			String administrationName, Class<E> extensionType) {

		// Record adding pre-administration
		final AdministrationBuilder<F, G> admin = this.createMock(AdministrationBuilder.class);
		AdministrationFactory<E, F, G> adminFactory = null;
		this.recordReturn(this.functionBuilder,
				this.functionBuilder.preAdminister(administrationName, extensionType, adminFactory), admin);
		if (!this.isMatcherSet_functionBuilder_preAdministration) {
			this.control(this.functionBuilder).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					assertNotNull("Must have administration factory", actual[2]);
					// Match based on name and extension type
					return expected[0].equals(actual[0]) && expected[1].equals(actual[1]);
				}
			});
			this.isMatcherSet_functionBuilder_preAdministration = true;
		}

		// Return the admin builder
		return admin;
	}

	/**
	 * Flags if the matcher has been specified to add post {@link Administration}.
	 */
	private boolean isMatcherSet_functionBuilder_postAdministration = false;

	/**
	 * Records adding post {@link Administration}.
	 * 
	 * @param administrationName Name of the {@link Administration}.
	 * @param extensionType      Extension type.
	 * @return {@link AdministrationBuilder} for the added {@link Administration}.
	 */
	@SuppressWarnings("unchecked")
	protected <E, F extends Enum<F>, G extends Enum<G>, S extends AdministrationSource<E, F, G>> AdministrationBuilder<F, G> record_functionBuilder_postAdministration(
			String administrationName, Class<E> extensionType) {

		// Record adding post-administration
		final AdministrationBuilder<F, G> admin = this.createMock(AdministrationBuilder.class);
		AdministrationFactory<E, F, G> adminFactory = null;
		this.recordReturn(this.functionBuilder,
				this.functionBuilder.postAdminister(administrationName, extensionType, adminFactory), admin);
		if (!this.isMatcherSet_functionBuilder_postAdministration) {
			this.control(this.functionBuilder).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					assertNotNull("Must have administration factory", actual[2]);
					// Match based on name and extension type
					return expected[0].equals(actual[0]) && expected[1].equals(actual[1]);
				}
			});
			this.isMatcherSet_functionBuilder_postAdministration = true;
		}

		// Return the admin builder
		return admin;
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

		// OfficeFloor potentially built
		OfficeFloor officeFloor = null;

		// Record building if expected to build OfficeFloor
		if (isExpectBuild) {
			// Create the mock OfficeFloor built
			officeFloor = this.createMock(OfficeFloor.class);

			// Record successfully building the OfficeFloor
			this.recordReturn(this.officeFloorBuilder, this.officeFloorBuilder.buildOfficeFloor(null), officeFloor,
					new TypeMatcher(OfficeFloorIssues.class));
		}

		// Replay the mocks
		this.replayMockObjects();

		// Create the office frame to return the mock OfficeFloor builder
		OfficeFrame officeFrame = new OfficeFrame() {
			@Override
			public OfficeFloorBuilder createOfficeFloorBuilder(String officeFloorName) {
				return AbstractCompileTestCase.this.officeFloorBuilder;
			}
		};

		// Obtain the resource source
		ResourceSource resourceSource = this.getResourceSource();

		// Create the compiler (overriding values to allow testing)
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.enhancedIssues);
		compiler.setOfficeFloorSourceClass(OfficeFloorModelOfficeFloorSource.class);
		compiler.setOfficeFloorLocation("office-floor");
		compiler.setOfficeFrame(officeFrame);
		compiler.addResources(resourceSource);
		if (this.isOverrideProperties) {
			try {
				File propertiesDirectory = this.findFile("net/officefloor/properties");
				compiler.setOverridePropertiesDirectory(propertiesDirectory);
			} catch (FileNotFoundException ex) {
				// Should not happen
				throw fail(ex);
			}
		}

		// Add the properties
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			compiler.addProperty(name, value);
		}

		// Compile the OfficeFloor
		OfficeFloor loadedOfficeFloor = compiler.compile("OfficeFloor");

		// Run the validators
		for (OfficeFloorValidator validator : this.validators) {
			try {
				validator.validate(loadedOfficeFloor);
			} catch (Throwable ex) {
				throw fail(ex);
			}
		}

		// Verify the mocks
		this.verifyMockObjects();

		// Ensure the correct loaded office floor
		if (isExpectBuild) {
			assertEquals("Incorrect built office floor", officeFloor, loadedOfficeFloor);
		} else {
			assertNull("Should not build the office floor", officeFloor);
		}
	}

}