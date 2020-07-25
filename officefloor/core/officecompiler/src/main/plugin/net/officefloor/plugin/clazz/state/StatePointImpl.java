package net.officefloor.plugin.clazz.state;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 * {@link StatePoint} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class StatePointImpl implements StatePoint {

	/**
	 * {@link Field}.
	 */
	private final Field field;

	/**
	 * {@link Executable}.
	 */
	private final Executable executable;

	/**
	 * Parameter index.
	 */
	private final int parameterIndex;

	/**
	 * Allows extension of the {@link StatePoint}.
	 * 
	 * @param statePoint {@link StatePoint}.
	 */
	public StatePointImpl(StatePoint statePoint) {
		this.field = statePoint.getField();
		this.executable = statePoint.getExecutable();
		this.parameterIndex = statePoint.getExecutableParameterIndex();
	}

	/**
	 * Instantiates for {@link Field}.
	 * 
	 * @param field {@link Field}.
	 */
	StatePointImpl(Field field) {
		this.field = field;
		this.executable = null;
		this.parameterIndex = -1;
	}

	/**
	 * Instantiates for {@link Executable} {@link Parameter}.
	 * 
	 * @param executable     {@link Executable}.
	 * @param parameterIndex {@link Parameter} index.
	 */
	StatePointImpl(Executable executable, int parameterIndex) {
		this.field = null;
		this.executable = executable;
		this.parameterIndex = parameterIndex;
	}

	/*
	 * ==================== StatePoint ====================
	 */

	@Override
	public Field getField() {
		return this.field;
	}

	@Override
	public Executable getExecutable() {
		return this.executable;
	}

	@Override
	public int getExecutableParameterIndex() {
		return this.parameterIndex;
	}

}