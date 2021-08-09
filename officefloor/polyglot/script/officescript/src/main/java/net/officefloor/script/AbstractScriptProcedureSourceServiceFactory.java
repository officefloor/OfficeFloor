/*-
 * #%L
 * PolyglotScript
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

package net.officefloor.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureManagedFunctionContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.impl.AsynchronousFlowClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.impl.ObjectClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.impl.VariableClassDependencyFactory;
import net.officefloor.plugin.section.clazz.ParameterAnnotation;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableAnnotation;
import net.officefloor.plugin.variable.VariableManagedObjectSource;
import net.officefloor.script.ScriptManagedFunction.ScriptEngineDecorator;
import net.officefloor.web.HttpCookieParameterAnnotation;
import net.officefloor.web.HttpHeaderParameterAnnotation;
import net.officefloor.web.HttpObjectAnnotation;
import net.officefloor.web.HttpParametersAnnotation;
import net.officefloor.web.HttpPathParameterAnnotation;
import net.officefloor.web.HttpQueryParameterAnnotation;

/**
 * {@link ProcedureSourceServiceFactory} providing abstract support for Scripts.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractScriptProcedureSourceServiceFactory implements ProcedureSourceServiceFactory {

	/**
	 * Obtains the source name for this {@link ProcedureSource}.
	 * 
	 * @return Source name for this {@link ProcedureSource}.
	 */
	protected abstract String getSourceName();

	/**
	 * Obtains the extensions for the script resource.
	 * 
	 * @param context {@link SourceContext}.
	 * @return Extensions for the script resource.
	 * @throws Exception If fails to obtain the file extensions.
	 */
	protected abstract String[] getScriptFileExtensions(SourceContext context) throws Exception;

	/**
	 * Obtains the name of the {@link ScriptEngine}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return Name of the {@link ScriptEngine}.
	 * @throws Exception If fails to obtain the name of the {@link ScriptEngine}.
	 */
	protected abstract String getScriptEngineName(SourceContext context) throws Exception;

	/**
	 * Enables overriding to decorate the {@link ScriptEngine}.
	 * 
	 * @param engine  {@link ScriptEngine}.
	 * @param context {@link SourceContext}.
	 * @throws Exception If fails to decorate the {@link ScriptEngine}.
	 */
	protected void decorateScriptEngine(ScriptEngine engine, SourceContext context) throws Exception {
		// No decoration by default
	}

	/**
	 * Obtains the path to the setup script.
	 * 
	 * @param context {@link SourceContext}.
	 * @return Path to the setup script or <code>null</code> if no setup.
	 * @throws Exception If fails to obtain the setup script path.
	 */
	protected String getSetupScriptPath(SourceContext context) throws Exception {
		return null;
	}

	/**
	 * Obtains the path to the script that extracts the function meta-data.
	 * 
	 * @param context {@link SourceContext}.
	 * @return Path to the script that extracts the function meta-data.
	 * @throws Exception If fails to obtain the path to the function meta-data
	 *                   script.
	 */
	protected abstract String getMetaDataScriptPath(SourceContext context) throws Exception;

	/**
	 * Obtains the {@link ScriptExceptionTranslator}.
	 * 
	 * @return {@link ScriptExceptionTranslator}.
	 */
	protected ScriptExceptionTranslator getScriptExceptionTranslator() {
		return null; // will be defaulted
	}

	/**
	 * Loads the setup script.
	 * 
	 * @param sourceContext {@link SourceContext}.
	 * @return Setup script or <code>null</code> if no setup.
	 * @throws Exception If fails to load setup script.
	 */
	protected String loadSetupScript(SourceContext sourceContext) throws Exception {
		String setupScriptPath = this.getSetupScriptPath(sourceContext);
		String setupScript = null;
		if (setupScriptPath != null) {
			setupScript = readContent(sourceContext.getResource(setupScriptPath));
		}
		return setupScript;
	}

	/**
	 * Loads the source script.
	 * 
	 * @param resource      Resource.
	 * @param sourceContext {@link SourceContext}.
	 * @return Resource script.
	 * @throws Exception If fails to load resource script.
	 */
	protected String loadResourceScript(String resource, SourceContext sourceContext) throws Exception {
		return readContent(sourceContext.getResource(resource));
	}

	/**
	 * Loads the {@link ScriptEngine}.
	 * 
	 * @param engineManager  {@link ScriptEngineManager}.
	 * @param engineName     Name of the {@link ScriptEngine}.
	 * @param setupScript    Setup script. May be <code>null</code> for no setup.
	 * @param script         Script containing the function(s).
	 * @param metaDataScript Script to extract meta-data.
	 * @param sourceContext  {@link SourceContext}.
	 * @return {@link Invocable} for the {@link ScriptEngine}.
	 * @throws Exception IF fails to load the {@link ScriptEngine}.
	 */
	protected Invocable loadScriptEngine(ScriptEngineManager engineManager, String engineName, String setupScript,
			String script, String metaDataScript, SourceContext sourceContext) throws Exception {

		// Obtain the script engine
		ScriptEngine engine = engineManager.getEngineByName(engineName);
		this.decorateScriptEngine(engine, sourceContext);

		// Ensure invocable
		if (!(engine instanceof Invocable)) {
			throw new IllegalStateException(
					"Script engine " + engineName + " must be " + Invocable.class.getSimpleName());
		}
		Invocable invocable = (Invocable) engine;

		// Load the setup script (if provided)
		if (setupScript != null) {
			engine.eval(setupScript);
		}

		// Load the Script contents
		engine.eval(script);

		// Load the meta-data for the function (if provided)
		if (metaDataScript != null) {
			engine.eval(metaDataScript);
		}

		// Return the invocable script engine
		return invocable;
	}

	/**
	 * Lists the {@link Procedure} instances.
	 * 
	 * @param context {@link ProcedureListContext}.
	 * @throws Exception If fails to list the {@link Procedure} instances.
	 */
	protected void listProcedures(ProcedureListContext context) throws Exception {

		// Obtain the resource
		String resource = context.getResource();

		// Determine if extension expected for script
		boolean isScriptResource = false;
		String[] extensions = this.getScriptFileExtensions(context.getSourceContext());
		if (extensions != null) {
			for (String extension : extensions) {
				if (!CompileUtil.isBlank(extension)) {
					String compareExtension = extension.startsWith(".") ? extension : "." + extension;
					if (resource.toLowerCase().endsWith(compareExtension.toLowerCase())) {
						isScriptResource = true;
					}
				}
			}
		}

		// As able to load script, able to attempt function
		if (isScriptResource) {
			context.addProcedure(null);
		}
	}

	/**
	 * Loads the {@link ManagedFunction} for the {@link Procedure}.
	 * 
	 * @param context {@link ProcedureManagedFunctionContext}.
	 * @throws Exception If fails to load the {@link ManagedFunction}.
	 */
	protected void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {

		// Obtain details to load script function
		String resource = context.getResource();
		String procedureName = context.getProcedureName();
		SourceContext sourceContext = context.getSourceContext();

		// Create engine manager specific to script
		ScriptEngineManager engineManager = new ScriptEngineManager(sourceContext.getClassLoader());

		// Obtain the engine name
		String engineName = this.getScriptEngineName(sourceContext);

		// Load scripts
		String setupScript = this.loadSetupScript(sourceContext);
		String script = this.loadResourceScript(resource, sourceContext);

		// Load the meta-data script for the function
		String metaDataScriptPath = this.getMetaDataScriptPath(sourceContext);
		String metaDataScript = readContent(sourceContext.getResource(metaDataScriptPath));
		metaDataScript = metaDataScript.replace("_FUNCTION_NAME_", procedureName);

		// Obtain the script engine
		Invocable invocable = this.loadScriptEngine(engineManager, engineName, setupScript, script, metaDataScript,
				sourceContext);

		// Obtain the function meta-data
		Object metaData = invocable.invokeFunction("OFFICEFLOOR_METADATA_" + procedureName);

		// Parse out the meta-data
		if (metaData == null) {
			throw new Exception("No meta-data provided for " + procedureName);
		} else if (!(metaData instanceof String)) {
			throw new Exception("Meta-data provided for " + procedureName + " must be JSON string ("
					+ metaData.getClass().getName() + ")");
		}
		String metaDataJsonString = (String) metaData;

		// Obtain the script meta-data
		ScriptFunctionMetaData functionMetaData = new ObjectMapper().readValue(metaDataJsonString,
				ScriptFunctionMetaData.class);

		// Ensure no error
		String error = functionMetaData.getError();
		if (!CompileUtil.isBlank(error)) {
			throw new Exception(error);
		}

		// Translates the class
		Function<String, String> translateClass = (className) -> {
			switch (className) {
			case "boolean":
				return Boolean.class.getName();
			case "byte":
				return Byte.class.getName();
			case "short":
				return Short.class.getName();
			case "char":
				return Character.class.getName();
			case "int":
				return Integer.class.getName();
			case "long":
				return Long.class.getName();
			case "float":
				return Float.class.getName();
			case "double":
				return Double.class.getName();
			default:
				return className;
			}
		};

		// Loads the class
		Function<String, Class<?>> loadClass = (className) -> sourceContext.loadClass(translateClass.apply(className));

		// Obtain the parameters (ensuring have list)
		List<ScriptParameterMetaData> parameterMetaDatas = functionMetaData.getParameters();
		if (parameterMetaDatas == null) {
			parameterMetaDatas = Collections.emptyList();
		}

		// Obtain the exception translator (defaulting to no translation)
		ScriptExceptionTranslator scriptExceptionTranslator = this.getScriptExceptionTranslator();
		if (scriptExceptionTranslator == null) {
			scriptExceptionTranslator = (ex) -> ex;
		}

		// Load the function
		ScriptEngineDecorator scriptEngineDecorator = (engineToDecorate) -> this.decorateScriptEngine(engineToDecorate,
				sourceContext);
		ClassDependencyFactory[] parameterFactories = new ClassDependencyFactory[parameterMetaDatas.size()];
		ManagedFunctionTypeBuilder<Indexed, Indexed> function = context
				.setManagedFunction(
						new ScriptManagedFunction(engineManager, engineName, scriptEngineDecorator, setupScript, script,
								procedureName, parameterFactories, scriptExceptionTranslator),
						Indexed.class, Indexed.class);

		// Load the parameters
		int objectIndex = 0;
		int flowIndex = 0;
		for (int i = 0; i < parameterMetaDatas.size(); i++) {
			ScriptParameterMetaData parameterMetaData = parameterMetaDatas.get(i);
			// Obtain the object details
			String parameterName = parameterMetaData.getName();
			String qualifier = parameterMetaData.getQualifier();
			String typeName = parameterMetaData.getType();

			// Obtain type (reflection will not provide primitives)
			Class<?> type;
			final String arraySuffix = "[]";
			if (CompileUtil.isBlank(typeName)) {
				// No type provided
				type = null;

			} else if (typeName.endsWith(arraySuffix)) {
				// Load array
				String componentTypeName = typeName.substring(0, typeName.length() - arraySuffix.length());
				Class<?> componentType = sourceContext.loadClass(componentTypeName);
				type = Array.newInstance(componentType, 0).getClass();

			} else {
				// No array, so load class
				type = loadClass.apply(typeName);
			}

			// Ease checking for type and name
			int parameterIndex = i;
			Runnable ensureHaveType = () -> {
				if (type == null) {
					throw new IllegalStateException("Must provide type for parameter " + parameterIndex
							+ " (with nature " + parameterMetaData.getNature() + ")");
				}
			};
			Runnable ensureHaveName = () -> {
				if (CompileUtil.isBlank(parameterName)) {
					throw new IllegalStateException("Must provide name for parameter " + parameterIndex
							+ " (with nature " + parameterMetaData.getNature() + ")");
				}
			};

			// Obtain the nature
			String nature = parameterMetaData.getNature();
			if (nature == null) {
				nature = "object";
			}
			ManagedFunctionObjectTypeBuilder<Indexed> parameter;
			boolean isVariable;
			switch (nature) {

			case "object":
			case "parameter":
				// Add the object
				ensureHaveType.run();
				parameterFactories[i] = new ObjectClassDependencyFactory(objectIndex++);
				parameter = function.addObject(type);
				if (qualifier != null) {
					// Use qualified type for name
					parameter.setLabel(qualifier + "-" + type.getName());
					parameter.setTypeQualifier(qualifier);
				} else {
					// Use type for name
					parameter.setLabel(type.getName());
				}
				if ("parameter".equals(nature)) {
					parameter.addAnnotation(new ParameterAnnotation());
				}
				isVariable = false;
				break;

			case "val":
				ensureHaveType.run();
				parameterFactories[i] = new VariableClassDependencyFactory(objectIndex++,
						VariableManagedObjectSource::val);
				isVariable = true;
				break;

			case "in":
				ensureHaveType.run();
				parameterFactories[i] = new VariableClassDependencyFactory(objectIndex++,
						VariableManagedObjectSource::in);
				isVariable = true;
				break;

			case "out":
				ensureHaveType.run();
				parameterFactories[i] = new VariableClassDependencyFactory(objectIndex++,
						VariableManagedObjectSource::out);
				isVariable = true;
				break;

			case "var":
				ensureHaveType.run();
				parameterFactories[i] = new VariableClassDependencyFactory(objectIndex++,
						VariableManagedObjectSource::var);
				isVariable = true;
				break;

			case "httpPathParameter":
				ensureHaveName.run();
				parameterFactories[i] = new ObjectClassDependencyFactory(objectIndex++);
				parameter = function.addObject(String.class);
				HttpPathParameterAnnotation httpPathParameter = new HttpPathParameterAnnotation(parameterName);
				parameter.addAnnotation(httpPathParameter);
				parameter.setTypeQualifier(httpPathParameter.getQualifier());
				isVariable = false;
				break;

			case "httpQueryParameter":
				ensureHaveName.run();
				parameterFactories[i] = new ObjectClassDependencyFactory(objectIndex++);
				parameter = function.addObject(String.class);
				HttpQueryParameterAnnotation httpQueryParameter = new HttpQueryParameterAnnotation(parameterName);
				parameter.addAnnotation(httpQueryParameter);
				parameter.setTypeQualifier(httpQueryParameter.getQualifier());
				isVariable = false;
				break;

			case "httpHeaderParameter":
				ensureHaveName.run();
				parameterFactories[i] = new ObjectClassDependencyFactory(objectIndex++);
				parameter = function.addObject(String.class);
				HttpHeaderParameterAnnotation httpHeaderParameter = new HttpHeaderParameterAnnotation(parameterName);
				parameter.addAnnotation(httpHeaderParameter);
				parameter.setTypeQualifier(httpHeaderParameter.getQualifier());
				isVariable = false;
				break;

			case "httpCookieParameter":
				ensureHaveName.run();
				parameterFactories[i] = new ObjectClassDependencyFactory(objectIndex++);
				parameter = function.addObject(String.class);
				HttpCookieParameterAnnotation httpCookieParameter = new HttpCookieParameterAnnotation(parameterName);
				parameter.addAnnotation(httpCookieParameter);
				parameter.setTypeQualifier(httpCookieParameter.getQualifier());
				isVariable = false;
				break;

			case "httpParameters":
				ensureHaveType.run();
				parameterFactories[i] = new ObjectClassDependencyFactory(objectIndex++);
				parameter = function.addObject(type);
				parameter.addAnnotation(new HttpParametersAnnotation());
				if (qualifier != null) {
					parameter.setTypeQualifier(qualifier);
				}
				isVariable = false;
				break;

			case "httpObject":
				ensureHaveType.run();
				parameterFactories[i] = new ObjectClassDependencyFactory(objectIndex++);
				parameter = function.addObject(type);
				parameter.addAnnotation(new HttpObjectAnnotation());
				if (qualifier != null) {
					parameter.setTypeQualifier(qualifier);
				}
				isVariable = false;
				break;

			case "flow":
				ensureHaveName.run();
				ManagedFunctionFlowTypeBuilder<?> flow = function.addFlow();
				flow.setLabel(parameterName);
				if (type != null) {
					flow.setArgumentType(type);
				}
				parameterFactories[i] = new ScriptFlowParameterFactory(flowIndex++);
				isVariable = false;
				break;

			case "asynchronousFlow":
				parameterFactories[i] = new AsynchronousFlowClassDependencyFactory();
				isVariable = false;
				break;

			default:
				// Unknown nature
				throw new IllegalStateException("Unknown nature " + nature + " for parameter " + i + " ("
						+ (qualifier == null ? "" : "qualifier=" + qualifier + ", ") + "type=" + typeName + ")");
			}

			// Configure variable
			if (isVariable) {
				String validTypeName = VariableManagedObjectSource.type(typeName);
				String variableName = VariableManagedObjectSource.name(qualifier, validTypeName);
				parameter = function.addObject(Var.class);
				parameter.setTypeQualifier(variableName);
				parameter.addAnnotation(new VariableAnnotation(variableName, validTypeName));
			}
		}

		// Load the section annotations for the function
		String nextArgumentType = functionMetaData.getNextArgumentType();
		if (nextArgumentType != null) {
			Class<?> argumentType = (nextArgumentType == null) ? null : sourceContext.loadClass(nextArgumentType);
			function.setReturnType(argumentType);
		}
	}

	/**
	 * Reads in the content.
	 * 
	 * @param content Content.
	 * @return Content as string.
	 * @throws IOException If fails to read content.
	 */
	private static String readContent(InputStream content) throws IOException {
		StringWriter buffer = new StringWriter();
		try (Reader reader = new InputStreamReader(content)) {
			for (int character = reader.read(); character != -1; character = reader.read()) {
				buffer.write(character);
			}
		}
		return buffer.toString();
	}

	/*
	 * ==================== ProcedureSourceServiceFactory ===================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return new ScriptProcedureSource();
	}

	/**
	 * {@link ProcedureSource} providing abstract support for Scripts.
	 */
	private class ScriptProcedureSource implements ManagedFunctionProcedureSource {

		/*
		 * ==================== ManagedFunctionProcedureSource ====================
		 */

		@Override
		public String getSourceName() {
			return AbstractScriptProcedureSourceServiceFactory.this.getSourceName();
		}

		@Override
		public void listProcedures(ProcedureListContext context) throws Exception {
			AbstractScriptProcedureSourceServiceFactory.this.listProcedures(context);
		}

		@Override
		public void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {
			AbstractScriptProcedureSourceServiceFactory.this.loadManagedFunction(context);
		}
	}

}
