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
