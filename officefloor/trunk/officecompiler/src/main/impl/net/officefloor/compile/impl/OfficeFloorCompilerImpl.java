/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.officefloor.OfficeFloorLoaderImpl;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * <p>
 * {@link OfficeFloorCompiler} implementation.
 * <p>
 * The default {@link OfficeFloorSource} is
 * {@link OfficeFloorModelOfficeFloorSource}.
 * 
 * @author Daniel
 */
public class OfficeFloorCompilerImpl extends OfficeFloorCompiler {

	/**
	 * {@link ClassLoader}.
	 */
	private ClassLoader classLoader = null;

	/**
	 * {@link ConfigurationContext}.
	 */
	private ConfigurationContext configurationContext = null;

	/**
	 * {@link OfficeFrame}.
	 */
	private OfficeFrame officeFrame = null;

	/**
	 * {@link OfficeFloorSource} {@link Class}.
	 */
	private Class<? extends OfficeFloorSource> officeFloorSourceClass = null;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList properties = new PropertyListImpl();

	/**
	 * {@link OfficeSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<? extends OfficeSource>> officeSourceAliases = new HashMap<String, Class<? extends OfficeSource>>();

	/**
	 * {@link SectionSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<? extends SectionSource>> sectionSourceAliases = new HashMap<String, Class<? extends SectionSource>>();

	/**
	 * {@link WorkSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<? extends WorkSource<?>>> workSourceAliases = new HashMap<String, Class<? extends WorkSource<?>>>();

	/**
	 * {@link ManagedObjectSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<? extends ManagedObjectSource<?, ?>>> managedObjectSourceAliases = new HashMap<String, Class<? extends ManagedObjectSource<?, ?>>>();

	/**
	 * {@link AdministratorSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<? extends AdministratorSource<?, ?>>> administratorSourceAliases = new HashMap<String, Class<? extends AdministratorSource<?, ?>>>();

	/**
	 * {@link TeamSource} {@link Class} instances by their alias name.
	 */
	private final Map<String, Class<? extends TeamSource>> teamSourceAliases = new HashMap<String, Class<? extends TeamSource>>();

	/*
	 * ==================== OfficeFloorCompiler ==============================
	 */

	@Override
	public void setConfigurationContext(
			ConfigurationContext configurationContext) {
		this.configurationContext = configurationContext;
	}

	@Override
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
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
	public <I, A extends Enum<A>, S extends AdministratorSource<I, A>> void addAdministratorSourceAlias(
			String alias, Class<S> administratorSourceClass) {
		this.registerAlias(alias, administratorSourceClass,
				this.administratorSourceAliases, "administrator");
	}

	@Override
	public <S extends TeamSource> void addTeamSourceAlias(String alias,
			Class<S> teamSourceClass) {
		this.registerAlias(alias, teamSourceClass, this.teamSourceAliases,
				"team");
	}

	@Override
	public OfficeFloor compile(String officeFloorLocation, CompilerIssues issues) {

		// Obtain the class loader
		ClassLoader classLoader = (this.classLoader != null ? this.classLoader
				: OfficeFloorCompiler.class.getClassLoader());

		// Obtain the configuration context
		ConfigurationContext configurationContext = (this.configurationContext != null ? this.configurationContext
				: new ClassLoaderConfigurationContext(classLoader));

		// Obtain the office floor source
		Class<? extends OfficeFloorSource> officeFloorSourceClass = (this.officeFloorSourceClass != null ? this.officeFloorSourceClass
				: OfficeFloorModelOfficeFloorSource.class);

		// Obtain the office frame
		OfficeFrame officeFrame = (this.officeFrame != null ? this.officeFrame
				: OfficeFrame.getInstance());

		// Create the office floor loader
		OfficeFloorLoader officeFloorLoader = new OfficeFloorLoaderImpl();

		// Compile, build and return the office floor
		return officeFloorLoader.loadOfficeFloor(officeFloorSourceClass,
				officeFloorLocation, this.properties, configurationContext,
				classLoader, issues, officeFrame);
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
		if (aliasMap.containsKey(alias)) {
			// Provide warning and ignore duplicates
			System.err.println("WARNING: duplicate " + aliasType + " alias "
					+ alias);
			return; // do not register the duplicate
		}

		// Register the alias
		aliasMap.put(alias, aliasSourceClass);
	}

}