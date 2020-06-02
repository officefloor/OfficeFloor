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

package net.officefloor.plugin.managedobject.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.source.SourceContext;
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
	 * Obtains the name of the {@link ManagedObject}.
	 * 
	 * @return Name of the {@link ManagedObject}.
	 */
	String getObjectName();

	/**
	 * Adds a {@link ClassDependency}.
	 * 
	 * @param objectType Type of the dependent {@link Object}.
	 * @param builder    Means to build the
	 *                   {@link ManagedFunctionObjectTypeBuilder}.
	 * @return {@link ClassDependency}.
	 */
	ClassDependency addDependency(Class<?> objectType);

	/**
	 * Adds a {@link ClassFlow}.
	 * 
	 * @param builder Means to build the {@link ManagedFunctionFlowTypeBuilder}.
	 * @return {@link ClassFlow}.
	 */
	ClassFlow addFlow();

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
			extends AddedIndex, NameConfigurer<ClassDependency>, TypeQualifierConfigurer<ClassDependency> {
	}

	/**
	 * Allows further configuration of the {@link Class} dependency.
	 */
	public static interface ClassFlow extends AddedIndex, NameConfigurer<ClassFlow> {

		/**
		 * Specifies the argument type.
		 * 
		 * @param argumentType Argument type.
		 * @return <code>this</code>.
		 */
		ClassFlow setArgumentType(Class<?> argumentType);
	}

	/**
	 * Added index of item.
	 */
	public static interface AddedIndex {

		/**
		 * Obtains the index of item.
		 * 
		 * @return Index of item.
		 */
		int getIndex();
	}

	/**
	 * Allows specifying the name.
	 */
	public static interface NameConfigurer<T> {

		/**
		 * Specifies the name.
		 * 
		 * @param name Name.
		 * @return <code>this</code>.
		 */
		T setName(String name);
	}

	/**
	 * Allows specifying the type qualifier.
	 */
	public static interface TypeQualifierConfigurer<T> {

		/**
		 * Specifies the type qualifier.
		 * 
		 * @param typeQualifier Type qualifier.
		 * @return <code>this</code>.
		 */
		T setTypeQualifier(String typeQualifier);
	}

}