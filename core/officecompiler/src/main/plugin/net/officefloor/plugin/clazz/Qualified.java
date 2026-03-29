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

package net.officefloor.plugin.clazz;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Enables qualifying dependencies by textual name.
 * <p>
 * <strong>Caution</strong> should be placed on using this {@link Annotation}.
 * Using {@link Qualifier} {@link Annotation} allows for refactoring of
 * qualification names and compile errors about changes in names. Using this
 * {@link Qualified} provides no type safety about qualification names, no
 * compiler assistance and difficult refactoring of names in the code base.
 * <p>
 * <strong>Preference should always be for using {@link Qualifier}
 * {@link Annotation} instances.</strong>
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Qualifier(nameFactory = Qualified.QualifiedNameFactory.class)
public @interface Qualified {

	/**
	 * {@link QualifierNameFactory}.
	 */
	public static class QualifiedNameFactory implements QualifierNameFactory<Qualified> {
		@Override
		public String getQualifierName(Qualified annotation) {
			return annotation.value();
		}
	}

	/**
	 * Qualifier name.
	 * 
	 * @return Qualifier name.
	 */
	String value();

}
