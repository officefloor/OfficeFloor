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

import net.officefloor.frame.api.source.ServiceFactory;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.state.StatePoint;

/**
 * Context for the {@link TypeQualifierInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TypeQualifierInterrogatorServiceFactory extends ServiceFactory<TypeQualifierInterrogator> {

	/**
	 * Extracts the possible type qualifier.
	 * 
	 * @param statePoint    {@link StatePoint}.
	 * @param sourceContext {@link SourceContext}.
	 * @return Type qualifier or <code>null</code> if no qualification.
	 * @throws Exception If fails to extract the type qualifier.
	 */
	static String extractTypeQualifier(StatePoint statePoint, SourceContext sourceContext) throws Exception {

		// Create the context
		TypeQualifierInterrogatorContext context = new TypeQualifierInterrogatorContextImpl(statePoint, sourceContext);

		// Interrogate for type qualifier
		for (TypeQualifierInterrogator interrogator : sourceContext
				.loadOptionalServices(TypeQualifierInterrogatorServiceFactory.class)) {
			String typeQualifier = interrogator.interrogate(context);
			if (typeQualifier != null) {
				return typeQualifier; // found qualifier
			}
		}

		// As here, no type qualifier
		return null;
	}

}