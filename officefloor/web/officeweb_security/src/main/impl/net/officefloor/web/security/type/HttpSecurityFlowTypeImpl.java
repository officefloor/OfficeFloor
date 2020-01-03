package net.officefloor.web.security.type;

import net.officefloor.compile.managedobject.ManagedObjectFlowType;

/**
 * {@link HttpSecurityFlowType} adapted from the {@link ManagedObjectFlowType}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityFlowTypeImpl<F extends Enum<F>> implements HttpSecurityFlowType<F> {

	/**
	 * {@link ManagedObjectFlowType}.
	 */
	private final ManagedObjectFlowType<F> flow;

	/**
	 * Initiate.
	 * 
	 * @param flow {@link ManagedObjectFlowType}.
	 */
	public HttpSecurityFlowTypeImpl(ManagedObjectFlowType<F> flow) {
		this.flow = flow;
	}

	/*
	 * ==================== HttpSecurityFlowType =======================
	 */

	@Override
	public String getFlowName() {
		return this.flow.getFlowName();
	}

	@Override
	public F getKey() {
		return this.flow.getKey();
	}

	@Override
	public int getIndex() {
		return this.flow.getIndex();
	}

	@Override
	public Class<?> getArgumentType() {
		return this.flow.getArgumentType();
	}
}
