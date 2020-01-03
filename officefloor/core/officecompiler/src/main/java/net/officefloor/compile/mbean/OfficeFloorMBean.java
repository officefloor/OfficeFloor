package net.officefloor.compile.mbean;

import java.lang.reflect.Proxy;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link Proxy} interface for the {@link OfficeFloor} MBean.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorMBean {

	/**
	 * Obtain the names of the {@link Office} instances within the
	 * {@link OfficeFloor}.
	 * 
	 * @return Names of the {@link Office} instances within the
	 *         {@link OfficeFloor}.
	 */
	String[] getOfficeNames();

	/**
	 * Obtains the names of the {@link ManagedFunction} instances within the
	 * {@link Office}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @return Names of the {@link ManagedFunction} instances within the
	 *         {@link Office}.
	 */
	String[] getManagedFunctionNames(String officeName);

	/**
	 * Obtains the parameter type for the {@link ManagedFunction}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return Parameter type for the {@link ManagedFunction}. May be
	 *         <code>null</code> if no parameter for {@link ManagedFunction}.
	 */
	String getManagedFunctionParameterType(String officeName, String functionName);

	/**
	 * Invokes the {@link ManagedFunction}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param functionName
	 *            Name of the {@link ManagedFunction} within the {@link Office}.
	 * @param parameter
	 *            Optional parameter for the {@link ManagedFunction}. May be
	 *            <code>null</code>.
	 */
	void invokeFunction(String officeName, String functionName, String parameter);

	/**
	 * Closes the {@link OfficeFloor}.
	 */
	void closeOfficeFloor();

}