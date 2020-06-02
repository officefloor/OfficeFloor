package net.officefloor.plugin.clazz.qualifier;

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.state.StatePoint;
import net.officefloor.plugin.clazz.state.StatePointImpl;

/**
 * {@link TypeQualifierInterrogatorContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TypeQualifierInterrogatorContextImpl extends StatePointImpl implements TypeQualifierInterrogatorContext {

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext;

	/**
	 * Instantiate.
	 * 
	 * @param statePoint    {@link StatePoint}.
	 * @param sourceContext {@link SourceContext}.
	 */
	public TypeQualifierInterrogatorContextImpl(StatePoint statePoint, SourceContext sourceContext) {
		super(statePoint);
		this.sourceContext = sourceContext;
	}

	/*
	 * ==================== TypeQualifierInterrogatorContext ====================
	 */

	@Override
	public SourceContext getSourceContext() {
		return this.sourceContext;
	}

}