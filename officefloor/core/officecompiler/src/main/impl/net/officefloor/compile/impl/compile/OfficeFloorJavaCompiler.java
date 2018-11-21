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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

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
	 * Indicates if the compiler is available.
	 */
	private static boolean isCompilerAvailable = true;

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
		if ((!isActiveCompiling) || (!isCompilerAvailable)) {
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
	 * Operation undertaken without the Java compiler being available.
	 */
	public static interface NonCompilerOperation<T extends Throwable> {

		/**
		 * Logic of operation.
		 * 
		 * @throws T Possible failure.
		 */
		void run() throws T;
	}

	/**
	 * <p>
	 * Undertakes the {@link NonCompilerOperation} without making the Java compiler
	 * available.
	 * <p>
	 * This is useful for testing non Java compile solutions.
	 *
	 * @param           <T> Possible {@link Throwable} type.
	 * @param operation {@link NonCompilerOperation}.
	 * @throws T Possible failure.
	 */
	public static <T extends Throwable> void runWithoutCompiler(NonCompilerOperation<T> operation) throws T {
		try {
			isCompilerAvailable = false;

			// Undertake the operation (without compiler)
			operation.run();

		} finally {
			isCompilerAvailable = true;
		}
	}

	/**
	 * {@link Class} name.
	 */
	public static interface ClassName {

		/**
		 * Package name.
		 * 
		 * @return Package name.
		 */
		String getPackageName();

		/**
		 * Simple class name.
		 * 
		 * @return Simple class name.
		 */
		String getClassName();

		/**
		 * Fully qualified class name.
		 * 
		 * @return Fully qualified class name.
		 */
		String getName();
	}

	/**
	 * Generates a unique {@link ClassName}.
	 * 
	 * @param name Seed name.
	 * @return {@link ClassName}.
	 */
	public abstract ClassName createClassName(String name);

	/**
	 * Obtains the source name for the {@link Class}.
	 * 
	 * @param type {@link Class}.
	 * @return Name used in source to reference the type.
	 */
	public abstract String getSourceName(Class<?> type);

	/**
	 * Class {@link Field}.
	 */
	public static interface ClassField {

		/**
		 * Obtains the {@link Field} type.
		 * 
		 * @return {@link Field} type.
		 */
		Class<?> getFieldType();

		/**
		 * Obtains the {@link Field} name.
		 * 
		 * @return {@link Field} name.
		 */
		String getFieldName();
	}

	/**
	 * Creates the {@link ClassField}.
	 * 
	 * @param fieldType {@link Field} type.
	 * @param fieldName {@link Field} name.
	 * @return {@link ClassField}.
	 */
	public abstract ClassField createField(Class<?> fieldType, String fieldName);

	/**
	 * Writes the {@link Constructor}.
	 * 
	 * @param appendable {@link Appendable}.
	 * @param className  Simple name of the {@link Class}.
	 * @param fields     {@link ClassField} instances.
	 * @throws IOException If fails to write the {@link Constructor}.
	 */
	public abstract void writeConstructor(Appendable appendable, String className, ClassField... fields)
			throws IOException;

	/**
	 * Writes the {@link Method} signature to the {@link Appendable}.
	 * 
	 * @param appendable {@link Appendable}.
	 * @param method     {@link Method}.
	 * @return <code>true</code> if returns a value (<code>false</code> for
	 *         <code>void</code> {@link Method} return).
	 * @throws IOException If fails to write the {@link Method} signature.
	 */
	public abstract boolean writeMethodSignature(Appendable appendable, Method method) throws IOException;

	/**
	 * Writes the delegate {@link Method} call.
	 * 
	 * @param source   {@link Appendable}.
	 * @param delegate Means to access delegate.
	 * @param method   {@link Method}.
	 * @throws IOException If fails write delegate {@link Method} call.
	 */
	public abstract void writeDelegateMethodCall(Appendable source, String delegate, Method method) throws IOException;

	/**
	 * Writes the {@link Method} implementation by invoking the delegate.
	 * 
	 * @param source   {@link Appendable}.
	 * @param delegate Means to access delegate.
	 * @param method   {@link Method}.
	 * @throws IOException If fails write delegate {@link Method} implementation.
	 */
	public abstract void writeMethodImplementation(Appendable source, String delegate, Method method)
			throws IOException;

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
	 * @param className {@link ClassName}.
	 * @param source    Source for the {@link Class}.
	 * @return {@link JavaSource}.
	 */
	public JavaSource addSource(ClassName className, String source) {
		return this.addSource(className.getName(), source);
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
	 * Writes the {@link Constructor} for the wrapping implementation.
	 */
	@FunctionalInterface
	public static interface ConstructorWriter {

		/**
		 * Writes the {@link Constructor}.
		 * 
		 * @param context {@link ConstructorWriterContext}.
		 * @throws IOException If fails to write the {@link Constructor}.
		 */
		void write(ConstructorWriterContext context) throws IOException;
	}

	/**
	 * Context for the {@link ConstructorWriter}.
	 */
	public static interface ConstructorWriterContext {

		/**
		 * Obtains the {@link ClassName} name for the {@link JavaSource}.
		 * 
		 * @return {@link ClassName} name for the {@link JavaSource}.
		 */
		ClassName getClassName();

		/**
		 * Obtains the {@link Appendable} to write additional source.
		 * 
		 * @return {@link Appendable} to write additional source.
		 */
		Appendable getSource();
	}

	/**
	 * Writes each {@link Method} required by implementing interfaces.
	 */
	@FunctionalInterface
	public static interface MethodWriter {

		/**
		 * Writes the {@link Method}.
		 * 
		 * @param context {@link MethodWriterContext}.
		 * @throws IOException If fails to write the {@link Method}.
		 */
		void write(MethodWriterContext context) throws IOException;
	}

	/**
	 * Context for the {@link MethodWriterContext}.
	 */
	public static interface MethodWriterContext {

		/**
		 * Obtains the {@link Class} of the interface being implemented.
		 * 
		 * @return {@link Class} of the interface being implemented.
		 */
		Class<?> getInterface();

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

		/**
		 * Obtains the source to re-use {@link OfficeFloorJavaCompiler} helper methods.
		 * 
		 * @return Source to re-use {@link OfficeFloorJavaCompiler} helper methods.
		 */
		Appendable getSource();
	}

	/**
	 * Provides means to including source.
	 */
	@FunctionalInterface
	public static interface JavaSourceWriter {

		/**
		 * Provides additional source.
		 * 
		 * @param context {@link JavaSourceContext}.
		 * @throws IOException If fails to write the additional source code.
		 */
		void write(JavaSourceContext context) throws IOException;
	}

	/**
	 * Context for the {@link JavaSourceWriter}.
	 */
	public static interface JavaSourceContext {

		/**
		 * Obtains the {@link ClassName} name for the {@link JavaSource}.
		 * 
		 * @return {@link ClassName} name for the {@link JavaSource}.
		 */
		ClassName getClassName();

		/**
		 * Obtains the {@link Appendable} to write additional source.
		 * 
		 * @return {@link Appendable} to write additional source.
		 */
		Appendable getSource();
	}

	/**
	 * Adds a wrapper {@link JavaSource}.
	 * 
	 * @param type                   Type being wrapped.
	 * @param methodWriter           {@link MethodWriter}. May be <code>null</code>
	 *                               to use default implementation.
	 * @param additionalSourceWriter {@link JavaSourceWriter} instances.
	 * @return {@link JavaSource} for the wrapper.
	 * @throws IOException If fails to write the wrapper.
	 */
	public JavaSource addWrapper(Class<?> type, MethodWriter methodWriter, JavaSourceWriter... additionalSourceWriter)
			throws IOException {
		return this.addWrapper(new Class[] { type }, type, null, null, methodWriter, additionalSourceWriter);
	}

	/**
	 * Adds a wrapper {@link JavaSource}.
	 * 
	 * @param wrappingTypes          Wrapping types.
	 * @param delegateType           Delegate type.
	 * @param delegateExtraction     Means to extract the wrapped implementation
	 *                               from the delegate. May be <code>null</code> to
	 *                               use default.
	 * @param constructorWriter      {@link ConstructorWriter}. May be
	 *                               <code>null</code> to use default
	 *                               {@link Constructor}.
	 * @param methodWriter           {@link MethodWriter}. May be <code>null</code>
	 *                               to use default implementation.
	 * @param additionalSourceWriter {@link JavaSourceWriter} instances.
	 * @return {@link JavaSource} for the wrapper.
	 * @throws IOException If fails to write the wrapper.
	 */
	public abstract JavaSource addWrapper(Class<?>[] wrappingTypes, Class<?> delegateType, String delegateExtraction,
			ConstructorWriter constructorWriter, MethodWriter methodWriter, JavaSourceWriter... additionalSourceWriter)
			throws IOException;

	/**
	 * Compiles all the added {@link JavaSource} instances.
	 * 
	 * @return {@link Map} of {@link JavaSource} to its {@link Class}.
	 */
	public abstract Map<JavaSource, Class<?>> compile();

}