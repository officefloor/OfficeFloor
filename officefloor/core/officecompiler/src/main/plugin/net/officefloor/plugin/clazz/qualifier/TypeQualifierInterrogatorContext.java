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
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.state.StatePoint;

/**
 * Context for the {@link TypeQualifierInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TypeQualifierInterrogatorContext extends StatePoint {

	/**
	 * <p>
	 * Obtains the {@link AnnotatedElement}.
	 * <p>
	 * Typically this is either a {@link Field} or {@link Parameter}.
	 * 
	 * @return {@link AnnotatedElement}.
	 */
	AnnotatedElement getAnnotatedElement();

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}
