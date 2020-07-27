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
