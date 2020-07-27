/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
