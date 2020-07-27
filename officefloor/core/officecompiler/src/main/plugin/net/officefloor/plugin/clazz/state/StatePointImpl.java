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
