/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.lang.reflect.Method;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.pool.ManagedObjectPoolLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.work.WorkLoader;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
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
	public void addResources(ResourceSource resourceSource) {
		this.invokeMethod("addResources", new Object[] { resourceSource },
				ResourceSource.class);
	}

	@Override
	public void setCompilerIssues(CompilerIssues issues) {
		this.invokeMethod("setCompilerIssues", new Object[] { issues },
				CompilerIssues.class);
	}

	@Override
	public void setOfficeFrame(OfficeFrame officeFrame) {
		// TODO implement OfficeFloorCompiler.setOfficeFrame
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.setOfficeFrame");
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
		// TODO implement OfficeFloorCompiler.addOfficeSourceAlias
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.addOfficeSourceAlias");
	}

	@Override
	public <S extends SectionSource> void addSectionSourceAlias(String alias,
			Class<S> sectionSourceClass) {
		// TODO implement OfficeFloorCompiler.addSectionSourceAlias
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.addSectionSourceAlias");
	}

	@Override
	public <W extends Work, S extends WorkSource<W>> void addWorkSourceAlias(
			String alias, Class<S> workSourceClass) {
		// TODO implement OfficeFloorCompiler.addWorkSourceAlias
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.addWorkSourceAlias");
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> void addManagedObjectSourceAlias(
			String alias, Class<S> managedObjectSourceClass) {
		// TODO implement OfficeFloorCompiler.addManagedObjectSourceAlias
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.addManagedObjectSourceAlias");
	}

	@Override
	public <I, A extends Enum<A>, S extends AdministratorSource<I, A>> void addAdministratorSourceAlias(
			String alias, Class<S> administratorSourceClass) {
		// TODO implement OfficeFloorCompiler.addAdministratorSourceAlias
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.addAdministratorSourceAlias");
	}

	@Override
	public <S extends TeamSource> void addTeamSourceAlias(String alias,
			Class<S> teamSourceClass) {
		// TODO implement OfficeFloorCompiler.addTeamSourceAlias
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.addTeamSourceAlias");
	}

	@Override
	public PropertyList createPropertyList() {
		return (PropertyList) this.invokeMethod("createPropertyList", null);
	}

	@Override
	public OfficeFloorLoader getOfficeFloorLoader() {
		// TODO implement OfficeFloorCompiler.getOfficeFloorLoader
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.getOfficeFloorLoader");
	}

	@Override
	public OfficeLoader getOfficeLoader() {
		// TODO implement OfficeFloorCompiler.getOfficeLoader
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.getOfficeLoader");
	}

	@Override
	public SectionLoader getSectionLoader() {
		// TODO implement OfficeFloorCompiler.getSectionLoader
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.getSectionLoader");
	}

	@Override
	public WorkLoader getWorkLoader() {
		// TODO implement OfficeFloorCompiler.getWorkLoader
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.getWorkLoader");
	}

	@Override
	public ManagedObjectLoader getManagedObjectLoader() {
		// TODO implement OfficeFloorCompiler.getManagedObjectLoader
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.getManagedObjectLoader");
	}

	@Override
	public ManagedObjectPoolLoader getManagedObjectPoolLoader() {
		// TODO implement OfficeFloorCompiler.getManagedObjectPoolLoader
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.getManagedObjectPoolLoader");
	}

	@Override
	public AdministratorLoader getAdministratorLoader() {
		// TODO implement OfficeFloorCompiler.getAdministratorLoader
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.getAdministratorLoader");
	}

	@Override
	public TeamLoader getTeamLoader() {
		// TODO implement OfficeFloorCompiler.getTeamLoader
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorCompiler.getTeamLoader");
	}

	@Override
	public OfficeFloor compile(String officeFloorLocation) {
		return (OfficeFloor) this.invokeMethod("compile",
				new Object[] { officeFloorLocation }, String.class);
	}

}