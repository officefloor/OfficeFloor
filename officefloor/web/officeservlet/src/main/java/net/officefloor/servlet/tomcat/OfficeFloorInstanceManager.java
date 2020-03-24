package net.officefloor.servlet.tomcat;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.tomcat.InstanceManager;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.servlet.inject.ServletInjector;

/**
 * {@link OfficeFloor} {@link InstanceManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorInstanceManager implements InstanceManager {

	/**
	 * {@link Class} to its {@link ServletInjector}.
	 */
	private final Map<Class<?>, ServletInjector> injectors;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Instantiate.
	 * 
	 * @param injectors   {@link Class} to its {@link ServletInjector}.
	 * @param classLoader {@link ClassLoader}.
	 */
	public OfficeFloorInstanceManager(Map<Class<?>, ServletInjector> injectors, ClassLoader classLoader) {
		this.injectors = injectors;
		this.classLoader = classLoader;
	}

	/*
	 * ==================== InstanceManager ===========================
	 */

	@Override
	public Object newInstance(String fqcn, ClassLoader classLoader)
			throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException,
			ClassNotFoundException, IllegalArgumentException, NoSuchMethodException, SecurityException {
		Class<?> clazz = classLoader.loadClass(fqcn);
		return this.newInstance(clazz);
	}

	@Override
	public Object newInstance(String className)
			throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException,
			ClassNotFoundException, IllegalArgumentException, NoSuchMethodException, SecurityException {
		return this.newInstance(this.classLoader.loadClass(className));
	}

	@Override
	public Object newInstance(Class<?> clazz) throws IllegalAccessException, InvocationTargetException, NamingException,
			InstantiationException, IllegalArgumentException, NoSuchMethodException, SecurityException {

		// Instantiate
		Object object = clazz.getConstructor().newInstance();

		// Load any possible injection
		this.newInstance(object);

		// Return the instance
		return object;
	}

	@Override
	public void newInstance(Object o) throws IllegalAccessException, InvocationTargetException, NamingException {

		// Undertake injection of dependencies
		ServletInjector injector = this.injectors.get(o.getClass());
		if (injector != null) {
			try {
				injector.inject(o);
			} catch (Exception ex) {
				throw new InvocationTargetException(ex);
			}
		}
	}

	@Override
	public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException {
		// Nothing to destroy
	}

}