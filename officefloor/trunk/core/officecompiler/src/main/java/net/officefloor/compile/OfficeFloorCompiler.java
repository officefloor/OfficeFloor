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
package net.officefloor.compile;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.supplier.SupplierLoader;
import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.impl.OfficeFloorCompilerImpl;
import net.officefloor.compile.impl.adapt.OfficeFloorCompilerAdapter;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.pool.ManagedObjectPoolLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyConfigurable;
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
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * <p>
 * Office Floor compiler to compile the {@link OfficeFloor} into the
 * {@link OfficeFrame} to be built.
 * <p>
 * This is the starting point to use the compiler.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class OfficeFloorCompiler implements PropertyConfigurable {

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
		 * @param classLoader
		 *            {@link ClassLoader}.
		 * @return {@link OfficeFloorCompiler}.
		 */
		OfficeFloorCompiler createOfficeFloorCompiler(ClassLoader classLoader);
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
			throw new IllegalStateException(
					OfficeFloorCompilerFactory.class.getSimpleName()
							+ " has already been specified");
		}

		// Specify office floor compiler factory
		FACTORY = factory;
	}

	/**
	 * Creates a new instance of a {@link OfficeFloorCompiler}.
	 * 
	 * @param implClassLoader
	 *            {@link ClassLoader} to use for the {@link OfficeFloor}. May be
	 *            <code>null</code> which will default to use the current
	 *            {@link Thread#getContextClassLoader()}.
	 * @return New {@link OfficeFloorCompiler}.
	 */
	public synchronized static final OfficeFloorCompiler newOfficeFloorCompiler(
			ClassLoader implClassLoader) {

		// Ensure have implementation class loader
		if (implClassLoader == null) {
			implClassLoader = Thread.currentThread().getContextClassLoader();
		}

		// Obtain the client class loader
		ClassLoader clientClassLoader = OfficeFloorCompiler.class
				.getClassLoader();

		// Determine if use factory
		Object implementation = null;
		if (FACTORY != null) {
			// Factory to create the office floor compiler
			implementation = FACTORY.createOfficeFloorCompiler(implClassLoader);

		} else {
			// Determine if overriding the implementation
			String implementationClassName = System
					.getProperty(IMPLEMENTATION_CLASS_PROPERTY_NAME);
			if (CompileUtil.isBlank(implementationClassName)) {
				// Use default implementation as no override
				implementationClassName = OfficeFloorCompilerImpl.class
						.getName();
			}

			// Load implementation
			try {
				implementation = implClassLoader.loadClass(
						implementationClassName).newInstance();
			} catch (Throwable ex) {
				throw new IllegalArgumentException(
						"Can not create instance of " + implementationClassName
								+ " from default constructor", ex);
			}
		}

		// Obtain the office floor compiler
		OfficeFloorCompiler compiler = null;
		if (implementation instanceof OfficeFloorCompiler) {
			// Compatible by type (as likely same class loader)
			compiler = (OfficeFloorCompiler) implementation;

		} else {
			// Not compatible so must wrap to enable compatibility
			compiler = new OfficeFloorCompilerAdapter(implementation,
					clientClassLoader, implClassLoader);

			// Specify class loader on the implementation
			try {
				// Find the classLoader field on the OfficeFloor Compiler
				Field classLoaderField = null;
				Class<?> implementationClass = implementation.getClass();
				do {
					// Determine if the OfficeFloor Compiler
					if (OfficeFloorCompiler.class.getName().equals(
							implementationClass.getName())) {
						// Find the classLoader field on the compiler
						for (Field field : implementationClass
								.getDeclaredFields()) {
							if ("classLoader".equals(field.getName())) {
								classLoaderField = field; // found
							}
						}
					}
					implementationClass = implementationClass.getSuperclass();
				} while (implementationClass != null);

				// Specify the class loader
				classLoaderField.setAccessible(true);
				classLoaderField.set(implementation, implClassLoader);

			} catch (Exception ex) {
				throw new IllegalStateException(
						"Unable to specify class loader on "
								+ OfficeFloorCompiler.class.getSimpleName()
								+ " implementation", ex);
			}
		}

		// Specify the class loader on the compiler
		compiler.classLoader = clientClassLoader;

		// Return the office floor compiler implementation
		return compiler;
	}

	/**
	 * Convenience method to create a new {@link PropertyList}.
	 * 
	 * @return New {@link PropertyList}.
	 */
	public static final PropertyList newPropertyList() {

		// Create the office floor compiler
		OfficeFloorCompiler compiler = newOfficeFloorCompiler(Thread
				.currentThread().getContextClassLoader());

		// Use the compiler to return a new property list
		return compiler.createPropertyList();
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
	public final ClassLoader getClassLoader() {

		// Ensure have a class loader
		if (this.classLoader == null) {
			this.classLoader = Thread.currentThread().getContextClassLoader();
		}

		// Return the class loader
		return this.classLoader;
	}

	/**
	 * <p>
	 * Executes the {@link OfficeFloorCompilerRunnable} by instantiating it with
	 * the same {@link ClassLoader} being used by this
	 * {@link OfficeFloorCompiler}.
	 * <p>
	 * This is typically used by graphical editors that need to use the project
	 * class path rather than the editor's class path.
	 * 
	 * @param <T>
	 *            Return type.
	 * @param runnableClass
	 *            {@link OfficeFloorCompilerRunnable} class.
	 * @param parameters
	 *            Parameters to enable configuration of the
	 *            {@link OfficeFloorCompilerRunnable}. As {@link Proxy}
	 *            instances are used to bridge {@link Class} compatibility
	 *            issues due to different {@link ClassLoader}, all parameters
	 *            should only be access via their implementing interfaces.
	 * @return Value returned from the {@link OfficeFloorCompilerRunnable}.
	 * @throws Exception
	 *             If fails to run the {@link OfficeFloorCompilerRunnable}.
	 */
	public <T> T run(
			Class<? extends OfficeFloorCompilerRunnable<T>> runnableClass,
			Object... parameters) throws Exception {

		// Run the runnable
		T result = runnableClass.newInstance().run(this, parameters);

		// Return the result
		return result;
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
			this.addOfficeSourceAlias(service.getOfficeSourceAlias(),
					service.getOfficeSourceClass());
		}

		// Add the section source aliases from the class path
		for (SectionSourceService<?> service : ServiceLoader.load(
				SectionSourceService.class, this.getClassLoader())) {
			this.addSectionSourceAlias(service.getSectionSourceAlias(),
					service.getSectionSourceClass());
		}

		// Add the work source aliases from the class path
		for (WorkSourceService<?, ?> service : ServiceLoader.load(
				WorkSourceService.class, this.getClassLoader())) {
			this.addWorkSourceAlias(service.getWorkSourceAlias(),
					service.getWorkSourceClass());
		}

		// Add the managed object source aliases from the class path
		for (ManagedObjectSourceService<?, ?, ?> service : ServiceLoader.load(
				ManagedObjectSourceService.class, this.getClassLoader())) {
			this.addManagedObjectSourceAlias(
					service.getManagedObjectSourceAlias(),
					service.getManagedObjectSourceClass());
		}

		// Add the supplier source aliases from the class path
		for (SupplierSourceService<?> service : ServiceLoader.load(
				SupplierSourceService.class, this.getClassLoader())) {
			this.addSupplierSourceAlias(service.getSupplierSourceAlias(),
					service.getSupplierSourceClass());
		}

		// Add the administrator source aliases from the class path
		for (AdministratorSourceService<?, ?, ?> service : ServiceLoader.load(
				AdministratorSourceService.class, this.getClassLoader())) {
			this.addAdministratorSourceAlias(
					service.getAdministratorSourceAlias(),
					service.getAdministratorSourceClass());
		}

		// Add the governance source alias from the class path
		for (GovernanceSourceService<?, ?, ?> service : ServiceLoader.load(
				GovernanceSourceService.class, this.getClassLoader())) {
			this.addGovernanceSourceAlias(service.getGovernanceSourceAlias(),
					service.getGovernanceSourceClass());
		}

		// Add the team source aliases from the class path
		for (TeamSourceService<?> service : ServiceLoader.load(
				TeamSourceService.class, this.getClassLoader())) {
			this.addTeamSourceAlias(service.getTeamSourceAlias(),
					service.getTeamSourceClass());
		}
	}

	/*
	 * ======= Methods to be implemented by the OfficeFloorCompiler ===========
	 */

	/**
	 * <p>
	 * Adds a {@link ResourceSource}.
	 * <p>
	 * This will be added to the {@link OfficeFrame} before compiling the
	 * {@link OfficeFloor} and will be available in the {@link SourceContext}
	 * for loading the various sources.
	 * 
	 * @param resourceSource
	 *            {@link ResourceSource}.
	 */
	public abstract void addResources(ResourceSource resourceSource);

	/**
	 * <p>
	 * Overrides the default {@link EscalationHandler}.
	 * <p>
	 * This will be specified on the {@link OfficeFrame} before compiling the
	 * {@link OfficeFloor}.
	 * 
	 * @param escalationHandler
	 *            {@link EscalationHandler}.
	 */
	public abstract void setEscalationHandler(
			EscalationHandler escalationHandler);

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
	 * @param <S>
	 *            {@link OfficeFloorSource} type.
	 * @param officeFloorSourceClass
	 *            {@link OfficeFloorSource} {@link Class}.
	 */
	public abstract <S extends OfficeFloorSource> void setOfficeFloorSourceClass(
			Class<S> officeFloorSourceClass);

	/**
	 * <p>
	 * Specifies the {@link OfficeFloorSource} instance to use for compiling the
	 * {@link OfficeFloor}.
	 * <p>
	 * This will take precedence over specifying the {@link OfficeFloorSource}
	 * class.
	 * 
	 * @param officeFloorSource
	 *            {@link OfficeFloorSource}.
	 */
	public abstract void setOfficeFloorSource(
			OfficeFloorSource officeFloorSource);

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
	 * @param <S>
	 *            {@link OfficeSource} type.
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
	 * @param <S>
	 *            {@link SectionSource} type.
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
	 * @param <W>
	 *            {@link Work} type.
	 * @param <S>
	 *            {@link WorkSource} type.
	 * @param alias
	 *            Alias name for the {@link WorkSource}.
	 * @param workSourceClass
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
	 * @param <D>
	 *            Dependency type keys.
	 * @param <F>
	 *            {@link JobSequence} type keys.
	 * @param <S>
	 *            {@link ManagedObjectSource} type.
	 * @param alias
	 *            Alias name for the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} {@link Class} for the alias.
	 */
	public abstract <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> void addManagedObjectSourceAlias(
			String alias, Class<S> managedObjectSourceClass);

	/**
	 * <p>
	 * Allows providing an alias name for a {@link SupplierSource}.
	 * <p>
	 * This stops the configuration files from being littered with fully
	 * qualified class names of the {@link SupplierSource} classes. This is
	 * anticipated to allow flexibility as the functionality evolves so that
	 * relocating/renaming classes does not require significant configuration
	 * changes.
	 * <p>
	 * Typically this should not be used directly as the
	 * {@link SupplierSourceService} is the preferred means to provide
	 * {@link SupplierSource} aliases.
	 * 
	 * @param <S>
	 *            {@link SupplierSource} type.
	 * @param alias
	 *            Alias name for the {@link SupplierSource}.
	 * @param supplierSourceClass
	 *            {@link SupplierSource} {@link Class} for the alias.
	 */
	public abstract <S extends SupplierSource> void addSupplierSourceAlias(
			String alias, Class<S> supplierSourceClass);

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
	 * @param <I>
	 *            Extension interface type.
	 * @param <A>
	 *            {@link Administrator} type keys.
	 * @param <S>
	 *            {@link AdministratorSource} type.
	 * @param alias
	 *            Alias name for the {@link AdministratorSource}.
	 * @param administratorSourceClass
	 *            {@link AdministratorSource} {@link Class} for the alias.
	 */
	public abstract <I, A extends Enum<A>, S extends AdministratorSource<I, A>> void addAdministratorSourceAlias(
			String alias, Class<S> administratorSourceClass);

	/**
	 * <p>
	 * Allows providing an alias name for a {@link GovernanceSource}.
	 * <p>
	 * This stops the configuration files from being littered with fully
	 * qualified class names of the {@link GovernanceSource} classes. This is
	 * anticipated to allow flexibility as the functionality evolves so that
	 * relocating/renaming classes does not require significant configuration
	 * changes.
	 * <p>
	 * Typically this should not be used directly as the
	 * {@link GovernanceSourceService} is the preferred means to provide
	 * {@link GovernanceSource} aliases.
	 * 
	 * @param <I>
	 *            Extension interface type.
	 * @param <F>
	 *            {@link JobSequence} type keys.
	 * @param <S>
	 *            {@link GovernanceSource} type.
	 * @param alias
	 *            Alias name for the {@link GovernanceSource}.
	 * @param governanceSourceClass
	 *            {@link GovernanceSource} {@link Class} for the alias.
	 */
	public abstract <I, F extends Enum<F>, S extends GovernanceSource<I, F>> void addGovernanceSourceAlias(
			String alias, Class<S> governanceSourceClass);

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
	 * @param <S>
	 *            {@link TeamSource} type.
	 * @param alias
	 *            Alias name for the {@link TeamSource}.
	 * @param teamSourceClass
	 *            {@link TeamSource} {@link Class} for the alias.
	 */
	public abstract <S extends TeamSource> void addTeamSourceAlias(
			String alias, Class<S> teamSourceClass);

	/**
	 * Adds the {@link Profiler} for the {@link Office}.
	 * 
	 * @param officeName
	 *            Name of {@link Office} to be profiled.
	 * @param profiler
	 *            {@link Profiler} for the {@link Office}.
	 */
	public abstract void addProfiler(String officeName, Profiler profiler);

	/**
	 * Creates a new empty {@link PropertyList}.
	 * 
	 * @return New empty {@link PropertyList}.
	 */
	public abstract PropertyList createPropertyList();

	/**
	 * Obtains the {@link TypeLoader}.
	 * 
	 * @return {@link TypeLoader}.
	 */
	public abstract TypeLoader getTypeLoader();

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
	 * Obtains the {@link SupplierLoader}.
	 * 
	 * @return {@link SupplierLoader}.
	 */
	public abstract SupplierLoader getSupplierLoader();

	/**
	 * Obtains the {@link GovernanceLoader}.
	 * 
	 * @return {@link GovernanceLoader}.
	 */
	public abstract GovernanceLoader getGovernanceLoader();

	/**
	 * Obtains the {@link ManagedObjectPoolLoader}.
	 * 
	 * @return {@link ManagedObjectPoolLoader}.
	 */
	public abstract ManagedObjectPoolLoader getManagedObjectPoolLoader();

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