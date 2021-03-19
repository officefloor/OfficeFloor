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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.logging.Logger;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.clazz.state.StatePoint;

/**
 * Context for the {@link ClassDependencyManufacturer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassDependencyManufacturerContext extends StatePoint {

	/**
	 * Obtains the {@link Class} of the dependency.
	 * 
	 * @return {@link Class} of the dependency.
	 */
	Class<?> getDependencyClass();

	/**
	 * Obtains the {@link Type} of the dependency.
	 * 
	 * @return {@link Type} of the dependency.
	 */
	Type getDependencyType();

	/**
	 * <p>
	 * Obtains the dependency qualifier.
	 * <p>
	 * This is via {@link Qualifier} or {@link Qualified} {@link Annotation} on the
	 * dependency.
	 * <p>
	 * This provides standard means to obtain the qualifier and avoid each
	 * {@link ClassDependencyManufacturer} handling {@link Annotation} to determine.
	 * 
	 * @return Qualifier for the dependency.
	 */
	String getDependencyQualifier();

	/**
	 * Obtains the {@link Annotation} instances for the dependency.
	 * 
	 * @return {@link Annotation} instances for the dependency.
	 */
	Annotation[] getDependencyAnnotations();

	/**
	 * Obtains the {@link Annotation} by the input type.
	 * 
	 * @param annotationType Required {@link Annotation} type.
	 * @return {@link Annotation} or <code>null</code> if no {@link Annotation} by
	 *         the required type.
	 */
	<A extends Annotation> A getDependencyAnnotation(Class<? extends A> annotationType);

	/**
	 * Obtains the name of item receiving the dependency.
	 * 
	 * @return Name of item receiving the dependency.
	 */
	String getName();

	/**
	 * Obtains the {@link Logger}.
	 * 
	 * @return {@link Logger}.
	 */
	Logger getLogger();

	/**
	 * Adds a {@link ClassDependency}.
	 * 
	 * @param objectType Type of the dependent {@link Object}.
	 * @return {@link ClassDependency}.
	 */
	ClassDependency newDependency(Class<?> objectType);

	/**
	 * Adds a {@link ClassFlow}.
	 * 
	 * @param flowName Name of {@link Flow}.
	 * @return {@link ClassFlow}.
	 */
	ClassFlow newFlow(String flowName);

	/**
	 * Registers an {@link Escalation}.
	 * 
	 * @param <E>            {@link Escalation} type.
	 * @param escalationType Type to be handled by an {@link EscalationFlow}.
	 */
	<E extends Throwable> void addEscalation(Class<E> escalationType);

	/**
	 * Adds an annotation to the {@link ManagedFunction} / {@link ManagedObject}
	 * requiring the dependency.
	 * 
	 * @param annotation Annotation.
	 */
	void addAnnotation(Object annotation);

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

	/**
	 * Allows further configuration of the {@link Class} dependency.
	 */
	public static interface ClassDependency
			extends ItemBuilder, QualifierConfigurer<ClassDependency>, AnnotationConfigurer<ClassDependency> {
	}

	/**
	 * Allows further configuration of the {@link Class} dependency.
	 */
	public static interface ClassFlow extends ItemBuilder, AnnotationConfigurer<ClassFlow> {

		/**
		 * Specifies the argument type.
		 * 
		 * @param argumentType Argument type.
		 * @return <code>this</code>.
		 */
		ClassFlow setArgumentType(Class<?> argumentType);
	}

	/**
	 * Builds and adds the item.
	 */
	public static interface ItemBuilder {

		/**
		 * Builds and adds the item.
		 * 
		 * @return Index of item.
		 */
		ClassItemIndex build();
	}

	/**
	 * Allows specifying the qualifier.
	 */
	public static interface QualifierConfigurer<T> {

		/**
		 * Specifies the qualifier.
		 * 
		 * @param qualifier Qualifier.
		 * @return <code>this</code>.
		 */
		T setQualifier(String qualifier);
	}

	/**
	 * Allows specifying annotation.
	 */
	public static interface AnnotationConfigurer<T> {

		/**
		 * Adds an annotation.
		 * 
		 * @param annotation Annotation.
		 * @return <code>this</code>.
		 */
		T addAnnotation(Object annotation);

		/**
		 * Adds many annotations.
		 * 
		 * @param annotations Annotations.
		 * @return <code>this</code>.
		 */
		T addAnnotations(Collection<? extends Object> annotations);
	}

}
