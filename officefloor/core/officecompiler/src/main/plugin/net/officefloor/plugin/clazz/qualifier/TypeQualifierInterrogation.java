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
