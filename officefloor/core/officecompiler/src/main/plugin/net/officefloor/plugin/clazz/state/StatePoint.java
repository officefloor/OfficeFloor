package net.officefloor.plugin.clazz.state;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Identifies a state point.
 * 
 * @author Daniel Sagenschneider
 */
public interface StatePoint {

	/**
	 * Obtains the {@link Field}.
	 * 
	 * @return {@link Field}. Will be <code>null</code> if {@link Executable}.
	 */
	Field getField();

	/**
	 * Obtains the {@link Executable}. Typically this is either the
	 * {@link Constructor} or {@link Method} loading the dependency.
	 * 
	 * @return {@link Executable}. Will be <code>null</code> if {@link Field}.
	 */
	Executable getExecutable();

	/**
	 * Obtains the index of the parameter on the {@link Executable}.
	 * 
	 * @return Index of the parameter on the {@link Executable}.
	 */
	int getExecutableParameterIndex();

	/**
	 * <p>
	 * Convenience to obtain the {@link AnnotatedElement}.
	 * <p>
	 * This is the {@link Field} or {@link Parameter}.
	 * 
	 * @return {@link AnnotatedElement}.
	 */
	default AnnotatedElement getAnnotatedElement() {
		Field field = this.getField();
		return field != null ? field : this.getExecutable().getParameters()[this.getExecutableParameterIndex()];
	}

	/**
	 * Creates {@link StatePoint} for a {@link Field}.
	 * 
	 * @param field {@link Field}.
	 * @return {@link StatePoint}.
	 */
	static StatePoint of(Field field) {
		return new StatePointImpl(field);
	}

	/**
	 * Creates a {@link StatePoint} for {@link Executable} {@link Parameter}.
	 * 
	 * @param executable     {@link Executable}.
	 * @param parameterIndex {@link Parameter} index.
	 * @return {@link StatePoint}.
	 */
	static StatePoint of(Executable executable, int parameterIndex) {
		return new StatePointImpl(executable, parameterIndex);
	}
}