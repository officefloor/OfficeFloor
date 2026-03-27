/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
	 * Obtains the location of this {@link StatePoint}.
	 * 
	 * @return Location of this {@link StatePoint}.
	 */
	default String toLocation() {
		return toLocation(this);
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

	/**
	 * Obtains the location of the {@link StatePoint}. This is typically for
	 * logging.
	 * 
	 * @param statePoint {@link StatePoint}.
	 * @return Location of the {@link StatePoint}.
	 */
	static String toLocation(StatePoint statePoint) {
		Field field = statePoint.getField();
		if (field != null) {
			return "Field " + field.getName();
		} else {
			Executable executable = statePoint.getExecutable();
			return (executable instanceof Constructor ? Constructor.class.getSimpleName()
					: "Method " + executable.getName()) + " parameter " + statePoint.getExecutableParameterIndex();
		}
	}

}
