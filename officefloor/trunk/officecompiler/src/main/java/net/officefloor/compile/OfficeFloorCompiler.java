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
package net.officefloor.compile;

import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.impl.OfficeFloorCompilerImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.Property;
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
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * <p>
 * Office Floor compiler to compile the {@link OfficeFloor} into the
 * {@link OfficeFrame} to be built.
 * <p>
 * This is the starting point to use the compiler.
 * 
 * @author Daniel
 */
public abstract class OfficeFloorCompiler {

	/**
	 * <p>
	 * {@link System#getProperty(String)} that allows specifying the
	 * {@link OfficeFloorCompiler} implementation {@link Class}.
	 * <p>
	 * Should this not be specified the default {@link OfficeFloorCompilerImpl}
	 * will be used.
	 * <p>
	 * Note: it is anticipated that {@link OfficeFloorCompilerImpl} will always
	 * be used.
	 */
	public static final String IMPLEMENTATION_CLASS_PROPERTY_NAME = "net.officefloor.compiler.implementation";

	/**
	 * <p>
	 * Factory to create the {@link OfficeFloorCompiler}.
	 * <p>
	 * This enables overriding the default behaviour in creating an
	 * {@link OfficeFloorCompiler} instances.
	 */
	public static interface OfficeFloorCompilerFactory {

		/**
		 * Creates the {@link OfficeFloorCompiler}.
		 * 
		 * @return {@link OfficeFloorCompiler}.
		 */
		OfficeFloorCompiler createOfficeFloorCompiler();
	}

	/**
	 * Singleton {@link OfficeFloorCompilerFactory}.
	 */
	private static OfficeFloorCompilerFactory FACTORY = null;

	/**
	 * <p>
	 * Specifies the {@link OfficeFloorCompilerFactory}. Allows for overriding
	 * the default behaviour in creating an {@link OfficeFloorCompiler}.
	 * <p>
	 * Typically this should not be called however is included to enable
	 * flexibility in creating {@link OfficeFloorCompiler} instances.
	 * 
	 * @param factory
	 *            {@link OfficeFloorCompilerFactory}.
	 */
	public synchronized static final void setFactory(
			OfficeFloorCompilerFactory factory) {

		// Ensure not already specified
		if (FACTORY != null) {
			throw new IllegalStateException(OfficeFloorCompilerFactory.class
					.getSimpleName()
					+ " has already been specified");
		}

		// Specify office floor compiler factory
		FACTORY = factory;
	}

	/**
	 * Creates a new instance of a {@link OfficeFloorCompiler}.
	 * 
	 * @return New {@link OfficeFloorCompiler}.
	 */
	public synchronized static final OfficeFloorCompiler newOfficeFloorCompiler() {

		// Determine if use factory
		if (FACTORY != null) {
			// Factory to create the office floor compiler
			return FACTORY.createOfficeFloorCompiler();
		}

		// Obtain the office floor compiler
		OfficeFloorCompiler compiler = null;

		// Determine if overriding the implementation
		String implementationClassName = System
				.getProperty(IMPLEMENTATION_CLASS_PROPERTY_NAME);
		if (!CompileUtil.isBlank(implementationClassName)) {
			// Have override implementation, so use
			try {
				compiler = (OfficeFloorCompiler) Class.forName(
						implementationClassName).newInstance();
			} catch (Throwable ex) {
				throw new IllegalArgumentException(
						"Can not create instance of " + implementationClassName
								+ " from default constructor", ex);
			}
		}

		// Use default implementation if no override
		if (compiler == null) {
			compiler = new OfficeFloorCompilerImpl();
		}

		// Return the office floor compiler
		return compiler;
	}

	/**
	 * {@link ClassLoader} initialised to be the {@link Thread} instance's
	 * {@link ClassLoader}.
	 */
	private ClassLoader classLoader = null;

	/**
	 * Obtains the {@link ClassLoader} for this {@link OfficeFloorCompiler}.
	 * 
	 * @return {@link ClassLoader} for this {@link OfficeFloorCompiler}.
	 */
	public ClassLoader getClassLoader() {

		// Ensure have a class loader
		if (this.classLoader == null) {
			this.classLoader = Thread.currentThread().getContextClassLoader();
		}

		// Return the class loader
		return this.classLoader;
	}

	/**
	 * Overrides the default {@link ClassLoader} to use in compiling the
	 * {@link OfficeFloor}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * <p>
	 * Convenience method to add all the {@link Property} instances from
	 * {@link System#getProperties()}.
	 * <p>
	 * {@link #addProperty(String, String)} will be invoked for each
	 * {@link Property}.
	 */
	public void addSystemProperties() {
		Properties systemProperties = System.getProperties();
		for (String name : systemProperties.stringPropertyNames()) {
			String value = systemProperties.getProperty(name);
			this.addProperty(name, value);
		}
	}

	/**
	 * <p>
	 * Convenience method to add all the {@link Property} instances from
	 * {@link System#getenv()}.
	 * <p>
	 * {@link #addProperty(String, String)} will be invoked for each
	 * {@link Property}.
	 */
	public void addEnvProperties() {
		Map<String, String> envProperties = System.getenv();
		for (String name : envProperties.keySet()) {
			String value = envProperties.get(name);
			this.addProperty(name, value);
		}
	}

	/**
	 * Adds the aliases for the source classes.
	 */
	public void addSourceAliases() {

		// Add the office source aliases from the class path
		for (OfficeSourceService<?> service : ServiceLoader.load(
				OfficeSourceService.class, this.getClassLoader())) {
			this.addOfficeSourceAlias(service.getOfficeSourceAlias(), service
					.getOfficeSourceClass());
		}

		// Add the section source aliases from the class path
		for (SectionSourceService<?> service : ServiceLoader.load(
				SectionSourceService.class, this.getClassLoader())) {
			this.addSectionSourceAlias(service.getSectionSourceAlias(), service
					.getSectionSourceClass());
		}

		// Add the work source aliases from the class path
		for (WorkSourceService<?, ?> service : ServiceLoader.load(
				WorkSourceService.class, this.getClassLoader())) {
			this.addWorkSourceAlias(service.getWorkSourceAlias(), service
					.getWorkSourceClass());
		}

		// Add the managed object source aliases from the class path
		for (ManagedObjectSourceService<?, ?, ?> service : ServiceLoader.load(
				ManagedObjectSourceService.class, this.getClassLoader())) {
			this.addManagedObjectSourceAlias(service
					.getManagedObjectSourceAlias(), service
					.getManagedObjectSourceClass());
		}

		// Add the administrator source aliases from the class path
		for (AdministratorSourceService<?, ?, ?> service : ServiceLoader.load(
				AdministratorSourceService.class, this.getClassLoader())) {
			this.addAdministratorSourceAlias(service
					.getAdministratorSourceAlias(), service
					.getAdministratorSourceClass());
		}

		// Add the team source aliases from the class path
		for (TeamSourceService<?> service : ServiceLoader.load(
				TeamSourceService.class, this.getClassLoader())) {
			this.addTeamSourceAlias(service.getTeamSourceAlias(), service
					.getTeamSourceClass());
		}
	}

	/*
	 * ======= Methods to be implemented by the OfficeFloorCompiler ===========
	 */

	/**
	 * <p>
	 * Overrides the default {@link ConfigurationContext} to use in compiling
	 * the {@link OfficeFloor}.
	 * <p>
	 * Implementations of {@link OfficeFloorCompiler} must provide a default
	 * {@link ConfigurationContext}.
	 * 
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 */
	public abstract void setConfigurationContext(
			ConfigurationContext configurationContext);

	/**
	 * <p>
	 * Overrides the default {@link CompilerIssues} to use in compiling the
	 * {@link OfficeFloor}.
	 * <p>
	 * Implementations of {@link OfficeFloorCompiler} must provide a default
	 * {@link CompilerIssues}. Typically this will be an implementation that
	 * writes issues to {@link System#err}.
	 * 
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public abstract void setCompilerIssues(CompilerIssues issues);

	/**
	 * <p>
	 * Overrides the default {@link OfficeFrame} for building the
	 * {@link OfficeFloor}.
	 * <p>
	 * Should this not be called the {@link OfficeFloorCompiler} implementation
	 * should use the {@link OfficeFrame#getInstance()} to build the
	 * {@link OfficeFloor}.
	 * 
	 * @param officeFrame
	 *            {@link OfficeFrame}.
	 */
	public abstract void setOfficeFrame(OfficeFrame officeFrame);

	/**
	 * <p>
	 * Overrides the default {@link OfficeFloorSource} to source the
	 * {@link OfficeFloor}.
	 * <p>
	 * {@link OfficeFloorCompiler} implementations must provide a default
	 * {@link OfficeFloorSource} implementation. The choice of
	 * {@link OfficeFloorSource} implementation is left to the
	 * {@link OfficeFloorCompiler}, however it should be clearly documented on
	 * the {@link OfficeFloorCompiler} implementation which
	 * {@link OfficeFloorSource} implementation is being used.
	 * 
	 * @param officeFloorSourceClass
	 *            {@link OfficeFloorSource} {@link Class}.
	 */
	public abstract <S extends OfficeFloorSource> void setOfficeFloorSourceClass(
			Class<S> officeFloorSourceClass);

	/**
	 * Adds a {@link Property} that is made available to the
	 * {@link OfficeFloorSource} to source the {@link OfficeFloor}.
	 * 
	 * @param name
	 *            Name of the {@link Property}.
	 * @param value
	 *            Value of the {@link Property}.
	 */
	public abstract void addProperty(String name, String value);

	/**
	 * <p>
	 * Allows providing an alias name for an {@link OfficeSource}.
	 * <p>
	 * This stops the configuration files from being littered with fully
	 * qualified class names of the {@link OfficeSource} classes. This is
	 * anticipated to allow flexibility as the functionality evolves so that
	 * relocating/renaming classes does not require significant configuration
	 * changes.
	 * <p>
	 * Typically this should not be used directly as the
	 * {@link OfficeSourceService} is the preferred means to provide
	 * {@link OfficeSource} aliases.
	 * 
	 * @param alias
	 *            Alias name for the {@link OfficeSource}.
	 * @param officeSourceClass
	 *            {@link OfficeSource} {@link Class} for the alias.
	 */
	public abstract <S extends OfficeSource> void addOfficeSourceAlias(
			String alias, Class<S> officeSourceClass);

	/**
	 * <p>
	 * Allows providing an alias name for a {@link SectionSource}.
	 * <p>
	 * This stops the configuration files from being littered with fully
	 * qualified class names of the {@link SectionSource} classes. This is
	 * anticipated to allow flexibility as the functionality evolves so that
	 * relocating/renaming classes does not require significant configuration
	 * changes.
	 * <p>
	 * Typically this should not be used directly as the
	 * {@link SectionSourceService} is the preferred means to provide
	 * {@link SectionSource} aliases.
	 * 
	 * @param alias
	 *            Alias name for the {@link SectionSource}.
	 * @param sectionSourceClass
	 *            {@link SectionSource} {@link Class} for the alias.
	 */
	public abstract <S extends SectionSource> void addSectionSourceAlias(
			String alias, Class<S> sectionSourceClass);

	/**
	 * <p>
	 * Allows providing an alias name for a {@link WorkSource}.
	 * <p>
	 * This stops the configuration files from being littered with fully
	 * qualified class names of the {@link WorkSource} classes. This is
	 * anticipated to allow flexibility as the functionality evolves so that
	 * relocating/renaming classes does not require significant configuration
	 * changes.
	 * <p>
	 * Typically this should not be used directly as the
	 * {@link WorkSourceService} is the preferred means to provide
	 * {@link WorkSource} aliases.
	 * 
	 * @param alias
	 *            Alias name for the {@link WorkSource}.
	 * @param sectionSourceClass
	 *            {@link WorkSource} {@link Class} for the alias.
	 */
	public abstract <W extends Work, S extends WorkSource<W>> void addWorkSourceAlias(
			String alias, Class<S> workSourceClass);

	/**
	 * <p>
	 * Allows providing an alias name for a {@link ManagedObjectSource}.
	 * <p>
	 * This stops the configuration files from being littered with fully
	 * qualified class names of the {@link ManagedObjectSource} classes. This is
	 * anticipated to allow flexibility as the functionality evolves so that
	 * relocating/renaming classes does not require significant configuration
	 * changes.
	 * <p>
	 * Typically this should not be used directly as the
	 * {@link ManagedObjectSourceService} is the preferred means to provide
	 * {@link ManagedObjectSource} aliases.
	 * 
	 * @param alias
	 *            Alias name for the {@link ManagedObjectSource}.
	 * @param sectionSourceClass
	 *            {@link ManagedObjectSource} {@link Class} for the alias.
	 */
	public abstract <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> void addManagedObjectSourceAlias(
			String alias, Class<S> managedObjectSourceClass);

	/**
	 * <p>
	 * Allows providing an alias name for a {@link AdministratorSource}.
	 * <p>
	 * This stops the configuration files from being littered with fully
	 * qualified class names of the {@link AdministratorSource} classes. This is
	 * anticipated to allow flexibility as the functionality evolves so that
	 * relocating/renaming classes does not require significant configuration
	 * changes.
	 * <p>
	 * Typically this should not be used directly as the
	 * {@link AdministratorSourceService} is the preferred means to provide
	 * {@link AdministratorSource} aliases.
	 * 
	 * @param alias
	 *            Alias name for the {@link AdministratorSource}.
	 * @param sectionSourceClass
	 *            {@link AdministratorSource} {@link Class} for the alias.
	 */
	public abstract <I, A extends Enum<A>, S extends AdministratorSource<I, A>> void addAdministratorSourceAlias(
			String alias, Class<S> administratorSourceClass);

	/**
	 * <p>
	 * Allows providing an alias name for a {@link TeamSource}.
	 * <p>
	 * This stops the configuration files from being littered with fully
	 * qualified class names of the {@link TeamSource} classes. This is
	 * anticipated to allow flexibility as the functionality evolves so that
	 * relocating/renaming classes does not require significant configuration
	 * changes.
	 * <p>
	 * Typically this should not be used directly as the
	 * {@link TeamSourceService} is the preferred means to provide
	 * {@link TeamSource} aliases.
	 * 
	 * @param alias
	 *            Alias name for the {@link TeamSource}.
	 * @param sectionSourceClass
	 *            {@link TeamSource} {@link Class} for the alias.
	 */
	public abstract <S extends TeamSource> void addTeamSourceAlias(
			String alias, Class<S> teamSourceClass);

	/**
	 * Obtains the {@link OfficeFloorLoader}.
	 * 
	 * @return {@link OfficeFloorLoader}.
	 */
	public abstract OfficeFloorLoader getOfficeFloorLoader();

	/**
	 * Obtains the {@link OfficeLoader}.
	 * 
	 * @return {@link OfficeLoader}.
	 */
	public abstract OfficeLoader getOfficeLoader();

	/**
	 * Obtains the {@link SectionLoader}.
	 * 
	 * @return {@link SectionLoader}.
	 */
	public abstract SectionLoader getSectionLoader();

	/**
	 * Obtains the {@link WorkLoader}.
	 * 
	 * @return {@link WorkLoader}.
	 */
	public abstract WorkLoader getWorkLoader();

	/**
	 * Obtains the {@link ManagedObjectLoader}.
	 * 
	 * @return {@link ManagedObjectLoader}.
	 */
	public abstract ManagedObjectLoader getManagedObjectLoader();

	/**
	 * Obtains the {@link AdministratorLoader}.
	 * 
	 * @return {@link AdministratorLoader}.
	 */
	public abstract AdministratorLoader getAdministratorLoader();

	/**
	 * Obtains the {@link TeamLoader}.
	 * 
	 * @return {@link TeamLoader}.
	 */
	public abstract TeamLoader getTeamLoader();

	/**
	 * <p>
	 * Compiles and builds the {@link OfficeFloor}.
	 * <p>
	 * Use this method in preference to {@link OfficeFloorLoader}.
	 * 
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @return {@link OfficeFloor} or <code>null</code> if issues in compiling
	 *         which are reported to the {@link CompilerIssues}.
	 */
	public abstract OfficeFloor compile(String officeFloorLocation);

}