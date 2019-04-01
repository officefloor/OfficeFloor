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
package net.officefloor.polyglot.javascript;

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
public class JavaScriptManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * {@link Property} name for the location of the JavaScript file on the class
	 * path.
	 */
	public static final String PROPERTY_JAVASCRIPT_PATH = "javascript.resource.path";

	/**
	 * {@link Property} name for the JavaScript function name.
	 */
	public static final String PROPERTY_FUNCTION_NAME = "function.name";

	/**
	 * {@link Property} name for the {@link ScriptEngine}.
	 */
	public static final String PROPERTY_ENGINE_NAME = "javascript.engine.name";

	/**
	 * {@link Property} name for the default {@link ScriptEngine}.
	 */
	public static final String DEFAULT_ENGINE_NAME = "graal.js";

	/**
	 * {@link ScriptEngineManager}.
	 */
	private static final ScriptEngineManager engineManager = new ScriptEngineManager();

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/*
	 * ===================== ManagedFunctionSource ===============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_JAVASCRIPT_PATH, "JavaScript Path");
		context.addProperty(PROPERTY_FUNCTION_NAME, "Function");
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the function name
		String functionName = context.getProperty(PROPERTY_FUNCTION_NAME);

		// Obtain the script engine
		String engineName = context.getProperty(PROPERTY_ENGINE_NAME, DEFAULT_ENGINE_NAME);
		ScriptEngine engine = engineManager.getEngineByName(engineName);

		// Ensure invocable
		if (!(engine instanceof Invocable)) {
			throw new Exception("Script engine " + engineName + " must be " + Invocable.class.getSimpleName());
		}
		Invocable invocable = (Invocable) engine;

		// Load the JavaScript contents
		String javaScriptPath = context.getProperty(PROPERTY_JAVASCRIPT_PATH);
		engine.eval(new InputStreamReader(context.getResource(javaScriptPath)));

		// Load the meta-data for the function
		StringWriter metaDataExtraction = new StringWriter();
		try (Reader metaDataReader = new InputStreamReader(
				context.getResource(JavaScriptManagedFunctionSource.class.getPackage().getName().replace('.', '/')
						+ "/OfficeFloorFunctionMetaData.js"))) {
			for (int character = metaDataReader.read(); character != -1; character = metaDataReader.read()) {
				metaDataExtraction.write(character);
			}
		}
		String metaDataExtractionScript = metaDataExtraction.toString().replace("_FUNCTION_NAME_", functionName);
		engine.eval(metaDataExtractionScript);
		String metaData = (String) invocable.invokeFunction("OFFICEFLOOR_METADATA_" + functionName);
		ScriptFunctionMetaData functionMetaData = mapper.readValue(metaData, ScriptFunctionMetaData.class);

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
		ManagedFunctionTypeBuilder<Indexed, Indexed> function = functionNamespaceTypeBuilder.addManagedFunctionType(
				functionName, new ScriptManagedFunction(invocable, functionName, parameterFactories), Indexed.class,
				Indexed.class);
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