/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.comet.generate;

import java.io.PrintWriter;

import net.officefloor.plugin.comet.api.CometListener;
import net.officefloor.plugin.comet.internal.CometListenerAdapter;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * Generates the {@link CometListenerAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometListenerAdapaterGenerator extends Generator {

	@Override
	public String generate(TreeLogger logger, GeneratorContext context,
			String typeName) throws UnableToCompleteException {

		final String COMET_LISTENER_CLASS_NAME = CometListener.class.getName();

		// Determine if the CometListener
		if (COMET_LISTENER_CLASS_NAME.equals(typeName)) {
			logger.log(Type.DEBUG, "Not generating for " + typeName);
			return typeName; // use CometListener as is
		}

		// Obtain the type
		TypeOracle oracle = context.getTypeOracle();
		JClassType type = oracle.findType(typeName);
		if (type == null) {
			logger.log(Type.ERROR,
					"Can not find " + JClassType.class.getSimpleName()
							+ " for " + typeName);
			throw new UnableToCompleteException();
		}

		// Ensure type has correct signature
		JMethod[] methods = type.getMethods();
		if (methods.length != 1) {
			logger.log(
					Type.ERROR,
					"Interface "
							+ type.getQualifiedSourceName()
							+ " must only have one method declared as the interface is marked with "
							+ CometListener.class.getSimpleName());
			throw new UnableToCompleteException();
		}
		JMethod method = methods[0];

		// Ensure method has only one parameter
		JParameter[] parameters = method.getParameters();
		if (parameters.length != 1) {
			logger.log(
					Type.ERROR,
					"Interface method "
							+ type.getQualifiedSourceName()
							+ "."
							+ method.getName()
							+ " must only have one parameter as the interface is marked with "
							+ CometListener.class.getSimpleName());
			throw new UnableToCompleteException();
		}
		JParameter parameter = parameters[0];

		// Ensure no throws on the method
		if (method.getThrows().length != 0) {
			logger.log(
					Type.ERROR,
					"Interface method "
							+ type.getQualifiedSourceName()
							+ "."
							+ method.getName()
							+ " must not throw exceptions as the interface is marked with "
							+ CometListener.class.getSimpleName());
			throw new UnableToCompleteException();
		}

		// Obtain details to generate adapter class
		String packageName = type.getPackage().getName();
		String simpleName = type.getSimpleSourceName() + "Adapter";
		String qualifiedName = packageName + "." + simpleName;
		String methodName = method.getName();
		String parameterType = parameter.getType().getQualifiedSourceName();
		logger.log(Type.TRACE,
				"Generating " + CometListener.class.getSimpleName()
						+ " Adapter for " + typeName + " [resulting class "
						+ qualifiedName + "]");

		// Generate the adapter
		ClassSourceFileComposerFactory adapter = new ClassSourceFileComposerFactory(
				packageName, simpleName);
		adapter.addImplementedInterface(CometListenerAdapter.class.getName());
		adapter.addImport(CometListenerAdapter.class.getName());
		PrintWriter src = context.tryCreate(logger, packageName, simpleName);
		if (src == null) {
			logger.log(Type.ERROR, "Unable to generate " + qualifiedName
					+ " for adapting " + CometListener.class.getSimpleName()
					+ " service. Likely cause is that class already exists.");
			throw new UnableToCompleteException();
		}
		SourceWriter writer = adapter.createSourceWriter(context, src);
		writer.println("@Override");
		writer.println("public void handleEvent(Object handler, Object event) {");
		writer.println("    ((" + typeName + ") handler)." + methodName + "(("
				+ parameterType + ") event);");
		writer.println("}");
		writer.commit(logger);

		// Return adapter
		return qualifiedName;
	}
}