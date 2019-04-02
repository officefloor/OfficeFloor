/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.polyglot.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.List;
import java.util.function.Function;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.plugin.managedfunction.clazz.ManagedFunctionInParameterFactory;
import net.officefloor.plugin.managedfunction.clazz.ManagedFunctionObjectParameterFactory;
import net.officefloor.plugin.managedfunction.clazz.ManagedFunctionOutParameterFactory;
import net.officefloor.plugin.managedfunction.clazz.ManagedFunctionParameterFactory;
import net.officefloor.plugin.managedfunction.clazz.ManagedFunctionValueParameterFactory;
import net.officefloor.plugin.managedfunction.clazz.ManagedFunctionVariableParameterFactory;
import net.officefloor.plugin.section.clazz.NextFunctionAnnotation;
import net.officefloor.plugin.section.clazz.ParameterAnnotation;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableAnnotation;
import net.officefloor.plugin.variable.VariableManagedObjectSource;

/**
 * {@link ManagedFunctionSource} for JavaScript function.
 * 
 * @author Daniel Sagenschneider
 */
public class ScriptManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * {@link Property} name for the {@link ScriptEngine}.
	 */
	public static final String PROPERTY_ENGINE_NAME = "javascript.engine.name";

	/**
	 * {@link Property} name for optional script file to load to setup.
	 */
	public static final String PROPERTY_SETUP_SCRIPT_PATH = "script.setup.path";

	/**
	 * {@link Property} name for the location of the Script file on the class path.
	 */
	public static final String PROPERTY_SCRIPT_PATH = "script.path";

	/**
	 * {@link Property} name for the location of the Script to extra meta-data for
	 * the function.
	 */
	public static final String PROPERTY_METADATA_SCRIPT_PATH = "script.metadata.path";

	/**
	 * {@link Property} name for the JavaScript function name.
	 */
	public static final String PROPERTY_FUNCTION_NAME = "function.name";

	/**
	 * {@link ScriptEngineManager}.
	 */
	private static final ScriptEngineManager engineManager = new ScriptEngineManager();

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

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
	 * ===================== ManagedFunctionSource ===============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_ENGINE_NAME, "Engine");
		context.addProperty(PROPERTY_SCRIPT_PATH, "Script Path");
		context.addProperty(PROPERTY_FUNCTION_NAME, "Function");
		context.addProperty(PROPERTY_METADATA_SCRIPT_PATH, "MetaData Script Path");
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the function name
		String functionName = context.getProperty(PROPERTY_FUNCTION_NAME);

		// Obtain the script engine
		String engineName = context.getProperty(PROPERTY_ENGINE_NAME);
		ScriptEngine engine = engineManager.getEngineByName(engineName);

		// Ensure invocable
		if (!(engine instanceof Invocable)) {
			throw new Exception("Script engine " + engineName + " must be " + Invocable.class.getSimpleName());
		}
		Invocable invocable = (Invocable) engine;

		// Load the setup script (if provided)
		String setupScriptPath = context.getProperty(PROPERTY_SETUP_SCRIPT_PATH, null);
		String setupScript = null;
		if (setupScriptPath != null) {
			setupScript = readContent(context.getResource(setupScriptPath));
			engine.eval(setupScript);
		}

		// Load the Script contents
		String scriptPath = context.getProperty(PROPERTY_SCRIPT_PATH);
		String script = readContent(context.getResource(scriptPath));
		engine.eval(script);

		// Load the meta-data for the function
		String metaDataScriptPath = context.getProperty(PROPERTY_METADATA_SCRIPT_PATH);
		String metaDataScript = readContent(context.getResource(metaDataScriptPath));
		metaDataScript = metaDataScript.toString().replace("_FUNCTION_NAME_", functionName);
		engine.eval(metaDataScript);
		Object metaData = invocable.invokeFunction("OFFICEFLOOR_METADATA_" + functionName);

		// Parse out the meta-data
		if (metaData == null) {
			throw new Exception("No meta-data provided for function " + functionName);
		} else if (!(metaData instanceof String)) {
			throw new Exception("Meta-data provide for function " + functionName + " must be JSON string ("
					+ metaData.getClass().getName() + ")");
		}
		String metaDataJsonString = (String) metaData;
		ScriptFunctionMetaData functionMetaData = mapper.readValue(metaDataJsonString, ScriptFunctionMetaData.class);

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
		Function<String, Class<?>> loadClass = (className) -> context.loadClass(translateClass.apply(className));

		// Add the function
		List<ScriptParameterMetaData> parameterMetaDatas = functionMetaData.getParameters();
		ManagedFunctionParameterFactory[] parameterFactories = new ManagedFunctionParameterFactory[parameterMetaDatas
				.size()];
		ManagedFunctionTypeBuilder<Indexed, Indexed> function = functionNamespaceTypeBuilder
				.addManagedFunctionType(functionName, new ScriptManagedFunction(engineManager, engineName, setupScript,
						script, functionName, parameterFactories), Indexed.class, Indexed.class);
		int objectIndex = 0;
		for (int i = 0; i < parameterMetaDatas.size(); i++) {
			ScriptParameterMetaData parameterMetaData = parameterMetaDatas.get(i);
			// Obtain the object details
			String qualifier = parameterMetaData.getQualifier();
			String typeName = parameterMetaData.getType();

			// Obtain type (reflection will not provide primitives)
			Class<?> type;
			final String arraySuffix = "[]";
			if (CompileUtil.isBlank(typeName)) {
				throw new IllegalStateException("No type configured for parameter " + i);

			} else if (typeName.endsWith(arraySuffix)) {
				// Load array
				String componentTypeName = typeName.substring(0, typeName.length() - arraySuffix.length());
				Class<?> componentType = context.loadClass(componentTypeName);
				type = Array.newInstance(componentType, 0).getClass();

			} else {
				// No array, so load class
				type = loadClass.apply(typeName);
			}

			// Obtain the nature
			String nature = parameterMetaData.getNature();
			if (nature == null) {
				nature = "object";
			}
			ManagedFunctionObjectTypeBuilder<Indexed> parameter;
			boolean isVariable;
			switch (nature) {

			case "parameter":
				// Add the parameter
				function.addAnnotation(new ParameterAnnotation(type, i));
				// Carry on to load object for parameter
			case "object":
				// Add the object
				parameterFactories[i] = new ManagedFunctionObjectParameterFactory(objectIndex++);
				parameter = function.addObject(type);
				if (qualifier != null) {
					parameter.setTypeQualifier(qualifier);
				}
				isVariable = false;
				break;

			case "val":
				parameterFactories[i] = new ManagedFunctionValueParameterFactory(objectIndex++);
				isVariable = true;
				break;

			case "in":
				parameterFactories[i] = new ManagedFunctionInParameterFactory(objectIndex++);
				isVariable = true;
				break;

			case "out":
				parameterFactories[i] = new ManagedFunctionOutParameterFactory(objectIndex++);
				isVariable = true;
				break;

			case "var":
				parameterFactories[i] = new ManagedFunctionVariableParameterFactory(objectIndex++);
				isVariable = true;
				break;

			default:
				// Unknown nature
				throw new IllegalStateException("Unknown nature " + nature + " for parameter " + i + " ("
						+ (qualifier == null ? "" : "qualifier=" + qualifier + ", ") + "type=" + typeName + ")");
			}

			// Configure variable
			if (isVariable) {
				String variableName = VariableManagedObjectSource.name(qualifier, typeName);
				parameter = function.addObject(Var.class);
				parameter.setTypeQualifier(variableName);
				parameter.addAnnotation(new VariableAnnotation(variableName));
			}
		}

		// Load the section annotations for the function
		ScriptNextFunctionMetaData nextFunction = functionMetaData.getNextFunction();
		if (nextFunction != null) {
			String argumentTypeName = nextFunction.getArgumentType();
			Class<?> argumentType = (argumentTypeName == null) ? null : context.loadClass(argumentTypeName);
			function.addAnnotation(new NextFunctionAnnotation(nextFunction.getName(), argumentType));
		}
	}

}