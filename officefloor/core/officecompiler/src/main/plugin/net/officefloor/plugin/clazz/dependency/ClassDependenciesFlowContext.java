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
