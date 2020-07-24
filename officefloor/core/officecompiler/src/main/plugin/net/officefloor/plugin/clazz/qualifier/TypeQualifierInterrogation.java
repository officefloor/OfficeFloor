package net.officefloor.plugin.clazz.qualifier;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.state.StatePoint;

/**
 * {@link TypeQualifierInterrogatorContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TypeQualifierInterrogation implements TypeQualifierInterrogatorContext {

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext;

	/**
	 * {@link StatePoint}.
	 */
	private StatePoint statePoint;

	/**
	 * Instantiate.
	 * 
	 * @param sourceContext {@link SourceContext}.
	 */
	public TypeQualifierInterrogation(SourceContext sourceContext) {
		this.sourceContext = sourceContext;
	}

	/**
	 * Extracts the possible type qualifier.
	 * 
	 * @param statePoint {@link StatePoint}.
	 * @return Type qualifier or <code>null</code> if no qualification.
	 * @throws Exception If fails to extract the type qualifier.
	 */
	public String extractTypeQualifier(StatePoint statePoint) throws Exception {

		// Specify state point
		this.statePoint = statePoint;

		// Interrogate for type qualifier
		for (TypeQualifierInterrogator interrogator : this.sourceContext
				.loadOptionalServices(TypeQualifierInterrogatorServiceFactory.class)) {
			String typeQualifier = interrogator.interrogate(this);
			if (typeQualifier != null) {
				return typeQualifier; // found qualifier
			}
		}

		// As here, no type qualifier
		return null;
	}

	/*
	 * ==================== TypeQualifierInterrogatorContext ====================
	 */

	@Override
	public AnnotatedElement getAnnotatedElement() {
		return this.statePoint.getAnnotatedElement();
	}

	@Override
	public Field getField() {
		return this.statePoint.getField();
	}

	@Override
	public Executable getExecutable() {
		return this.statePoint.getExecutable();
	}

	@Override
	public int getExecutableParameterIndex() {
		return this.statePoint.getExecutableParameterIndex();
	}

	@Override
	public SourceContext getSourceContext() {
		return this.sourceContext;
	}

}