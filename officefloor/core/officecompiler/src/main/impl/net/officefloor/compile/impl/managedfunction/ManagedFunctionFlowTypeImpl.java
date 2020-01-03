package net.officefloor.compile.impl.managedfunction;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link ManagedFunctionFlowType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionFlowTypeImpl<F extends Enum<F>>
		implements ManagedFunctionFlowType<F>, ManagedFunctionFlowTypeBuilder<F> {

	/**
	 * Index of this {@link ManagedFunctionFlowType}.
	 */
	private int index;

	/**
	 * Label for the {@link ManagedFunctionFlowType}.
	 */
	private String label = null;

	/**
	 * {@link Flow} key.
	 */
	private F key = null;

	/**
	 * Type of the argument.
	 */
	private Class<?> argumentType = null;

	/**
	 * Initiate.
	 * 
	 * @param index
	 *            Index of this {@link ManagedFunctionFlowType}.
	 */
	public ManagedFunctionFlowTypeImpl(int index) {
		this.index = index;
	}

	/*
	 * ==================== TaskFlowTypeBuilder ============================
	 */

	@Override
	public void setKey(F key) {
		this.key = key;
		this.index = key.ordinal();
	}

	@Override
	public void setArgumentType(Class<?> argumentType) {
		this.argumentType = argumentType;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	/*
	 * =================== TaskFlowType ===================================
	 */

	@Override
	public String getFlowName() {
		// Follow priorities to obtain the flow name
		if (!CompileUtil.isBlank(this.label)) {
			return this.label;
		} else if (this.key != null) {
			return this.key.toString();
		} else {
			return String.valueOf(this.index);
		}
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public Class<?> getArgumentType() {
		return this.argumentType;
	}

	@Override
	public F getKey() {
		return this.key;
	}

}