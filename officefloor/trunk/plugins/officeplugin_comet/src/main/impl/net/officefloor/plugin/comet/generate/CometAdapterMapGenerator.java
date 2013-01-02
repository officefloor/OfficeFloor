/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.comet.api.CometSubscriber;
import net.officefloor.plugin.comet.internal.CometAdapter;
import net.officefloor.plugin.comet.internal.CometAdapterMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * Generates the {@link CometAdapterMap}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometAdapterMapGenerator extends Generator {

	@Override
	public String generate(TreeLogger logger, GeneratorContext context,
			String typeName) throws UnableToCompleteException {

		// Obtain the type
		TypeOracle oracle = context.getTypeOracle();
		JClassType type = oracle.findType(typeName);
		if (type == null) {
			logger.log(Type.ERROR,
					"Can not find " + JClassType.class.getSimpleName()
							+ " for " + typeName);
			throw new UnableToCompleteException();
		}

		// Obtain details to generate adapter class
		String packageName = type.getPackage().getName();
		String simpleName = type.getSimpleSourceName() + "Impl";
		String qualifiedName = packageName + "." + simpleName;
		logger.log(Type.TRACE,
				"Generating " + CometAdapterMap.class.getSimpleName()
						+ " implementation");

		// Obtain the CometListener type
		JClassType cometListenerType = oracle.findType(CometSubscriber.class
				.getName());
		if (cometListenerType == null) {
			logger.log(Type.ERROR,
					"Can not find " + JClassType.class.getSimpleName()
							+ " for " + CometSubscriber.class.getName());
			throw new UnableToCompleteException();
		}

		// Generate the map
		ClassSourceFileComposerFactory adapter = new ClassSourceFileComposerFactory(
				packageName, simpleName);
		adapter.addImplementedInterface(CometAdapterMap.class.getName());
		adapter.addImport(CometAdapterMap.class.getName());
		adapter.addImport(CometAdapter.class.getName());
		adapter.addImport(Map.class.getName());
		adapter.addImport(HashMap.class.getName());
		adapter.addImport(GWT.class.getName());
		PrintWriter src = context.tryCreate(logger, packageName, simpleName);
		if (src == null) {
			logger.log(Type.WARN, qualifiedName + " for "
					+ CometAdapterMap.class.getSimpleName()
					+ " implementation already generated.");
			return qualifiedName; // should already exist
		}
		SourceWriter writer = adapter.createSourceWriter(context, src);
		writer.println("@Override");
		writer.println("public Map<Class<?>, "
				+ CometAdapter.class.getSimpleName() + "> getMap() {");
		writer.println("    Map<Class<?>, "
				+ CometAdapter.class.getSimpleName()
				+ "> map = new HashMap<Class<?>, "
				+ CometAdapter.class.getSimpleName() + ">();");

		// Add all CometListener types to the map
		for (JClassType check : oracle.getTypes()) {

			// Determine if a CometListener type
			if (!(check.getFlattenedSupertypeHierarchy()
					.contains(cometListenerType))) {
				logger.log(Type.DEBUG, "Type " + check.getQualifiedSourceName()
						+ " not included in map for " + simpleName);
				continue; // ignore non CometListener type
			}

			// Ensure not the actual CometListener type
			if (cometListenerType.getQualifiedSourceName().equals(
					check.getQualifiedSourceName())) {
				logger.log(Type.DEBUG,
						"Not including " + check.getQualifiedSourceName()
								+ " in map for " + simpleName);
				continue; // ignore the CometListener
			}

			// Add to map
			String serviceTypeName = check.getQualifiedSourceName();
			logger.log(Type.TRACE, "Including " + serviceTypeName
					+ " in map for " + simpleName);
			writer.println("    map.put(" + serviceTypeName + ".class, ("
					+ CometAdapter.class.getSimpleName() + ") GWT.create("
					+ serviceTypeName + ".class));");
		}

		writer.println("    return map;");
		writer.println("}");

		// Committing
		logger.log(Type.TRACE,
				"Committing " + CometAdapterMap.class.getSimpleName()
						+ " implementation to have type created");
		writer.commit(logger);
		logger.log(Type.TRACE, CometAdapterMap.class.getSimpleName()
				+ " created");

		// Return adapter
		return qualifiedName;
	}
}