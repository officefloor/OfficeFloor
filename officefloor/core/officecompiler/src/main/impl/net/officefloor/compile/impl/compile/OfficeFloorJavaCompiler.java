/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.compile;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.function.Consumer;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Java compiler to avoid {@link Proxy} implementations.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class OfficeFloorJavaCompiler {

	/**
	 * {@link System} property to indicate if Java compiling active for
	 * {@link OfficeFloor}.
	 */
	public static final String SYSTEM_PROPERTY_JAVA_COMPILING = "officefloor.java.compiling";

	/**
	 * Creates a new instance of the {@link OfficeFloorJavaCompiler}.
	 * 
	 * @param classLoader {@link ClassLoader} to load existing {@link Class}
	 *                    instances.
	 * @return {@link OfficeFloorJavaCompiler} or <code>null</code> if Java
	 *         compiling not available.
	 */
	public static OfficeFloorJavaCompiler newInstance(ClassLoader classLoader) {

		// Determine if compiling active
		boolean isActiveCompiling = Boolean
				.parseBoolean(System.getProperty(SYSTEM_PROPERTY_JAVA_COMPILING, Boolean.TRUE.toString()));
		if (!isActiveCompiling) {
			return null;
		}

		// Attempt to load (will fail if Java compiler not available)
		try {
			Class<?> implClass = classLoader.loadClass(OfficeFloorJavaCompiler.class.getName() + "Impl");
			return (OfficeFloorJavaCompiler) implClass.getConstructor(ClassLoader.class).newInstance(classLoader);

		} catch (Exception ex) {
			// Java compiling module not available
			return null;
		}
	}

	/**
	 * Wrapper context for a {@link Method} implementation.
	 */
	public static interface WrapperContext {

		/**
		 * Obtains the {@link Method} being implemented.
		 * 
		 * @return {@link Method} being implemented.
		 */
		Method getMethod();

		/**
		 * <p>
		 * Specifies the return wrap {@link Class}.
		 * <p>
		 * This results in implementation of the form:
		 * <p>
		 * <code>
		 * 	return new &lt;className&gt;(&lt;default delegation&gt;);
		 * </code>
		 * 
		 * @param className Wrap {@link Class}.
		 */
		void setReturnWrapClass(String className);

		/**
		 * Specifies the return wrap {@link Class}.
		 * 
		 * @param javaSource {@link JavaSource}.
		 * 
		 * @see #setReturnWrapClass(String)
		 */
		void setReturnWrapClass(JavaSource javaSource);

		/**
		 * Writes custom source implementation.
		 * 
		 * @param source Source for custom implementation.
		 */
		void write(String source);

		/**
		 * Writes custom source line implementation.
		 * 
		 * @param source Source line for custom implementation.
		 */
		void writeln(String source);
	}

	/**
	 * Java source.
	 */
	public static interface JavaSource {

		/**
		 * Obtains the {@link Class} name for the {@link JavaSource}.
		 * 
		 * @return {@link Class} name for the {@link JavaSource}.
		 */
		String getClassName();

		/**
		 * Convenience method to compile a single {@link JavaSource}.
		 * 
		 * @return {@link Class} for the {@link JavaSource}.
		 */
		Class<?> compile();
	}

	/**
	 * Adds a {@link JavaSource}.
	 * 
	 * @param className {@link Class} name.
	 * @param source    Source for the {@link Class}.
	 * @return {@link JavaSource}.
	 */
	public abstract JavaSource addSource(String className, String source);

	/**
	 * Adds a wrapper {@link JavaSource}.
	 * 
	 * @param type           Type being wrapped.
	 * @param wrapperContext {@link Consumer} to configure the
	 *                       {@link WrapperContext}.
	 * @return {@link JavaSource} for the wrapper.
	 */
	public JavaSource addWrapper(Class<?> type, Consumer<WrapperContext> wrapperContext) {
		return this.addWrapper(type, type, wrapperContext);
	}

	/**
	 * Adds a wrapper {@link JavaSource}.
	 * 
	 * @param wrappedType    Wrapper type.
	 * @param delegateType   Delegate type.
	 * @param wrapperContext {@link Consumer} to configure the
	 *                       {@link WrapperContext}.
	 * @return {@link JavaSource} for the wrapper.
	 */
	public abstract JavaSource addWrapper(Class<?> wrappedType, Class<?> delegateType,
			Consumer<WrapperContext> wrapperContext);

	/**
	 * Compiles all the added {@link JavaSource} instances.
	 * 
	 * @return {@link Map} of {@link JavaSource} to its {@link Class}.
	 */
	public abstract Map<JavaSource, Class<?>> compile();

}