/*-
 * #%L
 * Procedure
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

package net.officefloor.activity.procedure.spi;

import java.lang.reflect.Method;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.method.MethodObjectFactory;

/**
 * Context for the {@link ProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureMethodContext {

	/**
	 * Obtains the resource {@link Class} to provide the {@link Procedure}.
	 * 
	 * @return Resource {@link Class} configured to provide the {@link Procedure}.
	 */
	Class<?> getResource();

	/**
	 * Name of the {@link Procedure}.
	 * 
	 * @return Name of the {@link Procedure}.
	 */
	String getProcedureName();

	/**
	 * <p>
	 * Overrides the default {@link MethodObjectFactory}.
	 * <p>
	 * Specifying <code>null</code> indicates a static {@link Method}.
	 * 
	 * @param factory {@link MethodObjectFactory}.
	 */
	void setMethodObjectInstanceFactory(MethodObjectFactory factory);

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}
