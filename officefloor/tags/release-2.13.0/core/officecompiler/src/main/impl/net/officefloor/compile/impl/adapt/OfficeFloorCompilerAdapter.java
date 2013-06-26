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
package net.officefloor.compile.impl.adapt;

import java.lang.reflect.Method;

import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.supplier.SupplierLoader;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerRunnable;
import net.officefloor.compile.TypeLoader;
import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
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
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * <p>
 * Adapts the implementing {@link OfficeFloorCompiler} to be used when the
 * {@link OfficeFloorCompiler} reference may be from a different
 * {@link ClassLoader}. Typically this occurs within the Eclipse plug-ins but is
 * useful if wanting to run a different versions of the
 * {@link OfficeFloorCompiler} in the same JVM.
 * <p>
 * This adapter reflectively calls on the implementation to achieve
 * compatibility. Also, as this is loaded in the same {@link ClassLoader} as the
 * {@link OfficeFloorCompiler} it will always be assignable.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCompilerAdapter extends OfficeFloorCompiler {

	/**
	 * {@link OfficeFloorCompiler} implementation.
	 */
	private final Object implementation;

	/**
	 * {@link ClassLoader} for the client.
	 */
	private final ClassLoader clientClassLoader;

	/**
	 * {@link ClassLoader} for the implementation.
	 */
	private final ClassLoader implClassLoader;

	/**
	 * Initiate.
	 * 
	 * @param implementation
	 *            {@link OfficeFloorCompiler} implementation.
	 * @param clientClassLoader
	 *            {@link ClassLoader} of the client.
	 * @param implClassLoader
	 *            {@link ClassLoader} of the implementation.
	 */
	public OfficeFloorCompilerAdapter(Object implementation,
			ClassLoader clientClassLoader, ClassLoader implClassLoader) {
		this.implementation = implementation;
		this.clientClassLoader = clientClassLoader;
		this.implClassLoader = implClassLoader;
	}

	/**
	 * Invokes the method.
	 * 
	 * @param methodName
	 *            Name of the {@link Method}.
	 * @param arguments
	 *            Arguments for the {@link Method}.
	 * @param paramTypes
	 *            Parameter types.
	 * @return Return on the value.
	 */
	private Object invokeMethod(String methodName, Object[] arguments,
			Class<?>... paramTypes) {
		return TypeAdapter.invokeNoExceptionMethod(this.implementation,
				methodName, (arguments == null ? new Object[0] : arguments),
				paramTypes, this.clientClassLoader, this.implClassLoader);
	}

	/*
	 * ======================== OfficeFloorCompiler ========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public <T> T run(
			Class<? extends OfficeFloorCompilerRunnable<T>> runnableClass,
			Object... parameters) throws Exception {
		return (T) this.invokeMethod("run", new Object[] { runnableClass,
				parameters }, Class.class, Object[].class);
	}

	@Override
	public void addResources(ResourceSource resourceSource) {
		this.invokeMethod("addResources", new Object[] { resourceSource },
				ResourceSource.class);
	}

	@Override
	public void setEscalationHandler(EscalationHandler escalationHandler) {
		this.invokeMethod("setEscalationHandler",
				new Object[] { escalationHandler }, EscalationHandler.class);
	}

	@Override
	public void setCompilerIssues(CompilerIssues issues) {
		this.invokeMethod("setCompilerIssues", new Object[] { issues },
				CompilerIssues.class);
	}

	@Override
	public void setOfficeFrame(OfficeFrame officeFrame) {
		throw new IllegalStateException("Can not specify "
				+ OfficeFrame.class.getSimpleName() + " when "
				+ OfficeFloorCompiler.class.getSimpleName()
				+ " is being adapted due to running within another "
				+ ClassLoader.class.getSimpleName());
	}

	@Override
	public <S extends OfficeFloorSource> void setOfficeFloorSourceClass(
			Class<S> officeFloorSourceClass) {
		this.invokeMethod("setOfficeFloorSourceClass",
				new Object[] { officeFloorSourceClass }, Class.class);
	}

	@Override
	public void setOfficeFloorSource(OfficeFloorSource officeFloorSource) {
		this.invokeMethod("setOfficeFloorSource",
				new Object[] { officeFloorSource }, OfficeFloorSource.class);
	}

	@Override
	public void addProperty(String name, String value) {
		this.invokeMethod("addProperty", new Object[] { name, value },
				String.class, String.class);
	}

	@Override
	public <S extends OfficeSource> void addOfficeSourceAlias(String alias,
			Class<S> officeSourceClass) {
		this.invokeMethod("addOfficeSourceAlias", new Object[] { alias,
				officeSourceClass }, String.class, Class.class);
	}

	@Override
	public <S extends SectionSource> void addSectionSourceAlias(String alias,
			Class<S> sectionSourceClass) {
		this.invokeMethod("addSectionSourceAlias", new Object[] { alias,
				sectionSourceClass }, String.class, Class.class);
	}

	@Override
	public <W extends Work, S extends WorkSource<W>> void addWorkSourceAlias(
			String alias, Class<S> workSourceClass) {
		this.invokeMethod("addWorkSourceAlias", new Object[] { alias,
				workSourceClass }, String.class, Class.class);
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> void addManagedObjectSourceAlias(
			String alias, Class<S> managedObjectSourceClass) {
		this.invokeMethod("addManagedObjectSourceAlias", new Object[] { alias,
				managedObjectSourceClass }, String.class, Class.class);
	}

	@Override
	public <S extends SupplierSource> void addSupplierSourceAlias(String alias,
			Class<S> supplierSourceClass) {
		this.invokeMethod("addSupplierSourceAlias", new Object[] { alias,
				supplierSourceClass }, String.class, Class.class);
	}

	@Override
	public <I, A extends Enum<A>, S extends AdministratorSource<I, A>> void addAdministratorSourceAlias(
			String alias, Class<S> administratorSourceClass) {
		this.invokeMethod("addAdministratorSourceAlias", new Object[] { alias,
				administratorSourceClass }, String.class, Class.class);
	}

	@Override
	public <I, F extends Enum<F>, S extends GovernanceSource<I, F>> void addGovernanceSourceAlias(
			String alias, Class<S> governanceSourceClass) {
		this.invokeMethod("addGovernanceSourceAlias", new Object[] { alias,
				governanceSourceClass }, String.class, Class.class);
	}

	@Override
	public <S extends TeamSource> void addTeamSourceAlias(String alias,
			Class<S> teamSourceClass) {
		this.invokeMethod("addTeamSourceAlias", new Object[] { alias,
				teamSourceClass }, String.class, Class.class);
	}

	@Override
	public void addProfiler(String officeName, Profiler profiler) {
		this.invokeMethod("addProfiler", new Object[] { officeName, profiler },
				String.class, Profiler.class);
	}

	@Override
	public PropertyList createPropertyList() {
		return (PropertyList) this.invokeMethod("createPropertyList", null);
	}

	@Override
	public TypeLoader getTypeLoader() {
		return (TypeLoader) this.invokeMethod("getTypeLoader", null);
	}

	@Override
	public OfficeFloorLoader getOfficeFloorLoader() {
		return (OfficeFloorLoader) this.invokeMethod("getOfficeFloorLoader",
				null);
	}

	@Override
	public OfficeLoader getOfficeLoader() {
		return (OfficeLoader) this.invokeMethod("getOfficeLoader", null);
	}

	@Override
	public SectionLoader getSectionLoader() {
		return (SectionLoader) this.invokeMethod("getSectionLoader", null);
	}

	@Override
	public WorkLoader getWorkLoader() {
		return (WorkLoader) this.invokeMethod("getWorkLoader", null);
	}

	@Override
	public ManagedObjectLoader getManagedObjectLoader() {
		return (ManagedObjectLoader) this.invokeMethod(
				"getManagedObjectLoader", null);
	}

	@Override
	public SupplierLoader getSupplierLoader() {
		return (SupplierLoader) this.invokeMethod("getSupplierLoader", null);
	}

	@Override
	public GovernanceLoader getGovernanceLoader() {
		return (GovernanceLoader) this
				.invokeMethod("getGovernanceLoader", null);
	}

	@Override
	public ManagedObjectPoolLoader getManagedObjectPoolLoader() {
		return (ManagedObjectPoolLoader) this.invokeMethod(
				"getManagedObjectPoolLoader", null);
	}

	@Override
	public AdministratorLoader getAdministratorLoader() {
		return (AdministratorLoader) this.invokeMethod(
				"getAdministratorLoader", null);
	}

	@Override
	public TeamLoader getTeamLoader() {
		return (TeamLoader) this.invokeMethod("getTeamLoader", null);
	}

	@Override
	public OfficeFloor compile(String officeFloorLocation) {
		return (OfficeFloor) this.invokeMethod("compile",
				new Object[] { officeFloorLocation }, String.class);
	}

}