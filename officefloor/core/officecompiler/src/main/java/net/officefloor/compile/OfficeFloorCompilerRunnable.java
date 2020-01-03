package net.officefloor.compile;

import java.lang.reflect.Proxy;

/**
 * <p>
 * Runnable using {@link ClassLoader} of the {@link OfficeFloorCompiler}.
 * <p>
 * This is typically used for graphical configuration to run extended
 * functionality adapted for the {@link ClassLoader} of the
 * {@link OfficeFloorCompiler}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorCompilerRunnable<T> {

	/**
	 * Contains the runnable functionality.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler} loaded with the
	 *            {@link ClassLoader}.
	 * @param parameters
	 *            Parameters. As {@link Proxy} instances are used to bridge
	 *            {@link Class} compatibility issues of using different
	 *            {@link ClassLoader} instances, parameters should only be
	 *            referenced by their implementing interfaces.
	 * @return Result from runnable.
	 * @throws Exception
	 *             If failure in running.
	 */
	T run(OfficeFloorCompiler compiler, Object[] parameters) throws Exception;

}