package net.officefloor.servlet.supply;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.ServletServicer;
import net.officefloor.servlet.inject.InjectionRegistry;
import net.officefloor.servlet.tomcat.TomcatServletManager;

/**
 * {@link ManagedObjectSource} to provide {@link ServletManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletServicerManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject {

	/**
	 * {@link TomcatServletManager}.
	 */
	private final TomcatServletManager servletManager;

	/**
	 * {@link InjectionRegistry}.
	 */
	private final InjectionRegistry injectionRegistry;

	/**
	 * {@link Logger}.
	 */
	private Logger logger;

	/**
	 * Instantiate.
	 * 
	 * @param servletManager    {@link TomcatServletManager}.
	 * @param injectionRegistry {@link InjectionRegistry}.
	 */
	public ServletServicerManagedObjectSource(TomcatServletManager servletManager,
			InjectionRegistry injectionRegistry) {
		this.servletManager = servletManager;
		this.injectionRegistry = injectionRegistry;
	}

	/*
	 * ================== ManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {

		// Specify meta-data
		context.setObjectClass(ServletServicer.class);

		// Must depend on all injections for thread locals to be available
		Set<Class<?>> unqalifiedExists = new HashSet<>();
		DoubleKeyMap<String, Class<?>, Boolean> qualifiedExists = new DoubleKeyMap<>();
		this.injectionRegistry.forEachDependency((qualifier, type) -> {

			// Determine if already added dependency
			if (qualifier != null) {
				if (qualifiedExists.get(qualifier, type) != null) {
					return; // already added
				}
				qualifiedExists.put(qualifier, type, true);
			} else {
				if (unqalifiedExists.contains(type)) {
					return; // already added
				}
				unqalifiedExists.add(type);
			}

			// Add the dependency
			context.addDependency(type).setTypeQualifier(qualifier);
		});
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context) throws Exception {

		// Capture logger for possible stop failure
		this.logger = context.getLogger();
	}

	@Override
	public void stop() {
		try {
			this.servletManager.stop();
		} catch (Exception ex) {
			this.logger.log(Level.WARNING, "Failed to shutdown " + this.servletManager.getClass().getSimpleName(), ex);
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ================== ManagedObject ========================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.servletManager;
	}

}