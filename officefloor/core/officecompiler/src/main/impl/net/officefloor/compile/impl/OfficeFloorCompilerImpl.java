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

package net.officefloor.compile.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerConfigurer;
import net.officefloor.compile.OfficeFloorCompilerConfigurerContext;
import net.officefloor.compile.OfficeFloorCompilerConfigurerServiceFactory;
import net.officefloor.compile.TypeLoader;
import net.officefloor.compile.administration.AdministrationLoader;
import net.officefloor.compile.executive.ExecutiveLoader;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.impl.administrator.AdministrationLoaderImpl;
import net.officefloor.compile.impl.executive.ExecutiveLoaderImpl;
import net.officefloor.compile.impl.governance.GovernanceLoaderImpl;
import net.officefloor.compile.impl.issues.FailCompilerIssues;
import net.officefloor.compile.impl.managedfunction.ManagedFunctionLoaderImpl;
import net.officefloor.compile.impl.managedobject.ManagedObjectLoaderImpl;
import net.officefloor.compile.impl.mxbean.OfficeFloorMBeanImpl;
import net.officefloor.compile.impl.office.OfficeLoaderImpl;
import net.officefloor.compile.impl.officefloor.OfficeFloorLoaderImpl;
import net.officefloor.compile.impl.pool.ManagedObjectPoolLoaderImpl;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.section.SectionLoaderImpl;
import net.officefloor.compile.impl.state.autowire.AutoWireStateManagerFactoryImpl;
import net.officefloor.compile.impl.structure.AdministrationNodeImpl;
import net.officefloor.compile.impl.structure.AutoWirerImpl;
import net.officefloor.compile.impl.structure.CompileContextImpl;
import net.officefloor.compile.impl.structure.EscalationNodeImpl;
import net.officefloor.compile.impl.structure.ExecutionStrategyNodeImpl;
import net.officefloor.compile.impl.structure.ExecutiveNodeImpl;
import net.officefloor.compile.impl.structure.FunctionFlowNodeImpl;
import net.officefloor.compile.impl.structure.FunctionNamespaceNodeImpl;
import net.officefloor.compile.impl.structure.FunctionObjectNodeImpl;
import net.officefloor.compile.impl.structure.GovernanceNodeImpl;
import net.officefloor.compile.impl.structure.InputManagedObjectNodeImpl;
import net.officefloor.compile.impl.structure.ManagedFunctionNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectDependencyNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectExecutionStrategyNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectFlowNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectFunctionDependencyNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectPoolNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectSourceNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectTeamNodeImpl;
import net.officefloor.compile.impl.structure.ManagingOfficeNodeImpl;
import net.officefloor.compile.impl.structure.OfficeFloorMBeanRegistratorImpl;
import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.impl.structure.OfficeInputNodeImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.impl.structure.OfficeObjectNodeImpl;
import net.officefloor.compile.impl.structure.OfficeOutputNodeImpl;
import net.officefloor.compile.impl.structure.OfficeStartNodeImpl;
import net.officefloor.compile.impl.structure.OfficeTeamNodeImpl;
import net.officefloor.compile.impl.structure.ResponsibleTeamNodeImpl;
import net.officefloor.compile.impl.structure.SectionInputNodeImpl;
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.impl.structure.SectionObjectNodeImpl;
import net.officefloor.compile.impl.structure.SectionOutputNodeImpl;
import net.officefloor.compile.impl.structure.SuppliedManagedObjectSourceNodeImpl;
import net.officefloor.compile.impl.structure.SupplierNodeImpl;
import net.officefloor.compile.impl.structure.SupplierThreadLocalNodeImpl;
import net.officefloor.compile.impl.structure.TeamNodeImpl;
import net.officefloor.compile.impl.supplier.SupplierLoaderImpl;
import net.officefloor.compile.impl.team.TeamLoaderImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.AdministrationNode;
import net.officefloor.compile.internal.structure.AutoWireDirection;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.AutoWirerVisitor;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.EscalationNode;
import net.officefloor.compile.internal.structure.ExecutionStrategyNode;
import net.officefloor.compile.internal.structure.ExecutiveNode;
import net.officefloor.compile.internal.structure.FunctionFlowNode;
import net.officefloor.compile.internal.structure.FunctionNamespaceNode;
import net.officefloor.compile.internal.structure.FunctionObjectNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedFunctionNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectExecutionStrategyNode;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectFunctionDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectPoolNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagedObjectTeamNode;
import net.officefloor.compile.internal.structure.ManagingOfficeNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeInputNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.internal.structure.OfficeOutputNode;
import net.officefloor.compile.internal.structure.OfficeStartNode;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.internal.structure.OverrideProperties;
import net.officefloor.compile.internal.structure.ResponsibleTeamNode;
import net.officefloor.compile.internal.structure.SectionInputNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.internal.structure.SectionOutputNode;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.internal.structure.SupplierThreadLocalNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.pool.ManagedObjectPoolLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.mbean.MBeanRegistrator;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.state.autowire.AutoWireStateManager;
import net.officefloor.compile.state.autowire.AutoWireStateManagerFactory;
import net.officefloor.compile.state.autowire.AutoWireStateManagerVisitor;
import net.officefloor.compile.supplier.SupplierLoader;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.build.OfficeVisitor;
import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.manage.UnknownOfficeException;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.impl.execute.clock.ClockFactoryImpl;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.OfficeMetaData;

/**
 * <p>
 * {@link OfficeFloorCompiler} implementation.
 * <p>
 * The default {@link OfficeFloorSource} is
 * {@link ApplicationOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCompilerImpl extends OfficeFloorCompiler implements NodeContext, TypeLoader {

	/**
	 * {@link ClockFactory} to allow capture of {@link Clock} instances to be passed
	 * to the {@link OfficeFrame}.
	 */
	private ClockFactory clockFactory = new ClockFactoryImpl();

	/**
	 * {@link ResourceSource} instances.
	 */
	private final List<ResourceSource> resourceSources = new LinkedList<ResourceSource>();

	/**
	 * {@link SourceContext}.
	 */
	private SourceContext sourceContext = null;

	/**
	 * {@link EscalationHandler}.
	 */
	private EscalationHandler escalationHandler = null;

	/**
	 * {@link CompilerIssues}.
	 */
	private CompilerIssues issues;

	/**
	 * {@link OfficeFrame}.
	 */
	private OfficeFrame officeFrame = null;

	/**
	 * Directory containing the override {@link Property} files.
	 */
	private File overridePropertiesDirectory = null;

	/**
	 * {@link MBeanRegistrator}.
	 */
	private MBeanRegistrator mbeanRegistrator = null;

	/**
	 * {@link OfficeFloorListener} instances.
	 */
	private final List<OfficeFloorListener> officeFloorListeners = new LinkedList<>();

	/**
	 * {@link AutoWireStateManagerVisitor} instances.
	 */
	private final List<AutoWireStateManagerVisitor> autoWireStateManagerListeners = new LinkedList<>();

	/**
	 * {@link OfficeFloorSource} {@link Class}.
	 */
	private Class<? extends OfficeFloorSource> officeFloorSourceClass = null;

	/**
	 * {@link OfficeFloorSource}.
	 */
	private OfficeFloorSource officeFloorSource = null;

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private String officeFloorLocation = null;

	/**
	 * Profiles.
	 */
	private final List<String> profiles = new LinkedList<>();

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList properties = new PropertyListImpl();

	/**
	 * {@link OfficeFloorSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> officeFloorAliases = new HashMap<String, Class<?>>();

	/**
	 * {@link OfficeSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> officeSourceAliases = new HashMap<String, Class<?>>();

	/**
	 * {@link SectionSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> sectionSourceAliases = new HashMap<String, Class<?>>();

	/**
	 * {@link ManagedFunctionSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> managedFunctionSourceAliases = new HashMap<String, Class<?>>();

	/**
	 * {@link ManagedObjectSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> managedObjectSourceAliases = new HashMap<String, Class<?>>();

	/**
	 * {@link ManagedObjectPoolSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> managedObjectPoolSourceAliases = new HashMap<>();

	/**
	 * {@link SupplierSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> supplierSourceAliases = new HashMap<String, Class<?>>();

	/**
	 * {@link AdministrationSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> administrationSourceAliases = new HashMap<String, Class<?>>();

	/**
	 * {@link GovernanceSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> governanceSourceAliases = new HashMap<String, Class<?>>();

	/**
	 * {@link TeamSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> teamSourceAliases = new HashMap<String, Class<?>>();

	/**
	 * Mapping of {@link Profiler} by their {@link Office} name.
	 */
	private final Map<String, Profiler> profilers = new HashMap<String, Profiler>();

	/**
	 * Flag indicating if the source aliases have been added.
	 */
	private boolean isSourceAliasesAdded = false;

	/**
	 * Flag indicating if this {@link OfficeFloorCompiler} has been configured with
	 * the {@link OfficeFloorCompilerConfigurer} instances.
	 */
	private boolean isCompilerConfigured = false;

	/**
	 * Ensures that the source aliases have been added.
	 */
	private void ensureSourceAliasesAdded() {
		if (!this.isSourceAliasesAdded) {
			// Not added, so add and flag now added
			this.addSourceAliases();
			this.isSourceAliasesAdded = true;
		}
	}

	/**
	 * <p>
	 * Registers the alias ensuring only the first alias is used.
	 * <p>
	 * This follows class loading behaviour of loading the first class found on the
	 * class path.
	 * 
	 * @param alias            Alias.
	 * @param aliasSourceClass Alias source class.
	 * @param aliasMap         Map of aliases to the alias source class.
	 * @param aliasType        Type of alias for providing a warning of duplicate
	 *                         aliases.
	 */
	private <C> void registerAlias(String alias, C aliasSourceClass, Map<String, C> aliasMap, String aliasType) {

		// Ensure the alias is not already registered
		C sourceClass = aliasMap.get(alias);
		if (sourceClass != null) {

			// Ignore if same class
			if (sourceClass.equals(aliasSourceClass)) {
				return; // same class, therefore ignore
			}

			// Issue as alias with different source classes
			this.getCompilerIssues().addIssue(this, "Duplicate " + aliasType + " alias " + alias);
			return; // do not register the duplicate
		}

		// Register the alias
		aliasMap.put(alias, aliasSourceClass);
	}

	/**
	 * Obtains the {@link ManagedFunctionSource} {@link Class}.
	 * 
	 * @param <S>                       Type of {@link ManagedFunctionSource}.
	 * @param node                      {@link Node} requiring the
	 *                                  {@link ManagedFunctionSource} {@link Class}.
	 * @param managedFunctionSourceName Name of {@link ManagedFunctionSource}
	 *                                  {@link Class} or alias.
	 * @return {@link ManagedFunctionSource} {@link Class}.
	 */
	@SuppressWarnings("unchecked")
	private <S extends ManagedFunctionSource> Class<S> getManagedFunctionSourceClass(Node node,
			String managedFunctionSourceName) {
		return (Class<S>) CompileUtil.obtainClass(managedFunctionSourceName, ManagedFunctionSource.class,
				this.managedFunctionSourceAliases, this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	/**
	 * Obtains the {@link ManagedObjectSource} {@link Class}.
	 * 
	 * @param <S>                     Type of {@link ManagedObjectSource}.
	 * @param node                    {@link Node} requiring the
	 *                                {@link ManagedObjectSource} {@link Class}.
	 * @param managedObjectSourceName Name of {@link ManagedObjectSource}
	 *                                {@link Class} or alias.
	 * @return {@link ManagedObjectSource} {@link Class}.
	 */
	@SuppressWarnings("unchecked")
	private <S extends ManagedObjectSource<?, ?>> Class<S> getManagedObjectSourceClass(Node node,
			String managedObjectSourceName) {
		return (Class<S>) CompileUtil.obtainClass(managedObjectSourceName, ManagedObjectSource.class,
				this.managedObjectSourceAliases, this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	/*
	 * ==================== OfficeFloorCompiler ==============================
	 */

	@Override
	public void setClockFactory(ClockFactory clockFactory) {
		this.clockFactory = clockFactory;

		// Reset source context to include clock
		this.resetRootSourceContext();
	}

	@Override
	public void addResources(ResourceSource resourceSource) {
		this.resourceSources.add(resourceSource);

		// Reset source context to include resources
		this.resetRootSourceContext();
	}

	@Override
	public void setEscalationHandler(EscalationHandler escalationHandler) {
		this.escalationHandler = escalationHandler;
	}

	@Override
	public void setCompilerIssues(CompilerIssues issues) {
		this.issues = issues;
	}

	@Override
	public void setOfficeFrame(OfficeFrame officeFrame) {
		this.officeFrame = officeFrame;
	}

	@Override
	public <S extends OfficeFloorSource> void setOfficeFloorSourceClass(Class<S> officeFloorSourceClass) {
		this.officeFloorSourceClass = officeFloorSourceClass;
	}

	@Override
	public void setOfficeFloorSource(OfficeFloorSource officeFloorSource) {
		this.officeFloorSource = officeFloorSource;
	}

	@Override
	public void setOfficeFloorLocation(String officeFloorLocation) {
		this.officeFloorLocation = officeFloorLocation;
	}

	@Override
	public void setMBeanRegistrator(MBeanRegistrator mbeanRegistrator) {
		this.mbeanRegistrator = mbeanRegistrator;
	}

	@Override
	public void addProfile(String profile) {
		this.profiles.add(profile);

		// Reset source context to include profile
		this.resetRootSourceContext();
	}

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	@Override
	public <S extends OfficeSource> void addOfficeSourceAlias(String alias, Class<S> officeSourceClass) {
		this.registerAlias(alias, officeSourceClass, this.officeSourceAliases, "office");
	}

	@Override
	public <S extends SectionSource> void addSectionSourceAlias(String alias, Class<S> sectionSourceClass) {
		this.registerAlias(alias, sectionSourceClass, this.sectionSourceAliases, "section");
	}

	@Override
	public <S extends ManagedFunctionSource> void addManagedFunctionSourceAlias(String alias,
			Class<S> managedFunctionSourceClass) {
		this.registerAlias(alias, managedFunctionSourceClass, this.managedFunctionSourceAliases, "managed function");
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> void addManagedObjectSourceAlias(
			String alias, Class<S> managedObjectSourceClass) {
		this.registerAlias(alias, managedObjectSourceClass, this.managedObjectSourceAliases, "managed object");
	}

	@Override
	public <S extends ManagedObjectPoolSource> void addManagedObjectPoolSourceAlias(String alias,
			Class<S> managedObjectPoolSourceClass) {
		this.registerAlias(alias, managedObjectPoolSourceClass, this.managedObjectPoolSourceAliases,
				"managed object pool");
	}

	@Override
	public <S extends SupplierSource> void addSupplierSourceAlias(String alias, Class<S> supplierSourceClass) {
		this.registerAlias(alias, supplierSourceClass, this.supplierSourceAliases, "supplier");
	}

	@Override
	public <E, F extends Enum<F>, G extends Enum<G>, S extends AdministrationSource<E, F, G>> void addAdministrationSourceAlias(
			String alias, Class<S> administrationSourceClass) {
		this.registerAlias(alias, administrationSourceClass, this.administrationSourceAliases, "administration");
	}

	@Override
	public <I, F extends Enum<F>, S extends GovernanceSource<I, F>> void addGovernanceSourceAlias(String alias,
			Class<S> governanceSourceClass) {
		this.registerAlias(alias, governanceSourceClass, this.governanceSourceAliases, "governance");
	}

	@Override
	public <S extends TeamSource> void addTeamSourceAlias(String alias, Class<S> teamSourceClass) {
		this.registerAlias(alias, teamSourceClass, this.teamSourceAliases, "team");
	}

	@Override
	public void addProfiler(String officeName, Profiler profiler) {
		this.profilers.put(officeName, profiler);
	}

	@Override
	public void setOverridePropertiesDirectory(File propertiesDirectory) {
		this.overridePropertiesDirectory = propertiesDirectory;
	}

	@Override
	public void addOfficeFloorListener(OfficeFloorListener officeFloorListener) {
		this.officeFloorListeners.add(officeFloorListener);
	}

	@Override
	public void addAutoWireStateManagerVisitor(AutoWireStateManagerVisitor autoWireStateManagerVisitor) {
		this.autoWireStateManagerListeners.add(autoWireStateManagerVisitor);
	}

	@Override
	public PropertyList createPropertyList() {
		return new PropertyListImpl();
	}

	@Override
	public SourceContext createRootSourceContext() {
		return new SourceContextImpl(OfficeFloor.class.getSimpleName(), false,
				this.profiles.toArray(new String[this.profiles.size()]), this.getClassLoader(), this.clockFactory,
				this.resourceSources.toArray(new ResourceSource[this.resourceSources.size()]));
	}

	@Override
	public <N extends Node> AutoWirer<N> createAutoWirer(Class<N> nodeType, AutoWireDirection direction) {
		return new AutoWirerImpl<>(this.getRootSourceContext(), this.getCompilerIssues(), direction);
	}

	@Override
	public TypeLoader getTypeLoader() {
		return this;
	}

	@Override
	public OfficeFloorLoader getOfficeFloorLoader() {
		return new OfficeFloorLoaderImpl(this, this);
	}

	@Override
	public OfficeLoader getOfficeLoader() {
		return new OfficeLoaderImpl(this, this);
	}

	@Override
	public SectionLoader getSectionLoader() {
		OfficeNode officeNode = this.createOfficeNode(this.getNodeName(), null);
		return new SectionLoaderImpl(officeNode, null, this);
	}

	@Override
	public ManagedFunctionLoader getManagedFunctionLoader() {
		return new ManagedFunctionLoaderImpl(this, null, this, true);
	}

	@Override
	public ManagedObjectLoader getManagedObjectLoader() {
		return new ManagedObjectLoaderImpl(this, null, this);
	}

	@Override
	public SupplierLoader getSupplierLoader() {
		return new SupplierLoaderImpl(this, null, this, true);
	}

	@Override
	public GovernanceLoader getGovernanceLoader() {
		return new GovernanceLoaderImpl(this, null, this, true);
	}

	@Override
	public ManagedObjectPoolLoader getManagedObjectPoolLoader() {
		return new ManagedObjectPoolLoaderImpl(this, null, this, true);
	}

	@Override
	public AdministrationLoader getAdministrationLoader() {
		return new AdministrationLoaderImpl(this, null, this, true);
	}

	@Override
	public TeamLoader getTeamLoader() {
		return new TeamLoaderImpl(this, this);
	}

	@Override
	public ExecutiveLoader getExecutiveLoader() {
		return new ExecutiveLoaderImpl(this, this);
	}

	@Override
	public boolean configureOfficeFloorCompiler() {

		// Create source context from compiler properties
		SourcePropertiesImpl sourceProperties = new SourcePropertiesImpl();
		for (Property property : this.properties) {
			sourceProperties.addProperty(property.getName(), property.getValue());
		}
		SourceContext sourceContext = new SourceContextImpl(OfficeFloorCompiler.class.getSimpleName(), false, null,
				this.getRootSourceContext(), sourceProperties);

		// Configure this OfficeFloor compiler
		for (OfficeFloorCompilerConfigurer configurationService : sourceContext
				.loadOptionalServices(OfficeFloorCompilerConfigurerServiceFactory.class)) {
			try {
				configurationService.configureOfficeFloorCompiler(new OfficeFloorCompilerConfigurerContext() {
					@Override
					public OfficeFloorCompiler getOfficeFloorCompiler() {
						return OfficeFloorCompilerImpl.this;
					}

					@Override
					public void setClassLoader(ClassLoader classLoader) throws IllegalArgumentException {

						// Easy access to compiler
						OfficeFloorCompilerImpl compiler = OfficeFloorCompilerImpl.this;

						// Load the compiler
						compiler.setClassLoader(classLoader);

						// Reset source context to use class loader
						compiler.sourceContext = null;
					}
				});
			} catch (Exception ex) {
				this.getCompilerIssues().addIssue(this, configurationService.getClass().getName()
						+ " failed to configure " + OfficeFloorCompiler.class.getSimpleName(), ex);
				return false; // failed to configure compiler
			}
		}

		// As here successfully configure this OfficeFloor compiler
		return true;
	}

	@Override
	public OfficeFloor compile(String officeFloorName) {

		// Ensure aliases are added
		this.ensureSourceAliasesAdded();

		// Ensure compiler is configured
		if (!this.isCompilerConfigured) {
			if (!this.configureOfficeFloorCompiler()) {
				return null; // must configure
			}
			this.isCompilerConfigured = true;
		}

		// Compile, build and return the OfficeFloor
		OfficeFloorSource officeFloorSource;
		if (this.officeFloorSource != null) {
			// Use the OfficeFloor source
			officeFloorSource = this.officeFloorSource;

		} else {
			// Obtain the OfficeFloor source class
			Class<? extends OfficeFloorSource> officeFloorSourceClass = (this.officeFloorSourceClass != null
					? this.officeFloorSourceClass
					: ApplicationOfficeFloorSource.class);

			// Instantiate the OfficeFloor source
			officeFloorSource = CompileUtil.newInstance(officeFloorSourceClass, OfficeFloorSource.class, this,
					this.getCompilerIssues());
			if (officeFloorSource == null) {
				return null; // failed to instantiate
			}
		}

		// Create the MBean registrator
		OfficeFloorMBeanRegistratorImpl officeFloorMBeanRegistrator = (this.mbeanRegistrator != null)
				? new OfficeFloorMBeanRegistratorImpl(this.mbeanRegistrator)
				: null;

		// Determine if capture the auto wirers and office meta-data
		OfficeAutoWirerVisitorImpl officeVisitor = null;
		if (this.autoWireStateManagerListeners.size() > 0) {
			officeVisitor = new OfficeAutoWirerVisitorImpl();
		}

		// Create the compile context
		CompileContextImpl compileContext = new CompileContextImpl(officeFloorMBeanRegistrator);

		// Source the OfficeFloor tree
		OfficeFloorNode node = this.createOfficeFloorNode(officeFloorSource.getClass().getName(), officeFloorSource,
				this.officeFloorLocation);
		this.properties.configureProperties(node);
		boolean isSourced = node.sourceOfficeFloorTree(officeVisitor, compileContext);
		if (!isSourced) {
			return null; // must source tree
		}

		// Obtain the OfficeFloor builder
		OfficeFrame officeFrame = this.getOfficeFrame();
		OfficeFloorBuilder builder = officeFrame.createOfficeFloorBuilder(officeFloorName);
		if (officeVisitor != null) {
			builder.addOfficeVisitor(officeVisitor);
		}

		// Load the profiles
		for (String profile : this.profiles) {
			builder.addProfile(profile);
		}

		// Register the possible MBeans
		if (officeFloorMBeanRegistrator != null) {
			builder.addOfficeFloorListener(officeFloorMBeanRegistrator);
		}

		// Register the external servicer OfficeFloor listener
		builder.addOfficeFloorListener(new ExternalServicingOfficeFloorListener(node));

		// Add compiler configured OfficeFloor listeners
		if (this.clockFactory instanceof OfficeFloorListener) {
			builder.addOfficeFloorListener((OfficeFloorListener) this.clockFactory);
		}
		for (OfficeFloorListener listener : this.officeFloorListeners) {
			builder.addOfficeFloorListener(listener);
		}

		// Add deployer configured OfficeFloor listeners
		for (OfficeFloorListener listener : node.getOfficeFloorListeners()) {
			builder.addOfficeFloorListener(listener);
		}

		// Deploy the OfficeFloor
		OfficeFloor officeFloor = node.deployOfficeFloor(officeFloorName, builder, compileContext);
		if (officeFloor == null) {
			return null; // must compile OfficeFloor
		}

		// Register the OfficeFloor MBean
		if (officeFloorMBeanRegistrator != null) {
			officeFloorMBeanRegistrator.registerPossibleMBean(OfficeFloor.class, officeFloorName,
					new OfficeFloorMBeanImpl(officeFloor));
		}

		// Allow visiting of the auto wire state managers
		if (officeVisitor != null) {

			// Visit each of the Office auto wire state managers
			for (OfficeNode officeNode : officeVisitor.autoWirers.keySet()) {

				// Obtain the office name
				String officeName = officeNode.getQualifiedName();

				// Obtain the auto wirer
				AutoWirer<LinkObjectNode> autoWirer = officeVisitor.autoWirers.get(officeNode);

				// Obtain the office meta-data
				OfficeMetaData officeMetaData = officeVisitor.officeMetaDatas.get(officeName);
				MonitorClock monitorClock = officeMetaData.getMonitorClock();

				// Obtain the internal suppliers for the office
				InternalSupplier[] internalSuppliers = officeNode.getInternalSuppliers();

				// Create the auto wire state manager factory
				AutoWireStateManagerFactory autoWireStateManagerFactory = new AutoWireStateManagerFactoryImpl(
						officeFloor, officeNode, autoWirer, internalSuppliers, monitorClock);

				// Allow visiting
				for (AutoWireStateManagerVisitor visitor : this.autoWireStateManagerListeners) {
					try {
						visitor.visit(officeName, autoWireStateManagerFactory);
					} catch (Exception ex) {
						this.getCompilerIssues().addIssue(this,
								"Failed to visit " + AutoWireStateManager.class.getSimpleName() + " for visitor "
										+ visitor.getClass().getName(),
								ex);
					}
				}
			}
		}

		// Return the OfficeFloor
		return officeFloor;
	}

	/**
	 * External servicing {@link OfficeFloorListener}.
	 */
	private static class ExternalServicingOfficeFloorListener implements OfficeFloorListener {

		/**
		 * {@link OfficeFloorNode}.
		 */
		private OfficeFloorNode officeFloorNode;

		/**
		 * Instantiate.
		 * 
		 * @param officeFloorNode {@link OfficeFloorNode}.
		 */
		public ExternalServicingOfficeFloorListener(OfficeFloorNode officeFloorNode) {
			this.officeFloorNode = officeFloorNode;
		}

		/*
		 * ================= OfficeFloorListener ========================
		 */

		@Override
		public void officeFloorOpened(OfficeFloorEvent event) throws UnknownOfficeException, UnknownFunctionException {

			// Load the external servicing
			this.officeFloorNode.loadExternalServicing(event.getOfficeFloor());

			// Release node for garbage collection
			this.officeFloorNode = null;
		}

		@Override
		public void officeFloorClosed(OfficeFloorEvent event) {
		}
	}

	/**
	 * {@link AutoWirerVisitor} and {@link OfficeVisitor} implementation.
	 */
	private static class OfficeAutoWirerVisitorImpl implements AutoWirerVisitor, OfficeVisitor {

		/**
		 * {@link AutoWirer} instances by their {@link OfficeNode}.
		 */
		private final Map<OfficeNode, AutoWirer<LinkObjectNode>> autoWirers = new HashMap<>();

		/**
		 * {@link OfficeMetaData} instances by their {@link Office} name.
		 */
		private final Map<String, OfficeMetaData> officeMetaDatas = new HashMap<>();

		/*
		 * =================== AutoWirerVisitor ===========================
		 */

		@Override
		public void visit(OfficeNode officeNode, AutoWirer<LinkObjectNode> autoWirer) {
			this.autoWirers.put(officeNode, autoWirer);
		}

		/*
		 * ===================== OfficeVisitor =============================
		 */

		@Override
		public void visit(OfficeMetaData officeMetaData) {
			this.officeMetaDatas.put(officeMetaData.getOfficeName(), officeMetaData);
		}
	}

	/*
	 * ======================= TypeLoader ===================================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public FunctionNamespaceType loadManagedFunctionType(String managedFunctionSourceName,
			String managedFunctionSourceClassName, PropertyList properties) {

		// Obtain the managed function source class
		Class managedFunctionSourceClass = this.getManagedFunctionSourceClass(this, managedFunctionSourceClassName);
		if (managedFunctionSourceClass == null) {
			return null; // not able to load type
		}

		// Load and return the managed function type
		return this.getManagedFunctionLoader().loadManagedFunctionType(managedFunctionSourceClass, properties);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ManagedObjectType<?> loadManagedObjectType(String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties) {

		// Obtain the managed object source class
		Class managedObjectSourceClass = this.getManagedObjectSourceClass(this, managedObjectSourceClassName);
		if (managedObjectSourceClass == null) {
			return null; // not able to load type
		}

		// Load and return the managed object type
		return this.getManagedObjectLoader().loadManagedObjectType(managedObjectSourceClass, properties);
	}

	/*
	 * ===================== NodeContext =====================================
	 */

	@Override
	public SourceContext getRootSourceContext() {
		// Ensure have source context
		if (this.sourceContext == null) {
			this.sourceContext = this.createRootSourceContext();
		}

		// Return the source context
		return this.sourceContext;
	}

	/**
	 * Resets the root {@link SourceContext}.
	 */
	protected void resetRootSourceContext() {
		this.sourceContext = null;
	}

	@Override
	public CompileContext createCompileContext() {
		// Never register MBeans for types
		return new CompileContextImpl(null);
	}

	@Override
	public CompilerIssues getCompilerIssues() {
		// Ensure have compiler issues
		if (this.issues == null) {
			this.issues = new FailCompilerIssues();
		}

		// Return the issues
		return this.issues;
	}

	@Override
	public OfficeFrame getOfficeFrame() {
		// Return the OfficeFrame (default if none specified)
		return (this.officeFrame != null ? this.officeFrame : OfficeFrame.getInstance());
	}

	@Override
	public void initiateOfficeFloorBuilder(OfficeFloorBuilder builder) {
		builder.setClassLoader(this.getClassLoader());
		builder.setClockFactory(this.clockFactory);
		for (ResourceSource resourceSource : this.resourceSources) {
			builder.addResources(resourceSource);
		}
		if (this.escalationHandler != null) {
			builder.setEscalationHandler(this.escalationHandler);
		}
	}

	@Override
	public String[] additionalProfiles(OfficeNode officeNode) {
		return officeNode == null ? new String[0] : officeNode.getAdditionalProfiles();
	}

	@Override
	public PropertyList overrideProperties(Node node, String qualifiedName, PropertyList originalProperties) {
		return this.overrideProperties(node, qualifiedName, null, originalProperties);
	}

	@Override
	public PropertyList overrideProperties(Node node, String qualifiedName, OverrideProperties overrideProperties,
			PropertyList originalProperties) {

		// Create a clone of the properties
		PropertyList overridePropertiesList = this.createPropertyList();
		if (originalProperties != null) {
			for (Property property : originalProperties) {
				overridePropertiesList.addProperty(property.getName(), property.getLabel())
						.setValue(property.getValue());
			}
		}

		// Determine if override the properties via Office overrides
		if (overrideProperties != null) {

			// Obtain the override prefix
			String qualifiedPrefix = qualifiedName + ".";

			// Determine if include override property
			for (Property property : overrideProperties.getOverridePropertyList()) {
				String propertyName = property.getName();
				if (propertyName.startsWith(qualifiedPrefix)) {

					// Override property
					String overridePropertyName = propertyName.substring(qualifiedPrefix.length());
					overridePropertiesList.getOrAddProperty(overridePropertyName).setValue(property.getValue());
				}
			}
		}

		// Determine if override the properties via directory
		if (this.overridePropertiesDirectory != null) {

			// Determine if override properties file
			File propertiesFile = new File(this.overridePropertiesDirectory, qualifiedName + ".properties");
			if (propertiesFile.exists()) {

				// Load the properties
				Properties properties = new Properties();
				try {
					properties.load(new FileReader(propertiesFile));
				} catch (IOException ex) {
					this.getCompilerIssues().addIssue(node, "Failed to override properties for " + qualifiedName, ex);
				}

				// Override the properties
				for (String propertyName : properties.stringPropertyNames()) {
					String propertyValue = properties.getProperty(propertyName);
					overridePropertiesList.getOrAddProperty(propertyName).setValue(propertyValue);
				}
			}
		}

		// Return the properties
		return overridePropertiesList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends OfficeFloorSource> Class<S> getOfficeFloorSourceClass(String officeFloorSourceClassName,
			OfficeFloorNode node) {
		return (Class<S>) CompileUtil.obtainClass(officeFloorSourceClassName, OfficeFloorSource.class,
				this.officeFloorAliases, this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public OfficeFloorLoader getOfficeFloorLoader(OfficeFloorNode node) {
		return new OfficeFloorLoaderImpl(node, this);
	}

	@Override
	public OfficeFloorNode createOfficeFloorNode(String officeFloorSourceClassName, OfficeFloorSource officeFloorSource,
			String officeFloorLocation) {
		return new OfficeFloorNodeImpl(officeFloorSourceClassName, officeFloorSource, officeFloorLocation, this,
				this.profilers);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends OfficeSource> Class<S> getOfficeSourceClass(String officeSourceName, OfficeNode node) {
		return (Class<S>) CompileUtil.obtainClass(officeSourceName, OfficeSource.class, this.officeSourceAliases,
				this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public OfficeLoader getOfficeLoader(OfficeNode node) {
		return new OfficeLoaderImpl(node, this);
	}

	@Override
	public OfficeNode createOfficeNode(String officeName, OfficeFloorNode officeFloor) {
		return new OfficeNodeImpl(officeName, officeFloor, this);
	}

	@Override
	public OfficeInputNode createOfficeInputNode(String officeInputName, OfficeNode office) {
		return new OfficeInputNodeImpl(officeInputName, office, this);
	}

	@Override
	public OfficeObjectNode createOfficeObjectNode(String objectName, OfficeNode office) {
		return new OfficeObjectNodeImpl(objectName, office, this);
	}

	@Override
	public OfficeOutputNode createOfficeOutputNode(String name, OfficeNode office) {
		return new OfficeOutputNodeImpl(name, office, this);
	}

	@Override
	public OfficeStartNode createOfficeStartNode(String startName, OfficeNode office) {
		return new OfficeStartNodeImpl(startName, office, this);
	}

	@Override
	public OfficeTeamNode createOfficeTeamNode(String officeTeamName, OfficeNode office) {
		return new OfficeTeamNodeImpl(officeTeamName, office, this);
	}

	@Override
	public EscalationNode createEscalationNode(String escalationType, OfficeNode officeNode) {
		return new EscalationNodeImpl(escalationType, officeNode, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends SectionSource> Class<S> getSectionSourceClass(String sectionSourceName, SectionNode node) {
		return (Class<S>) CompileUtil.obtainClass(sectionSourceName, SectionSource.class, this.sectionSourceAliases,
				this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public SectionLoader getSectionLoader(SectionNode sectionNode) {
		return new SectionLoaderImpl(sectionNode.getOfficeNode(), sectionNode, this);
	}

	@Override
	public SectionLoader getSectionLoader(OfficeNode officeNode) {
		return new SectionLoaderImpl(officeNode, null, this);
	}

	@Override
	public SectionInputNode createSectionInputNode(String inputName, SectionNode section) {
		return new SectionInputNodeImpl(inputName, section, this);
	}

	@Override
	public SectionObjectNode createSectionObjectNode(String objectName, SectionNode section) {
		return new SectionObjectNodeImpl(objectName, section, this);
	}

	@Override
	public SectionOutputNode createSectionOutputNode(String outputName, SectionNode section) {
		return new SectionOutputNodeImpl(outputName, section, this);
	}

	@Override
	public SectionNode createSectionNode(String sectionName, SectionNode parentSection) {
		return new SectionNodeImpl(true, sectionName, parentSection, parentSection.getOfficeNode(), this);
	}

	@Override
	public SectionNode createSectionNode(String sectionName, OfficeNode office) {
		return new SectionNodeImpl(true, sectionName, null, office, this);
	}

	@Override
	public <S extends ManagedFunctionSource> Class<S> getManagedFunctionSourceClass(String managedFunctionSourceName,
			FunctionNamespaceNode node) {
		return this.getManagedFunctionSourceClass(node, managedFunctionSourceName);
	}

	@Override
	public ManagedFunctionLoader getManagedFunctionLoader(FunctionNamespaceNode node, boolean isLoadingType) {
		OfficeNode officeNode = node.getSectionNode().getOfficeNode();
		return new ManagedFunctionLoaderImpl(node, officeNode, this, isLoadingType);
	}

	@Override
	public <S extends ManagedObjectSource<?, ?>> Class<S> getManagedObjectSourceClass(String managedObjectSourceName,
			ManagedObjectSourceNode node) {
		return this.getManagedObjectSourceClass(node, managedObjectSourceName);
	}

	@Override
	public ManagedObjectLoader getManagedObjectLoader(ManagedObjectSourceNode node) {
		OfficeNode office = node.getOfficeNode();
		return new ManagedObjectLoaderImpl(node, office, this);
	}

	@Override
	public InputManagedObjectNode createInputManagedNode(String inputManagedObjectName, String inputObjectType,
			OfficeFloorNode officeFloor) {
		return new InputManagedObjectNodeImpl(inputManagedObjectName, inputObjectType, officeFloor, this);
	}

	@Override
	public ManagedObjectDependencyNode createManagedObjectDependencyNode(String dependencyName,
			ManagedObjectNode managedObject) {
		return new ManagedObjectDependencyNodeImpl(dependencyName, managedObject, this);
	}

	@Override
	public ManagedObjectDependencyNode createManagedObjectDependencyNode(String dependencyName,
			ManagedObjectSourceNode managedObjectSource) {
		return new ManagedObjectDependencyNodeImpl(dependencyName, managedObjectSource, this);
	}

	@Override
	public ManagedObjectFunctionDependencyNode createManagedObjectFunctionDependencyNode(String dependencyName,
			ManagedObjectSourceNode managedObjectSource) {
		return new ManagedObjectFunctionDependencyNodeImpl(dependencyName, managedObjectSource, this);
	}

	@Override
	public ManagedObjectFlowNode createManagedObjectFlowNode(String flowName,
			ManagedObjectSourceNode managedObjectSource) {
		return new ManagedObjectFlowNodeImpl(flowName, managedObjectSource, this);
	}

	@Override
	public ManagedObjectTeamNode createManagedObjectTeamNode(String teamName,
			ManagedObjectSourceNode managedObjectSource) {
		return new ManagedObjectTeamNodeImpl(teamName, managedObjectSource, this);
	}

	@Override
	public ManagedObjectExecutionStrategyNode createManagedObjectExecutionStrategyNode(String executionStrategyName,
			ManagedObjectSourceNode managedObjectSource) {
		return new ManagedObjectExecutionStrategyNodeImpl(executionStrategyName, managedObjectSource, this);
	}

	@Override
	public ManagingOfficeNode createManagingOfficeNode(ManagedObjectSourceNode managedObjectSource) {
		return new ManagingOfficeNodeImpl(managedObjectSource, this);
	}

	@Override
	public ManagedObjectNode createManagedObjectNode(String managedObjectName, SectionNode section) {
		OfficeNode office = section.getOfficeNode();
		OfficeFloorNode officeFloor = (office != null ? office.getOfficeFloorNode() : null);
		return new ManagedObjectNodeImpl(managedObjectName, section, office, officeFloor, this);
	}

	@Override
	public ManagedObjectNode createManagedObjectNode(String managedObjectName, OfficeNode office) {
		OfficeFloorNode officeFloor = office.getOfficeFloorNode();
		return new ManagedObjectNodeImpl(managedObjectName, null, office, officeFloor, this);
	}

	@Override
	public ManagedObjectNode createManagedObjectNode(String managedObjectName, OfficeFloorNode officeFloor) {
		return new ManagedObjectNodeImpl(managedObjectName, null, null, officeFloor, this);
	}

	@Override
	public ManagedObjectSourceNode createManagedObjectSourceNode(String managedObjectSourceName, SectionNode section) {
		OfficeNode office = section.getOfficeNode();
		OfficeFloorNode officeFloor = (office != null ? office.getOfficeFloorNode() : null);
		return new ManagedObjectSourceNodeImpl(managedObjectSourceName, section, office, null, officeFloor, this);
	}

	@Override
	public ManagedObjectSourceNode createManagedObjectSourceNode(String managedObjectSourceName, OfficeNode office) {
		OfficeFloorNode officeFloor = office.getOfficeFloorNode();
		return new ManagedObjectSourceNodeImpl(managedObjectSourceName, null, office, null, officeFloor, this);
	}

	@Override
	public ManagedObjectSourceNode createManagedObjectSourceNode(String managedObjectSourceName,
			SuppliedManagedObjectSourceNode suppliedManagedObject) {
		OfficeNode office = null;
		OfficeFloorNode officeFloor = null;
		if (suppliedManagedObject != null) {
			SupplierNode supplier = suppliedManagedObject.getSupplierNode();
			office = supplier.getOfficeNode();
			officeFloor = supplier.getOfficeFloorNode();
		}
		return new ManagedObjectSourceNodeImpl(managedObjectSourceName, null, office, suppliedManagedObject,
				officeFloor, this);
	}

	@Override
	public ManagedObjectSourceNode createManagedObjectSourceNode(String managedObjectSourceName,
			OfficeFloorNode officeFloor) {
		return new ManagedObjectSourceNodeImpl(managedObjectSourceName, null, null, null, officeFloor, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends ManagedObjectPoolSource> Class<S> getManagedObjectPoolSourceClass(
			String managedObjectPoolSourceName, ManagedObjectPoolNode node) {
		return (Class<S>) CompileUtil.obtainClass(managedObjectPoolSourceName, ManagedObjectPoolSource.class,
				this.managedObjectPoolSourceAliases, this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public ManagedObjectPoolLoader getManagedObjectPoolLoader(ManagedObjectPoolNode node, OfficeNode officeNode,
			boolean isLoadingType) {
		return new ManagedObjectPoolLoaderImpl(node, officeNode, this, isLoadingType);
	}

	@Override
	public ManagedObjectPoolNode createManagedObjectPoolNode(String managedObjectPoolName,
			OfficeFloorNode officeFloorNode) {
		return new ManagedObjectPoolNodeImpl(managedObjectPoolName, null, null, officeFloorNode, this);
	}

	@Override
	public ManagedObjectPoolNode createManagedObjectPoolNode(String managedObjectPoolName, OfficeNode officeNode) {
		OfficeFloorNode officeFloorNode = officeNode.getOfficeFloorNode();
		return new ManagedObjectPoolNodeImpl(managedObjectPoolName, null, officeNode, officeFloorNode, this);
	}

	@Override
	public ManagedObjectPoolNode createManagedObjectPoolNode(String managedObjectPoolName, SectionNode sectionNode) {
		OfficeNode officeNode = sectionNode.getOfficeNode();
		OfficeFloorNode officeFloorNode = (officeNode == null ? null : officeNode.getOfficeFloorNode());
		return new ManagedObjectPoolNodeImpl(managedObjectPoolName, sectionNode, officeNode, officeFloorNode, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends SupplierSource> Class<S> getSupplierSourceClass(String supplierSourceClassName,
			SupplierNode node) {
		return (Class<S>) CompileUtil.obtainClass(supplierSourceClassName, SupplierSource.class,
				this.supplierSourceAliases, this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public SupplierLoader getSupplierLoader(SupplierNode node, boolean isLoadingType) {
		OfficeNode office = node.getOfficeNode();
		return new SupplierLoaderImpl(node, office, this, isLoadingType);
	}

	@Override
	public SupplierThreadLocalNode createSupplierThreadLocalNode(String qualifier, String type, SupplierNode supplier) {
		return new SupplierThreadLocalNodeImpl(qualifier, type, supplier, this);
	}

	@Override
	public SuppliedManagedObjectSourceNode createSuppliedManagedObjectSourceNode(String qualifier, String type,
			SupplierNode supplier) {
		return new SuppliedManagedObjectSourceNodeImpl(qualifier, type, supplier, this);
	}

	@Override
	public SupplierNode createSupplierNode(String supplierName, OfficeFloorNode officeFloor) {
		return new SupplierNodeImpl(supplierName, null, officeFloor, this);
	}

	@Override
	public SupplierNode createSupplierNode(String supplierName, OfficeNode office) {
		OfficeFloorNode officeFloor = office.getOfficeFloorNode();
		return new SupplierNodeImpl(supplierName, office, officeFloor, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends AdministrationSource<?, ?, ?>> Class<S> getAdministrationSourceClass(
			String administrationSourceName, AdministrationNode node) {
		return (Class<S>) CompileUtil.obtainClass(administrationSourceName, AdministrationSource.class,
				this.administrationSourceAliases, this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public AdministrationLoader getAdministrationLoader(AdministrationNode node, boolean isLoadingType) {
		OfficeNode office = (OfficeNode) node.getParentNode();
		return new AdministrationLoaderImpl(node, office, this, isLoadingType);
	}

	@Override
	public AdministrationNode createAdministrationNode(String administrationName, OfficeNode office) {
		return new AdministrationNodeImpl(administrationName, office, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends GovernanceSource<?, ?>> Class<S> getGovernanceSourceClass(String governanceSourceName,
			GovernanceNode node) {
		return (Class<S>) CompileUtil.obtainClass(governanceSourceName, GovernanceSource.class,
				this.governanceSourceAliases, this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public GovernanceLoader getGovernanceLoader(GovernanceNode node, boolean isLoadingType) {
		OfficeNode office = (OfficeNode) node.getParentNode();
		return new GovernanceLoaderImpl(node, office, this, isLoadingType);
	}

	@Override
	public GovernanceNode createGovernanceNode(String governanceName, OfficeNode office) {
		return new GovernanceNodeImpl(governanceName, office, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends TeamSource> Class<S> getTeamSourceClass(String teamSourceName, TeamNode node) {
		return (Class<S>) CompileUtil.obtainClass(teamSourceName, TeamSource.class, this.teamSourceAliases,
				this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public TeamLoader getTeamLoader(TeamNode node) {
		return new TeamLoaderImpl(node, this);
	}

	@Override
	public TeamNode createTeamNode(String teamName, OfficeFloorNode officeFloor) {
		return new TeamNodeImpl(teamName, officeFloor, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends ExecutiveSource> Class<S> getExecutiveSourceClass(String executiveSourceClassName,
			ExecutiveNode node) {
		return (Class<S>) CompileUtil.obtainClass(executiveSourceClassName, ExecutiveSource.class, new HashMap<>(),
				this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public ExecutiveLoader getExecutiveLoader(ExecutiveNode node) {
		return new ExecutiveLoaderImpl(node, this);
	}

	@Override
	public ExecutiveNode createExecutiveNode(OfficeFloorNode officeFloor) {
		return new ExecutiveNodeImpl(officeFloor, this);
	}

	@Override
	public ExecutionStrategyNode createExecutionStrategyNode(String executionStrategyName, ExecutiveNode executive) {
		return new ExecutionStrategyNodeImpl(executionStrategyName, executive, this);
	}

	@Override
	public FunctionFlowNode createFunctionFlowNode(String flowName, boolean isEscalation,
			ManagedFunctionNode function) {
		return new FunctionFlowNodeImpl(flowName, isEscalation, function, this);
	}

	@Override
	public FunctionObjectNode createFunctionObjectNode(String objectName, ManagedFunctionNode functionNode) {
		return new FunctionObjectNodeImpl(objectName, functionNode, this);
	}

	@Override
	public ResponsibleTeamNode createResponsibleTeamNode(String teamName, ManagedFunctionNode function) {
		return new ResponsibleTeamNodeImpl(teamName, function, this);
	}

	@Override
	public ManagedFunctionNode createFunctionNode(String functionName, SectionNode section) {
		return new ManagedFunctionNodeImpl(functionName, section, this);
	}

	@Override
	public FunctionNamespaceNode createFunctionNamespaceNode(String functionNamespaceName, SectionNode section) {
		return new FunctionNamespaceNodeImpl(functionNamespaceName, section, this);
	}

}
