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

package net.officefloor.plugin.clazz.dependency;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Context for the {@link ClassDependencies}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassDependenciesFlowContext {

	/**
	 * Adds a {@link Flow}.
	 * 
	 * @param flowName     Name of {@link Flow}.
	 * @param argumentType Argument {@link Class} to {@link Flow}.
	 * @param annotations  Annotations.
	 * @return {@link ClassItemIndex} of the {@link Flow}.
	 */
	ClassItemIndex addFlow(String flowName, Class<?> argumentType, Object[] annotations);

	/**
	 * Adds an {@link Escalation}.
	 * 
	 * @param escalationType Type of {@link Escalation}.
	 */
	void addEscalation(Class<? extends Throwable> escalationType);

	/**
	 * Adds an annotation to the {@link ManagedFunction} / {@link ManagedObject}
	 * requiring the dependency.
	 * 
	 * @param annotation Annotation.
	 */
	void addAnnotation(Object annotation);
}
