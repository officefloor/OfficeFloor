package net.officefloor.plugin.managedobject.clazz;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Meta-data for the invocation of a {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessMethodMetaData {

	/**
	 * Obtains the index of the {@link ProcessState} invocation.
	 */
	private final int processIndex;

	/**
	 * Indicates if parameter to the {@link ProcessState}.
	 */
	private final boolean isParameter;

	/**
	 * Indicates if {@link FlowCallback} for the {@link ProcessState}.
	 */
	private final boolean isFlowCallback;

	/**
	 * Instantiate.
	 * 
	 * @param processIndex
	 *            Obtains the index of the {@link ProcessState} invocation.
	 * @param isParameter
	 *            Indicates if parameter to the {@link ProcessState}.
	 * @param isFlowCallback
	 *            Indicates if {@link FlowCallback} for the
	 *            {@link ProcessState}.
	 */
	public ProcessMethodMetaData(int processIndex, boolean isParameter, boolean isFlowCallback) {
		this.processIndex = processIndex;
		this.isParameter = isParameter;
		this.isFlowCallback = isFlowCallback;
	}

	/**
	 * Obtains the index of the {@link ProcessState} invocation.
	 * 
	 * @return Index of the {@link ProcessState} invocation.
	 */
	public int getProcessIndex() {
		return this.processIndex;
	}

	/**
	 * Indicates if parameter to the {@link ProcessState}.
	 * 
	 * @return <code>true</code> if parameter to the {@link ProcessState}.
	 */
	public boolean isParameter() {
		return this.isParameter;
	}

	/**
	 * Indicates if {@link FlowCallback} to the {@link ProcessState}.
	 * 
	 * @return <code>true</code> if {@link FlowCallback} to the
	 *         {@link ProcessState}.
	 */
	public boolean isFlowCallback() {
		return this.isFlowCallback;
	}

}