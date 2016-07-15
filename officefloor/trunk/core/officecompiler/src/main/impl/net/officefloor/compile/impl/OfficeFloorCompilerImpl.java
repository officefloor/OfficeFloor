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
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.impl.team.TeamLoaderImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.work.WorkLoaderImpl;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
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
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
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
			this.getCompilerIssues().addIssue(LocationType.OFFICE_FLOOR, null,
					null, null, "Duplicate " + aliasType + " alias " + alias);
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
		return new OfficeFloorLoaderImpl(this, this.profilers);
	}

	@Override
	public OfficeLoader getOfficeLoader() {
		return new OfficeLoaderImpl(this);
	}

	@Override
	public SectionLoader getSectionLoader() {
		return new SectionLoaderImpl(this);
	}

	@Override
	public WorkLoader getWorkLoader() {
		return new WorkLoaderImpl(this);
	}

	@Override
	public ManagedObjectLoader getManagedObjectLoader() {
		return new ManagedObjectLoaderImpl(this);
	}

	@Override
	public SupplierLoader getSupplierLoader() {
		return new SupplierLoaderImpl(this);
	}

	@Override
	public GovernanceLoader getGovernanceLoader() {
		return new GovernanceLoaderImpl(this);
	}

	@Override
	public ManagedObjectPoolLoader getManagedObjectPoolLoader() {
		return new ManagedObjectPoolLoaderImpl(this);
	}

	@Override
	public AdministratorLoader getAdministratorLoader() {
		return new AdministratorLoaderImpl(this);
	}

	@Override
	public TeamLoader getTeamLoader() {
		return new TeamLoaderImpl(this);
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
				null, null);
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
				managedObjectSourceClassName, null, null, null);
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
	public SourceContext getSourceContext() {
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
	@SuppressWarnings("unchecked")
	public <S extends OfficeSource> Class<S> getOfficeSourceClass(
			String officeSourceName, String officeLocation, String officeName) {
		return (Class<S>) CompileUtil.obtainClass(officeSourceName,
				OfficeSource.class, this.officeSourceAliases,
				this.getSourceContext(), LocationType.OFFICE, officeLocation,
				AssetType.OFFICE, officeName, this.getCompilerIssues());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends SectionSource> Class<S> getSectionSourceClass(
			String sectionSourceName, String sectionLocation, String sectionName) {
		return (Class<S>) CompileUtil.obtainClass(sectionSourceName,
				SectionSource.class, this.sectionSourceAliases,
				this.getSourceContext(), LocationType.SECTION, sectionLocation,
				null, null, this.getCompilerIssues());
	}

	@Override
	public SectionNode createSectionNode(String sectionName, OfficeNode office) {
		return new SectionNodeImpl(sectionName, office, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends WorkSource<?>> Class<S> getWorkSourceClass(
			String workSourceName, String sectionLocation, String workName) {
		return (Class<S>) CompileUtil.obtainClass(workSourceName,
				WorkSource.class, this.workSourceAliases,
				this.getSourceContext(), LocationType.SECTION, sectionLocation,
				AssetType.WORK, workName, this.getCompilerIssues());
	}

	@Override
	public WorkLoader getWorkLoader(String sectionLocation, String workName) {
		return new WorkLoaderImpl(sectionLocation, workName, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends ManagedObjectSource<?, ?>> Class<S> getManagedObjectSourceClass(
			String managedObjectSourceName, LocationType locationType,
			String location, String managedObjectName) {
		return (Class<S>) CompileUtil.obtainClass(managedObjectSourceName,
				ManagedObjectSource.class, this.managedObjectSourceAliases,
				this.getSourceContext(), locationType, location,
				AssetType.MANAGED_OBJECT, managedObjectName,
				this.getCompilerIssues());
	}

	@Override
	public ManagedObjectLoader getManagedObjectLoader(
			LocationType locationType, String location, String managedObjectName) {
		return new ManagedObjectLoaderImpl(locationType, location,
				managedObjectName, this);
	}

	@Override
	public ManagedObjectPoolLoader getManagedObjectPoolLoader(
			LocationType locationType, String location,
			String managedObjectPoolName) {
		return new ManagedObjectPoolLoaderImpl(locationType, location,
				managedObjectPoolName, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends SupplierSource> Class<S> getSupplierSourceClass(
			String supplierSourceClassName, String officeFloorLocation,
			String supplierName) {
		return (Class<S>) CompileUtil.obtainClass(supplierSourceClassName,
				SupplierSource.class, this.supplierSourceAliases,
				this.getSourceContext(), LocationType.OFFICE_FLOOR,
				officeFloorLocation, null, supplierName,
				this.getCompilerIssues());
	}

	@Override
	public SupplierLoader getSupplierLoader(String officeFloorLocation,
			String supplierName) {
		return new SupplierLoaderImpl(officeFloorLocation, supplierName, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends AdministratorSource<?, ?>> Class<S> getAdministratorSourceClass(
			String administratorSourceName, String officeLocation,
			String administratorName) {
		return (Class<S>) CompileUtil.obtainClass(administratorSourceName,
				AdministratorSource.class, this.administratorSourceAliases,
				this.getSourceContext(), LocationType.OFFICE, officeLocation,
				AssetType.ADMINISTRATOR, administratorName,
				this.getCompilerIssues());
	}

	@Override
	public AdministratorLoader getAdministratorLoader(String officeLocation,
			String administratorName) {
		return new AdministratorLoaderImpl(officeLocation, administratorName,
				this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends GovernanceSource<?, ?>> Class<S> getGovernanceSourceClass(
			String governanceSourceName, String officeLocation,
			String governanceName) {
		return (Class<S>) CompileUtil.obtainClass(governanceSourceName,
				GovernanceSource.class, this.governanceSourceAliases,
				this.getSourceContext(), LocationType.OFFICE, officeLocation,
				AssetType.GOVERNANCE, governanceName, this.getCompilerIssues());
	}

	@Override
	public GovernanceLoader getGovernanceLoader(String officeLocation,
			String governanceName) {
		return new GovernanceLoaderImpl(officeLocation, governanceName, this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends TeamSource> Class<S> getTeamSourceClass(
			String teamSourceName, String officeFloorLocation, String teamName) {
		return (Class<S>) CompileUtil.obtainClass(teamSourceName,
				TeamSource.class, this.teamSourceAliases,
				this.getSourceContext(), LocationType.OFFICE_FLOOR,
				officeFloorLocation, AssetType.TEAM, teamName,
				this.getCompilerIssues());
	}

	@Override
	public TeamLoader getTeamLoader(LocationType locationType, String location,
			String teamName) {
		return new TeamLoaderImpl(location, teamName, this);
	}

}