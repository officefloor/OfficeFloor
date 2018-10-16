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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import net.officefloor.compile.issues.CompileError;

/**
 * {@link OfficeFloorJavaCompiler} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorJavaCompilerImpl extends OfficeFloorJavaCompiler {

	/**
	 * Indicates if the {@link Method} has a return value.
	 * 
	 * @param method {@link Method}.
	 * @return <code>true</code> if the {@link Method} has a return value.
	 */
	private static boolean isReturnValue(Method method) {
		Class<?> returnType = method.getReturnType();
		return ((returnType != null) && (!Void.TYPE.equals(returnType)));
	}

	/**
	 * Next {@link Class} index.
	 */
	private static final AtomicInteger nextClassIndex = new AtomicInteger(1);

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link JavaCompiler}.
	 */
	private final JavaCompiler javaCompiler;

	/**
	 * {@link OfficeFloorJavaFileManager}.
	 */
	private final OfficeFloorJavaFileManager fileManager;

	/**
	 * Compiled {@link Class} definitions.
	 */
	private final Map<String, ByteArrayOutputStream> compiledClasses = new HashMap<>();

	/**
	 * {@link CompiledClassLoader}.
	 */
	private final CompiledClassLoader compiledClassLoader;

	/**
	 * Source {@link JavaSourceImpl} instances.
	 */
	private final List<JavaSourceImpl> sources = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param classLoader {@link ClassLoader}.
	 */
	public OfficeFloorJavaCompilerImpl(ClassLoader classLoader) {
		this.classLoader = classLoader;
		this.compiledClassLoader = new CompiledClassLoader(this.classLoader);

		// Obtain the Java Compiler
		this.javaCompiler = ToolProvider.getSystemJavaCompiler();

		// Create in memory file manager
		this.fileManager = new OfficeFloorJavaFileManager(this.javaCompiler.getStandardFileManager(null, null, null));
	}

	/*
	 * ================ OfficeFloorJavaCompiler =======================
	 */

	@Override
	public ClassName createClassName(String name) {

		// Handle inner class names
		name = name.replace('$', '.');

		// Split the name
		int splitIndex = name.lastIndexOf('.');
		String packageName;
		String className;
		if (splitIndex > 0) {
			packageName = "generated.officefloor." + name.substring(0, splitIndex);
			className = name.substring(splitIndex + ".".length());
		} else {
			packageName = "generated.officefloor";
			className = name;
		}

		// Suffix class name with index to keep unique naming
		className = className + nextClassIndex.getAndIncrement();

		// Return class name
		final String finalPackageName = packageName;
		final String finalClassName = className;
		return new ClassName() {

			@Override
			public String getPackageName() {
				return finalPackageName;
			}

			@Override
			public String getName() {
				return finalPackageName + "." + finalClassName;
			}

			@Override
			public String getClassName() {
				return finalClassName;
			}
		};
	}

	@Override
	public String getSourceName(Class<?> type) {
		if (type.isArray()) {
			return type.getComponentType().getName().replace('$', '.') + "[]";
		} else {
			return type.getName().replace('$', '.');
		}
	}

	@Override
	public ClassField createField(Class<?> fieldType, String fieldName) {
		return new ClassField() {

			@Override
			public Class<?> getFieldType() {
				return fieldType;
			}

			@Override
			public String getFieldName() {
				return fieldName;
			}
		};
	}

	@Override
	public void writeConstructor(Appendable appendable, String className, ClassField... fields) throws IOException {

		// Write out the fields
		for (ClassField field : fields) {
			appendable.append(
					"  private " + this.getSourceName(field.getFieldType()) + " " + field.getFieldName() + ";\n");
		}

		// Write constructor signature
		appendable.append("  public " + className + "(");
		for (int i = 0; i < fields.length; i++) {
			ClassField field = fields[i];
			if (i > 0) {
				appendable.append(", ");
			}
			appendable.append(this.getSourceName(field.getFieldType()) + " " + field.getFieldName());
		}
		appendable.append(") {\n");

		// Set state for fields
		for (ClassField field : fields) {
			appendable.append("    this." + field.getFieldName() + " = " + field.getFieldName() + ";\n");
		}

		// Complete constructor
		appendable.append("  }\n");
	}

	@Override
	public boolean writeMethodSignature(Appendable source, Method method) throws IOException {

		// Obtain details of the method
		boolean isReturn = isReturnValue(method);
		Class<?> returnType = method.getReturnType();
		Class<?>[] parameters = method.getParameterTypes();
		Class<?>[] exceptions = method.getExceptionTypes();

		// Write the method signature
		source.append((isReturn ? this.getSourceName(returnType) : "void") + " " + method.getName() + "(");
		for (int i = 0; i < parameters.length; i++) {
			if (i > 0) {
				source.append(", ");
			}
			source.append(this.getSourceName(parameters[i]));
			source.append(" p" + i);
		}
		source.append(")");
		if (exceptions.length > 0) {
			source.append(" throws ");
			for (int i = 0; i < exceptions.length; i++) {
				if (i > 0) {
					source.append(", ");
				}
				source.append(this.getSourceName(exceptions[i]));
			}
		}

		// Indicate if return value
		return isReturn;
	}

	@Override
	public void writeMethodImplementation(Appendable source, String delegate, Method method) throws IOException {
		this.writeDelegateMethodImplementation(source, null, delegate, method);
	}

	@Override
	public void writeDelegateMethodCall(Appendable source, String delegate, Method method) throws IOException {

		// Write the delegate method call
		source.append(delegate + "." + method.getName() + "(");
		Class<?>[] parameters = method.getParameterTypes();
		for (int i = 0; i < parameters.length; i++) {
			if (i > 0) {
				source.append(", ");
			}
			source.append("p" + i);
		}
		source.append(")");
	}

	/**
	 * Writes the delegate {@link Method} implementation.
	 * 
	 * @param source    {@link Appendable}.
	 * @param wrapClass Optional wrap {@link Class} to the returned delegate.
	 * @param delegate  Means to access the delegate.
	 * @param method    {@link Method}.
	 * @throws IOException If fails write delegate {@link Method} implementation.
	 */
	private void writeDelegateMethodImplementation(Appendable source, String wrapClass, String delegate, Method method)
			throws IOException {

		// Indicate if has return
		boolean isReturnValue = isReturnValue(method);

		// Write the default implementation
		source.append("    ");
		if (isReturnValue) {
			source.append("return ");
			if (wrapClass != null) {
				source.append("new " + wrapClass + "(");
			}
		}
		this.writeDelegateMethodCall(source, delegate, method);
		if (isReturnValue && (wrapClass != null)) {
			source.append(")");
		}
		source.append(";\n");
	}

	@Override
	public JavaSource addSource(String className, String source) {
		JavaSourceImpl javaSource = new JavaSourceImpl(className, source);
		this.sources.add(javaSource);
		return javaSource;
	}

	@Override
	public JavaSource addWrapper(Class<?>[] wrappingTypes, Class<?> delegateType, String delegateExtraction,
			ConstructorWriter constructorWriter, MethodWriter methodWriter, JavaSourceWriter... additionalSourceWriter)
			throws IOException {

		// Provide default delegate extraction
		if (delegateExtraction == null) {
			delegateExtraction = "this.delegate";
		}

		// Create the source
		StringWriter buffer = new StringWriter();
		PrintWriter source = new PrintWriter(buffer);

		// Determine package and class name
		ClassName className = this.createClassName(wrappingTypes[0].getName() + "Wrapper");

		// Write the initial class details
		source.println("package " + className.getPackageName() + ";");
		source.println(
				"@" + SuppressWarnings.class.getName() + "({\"all\",\"unchecked\",\"rawtypes\",\"deprecation\"})");
		source.print("public class " + className.getClassName() + " implements ");
		source.print(String.join(", ", Arrays.stream(wrappingTypes)
				.map((wrappingType) -> this.getSourceName(wrappingType)).toArray(String[]::new)));
		source.println(" {");

		// Provide constructor to delegate
		if (constructorWriter != null) {
			// Use custom constructor
			constructorWriter.write(new ConstructorWriterContext() {

				@Override
				public ClassName getClassName() {
					return className;
				}

				@Override
				public Appendable getSource() {
					return source;
				}
			});

		} else {
			// Use default constructor
			this.writeConstructor(source, className.getClassName(), this.createField(delegateType, "delegate"));
		}

		// Implement the various functions
		for (Class<?> wrappingType : wrappingTypes) {

			// Implement the methods
			NEXT_METHOD: for (Method method : wrappingType.getMethods()) {

				// Determine if method to implement
				if (method.isDefault() || Modifier.isStatic(method.getModifiers())) {
					continue NEXT_METHOD;
				}

				// Create the wrapper context for the method
				WrapperContextImpl methodContext = new WrapperContextImpl(wrappingType, method);
				if (methodWriter != null) {
					methodWriter.write(methodContext);
				}

				// Write the method signature
				source.print("  public ");
				this.writeMethodSignature(source, method);
				source.println(" {");

				// Determine if override implementation
				if (methodContext.overrideBuffer != null) {
					// Override implementation
					methodContext.override.flush();
					source.print(methodContext.overrideBuffer.toString());

				} else {
					// Write the default implementation
					this.writeDelegateMethodImplementation(source, methodContext.wrapClass, delegateExtraction, method);
				}

				// Complete method
				source.println("  }");
			}
		}

		// Write the additional wrappings
		for (JavaSourceWriter additional : additionalSourceWriter) {
			additional.write(new JavaSourceContext() {

				@Override
				public ClassName getClassName() {
					return className;
				}

				@Override
				public Appendable getSource() {
					return source;
				}
			});
		}

		// Complete class
		source.println("}");
		source.flush();

		// Add java source
		return this.addSource(className.getName(), buffer.toString());
	}

	/**
	 * {@link MethodWriterContext} implementation.
	 */
	private static class WrapperContextImpl implements MethodWriterContext {

		/**
		 * Interface.
		 */
		private final Class<?> interfaceClass;

		/**
		 * {@link Method}.
		 */
		private final Method method;

		/**
		 * Wrap {@link Class} name.
		 */
		private String wrapClass = null;

		/**
		 * Override implementation.
		 */
		private StringWriter overrideBuffer = null;

		/**
		 * {@link PrintWriter} for override implementation.
		 */
		private PrintWriter override = null;

		/**
		 * Instantiate.
		 * 
		 * @param interfaceClass Interface {@link Class}.
		 * @param method         {@link Method}.
		 */
		private WrapperContextImpl(Class<?> interfaceClass, Method method) {
			this.interfaceClass = interfaceClass;
			this.method = method;
		}

		/**
		 * Obtains the {@link PrintWriter} to override the implementation.
		 * 
		 * @return {@link PrintWriter} to override the implementation.
		 */
		private PrintWriter getWriter() {
			if (this.override == null) {
				this.overrideBuffer = new StringWriter();
				this.override = new PrintWriter(this.overrideBuffer);
			}
			return this.override;
		}

		/*
		 * =============== WrapperContext =================
		 */

		@Override
		public Class<?> getInterface() {
			return this.interfaceClass;
		}

		@Override
		public Method getMethod() {
			return this.method;
		}

		@Override
		public void setReturnWrapClass(String className) {
			this.wrapClass = className;
		}

		@Override
		public void setReturnWrapClass(JavaSource javaSource) {
			this.wrapClass = javaSource.getClassName();
		}

		@Override
		public void write(String source) {
			this.getWriter().print(source);
		}

		@Override
		public void writeln(String source) {
			this.getWriter().println(source);
		}

		@Override
		public Appendable getSource() {
			return this.getWriter();
		}
	}

	@Override
	public Map<JavaSource, Class<?>> compile() {

		// Create diagnostics to report errors
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

		// Undertake compiling
		JavaCompiler.CompilationTask task = this.javaCompiler.getTask(null, this.fileManager, diagnostics, null, null,
				this.sources);
		boolean isSuccessful = task.call();
		if ((!isSuccessful) || (diagnostics.getDiagnostics().size() > 0)) {
			StringBuilder msg = new StringBuilder();
			msg.append("Failed compiling");
			boolean isError = false;
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
				switch (diagnostic.getKind()) {
				case ERROR:
				default:
					isError = true;
					JavaFileObject failedSource = diagnostic.getSource();
					msg.append("\n\t " + failedSource.getName() + " line " + diagnostic.getLineNumber() + ": "
							+ diagnostic.getMessage(Locale.getDefault()));
					msg.append("\n\n");
					try {
						msg.append(failedSource.getCharContent(true));
					} catch (IOException ex) {
						msg.append("ERROR: failed to log remaining source content");
					}
					break;
				}
			}
			if (isError) {
				throw new CompileError(msg.toString());
			}
		}

		// Build and return the classes
		try {
			Map<JavaSource, Class<?>> classes = new HashMap<>();
			for (JavaSourceImpl javaSource : this.sources) {
				Class<?> clazz = this.compiledClassLoader.loadClass(javaSource.className);
				if (clazz == null) {
					throw new CompileError("Failed to compile class " + javaSource.className);
				}
				classes.put(javaSource, clazz);
			}
			return classes;

		} catch (Exception ex) {
			// Should not occur
			throw new RuntimeException(ex);
		}
	}

	/**
	 * {@link JavaSource} implementation.
	 */
	private class JavaSourceImpl extends SimpleJavaFileObject implements JavaSource {

		/**
		 * {@link Class} name.
		 */
		private final String className;

		/**
		 * Content.
		 */
		private final String contents;

		/**
		 * Instantiate.
		 * 
		 * @param className {@link Class} name.
		 * @param contents  Content.
		 */
		private JavaSourceImpl(String className, String contents) {
			super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.className = className;
			this.contents = contents;
		}

		/*
		 * ================ JavaFileObject =================
		 */

		@Override
		public String getClassName() {
			return className;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return contents;
		}

		/*
		 * ================== JavaSource ====================
		 */

		@Override
		public Class<?> compile() {
			return OfficeFloorJavaCompilerImpl.this.compile().get(this);
		}
	}

	/**
	 * {@link ClassLoader} implementation.
	 */
	private class CompiledClassLoader extends ClassLoader {

		/**
		 * Instantiate.
		 * 
		 * @param classLoader {@link ClassLoader}.
		 */
		private CompiledClassLoader(ClassLoader classLoader) {
			super(classLoader);
		}

		/*
		 * ============== ClassLoader ================
		 */

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {

			// Determine if compiled class
			ByteArrayOutputStream classDefinition = OfficeFloorJavaCompilerImpl.this.compiledClasses.get(name);
			if (classDefinition != null) {
				byte[] byteCode = classDefinition.toByteArray();
				return this.defineClass(name, byteCode, 0, byteCode.length);
			}

			// Delegate to parent
			return this.getParent().loadClass(name);
		}
	}

	/**
	 * {@link JavaFileManager}.
	 */
	private class OfficeFloorJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

		/**
		 * Instantiate.
		 * 
		 * @param fileManager {@link JavaFileManager}.
		 */
		protected OfficeFloorJavaFileManager(JavaFileManager fileManager) {
			super(fileManager);
		}

		/*
		 * ================== JavaFileManager ========================
		 */

		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling)
				throws IOException {
			try {
				ByteArrayOutputStream byteCode = new ByteArrayOutputStream();
				OfficeFloorJavaCompilerImpl.this.compiledClasses.put(className, byteCode);
				return new SimpleJavaFileObject(new URI(className), kind) {

					@Override
					public OutputStream openOutputStream() throws IOException {
						return byteCode;
					}
				};
			} catch (URISyntaxException ex) {
				throw new IOException(ex);
			}
		}

		@Override
		public ClassLoader getClassLoader(Location location) {

			// Override class loader
			if (StandardLocation.CLASS_PATH.name().equals(location.getName())) {
				return OfficeFloorJavaCompilerImpl.this.compiledClassLoader;
			}

			// Default class loader
			return super.getClassLoader(location);
		}
	}

}