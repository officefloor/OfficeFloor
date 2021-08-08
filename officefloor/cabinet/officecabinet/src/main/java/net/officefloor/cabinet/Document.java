/*-
 * #%L
 * OfficeFloor Filing Cabinet
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.cabinet;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotates a {@link Class} to represent a {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Document {
		
	/**
	 * Allows dynamic naming of {@link Document}.
	 */
	@FunctionalInterface
	public static interface DocumentNamer {

		/**
		 * Obtains the name for the {@link Document}.
		 * 
		 * @param context {@link DocumentNameContext}.
		 * @return Name for the {@link Document}.
		 * @throws Exception If fails to name the {@link Document}.
		 */
		String getName(DocumentNameContext context) throws Exception;
	}

	/**
	 * Context for the {@link DocumentNamer}.
	 */
	public static interface DocumentNameContext {

		/**
		 * Obtains {@link Document} {@link Annotation} on {@link Class}.
		 * 
		 * @return {@link Document} {@link Annotation} on {@link Class}.
		 */
		Document getDocument();

		/**
		 * Obtains the {@link Class} of the {@link Document}.
		 * 
		 * @return {@link Class} of the {@link Document}.
		 */
		Class<?> getDocumentClass();
	}

	/**
	 * Default {@link DocumentNamer} that uses the simple name of the {@link Class}.
	 */
	public static class SimpleClassNameDocumentNamer implements DocumentNamer {

		/*
		 * ===================== DocumentNamer =============================
		 */

		@Override
		public String getName(DocumentNameContext context) throws Exception {
			String configuredName = context.getDocument().name();
			return ((configuredName == null) || (configuredName.trim().length() == 0))
					? context.getDocumentClass().getSimpleName()
					: configuredName;
		}
	}

	/**
	 * Custom {@link DocumentNamer} to name the {@link Document}.
	 * 
	 * @return {@link DocumentNamer} to name the {@link Document}.
	 */
	Class<? extends DocumentNamer> documentNamer() default SimpleClassNameDocumentNamer.class;

	/**
	 * Obtains the name of the document in cloud store.
	 * 
	 * @return Name of the document in cloud store.
	 */
	String name() default "";

}
