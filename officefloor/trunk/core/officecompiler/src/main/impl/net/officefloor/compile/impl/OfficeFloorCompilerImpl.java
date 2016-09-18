/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.compile.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.impl.supplier.SupplierLoaderImpl;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.supplier.SupplierLoader;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.TypeLoader;
import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.impl.administrator.AdministratorLoaderImpl;
import net.officefloor.compile.impl.governance.GovernanceLoaderImpl;
import net.officefloor.compile.impl.issues.StderrCompilerIssues;
import net.officefloor.compile.impl.managedobject.ManagedObjectLoaderImpl;
import net.officefloor.compile.impl.office.OfficeLoaderImpl;
import net.officefloor.compile.impl.officefloor.OfficeFloorLoaderImpl;
import net.officefloor.compile.impl.pool.ManagedObjectPoolLoaderImpl;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.section.SectionLoaderImpl;
import net.officefloor.compile.impl.structure.AdministratorNodeImpl;
import net.officefloor.compile.impl.structure.EscalationNodeImpl;
import net.officefloor.compile.impl.structure.GovernanceNodeImpl;
import net.officefloor.compile.impl.structure.InputManagedObjectNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectDependencyNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectFlowNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectSourceNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectTeamNodeImpl;
import net.officefloor.compile.impl.structure.ManagingOfficeNodeImpl;
import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.impl.structure.OfficeInputNodeImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.impl.structure.OfficeObjectNodeImpl;
import net.officefloor.compile.impl.structure.OfficeOutputNodeImpl;
import net.officefloor.compile.impl.structure.OfficeStartNodeImpl;
import net.officefloor.compile.impl.structure.OfficeTeamNodeImpl;
import net.officefloor.compile.impl.structure.SectionInputNodeImpl;
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.impl.structure.SectionObjectNodeImpl;
import net.officefloor.compile.impl.structure.SectionOutputNodeImpl;
import net.officefloor.compile.impl.structure.SuppliedManagedObjectNodeImpl;
import net.officefloor.compile.impl.structure.SupplierNodeImpl;
import net.officefloor.compile.impl.structure.TaskFlowNodeImpl;
import net.officefloor.compile.impl.structure.TaskNodeImpl;
import net.officefloor.compile.impl.structure.TaskObjectNodeImpl;
import net.officefloor.compile.impl.structure.TaskTeamNodeImpl;
import net.officefloor.compile.impl.structure.TeamNodeImpl;
import net.officefloor.compile.impl.structure.WorkNodeImpl;
import net.officefloor.compile.impl.team.TeamLoaderImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.work.WorkLoaderImpl;
import net.officefloor.compile.internal.structure.AdministratorNode;
import net.officefloor.compile.internal.structure.EscalationNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
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
import net.officefloor.compile.internal.structure.SectionInputNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.internal.structure.SectionOutputNode;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.internal.structure.TaskFlowNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.TaskObjectNode;
import net.officefloor.compile.internal.structure.TaskTeamNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.pool.ManagedObjectPoolLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.work.WorkLoader;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;

/**
 * <p>
 * {@link OfficeFloorCompiler} implementation.
 * <p>
 * The default {@link OfficeFloorSource} is
 * {@link OfficeFloorModelOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCompilerImpl extends OfficeFloorCompiler implements
		NodeContext, TypeLoader {

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
	private CompilerIssues issues = new StderrCompilerIssues();

	/**
	 * {@link OfficeFrame}.
	 */
	private OfficeFrame officeFrame = null;

	/**
	 * {@link OfficeFloorSource} {@link Class}.
	 */
	private Class<? extends OfficeFloorSource> officeFloorSourceClass = null;

	/**
	 * {@link OfficeFloorSource}.
	 */
	private OfficeFloorSource officeFloorSource = null;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList properties = new PropertyListImpl();

	/**
	 * {@link OfficeSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> officeSourceAliases = new HashMap<String, Class<?>>();

	/**
	 * {@link SectionSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> sectionSourceAliases = new HashMap<String, Class<?>>();

	/**
	 * {@link WorkSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> workSourceAliases = new HashMap<String, Class<?>>();

	/**
	 * {@link ManagedObjectSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> managedObjectSourceAliases = new HashMap<String, Class<?>>();

	/**
	 * {@link SupplierSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> supplierSourceAliases = new HashMap<String, Class<?>>();

	/**
	 * {@link AdministratorSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<?>> administratorSourceAliases = new HashMap<String, Class<?>>();

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
	 * This follows class loading behaviour of loading the first class found on
	 * the class path.
	 * 
	 * @param alias
	 *            Alias.
	 * @param aliasSourceClass
	 *            Alias source class.
	 * @param aliasMap
	 *            Map of aliases to the alias source class.
	 * @param aliasType
	 *            Type of alias for providing a warning of duplicate aliases.
	 */
	private <C> void registerAlias(String alias, C aliasSourceClass,
			Map<String, C> aliasMap, String aliasType) {

		// Ensure the alias is not already registered
		C sourceClass = aliasMap.get(alias);
		if (sourceClass != null) {

			// Ignore if same class
			if (sourceClass.equals(aliasSourceClass)) {
				return; // same class, therefore ignore
			}

			// Issue as alias with different source classes
			this.getCompilerIssues().addIssue(this,
					"Duplicate " + aliasType + " alias " + alias);
			return; // do not register the duplicate
		}

		// Register the alias
		aliasMap.put(alias, aliasSourceClass);
	}

	/*
	 * ==================== OfficeFloorCompiler ==============================
	 */

	@Override
	public void addResources(ResourceSource resourceSource) {
		this.resourceSources.add(resourceSource);
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
	public <S extends OfficeFloorSource> void setOfficeFloorSourceClass(
			Class<S> officeFloorSourceClass) {
		this.officeFloorSourceClass = officeFloorSourceClass;
	}

	@Override
	public void setOfficeFloorSource(OfficeFloorSource officeFloorSource) {
		this.officeFloorSource = officeFloorSource;
	}

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	@Override
	public <S extends OfficeSource> void addOfficeSourceAlias(String alias,
			Class<S> officeSourceClass) {
		this.registerAlias(alias, officeSourceClass, this.officeSourceAliases,
				"office");
	}

	@Override
	public <S extends SectionSource> void addSectionSourceAlias(String alias,
			Class<S> sectionSourceClass) {
		this.registerAlias(alias, sectionSourceClass,
				this.sectionSourceAliases, "section");
	}

	@Override
	public <W extends Work, S extends WorkSource<W>> void addWorkSourceAlias(
			String alias, Class<S> workSourceClass) {
		this.registerAlias(alias, workSourceClass, this.workSourceAliases,
				"work");
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> void addManagedObjectSourceAlias(
			String alias, Class<S> managedObjectSourceClass) {
		this.registerAlias(alias, managedObjectSourceClass,
				this.managedObjectSourceAliases, "managed object");
	}

	@Override
	public <S extends SupplierSource> void addSupplierSourceAlias(String alias,
			Class<S> supplierSourceClass) {
		this.registerAlias(alias, supplierSourceClass,
				this.supplierSourceAliases, "supplier");
	}

	@Override
	public <I, A extends Enum<A>, S extends AdministratorSource<I, A>> void addAdministratorSourceAlias(
			String alias, Class<S> administratorSourceClass) {
		this.registerAlias(alias, administratorSourceClass,
				this.administratorSourceAliases, "administrator");
	}

	@Override
	public <I, F extends Enum<F>, S extends GovernanceSource<I, F>> void addGovernanceSourceAlias(
			String alias, Class<S> governanceSourceClass) {
		this.registerAlias(alias, governanceSourceClass,
				this.governanceSourceAliases, "governance");
	}

	@Override
	public <S extends TeamSource> void addTeamSourceAlias(String alias,
			Class<S> teamSourceClass) {
		this.registerAlias(alias, teamSourceClass, this.teamSourceAliases,
				"team");
	}

	@Override
	public void addProfiler(String officeName, Profiler profiler) {
		this.profilers.put(officeName, profiler);
	}

	@Override
	public PropertyList createPropertyList() {
		return new PropertyListImpl();
	}

	@Override
	public TypeLoader getTypeLoader() {
		return this;
	}

	@Override
	public OfficeFloorLoader getOfficeFloorLoader() {
		return new OfficeFloorLoaderImpl(this, this, this.profilers);
	}

	@Override
	public OfficeLoader getOfficeLoader() {
		return new OfficeLoaderImpl(this, this);
	}

	@Override
	public SectionLoader getSectionLoader() {
		return new SectionLoaderImpl(this, this);
	}

	@Override
	public WorkLoader getWorkLoader() {
		return new WorkLoaderImpl(this, this);
	}

	@Override
	public ManagedObjectLoader getManagedObjectLoader() {
		return new ManagedObjectLoaderImpl(this, this);
	}

	@Override
	public SupplierLoader getSupplierLoader() {
		return new SupplierLoaderImpl(this, this);
	}

	@Override
	public GovernanceLoader getGovernanceLoader() {
		return new GovernanceLoaderImpl(this, this);
	}

	@Override
	public ManagedObjectPoolLoader getManagedObjectPoolLoader() {
		return new ManagedObjectPoolLoaderImpl(this, this);
	}

	@Override
	public AdministratorLoader getAdministratorLoader() {
		return new AdministratorLoaderImpl(this, this);
	}

	@Override
	public TeamLoader getTeamLoader() {
		return new TeamLoaderImpl(this, this);
	}

	@Override
	public OfficeFloor compile(String officeFloorLocation) {

		// Ensure aliases are added
		this.ensureSourceAliasesAdded();

		// Create the OfficeFloor loader
		OfficeFloorLoader officeFloorLoader = this.getOfficeFloorLoader();

		// Compile, build and return the office floor
		if (this.officeFloorSource != null) {
			// Load from supplied instance
			return officeFloorLoader.loadOfficeFloor(this.officeFloorSource,
					officeFloorLocation, this.properties);

		} else {
			// Obtain the OfficeFloor source class
			Class<? extends OfficeFloorSource> officeFloorSourceClass = (this.officeFloorSourceClass != null ? this.officeFloorSourceClass
					: OfficeFloorModelOfficeFloorSource.class);

			// Load from class
			return officeFloorLoader.loadOfficeFloor(officeFloorSourceClass,
					officeFloorLocation, this.properties);
		}
	}

	/*
	 * ======================= TypeLoader ===================================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public WorkType<?> loadWorkType(String workSourceClassName,
			PropertyList properties) {

		// Obtain the work source class
		Class workSourceClass = this.getWorkSourceClass(workSourceClassName,
				this);
		if (workSourceClass == null) {
			return null; // not able to load type
		}

		// Load and return the work type
		return this.getWorkLoader().loadWorkType(workSourceClass, properties);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ManagedObjectType<?> loadManagedObjectType(
			String managedObjectSourceClassName, PropertyList properties) {

		// Obtain the managed object source class
		Class managedObjectSourceClass = this.getManagedObjectSourceClass(
				managedObjectSourceClassName, this);
		if (managedObjectSourceClass == null) {
			return null; // not able to load type
		}

		// Load and return the managed object type
		return this.getManagedObjectLoader().loadManagedObjectType(
				managedObjectSourceClass, properties);
	}

	/*
	 * ===================== NodeContext =====================================
	 */

	@Override
	public SourceContext getRootSourceContext() {
		// Ensure have source context
		if (this.sourceContext == null) {
			this.sourceContext = new SourceContextImpl(false,
					this.getClassLoader(),
					this.resourceSources
							.toArray(new ResourceSource[this.resourceSources
									.size()]));
		}

		// Return the source context
		return this.sourceContext;
	}

	@Override
	public CompilerIssues getCompilerIssues() {
		// Ensure have compiler issues
		if (this.issues == null) {
			this.issues = new StderrCompilerIssues();
		}

		// Return the issues
		return this.issues;
	}

	@Override
	public OfficeFrame getOfficeFrame() {
		// Return the OfficeFrame (default if none specified)
		return (this.officeFrame != null ? this.officeFrame : OfficeFrame
				.getInstance());
	}

	@Override
	public void initiateOfficeFloorBuilder(OfficeFloorBuilder builder) {
		builder.setClassLoader(this.getClassLoader());
		for (ResourceSource resourceSource : this.resourceSources) {
			builder.addResources(resourceSource);
		}
		if (this.escalationHandler != null) {
			builder.setEscalationHandler(this.escalationHandler);
		}
	}

	@Override
	public <S extends OfficeFloorSource> Class<S> getOfficeFloorSourceClass(
			String officeFloorSourceClassName, Node node) {
		// TODO implement NodeContext.getOfficeFloorSourceClass
		throw new UnsupportedOperationException(
				"TODO implement NodeContext.getOfficeFloorSourceClass");

	}

	@Override
	public OfficeFloorLoader getOfficeFloorLoader(Node node) {
		// TODO implement NodeContext.getOfficeFloorLoader
		throw new UnsupportedOperationException(
				"TODO implement NodeContext.getOfficeFloorLoader");
	}

	@Override
	public OfficeFloorNode createOfficeFloorNode(
			String officeFloorSourceClassName,
			OfficeFloorSource officeFloorSource, String officeFloorLocation) {
		return new OfficeFloorNodeImpl(officeFloorSourceClassName,
				officeFloorSource, officeFloorLocation, this, this.profilers);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends OfficeSource> Class<S> getOfficeSourceClass(
			String officeSourceName, Node node) {
		return (Class<S>) CompileUtil.obtainClass(officeSourceName,
				OfficeSource.class, this.officeSourceAliases,
				this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public OfficeLoader getOfficeLoader(Node node) {
		return new OfficeLoaderImpl(node, this);
	}

	@Override
	public OfficeNode createOfficeNode(String officeName,
			String officeSourceClassName, OfficeSource officeSource,
			String officeLocation, OfficeFloorNode officeFloor) {
		return new OfficeNodeImpl(officeName, officeSourceClassName,
				officeSource, officeLocation, officeFloor, this);
	}

	@Override
	public OfficeInputNode createOfficeInputNode(String officeInputName,
			String parameterType, OfficeNode office) {
		return new OfficeInputNodeImpl(officeInputName, parameterType, office,
				this);
	}

	@Override
	public OfficeObjectNode createOfficeObjectNode(String objectName,
			OfficeNode office) {
		return new OfficeObjectNodeImpl(objectName, office, this);
	}

	@Override
	public OfficeOutputNode createOfficeOutputNode(String name,
			String argumentType, OfficeNode office) {
		return new OfficeOutputNodeImpl(name, argumentType, office, this);
	}

	@Override
	public OfficeStartNode createOfficeStartNode(String startName,
			OfficeNode office) {
		return new OfficeStartNodeImpl(startName, office, this);
	}

	@Override
	public OfficeTeamNode createOfficeTeamNode(String officeTeamName,
			OfficeNode office) {
		return new OfficeTeamNodeImpl(officeTeamName, office, this);
	}

	@Override
	public EscalationNode createEscalationNode(String escalationType,
			OfficeNode officeNode) {
		return new EscalationNodeImpl(escalationType, officeNode, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends SectionSource> Class<S> getSectionSourceClass(
			String sectionSourceName, Node node) {
		return (Class<S>) CompileUtil.obtainClass(sectionSourceName,
				SectionSource.class, this.sectionSourceAliases,
				this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public SectionLoader getSectionLoader(Node node) {
		return new SectionLoaderImpl(node, this);
	}

	@Override
	public SectionInputNode createSectionInputNode(String inputName,
			SectionNode section) {
		return new SectionInputNodeImpl(inputName, section, this);
	}

	@Override
	public SectionObjectNode createSectionObjectNode(String objectName,
			SectionNode section) {
		return new SectionObjectNodeImpl(objectName, section, this);
	}

	@Override
	public SectionOutputNode createSectionOutputNode(String outputName,
			SectionNode section) {
		return new SectionOutputNodeImpl(outputName, section, this);
	}

	@Override
	public SectionNode createSectionNode(String sectionName,
			SectionNode parentSection) {
		return new SectionNodeImpl(sectionName, parentSection,
				parentSection.getOfficeNode(), this);
	}

	@Override
	public SectionNode createSectionNode(String sectionName, OfficeNode office) {
		return new SectionNodeImpl(sectionName, null, office, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends WorkSource<?>> Class<S> getWorkSourceClass(
			String workSourceName, Node node) {
		return (Class<S>) CompileUtil.obtainClass(workSourceName,
				WorkSource.class, this.workSourceAliases,
				this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public WorkLoader getWorkLoader(Node node) {
		return new WorkLoaderImpl(node, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends ManagedObjectSource<?, ?>> Class<S> getManagedObjectSourceClass(
			String managedObjectSourceName, Node node) {
		return (Class<S>) CompileUtil.obtainClass(managedObjectSourceName,
				ManagedObjectSource.class, this.managedObjectSourceAliases,
				this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public ManagedObjectLoader getManagedObjectLoader(Node node) {
		return new ManagedObjectLoaderImpl(node, this);
	}

	@Override
	public InputManagedObjectNode createInputManagedNode(
			String inputManagedObjectName, OfficeFloorNode officeFloor) {
		return new InputManagedObjectNodeImpl(inputManagedObjectName,
				officeFloor, this);
	}

	@Override
	public ManagedObjectDependencyNode createManagedObjectDependencyNode(
			String dependencyName, ManagedObjectNode managedObject) {
		return new ManagedObjectDependencyNodeImpl(dependencyName,
				managedObject, this);
	}

	@Override
	public ManagedObjectDependencyNode createManagedObjectDependencyNode(
			String dependencyName, ManagedObjectSourceNode managedObjectSource) {
		return new ManagedObjectDependencyNodeImpl(dependencyName,
				managedObjectSource, this);
	}

	@Override
	public ManagedObjectFlowNode createManagedObjectFlowNode(String flowName,
			ManagedObjectSourceNode managedObjectSource) {
		return new ManagedObjectFlowNodeImpl(flowName, managedObjectSource,
				this);
	}

	@Override
	public ManagedObjectTeamNode createManagedObjectTeamNode(String teamName,
			ManagedObjectSourceNode managedObjectSource) {
		return new ManagedObjectTeamNodeImpl(teamName, managedObjectSource,
				this);
	}

	@Override
	public ManagingOfficeNode createManagingOfficeNode(
			ManagedObjectSourceNode managedObjectSource) {
		return new ManagingOfficeNodeImpl(managedObjectSource, this);
	}

	@Override
	public ManagedObjectNode createManagedObjectNode(String managedObjectName,
			ManagedObjectScope managedObjectScope,
			ManagedObjectSourceNode managedObjectSourceNode) {
		return new ManagedObjectNodeImpl(managedObjectName, managedObjectScope,
				managedObjectSourceNode, this);
	}

	@Override
	public ManagedObjectSourceNode createManagedObjectSourceNode(
			String managedObjectSourceName,
			String managedObjectSourceClassName,
			ManagedObjectSource<?, ?> managedObjectSource, SectionNode section) {
		OfficeNode office = section.getOfficeNode();
		OfficeFloorNode officeFloor = (office != null ? office
				.getOfficeFloorNode() : null);
		return new ManagedObjectSourceNodeImpl(managedObjectSourceName, null,
				managedObjectSourceClassName, managedObjectSource, section,
				office, officeFloor, this);
	}

	@Override
	public ManagedObjectSourceNode createManagedObjectSourceNode(
			String managedObjectSourceName,
			String managedObjectSourceClassName,
			ManagedObjectSource<?, ?> managedObjectSource, OfficeNode office) {
		OfficeFloorNode officeFloor = office.getOfficeFloorNode();
		return new ManagedObjectSourceNodeImpl(managedObjectSourceName, null,
				managedObjectSourceClassName, managedObjectSource, null,
				office, officeFloor, this);
	}

	@Override
	public ManagedObjectSourceNode createManagedObjectSourceNode(
			String managedObjectSourceName,
			String managedObjectSourceClassName,
			ManagedObjectSource<?, ?> managedObjectSource,
			SuppliedManagedObjectNode suppliedManagedObject) {
		OfficeFloorNode officeFloor = suppliedManagedObject.getSupplierNode()
				.getOfficeFloorNode();
		return new ManagedObjectSourceNodeImpl(managedObjectSourceName,
				suppliedManagedObject, managedObjectSourceClassName,
				managedObjectSource, null, null, officeFloor, this);
	}

	@Override
	public ManagedObjectSourceNode createManagedObjectSourceNode(
			String managedObjectSourceName,
			String managedObjectSourceClassName,
			ManagedObjectSource<?, ?> managedObjectSource,
			OfficeFloorNode officeFloor) {
		return new ManagedObjectSourceNodeImpl(managedObjectSourceName, null,
				managedObjectSourceClassName, managedObjectSource, null, null,
				officeFloor, this);
	}

	@Override
	public ManagedObjectPoolLoader getManagedObjectPoolLoader(Node node) {
		return new ManagedObjectPoolLoaderImpl(node, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends SupplierSource> Class<S> getSupplierSourceClass(
			String supplierSourceClassName, Node node) {
		return (Class<S>) CompileUtil.obtainClass(supplierSourceClassName,
				SupplierSource.class, this.supplierSourceAliases,
				this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public SupplierLoader getSupplierLoader(Node node) {
		return new SupplierLoaderImpl(node, this);
	}

	@Override
	public SuppliedManagedObjectNode createSuppliedManagedObjectNode(
			AutoWire autoWire, SupplierNode supplier) {
		return new SuppliedManagedObjectNodeImpl(autoWire, supplier, this);
	}

	@Override
	public SupplierNode createSupplierNode(String supplierName,
			String supplierSourceClassName, OfficeFloorNode officeFloor) {
		return new SupplierNodeImpl(supplierName, supplierSourceClassName,
				officeFloor, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends AdministratorSource<?, ?>> Class<S> getAdministratorSourceClass(
			String administratorSourceName, Node node) {
		return (Class<S>) CompileUtil.obtainClass(administratorSourceName,
				AdministratorSource.class, this.administratorSourceAliases,
				this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public AdministratorLoader getAdministratorLoader(Node node) {
		return new AdministratorLoaderImpl(node, this);
	}

	@Override
	public AdministratorNode createAdministratorNode(String administratorName,
			String administratorSourceClassName,
			AdministratorSource<?, ?> administratorSource, OfficeNode office) {
		return new AdministratorNodeImpl(administratorName,
				administratorSourceClassName, administratorSource, office, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends GovernanceSource<?, ?>> Class<S> getGovernanceSourceClass(
			String governanceSourceName, Node node) {
		return (Class<S>) CompileUtil.obtainClass(governanceSourceName,
				GovernanceSource.class, this.governanceSourceAliases,
				this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public GovernanceLoader getGovernanceLoader(Node node) {
		return new GovernanceLoaderImpl(node, this);
	}

	@Override
	public GovernanceNode createGovernanceNode(String governanceName,
			String governanceSourceClassName,
			GovernanceSource<?, ?> governanceSource, OfficeNode office) {
		return new GovernanceNodeImpl(governanceName,
				governanceSourceClassName, governanceSource, office, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends TeamSource> Class<S> getTeamSourceClass(
			String teamSourceName, Node node) {
		return (Class<S>) CompileUtil.obtainClass(teamSourceName,
				TeamSource.class, this.teamSourceAliases,
				this.getRootSourceContext(), node, this.getCompilerIssues());
	}

	@Override
	public TeamLoader getTeamLoader(Node node) {
		return new TeamLoaderImpl(node, this);
	}

	@Override
	public TeamNode createTeamNode(String teamName, String teamSourceClassName,
			OfficeFloorNode officeFloor) {
		return new TeamNodeImpl(teamName, teamSourceClassName, officeFloor,
				this);
	}

	@Override
	public TaskFlowNode createTaskFlowNode(String flowName,
			boolean isEscalation, TaskNode task) {
		return new TaskFlowNodeImpl(flowName, isEscalation, task, this);
	}

	@Override
	public TaskObjectNode createTaskObjectNode(String objectName,
			TaskNode taskNode) {
		return new TaskObjectNodeImpl(objectName, taskNode, this);
	}

	@Override
	public TaskTeamNode createTaskTeamNode(String teamName, TaskNode task) {
		return new TaskTeamNodeImpl(teamName, task, this);
	}

	@Override
	public TaskNode createTaskNode(String taskName, String taskTypeName,
			WorkNode work) {
		return new TaskNodeImpl(taskName, taskTypeName, work, this);
	}

	@Override
	public WorkNode createWorkNode(String workName, String workSourceClassName,
			WorkSource<?> workSource, SectionNode section) {
		return new WorkNodeImpl(workName, workSourceClassName, workSource,
				section, this);
	}

}