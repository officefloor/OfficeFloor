package net.officefloor.frame.api.function;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * {@link Office} aware {@link ManagedFunctionFactory}.
 * <p>
 * This allows the {@link ManagedFunctionFactory} to:
 * <ol>
 * <li>obtain the dynamic meta-data of its containing {@link Office}</li>
 * <li>ability to spawn {@link ProcessState} instances</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeAwareManagedFunctionFactory<O extends Enum<O>, F extends Enum<F>>
		extends ManagedFunctionFactory<O, F> {

	/**
	 * Provides the {@link ManagedFunctionFactory} its containing
	 * {@link Office}.
	 * 
	 * @param office
	 *            {@link Office} containing this {@link ManagedFunctionFactory}.
	 * @throws Exception
	 *             If fails to use the {@link Office}.
	 */
	void setOffice(Office office) throws Exception;

}