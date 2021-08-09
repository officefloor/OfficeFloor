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

package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;

import net.officefloor.compile.section.TypeQualification;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link TypeQualification} of a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
public @interface TypeQualifier {

	/**
	 * Qualifier.
	 * 
	 * @return Qualifier.
	 */
	Class<? extends Annotation> qualifier() default TypeQualifier.class;

	/**
	 * Type.
	 * 
	 * @return Type.
	 */
	Class<?> type();

}
