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

package net.officefloor.compile.impl.classes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Map;
import java.util.Set;
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

import net.officefloor.compile.classes.OfficeFloorClassPathScanner;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.frame.api.source.SourceContext;

/**
 * {@link OfficeFloorJavaCompiler} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorJavaCompilerImpl extends OfficeFloorJavaCompiler {

	/**
	 * {@link Class} name for the JavacProcessingEnvironment to confirm on class
	 * path to ensure can compile.
	 */
	public static final String JAVAC_PROCESSING_ENVIRONMENT_CLASS_NAME = "com.sun.tools.javac.processing.JavacProcessingEnvironment";

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
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext;

	/**
	 * {@link JavaCompiler}.
	 */
	private final JavaCompiler javaCompiler;

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
	 * @param sourceContext {@link SourceContext}.
	 * @throws ClassNotFoundException If missing {@link Class} instances for
	 *                                compiling.
	 */
	public OfficeFloorJavaCompilerImpl(SourceContext sourceContext) throws ClassNotFoundException {
		this.sourceContext = sourceContext;

		// Ensure compiler available
		ClassLoader classLoader = this.sourceContext.getClassLoader();
		Class<?> javacProcessingEnvironmentClass = classLoader.loadClass(JAVAC_PROCESSING_ENVIRONMENT_CLASS_NAME);
		if (javacProcessingEnvironmentClass == null) {
			throw new ClassNotFoundException(JAVAC_PROCESSING_ENVIRONMENT_CLASS_NAME);
		}

		// Load state
		this.compiledClassLoader = new CompiledClassLoader(classLoader);

		// Obtain the Java Compiler
		this.javaCompiler = ToolProvider.getSystemJavaCompiler();
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
		String delegate = (delegateExtraction == null) ? "this.delegate" : delegateExtraction;

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
			for (Method method : wrappingType.getMethods()) {
				this.writeWrapperMethod(delegate, method, methodWriter, wrappingType, source);
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
	 * Writes the wrapper {@link Method}.
	 * 
	 * @param delegateExtraction Delegate extraction.
	 * @param method             {@link Method}.
	 * @param methodWriter       {@link MethodWriter}.
	 * @param wrappingType       Wrapping type.
	 * @param source             Source.
	 * @throws IOException If fails to write the {@link Method}.
	 */
	private void writeWrapperMethod(String delegateExtraction, Method method, MethodWriter methodWriter,
			Class<?> wrappingType, PrintWriter source) throws IOException {

		// Determine if method to implement
		if (method.isDefault() || Modifier.isStatic(method.getModifiers())) {
			return;
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

		// Create in memory file manager
		OfficeFloorJavaFileManager fileManager = new OfficeFloorJavaFileManager(
				this.javaCompiler.getStandardFileManager(null, null, null), this.sourceContext);
		try {

			// Undertake compiling
			JavaCompiler.CompilationTask task = this.javaCompiler.getTask(null, fileManager, diagnostics, null, null,
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
						msg.append("\n\t " + (failedSource != null ? failedSource.getName() + " " : "") + "line "
								+ diagnostic.getLineNumber() + ": " + diagnostic.getMessage(null));
						msg.append("\n\n");
						try {
							if (failedSource != null) {
								msg.append(failedSource.getCharContent(true));
							}
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

		} finally {
			try {
				fileManager.close();
			} catch (Exception ex) {
				// Should not occur
				throw new RuntimeException(ex);
			}
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
		public Class<?> loadClass(String name) throws ClassNotFoundException {

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
		 * {@link OfficeFloorClassPathScanner}.
		 */
		private OfficeFloorClassPathScanner scanner;

		/**
		 * Instantiate.
		 * 
		 * @param fileManager {@link JavaFileManager}.
		 * @param context     {@link SourceContext}.
		 */
		protected OfficeFloorJavaFileManager(JavaFileManager fileManager, SourceContext context) {
			super(fileManager);

			// Create the class path scanner
			this.scanner = new OfficeFloorClassPathScanner(context);
		}

		/*
		 * ================== JavaFileManager ========================
		 */

		@Override
		public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
				throws IOException {

			// Obtain the default list
			List<JavaFileObject> javaFileObjects = new LinkedList<>();
			for (JavaFileObject javaFileObject : super.list(location, packageName, kinds, recurse)) {
				javaFileObjects.add(javaFileObject);
			}

			// Determine if able to list for package
			if (javaFileObjects.size() > 0) {
				return javaFileObjects;
			}

			// Attempt to scan classes from class path
			Set<String> classNames = this.scanner.scanClasses(packageName);
			for (String className : classNames) {

				// Obtain the class resource path
				String classResourcePath = className.replace('.', '/') + ".class";

				// Add the class
				try {
					javaFileObjects.add(new ClassLoadedJavaFileObject(className, Kind.CLASS) {
						@Override
						public InputStream openInputStream() throws IOException {
							return OfficeFloorJavaCompilerImpl.this.sourceContext.getClassLoader()
									.getResourceAsStream(classResourcePath);
						}
					});
				} catch (URISyntaxException ex) {
					// Should not occur
				}
			}

			// Return list
			return javaFileObjects;
		}

		@Override
		public String inferBinaryName(Location location, JavaFileObject file) {
			if (file instanceof ClassLoadedJavaFileObject) {
				return ((ClassLoadedJavaFileObject) file).className;
			} else {
				return super.inferBinaryName(location, file);
			}
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling)
				throws IOException {
			try {
				ByteArrayOutputStream byteCode = new ByteArrayOutputStream();
				OfficeFloorJavaCompilerImpl.this.compiledClasses.put(className, byteCode);
				return new ClassLoadedJavaFileObject(className, kind) {
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

			// Override class loader (to provide existing compiled classes)
			if (StandardLocation.CLASS_PATH.name().equals(location.getName())) {
				return OfficeFloorJavaCompilerImpl.this.compiledClassLoader;
			}

			// Default class loader
			return super.getClassLoader(location);
		}
	}

	/**
	 * Class loaded {@link JavaFileObject}.
	 */
	private static class ClassLoadedJavaFileObject extends SimpleJavaFileObject {

		/**
		 * {@link Class} name.
		 */
		private final String className;

		/**
		 * Instantiate.
		 * 
		 * @param className {@link Class} name.
		 * @param kind      {@link Kind}.
		 * @throws URISyntaxException If fails to create {@link URI} for {@link Class}
		 *                            name.
		 */
		public ClassLoadedJavaFileObject(String className, Kind kind) throws URISyntaxException {
			super(new URI(className), kind);
			this.className = className;
		}
	}

}
