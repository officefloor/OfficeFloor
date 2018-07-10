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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;

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
	public JavaSource addSource(String className, String source) {
		JavaSourceImpl javaSource = new JavaSourceImpl(className, source);
		this.sources.add(javaSource);
		return javaSource;
	}

	@Override
	public JavaSource addWrapper(Class<?> wrappedType, Class<?> delegateType, Consumer<WrapperContext> wrapperContext) {

		// Create the source
		StringWriter buffer = new StringWriter();
		PrintWriter source = new PrintWriter(buffer);

		// Determine package and class name
		String packageName = "net.officefloor.compiled." + wrappedType.getPackageName();
		String simpleName = wrappedType.getSimpleName() + "Wrapper"
				+ ThreadLocalRandom.current().nextInt(10000, 100000);
		String className = packageName + "." + simpleName;

		// Obtain the type names
		String wrappedTypeName = wrappedType.getName().replace('$', '.');
		String delegateTypeName = delegateType.getName().replace('$', '.');

		// Write the initial class details
		source.println("package " + packageName + ";");
		source.println("@" + SuppressWarnings.class.getName() + "({\"unchecked\", \"deprecation\"})");
		source.println("public class " + simpleName + " implements " + wrappedTypeName + " {");

		// Provide constructor to delegate
		source.println("  private final " + delegateTypeName + " delegate;");
		source.println("  public " + simpleName + "(" + delegateTypeName + " delegate) {");
		source.println("    this.delegate = delegate;");
		source.println("  }");

		// Implement the methods
		for (Method method : wrappedType.getMethods()) {

			// Create the wrapper context for the method
			WrapperContextImpl methodContext = new WrapperContextImpl(method);
			if (wrapperContext != null) {
				wrapperContext.accept(methodContext);
			}

			// Obtain details of the method
			Class<?> returnType = method.getReturnType();
			boolean isReturn = ((returnType != null) && (!Void.TYPE.equals(returnType)));
			Class<?>[] parameters = method.getParameterTypes();
			Class<?>[] exceptions = method.getExceptionTypes();

			// Obtains the type name
			Function<Class<?>, String> getTypeName = (type) -> {
				if (type.isArray()) {
					return type.getComponentType().getName().replace('$', '.') + "[]";
				} else {
					return type.getName().replace('$', '.');
				}
			};

			// Write the method signature
			source.print(
					"  public " + (isReturn ? getTypeName.apply(returnType) : "void") + " " + method.getName() + "(");
			for (int i = 0; i < parameters.length; i++) {
				if (i > 0) {
					source.print(", ");
				}
				source.print(getTypeName.apply(parameters[i]));
				source.print(" p" + i);
			}
			source.print(")");
			if (exceptions.length > 0) {
				source.print(" throws ");
				for (int i = 0; i < exceptions.length; i++) {
					if (i > 0) {
						source.print(", ");
					}
					source.print(getTypeName.apply(exceptions[i]));
				}
			}
			source.println(" {");

			// Determine if override implementation
			if (methodContext.overrideBuffer != null) {
				// Override implementation
				methodContext.override.flush();
				source.print(methodContext.overrideBuffer.toString());

			} else {
				// Write the default implementation
				source.print("    ");
				if (isReturn) {
					source.print("return ");
					if (methodContext.wrapClass != null) {
						source.print("new " + methodContext.wrapClass + "(");
					}
				}
				source.print("this.delegate." + method.getName() + "(");
				for (int i = 0; i < parameters.length; i++) {
					if (i > 0) {
						source.print(", ");
					}
					source.print("p" + i);
				}
				if (isReturn && (methodContext.wrapClass != null)) {
					source.print(")");
				}
				source.println(");");
			}

			// Complete method
			source.println("  }");
		}

		// Complete class
		source.println("}");
		source.flush();

		// Add java source
		return this.addSource(className, buffer.toString());
	}

	/**
	 * {@link WrapperContext} implementation.
	 */
	private static class WrapperContextImpl implements WrapperContext {

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
		 * @param method {@link Method}.
		 */
		private WrapperContextImpl(Method method) {
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
					msg.append("\n\t line " + diagnostic.getLineNumber() + ": "
							+ diagnostic.getMessage(Locale.getDefault()));
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